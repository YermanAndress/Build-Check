package co.edu.uceva.buildcheck.modules.proyectos.controller;

import co.edu.uceva.buildcheck.exception.NoHayDatosException;
import co.edu.uceva.buildcheck.exception.RecursoNoEncontradoException;
import co.edu.uceva.buildcheck.exception.ValidationException;
import co.edu.uceva.buildcheck.modules.proyectos.model.Proyecto;
import co.edu.uceva.buildcheck.modules.proyectos.service.IProyectoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/proyecto-service")
@CrossOrigin(origins = "*")
public class ProyectoRestController {
    private final IProyectoService proyectoService;

    private static final String MENSAJE = "mensaje";
    private static final String PROYECTO = "proyecto";
    private static final String PROYECTOS = "proyectos";

    public ProyectoRestController(IProyectoService proyectoService){
        this.proyectoService = proyectoService;
    }

    /**
     * Listar todos los proyectos
     */
    @GetMapping("/proyectos")
    public ResponseEntity<Map<String, Object>> getProyectos(){
        List<Proyecto> proyectos = proyectoService.findAll();
        if (proyectos.isEmpty()) {
            throw new NoHayDatosException("No hay proyectos en la base de datos");
        }
        Map<String, Object> response = new HashMap<>();
        response.put(PROYECTOS, proyectos);
        return ResponseEntity.ok(response);
    }

    /**
     * Crear un nuevo proyecto
     */
    @PostMapping("/proyectos")
    public ResponseEntity<Map<String, Object>> save(@Valid @RequestBody Proyecto proyecto, BindingResult result){
        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
        Map<String, Object> response = new HashMap<>();
        Proyecto nuevProyecto = proyectoService.save(proyecto);
        response.put(MENSAJE, "El proyecto ha sido creado con exito");
        response.put(PROYECTO, nuevProyecto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtener un proyecto por su ID
     */
    @GetMapping("/proyectos/{id}")
    public ResponseEntity<Map<String, Object>> findById(@PathVariable Long id){
        Proyecto proyecto = proyectoService.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el proyecto con ID: " + id));
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "El proyecto ha sido encontrado con exito");
        response.put(PROYECTO, proyecto);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar un proyecto
     */
    @PutMapping("/proyectos/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @Valid @RequestBody Proyecto proyecto, BindingResult result){
        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
        proyectoService.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el proyecto con ID: " + id));
        proyecto.setId(id);        
        Map<String, Object> response = new HashMap<>();
        Proyecto proyectoActualizado = proyectoService.update(proyecto);
        response.put(MENSAJE, "El proyecto ha sido actualizado con exito");
        response.put(PROYECTO, proyectoActualizado);
        return ResponseEntity.ok(response);
    }

    /**
     * Eliminar un proyecto por su ID
     */
    @DeleteMapping("/proyectos/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id){
        Proyecto proyecto = proyectoService.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("No existe el proyecto con ID: " + id));
        proyectoService.delete(proyecto);
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "El proyecto ha sido eliminado con exito");
        return ResponseEntity.ok(response);
    }
}