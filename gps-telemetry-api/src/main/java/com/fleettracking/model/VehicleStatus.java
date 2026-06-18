package com.fleettracking.model;

/**
 * Estados posibles de un vehículo según las reglas de negocio del enunciado:
 *
 * EN_MOVIMIENTO -> coordenadas distintas recibidas en los últimos 60 segundos
 * DETENIDO      -> misma coordenada sin cambio durante más de 1 minuto
 * SIN_SENAL     -> no se reciben datos hace más de 2 minutos
 */
public enum VehicleStatus {

    EN_MOVIMIENTO("En movimiento"),
    DETENIDO("Detenido"),
    SIN_SENAL("Sin señal");

    private final String label;

    VehicleStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
