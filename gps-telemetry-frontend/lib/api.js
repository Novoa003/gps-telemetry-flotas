// lib/api.js

// URL base de la API backend. La centralizamos aquí en vez de repetirla
// en cada componente, para que si cambia el puerto o el host (por ejemplo
// al desplegar a producción) solo haya que tocar una línea.
const API_BASE_URL = "http://localhost:8080";

/**
 * Obtiene el listado completo de vehículos desde el backend.
 * Lanza un error si la respuesta no es exitosa, para que el componente
 * que llama pueda mostrar un mensaje de error apropiado.
 */
export async function fetchVehicles() {
  const response = await fetch(`${API_BASE_URL}/vehicles`);

  if (!response.ok) {
    throw new Error(`Error al consultar vehículos: ${response.status}`);
  }

  return response.json();
}

/**
 * Elimina un vehículo del sistema por su ID.
 */
export async function deleteVehicle(vehicleId) {
  const response = await fetch(`${API_BASE_URL}/vehicles/${vehicleId}`, {
    method: "DELETE",
  });

  if (!response.ok) {
    throw new Error(`Error al eliminar vehículo: ${response.status}`);
  }
}