package co.edu.uceva.buildcheck.modules.proyecto_invitacion.repository;

import co.edu.uceva.buildcheck.modules.proyecto_invitacion.model.ProyectoInvitacion;
import co.edu.uceva.buildcheck.modules.proyectos.model.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IProyectoInvitacionRepository extends JpaRepository<ProyectoInvitacion, Long> {
    
    /**
     * Encuentra una invitación por token
     */
    Optional<ProyectoInvitacion> findByToken(String token);

    /**
     * Encuentra todas las invitaciones de un proyecto
     */
    List<ProyectoInvitacion> findByProyecto(Proyecto proyecto);

    /**
     * Encuentra todas las invitaciones activas de un proyecto
     */
    List<ProyectoInvitacion> findByProyectoAndActivo(Proyecto proyecto, Boolean activo);

    /**
     * Elimina invitaciones expiradas
     */
    long deleteByExpiresAtBefore(LocalDateTime fecha);

    /**
     * Verifica si existe una invitación activa con un token
     */
    boolean existsByTokenAndActivo(String token, Boolean activo);
}
