package co.edu.uceva.buildcheck.modules.proyectos.model;

import com.fasterxml.jackson.annotation.JsonFormat;

public class Estados {

    @JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING)
    public enum EstadoNombre {
        Pendiente,
        Planificacion,
        Ejecucion,
        Terminado,
    }
}
