package co.edu.uceva.buildcheck.modules.proyectos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.uceva.buildcheck.modules.proyectos.model.Proyecto;

@Repository
public interface IProyectoRepository extends JpaRepository<Proyecto, Long>{
}