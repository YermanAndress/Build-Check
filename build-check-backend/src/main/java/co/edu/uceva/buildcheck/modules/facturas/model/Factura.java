package co.edu.uceva.buildcheck.modules.facturas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

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

    @NotEmpty(message = "El nombre del proveedor no puede estar vacío")
    private String proveedor;

    @Size(max = 500, message = "La descripción es muy larga")
    private String observaciones;

    @NotNull(message = "El valor total es obligatorio")
    @DecimalMin(value = "0", message = "El total debe ser positivo")
    private Double valorTotal;

    @Column(name = "proyecto_id")
    private Long proyectoId;
}