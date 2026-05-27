package co.edu.uceva.buildcheck.modules.facturas.service;

import co.edu.uceva.buildcheck.modules.facturas.DTO.FacturaItemOCRResponse;
import co.edu.uceva.buildcheck.modules.facturas.DTO.FacturaOCRResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacturaOCRService {

    private final OCRService ocrService;
    private static final Gson gson = new Gson();

    public FacturaOCRResponse procesarImagenOCR(byte[] imageData, String fileName, Long proyectoId, Long usuarioId)
            throws IOException {
        log.info("Iniciando procesamiento OCR para imagen: {}", fileName);

        try {
            // 1. Extraer texto con Tesseract
            log.info("Paso 1/3: Extrayendo texto con Tesseract");
            String rawOCRText = ocrService.extractTextFromImage(imageData);
            if (rawOCRText.isEmpty()) {
                log.warn("Tesseract no extrajo texto");
                throw new IOException("No se detectó factura válida");
            }
            log.info("Texto extraído: {} caracteres", rawOCRText.length());

            // 2. Limpiar y estructurar con Groq
            log.info("Paso 2/3: Enviando a Groq para limpieza y estructuración");
            String cleanedJSON = ocrService.cleanAndStructureWithGroq(rawOCRText);
            log.info("JSON limpio recibido de Groq");
            log.info("Respuesta Groq (JSON): {}", cleanedJSON);

            // 3. Validar datos extraídos
            log.info("Paso 3/3: Validando datos extraídos");
            ocrService.validateExtractedData(cleanedJSON);

            // 4. Parsear y mapear al DTO
            FacturaOCRResponse response = parseOCRResponse(cleanedJSON, null, proyectoId, usuarioId);
            log.info("Procesamiento OCR completado exitosamente");
            return response;

        } catch (JsonSyntaxException e) {
            log.error("Error parseando JSON de Groq", e);
            throw new IOException("Error procesando respuesta de IA", e);
        }
    }

    private FacturaOCRResponse parseOCRResponse(String jsonData, String urlImagen, Long proyectoId, Long usuarioId) {
        log.info("Parseando respuesta OCR a FacturaOCRResponse");

        JsonObject json = gson.fromJson(jsonData, JsonObject.class);

        // Parsear número de factura
        String numeroFactura = json.has("numeroFactura") && !json.get("numeroFactura").isJsonNull()
                ? json.get("numeroFactura").getAsString()
                : "";

        // Parsear fecha
        LocalDate fecha = LocalDate.now();
        if (json.has("fecha") && !json.get("fecha").isJsonNull()) {
            try {
                String fechaStr = json.get("fecha").getAsString();
                fecha = LocalDate.parse(fechaStr, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception e) {
                log.warn("Error parseando fecha, usando fecha actual", e);
            }
        }

        // Parsear proveedor
        String proveedor = json.has("proveedor") && !json.get("proveedor").isJsonNull()
                ? json.get("proveedor").getAsString()
                : "";

        // Parsear valor total
        Double valorTotal = 0.0;
        if (json.has("valorTotal") && !json.get("valorTotal").isJsonNull()) {
            try {
                valorTotal = json.get("valorTotal").getAsDouble();
            } catch (Exception e) {
                log.warn("Error parseando valor total", e);
            }
        }

        // Parsear observaciones
        String observaciones = json.has("observaciones") && !json.get("observaciones").isJsonNull()
                ? json.get("observaciones").getAsString()
                : "";

        // Parsear items
        List<FacturaItemOCRResponse> items = new ArrayList<>();
        if (json.has("items") && json.get("items").isJsonArray()) {
            JsonArray itemsArray = json.getAsJsonArray("items");
            for (int i = 0; i < itemsArray.size(); i++) {
                JsonObject itemObj = itemsArray.get(i).getAsJsonObject();
                FacturaItemOCRResponse item = FacturaItemOCRResponse.builder()
                        .nombre(itemObj.has("nombre") && !itemObj.get("nombre").isJsonNull()
                                ? itemObj.get("nombre").getAsString()
                                : "")
                        .cantidad(itemObj.has("cantidad") && !itemObj.get("cantidad").isJsonNull()
                                ? itemObj.get("cantidad").getAsDouble()
                                : 0.0)
                        .precioUnitario(itemObj.has("precioUnitario") && !itemObj.get("precioUnitario").isJsonNull()
                                ? itemObj.get("precioUnitario").getAsDouble()
                                : 0.0)
                        .unidadMedida(itemObj.has("unidadMedida") && !itemObj.get("unidadMedida").isJsonNull()
                                ? itemObj.get("unidadMedida").getAsString()
                                : "UNIDAD")
                        .build();
                items.add(item);
            }
        }

        FacturaOCRResponse response = FacturaOCRResponse.builder()
                .numeroFactura(numeroFactura)
                .fecha(fecha)
                .proveedor(proveedor)
                .valorTotal(valorTotal)
                .observaciones(observaciones)
                .proyectoId(proyectoId)
                .usuarioId(usuarioId)
                .urlImagen(urlImagen)
                .items(items)
                .build();

        log.info("FacturaOCRResponse creado: {} items, total: {}", items.size(), valorTotal);
        return response;
    }
}
