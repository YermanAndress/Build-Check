package co.edu.uceva.buildcheck.modules.proyectos.exception;

public class ProyectoNoEncontradoException extends RuntimeException{
    public ProyectoNoEncontradoException(Long id){
        super("El proyecto con id " + id + " no fue encontrado");
    }
}
