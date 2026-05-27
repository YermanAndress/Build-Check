package co.edu.uceva.buildcheck.modules.proyectos.repository;

import co.edu.uceva.buildcheck.modules.proyectos.model.Proyecto;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IProyectoRepository extends JpaRepository<Proyecto, Long> {
    List<Proyecto> findByUsuarioPropietario_Id(Long usuarioId);
}