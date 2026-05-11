package co.edu.uceva.buildcheck.modules.materiales.controller;

import co.edu.uceva.buildcheck.modules.materiales.DTO.MaterialStockBajoDTO;
import co.edu.uceva.buildcheck.modules.materiales.service.MaterialService;
import co.edu.uceva.buildcheck.exception.RecursoNoEncontradoException;
import co.edu.uceva.buildcheck.modules.materiales.model.Material;
import co.edu.uceva.buildcheck.modules.proyectos.model.Proyecto;
import co.edu.uceva.buildcheck.modules.proyectos.repository.IProyectoRepository;
import co.edu.uceva.buildcheck.security.annotations.RequireProyectoAccess;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/materiales-service")
@CrossOrigin(origins = "*")
public class MaterialController {

    private final MaterialService materialService;
    private final IProyectoRepository proyectoRepository;

    private static final String MENSAJE = "mensaje";
    private static final String MATERIAL = "material";
    private static final String MATERIALES = "materiales";

    public MaterialController(MaterialService materialService, IProyectoRepository proyectoRepository) {
        this.materialService = materialService;
        this.proyectoRepository = proyectoRepository;
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
     * Listar materiales por proyecto
     */
    @GetMapping("/proyecto/{proyectoId}/materiales")
    @RequireProyectoAccess(projectIdParam = "proyectoId")
    public ResponseEntity<Map<String, Object>> getMaterialesByProyecto(@PathVariable Long proyectoId) {
        List<Material> materiales = materialService.findByProyectoId(proyectoId);
        Map<String, Object> response = new HashMap<>();
        response.put(MATERIALES, materiales);
        return ResponseEntity.ok(response);
    }

    /**
     * Crear un nuevo material en un proyecto
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
     * Crear un nuevo material en un proyecto específico
     */
    @PostMapping("/proyecto/{proyectoId}/materiales")
    @RequireProyectoAccess(projectIdParam = "proyectoId", allowedRoles = {"ROLE_OWNER", "ROLE_ADMIN", "ROLE_ALMACENISTA"})
    public ResponseEntity<Map<String, Object>> saveByProyecto(@PathVariable Long proyectoId, @Valid @RequestBody Material material) {
        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el proyecto con ID: " + proyectoId));
        material.setProyecto(proyecto);
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
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el material con ID: " + id));
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
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el material con ID: " + id));

        material.setId(id); // Aseguramos que se actualice el ID correcto
        Material materialActualizado = materialService.update(material);

        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "El material ha sido actualizado con éxito!");
        response.put(MATERIAL, materialActualizado);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar un material en un proyecto específico
     */
    @PutMapping("/proyecto/{proyectoId}/materiales/{id}")
    @RequireProyectoAccess(projectIdParam = "proyectoId", allowedRoles = {"ROLE_OWNER", "ROLE_ADMIN", "ROLE_ALMACENISTA"})
    public ResponseEntity<Map<String, Object>> updateByProyecto(@PathVariable Long proyectoId, @PathVariable Long id, @Valid @RequestBody Material material) {
        Material materialExistente = materialService.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el material con ID: " + id));
        
        // Verificar que el material pertenece al proyecto
        if (!materialExistente.getProyectoId().equals(proyectoId)) {
            throw new RecursoNoEncontradoException("El material no pertenece al proyecto especificado");
        }

        material.setId(id);
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
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el material con ID: " + id));

        materialService.delete(material);
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "El Material Ha sido eliminado con éxito!");
        return ResponseEntity.ok(response);
    }

    /**
     * Eliminar un material en un proyecto específico
     */
    @DeleteMapping("/proyecto/{proyectoId}/materiales/{id}")
    @RequireProyectoAccess(projectIdParam = "proyectoId", allowedRoles = {"ROLE_OWNER", "ROLE_ADMIN"})
    public ResponseEntity<Map<String, Object>> deleteByProyecto(@PathVariable Long proyectoId, @PathVariable Long id) {
        Material material = materialService.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el material con ID: " + id));

        // Verificar que el material pertenece al proyecto
        if (!material.getProyectoId().equals(proyectoId)) {
            throw new RecursoNoEncontradoException("El material no pertenece al proyecto especificado");
        }

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

    /**
     * Obtener alertas de materiales con stock bajo por proyecto
     */
    @GetMapping("/proyecto/{proyectoId}/alertas")
    @RequireProyectoAccess(projectIdParam = "proyectoId")
    public ResponseEntity<List<MaterialStockBajoDTO>> getAlertasByProyecto(@PathVariable Long proyectoId) {
        List<MaterialStockBajoDTO> alertas = materialService.obtenerAlertasStockBajoByProyecto(proyectoId);
        return ResponseEntity.ok(alertas);
    }
}