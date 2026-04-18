package co.edu.uceva.buildcheck.modules.facturas.service;

import co.edu.uceva.buildcheck.modules.factura_material.repository.IFacturaMaterialRepository;
import co.edu.uceva.buildcheck.modules.proveedores.repository.ProveedorRepository;
import co.edu.uceva.buildcheck.modules.materiales.repository.MaterialRepository;
import co.edu.uceva.buildcheck.modules.facturas.repository.FacturaRepository;
import co.edu.uceva.buildcheck.modules.factura_material.model.FacturaMaterial;
import co.edu.uceva.buildcheck.modules.proveedores.model.Proveedor;
import co.edu.uceva.buildcheck.modules.materiales.model.Material;
import co.edu.uceva.buildcheck.modules.factura_material.DTO.FacturaMaterialDTO;
import co.edu.uceva.buildcheck.modules.facturas.DTO.FacturaItemRequest;
import co.edu.uceva.buildcheck.modules.facturas.DTO.FacturaRequest;
import co.edu.uceva.buildcheck.modules.facturas.DTO.FacturaDTO;
import co.edu.uceva.buildcheck.modules.facturas.model.Factura;
import co.edu.uceva.buildcheck.exception.OperacionNoPermitidaException;
import co.edu.uceva.buildcheck.exception.RecursoNoEncontradoException;

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
    public FacturaService(FacturaRepository facturaRepository, IFacturaMaterialRepository facturaMaterialRepository,
            ProveedorRepository proveedorRepository, MaterialRepository materialRepository) {
        this.facturaRepository = facturaRepository;
        this.facturaMaterialRepository = facturaMaterialRepository;
        this.proveedorRepository = proveedorRepository;
        this.materialRepository = materialRepository;
    }

    /**
     * Guarda Una Factura Nueva
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
            for (FacturaItemRequest itemRequest : request.getItems()) {
                Material material = materialRepository.findById(itemRequest.getMaterialId())
                        .orElseThrow(() -> new RecursoNoEncontradoException(
                                "Material no encontrado con ID: " + itemRequest.getMaterialId()));
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
     * Elimina Una Factura
     */
    @Transactional
    public void delete(Factura factura) {
        boolean tieneMateriales = facturaMaterialRepository.existsByFactura(factura);
        if (!tieneMateriales) {
            facturaRepository.delete(factura);
        } else {
            throw new OperacionNoPermitidaException(
                    "No se puede eliminar la factura debido a que tiene materiales asociados");
        }
    }

    /**
     * Busca Una Factura Por ID
     */
    @Transactional(readOnly = true)
    public Optional<Factura> findById(Long id) {
        return facturaRepository.findById(id);
    }

    /**
     * Actualiza Una Factura Por ID
     */
    @Transactional
    public Factura update(Long id, FacturaRequest factura) {
        Factura facturaExistente = facturaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la factura con el ID: " + id));
        facturaExistente.setNumeroFactura(factura.getNumeroFactura());
        facturaExistente.setFecha(factura.getFecha());
        facturaExistente.setObservaciones(factura.getObservaciones());
        facturaExistente.setValorTotal(factura.getValorTotal());
        facturaExistente.setProyectoId(factura.getProyectoId());
        Proveedor proveedor = proveedorRepository.findByNombre(factura.getProveedor())
                .orElseGet(() -> {
                    Proveedor nuevoProveedor = new Proveedor();
                    nuevoProveedor.setNombre(factura.getProveedor());
                    return proveedorRepository.save(nuevoProveedor);
                });
        facturaExistente.setProveedor(proveedor);
        facturaExistente.getItems().clear();
        if (factura.getItems() != null) {
            for (FacturaItemRequest itemRequest : factura.getItems()) {
                Material material = materialRepository.findById(itemRequest.getMaterialId())
                        .orElseThrow(() -> new RecursoNoEncontradoException(
                                "Material no encontrado con ID: " + itemRequest.getMaterialId()));
                FacturaMaterial facturaMaterial = new FacturaMaterial();
                facturaMaterial.setFactura(facturaExistente);
                facturaMaterial.setMaterial(material);
                facturaMaterial.setCantidad(itemRequest.getCantidad());
                facturaMaterial.setPrecioUnitario(itemRequest.getPrecioUnitario());
                facturaExistente.getItems().add(facturaMaterial);
            }
        }
        return facturaRepository.save(facturaExistente);
    }

    /**
     * Lista todos los facturas
     */
    @Transactional(readOnly = true)
    public List<Factura> findAll() {
        return facturaRepository.findAll();
    }

    // Mostrar los items de una factura
    public FacturaDTO toDTO(Factura factura) {
        FacturaDTO facturaDTO = new FacturaDTO();
        facturaDTO.setId(factura.getId());
        facturaDTO.setNumeroFactura(factura.getNumeroFactura());
        facturaDTO.setFecha(factura.getFecha());
        facturaDTO.setProveedor(factura.getProveedor().getNombre());
        facturaDTO.setValorTotal(factura.getValorTotal());
        facturaDTO.setProyectoId(factura.getProyectoId());
        facturaDTO.setItems(
                factura.getItems().stream().map(fm -> {
                    FacturaMaterialDTO fMaterialDTO = new FacturaMaterialDTO();
                    fMaterialDTO.setId(fm.getId());
                    fMaterialDTO.setCantidad(fm.getCantidad());
                    fMaterialDTO.setPrecioUnitario(fm.getPrecioUnitario());
                    fMaterialDTO.setFacturaId(factura.getId());
                    return fMaterialDTO;
                }).toList());
        return facturaDTO;
    }
}