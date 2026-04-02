package co.edu.uceva.buildcheck.modules.proyectos.exceptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(1)
@RestControllerAdvice(assignableTypes = co.edu.uceva.buildcheck.modules.proyectos.controller.ProyectoRestController.class)
public class ProyectosGlobalExceptionHandler {
    private static final String ERROR = "error";
    private static final String MENSAJE = "mensaje";
    private static final String PROYECTOS = "proyectos";
    private static final String STATUS = "status";

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "No hay proyectos en la base de datos");
        response.put(PROYECTOS, null);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @ExceptionHandler(ProyectoNoEncontadoException.class)
    public ResponseEntity<Map<String, Object>> handleProyectoNoEncontrado(ProyectoNoEncontadoException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put(ERROR, ex.getMessage());
        response.put(STATUS, HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, Object>> handleDataAccessException(DataAccessException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, "Error al consultar la base de datos");
        response.put(ERROR, ex.getMessage().concat(": " + ex.getMostSpecificCause().getMessage()));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(ValidationException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put(MENSAJE, ex.getMessage());
        response.put(ERROR, ex.getMessage());
        List<String> errors = ex.result.getFieldErrors()
                .stream()
                .map(err -> "El campo '" + err.getField() + "' " + err.getDefaultMessage())
                .toList();
        response.put(ERROR, errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(NoHayProyectosException.class)
    public ResponseEntity<Map<String, Object>> handleNoHayProyectos(NoHayProyectosException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", ex.getMessage());
        response.put("proyectos", null);
        response.put("status", HttpStatus.OK.value());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put(ERROR, "Error inesperado: " + ex.getMessage());
        response.put(STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
