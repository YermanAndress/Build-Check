package co.edu.uceva.buildcheck.modules.usuario_proyecto.service;

import co.edu.uceva.buildcheck.modules.usuario_proyecto.model.UsuarioProyecto;
import co.edu.uceva.buildcheck.modules.usuarios.model.Roles.RolNombre;

import java.util.List;
import java.util.Optional;

public interface IUsuarioProyectoService {
    
    /**
     * Agrega un usuario a un proyecto con un rol específico
     */
    UsuarioProyecto agregarUsuarioAlProyecto(Long usuarioId, Long proyectoId, RolNombre rol);

    /**
     * Cambia el rol de un usuario en un proyecto
     */
    UsuarioProyecto cambiarRolUsuario(Long usuarioId, Long proyectoId, RolNombre nuevoRol);

    /**
     * Remueve un usuario de un proyecto
     */
    void removerUsuarioDelProyecto(Long usuarioId, Long proyectoId);

    /**
     * Obtiene todos los proyectos de un usuario
     */
    List<UsuarioProyecto> obtenerProyectosDelUsuario(Long usuarioId);

    /**
     * Obtiene todos los miembros de un proyecto
     */
    List<UsuarioProyecto> obtenerMiembrosDelProyecto(Long proyectoId);

    /**
     * Obtiene el rol de un usuario en un proyecto específico
     */
    Optional<UsuarioProyecto> obtenerRolUsuarioEnProyecto(Long usuarioId, Long proyectoId);

    /**
     * Verifica si un usuario es miembro de un proyecto
     */
    boolean esUsuarioMiembroDelProyecto(Long usuarioId, Long proyectoId);

    /**
     * Obtiene todos los miembros de un proyecto con un rol específico
     */
    List<UsuarioProyecto> obtenerMiembrosConRol(Long proyectoId, RolNombre rol);
}
