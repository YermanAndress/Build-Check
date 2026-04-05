package co.edu.uceva.buildcheck.modules.proyectos.model;

import co.edu.uceva.buildcheck.modules.proyectos.model.Estados.EstadoNombre;
import co.edu.uceva.buildcheck.modules.movimientos.model.Movimiento;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Entity
@Data
@Table(name = "proyectos")
public class Proyecto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "El nombre no puede estar vacio")
    @Size(min = 2, max = 200, message = "El tamaño del nombre debe estar entre 2 y 20 caracteres")
    @Column(nullable = false)
    private String nombre;

    @Size(max = 300, message = "La descripcion no puede tener mas de 255 caracteres")
    private String descripcion;

    @NotEmpty(message = "La ubicacion no puede estar vacia")
    @Column(nullable = false)
    private String ubicacion;

    @Min(value = 0, message = "El presupuesto no puede ser negativo")
    @NotNull(message = "El presupuesto no puede estar vacio")
    @Column(nullable = false)
    private Double presupuesto;

    @NotNull(message = "El tipo de movimiento no puede estar vacio")
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoNombre estado;

    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, orphanRemoval = false)
    @JsonIgnore
    private List<Movimiento> movimientos = new ArrayList<>();

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
