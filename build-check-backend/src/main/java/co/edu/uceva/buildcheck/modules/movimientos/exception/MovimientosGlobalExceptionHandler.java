package co.edu.uceva.buildcheck.modules.movimientos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import co.edu.uceva.buildcheck.modules.movimientos.controller.MovimientoController;

import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Order(2)
@RestControllerAdvice(assignableTypes = MovimientoController.class)
public class MovimientosGlobalExceptionHandler {

    private static final String ERROR = "error";
    private static final String STATUS = "status";
    private static final String MENSAJE = "mensaje";

    // 1. Recurso no encontrado
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoSuchElementException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put(ERROR, "Recurso no encontrado");
        response.put(MENSAJE, ex.getMessage());
        response.put(STATUS, HttpStatus.NOT_FOUND.value());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // 2. Error de base de datos
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataError(DataIntegrityViolationException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put(ERROR, "Error de validacion de datos");
        response.put(MENSAJE, "No se puede realizar la operacion porque el registro esta vinculado a otros datos.");
        response.put(STATUS, HttpStatus.BAD_REQUEST.value());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 3. Error de validaciones (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();

        String errorMessage = ex.getBindingResult()
                                .getFieldErrors()
                                .get(0)
                                .getDefaultMessage();

        response.put(ERROR, "Error de validacion");
        response.put(MENSAJE, errorMessage);
        response.put(STATUS, HttpStatus.BAD_REQUEST.value());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 4. Error general
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericError(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put(ERROR, "Error interno del servidor");
        response.put(MENSAJE, "Ocurrio un error inesperado");
        response.put(STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}