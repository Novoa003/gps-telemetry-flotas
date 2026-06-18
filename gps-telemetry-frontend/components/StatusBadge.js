// components/StatusBadge.js

// Mapeo de cada estado posible (tal como lo devuelve el backend) a un
// color y estilo visual. Centralizamos esto aquí para que tanto la tabla
// como los popups del mapa usen exactamente los mismos colores.
const STATUS_STYLES = {
  "En movimiento": {
    backgroundColor: "#dcfce7",
    color: "#166534",
    dotColor: "#22c55e",
  },
  Detenido: {
    backgroundColor: "#fef9c3",
    color: "#854d0e",
    dotColor: "#eab308",
  },
  "Sin señal": {
    backgroundColor: "#fee2e2",
    color: "#991b1b",
    dotColor: "#ef4444",
  },
};

export default function StatusBadge({ status }) {
  // Fallback por si llegara un estado inesperado del backend, para que
  // la UI nunca se rompa visualmente aunque el dato sea raro.
  const style = STATUS_STYLES[status] || {
    backgroundColor: "#f3f4f6",
    color: "#374151",
    dotColor: "#9ca3af",
  };

  return (
    <span
      style={{
        backgroundColor: style.backgroundColor,
        color: style.color,
        display: "inline-flex",
        alignItems: "center",
        gap: "6px",
        padding: "4px 10px",
        borderRadius: "9999px",
        fontSize: "13px",
        fontWeight: 500,
      }}
    >
      <span
        style={{
          width: "8px",
          height: "8px",
          borderRadius: "50%",
          backgroundColor: style.dotColor,
        }}
      />
      {status}
    </span>
  );
}