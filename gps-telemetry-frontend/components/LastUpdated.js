// components/LastUpdated.js
"use client";

import { useEffect, useState } from "react";

/**
 * Muestra cuánto tiempo ha pasado desde la última actualización exitosa
 * del polling (ej: "Última actualización: hace 3 segundos").
 *
 * Recibe el timestamp (objeto Date) de la última vez que el polling tuvo
 * éxito, y se re-renderiza cada segundo para que el contador avance solo,
 * sin depender de que llegue una nueva respuesta del backend.
 */
export default function LastUpdated({ lastUpdatedAt }) {
  const [secondsAgo, setSecondsAgo] = useState(0);

  useEffect(() => {
    if (!lastUpdatedAt) return;

    const updateCounter = () => {
      const diffMs = Date.now() - lastUpdatedAt.getTime();
      setSecondsAgo(Math.floor(diffMs / 1000));
    };

    updateCounter();
    const intervalId = setInterval(updateCounter, 1000);

    return () => clearInterval(intervalId);
  }, [lastUpdatedAt]);

  if (!lastUpdatedAt) {
    return <span style={{ color: "#6b7280", fontSize: "14px" }}>Cargando...</span>;
  }

  return (
    <span style={{ color: "#6b7280", fontSize: "14px" }}>
      Última actualización: hace {secondsAgo} {secondsAgo === 1 ? "segundo" : "segundos"}
    </span>
  );
}