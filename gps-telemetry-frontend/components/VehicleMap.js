// components/VehicleMap.js
"use client";

import { useEffect, useRef } from "react";
import L from "leaflet";
import "leaflet/dist/leaflet.css";

// Coordenadas centrales sugeridas en el enunciado (Bogotá), usadas como
// vista inicial del mapa antes de que lleguen datos de vehículos.
const BOGOTA_CENTER = [4.6750, -74.0721];
const DEFAULT_ZOOM = 12;

// Colores de marcador según estado, consistentes con StatusBadge.
const MARKER_COLORS = {
  "En movimiento": "#22c55e",
  Detenido: "#eab308",
  "Sin señal": "#ef4444",
};

/**
 * Crea un ícono de marcador circular coloreado según el estado del vehículo.
 * Usamos un divIcon (HTML/CSS) en vez de una imagen, para no depender de
 * archivos de imagen externos y poder controlar el color dinámicamente.
 */
function createMarkerIcon(status) {
  const color = MARKER_COLORS[status] || "#9ca3af";
  return L.divIcon({
    className: "vehicle-marker",
    html: `<div style="
      width: 20px;
      height: 20px;
      background-color: ${color};
      border: 3px solid white;
      border-radius: 50%;
      box-shadow: 0 1px 4px rgba(0,0,0,0.4);
    "></div>`,
    iconSize: [20, 20],
    iconAnchor: [10, 10],
  });
}

export default function VehicleMap({ vehicles }) {
  // Referencia al <div> del DOM donde Leaflet va a montar el mapa.
  const mapContainerRef = useRef(null);
  // Referencia a la instancia del mapa de Leaflet (persiste entre renders).
  const mapInstanceRef = useRef(null);
  // Referencia a los marcadores actuales, indexados por vehicle_id, para
  // poder actualizarlos o eliminarlos sin recrear el mapa completo.
  const markersRef = useRef({});

  // Efecto 1: inicializa el mapa UNA SOLA VEZ al montar el componente.
  useEffect(() => {
    if (mapInstanceRef.current) return; // ya inicializado, evita duplicados

    const map = L.map(mapContainerRef.current).setView(BOGOTA_CENTER, DEFAULT_ZOOM);

    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      attribution: "© OpenStreetMap contributors",
    }).addTo(map);

    mapInstanceRef.current = map;

    // Limpieza al desmontar el componente: evita memory leaks y el error
    // clásico de Leaflet "Map container is already initialized" si el
    // componente se vuelve a montar (por ejemplo en desarrollo con hot reload).
    return () => {
      map.remove();
      mapInstanceRef.current = null;
    };
  }, []);

  // Efecto 2: se ejecuta cada vez que cambia la lista de vehículos
  // (cada 5 segundos, por el polling). Actualiza o crea marcadores
  // sin recrear el mapa completo.
  useEffect(() => {
    const map = mapInstanceRef.current;
    if (!map) return;

    vehicles.forEach((vehicle) => {
      const position = [vehicle.last_lat, vehicle.last_lng];
      const existingMarker = markersRef.current[vehicle.vehicle_id];

      const popupContent = `
        <strong>${vehicle.vehicle_id}</strong><br/>
        Estado: ${vehicle.status}
      `;

      if (existingMarker) {
        // El vehículo ya tenía marcador: solo actualizamos posición,
        // ícono (por si cambió el estado) y contenido del popup.
        existingMarker.setLatLng(position);
        existingMarker.setIcon(createMarkerIcon(vehicle.status));
        existingMarker.setPopupContent(popupContent);
      } else {
        // Vehículo nuevo: creamos su marcador por primera vez.
        const marker = L.marker(position, { icon: createMarkerIcon(vehicle.status) })
          .addTo(map)
          .bindPopup(popupContent);
        markersRef.current[vehicle.vehicle_id] = marker;
      }
    });

    // Elimina del mapa los marcadores de vehículos que ya no existen
    // (por ejemplo, si el usuario los borró con el botón "Eliminar").
    const currentVehicleIds = new Set(vehicles.map((v) => v.vehicle_id));
    Object.keys(markersRef.current).forEach((vehicleId) => {
      if (!currentVehicleIds.has(vehicleId)) {
        markersRef.current[vehicleId].remove();
        delete markersRef.current[vehicleId];
      }
    });
  }, [vehicles]);

  return (
    <div
      ref={mapContainerRef}
      style={{ height: "450px", width: "100%", borderRadius: "8px" }}
    />
  );
}