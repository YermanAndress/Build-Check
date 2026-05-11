package co.edu.uceva.buildcheck.modules.usuario_proyecto.model;

import co.edu.uceva.buildcheck.modules.usuarios.model.Usuario;
import co.edu.uceva.buildcheck.modules.usuarios.model.Roles.RolNombre;
import co.edu.uceva.buildcheck.modules.proyectos.model.Proyecto;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "usuario_proyecto")
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioProyecto {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol_proyecto", nullable = false)
    private RolNombre rolProyecto;

    @Column(name = "fecha_agregacion", nullable = false, updatable = false)
    private LocalDateTime fechaAgregacion;

    @PrePersist
    public void prePersist() {
        this.fechaAgregacion = LocalDateTime.now();
        if (this.rolProyecto == null) {
            this.rolProyecto = RolNombre.ROLE_VIEWER;
        }
    }
}
