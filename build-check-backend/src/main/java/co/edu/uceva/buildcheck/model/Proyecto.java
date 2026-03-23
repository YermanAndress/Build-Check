package co.edu.uceva.buildcheck.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter
public class Proyecto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "El nombre no puede estar vacio")
    @Size(min = 2, max = 200, message = "El tamaño del nombre debe estar entre 2 y 20 caracteres")
    @Column(nullable = false)
    private String nombre;

    @Size(max = 255, message = "La descripcion no puede tener mas de 255 caracteres")
    private String descripcion;

    @NotEmpty(message = "La ubicacion no puede estar vacia")
    @Column(nullable = false)
    private String ubicacion;

    @Min(value = 0, message = "El presupuesto no puede ser negativo")
    @NotNull(message = "El presupuesto no puede estar vacio")
    @Column(nullable = false)
    private Double presupuesto;

    @Pattern(
        regexp = "^(Planificacion|Ejecucion|Terminado)$",
        message = "El estado debe ser Planificacion, Ejecucion o Terminado"
    )
    private String estado;
}
