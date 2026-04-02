package co.edu.uceva.buildcheck.modules.materiales.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Entity
@Data
@Table(name = "materiales")
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "No puede estar vacio")
    @Size(min = 2, max = 20, message = "El tamaño tiene que estar entre 2 y 20")
    @Column(nullable = false)
    private String nombre;

    @Size(max = 255, message = "La descripción no puede tener más de 255 caracteres")
    private String descripcion;

    @NotEmpty(message = "La unidad de medida no puede estar vacia")
    @Column(nullable = false)
    private String unidadMedida;

    @DecimalMin(value = "0", message = "El precio unitario debe ser un valor positivo")
    private Double precioUnitario;

    @NotNull(message = "El stock actual no puede estar vacio")
    @Min(value = 0, message = "El stock actual debe ser un valor positivo")
    private Integer stockActual;

    @Column(name = "proyecto_id")
    private Long proyectoId;
}
