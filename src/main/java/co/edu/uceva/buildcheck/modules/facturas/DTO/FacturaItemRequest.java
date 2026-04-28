package co.edu.uceva.buildcheck.modules.facturas.DTO;

import co.edu.uceva.buildcheck.modules.materiales.model.UnidadMedida;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class FacturaItemRequest {

    private Long materialId;

    @NotBlank(message = "El nombre del material es obligatorio")
    private String nombre;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Double cantidad;

    @NotNull(message = "El precio unitario es obligatorio")
    @Min(value = 0, message = "El precio unitario debe ser positivo")
    private Double precioUnitario;

    @NotNull(message = "La unidad de medida es obligatoria")
    private UnidadMedida unidadMedida;
}