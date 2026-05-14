package co.edu.uceva.buildcheck.modules.materiales.repository;

import co.edu.uceva.buildcheck.modules.materiales.model.Material;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    @Query(
        "SELECT m FROM Material m WHERE m.stockActual <= (m.stockReferencia * 0.25) AND m.stockReferencia > 0"
    )
    List<Material> findMaterialesBajoStock();

    Optional<Material> findByNombre(String nombre);

    @Query("SELECT m FROM Material m WHERE m.proyecto.id = :proyectoId")
    List<Material> findByProyectoId(Long proyectoId);

    @Query(
        "SELECT m FROM Material m WHERE m.proyecto.id = :proyectoId AND m.nombre ILIKE CONCAT('%', :nombre, '%')"
    )
    List<Material> findByProyectoIdAndNombreContaining(
        Long proyectoId,
        String nombre
    );

    @Query(
        "SELECT m FROM Material m WHERE m.proyecto.id = :proyectoId AND m.stockActual <= (m.stockReferencia * 0.25) AND m.stockReferencia > 0"
    )
    List<Material> findMaterialesBajoStockByProyectoId(Long proyectoId);
}
