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

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "usos_restantes")
    private Integer usosRestantes;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creado_por", nullable = false)
    private Usuario createdBy;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.token == null || this.token.isEmpty()) {
            this.token = generateToken();
        }
        if (this.expiresAt == null) {
            // 10 días desde ahora
            this.expiresAt = LocalDateTime.now().plusDays(10);
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
        return this.activo && LocalDateTime.now().isBefore(this.expiresAt);
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
