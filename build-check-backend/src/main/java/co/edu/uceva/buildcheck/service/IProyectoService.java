package main.java.co.edu.uceva.buildcheck.service;

import main.java.co.edu.uceva.buildcheck.model.Proyecto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface IProyectoService {
    Proyecto save(Proyecto proyecto);
    void delete(Proyecto proyecto);
    Optional<Proyecto> findById(Long id);
    Proyecto update(Proyecto proyecto);
    List<Proyecto> findAll();
    Page<Proyecto> findAll(Pageable pagueable);
}
