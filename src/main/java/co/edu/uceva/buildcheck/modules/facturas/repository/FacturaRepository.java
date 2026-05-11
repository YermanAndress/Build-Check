package co.edu.uceva.buildcheck.modules.facturas.repository;

import co.edu.uceva.buildcheck.modules.facturas.model.Factura;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {
    Optional<Factura> findByNumeroFactura(String numeroFactura);

    List<Factura> findByProyectoId(Long proyectoId);
}
