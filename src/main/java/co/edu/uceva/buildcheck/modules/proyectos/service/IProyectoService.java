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

    /**
     * Crea un nuevo proyecto y agrega al usuario creador como propietario
     */
    Proyecto crearProyecto(Proyecto proyecto, Long usuarioId);

    /**
     * Obtiene todos los proyectos de un usuario
     */
    List<Proyecto> obtenerProyectosDelUsuario(Long usuarioId);
}
