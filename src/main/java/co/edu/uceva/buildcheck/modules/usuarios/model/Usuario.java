package co.edu.uceva.buildcheck.modules.usuarios.model;

import co.edu.uceva.buildcheck.modules.usuarios.model.Roles.RolNombre;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Entity
@Data
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "No puede estar vacio")
    @Size(min = 2, max = 50, message = "El tamaño tiene que estar entre 2 y 50")
    @Column(nullable = false)
    private String nombre;

    @Email(message = "Debe ser un correo válido")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@(gmail\\.com|outlook\\.com|yahoo\\.com|icloud\\.com|protonmail\\.com|uceva\\.edu\\.co)$", message = "Solo se permiten correos de Gmail, Outlook, Yahoo, iCloud, ProtonMail o el institucional de la Uceva")
    @Column(nullable = false, unique = true)
    private String correo;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotEmpty(message = "La contraseña no puede estar vacia")
    @Column(nullable = false, length = 100)
    private String password;

    @NotNull(message = "El rol no puede ser nulo")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolNombre rol;

    @Column(nullable = false)
    private Boolean activo = true;
}
