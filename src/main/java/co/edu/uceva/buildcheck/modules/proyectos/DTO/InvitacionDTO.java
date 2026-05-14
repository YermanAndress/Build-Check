package co.edu.uceva.buildcheck.modules.proyectos.DTO;

import co.edu.uceva.buildcheck.modules.usuarios.model.Roles.RolNombre;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvitacionDTO {
    private Long id;
    private String token;
    private RolNombre rolPorDefecto;
    private Integer usosRestantes;
    private Boolean activo;
    private LocalDateTime fechaExpiracion;
    private LocalDateTime fechaCreacion;
}
