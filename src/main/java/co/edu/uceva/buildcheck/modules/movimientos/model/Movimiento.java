package co.edu.uceva.buildcheck.modules.movimientos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

import co.edu.uceva.buildcheck.modules.materiales.model.Material;
import co.edu.uceva.buildcheck.modules.proyectos.model.Proyecto;

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
    @NotNull(message = "La fecha no puede estar vacia")
    private LocalDate fecha;

    @Column(name = "usuario_id")
    private Long usuarioId;

    private String evidenciaFotografica;

    @ManyToOne(optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "usuario_creador", nullable = false, length = 100)
    private String usuarioCreador;

    @PrePersist
    public void prePersist(){
        this.fechaCreacion = LocalDateTime.now();
        if (this.usuarioCreador == null) {
            this.usuarioCreador = "system";
        }
    }
}