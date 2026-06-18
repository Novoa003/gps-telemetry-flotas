package com.fleettracking.exception;

/**
 * Excepción lanzada cuando se busca un vehículo por su ID (GET /vehicles/:id
 * o DELETE /vehicles/:id) y no existe en el sistema.
 *
 * Es una RuntimeException para no obligar a declarar "throws" en cada método
 * del service/controller; la capturamos de forma centralizada en
 * GlobalExceptionHandler y la traducimos a un 404.
 */
public class VehicleNotFoundException extends RuntimeException {

    public VehicleNotFoundException(String vehicleId) {
        super("Vehículo no encontrado: " + vehicleId);
    }
}
