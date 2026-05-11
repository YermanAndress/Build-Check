package co.edu.uceva.buildcheck.modules.proyectos.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearProyectoRequest {

    @NotEmpty(message = "El nombre no puede estar vacio")
    @Size(min = 2, max = 200, message = "El tamaño del nombre debe estar entre 2 y 200 caracteres")
    private String nombre;

    @Size(max = 300, message = "La descripcion no puede tener mas de 300 caracteres")
    private String descripcion;

    @NotEmpty(message = "La ubicacion no puede estar vacia")
    private String ubicacion;

    @NotNull(message = "El presupuesto no puede ser nulo")
    @Min(value = 0, message = "El presupuesto no puede ser negativo")
    private Double presupuesto;

    @NotNull(message = "El estado no puede ser nulo")
    private String estado;
}
