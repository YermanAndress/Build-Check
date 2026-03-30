package co.edu.uceva.buildcheck;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class BuildCheckApplication {
    public static void main(String[] args) {
        // 1. Cargar el archivo .env
        Dotenv dotenv = Dotenv.load();
        
        // 2. IMPORTANTE: Pasar cada variable del .env al sistema de Java
        dotenv.entries().forEach(entry -> 
            System.setProperty(entry.getKey(), entry.getValue())
        );

        // 3. Ahora sí, arrancar Spring
        SpringApplication.run(BuildCheckApplication.class, args);
    }
}