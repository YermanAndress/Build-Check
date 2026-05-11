package co.edu.uceva.buildcheck.modules.proyecto_invitacion.service;

import co.edu.uceva.buildcheck.modules.proyecto_invitacion.model.ProyectoInvitacion;
import co.edu.uceva.buildcheck.modules.usuarios.model.Roles.RolNombre;

import java.util.List;
import java.util.Optional;

public interface IProyectoInvitacionService {
    
    /**
     * Genera una nueva invitación para un proyecto
     */
    ProyectoInvitacion generarInvitacion(Long proyectoId, Long usuarioCreadorId, RolNombre rolDefault);

    /**
     * Valida y usa un token de invitación
     */
    ProyectoInvitacion validarYUsarToken(String token);

    /**
     * Obtiene una invitación por token
     */
    Optional<ProyectoInvitacion> obtenerPorToken(String token);

    /**
     * Revoca una invitación (la desactiva)
     */
    void revocarInvitacion(String token);

    /**
     * Lista todas las invitaciones activas de un proyecto
     */
    List<ProyectoInvitacion> listarInvitacionesPorProyecto(Long proyectoId);

    /**
     * Limpia invitaciones expiradas de la base de datos
     */
    void limpiarInvitacionesExpiradas();
}
