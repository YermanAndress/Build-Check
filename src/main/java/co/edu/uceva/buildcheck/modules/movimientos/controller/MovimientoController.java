package co.edu.uceva.buildcheck.modules.movimientos.controller;

import co.edu.uceva.buildcheck.modules.materiales.model.Material;
import co.edu.uceva.buildcheck.modules.materiales.repository.MaterialRepository;
import co.edu.uceva.buildcheck.modules.movimientos.model.Movimiento;
import co.edu.uceva.buildcheck.modules.movimientos.service.MovimientoService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.HashMap;

@RestController
@RequestMapping("/api/movimientos-service")
@CrossOrigin(origins = "*")
public class MovimientoController {

    private final MovimientoService movimientoService;
    private final MaterialRepository materialRepository;

    private static final String MENSAJE     = "mensaje";
    private static final String MOVIMIENTO  = "movimiento";
    private static final String MOVIMIENTOS = "movimientos";

    public MovimientoController(MovimientoService movimientoService,
                                MaterialRepository materialRepository) {
        this.movimientoService  = movimientoService;
        this.materialRepository = materialRepository;
    }

    @GetMapping("/movimientos")
    public ResponseEntity<Map<String, Object>> getMovimientos() {
        List<Movimiento> movimientos = movimientoService.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put(MOVIMIENTOS, movimientos);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/movimientos")
    public ResponseEntity<Map<String, Object>> save(@Valid @RequestBody Movimiento movimiento) {
        // Si el frontend envió materialId, buscar y asignar el material
        if (movimiento.getMaterialId() != null) {
            Material material = materialRepository.findById(movimiento.getMaterialId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "No existe el material con ID: " + movimiento.getMaterialId()));
            movimiento.setMaterial(material);
        }

        Movimiento nuevo = movimientoService.save(movimiento);
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE,    "El movimiento ha sido creado con éxito!");
        response.put(MOVIMIENTO, nuevo);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/movimientos/{id}")
    public ResponseEntity<Map<String, Object>> findById(@PathVariable Long id) {
        Movimiento movimiento = movimientoService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe el movimiento con ID: " + id));
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE,    "El movimiento ha sido encontrado con éxito!");
        response.put(MOVIMIENTO, movimiento);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/movimientos/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id,
                                                      @Valid @RequestBody Movimiento movimiento) {
        movimientoService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe el movimiento con ID: " + id));

        if (movimiento.getMaterialId() != null) {
            Material material = materialRepository.findById(movimiento.getMaterialId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "No existe el material con ID: " + movimiento.getMaterialId()));
            movimiento.setMaterial(material);
        }

        movimiento.setId(id);
        Movimiento actualizado = movimientoService.update(movimiento);
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE,    "El movimiento ha sido actualizado con éxito!");
        response.put(MOVIMIENTO, actualizado);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/movimientos/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        Movimiento movimiento = movimientoService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe el movimiento con ID: " + id));
        movimientoService.delete(movimiento);
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "El movimiento ha sido eliminado con éxito!");
        return ResponseEntity.ok(response);
    }
}