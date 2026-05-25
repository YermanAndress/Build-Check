package co.edu.uceva.buildcheck.modules.clasificador.controller;

import co.edu.uceva.buildcheck.modules.clasificador.service.ClasificadorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/clasificador")
@CrossOrigin(origins = "*")
public class ClasificadorController {

    private final ClasificadorService clasificadorService;

    public ClasificadorController(ClasificadorService clasificadorService) {
        this.clasificadorService = clasificadorService;
    }

    @PostMapping("/validar")
    public ResponseEntity<?> validar(
            @RequestParam("file") MultipartFile file,
            @RequestParam("materialEsperado") String materialEsperado) {
        try {
            Map<String, Object> resultado =
                clasificadorService.validar(file.getBytes(), materialEsperado);
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al procesar: " + e.getMessage()));
        }
    }

    @PostMapping("/clasificar")
    public ResponseEntity<?> clasificar(
            @RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> resultado =
                clasificadorService.clasificar(file.getBytes());
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al procesar: " + e.getMessage()));
        }
    }
}