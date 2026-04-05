package co.edu.uceva.buildcheck.modules.movimientos.model;

import co.edu.uceva.buildcheck.modules.movimientos.model.tipoMovimiento.TipoMovimientoNombre;
import co.edu.uceva.buildcheck.modules.materiales.model.Material;
import co.edu.uceva.buildcheck.modules.proyectos.model.Proyecto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import java.time.LocalDate;
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
    private TipoMovimientoNombre tipoMovimiento; // ENTRADA o SALIDA

    // Cantidad
    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.1", message = "La cantidad debe ser mayor a 0")
    private Double cantidad;

    // Fecha
    @NotNull(message = "La fecha no puede estar vacia")
    private LocalDate fecha;

    @Column(name = "usuario_id")
    private Long usuarioId;

    private String evidenciaFotografica;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    @JsonIgnore
    private Proyecto proyecto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    @JsonIgnore
    private Material material;

    @Column(name = "usuario_creador", nullable = false, length = 100)
    private String usuarioCreador;

    public Long getMaterialId() {
        return material != null ? material.getId() : null;
    }

    public Long getProyectoId() {
        return proyecto != null ? proyecto.getId() : null;
    }

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.usuarioCreador == null) {
            this.usuarioCreador = "system";
        }
    }
}