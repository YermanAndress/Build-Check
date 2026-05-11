package co.edu.uceva.buildcheck.modules.proyectos.DTO;

import co.edu.uceva.buildcheck.modules.usuarios.model.Roles.RolNombre;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerarInvitacionRequest {
    
    @NotNull(message = "El rol por defecto no puede ser nulo")
    private RolNombre rolPorDefecto;
}
