package co.edu.uceva.buildcheck.modules.facturas.service;

import co.edu.uceva.buildcheck.modules.facturas.repository.FacturaRepository;
import co.edu.uceva.buildcheck.modules.facturas.model.Factura;

import org.springframework.transaction.annotation.Transactional; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FacturaService {

    @Autowired
    private FacturaRepository facturaRepository;

    /**
    *   Guarda Una Factura Nueva
    */ 
    @Transactional
    public Factura save(Factura factura) {
        return facturaRepository.save(factura);
    }

    /**
    *   Elimina Una Factura
    */ 
    @Transactional
    public void delete(Factura factura) {
        facturaRepository.delete(factura);
    }

    /**
    *   Busca Una Factura Por ID
    */ 
    @Transactional(readOnly = true)
    public Optional<Factura> findById(Long id) {
        return facturaRepository.findById(id);
    }

    /**
    *   Actualiza Una Factura Por ID
    */ 
    @Transactional
    public Factura update(Factura factura) {
        return facturaRepository.save(factura);
    }


    /**
    *   Lista todos los facturas
    */ 
    @Transactional(readOnly = true)
    public List<Factura> findAll() {
        return facturaRepository.findAll();
    }
}