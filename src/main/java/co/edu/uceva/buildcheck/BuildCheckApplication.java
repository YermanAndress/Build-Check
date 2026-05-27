package co.edu.uceva.buildcheck;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class BuildCheckApplication {
    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        // Cargar el archivo .env
        // Dotenv dotenv = Dotenv.load();

        // Pasar cada variable del .env al sistema de Java
        dotenv.entries().forEach(entry -> {
            if (System.getProperty(entry.getKey()) == null &&
                    System.getenv(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });
        SpringApplication.run(BuildCheckApplication.class, args);
    }
}