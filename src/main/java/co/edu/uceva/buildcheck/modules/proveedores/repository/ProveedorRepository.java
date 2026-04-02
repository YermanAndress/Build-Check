package co.edu.uceva.buildcheck.modules.proveedores.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.uceva.buildcheck.modules.proveedores.model.Proveedor;

@Repository
public interface ProveedorRepository  extends JpaRepository<Proveedor, Long> {    
    Optional<Proveedor> findByNombre(String nombre);
}
