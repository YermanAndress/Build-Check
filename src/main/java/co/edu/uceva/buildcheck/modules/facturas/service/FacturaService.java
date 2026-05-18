package co.edu.uceva.buildcheck.modules.facturas.service;

import co.edu.uceva.buildcheck.exception.OperacionNoPermitidaException;
import co.edu.uceva.buildcheck.exception.RecursoNoEncontradoException;
import co.edu.uceva.buildcheck.modules.factura_material.DTO.FacturaMaterialDTO;
import co.edu.uceva.buildcheck.modules.factura_material.model.FacturaMaterial;
import co.edu.uceva.buildcheck.modules.factura_material.repository.IFacturaMaterialRepository;
import co.edu.uceva.buildcheck.modules.facturas.DTO.FacturaDTO;
import co.edu.uceva.buildcheck.modules.facturas.DTO.FacturaItemRequest;
import co.edu.uceva.buildcheck.modules.facturas.DTO.FacturaRequest;
import co.edu.uceva.buildcheck.modules.facturas.model.Factura;
import co.edu.uceva.buildcheck.modules.facturas.repository.FacturaRepository;
import co.edu.uceva.buildcheck.modules.materiales.model.Material;
import co.edu.uceva.buildcheck.modules.materiales.repository.MaterialRepository;
import co.edu.uceva.buildcheck.modules.movimientos.model.Movimiento;
import co.edu.uceva.buildcheck.modules.movimientos.model.TipoMovimiento.TipoMovimientoNombre;
import co.edu.uceva.buildcheck.modules.movimientos.repository.MovimientoRepository;
import co.edu.uceva.buildcheck.modules.proveedores.model.Proveedor;
import co.edu.uceva.buildcheck.modules.proveedores.repository.ProveedorRepository;
import co.edu.uceva.buildcheck.modules.proyectos.model.Proyecto;
import co.edu.uceva.buildcheck.modules.proyectos.repository.IProyectoRepository;
import co.edu.uceva.buildcheck.modules.usuarios.model.Usuario;
import co.edu.uceva.buildcheck.modules.usuarios.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final IFacturaMaterialRepository facturaMaterialRepository;
    private final ProveedorRepository proveedorRepository;
    private final MaterialRepository materialRepository;
    private final MovimientoRepository movimientoRepository;
    private final IProyectoRepository iProyectoRepository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public FacturaService(
            FacturaRepository facturaRepository,
            IFacturaMaterialRepository facturaMaterialRepository,
            ProveedorRepository proveedorRepository,
            MaterialRepository materialRepository,
            MovimientoRepository movimientoRepository,
            IProyectoRepository iProyectoRepository,
            UsuarioRepository usuarioRepository) {
        this.facturaRepository = facturaRepository;
        this.facturaMaterialRepository = facturaMaterialRepository;
        this.proveedorRepository = proveedorRepository;
        this.materialRepository = materialRepository;
        this.movimientoRepository = movimientoRepository;
        this.iProyectoRepository = iProyectoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Guarda Una Factura Nueva
     */
    @Transactional
    public Factura save(FacturaRequest request) {
        // 1. Validar proyecto
        if (request.getProyectoId() == null) {
            throw new OperacionNoPermitidaException("La factura debe tener un proyecto asociado");
        }
        Proyecto proyecto = iProyectoRepository.findById(request.getProyectoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Proyecto no encontrado"));

        // 2. Obtener usuario (necesario para proveedor y factura)
        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        // 3. Proveedor (ahora podemos asignar el usuario)
        Proveedor proveedor = proveedorRepository.findByNombre(request.getProveedor())
                .orElseGet(() -> {
                    Proveedor nuevo = new Proveedor();
                    nuevo.setNombre(request.getProveedor());
                    nuevo.setUsuario(usuario);
                    nuevo.setFechaCreacion(LocalDateTime.now());
                    return proveedorRepository.save(nuevo);
                });

        // 4. Factura (igual que antes, pero con el proveedor ya con usuario)
        Optional<Factura> facturaExistente = facturaRepository.findByNumeroFactura(request.getNumeroFactura());
        if (facturaExistente.isPresent()) {
            throw new OperacionNoPermitidaException(
                    "Ya existe una factura con el número: " + request.getNumeroFactura());
        }
        Factura factura = new Factura();
        factura.setNumeroFactura(request.getNumeroFactura());
        factura.setFecha(request.getFecha());
        factura.setProveedor(proveedor);
        factura.setObservaciones(request.getObservaciones());
        factura.setValorTotal(request.getValorTotal());
        factura.setProyecto(proyecto);
        factura.setUsuario(usuario);
        factura.setFechaCreacion(LocalDateTime.now());

        // 5. Items y movimientos (sin cambios)
        List<FacturaMaterial> items = new ArrayList<>();
        for (FacturaItemRequest itemRequest : request.getItems()) {
            Material material = materialRepository.findByNombre(itemRequest.getNombre())
                    .orElseGet(() -> {
                        Material nuevo = new Material();
                        nuevo.setNombre(itemRequest.getNombre());
                        nuevo.setUnidadMedida(itemRequest.getUnidadMedida());
                        nuevo.setPrecioUnitario(itemRequest.getPrecioUnitario());
                        nuevo.setStockActual(0.0);
                        nuevo.setUsuario(usuario);
                        nuevo.setProyecto(proyecto);
                        nuevo.setFechaCreacion(LocalDateTime.now());
                        nuevo.setUsuario(usuario);
                        return materialRepository.save(nuevo);
                    });

            FacturaMaterial fm = new FacturaMaterial();
            fm.setFactura(factura);
            fm.setMaterial(material);
            fm.setCantidad(itemRequest.getCantidad());
            fm.setPrecioUnitario(itemRequest.getPrecioUnitario());
            fm.setUsuario(usuario);
            fm.setFechaCreacion(LocalDateTime.now());
            items.add(fm);

            Movimiento movimiento = new Movimiento();
            movimiento.setTipoMovimiento(TipoMovimientoNombre.ENTRADA);
            movimiento.setCantidad(itemRequest.getCantidad());
            movimiento.setFecha(factura.getFecha());
            movimiento.setMaterial(material);
            movimiento.setProyecto(proyecto);
            movimiento.setUsuario(usuario);
            movimiento.setFechaCreacion(LocalDateTime.now());

            movimientoRepository.save(movimiento);

            material.setStockActual(material.getStockActual() + itemRequest.getCantidad());
            materialRepository.save(material);
        }
        factura.setItems(items);
        return facturaRepository.save(factura);
    }

    /**
     * Elimina Una Factura
     */
    @Transactional
    public void delete(Factura factura) {
        boolean tieneMateriales = facturaMaterialRepository.existsByFactura(
                factura);
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
        Factura facturaExistente = facturaRepository
                .findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe la factura con el ID: " + id));
        facturaExistente.setNumeroFactura(factura.getNumeroFactura());
        facturaExistente.setFecha(factura.getFecha());
        facturaExistente.setObservaciones(factura.getObservaciones());
        facturaExistente.setValorTotal(factura.getValorTotal());

        // Actualizar proyecto
        if (factura.getProyectoId() != null) {
            Proyecto proyecto = iProyectoRepository
                    .findById(factura.getProyectoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Proyecto no encontrado"));
            facturaExistente.setProyecto(proyecto);
        }
        Usuario usuario = facturaExistente.getUsuario();
        Proveedor proveedor = proveedorRepository.findByNombre(factura.getProveedor())
                .orElseGet(() -> {
                    Proveedor nuevoProveedor = new Proveedor();
                    nuevoProveedor.setNombre(factura.getProveedor());
                    nuevoProveedor.setUsuario(usuario); // ✅
                    return proveedorRepository.save(nuevoProveedor);
                });
        facturaExistente.setProveedor(proveedor);
        facturaExistente.getItems().clear();
        if (factura.getItems() != null) {
            for (FacturaItemRequest itemRequest : factura.getItems()) {
                Material material;
                if (itemRequest.getMaterialId() != null) {
                    material = materialRepository
                            .findById(itemRequest.getMaterialId())
                            .orElseThrow(() -> new RecursoNoEncontradoException(
                                    "Material no encontrado con ID: " +
                                            itemRequest.getMaterialId()));
                } else {
                    material = materialRepository
                            .findByNombre(itemRequest.getNombre())
                            .orElseThrow(() -> new RecursoNoEncontradoException(
                                    "Material no encontrado con nombre: " +
                                            itemRequest.getNombre()));
                }

                FacturaMaterial facturaMaterial = new FacturaMaterial();
                facturaMaterial.setFactura(facturaExistente);
                facturaMaterial.setMaterial(material);
                facturaMaterial.setCantidad(itemRequest.getCantidad());
                facturaMaterial.setPrecioUnitario(
                        itemRequest.getPrecioUnitario());
                facturaMaterial.setFechaCreacion(LocalDateTime.now());
                facturaMaterial.setUsuario(facturaExistente.getUsuario());
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

    public FacturaDTO toDTO(Factura factura) {
        FacturaDTO facturaDTO = new FacturaDTO();
        facturaDTO.setId(factura.getId());
        facturaDTO.setNumeroFactura(factura.getNumeroFactura());
        facturaDTO.setFecha(factura.getFecha());
        facturaDTO.setProveedor(factura.getProveedor().getNombre());
        facturaDTO.setValorTotal(factura.getValorTotal());
        facturaDTO.setProyectoId(factura.getProyecto().getId());
        facturaDTO.setItems(
                factura
                        .getItems()
                        .stream()
                        .map(fm -> {
                            FacturaMaterialDTO fMaterialDTO = new FacturaMaterialDTO();
                            fMaterialDTO.setId(fm.getId());
                            fMaterialDTO.setCantidad(fm.getCantidad());
                            fMaterialDTO.setPrecioUnitario(fm.getPrecioUnitario());
                            fMaterialDTO.setFacturaId(factura.getId());
                            fMaterialDTO.setMaterialId(fm.getMaterial().getId());
                            fMaterialDTO.setNombreMaterial(
                                    fm.getMaterial().getNombre());
                            return fMaterialDTO;
                        })
                        .toList());
        return facturaDTO;
    }

    @Transactional(readOnly = true)
    public List<Factura> findByProyectoId(Long proyectoId) {
        return facturaRepository.findByProyectoId(proyectoId);
    }
}
