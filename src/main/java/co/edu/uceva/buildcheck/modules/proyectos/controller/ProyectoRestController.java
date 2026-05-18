package co.edu.uceva.buildcheck.modules.proyectos.controller;

import co.edu.uceva.buildcheck.exception.NoHayDatosException;
import co.edu.uceva.buildcheck.exception.RecursoNoEncontradoException;
import co.edu.uceva.buildcheck.exception.ValidationException;
import co.edu.uceva.buildcheck.exception.OperacionNoPermitidaException;
import co.edu.uceva.buildcheck.modules.proyecto_invitacion.service.IProyectoInvitacionService;
import co.edu.uceva.buildcheck.modules.proyectos.DTO.*;
import co.edu.uceva.buildcheck.modules.proyectos.model.Proyecto;
import co.edu.uceva.buildcheck.modules.proyectos.model.Estados.EstadoNombre;
import co.edu.uceva.buildcheck.modules.proyectos.service.IProyectoService;
import co.edu.uceva.buildcheck.modules.usuario_proyecto.service.IUsuarioProyectoService;
import co.edu.uceva.buildcheck.modules.usuarios.model.Roles.RolNombre;
import co.edu.uceva.buildcheck.security.Jwt;
import co.edu.uceva.buildcheck.security.CifradoSimetrico;
import co.edu.uceva.buildcheck.security.annotations.RequireProyectoAccess;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/proyecto-service")
@CrossOrigin(origins = "*")
public class ProyectoRestController {

    private final IProyectoService proyectoService;
    private final IProyectoInvitacionService invitacionService;
    private final IUsuarioProyectoService usuarioProyectoService;
    private final Jwt jwt;
    private final CifradoSimetrico cifradoSimetrico;

    private static final String MENSAJE = "mensaje";
    private static final String PROYECTO = "proyecto";
    private static final String PROYECTOS = "proyectos";

    public ProyectoRestController(
            IProyectoService proyectoService,
            IProyectoInvitacionService invitacionService,
            IUsuarioProyectoService usuarioProyectoService,
            Jwt jwt,
            CifradoSimetrico cifradoSimetrico) {
        this.proyectoService = proyectoService;
        this.invitacionService = invitacionService;
        this.usuarioProyectoService = usuarioProyectoService;
        this.jwt = jwt;
        this.cifradoSimetrico = cifradoSimetrico;
    }

    /**
     * Listar todos los proyectos (sin filtrar por usuario - admin)
     */
    @GetMapping("/proyectos")
    public ResponseEntity<Map<String, Object>> getProyectos() {
        List<Proyecto> proyectos = proyectoService.findAll();
        if (proyectos.isEmpty()) {
            throw new NoHayDatosException(
                    "No hay proyectos en la base de datos");
        }
        Map<String, Object> response = new HashMap<>();
        response.put(PROYECTOS, proyectos);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener los proyectos del usuario autenticado
     */
    /**
     * Obtener los proyectos del usuario autenticado con su rol en cada proyecto.
     * El rol proviene de usuario_proyecto.rol_proyecto, no del rol global del
     * usuario.
     */
    /**
     * Obtener los proyectos del usuario autenticado con su rol en cada proyecto.
     * El rol proviene de usuario_proyecto.rol_proyecto, no del rol global del
     * usuario.
     */
    @GetMapping("/proyectos/usuario/mis-proyectos")
    public ResponseEntity<Map<String, Object>> getMisProyectos(
            Authentication authentication) {
                if (authentication == null || !authentication.isAuthenticated()) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of("mensaje", "Se requiere autenticacion"));
                }
        String correo = authentication.getName();
        Long usuarioId = jwt.getUsuarioId(correo);

        // obtenerProyectosDelUsuario devuelve List<UsuarioProyecto>
        // → cada entry tiene .getProyecto() y .getRolProyecto()
        List<ProyectoDTO> proyectos = usuarioProyectoService
                .obtenerProyectosDelUsuario(usuarioId)
                .stream()
                .map(up -> {
                    Proyecto p = up.getProyecto();
                    ProyectoDTO dto = new ProyectoDTO();
                    dto.setId(p.getId());
                    dto.setNombre(p.getNombre());
                    dto.setDescripcion(p.getDescripcion());
                    dto.setUbicacion(p.getUbicacion());
                    dto.setPresupuesto(p.getPresupuesto());
                    dto.setEstado(p.getEstado());
                    dto.setFechaCreacion(p.getFechaCreacion());
                    dto.setRolDelUsuario(up.getRolProyecto());

                    // Asignar campos faltantes para evitar "Unexpected null value"
                    if (p.getUsuarioPropietario() != null) {
                        dto.setUsuarioPropietarioId(p.getUsuarioPropietario().getId());
                        // Desencriptar nombre del propietario
                        String nombreDescifrado = cifradoSimetrico.descifrar(p.getUsuarioPropietario().getNombre());
                        dto.setUsuarioPropietarioNombre(nombreDescifrado);
                    }
                    dto.setTotalMiembros(p.getMiembros() != null ? p.getMiembros().size() : 0);
                    dto.setTotalInvitacionesActivas(p.getInvitaciones() != null ? p.getInvitaciones().size() : 0);

                    return dto;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put(PROYECTOS, proyectos);
        return ResponseEntity.ok(response);
    }

    /**
     * Crear un nuevo proyecto
     */
    @PostMapping("/proyectos")
    public ResponseEntity<Map<String, Object>> crearProyecto(
            @Valid @RequestBody CrearProyectoRequest request,
            BindingResult result,
            Authentication authentication) {
        if (result.hasErrors()) {
            throw new ValidationException(result);
        }

        String correo = authentication.getName();
        Long usuarioId = jwt.getUsuarioId(correo);

        Proyecto proyecto = new Proyecto();
        proyecto.setNombre(request.getNombre());
        proyecto.setDescripcion(request.getDescripcion());
        proyecto.setUbicacion(request.getUbicacion());
        proyecto.setPresupuesto(request.getPresupuesto());
        proyecto.setEstado(Enum.valueOf(EstadoNombre.class, request.getEstado()));

        Proyecto nuevoProyecto = proyectoService.crearProyecto(
                proyecto,
                usuarioId);

        // Build ProyectoDTO with rolProyecto set to ROLE_OWNER (creator is always
        // owner)
        ProyectoDTO dto = new ProyectoDTO();
        dto.setId(nuevoProyecto.getId());
        dto.setNombre(nuevoProyecto.getNombre());
        dto.setDescripcion(nuevoProyecto.getDescripcion());
        dto.setUbicacion(nuevoProyecto.getUbicacion());
        dto.setPresupuesto(nuevoProyecto.getPresupuesto());
        dto.setEstado(nuevoProyecto.getEstado());
        dto.setFechaCreacion(nuevoProyecto.getFechaCreacion());
        dto.setRolDelUsuario(RolNombre.ROLE_OWNER); // Creator is always owner

        // Asignar campos faltantes para evitar "Unexpected null value"
        if (nuevoProyecto.getUsuarioPropietario() != null) {
            dto.setUsuarioPropietarioId(nuevoProyecto.getUsuarioPropietario().getId());
            // Desencriptar nombre del propietario
            String nombreDescifrado = cifradoSimetrico.descifrar(nuevoProyecto.getUsuarioPropietario().getNombre());
            dto.setUsuarioPropietarioNombre(nombreDescifrado);
        }
        dto.setTotalMiembros(nuevoProyecto.getMiembros() != null ? nuevoProyecto.getMiembros().size() : 1);

        dto.setTotalInvitacionesActivas(
                nuevoProyecto.getInvitaciones() != null ? nuevoProyecto.getInvitaciones().size() : 0);

        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "El proyecto ha sido creado con éxito");
        response.put(PROYECTO, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtener un proyecto por su ID
     */
    @GetMapping("/proyectos/{id}")
    public ResponseEntity<Map<String, Object>> findById(@PathVariable Long id) {
        Proyecto proyecto = proyectoService
                .findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el proyecto con ID: " + id));
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "El proyecto ha sido encontrado con éxito");
        response.put(PROYECTO, proyecto);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar un proyecto - solo OWNER o ADMIN del proyecto
     */
    @PutMapping("/proyectos/{id}")
    @RequireProyectoAccess(projectIdParam = "id", allowedRoles = { "ROLE_OWNER", "ROLE_ADMIN" })
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long id,
            @Valid @RequestBody Proyecto proyecto,
            BindingResult result) {
        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
        proyectoService
                .findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el proyecto con ID: " + id));
        proyecto.setId(id);
        Map<String, Object> response = new HashMap<>();
        Proyecto proyectoActualizado = proyectoService.update(proyecto);
        response.put(MENSAJE, "El proyecto ha sido actualizado con éxito");
        response.put(PROYECTO, proyectoActualizado);
        return ResponseEntity.ok(response);
    }

    /**
     * Eliminar un proyecto - solo OWNER del proyecto
     */
    @DeleteMapping("/proyectos/{id}")
    @RequireProyectoAccess(projectIdParam = "id", allowedRoles = { "ROLE_OWNER" })
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        Proyecto proyecto = proyectoService
                .findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el proyecto con ID: " + id));
        proyectoService.delete(proyecto);
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "El proyecto ha sido eliminado con éxito");
        return ResponseEntity.ok(response);
    }

    /**
     * Unirse a un proyecto usando un token de invitación
     */
    @PostMapping("/proyectos/unirse")
    public ResponseEntity<Map<String, Object>> unirseAProyecto(
            @Valid @RequestBody UnirseProyectoRequest request,
            Authentication authentication) {
        String correo = authentication.getName();
        Long usuarioId = jwt.getUsuarioId(correo);

        // Validar y usar el token
        var invitacion = invitacionService.validarYUsarToken(
                request.getToken());

        // Agregar usuario al proyecto con el rol por defecto
        usuarioProyectoService.agregarUsuarioAlProyecto(
                usuarioId,
                invitacion.getProyecto().getId(),
                invitacion.getRolPorDefecto());

        // Generar JWT con contexto del proyecto
        String nuevoToken = jwt.generarToken(
                correo,
                invitacion.getProyecto().getId(),
                invitacion.getRolPorDefecto());

        Map<String, Object> response = new HashMap<>();
        response.put("token", nuevoToken);
        response.put("proyecto_id", invitacion.getProyecto().getId());
        response.put("rol_proyecto", invitacion.getRolPorDefecto().name());
        response.put(MENSAJE, "Te has unido al proyecto con éxito");

        return ResponseEntity.ok(response);
    }

    /**
     * Seleccionar un proyecto y generar un JWT con contexto de proyecto
     */
    @PostMapping("/proyectos/{id}/seleccionar")
    public ResponseEntity<Map<String, Object>> seleccionarProyecto(
            @PathVariable Long id,
            Authentication authentication) {
        String correo = authentication.getName();
        Long usuarioId = jwt.getUsuarioId(correo);

        var usuarioProyecto = usuarioProyectoService
                .obtenerRolUsuarioEnProyecto(usuarioId, id)
                .orElseThrow(() -> new OperacionNoPermitidaException(
                        "El usuario no pertenece al proyecto con ID: " + id));

        String nuevoToken = jwt.generarToken(
                correo,
                id,
                usuarioProyecto.getRolProyecto());

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", nuevoToken);
        response.put("proyecto_id", id);
        response.put("rol_proyecto", usuarioProyecto.getRolProyecto().name());
        response.put(MENSAJE, "Proyecto seleccionado con éxito");

        return ResponseEntity.ok(response);
    }

    /**
     * Generar una nueva invitación para un proyecto
     */
    @PostMapping("/proyectos/{id}/invitaciones/generar")
    @RequireProyectoAccess(projectIdParam = "id", allowedRoles = { "ROLE_OWNER", "ROLE_ADMIN" })
    public ResponseEntity<Map<String, Object>> generarInvitacion(
            @PathVariable Long id,
            @Valid @RequestBody GenerarInvitacionRequest request,
            Authentication authentication) {
        // Get usuarioId from Authentication (injected by Spring Security from JWT)
        String correo = authentication.getName();
        Long usuarioId = jwt.getUsuarioId(correo);

        if (usuarioId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of(MENSAJE, "No se pudo obtener el ID del usuario"));
        }

        var invitacion = invitacionService.generarInvitacion(
                id,
                usuarioId,
                request.getRolPorDefecto());

        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "Invitación generada con éxito");
        response.put("token", invitacion.getToken());
        response.put("fecha_expiracion", invitacion.getFechaExpiracion());
        response.put("usos_restantes", invitacion.getUsosRestantes());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Listar invitaciones activas de un proyecto
     */
    @GetMapping("/proyectos/{id}/invitaciones")
    public ResponseEntity<Map<String, Object>> listarInvitaciones(
            @PathVariable Long id) {
        List<InvitacionDTO> invitaciones = invitacionService
                .listarInvitacionesPorProyecto(id)
                .stream()
                .map(inv -> new InvitacionDTO(
                        inv.getId(),
                        inv.getToken(),
                        inv.getRolPorDefecto(),
                        inv.getUsosRestantes(),
                        inv.getActivo(),
                        inv.getFechaExpiracion(),
                        inv.getFechaCreacion()))
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("invitaciones", invitaciones);
        return ResponseEntity.ok(response);
    }

    /**
     * Revocar una invitación
     */
    @DeleteMapping("/proyectos/{id}/invitaciones/{token}")
    public ResponseEntity<Map<String, Object>> revocarInvitacion(
            @PathVariable Long id,
            @PathVariable String token) {
        invitacionService.revocarInvitacion(token);

        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "Invitación revocada con éxito");
        return ResponseEntity.ok(response);
    }

    /**
     * Listar miembros de un proyecto
     */
    @GetMapping("/proyectos/{id}/miembros")
    public ResponseEntity<Map<String, Object>> listarMiembros(
            @PathVariable Long id) {
        List<UsuarioProyectoDTO> miembros = (List<UsuarioProyectoDTO>) usuarioProyectoService
                .obtenerMiembrosDelProyecto(id)
                .stream()
                .map(up -> new UsuarioProyectoDTO(
                        up.getUsuario().getId(),
                        up.getUsuario().getNombre(),
                        up.getUsuario().getCorreo(),
                        up.getRolProyecto(),
                        up.getFechaCreacion()))
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("miembros", miembros);
        response.put("total", miembros.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Cambiar el rol de un usuario en el proyecto
     */
    @PutMapping("/proyectos/{id}/miembros/{usuarioId}/rol")
    public ResponseEntity<Map<String, Object>> cambiarRolMiembro(
            @PathVariable Long id,
            @PathVariable Long usuarioId,
            @Valid @RequestBody CambiarRolRequest request) {
        usuarioProyectoService.cambiarRolUsuario(
                usuarioId,
                id,
                request.getNuevoRol());

        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "Rol actualizado con éxito");
        response.put("nuevo_rol", request.getNuevoRol().name());

        return ResponseEntity.ok(response);
    }

    /**
     * Remover un usuario del proyecto
     */
    @DeleteMapping("/proyectos/{id}/miembros/{usuarioId}")
    public ResponseEntity<Map<String, Object>> removerMiembro(
            @PathVariable Long id,
            @PathVariable Long usuarioId) {
        usuarioProyectoService.removerUsuarioDelProyecto(usuarioId, id);

        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "Usuario removido del proyecto con éxito");

        return ResponseEntity.ok(response);
    }
}
