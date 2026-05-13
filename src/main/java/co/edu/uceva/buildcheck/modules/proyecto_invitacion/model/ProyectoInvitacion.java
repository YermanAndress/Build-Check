package co.edu.uceva.buildcheck.modules.proyecto_invitacion.model;

import co.edu.uceva.buildcheck.modules.proyectos.model.Proyecto;
import co.edu.uceva.buildcheck.modules.usuarios.model.Usuario;
import co.edu.uceva.buildcheck.modules.usuarios.model.Roles.RolNombre;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "proyecto_invitacion")
@NoArgsConstructor
@AllArgsConstructor
public class ProyectoInvitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @Column(name = "token", unique = true, nullable = false, length = 255)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol_por_defecto", nullable = false)
    private RolNombre rolPorDefecto;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "usos_restantes")
    private Integer usosRestantes;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creado_por", nullable = false)
    private Usuario createdBy;

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.token == null || this.token.isEmpty()) {
            this.token = generateToken();
        }
        if (this.fechaExpiracion == null) {
            // 10 días desde ahora
            this.fechaExpiracion = LocalDateTime.now().plusDays(10);
        }
        if (this.usosRestantes == null) {
            this.usosRestantes = 7;
        }
        if (this.activo == null) {
            this.activo = true;
        }
        if (this.rolPorDefecto == null) {
            this.rolPorDefecto = RolNombre.ROLE_VIEWER;
        }
    }

    /**
     * Genera un token único de 32 caracteres
     */
    public static String generateToken() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 32);
    }

    /**
     * Valida si el token es válido (no expiró y está activo)
     */
    public boolean isValid() {
        return this.activo && LocalDateTime.now().isBefore(this.fechaExpiracion);
    }

    /**
     * Decrementa los usos restantes
     */
    public void decrementarUsos() {
        if (this.usosRestantes != null && this.usosRestantes > 0) {
            this.usosRestantes--;
        }
    }

    /**
     * Verifica si aún hay usos disponibles
     */
    public boolean tieneUsosDisponibles() {
        return this.usosRestantes == null || this.usosRestantes > 0;
    }
}
