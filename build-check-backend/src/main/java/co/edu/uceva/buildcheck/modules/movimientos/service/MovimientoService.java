package co.edu.uceva.buildcheck.modules.movimientos.service;

import co.edu.uceva.buildcheck.modules.movimientos.model.Movimiento;
import co.edu.uceva.buildcheck.modules.movimientos.repository.MovimientoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MovimientoService {

    @Autowired
    private MovimientoRepository movimientoRepository;

    @Transactional
    public Movimiento save(Movimiento movimiento) {
        return movimientoRepository.save(movimiento);
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
    public Movimiento update(Movimiento movimiento) {
        return movimientoRepository.save(movimiento);
    }
}