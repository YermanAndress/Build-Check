package co.edu.uceva.buildcheck.modules.factura_material.DTO;

import lombok.Data;

@Data
public class FacturaMaterialDTO {
    private Long id;
    private String nombreMaterial;
    private Double cantidad;
    private Double precioUnitario;
    private Long FacturaId;
}
