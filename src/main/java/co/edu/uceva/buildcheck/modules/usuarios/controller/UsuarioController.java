package co.edu.uceva.buildcheck.modules.usuarios.controller;

import co.edu.uceva.buildcheck.modules.usuarios.service.UsuarioService;
import co.edu.uceva.buildcheck.modules.usuarios.login.GenerarPassword;
import co.edu.uceva.buildcheck.exception.RecursoNoEncontradoException;
import co.edu.uceva.buildcheck.modules.usuarios.login.EmailService;
import co.edu.uceva.buildcheck.modules.usuarios.login.LoginRequest;
import co.edu.uceva.buildcheck.modules.usuarios.model.Usuario;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con ID: " + id));
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
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con ID: " + id));

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
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con ID: " + id));

        usuarioService.delete(usuario);
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "El Usuario Ha sido eliminado con éxito!");
        return ResponseEntity.ok(response);
    }

    // Envio de datos para el login
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Usuario usuario = usuarioService.findByCorreo(loginRequest.getCorreo())
                .orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario o contraseña incorrecta"));
        }
        if (!passwordEncoder.matches(loginRequest.getPassword(), usuario.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Usuario o contraseña incorrecta"));
        }
        return ResponseEntity.ok(usuario);
    }

    // Buscar usuario por correo

    @GetMapping("/usuarios/buscar")
    public ResponseEntity<?> findByCorreo(@RequestParam String correo) {
        Usuario usuario = usuarioService.findByCorreo(correo)
                .orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No existe el usuario con correo: " + correo));
        }
        return ResponseEntity.ok(usuario);
    }

    // Recuperar contraseña
    @Autowired
    private EmailService emailService;

    @PostMapping("/usuarios/recuperar")
    public ResponseEntity<?> recuperarPassword(@RequestParam String correo) {
        Usuario usuario = usuarioService.findByCorreo(correo)
                .orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No existe el usuario con correo: " + correo));
        }
        String nuevaPassword = GenerarPassword.generarPassword();
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioService.update(usuario);
        emailService.enviarCorreo(correo, "Recuperación de contraseña", "Tu nueva contraseña es: " + nuevaPassword);
        return ResponseEntity.ok(Map.of("mensaje", "Se ha enviado una nueva contraseña a tu correo"));
    }
}