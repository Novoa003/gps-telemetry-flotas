// gps-simulator/simulate.js

// URL del backend al que le vamos a mandar las coordenadas.
const API_URL = "http://localhost:8080/gps";

// Límites geográficos para Bogotá.
const LAT_MIN = 4.6;
const LAT_MAX = 4.75;
const LNG_MIN = -74.2;
const LNG_MAX = -73.95;

// Probabilidad de que un request salga con datos inválidos a propósito (~10%).
const ERROR_INJECTION_RATE = 0.1;

/**
 * Definición de los vehículos simulados.
 * VH-STATIC se queda fijo en su posición inicial para forzar el estado
 * "Detenido" después de 60 segundos sin cambio de coordenada.
 * Los demás se mueven en cada envío con un delta aleatorio pequeño.
 */
const vehicles = [
  { id: "VH-201", lat: 4.65, lng: -74.10, moving: true },
  { id: "VH-202", lat: 4.70, lng: -74.05, moving: true },
  { id: "VH-203", lat: 4.62, lng: -74.15, moving: true },
  { id: "VH-204", lat: 4.68, lng: -74.00, moving: true },
  { id: "VH-STATIC", lat: 4.64, lng: -74.08, moving: false },
];

/**
 * Genera un delta pequeño y aleatorio para simular movimiento real
 * (entre -0.003 y +0.003 grados, aproximadamente 200-300 metros por paso).
 */
function randomDelta() {
  return (Math.random() - 0.5) * 0.006;
}

/**
 * Mantiene la coordenada dentro de los límites de Bogotá, para que el
 * vehículo no "se escape" del mapa después de muchas iteraciones.
 */
function clamp(value, min, max) {
  return Math.min(Math.max(value, min), max);
}

/**
 * Construye el payload válido para un vehículo, actualizando su posición
 * si está marcado como "moving". Los vehículos estáticos siempre devuelven
 * la misma lat/lng.
 */
function buildValidPayload(vehicle) {
  if (vehicle.moving) {
    vehicle.lat = clamp(vehicle.lat + randomDelta(), LAT_MIN, LAT_MAX);
    vehicle.lng = clamp(vehicle.lng + randomDelta(), LNG_MIN, LNG_MAX);
  }

  return {
    vehicle_id: vehicle.id,
    lat: Number(vehicle.lat.toFixed(6)),
    lng: Number(vehicle.lng.toFixed(6)),
    timestamp: new Date().toISOString(),
  };
}

/**
 * Genera un payload intencionalmente inválido, para cumplir con el
 * requisito de inyección de errores (~10% de los requests).
 * Elige al azar uno de varios tipos de error posibles, para variar
 * lo que se ve en la consola y lo que el backend tiene que rechazar.
 */
function buildInvalidPayload(vehicle) {
  const errorTypes = [
    "missing_vehicle_id",
    "lat_out_of_range",
    "lng_out_of_range",
    "missing_timestamp",
    "malformed_timestamp",
  ];
  const errorType = errorTypes[Math.floor(Math.random() * errorTypes.length)];

  const basePayload = buildValidPayload(vehicle);

  switch (errorType) {
    case "missing_vehicle_id":
      delete basePayload.vehicle_id;
      break;
    case "lat_out_of_range":
      basePayload.lat = 999;
      break;
    case "lng_out_of_range":
      basePayload.lng = -999;
      break;
    case "missing_timestamp":
      delete basePayload.timestamp;
      break;
    case "malformed_timestamp":
      basePayload.timestamp = "fecha-invalida";
      break;
  }

  return { payload: basePayload, errorType };
}

/**
 * Envía una coordenada (válida o inválida) al backend y registra el
 * resultado en consola, incluyendo el status HTTP recibido.
 */
async function sendCoordinate(vehicle) {
  const shouldInjectError = Math.random() < ERROR_INJECTION_RATE;

  let payload;
  let errorType = null;

  if (shouldInjectError) {
    const result = buildInvalidPayload(vehicle);
    payload = result.payload;
    errorType = result.errorType;
  } else {
    payload = buildValidPayload(vehicle);
  }

  try {
    const response = await fetch(API_URL, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });

    const statusLabel = response.ok ? "OK" : "RECHAZADO";
    const errorNote = errorType ? ` (error inyectado: ${errorType})` : "";

    console.log(
      `[${vehicle.id}] ${statusLabel} - HTTP ${response.status}${errorNote}`
    );
  } catch (networkError) {
    console.error(`[${vehicle.id}] ERROR DE RED: ${networkError.message}`);
  }
}

/**
 * Arranca el ciclo de envío de un vehículo: manda una coordenada, espera
 * un tiempo aleatorio entre 3 y 5 segundos, y repite indefinidamente.
 * Cada vehículo tiene su propio temporizador independiente, para que no
 * todos manden datos exactamente al mismo tiempo (más realista).
 */
function startVehicleLoop(vehicle) {
  async function loop() {
    await sendCoordinate(vehicle);
    const nextDelayMs = 3000 + Math.random() * 2000; // entre 3000 y 5000 ms
    setTimeout(loop, nextDelayMs);
  }
  loop();
}

console.log(`Simulador de telemetría GPS iniciado. Enviando datos a ${API_URL}`);
console.log(`Vehículos simulados: ${vehicles.map((v) => v.id).join(", ")}`);
console.log(`Vehículo estático (para probar "Detenido"): VH-STATIC`);
console.log("Presiona Ctrl+C para detener.\n");

vehicles.forEach(startVehicleLoop);