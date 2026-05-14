package co.edu.uceva.buildcheck.modules.proyectos.DTO;

import co.edu.uceva.buildcheck.modules.usuarios.model.Roles.RolNombre;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioProyectoDTO {
    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioCorreo;
    private RolNombre rolProyecto;
    private LocalDateTime fechaAgregacion;
}
