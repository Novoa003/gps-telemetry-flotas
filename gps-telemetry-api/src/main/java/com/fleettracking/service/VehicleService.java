package com.fleettracking.service;

import com.fleettracking.dto.GpsCoordinateRequest;
import com.fleettracking.dto.VehicleResponse;
import com.fleettracking.exception.VehicleNotFoundException;
import com.fleettracking.model.Vehicle;
import com.fleettracking.model.VehicleStatus;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Contiene toda la lógica de negocio del sistema de telemetría:
 * - Almacenamiento en memoria de los vehículos (ConcurrentHashMap).
 * - Validación/parseo del timestamp.
 * - Cálculo del estado de cada vehículo (En movimiento / Detenido / Sin señal).
 *
 * DECISIÓN DE ARQUITECTURA: usamos un ConcurrentHashMap en memoria en vez de
 * una base de datos real (SQLite/MongoDB) porque para un prototipo funcional
 * es suficiente, evita dependencias externas y simplifica el "correr el
 * proyecto en 3 comandos". ConcurrentHashMap es thread-safe, lo cual importa
 * porque el simulador va a mandar requests concurrentes de varios vehículos
 * a la vez. La contrapartida (documentada en el README) es que los datos se
 * pierden al reiniciar el servidor — aceptable para este alcance.
 */
@Service
public class VehicleService {

    // Umbrales de tiempo según las reglas de negocio del enunciado.
    private static final long EN_MOVIMIENTO_SEGUNDOS = 60;
    private static final long DETENIDO_MINIMO_SEGUNDOS = 60;
    private static final long SIN_SENAL_SEGUNDOS = 120;

    private final Map<String, Vehicle> vehicles = new ConcurrentHashMap<>();

    /**
     * Procesa una nueva coordenada GPS: valida el timestamp y crea o
     * actualiza el vehículo correspondiente.
     */
    public void registerCoordinate(GpsCoordinateRequest request) {
        Instant timestamp = parseTimestamp(request.getTimestamp());

        vehicles.compute(request.getVehicleId(), (id, existingVehicle) -> {
            if (existingVehicle == null) {
                return new Vehicle(id, request.getLat(), request.getLng(), timestamp);
            }
            existingVehicle.updatePosition(request.getLat(), request.getLng(), timestamp);
            return existingVehicle;
        });
    }

    /**
     * Devuelve el estado actual de todos los vehículos registrados.
     */
    public List<VehicleResponse> getAllVehicles() {
        Instant now = Instant.now();
        return vehicles.values().stream()
                .map(vehicle -> toResponse(vehicle, now))
                .collect(Collectors.toList());
    }

    /**
     * Devuelve el estado actual de un vehículo puntual.
     * Lanza VehicleNotFoundException si no existe (-> 404 vía GlobalExceptionHandler).
     */
    public VehicleResponse getVehicleById(String vehicleId) {
        Vehicle vehicle = vehicles.get(vehicleId);
        if (vehicle == null) {
            throw new VehicleNotFoundException(vehicleId);
        }
        return toResponse(vehicle, Instant.now());
    }

    /**
     * Elimina un vehículo del sistema.
     * Lanza VehicleNotFoundException si no existe.
     */
    public void deleteVehicle(String vehicleId) {
        Vehicle removed = vehicles.remove(vehicleId);
        if (removed == null) {
            throw new VehicleNotFoundException(vehicleId);
        }
    }

    /**
     * Traduce el modelo interno (Vehicle) al DTO de salida (VehicleResponse),
     * calculando el estado "al vuelo" según la hora actual.
     */
    private VehicleResponse toResponse(Vehicle vehicle, Instant now) {
        VehicleStatus status = calculateStatus(vehicle, now);
        return new VehicleResponse(
                vehicle.getVehicleId(),
                vehicle.getLastLat(),
                vehicle.getLastLng(),
                vehicle.getLastSeen().toString(),
                status
        );
    }

    /**
     * Lógica de estados según el enunciado:
     *
     * SIN_SENAL     -> no se reciben datos hace más de 2 minutos (prioridad más alta:
     *                  si no hay señal, no importa si antes se estaba moviendo o detenido).
     * EN_MOVIMIENTO -> coordenadas distintas recibidas en los últimos 60 segundos.
     * DETENIDO      -> misma coordenada sin cambio durante más de 1 minuto.
     */
    private VehicleStatus calculateStatus(Vehicle vehicle, Instant now) {
        long secondsSinceLastSeen = secondsBetween(vehicle.getLastSeen(), now);

        if (secondsSinceLastSeen > SIN_SENAL_SEGUNDOS) {
            return VehicleStatus.SIN_SENAL;
        }

        long secondsSinceLastChange = secondsBetween(vehicle.getLastCoordinateChangeAt(), now);

        if (secondsSinceLastChange > DETENIDO_MINIMO_SEGUNDOS) {
            return VehicleStatus.DETENIDO;
        }

        if (secondsSinceLastChange <= EN_MOVIMIENTO_SEGUNDOS) {
            return VehicleStatus.EN_MOVIMIENTO;
        }

        // Caso borde entre 60s y "más de 60s" por timing: lo tratamos como Detenido,
        // ya que implica que no ha cambiado de posición en el último minuto.
        return VehicleStatus.DETENIDO;
    }

    private long secondsBetween(Instant from, Instant to) {
        return to.getEpochSecond() - from.getEpochSecond();
    }

    /**
     * Parsea el timestamp string a Instant. Si el formato es inválido,
     * lanza IllegalArgumentException con un mensaje descriptivo, que el
     * GlobalExceptionHandler traduce a un 400.
     */
    private Instant parseTimestamp(String timestampStr) {
        try {
            return Instant.parse(timestampStr);
        } catch (DateTimeException ex) {
            throw new IllegalArgumentException(
                    "timestamp inválido, debe tener formato ISO 8601 (ej: 2025-06-01T10:00:00Z): " + timestampStr
            );
        }
    }
}
