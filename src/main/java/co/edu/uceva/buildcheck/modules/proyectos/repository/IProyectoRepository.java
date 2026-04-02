package co.edu.uceva.buildcheck.modules.proyectos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uceva.buildcheck.modules.proyectos.model.Proyecto;

public interface IProyectoRepository extends JpaRepository<Proyecto, Long> {
}