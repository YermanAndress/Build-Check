package co.edu.uceva.buildcheck.modules.movimientos.repository;

import co.edu.uceva.buildcheck.modules.materiales.model.Material;
import co.edu.uceva.buildcheck.modules.movimientos.model.Movimiento;
import co.edu.uceva.buildcheck.modules.proyectos.model.Proyecto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    boolean existsByProyecto(Proyecto proyecto);
    boolean existsByMaterial(Material material);

    @Query("SELECT m FROM Movimiento m WHERE m.proyecto.id = :proyectoId")
    List<Movimiento> findByProyectoId(@Param("proyectoId") Long proyectoId);
}
