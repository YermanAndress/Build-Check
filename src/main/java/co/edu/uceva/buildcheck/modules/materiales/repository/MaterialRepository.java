package co.edu.uceva.buildcheck.modules.materiales.repository;

import co.edu.uceva.buildcheck.modules.materiales.model.Material;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
        "SELECT m FROM Material m WHERE m.proyecto.id = :proyectoId AND m.stockActual <= (m.stockReferencia * 0.25) AND m.stockReferencia > 0"
    )
    List<Material> findMaterialesBajoStockByProyectoId(Long proyectoId);

    @Query("SELECT m FROM Material m WHERE m.proyecto.id = :proyectoId " +
            "AND LOWER(m.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Material> findByProyectoIdAndNombreContaining(
        @Param("proyectoId") Long proyectoId,
        @Param("nombre") String nombre
    );

    Optional<Material> findByNombreIgnoreCaseAndProyectoId(String nombre, Long proyectoId);
}
