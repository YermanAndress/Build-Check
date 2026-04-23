package co.edu.uceva.buildcheck;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class BuildCheckApplication {
    public static void main(String[] args) {
        // Cargar el archivo .env
        Dotenv dotenv = Dotenv.load();

        // Pasar cada variable del .env al sistema de Java
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        SpringApplication.run(BuildCheckApplication.class, args);
    }
}