package co.edu.uceva.buildcheck.modules.movimientos.service;

import co.edu.uceva.buildcheck.modules.materiales.model.Material;
import co.edu.uceva.buildcheck.modules.materiales.repository.MaterialRepository;
import co.edu.uceva.buildcheck.modules.movimientos.DTO.MovimientoRequest;
import co.edu.uceva.buildcheck.modules.movimientos.model.Movimiento;
import co.edu.uceva.buildcheck.modules.movimientos.repository.MovimientoRepository;
import co.edu.uceva.buildcheck.modules.proyectos.model.Proyecto;
import co.edu.uceva.buildcheck.modules.proyectos.repository.IProyectoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final IProyectoRepository proyectoRepository;
    private final MaterialRepository materialRepository;

    @Autowired
    public MovimientoService(MovimientoRepository movimientoRepository, IProyectoRepository proyectoRepository, MaterialRepository materialRepository) {
        this.movimientoRepository = movimientoRepository;
        this.proyectoRepository = proyectoRepository;
        this.materialRepository = materialRepository;
    }

    @Transactional
    public Movimiento save(MovimientoRequest movimiento) {
        Proyecto proyecto = proyectoRepository.findById(movimiento.getProyectoId())
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado con ID: " + movimiento.getProyectoId()));
        Material material = materialRepository.findById(movimiento.getMaterialId())
                .orElseThrow(() -> new IllegalArgumentException("Material no encontrado con ID: " + movimiento.getMaterialId()));   
        Movimiento nuevoMovimiento = new Movimiento();
        nuevoMovimiento.setTipoMovimiento(movimiento.getTipoMovimiento());
        nuevoMovimiento.setCantidad(movimiento.getCantidad());
        nuevoMovimiento.setFecha(movimiento.getFecha());
        nuevoMovimiento.setUsuarioId(movimiento.getUsuarioId());
        nuevoMovimiento.setEvidenciaFotografica(movimiento.getEvidenciaFotografica());
        nuevoMovimiento.setProyecto(proyecto);
        nuevoMovimiento.setMaterial(material);
        return movimientoRepository.save(nuevoMovimiento);
    }

    @Transactional
    public void delete(Movimiento movimiento) {
        movimientoRepository.delete(movimiento);
    }

    @Transactional(readOnly = true)
    public Optional<Movimiento> findById(Long id) {
        return movimientoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Movimiento> findAll() {
        return movimientoRepository.findAll();
    }

    @Transactional
    public Movimiento update(Long id, MovimientoRequest request) {
        Movimiento movimiento = movimientoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Movimiento no encontrado con ID: " + id));
        movimiento.setTipoMovimiento(request.getTipoMovimiento());
        movimiento.setCantidad(request.getCantidad());
        movimiento.setFecha(request.getFecha());
        movimiento.setUsuarioId(request.getUsuarioId());
        movimiento.setEvidenciaFotografica(request.getEvidenciaFotografica());
        return movimientoRepository.save(movimiento);
    }
}