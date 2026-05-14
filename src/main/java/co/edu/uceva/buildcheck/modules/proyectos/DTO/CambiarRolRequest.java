package co.edu.uceva.buildcheck.modules.proyectos.DTO;

import co.edu.uceva.buildcheck.modules.usuarios.model.Roles.RolNombre;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambiarRolRequest {
    
    @NotNull(message = "El nuevo rol no puede ser nulo")
    private RolNombre nuevoRol;
}
