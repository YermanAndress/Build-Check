package co.edu.uceva.buildcheck.modules.facturas.controller;

import co.edu.uceva.buildcheck.exception.RecursoNoEncontradoException;
import co.edu.uceva.buildcheck.modules.facturas.DTO.FacturaDTO;
import co.edu.uceva.buildcheck.modules.facturas.DTO.FacturaOCRResponse;
import co.edu.uceva.buildcheck.modules.facturas.DTO.FacturaRequest;
import co.edu.uceva.buildcheck.modules.facturas.model.Factura;
import co.edu.uceva.buildcheck.modules.facturas.service.FacturaService;
import co.edu.uceva.buildcheck.modules.facturas.service.FacturaOCRService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/facturas-service")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class FacturaController {

    private final FacturaService facturaService;
    private final FacturaOCRService facturaOCRService;
    private final ObjectMapper objectMapper;

    private static final String MENSAJE = "mensaje";
    private static final String FACTURA = "factura";
    private static final String FACTURAS = "facturas";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * Listar todas las facturas
     */
    @GetMapping("/facturas")
    public ResponseEntity<Map<String, Object>> getFacturas() {
        List<FacturaDTO> facturas = facturaService
                .findAll()
                .stream()
                .map(facturaService::toDTO)
                .toList();
        Map<String, Object> response = new HashMap<>();
        response.put(FACTURAS, facturas);
        return ResponseEntity.ok(response);
    }

    /**
     * Crear una nueva factura
     */
    @PostMapping("/facturas")
    public ResponseEntity<Map<String, Object>> save(
            @RequestBody FacturaRequest factura) {
        Factura nuevoFactura = facturaService.save(factura);
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "La factura ha sido creado con éxito!");
        response.put(FACTURA, nuevoFactura);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/facturas/with-image", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> saveWithImage(
            @RequestParam("file") MultipartFile file,
            @RequestPart("factura") String facturaJson) {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("Guardando factura con imagen - archivo: {}, size: {}", file.getOriginalFilename(),
                    file.getSize());
            log.info("Payload factura JSON: {}", facturaJson);

            if (file.isEmpty()) {
                response.put(MENSAJE, "El archivo no puede estar vacío");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            String contentType = file.getContentType();
            if (!isValidImageType(contentType)) {
                response.put(MENSAJE, "Formato de archivo inválido. Solo se permiten JPG y PNG");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                response.put(MENSAJE, "El archivo no puede exceder 5MB");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            FacturaRequest factura;
            try {
                factura = objectMapper.readValue(facturaJson, FacturaRequest.class);
            } catch (Exception e) {
                log.error("Error parseando factura JSON", e);
                response.put(MENSAJE, "Error parseando datos de factura");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            Factura nuevoFactura = facturaService.saveWithImage(
                    factura,
                    file.getBytes(),
                    file.getOriginalFilename());

            response.put(MENSAJE, "La factura ha sido creado con éxito!");
            response.put(FACTURA, nuevoFactura);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error al guardar factura con imagen", e);
            response.put(MENSAJE, "Error al guardar la factura con imagen: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/facturas/{id}/image-url")
    public ResponseEntity<Map<String, Object>> getFacturaImageUrl(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Factura factura = facturaService.findById(id)
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "No existe la factura con el ID: " + id));

            String imagePath = factura.getUrlImagen();
            if (imagePath == null || imagePath.isEmpty()) {
                response.put(MENSAJE, "La factura no tiene imagen asociada");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            String signedUrl = facturaService.getSignedImageUrl(imagePath);
            response.put("url", signedUrl);
            return ResponseEntity.ok(response);
        } catch (RecursoNoEncontradoException e) {
            response.put(MENSAJE, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("Error generando URL firmada", e);
            response.put(MENSAJE, "Error al generar URL de imagen");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Obtener una factura por su ID
     */
    @GetMapping("/facturas/{id}")
    public ResponseEntity<Map<String, Object>> findById(@PathVariable Long id) {
        Factura producto = facturaService
                .findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe la factura con el ID: " + id));
        FacturaDTO facturaDTO = facturaService.toDTO(producto);
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "La factura ha sido encontrado con éxito!");
        response.put(FACTURA, facturaDTO);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar un factura
     */
    @PutMapping("/facturas/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @Valid @RequestBody FacturaRequest factura) {
        Factura facturaActualizado = facturaService.update(id, factura);
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "La factura ha sido actualizado con exito");
        response.put(FACTURA, facturaActualizado);
        return ResponseEntity.ok(response);
    }

    /**
     * Eliminar un factura por su ID
     */
    @DeleteMapping("/facturas/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        Factura factura = facturaService
                .findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe la factura con el ID: " + id));

        facturaService.delete(factura);
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "La Factura Ha sido eliminado con exito");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/proyecto/{proyectoId}/facturas")
    public ResponseEntity<Map<String, Object>> getFacturasByProyecto(
            @PathVariable Long proyectoId) {
        List<FacturaDTO> facturas = facturaService
                .findByProyectoId(proyectoId)
                .stream()
                .map(facturaService::toDTO)
                .toList();
        Map<String, Object> response = new HashMap<>();
        response.put(FACTURAS, facturas);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint OCR: Procesa imagen de factura con Tesseract + Groq
     * POST /api/facturas-service/ocr
     * 
     * Parameters:
     * - file: MultipartFile (jpg/png, max 5MB)
     * - proyectoId: Long
     * - usuarioId: Long
     */
    @PostMapping(value = "/ocr", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> processOCR(
            @RequestParam("file") MultipartFile file,
            @RequestParam("proyectoId") Long proyectoId,
            @RequestParam("usuarioId") Long usuarioId) {

        log.info("Endpoint OCR llamado - archivo: {}, proyecto: {}, usuario: {}",
                file.getOriginalFilename(), proyectoId, usuarioId);

        Map<String, Object> response = new HashMap<>();

        try {
            // Validación: archivo vacío
            if (file.isEmpty()) {
                log.warn("Archivo vacío recibido");
                response.put(MENSAJE, "El archivo no puede estar vacío");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Validación: tipo de archivo
            String contentType = file.getContentType();
            if (!isValidImageType(contentType)) {
                log.warn("Tipo de archivo inválido: {}", contentType);
                response.put(MENSAJE, "Formato de archivo inválido. Solo se permiten JPG y PNG");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Validación: tamaño máximo
            if (file.getSize() > MAX_FILE_SIZE) {
                log.warn("Archivo muy grande: {} bytes", file.getSize());
                response.put(MENSAJE, "El archivo no puede exceder 5MB");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Procesar OCR
            log.info("Iniciando procesamiento OCR...");
            FacturaOCRResponse ocrResponse = facturaOCRService.procesarImagenOCR(
                    file.getBytes(),
                    file.getOriginalFilename(),
                    proyectoId,
                    usuarioId);

            log.info("OCR completado exitosamente");
            response.put(MENSAJE, "Factura procesada exitosamente");
            response.put("factura", ocrResponse);
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("Validación fallida: {}", e.getMessage());
            response.put(MENSAJE, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(response);
        } catch (IOException e) {
            log.error("Error procesando OCR", e);
            response.put(MENSAJE, "Error al procesar la imagen");
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(response);
        } catch (Exception e) {
            log.error("Error inesperado en OCR", e);
            response.put(MENSAJE, "Error interno del servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private boolean isValidImageType(String contentType) {
        return contentType != null && (contentType.equals("image/jpeg") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/png"));
    }
}