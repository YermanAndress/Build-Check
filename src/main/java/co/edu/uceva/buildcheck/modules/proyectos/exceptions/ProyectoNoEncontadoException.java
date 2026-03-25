package co.edu.uceva.buildcheck.modules.proyectos.exceptions;

public class ProyectoNoEncontadoException extends RuntimeException{
    public ProyectoNoEncontadoException(Long id){
        super("El proyecto con id " + id + " no fue encontrado");
    }
}
