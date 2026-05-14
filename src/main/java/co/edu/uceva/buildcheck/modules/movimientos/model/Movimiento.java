package co.edu.uceva.buildcheck.modules.movimientos.model;

import co.edu.uceva.buildcheck.modules.materiales.model.Material;
import co.edu.uceva.buildcheck.modules.movimientos.model.TipoMovimiento.TipoMovimientoNombre;
import co.edu.uceva.buildcheck.modules.proyectos.model.Proyecto;
import co.edu.uceva.buildcheck.modules.usuarios.model.Usuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;


import lombok.Data;

@Entity
@Data
@Table(name = "movimientos")
public class Movimiento {

    // ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Movimiento
    @NotNull(message = "El tipo de movimiento no puede estar vacio")
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoMovimientoNombre tipoMovimiento;

    // Cantidad
    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.1", message = "La cantidad debe ser mayor a 0")
    private Double cantidad;

    // Fecha
    @NotNull(message = "La fecha no puede estar vacia")
    private LocalDate fecha;

    private String evidenciaFotografica;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
