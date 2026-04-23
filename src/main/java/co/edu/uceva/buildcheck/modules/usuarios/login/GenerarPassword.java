package co.edu.uceva.buildcheck.modules.usuarios.login;

import java.util.Random;

public class GenerarPassword {
    public static String generarPassword() {
        int longitud = 12;
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < longitud; i++) {
            password.append(caracteres.charAt(random.nextInt(caracteres.length())));
        }
        return password.toString();
    }
}
