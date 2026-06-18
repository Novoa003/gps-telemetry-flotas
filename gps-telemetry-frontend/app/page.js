// app/page.js
"use client";

import { useEffect, useState, useCallback } from "react";
import dynamic from "next/dynamic";
import VehicleTable from "@/components/VehicleTable";
import LastUpdated from "@/components/LastUpdated";
import { fetchVehicles, deleteVehicle } from "@/lib/api";

// Cargamos VehicleMap dinámicamente con ssr: false porque Leaflet depende
// del objeto "window", que no existe durante el renderizado en el servidor.
// Esto le dice a Next.js que solo cargue y ejecute este componente en el
// navegador, nunca en el servidor.
const VehicleMap = dynamic(() => import("@/components/VehicleMap"), {
  ssr: false,
  loading: () => (
    <div
      style={{
        height: "450px",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        backgroundColor: "#f3f4f6",
        borderRadius: "8px",
        color: "#6b7280",
      }}
    >
      Cargando mapa...
    </div>
  ),
});

// Intervalo de polling: cada 5 segundos, según pide el enunciado.
const POLLING_INTERVAL_MS = 5000;

export default function Home() {
  const [vehicles, setVehicles] = useState([]);
  const [lastUpdatedAt, setLastUpdatedAt] = useState(null);
  const [errorMessage, setErrorMessage] = useState(null);

  /**
   * Consulta el backend y actualiza el estado local.
   * useCallback evita recrear esta función en cada render, lo cual
   * importa porque la usamos como dependencia del useEffect de abajo.
   */
  const loadVehicles = useCallback(async () => {
    try {
      const data = await fetchVehicles();
      setVehicles(data);
      setLastUpdatedAt(new Date());
      setErrorMessage(null);
    } catch (error) {
      // Si el backend está caído o hay un error de red, lo mostramos
      // sin tumbar la UI -- el usuario sigue viendo los últimos datos
      // válidos que tenía, con un aviso de que algo falló.
      setErrorMessage("No se pudo conectar con el servidor. Verifica que el backend esté corriendo.");
    }
  }, []);

  /**
   * Maneja la eliminación de un vehículo desde la tabla, y refresca
   * la lista inmediatamente después (sin esperar al próximo ciclo de polling).
   */
  const handleDelete = useCallback(
    async (vehicleId) => {
      try {
        await deleteVehicle(vehicleId);
        await loadVehicles();
      } catch (error) {
        setErrorMessage(`No se pudo eliminar el vehículo ${vehicleId}.`);
      }
    },
    [loadVehicles]
  );

  // Carga inicial + polling cada 5 segundos.
  useEffect(() => {
    loadVehicles(); // primera carga inmediata, sin esperar 5s

    const intervalId = setInterval(loadVehicles, POLLING_INTERVAL_MS);

    return () => clearInterval(intervalId);
  }, [loadVehicles]);

  return (
    <main style={{ maxWidth: "1100px", margin: "0 auto", padding: "32px 24px" }}>
      <header style={{ marginBottom: "24px" }}>
        <h1 style={{ fontSize: "24px", fontWeight: 700, marginBottom: "4px" }}>
          Sistema de Telemetría de Flotas GPS
        </h1>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <p style={{ color: "#6b7280", fontSize: "14px" }}>
            {vehicles.length} {vehicles.length === 1 ? "vehículo registrado" : "vehículos registrados"}
          </p>
          <LastUpdated lastUpdatedAt={lastUpdatedAt} />
        </div>
      </header>

      {errorMessage && (
        <div
          style={{
            backgroundColor: "#fee2e2",
            color: "#991b1b",
            padding: "12px 16px",
            borderRadius: "8px",
            marginBottom: "16px",
            fontSize: "14px",
          }}
        >
          {errorMessage}
        </div>
      )}

      <section style={{ marginBottom: "24px" }}>
        <VehicleMap vehicles={vehicles} />
      </section>

      <section
        style={{
          border: "1px solid #e5e7eb",
          borderRadius: "8px",
          overflow: "hidden",
        }}
      >
        <VehicleTable vehicles={vehicles} onDelete={handleDelete} />
      </section>
    </main>
  );
}