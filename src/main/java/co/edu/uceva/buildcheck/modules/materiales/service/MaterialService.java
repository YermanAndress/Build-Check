package co.edu.uceva.buildcheck.modules.materiales.service;

import co.edu.uceva.buildcheck.modules.materiales.repository.MaterialRepository;
import co.edu.uceva.buildcheck.modules.movimientos.repository.MovimientoRepository;
import co.edu.uceva.buildcheck.exception.OperacionNoPermitidaException;
import co.edu.uceva.buildcheck.modules.factura_material.DTO.FacturaMaterialDTO;
import co.edu.uceva.buildcheck.modules.factura_material.repository.IFacturaMaterialRepository;
import co.edu.uceva.buildcheck.modules.materiales.DTO.MaterialDTO;
import co.edu.uceva.buildcheck.modules.materiales.model.Material;

import org.springframework.transaction.annotation.Transactional; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final MovimientoRepository movimientoRepository;
    private final IFacturaMaterialRepository facturaMaterialRepository;

    @Autowired
    public MaterialService(MaterialRepository materialRepository, MovimientoRepository movimientoRepository, IFacturaMaterialRepository facturaMaterialRepository){
        this.materialRepository = materialRepository;
        this.movimientoRepository = movimientoRepository;
        this.facturaMaterialRepository = facturaMaterialRepository;
    }
    //  Guarda un material nuevo
    @Transactional
    public Material save(Material material) {
        return materialRepository.save(material);
    }

    //  Elimina un material
    @Transactional
    public void delete(Material material) {
        boolean tieneMovimientos = movimientoRepository.existsByMaterial(material);
        boolean tieneFacturas = facturaMaterialRepository.existsByMaterial(material);
        if (!tieneMovimientos && !tieneFacturas) {
            materialRepository.delete(material);
        }else{
            throw new OperacionNoPermitidaException("No se puede eliminar el material porque tienen movimientos o facturas asociados");
        }
    }

    //  Busca un material por ID
    @Transactional(readOnly = true)
    public Optional<Material> findById(Long id) {
        return materialRepository.findById(id);
    }

    //  Actualiza un material existente
    @Transactional
    public Material update(Material material) {
        Material materialExistente = materialRepository.findById(material.getId())
                .orElseThrow(() -> new IllegalArgumentException("No existe el material con el ID: " + material.getId()));
        materialExistente.setNombre(material.getNombre());
        materialExistente.setDescripcion(material.getDescripcion());
        materialExistente.setUnidadMedida(material.getUnidadMedida());
        materialExistente.setPrecioUnitario(material.getPrecioUnitario());
        materialExistente.setStockActual(material.getStockActual());
        return materialRepository.save(materialExistente);
    }

    //  Lista todos los materiales
    @Transactional(readOnly = true)
    public List<Material> findAll() {
        return materialRepository.findAll();
    }

    public MaterialDTO toDTO(Material material) {
        MaterialDTO materialDTO = new MaterialDTO();
        materialDTO.setId(material.getId());
        materialDTO.setNombre(material.getNombre());
        materialDTO.setDescripcion(material.getDescripcion());
        materialDTO.setUnidadMedida(material.getUnidadMedida());
        materialDTO.setPrecioUnitario(material.getPrecioUnitario());
        materialDTO.setStockActual(material.getStockActual());
        materialDTO.setFechaCreacion(material.getFechaCreacion());
        materialDTO.setUsuarioCreador(material.getUsuarioCreador());
        materialDTO.setFacturas(
            material.getFacturas().stream().map(fm -> {
                FacturaMaterialDTO fMaterialDTO = new FacturaMaterialDTO();
                fMaterialDTO.setId(fm.getId());
                fMaterialDTO.setCantidad(fm.getCantidad());
                fMaterialDTO.setPrecioUnitario(fm.getPrecioUnitario());
                fMaterialDTO.setFacturaId(fm.getFactura().getId());
                return fMaterialDTO;
            }).toList()
        );
        return materialDTO;
    }

    public List<MaterialDTO> findAllDTO(){
        return materialRepository.findAll().stream().map(this::toDTO).toList();
    }
}