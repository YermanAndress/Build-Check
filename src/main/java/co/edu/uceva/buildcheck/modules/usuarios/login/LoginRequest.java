package co.edu.uceva.buildcheck.modules.usuarios.login;

import lombok.Data;

@Data
public class LoginRequest {
    private String correo;
    private String password;
}
