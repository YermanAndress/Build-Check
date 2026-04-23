package co.edu.uceva.buildcheck.modules.facturas.controller;

import co.edu.uceva.buildcheck.modules.facturas.service.FacturaService;
import co.edu.uceva.buildcheck.exception.RecursoNoEncontradoException;
import co.edu.uceva.buildcheck.modules.facturas.DTO.FacturaRequest;
import co.edu.uceva.buildcheck.modules.facturas.DTO.FacturaDTO;
import co.edu.uceva.buildcheck.modules.facturas.model.Factura;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        List<FacturaDTO> facturas = facturaService.findAll()
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
    public ResponseEntity<Map<String, Object>> save(@RequestBody FacturaRequest factura) {
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
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la factura con el ID: " + id));
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
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody FacturaRequest factura) {
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
        Factura factura = facturaService.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la factura con el ID: " + id));

        facturaService.delete(factura);
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "La Factura Ha sido eliminado con exito");
        return ResponseEntity.ok(response);
    }
}