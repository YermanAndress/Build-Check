package co.edu.uceva.buildcheck.modules.facturas.service;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OCRService {

    @Value("${GROQ_API_KEY}")
    private String groqApiKey;

    @Value("${GROQ_MODEL}")
    private String groqModel;
    
    @Value("${GOOGLE_APPLICATION_CREDENTIALS:./gcp-vision.json}")
    private String googleCredentialsPath;

    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final Gson gson = new Gson();
    private static final int TIMEOUT_SECONDS = 15;

    public String extractTextFromImage(byte[] imageData) throws IOException {
        log.info("Iniciando extracción de texto con Google Vision API");

        try {
            // Load credentials from file
            ServiceAccountCredentials credentials = ServiceAccountCredentials
                    .fromStream(new FileInputStream(googleCredentialsPath));
            
            // Create Vision client with credentials
            ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
                    .setCredentialsProvider(() -> credentials)
                    .build();
            
            try (ImageAnnotatorClient vision = ImageAnnotatorClient.create(settings)) {
                ByteString imgBytes = ByteString.copyFrom(imageData);
                Image img = Image.newBuilder().setContent(imgBytes).build();
                Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
                AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                        .addFeatures(feat)
                        .setImage(img)
                        .build();

                List<AnnotateImageRequest> requests = new ArrayList<>();
                requests.add(request);

                BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);
                List<AnnotateImageResponse> responses = response.getResponsesList();

                if (responses.isEmpty()) {
                    log.warn("No se detectó texto en la imagen");
                    return "";
                }

                AnnotateImageResponse res = responses.get(0);
                if (res.hasError()) {
                    log.error("Error en Vision API: {}", res.getError().getMessage());
                    return "";
                }

                TextAnnotation annotation = res.getFullTextAnnotation();
                String extractedText = annotation.getText();
                log.info("Texto extraído exitosamente. Longitud: {} caracteres", extractedText.length());

                return extractedText;
            }
        } catch (IOException e) {
            log.error("Error cargando credenciales de Google Cloud: {}", e.getMessage());
            throw new IOException("Error al acceder a Google Vision API: " + e.getMessage(), e);
        }
    }

    public String cleanAndStructureWithGroq(String rawOCRText) throws IOException {
        log.info("Enviando texto a Groq para limpieza y estructuración");

        if (rawOCRText == null || rawOCRText.trim().isEmpty()) {
            log.warn("Texto OCR vacío recibido en Groq");
            throw new IllegalArgumentException("No se detectó factura válida");
        }

        String prompt = buildGroqPrompt(rawOCRText);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .writeTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .readTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", groqModel);
        requestBody.addProperty("temperature", 0.3);
        requestBody.addProperty("max_tokens", 2000);

        com.google.gson.JsonArray messages = new com.google.gson.JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);
        messages.add(message);

        requestBody.add("messages", messages);

        RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(GROQ_API_URL)
                .header("Authorization", "Bearer " + groqApiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("Error en Groq API. Status: {}. Body: {}", response.code(), response.body());
                throw new IOException("Groq API error: " + response.code());
            }

            String responseBody = response.body().string();
            log.debug("Respuesta de Groq recibida");

            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            String content = jsonResponse
                    .getAsJsonArray("choices")
                    .get(0)
                    .getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content")
                    .getAsString();

            log.info("Respuesta de Groq procesada exitosamente");
            return content;
        }
    }

    private String buildGroqPrompt(String ocrText) {
        return """
                Extrae la siguiente información de este texto OCR de una factura y devuelve SOLO un JSON válido sin explicaciones adicionales:
                
                {
                  "numeroFactura": "número de la factura o número de documento",
                  "fecha": "fecha en formato yyyy-MM-dd, si no está disponible usa la fecha actual",
                  "proveedor": "nombre de la empresa o proveedor",
                  "valorTotal": número total de la factura,
                  "observaciones": "observaciones o notas adicionales, puede ser vacío",
                  "items": [
                    {
                      "nombre": "nombre del producto o servicio",
                      "cantidad": número de unidades,
                      "precioUnitario": precio por unidad,
                      "unidadMedida": "UND, MTS, KG, etc."
                    }
                  ]
                }
                
                IMPORTANTE:
                - Devuelve SOLO el JSON, sin markdown ni explicaciones
                - Si no encuentras un campo, déjalo como null
                - El array "items" debe estar vacío [] si no se encuentra información de productos
                - Los números deben ser valores numéricos, no strings
                - La fecha debe ser yyyy-MM-dd
                
                Texto OCR:
                """ + ocrText;
    }

    public void validateExtractedData(String jsonData) throws JsonSyntaxException {
        log.info("Validando datos extraídos");

        JsonObject json = gson.fromJson(jsonData, JsonObject.class);

        // Validar que no sea completamente vacío
        if (!json.has("numeroFactura") || json.get("numeroFactura").isJsonNull()) {
            throw new IllegalArgumentException("No se detectó factura válida");
        }

        // Validar que tenga al menos un campo con contenido
        boolean hasContent = json.entrySet().stream()
                .anyMatch(e -> !e.getValue().isJsonNull() && !e.getValue().getAsString().isEmpty());

        if (!hasContent) {
            throw new IllegalArgumentException("No se detectó factura válida");
        }

        log.info("Validación exitosa");
    }
}
