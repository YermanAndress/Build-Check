package co.edu.uceva.buildcheck.modules.facturas.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacturaOCRResponse {
    private String numeroFactura;
    private LocalDate fecha;
    private String proveedor;
    private Double valorTotal;
    private String observaciones;
    private Long proyectoId;
    private Long usuarioId;
    private String urlImagen;
    private List<FacturaItemOCRResponse> items;
}
