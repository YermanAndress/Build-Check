package co.edu.uceva.buildcheck.modules.materiales.model;

import co.edu.uceva.buildcheck.modules.factura_material.model.FacturaMaterial;
import co.edu.uceva.buildcheck.modules.movimientos.model.Movimiento;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
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

    private Integer stockReferencia = 0;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Movimiento> movimientos = new ArrayList<>();

    @OneToMany(mappedBy = "material", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<FacturaMaterial> facturas = new ArrayList<>();

    @Column(name = "usuario_creador", nullable = false, length = 100)
    private String usuarioCreador;

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.usuarioCreador == null) {
            this.usuarioCreador = "system";
        }
    }
}
