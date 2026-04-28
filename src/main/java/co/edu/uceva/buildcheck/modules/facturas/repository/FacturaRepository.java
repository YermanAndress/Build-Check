package co.edu.uceva.buildcheck.modules.facturas.repository;

import co.edu.uceva.buildcheck.modules.facturas.model.Factura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {

    Optional<Factura> findByNumeroFactura(String numeroFactura);

}
