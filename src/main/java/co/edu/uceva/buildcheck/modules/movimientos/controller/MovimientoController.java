package co.edu.uceva.buildcheck.modules.movimientos.controller;

import co.edu.uceva.buildcheck.exception.RecursoNoEncontradoException;
import co.edu.uceva.buildcheck.modules.movimientos.DTO.MovimientoRequest;
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

    private static final String MENSAJE = "mensaje";
    private static final String MOVIMIENTO = "movimiento";
    private static final String MOVIMIENTOS = "movimientos";

    public MovimientoController(MovimientoService movimientoService) {
        this.movimientoService = movimientoService;
    }

    /**
     * Listar todos los movimientos
     */
    @GetMapping("/movimientos")
    public ResponseEntity<Map<String, Object>> getMovimientos() {
        List<Movimiento> movimientos = movimientoService.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put(MOVIMIENTOS, movimientos);
        return ResponseEntity.ok(response);
    }

    /**
     * Crear un nuevo movimiento
     */
    @PostMapping("/movimientos")
    public ResponseEntity<?> save(@RequestBody MovimientoRequest movimientoRequest) {
        Movimiento nuevoMovimiento = movimientoService.save(movimientoRequest);

        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "El movimiento ha sido creado con éxito!");
        response.put(MOVIMIENTO, nuevoMovimiento);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtener un movimiento por ID
     */
    @GetMapping("/movimientos/{id}")
    public ResponseEntity<Map<String, Object>> findById(@PathVariable Long id) {

        Movimiento movimiento = movimientoService.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el movimiento con ID: " + id));

        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "El movimiento ha sido encontrado con éxito!");
        response.put(MOVIMIENTO, movimiento);

        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar un movimiento
     */
    @PutMapping("/movimientos/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody MovimientoRequest movimiento) {
        Movimiento actualizado = movimientoService.update(id, movimiento);

        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "El movimiento ha sido actualizado con éxito!");
        response.put(MOVIMIENTO, actualizado);

        return ResponseEntity.ok(response);
    }

    /**
     * Eliminar un movimiento
     */
    @DeleteMapping("/movimientos/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {

        Movimiento movimiento = movimientoService.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el movimiento con ID: " + id));

        movimientoService.delete(movimiento);

        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "El movimiento ha sido eliminado con éxito!");

        return ResponseEntity.ok(response);
    }
}