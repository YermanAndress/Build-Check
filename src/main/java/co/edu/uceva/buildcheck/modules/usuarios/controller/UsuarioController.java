package co.edu.uceva.buildcheck.modules.usuarios.controller;

import co.edu.uceva.buildcheck.modules.usuarios.service.UsuarioService;
import co.edu.uceva.buildcheck.security.CifradoSimetrico;
import co.edu.uceva.buildcheck.security.Jwt;
import co.edu.uceva.buildcheck.modules.usuarios.login.GenerarPassword;
import co.edu.uceva.buildcheck.exception.RecursoNoEncontradoException;
import co.edu.uceva.buildcheck.modules.usuarios.login.EmailService;
import co.edu.uceva.buildcheck.modules.usuarios.login.LoginRequest;
import co.edu.uceva.buildcheck.modules.usuarios.login.RsaKeyService;
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

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RsaKeyService rsaKeyService;
  
    @Autowired
    private CifradoSimetrico cifradoSimetrico;
    private Jwt jwt;

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
        usuarios.forEach(u ->{
            try{
                u.setNombre(cifradoSimetrico.descifrar(u.getNombre()));
            }catch (Exception e){
                // Si ocurre un error al descifrar, dejamos el nombre sin cambios
            }
        });
        Map<String, Object> response = new HashMap<>();
        response.put(USUARIOS, usuarios);
        return ResponseEntity.ok(response);
    }

    /**
    * Crear un nuevo usuario
    */
    @PostMapping("/usuarios")
    public ResponseEntity<Map<String, Object>> save(@RequestBody Usuario usuario) {
        try {
            // Desencriptar solo el correo
            String correoDescifrado = rsaKeyService.decrypt(usuario.getCorreo());
            usuario.setCorreo(correoDescifrado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(MENSAJE, "Error al desencriptar el correo"));
        }
        // Cifrar nombre con algoritmo simetrico
        usuario.setNombre(cifradoSimetrico.cifrar(usuario.getNombre()));
  
        Usuario nuevoUsuario = usuarioService.save(usuario);
        // Devolver nombre descifrado al frontend
        try{
            nuevoUsuario.setNombre(cifradoSimetrico.descifrar(nuevoUsuario.getNombre()));
        }catch (Exception e){
            // Si ocurre un error al descifrar, dejamos el nombre sin cambios
        }
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "El usuario ha sido creado con éxito!");
        response.put(USUARIO, nuevoUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── Obtener un usuario por ID ─────────────────────────────────
    @GetMapping("/usuarios/{id}")
    public ResponseEntity<Map<String, Object>> findById(@PathVariable Long id) {
        Usuario usuario = usuarioService.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con ID: " + id));
        try{
            usuario.setNombre(cifradoSimetrico.descifrar(usuario.getNombre()));
        }catch (Exception e){
            // Si ocurre un error al descifrar, dejamos el nombre sin cambios
        }
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "El usuario ha sido encontrado con éxito!");
        response.put(USUARIO, usuario);
        return ResponseEntity.ok(response);
    }

    // ── Actualizar un usuario ─────────────────────────────────────
    @PutMapping("/usuarios/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @Valid @RequestBody Usuario usuario) {
        usuarioService.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con ID: " + id));
        usuario.setId(id); // Aseguramos que se actualice el ID correcto
        // Cifrar nombre antes de guardar
        usuario.setNombre(cifradoSimetrico.cifrar(usuario.getNombre()));
        Usuario usuarioActualizado = usuarioService.update(usuario);
        try{
            usuarioActualizado.setNombre(cifradoSimetrico.descifrar(usuarioActualizado.getNombre()));
        }catch (Exception e){
            // Si ocurre un error al descifrar, dejamos el nombre sin cambios
        }
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "El usuario ha sido actualizado con éxito!");
        response.put(USUARIO, usuarioActualizado);
        return ResponseEntity.ok(response);
    }

    // ── Eliminar un usuario ───────────────────────────────────────
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        Usuario usuario = usuarioService.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con ID: " + id));
        usuarioService.delete(usuario);
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "El Usuario Ha sido eliminado con éxito!");
        return ResponseEntity.ok(response);
    }

    // ── Login con RSA ─────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            String correo   = rsaKeyService.decrypt(loginRequest.getCorreo());
            String password = rsaKeyService.decrypt(loginRequest.getPassword());

            Usuario usuario = usuarioService.findByCorreo(correo).orElse(null);
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Usuario o contraseña incorrecta"));
            }
            if (!passwordEncoder.matches(password, usuario.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Usuario o contraseña incorrecta"));
            }
            // Descifrar nombre antes de devolver
            try{
                usuario.setNombre(cifradoSimetrico.descifrar(usuario.getNombre()));
            }catch (Exception e){
              // Si ocurre un error al descifrar, dejamos el nombre sin cambios
            }
            String token = jwt.generarToken(usuario.getCorreo(), usuario.getRol());
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("usuario", usuario);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
          return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                  .body(Map.of("error", "Error al desencriptar datos"));
        }
    }

    // ── Buscar usuario por correo ─────────────────────────────────
    @GetMapping("/usuarios/buscar")
    public ResponseEntity<?> findByCorreo(@RequestParam String correo) {
        Usuario usuario = usuarioService.findByCorreo(correo).orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No existe el usuario con correo: " + correo));
        }
        try{
            usuario.setNombre(cifradoSimetrico.descifrar(usuario.getNombre()));
        }catch (Exception e){
            // Si ocurre un error al descifrar, dejamos el nombre sin cambios
        }
        return ResponseEntity.ok(usuario);
    }

    // ── Recuperar contraseña ──────────────────────────────────────
    @PostMapping("/usuarios/recuperar")
    public ResponseEntity<?> recuperarPassword(@RequestParam String correo) {
        Usuario usuario = usuarioService.findByCorreo(correo).orElse(null);
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

    // ── Exponer llave pública RSA ─────────────────────────────────
    @GetMapping("/public-key")
    public ResponseEntity<Map<String, String>> getPublicKey() {
        return ResponseEntity.ok(Map.of("publicKey", rsaKeyService.getPublicKeyBase64()));
    }
}