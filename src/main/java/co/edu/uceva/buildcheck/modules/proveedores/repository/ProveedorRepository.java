package co.edu.uceva.buildcheck.modules.proveedores.repository;

import co.edu.uceva.buildcheck.modules.proveedores.model.Proveedor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    Optional<Proveedor> findByNombre(String nombre);
}
