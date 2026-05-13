package co.edu.uceva.buildcheck.modules.usuario_proyecto.repository;

import co.edu.uceva.buildcheck.modules.usuario_proyecto.model.UsuarioProyecto;
import co.edu.uceva.buildcheck.modules.usuarios.model.Usuario;
import co.edu.uceva.buildcheck.modules.proyectos.model.Proyecto;
import co.edu.uceva.buildcheck.modules.usuarios.model.Roles.RolNombre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUsuarioProyectoRepository extends JpaRepository<UsuarioProyecto, Long> {

    /**
     * Encuentra la relación entre un usuario y un proyecto
     */
    Optional<UsuarioProyecto> findByUsuarioAndProyecto(Usuario usuario, Proyecto proyecto);

    /**
     * Encuentra todos los miembros de un proyecto
     */
    List<UsuarioProyecto> findByProyecto(Proyecto proyecto);

    /**
     * Encuentra todos los proyectos de un usuario
     */
    List<UsuarioProyecto> findByUsuario(Usuario usuario);

    /**
     * Encuentra miembros de un proyecto con un rol específico
     */
    List<UsuarioProyecto> findByProyectoAndRolProyecto(Proyecto proyecto, RolNombre rolProyecto);

    /**
     * Elimina la relación usuario-proyecto
     */
    void deleteByUsuarioAndProyecto(Usuario usuario, Proyecto proyecto);

    /**
     * Verifica si un usuario es miembro de un proyecto
     */
    boolean existsByUsuarioAndProyecto(Usuario usuario, Proyecto proyecto);

    /**
     * Obtiene el rol de un usuario en un proyecto
     */
    Optional<UsuarioProyecto> findByUsuario_IdAndProyecto_Id(Long usuarioId, Long proyectoId);

    /**
     * Encuentra todos los proyectos de un usuario con JOIN FETCH para evitar LAZY
     * loading
     */
    @Query("SELECT DISTINCT up FROM UsuarioProyecto up " +
            "JOIN FETCH up.proyecto p " +
            "WHERE up.usuario.id = :usuarioId")
    List<UsuarioProyecto> findByUsuarioIdWithProyectoEager(@Param("usuarioId") Long usuarioId);

    /**
     * Encuentra todos los miembros de un proyecto con JOIN FETCH para evitar LAZY
     * loading
     */
    @Query("SELECT DISTINCT up FROM UsuarioProyecto up " +
            "JOIN FETCH up.usuario u " +
            "WHERE up.proyecto.id = :proyectoId")
    List<UsuarioProyecto> findByProyectoIdWithUsuarioEager(@Param("proyectoId") Long proyectoId);
}
