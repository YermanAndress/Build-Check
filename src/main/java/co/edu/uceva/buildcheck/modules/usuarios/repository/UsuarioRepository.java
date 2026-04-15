package co.edu.uceva.buildcheck.modules.usuarios.repository;

import co.edu.uceva.buildcheck.modules.usuarios.model.Usuario;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);
}
