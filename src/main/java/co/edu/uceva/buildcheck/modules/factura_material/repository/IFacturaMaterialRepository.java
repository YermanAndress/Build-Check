package co.edu.uceva.buildcheck.modules.factura_material.repository;

import co.edu.uceva.buildcheck.modules.factura_material.model.FacturaMaterial;
import co.edu.uceva.buildcheck.modules.materiales.model.Material;
import co.edu.uceva.buildcheck.modules.facturas.model.Factura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IFacturaMaterialRepository extends JpaRepository<FacturaMaterial, Long> {
    boolean existsByMaterial(Material material);

    boolean existsByFactura(Factura factura);

    void deleteByFactura(Factura factura);
}
