package co.edu.uceva.buildcheck.modules.proyectos.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnirseProyectoRequest {
    
    @NotEmpty(message = "El token no puede estar vacio")
    private String token;
}
