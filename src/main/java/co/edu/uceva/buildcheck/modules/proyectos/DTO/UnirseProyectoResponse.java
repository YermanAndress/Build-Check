package co.edu.uceva.buildcheck.modules.proyectos.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnirseProyectoResponse {
    private String token;
    
    @JsonProperty("proyecto_id")
    private Long proyectoId;
    
    @JsonProperty("rol_proyecto")
    private String rolProyecto;
    
    private String mensaje;
}
