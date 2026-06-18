package com.fleettracking.controller;

import com.fleettracking.dto.GpsCoordinateRequest;
import com.fleettracking.dto.VehicleResponse;
import com.fleettracking.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Expone los endpoints REST del sistema de telemetría.
 *
 * Esta clase NO contiene lógica de negocio: solo recibe la petición HTTP,
 * delega al VehicleService, y construye la respuesta con el código HTTP
 * correcto. Cualquier excepción lanzada por el service es capturada por
 * GlobalExceptionHandler, así que aquí no hay try/catch.
 */
@RestController
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    /**
     * POST /gps
     * Recibe una coordenada GPS de un vehículo.
     *
     * @Valid activa las anotaciones de jakarta.validation declaradas en
     * GpsCoordinateRequest (NotBlank, NotNull, DecimalMin/Max). Si alguna
     * falla, Spring lanza MethodArgumentNotValidException ANTES de que este
     * método se ejecute, y GlobalExceptionHandler la convierte en un 400.
     */
    @PostMapping("/gps")
    public ResponseEntity<Void> receiveCoordinate(@Valid @RequestBody GpsCoordinateRequest request) {
        vehicleService.registerCoordinate(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * GET /vehicles
     * Devuelve el estado actual de todos los vehículos.
     * Si no hay ninguno registrado, devuelve un array vacío con 200 (no 404).
     */
    @GetMapping("/vehicles")
    public ResponseEntity<List<VehicleResponse>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    /**
     * GET /vehicles/:id
     * Devuelve el estado de un vehículo puntual.
     * Si no existe, VehicleService lanza VehicleNotFoundException -> 404.
     */
    @GetMapping("/vehicles/{id}")
    public ResponseEntity<VehicleResponse> getVehicleById(@PathVariable("id") String id) {
        return ResponseEntity.ok(vehicleService.getVehicleById(id));
    }

    /**
     * DELETE /vehicles/:id
     * Elimina un vehículo del sistema.
     * Si no existe, VehicleService lanza VehicleNotFoundException -> 404.
     * Si se elimina con éxito, devolvemos 204 No Content (sin body),
     * que es el código semánticamente correcto para un DELETE exitoso.
     */
    @DeleteMapping("/vehicles/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable("id") String id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}