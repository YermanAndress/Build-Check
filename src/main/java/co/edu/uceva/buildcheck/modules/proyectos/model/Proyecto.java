package co.edu.uceva.buildcheck.modules.proyectos.model;

import co.edu.uceva.buildcheck.modules.movimientos.model.Movimiento;
import co.edu.uceva.buildcheck.modules.proyecto_invitacion.model.ProyectoInvitacion;
import co.edu.uceva.buildcheck.modules.proyectos.model.Estados.EstadoNombre;
import co.edu.uceva.buildcheck.modules.usuario_proyecto.model.UsuarioProyecto;
import co.edu.uceva.buildcheck.modules.usuarios.model.Usuario;
import com.fasterxml.jackson.annotation.JsonFormat;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_propietario_id")
    private Usuario usuarioPropietario;

    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<UsuarioProyecto> miembros = new ArrayList<>();

    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ProyectoInvitacion> invitaciones = new ArrayList<>();

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
    }

}
