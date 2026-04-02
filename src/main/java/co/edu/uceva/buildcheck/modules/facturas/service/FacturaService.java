package co.edu.uceva.buildcheck.modules.facturas.service;

import co.edu.uceva.buildcheck.modules.facturas.repository.FacturaRepository;
import co.edu.uceva.buildcheck.modules.materiales.model.Material;
import co.edu.uceva.buildcheck.modules.materiales.repository.MaterialRepository;
import co.edu.uceva.buildcheck.modules.proveedores.model.Proveedor;
import co.edu.uceva.buildcheck.modules.proveedores.repository.ProveedorRepository;
import co.edu.uceva.buildcheck.modules.factura_material.model.FacturaMaterial;
import co.edu.uceva.buildcheck.modules.factura_material.repository.IFacturaMaterialRepository;
import co.edu.uceva.buildcheck.modules.facturas.DTO.FacturaItemRequest;
import co.edu.uceva.buildcheck.modules.facturas.DTO.FacturaRequest;
import co.edu.uceva.buildcheck.modules.facturas.model.Factura;

import org.springframework.transaction.annotation.Transactional; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final IFacturaMaterialRepository facturaMaterialRepository;
    private final ProveedorRepository proveedorRepository;
    private final MaterialRepository materialRepository;

    @Autowired
    public FacturaService(FacturaRepository facturaRepository, IFacturaMaterialRepository facturaMaterialRepository, ProveedorRepository proveedorRepository, MaterialRepository materialRepository){
        this.facturaRepository = facturaRepository;
        this.facturaMaterialRepository = facturaMaterialRepository;
        this.proveedorRepository = proveedorRepository;
        this.materialRepository = materialRepository;
    }
    /**
    *   Guarda Una Factura Nueva
    */ 
    @Transactional
    public Factura save(FacturaRequest request) {
        Proveedor proveedor = proveedorRepository.findByNombre(request.getProveedor())
                .orElseGet(() -> {
                    Proveedor nuevoProveedor = new Proveedor();
                    nuevoProveedor.setNombre(request.getProveedor());
                    return proveedorRepository.save(nuevoProveedor);
                });
        Factura factura = new Factura();
        factura.setNumeroFactura(request.getNumeroFactura());
        factura.setFecha(request.getFecha());
        factura.setProveedor(proveedor);
        factura.setObservaciones(request.getObservaciones());
        factura.setValorTotal(request.getValorTotal());
        factura.setProyectoId(request.getProyectoId());
        List<FacturaMaterial> items = new ArrayList<>();
        if (request.getItems() != null) {
            for(FacturaItemRequest itemRequest : request.getItems()){
                Material material = materialRepository.findById(itemRequest.getMaterialId())
                        .orElseThrow(() -> new IllegalArgumentException("Material no encontrado con ID: " + itemRequest.getMaterialId()));
                FacturaMaterial facturaMaterial = new FacturaMaterial();
                facturaMaterial.setFactura(factura);
                facturaMaterial.setMaterial(material);
                facturaMaterial.setCantidad(itemRequest.getCantidad());
                facturaMaterial.setPrecioUnitario(itemRequest.getPrecioUnitario());
                items.add(facturaMaterial);
            }
        }
        factura.setItems(items);
        return facturaRepository.save(factura);
    }

    /**
    *   Elimina Una Factura
    */ 
    @Transactional
    public void delete(Factura factura) {
        boolean tieneMateriales = facturaMaterialRepository.existsByFactura(factura);
        if (!tieneMateriales) {
            facturaRepository.delete(factura);
        }else{
            throw new IllegalStateException("No se puede eliminar la factura debido a que tiene materiales asociados");
        }
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