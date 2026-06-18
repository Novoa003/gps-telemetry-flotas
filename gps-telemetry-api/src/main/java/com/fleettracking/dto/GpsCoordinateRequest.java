package com.fleettracking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Representa el payload de entrada del endpoint POST /gps.
 *
 * Usamos @JsonProperty explícito en cada campo (en vez de depender de
 * spring.jackson.property-naming-strategy=SNAKE_CASE) porque esa propiedad
 * está deprecada en Spring Boot 4 / Jackson 3, y queremos que el mapeo
 * vehicle_id <-> vehicleId sea explícito y estable sin importar la versión.
 */
public class GpsCoordinateRequest {

    @NotBlank(message = "vehicle_id es obligatorio y no puede estar vacío")
    @JsonProperty("vehicle_id")
    private String vehicleId;

    @NotNull(message = "lat es obligatorio")
    @DecimalMin(value = "-90.0", message = "lat debe ser mayor o igual a -90")
    @DecimalMax(value = "90.0", message = "lat debe ser menor o igual a 90")
    @JsonProperty("lat")
    private Double lat;

    @NotNull(message = "lng es obligatorio")
    @DecimalMin(value = "-180.0", message = "lng debe ser mayor o igual a -180")
    @DecimalMax(value = "180.0", message = "lng debe ser menor o igual a 180")
    @JsonProperty("lng")
    private Double lng;

    @NotBlank(message = "timestamp es obligatorio")
    @JsonProperty("timestamp")
    private String timestamp;

    public GpsCoordinateRequest() {
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}