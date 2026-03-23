package co.edu.uceva.buildcheck.modules.movimientos.repository;

import co.edu.uceva.buildcheck.modules.movimientos.model.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

}