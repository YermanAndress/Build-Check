package co.edu.uceva.buildcheck.modules.materiales.repository;

import co.edu.uceva.buildcheck.modules.materiales.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    
}
