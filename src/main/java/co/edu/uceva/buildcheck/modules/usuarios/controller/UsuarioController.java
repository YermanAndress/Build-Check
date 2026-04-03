package co.edu.uceva.buildcheck.modules.usuarios.controller;

import co.edu.uceva.buildcheck.modules.usuarios.model.Usuario;
import co.edu.uceva.buildcheck.modules.usuarios.service.UsuarioService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.HashMap;

@RestController
@RequestMapping("/api/usuarios-service")
@CrossOrigin(origins = "*")
public class UsuarioController {
    private final UsuarioService usuarioService;

    private static final String MENSAJE = "mensaje";
    private static final String USUARIO = "usuario";
    private static final String USUARIOS = "usuarios";

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Listar todos los usuarios
     */
    @GetMapping("/usuarios")
    public ResponseEntity<Map<String, Object>> getUsuarios() {
        List<Usuario> usuarios = usuarioService.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put(USUARIOS, usuarios);
        return ResponseEntity.ok(response);
    }

    /**
     * Crear un nuevo usuario
     */
    @PostMapping("/usuarios")
    public ResponseEntity<Map<String, Object>> save(@Valid @RequestBody Usuario usuario) {
        Usuario nuevoUsuario = usuarioService.save(usuario);
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "El usuario ha sido creado con éxito!");
        response.put(USUARIO, nuevoUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtener un usuario por su ID
     */
    @GetMapping("/usuarios/{id}")
    public ResponseEntity<Map<String, Object>> findById(@PathVariable Long id) {
        Usuario usuario = usuarioService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe el usuario con ID: " + id));
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "El usuario ha sido encontrado con éxito!");
        response.put(USUARIO, usuario);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar un usuario
     */
    @PutMapping("/usuarios/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @Valid @RequestBody Usuario usuario) {
        usuarioService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe el usuario con ID: " + id));

        usuario.setId(id); // Aseguramos que se actualice el ID correcto
        Usuario usuarioActualizado = usuarioService.update(usuario);

        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "El usuario ha sido actualizado con éxito!");
        response.put(USUARIO, usuarioActualizado);
        return ResponseEntity.ok(response);
    }

    /**
     * Eliminar un usuario por su ID
     */
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        Usuario usuario = usuarioService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe el usuario con ID: " + id));

        usuarioService.delete(usuario);
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "El Usuario Ha sido eliminado con éxito!");
        return ResponseEntity.ok(response);
    }
}