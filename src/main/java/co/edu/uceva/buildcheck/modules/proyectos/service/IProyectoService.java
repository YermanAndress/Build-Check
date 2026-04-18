package co.edu.uceva.buildcheck.modules.proyectos.service;

import co.edu.uceva.buildcheck.modules.proyectos.model.Proyecto;

import java.util.Optional;
import java.util.List;

public interface IProyectoService {
    Proyecto save(Proyecto proyecto);

    void delete(Proyecto proyecto);

    Optional<Proyecto> findById(Long id);

    Proyecto update(Proyecto proyecto);

    List<Proyecto> findAll();
}
