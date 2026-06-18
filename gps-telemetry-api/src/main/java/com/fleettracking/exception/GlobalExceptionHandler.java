package com.fleettracking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Centraliza el manejo de errores de toda la API.
 *
 * En vez de tener try/catch repetidos en cada endpoint del controller,
 * @RestControllerAdvice intercepta las excepciones lanzadas en cualquier
 * controller y las traduce al código HTTP y formato JSON correctos.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Se dispara automáticamente cuando @Valid falla sobre un DTO
     * (por ejemplo: vehicle_id vacío, lat fuera de rango, etc).
     * Junta todos los mensajes de los campos inválidos en un solo string.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException ex) {
        String combinedMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(" | "));

        ApiError error = new ApiError(combinedMessage, HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Errores de validación manual que lanzamos nosotros mismos en el service
     * (por ejemplo: timestamp con formato inválido, que no se puede validar
     * con anotaciones porque llega como String libre).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        ApiError error = new ApiError(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Vehículo no encontrado -> 404.
     */
    @ExceptionHandler(VehicleNotFoundException.class)
    public ResponseEntity<ApiError> handleVehicleNotFound(VehicleNotFoundException ex) {
        ApiError error = new ApiError(ex.getMessage(), HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Red de seguridad: cualquier excepción no anticipada cae aquí en vez
     * de tumbar el servidor con un stacktrace crudo hacia el cliente.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericError(Exception ex) {
        ApiError error = new ApiError("Error interno: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
