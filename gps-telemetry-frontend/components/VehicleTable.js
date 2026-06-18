// components/VehicleTable.js
"use client";

import StatusBadge from "./StatusBadge";

/**
 * Formatea el timestamp ISO que devuelve el backend a una hora legible
 * en formato local (ej: "10:00:05 a.m.").
 */
function formatTimestamp(isoString) {
  try {
    return new Date(isoString).toLocaleTimeString("es-CO");
  } catch {
    return isoString;
  }
}

export default function VehicleTable({ vehicles, onDelete }) {
  if (vehicles.length === 0) {
    return (
      <div style={{ padding: "24px", textAlign: "center", color: "#6b7280" }}>
        No hay vehículos registrados todavía. Esperando datos del simulador...
      </div>
    );
  }

  return (
    <table style={{ width: "100%", borderCollapse: "collapse" }}>
      <thead>
        <tr style={{ borderBottom: "2px solid #e5e7eb", textAlign: "left" }}>
          <th style={{ padding: "10px 12px", fontSize: "13px", color: "#6b7280" }}>ID Vehículo</th>
          <th style={{ padding: "10px 12px", fontSize: "13px", color: "#6b7280" }}>Estado</th>
          <th style={{ padding: "10px 12px", fontSize: "13px", color: "#6b7280" }}>Última transmisión</th>
          <th style={{ padding: "10px 12px", fontSize: "13px", color: "#6b7280" }}>Coordenadas</th>
          <th style={{ padding: "10px 12px", fontSize: "13px", color: "#6b7280" }}></th>
        </tr>
      </thead>
      <tbody>
        {vehicles.map((vehicle) => (
          <tr key={vehicle.vehicle_id} style={{ borderBottom: "1px solid #f3f4f6" }}>
            <td style={{ padding: "10px 12px", fontWeight: 600 }}>{vehicle.vehicle_id}</td>
            <td style={{ padding: "10px 12px" }}>
              <StatusBadge status={vehicle.status} />
            </td>
            <td style={{ padding: "10px 12px", color: "#374151" }}>
              {formatTimestamp(vehicle.last_seen)}
            </td>
            <td style={{ padding: "10px 12px", color: "#374151", fontSize: "13px" }}>
              {vehicle.last_lat.toFixed(4)}, {vehicle.last_lng.toFixed(4)}
            </td>
            <td style={{ padding: "10px 12px", textAlign: "right" }}>
              <button
                onClick={() => onDelete(vehicle.vehicle_id)}
                style={{
                  background: "none",
                  border: "1px solid #fca5a5",
                  color: "#dc2626",
                  borderRadius: "6px",
                  padding: "4px 10px",
                  fontSize: "13px",
                  cursor: "pointer",
                }}
              >
                Eliminar
              </button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}