package co.edu.uceva.buildcheck.modules.movimientos.service;

import co.edu.uceva.buildcheck.modules.movimientos.repository.MovimientoRepository;
import co.edu.uceva.buildcheck.modules.materiales.repository.MaterialRepository;
import co.edu.uceva.buildcheck.modules.proyectos.repository.IProyectoRepository;
import co.edu.uceva.buildcheck.modules.movimientos.model.tipoMovimiento.TipoMovimientoNombre;
import co.edu.uceva.buildcheck.modules.movimientos.model.Movimiento;
import co.edu.uceva.buildcheck.modules.materiales.model.Material;
import co.edu.uceva.buildcheck.modules.proyectos.model.Proyecto;
import co.edu.uceva.buildcheck.modules.movimientos.DTO.MovimientoRequest;
import co.edu.uceva.buildcheck.exception.RecursoNoEncontradoException;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.List;

@Service
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final IProyectoRepository proyectoRepository;
    private final MaterialRepository materialRepository;

    @Autowired
    public MovimientoService(MovimientoRepository movimientoRepository, IProyectoRepository proyectoRepository,
            MaterialRepository materialRepository) {
        this.movimientoRepository = movimientoRepository;
        this.proyectoRepository = proyectoRepository;
        this.materialRepository = materialRepository;
    }

    @Transactional
    public Movimiento save(MovimientoRequest request) {
        // 1. Buscamos las entidades relacionadas (Validación)
        Proyecto proyecto = proyectoRepository.findById(request.getProyectoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Proyecto no encontrado"));

        Material material = materialRepository.findById(request.getMaterialId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Material no encontrado"));

        // 2. Creamos la entidad Movimiento desde el Request
        Movimiento nuevoMovimiento = new Movimiento();
        nuevoMovimiento.setTipoMovimiento(request.getTipoMovimiento());
        nuevoMovimiento.setCantidad(request.getCantidad());
        nuevoMovimiento.setFecha(request.getFecha());
        nuevoMovimiento.setUsuarioId(request.getUsuarioId());
        nuevoMovimiento.setEvidenciaFotografica(request.getEvidenciaFotografica());
        nuevoMovimiento.setProyecto(proyecto);
        nuevoMovimiento.setMaterial(material);

        // 3. LLAMAMOS A LA LÓGICA DE NEGOCIO (Actualización de Stock)
        actualizarStockMaterial(material, nuevoMovimiento);

        // 4. Guardamos todo (Gracias a @Transactional, si falla algo, no se guarda
        // nada)
        return movimientoRepository.save(nuevoMovimiento);
    }

    private void actualizarStockMaterial(Material material, Movimiento movimiento) {
        double cantidad = movimiento.getCantidad();

        if (movimiento.getTipoMovimiento() == TipoMovimientoNombre.ENTRADA) {
            // 1. Aumentamos el stock actual
            material.setStockActual(material.getStockActual() + (int) cantidad);

            // 2. ACTUALIZACIÓN DINÁMICA: El nuevo 100% es el stock actual tras la entrada
            material.setStockReferencia(material.getStockActual());
            System.out.println(
                    "Nueva referencia de stock para " + material.getNombre() + ": " + material.getStockReferencia());
        } else if (movimiento.getTipoMovimiento() == TipoMovimientoNombre.SALIDA) {
            if (material.getStockActual() < cantidad) {
                throw new IllegalStateException("Stock insuficiente.");
            }
            material.setStockActual(material.getStockActual() - (int) cantidad);

            // Comparamos el actual contra el 25% de la última referencia (bodega llena)
            double stockCritico = material.getStockReferencia() * 0.25;

            if (material.getStockActual() <= stockCritico) {
                System.out.println("ALERTA DE STOCK BAJO DEL MATERIAL: " + material.getNombre() +
                        " llegó al 25% de su capacidad referenciada (" + material.getStockActual() + ")");
            }
        }
        materialRepository.save(material);
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
        Movimiento movimientoExistente = movimientoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Movimiento no encontrado con ID: " + id));

        // 1. Revertimos el efecto del movimiento anterior antes de aplicar el nuevo
        revertirStock(movimientoExistente.getMaterial(), movimientoExistente);

        // 2. Actualizamos los datos del movimiento con el Request
        movimientoExistente.setTipoMovimiento(request.getTipoMovimiento());
        movimientoExistente.setCantidad(request.getCantidad());
        movimientoExistente.setFecha(request.getFecha());
        movimientoExistente.setUsuarioId(request.getUsuarioId());
        movimientoExistente.setEvidenciaFotografica(request.getEvidenciaFotografica());

        // 3. Aplicamos la nueva lógica de stock
        actualizarStockMaterial(movimientoExistente.getMaterial(), movimientoExistente);

        return movimientoRepository.save(movimientoExistente);
    }

    // Método auxiliar para no hacer un desastre con los números al editar
    private void revertirStock(Material material, Movimiento mov) {
        if (mov.getTipoMovimiento() == TipoMovimientoNombre.ENTRADA) {
            material.setStockActual(material.getStockActual() - mov.getCantidad().intValue());
        } else {
            material.setStockActual(material.getStockActual() + mov.getCantidad().intValue());
        }
        // No tocamos la referencia aquí, solo el actual
        materialRepository.save(material);
    }
}