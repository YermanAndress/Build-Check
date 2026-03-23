package co.edu.uceva.buildcheck.modules.movimientos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@Table(name = "movimientos")
public class Movimiento {

    //ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Movimiento
    @NotEmpty(message = "El tipo de movimiento no puede estar vacio")
    @Column(nullable = false)
    private String tipoMovimiento; // ENTRADA o SALIDA

    // Cantidad
    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.1", message = "La cantidad debe ser mayor a 0")
    private Double cantidad;

    // Fecha
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;

    // Relación con Material
    //@ManyToOne
    //@JoinColumn(name = "material_id", nullable = false)
    //private co.edu.uceva.buildcheck.modules.materiales.model.Material material;

    @Column(name = "usuario_id")
    private Long usuarioId;

    private String evidenciaFotografica;
}