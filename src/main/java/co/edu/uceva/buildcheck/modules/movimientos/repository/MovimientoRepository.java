package co.edu.uceva.buildcheck.modules.movimientos.repository;

import co.edu.uceva.buildcheck.modules.materiales.model.Material;
import co.edu.uceva.buildcheck.modules.movimientos.model.Movimiento;
import co.edu.uceva.buildcheck.modules.proyectos.model.Proyecto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    boolean existsByProyecto(Proyecto proyecto);

    boolean existsByMaterial(Material material);
}