package co.edu.uceva.buildcheck.modules.facturas.repository;

import co.edu.uceva.buildcheck.modules.facturas.model.Factura;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {
    Optional<Factura> findByNumeroFactura(String numeroFactura);

    @Query("SELECT f FROM Factura f WHERE f.proyecto.id = :proyectoId ORDER BY f.fechaCreacion DESC")
    List<Factura> findByProyectoId(@Param("proyectoId") Long proyectoId);
}
