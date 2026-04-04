package co.edu.uceva.buildcheck.modules.facturas.DTO;

import lombok.Data;

@Data
public class FacturaItemRequest {
    private Long materialId;
    private Double cantidad;
    private Double precioUnitario;
}
