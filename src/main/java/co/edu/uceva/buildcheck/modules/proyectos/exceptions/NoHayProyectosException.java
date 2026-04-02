package co.edu.uceva.buildcheck.modules.proyectos.exceptions;

public class NoHayProyectosException extends RuntimeException {
    public NoHayProyectosException() {
        super("No hay proyectos en la base de datos");
    }
}
