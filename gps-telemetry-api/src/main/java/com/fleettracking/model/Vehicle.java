package com.fleettracking.model;

import java.time.Instant;

/**
 * Representa el estado actual conocido de un vehículo.
 * Esta es la entidad "viva" que se actualiza cada vez que llega una nueva coordenada GPS.
 */
public class Vehicle {

    private final String vehicleId;
    private double lastLat;
    private double lastLng;
    private Instant lastSeen;

    // Guardamos también la coordenada anterior para poder determinar
    // si el vehículo se movió o se quedó "Detenido" en el mismo punto.
    private double previousLat;
    private double previousLng;
    private Instant lastCoordinateChangeAt;

    public Vehicle(String vehicleId, double lastLat, double lastLng, Instant lastSeen) {
        this.vehicleId = vehicleId;
        this.lastLat = lastLat;
        this.lastLng = lastLng;
        this.lastSeen = lastSeen;

        // En la primera coordenada, "anterior" y "actual" son iguales,
        // y el reloj de "sin cambio de posición" arranca aquí.
        this.previousLat = lastLat;
        this.previousLng = lastLng;
        this.lastCoordinateChangeAt = lastSeen;
    }

    /**
     * Actualiza el vehículo con una nueva coordenada recibida.
     * Si la coordenada es distinta a la anterior, reinicia el contador de "tiempo sin moverse".
     */
    public void updatePosition(double newLat, double newLng, Instant newTimestamp) {
        boolean positionChanged = newLat != this.lastLat || newLng != this.lastLng;

        this.previousLat = this.lastLat;
        this.previousLng = this.lastLng;

        this.lastLat = newLat;
        this.lastLng = newLng;
        this.lastSeen = newTimestamp;

        if (positionChanged) {
            this.lastCoordinateChangeAt = newTimestamp;
        }
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public double getLastLat() {
        return lastLat;
    }

    public double getLastLng() {
        return lastLng;
    }

    public Instant getLastSeen() {
        return lastSeen;
    }

    public Instant getLastCoordinateChangeAt() {
        return lastCoordinateChangeAt;
    }
}