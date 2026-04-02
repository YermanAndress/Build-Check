package co.edu.uceva.buildcheck.modules.facturas.DTO;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class FacturaRequest {
    private String numeroFactura;
    private LocalDate fecha;
    private String proveedor;
    private String observaciones;
    private Double valorTotal;
    private Long proyectoId;
    private List<FacturaItemRequest> items;
}
