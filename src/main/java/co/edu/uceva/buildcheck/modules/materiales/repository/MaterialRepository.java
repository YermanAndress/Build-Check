package co.edu.uceva.buildcheck.modules.materiales.repository;

import co.edu.uceva.buildcheck.modules.materiales.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

    @Query("SELECT m FROM Material m WHERE m.stockActual <= (m.stockReferencia * 0.25) AND m.stockReferencia > 0")
    java.util.List<Material> findMaterialesBajoStock();
}