package co.edu.uceva.buildcheck.modules.proyectos.DTO;

import co.edu.uceva.buildcheck.modules.proyectos.model.Estados.EstadoNombre;
import co.edu.uceva.buildcheck.modules.usuarios.model.Roles.RolNombre;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProyectoDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private String ubicacion;
    private Double presupuesto;
    private EstadoNombre estado;
    private Long usuarioPropietarioId;
    private String usuarioPropietarioNombre;
    @JsonProperty("rolProyecto")
    private RolNombre rolDelUsuario;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaCreacion;

    private Integer totalMiembros;
    private Integer totalInvitacionesActivas;
}
