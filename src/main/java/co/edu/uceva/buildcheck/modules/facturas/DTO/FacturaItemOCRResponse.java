package co.edu.uceva.buildcheck.modules.facturas.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacturaItemOCRResponse {
    private String nombre;
    private Double cantidad;
    private Double precioUnitario;
    private String unidadMedida;
}
