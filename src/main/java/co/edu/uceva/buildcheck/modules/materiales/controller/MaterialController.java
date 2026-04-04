package co.edu.uceva.buildcheck.modules.materiales.controller;

import co.edu.uceva.buildcheck.modules.materiales.DTO.MaterialStockBajoDTO;
import co.edu.uceva.buildcheck.modules.materiales.service.MaterialService;
import co.edu.uceva.buildcheck.modules.materiales.model.Material;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.NoSuchElementException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/materiales-service")
@CrossOrigin(origins = "*")
public class MaterialController {

    private final MaterialService materialService;

    private static final String MENSAJE = "mensaje";
    private static final String MATERIAL = "material";
    private static final String MATERIALES = "materiales";

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    /**
     * Listar todos los materiales
     */
    @GetMapping("/materiales")
    public ResponseEntity<Map<String, Object>> getMaterials() {
        List<Material> materiales = materialService.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put(MATERIALES, materiales);
        return ResponseEntity.ok(response);
    }

    /**
     * Crear un nuevo material
     */
    @PostMapping("/materiales")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENISTA')")
    public ResponseEntity<Map<String, Object>> save(@Valid @RequestBody Material material) {
        Material nuevoMaterial = materialService.save(material);
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "El material ha sido creado con éxito!");
        response.put(MATERIAL, nuevoMaterial);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtener un material por su ID
     */
    @GetMapping("/materiales/{id}")
    public ResponseEntity<Map<String, Object>> findById(@PathVariable Long id) {
        Material producto = materialService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe el material con ID: " + id));
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "El material ha sido encontrado con éxito!");
        response.put(MATERIAL, producto);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar un material
     */
    @PutMapping("/materiales/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACENISTA')")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @Valid @RequestBody Material material) {
        materialService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe el material con ID: " + id));

        material.setId(id); // Aseguramos que se actualice el ID correcto
        Material materialActualizado = materialService.update(material);

        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "El material ha sido actualizado con éxito!");
        response.put(MATERIAL, materialActualizado);
        return ResponseEntity.ok(response);
    }

    /**
     * Eliminar un material por su ID
     */
    @DeleteMapping("/materiales/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        Material material = materialService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe el material con ID: " + id));

        materialService.delete(material);
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "El Material Ha sido eliminado con éxito!");
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener alertas de materiales con stock bajo
     */
    @GetMapping("/alertas")
    public ResponseEntity<List<MaterialStockBajoDTO>> getAlertas() {
        List<MaterialStockBajoDTO> alertas = materialService.obtenerAlertasStockBajo();
        return ResponseEntity.ok(alertas);
    }
}