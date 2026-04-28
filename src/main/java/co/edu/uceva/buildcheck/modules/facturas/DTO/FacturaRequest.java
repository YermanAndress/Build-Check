package co.edu.uceva.buildcheck.modules.facturas.DTO;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class FacturaRequest {

    @NotBlank(message = "El número de factura es obligatorio")
    private String numeroFactura;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    @NotBlank(message = "El proveedor es obligatorio")
    private String proveedor;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;

    @NotNull(message = "El valor total es obligatorio")
    @DecimalMin(value = "0", message = "El valor total debe ser positivo")
    private Double valorTotal;

    @NotBlank(message = "El ID del proyecto es obligatorio")
    private Long proyectoId;

    @NotNull(message = "Debe incluir al menos un item")
    @Valid
    private List<FacturaItemRequest> items;
}