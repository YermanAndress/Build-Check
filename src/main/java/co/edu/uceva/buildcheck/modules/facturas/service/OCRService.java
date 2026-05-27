package co.edu.uceva.buildcheck.modules.facturas.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;

@Service
@Slf4j
public class OCRService {

    @Value("${GROQ_API_KEY}")
    private String groqApiKey;

    @Value("${GROQ_MODEL}")
    private String groqModel;

    @Value("${TESSDATA_PATH:./tessdata}")
    private String tessdataPath;

    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final Gson gson = new Gson();
    private static final int TIMEOUT_SECONDS = 15;

    public String extractTextFromImage(byte[] imageData) throws IOException {
        log.info("Iniciando extracción de texto con Tesseract OCR");

        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            if (image == null) {
                log.warn("No se pudo leer la imagen para OCR");
                return "";
            }

            BufferedImage processedImage = preprocessImage(image);
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(tessdataPath);
            tesseract.setLanguage("spa");

            String extractedText = tesseract.doOCR(processedImage);
            log.info("Texto extraído exitosamente. Longitud: {} caracteres", extractedText.length());
            return extractedText;
        } catch (TesseractException e) {
            log.error("Error ejecutando Tesseract OCR: {}", e.getMessage());
            throw new IOException("Error al ejecutar Tesseract OCR: " + e.getMessage(), e);
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
                MediaType.get("application/json; charset=utf-8"));

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
                - Si el numeroFactura no es claro, busca patrones como "Factura", "N°", "No.", "Nro" y toma el valor mas cercano, Solo el valor numerico
                - Para valorTotal, busca etiquetas como "TOTAL", "TOTAL A PAGAR", "IMPORTE TOTAL", "SUMA" y usa el numero mayor cercano
                - Si hay multiples fechas, usa la que este mas cerca de la palabra "Fecha"
                - Si proveedor no es claro, usa la razon social o el encabezado principal
                - Si hay una tabla de items, interpreta por columnas de izquierda a derecha: descripcion/material | cantidad | unidad | valor unitario | subtotal
                - Para cantidad, reconoce etiquetas como "cant", "cantidad", "#", "qty"
                - Para los (items), busca etiquetas como "detalle", "detalles", "descripcion", "descr", "producto", "servicios"
                - Para (items.valor unitario), reconoce "valor unitario", "vr unitario", "vr unidad", "p unit", "p/u", "unitario"
                - Los materiales (items.nombre) deben ser palabras o frases con sentido en contexto de obra (ej: cemento, arena, varilla, ladrillo, consumo, servicios)
                - Usa lenguaje claro en espanol y corrige palabras mal OCRizadas cuando sea evidente
                - Evita cadenas sin sentido; si no se puede inferir un material, usa null o un nombre simple coherente
                - Observaciones debe ser una frase corta y clara en espanol, eliminando caracteres basura

                Texto OCR:
                """
                + ocrText;
    }

    public void validateExtractedData(String jsonData) throws JsonSyntaxException {
        log.info("Validando datos extraídos");

        JsonObject json = gson.fromJson(jsonData, JsonObject.class);

        boolean hasContent = false;
        if (json.has("numeroFactura") && !json.get("numeroFactura").isJsonNull()) {
            hasContent = !json.get("numeroFactura").getAsString().isEmpty();
        }
        if (!hasContent && json.has("fecha") && !json.get("fecha").isJsonNull()) {
            hasContent = !json.get("fecha").getAsString().isEmpty();
        }
        if (!hasContent && json.has("proveedor") && !json.get("proveedor").isJsonNull()) {
            hasContent = !json.get("proveedor").getAsString().isEmpty();
        }
        if (!hasContent && json.has("observaciones") && !json.get("observaciones").isJsonNull()) {
            hasContent = !json.get("observaciones").getAsString().isEmpty();
        }
        if (!hasContent && json.has("valorTotal") && !json.get("valorTotal").isJsonNull()) {
            try {
                hasContent = json.get("valorTotal").getAsDouble() > 0;
            } catch (Exception ignored) {
                hasContent = false;
            }
        }
        if (!hasContent && json.has("items") && json.get("items").isJsonArray()) {
            hasContent = json.getAsJsonArray("items").size() > 0;
        }

        if (!hasContent) {
            throw new IllegalArgumentException("No se detectó factura válida");
        }

        log.info("Validación exitosa");
    }

    private BufferedImage preprocessImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        long total = 0;
        int count = width * height;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                int gray = (int) (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue());
                total += gray;
            }
        }

        int avg = count > 0 ? (int) (total / count) : 160;
        int threshold = Math.max(130, Math.min(200, avg));

        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                int gray = (int) (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue());
                int value = gray >= threshold ? 0xFFFFFF : 0x000000;
                output.setRGB(x, y, value);
            }
        }

        return output;
    }<<<<<<<HEAD
}=======}>>>>>>>ddff58b52976d9a456a04e5b1bbc480b5dc9a980
