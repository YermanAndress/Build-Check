package co.edu.uceva.buildcheck.modules.proyectos.service;

import java.util.List;
import java.util.Optional;

import co.edu.uceva.buildcheck.modules.proyectos.model.Proyecto;

public interface IProyectoService {
    Proyecto save(Proyecto proyecto);

    void delete(Proyecto proyecto);

    Optional<Proyecto> findById(Long id);

    Proyecto update(Proyecto proyecto);

    List<Proyecto> findAll();
}
