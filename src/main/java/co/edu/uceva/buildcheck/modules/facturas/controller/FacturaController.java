package co.edu.uceva.buildcheck.modules.facturas.controller;

import co.edu.uceva.buildcheck.modules.facturas.model.Factura;
import co.edu.uceva.buildcheck.modules.facturas.service.FacturaService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.HashMap;

@RestController
@RequestMapping("/api/facturas-service")
@CrossOrigin(origins = "*")
public class FacturaController {

    private final FacturaService facturaService;

    private static final String MENSAJE = "mensaje";
    private static final String FACTURA = "factura";
    private static final String FACTURAS = "facturas";

    public FacturaController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    /**
     * Listar todas las facturas
     */
    @GetMapping("/facturas")
    public ResponseEntity<Map<String, Object>> getFacturas() {
        List<Factura> facturas = facturaService.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put(FACTURAS, facturas);
        return ResponseEntity.ok(response);
    }

    /**
     * Crear una nueva factura
     */
    @PostMapping("/facturas")
    public ResponseEntity<Map<String, Object>> save(@Valid @RequestBody Factura factura) {
        Factura nuevoFactura = facturaService.save(factura);
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "La factura ha sido creado con éxito!");
        response.put(FACTURA, nuevoFactura);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtener una factura por su ID
     */
    @GetMapping("/facturas/{id}")
    public ResponseEntity<Map<String, Object>> findById(@PathVariable Long id) {
        Factura producto = facturaService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe la factura con el ID: " + id));
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "La factura ha sido encontrado con éxito!");
        response.put(FACTURA, producto);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar un factura
     */
    @PutMapping("/facturas/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @Valid @RequestBody Factura factura) {
        facturaService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe la factura con el ID: " + id));

        factura.setId(id);
        Factura facturaActualizado = facturaService.update(factura);
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
        Factura factura = facturaService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe la factura con el ID: " + id));

        facturaService.delete(factura);
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "La Factura Ha sido eliminado con exito");
        return ResponseEntity.ok(response);
    }
}