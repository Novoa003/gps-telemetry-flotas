package com.fleettracking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fleettracking.model.VehicleStatus;

public class VehicleResponse {

    @JsonProperty("vehicle_id")
    private String vehicleId;

    @JsonProperty("last_lat")
    private double lastLat;

    @JsonProperty("last_lng")
    private double lastLng;

    @JsonProperty("last_seen")
    private String lastSeen;

    @JsonProperty("status")
    private String status;

    public VehicleResponse() {
    }

    public VehicleResponse(String vehicleId, double lastLat, double lastLng, String lastSeen, VehicleStatus status) {
        this.vehicleId = vehicleId;
        this.lastLat = lastLat;
        this.lastLng = lastLng;
        this.lastSeen = lastSeen;
        this.status = status.getLabel();
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public double getLastLat() {
        return lastLat;
    }

    public void setLastLat(double lastLat) {
        this.lastLat = lastLat;
    }

    public double getLastLng() {
        return lastLng;
    }

    public void setLastLng(double lastLng) {
        this.lastLng = lastLng;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}