package co.edu.uceva.buildcheck.modules.facturas.DTO;

import co.edu.uceva.buildcheck.modules.factura_material.DTO.FacturaMaterialDTO;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class FacturaDTO {
    private Long id;
    private String numeroFactura;
    private LocalDate fecha;
    private String proveedor;
    private Double valorTotal;
    private Long proyectoId;
    private List<FacturaMaterialDTO> items;
}
