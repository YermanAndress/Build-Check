package co.edu.uceva.buildcheck.modules.movimientos.model;

import co.edu.uceva.buildcheck.modules.materiales.model.Material;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "movimientos")
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "El tipo de movimiento no puede estar vacio")
    @Column(nullable = false)
    private String tipoMovimiento;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.1", message = "La cantidad debe ser mayor a 0")
    private Double cantidad;

    @NotNull(message = "La fecha no puede estar vacia")
    private LocalDate fecha;

    // Relación con Material — nullable para no romper registros existentes
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "material_id", nullable = true)
    private Material material;

    // Campo extra para recibir solo el ID desde el frontend sin necesitar DTO
    // @Transient → no se persiste en BD, solo se usa para mapear el material en el controller
    @Transient
    private Long materialId;

    @Column(name = "usuario_id")
    private Long usuarioId;

    private String evidenciaFotografica;
}