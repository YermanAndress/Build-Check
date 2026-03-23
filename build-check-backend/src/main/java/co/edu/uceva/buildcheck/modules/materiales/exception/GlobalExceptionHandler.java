package co.edu.uceva.buildcheck.modules.materiales.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;


@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String ERROR = "error";
    private static final String STATUS = "status";
    private static final String MENSAJE = "mensaje";

    // 1. Captura cuando buscas un ID que no existe (ej: .findById().orElseThrow())
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoSuchElementException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put(ERROR, "Recurso no encontrado");
        response.put(MENSAJE, ex.getMessage());
        response.put(STATUS, HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // 2. Captura errores de base de datos (ej: intentar borrar un material usado en otra tabla)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataError(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put(ERROR, "Error de validacion de datos");
        response.put(MENSAJE, "No se puede realizar la operación porque el material está vinculado a otros registros.");
        response.put(STATUS, HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericError(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put(ERROR, "Error interno del servidor");
        response.put(MENSAJE, "Ocurrió un error inesperado.");
        response.put(STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // Captura los errores de las validaciones (@Valid, @NotEmpty, etc.)
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(org.springframework.web.bind.MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        
        // Extraemos el primer error de validación que encuentre
        String errorMessage = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        
        response.put(ERROR, "Error de validación");
        response.put(MENSAJE, errorMessage);
        response.put(STATUS, HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}