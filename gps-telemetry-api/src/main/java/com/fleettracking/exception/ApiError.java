package com.fleettracking.exception;

import java.time.Instant;

/**
 * Estructura estándar de error que devuelve la API en cualquier escenario
 * de falla (400, 404, etc). Mantener un formato consistente facilita que
 * el frontend maneje los errores de forma uniforme.
 */
public class ApiError {

    private String message;
    private int status;
    private Instant timestamp;

    public ApiError(String message, int status) {
        this.message = message;
        this.status = status;
        this.timestamp = Instant.now();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
