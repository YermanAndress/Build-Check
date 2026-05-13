package co.edu.uceva.buildcheck.modules.facturas.model;

import co.edu.uceva.buildcheck.modules.factura_material.model.FacturaMaterial;
import co.edu.uceva.buildcheck.modules.proveedores.model.Proveedor;
import co.edu.uceva.buildcheck.modules.proyectos.model.Proyecto;
import co.edu.uceva.buildcheck.modules.usuarios.model.Usuario;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

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

    @Size(max = 500, message = "La descripción es muy larga")
    private String observaciones;

    @NotNull(message = "El valor total es obligatorio")
    @DecimalMin(value = "0", message = "El total debe ser positivo")
    private Double valorTotal;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<FacturaMaterial> items = new ArrayList<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

}