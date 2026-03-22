package main.java.co.edu.uceva.buildcheck.repository;

import main.java.co.edu.uceva.buildcheck.model.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IProyectoRepository extends JpaRepository<Proyecto, Long>{
}