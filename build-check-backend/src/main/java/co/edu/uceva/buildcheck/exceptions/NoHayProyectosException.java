package co.edu.uceva.buildcheck.exceptions;

public class NoHayProyectosException extends RuntimeException{
    public NoHayProyectosException() {
        super("No hay proyectos en la base de datos");
    }
}
