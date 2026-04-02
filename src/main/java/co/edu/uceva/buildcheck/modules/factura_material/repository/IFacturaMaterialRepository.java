package co.edu.uceva.buildcheck.modules.factura_material.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.uceva.buildcheck.modules.factura_material.model.FacturaMaterial;
import co.edu.uceva.buildcheck.modules.facturas.model.Factura;
import co.edu.uceva.buildcheck.modules.materiales.model.Material;

@Repository
public interface IFacturaMaterialRepository extends JpaRepository<FacturaMaterial, Long>{
    boolean existsByMaterial(Material material);
    boolean existsByFactura(Factura factura);
}
