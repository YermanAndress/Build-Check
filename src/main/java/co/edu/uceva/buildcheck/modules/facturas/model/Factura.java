package co.edu.uceva.buildcheck.modules.facturas.model;

import co.edu.uceva.buildcheck.modules.factura_material.model.FacturaMaterial;
import co.edu.uceva.buildcheck.modules.proveedores.model.Proveedor;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "facturas")
public class Factura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "El número de factura no puede estar vacío")
    @Column(unique = true, nullable = false)
    private String numeroFactura;

    @NotNull(message = "La fecha no puede estar vacía")
    private LocalDate fecha;

    @ManyToOne(optional = false)
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    @Size(max = 500, message = "La descripción es muy larga")
    private String observaciones;

    @NotNull(message = "El valor total es obligatorio")
    @DecimalMin(value = "0", message = "El total debe ser positivo")
    private Double valorTotal;

    @Column(name = "proyecto_id")
    private Long proyectoId;

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<FacturaMaterial> items = new ArrayList<>();

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

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