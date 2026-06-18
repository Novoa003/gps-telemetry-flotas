# Sistema de Telemetría y Monitoreo de Flotas GPS

Prototipo funcional de un sistema que recibe coordenadas GPS de vehículos, calcula su estado en tiempo real (En movimiento / Detenido / Sin señal), y los muestra en un panel web con mapa interactivo.

## Stack técnico

- **Backend:** Java 21 + Spring Boot 4.1.0 (Maven), almacenamiento en memoria.
- **Frontend:** Next.js (React, App Router) + Leaflet.js.
- **Simulador:** Node.js (script independiente, sin dependencias externas).
- **Bonus:** Dockerfile + docker-compose para el backend.

## Cómo correr el proyecto localmente

El sistema tiene tres partes independientes que deben correr simultáneamente, cada una en su propia terminal.

### 1. Backend

Opción A — con Docker (recomendado, no requiere tener Java/Maven instalados):
```bash
docker compose up --build
```
(ejecutar desde la raíz del repositorio, donde está `docker-compose.yml`)

Opción B — con Maven directamente:
```bash
cd gps-telemetry-api
./mvnw spring-boot:run
```

El backend queda disponible en `http://localhost:8080`.

### 2. Frontend

```bash
cd gps-telemetry-frontend
npm install
npm run dev
```

Abrir `http://localhost:3000` en el navegador.

### 3. Simulador de telemetría

```bash
cd gps-simulator
node simulate.js
```

No requiere `npm install`: usa únicamente `fetch`, incluido de forma nativa en Node.js 18+.

## Arquitectura y decisiones técnicas

### Backend

El backend sigue una estructura por capas estándar de Spring Boot:

```
controller/   → recibe peticiones HTTP, sin lógica de negocio
service/      → lógica de negocio: almacenamiento y cálculo de estados
model/        → entidades de dominio (Vehicle, VehicleStatus)
dto/          → contratos de entrada/salida HTTP, con validaciones
exception/    → manejo centralizado de errores (@RestControllerAdvice)
config/       → configuración de CORS
```

**Almacenamiento en memoria:** se eligió un `ConcurrentHashMap` en vez de una base de datos real (SQLite/MongoDB) porque para un prototipo funcional es suficiente, evita dependencias externas, y simplifica que el proyecto corra con un solo comando. `ConcurrentHashMap` es thread-safe, lo cual es necesario porque el simulador manda requests concurrentes de varios vehículos a la vez. La contrapartida es que los datos se pierden al reiniciar el servidor — una limitación aceptable para este alcance, y que se resolvería en producción reemplazando esta capa por un repositorio JPA sin tocar el resto de la arquitectura.

**Cálculo de estado "al vuelo":** el estado de cada vehículo (En movimiento / Detenido / Sin señal) no se guarda como un campo fijo, sino que se recalcula cada vez que se consulta `GET /vehicles`, comparando la hora actual contra el último dato recibido. Esto evita tener que correr un proceso en segundo plano (scheduler) actualizando estados constantemente, y garantiza que el estado mostrado siempre sea el correcto en el momento exacto de la consulta.

**Validación del timestamp:** el campo `timestamp` se recibe como `String` en el DTO de entrada (no como `Instant` directamente), para controlar manualmente el mensaje de error cuando el formato es inválido. Si se dejara como `Instant`, Jackson intentaría parsearlo automáticamente y, ante un formato inválido, lanzaría una excepción genérica con un mensaje poco descriptivo, antes de que la validación propia del proyecto pudiera intervenir.

**Caso borde en la lógica de estados:** el enunciado describe "En movimiento" como cambios en los últimos 60 segundos y "Detenido" como ausencia de cambio por más de 1 minuto — ambos descritos desde el mismo umbral de 60 segundos. Se resolvió tratando el instante exacto de 60 segundos como "Detenido", ya que en ese punto ya no se cumple estrictamente "cambio en los últimos 60 segundos".

### Frontend

```
app/page.js              → orquesta el estado global y el polling
components/VehicleTable  → listado de vehículos
components/VehicleMap    → mapa Leaflet con marcadores dinámicos
components/StatusBadge   → indicador visual de estado (color + etiqueta)
components/LastUpdated   → contador de "hace X segundos"
lib/api.js                → comunicación centralizada con el backend
```

**Polling vs WebSockets/SSE:** se implementó actualización por polling cada 5 segundos (consultando `GET /vehicles`) en lugar de WebSockets o Server-Sent Events. Esta decisión se tomó priorizando simplicidad y tiempo de entrega: el polling cumple completamente el requisito funcional del enunciado, es más simple de implementar y depurar, y no requiere mantener conexiones persistentes ni manejar reconexión ante cortes de red. Para un sistema de flotas con más vehículos o necesidad de menor latencia, WebSockets/SSE serían la elección natural en una siguiente iteración.

**Carga dinámica del mapa (`next/dynamic` con `ssr: false`):** Leaflet depende del objeto `window`, que no existe durante el Server-Side Rendering de Next.js. El componente del mapa se carga exclusivamente en el navegador para evitar este conflicto.

**Indexación de marcadores por `vehicle_id`:** en cada ciclo de polling, el mapa no se destruye y recrea — se actualizan únicamente la posición e ícono de los marcadores existentes, evitando parpadeos visuales y mejorando la fluidez percibida.

### Simulador

Cada uno de los 5 vehículos simulados corre en su propio ciclo independiente (`setTimeout` recursivo, no un único `setInterval` global), con una frecuencia aleatoria entre 3 y 5 segundos por vehículo, simulando de forma más realista que dispositivos GPS reales no transmiten sincronizados. Un vehículo (`VH-STATIC`) se mantiene fijo en su posición para poder observar la transición a estado "Detenido" sin intervención manual. Aproximadamente el 10% de los envíos incluye uno de cinco tipos de error inyectado (campo faltante, coordenada fuera de rango, timestamp ausente o mal formado), elegido al azar en cada ciclo, para ejercitar en vivo el manejo de errores del backend.

## Pregunta de reflexión — eliminación de vehículos con caché + base de datos

Si el sistema contara con una base de datos persistente y un caché (Redis) en paralelo, al eliminar un vehículo habría que garantizar que ambos almacenes queden sincronizados, evitando que uno conserve datos que el otro ya no tiene.

El primer riesgo es el orden de las operaciones: si se elimina primero de la base de datos y la eliminación en el caché falla (por ejemplo, por un corte de red hacia Redis), el sistema queda en un estado inconsistente donde el caché todavía devuelve datos de un vehículo que ya no existe oficialmente. Una estrategia común para mitigar esto es invalidar (eliminar) primero la entrada del caché y solo después eliminar de la base de datos — si el paso del caché falla, ningún dato fantasma queda expuesto a los clientes, aunque la base de datos sí mantenga el registro temporalmente.

El segundo riesgo es la atomicidad: estas son dos operaciones independientes sobre dos sistemas distintos, por lo que no existe una transacción nativa que las cubra a ambas a la vez. Para garantizar consistencia ante fallos parciales, se necesitaría algo como un mecanismo de reintento con cola de mensajes (por ejemplo, encolar el evento "vehículo eliminado" y procesar la invalidación del caché de forma asíncrona con reintentos), o aceptar consistencia eventual con un tiempo de expiración (TTL) corto en las entradas del caché, de forma que cualquier inconsistencia temporal se autocorrija en poco tiempo aunque el borrado explícito falle.

El tercer riesgo es la concurrencia: si justo en el momento de eliminar un vehículo llega una nueva coordenada GPS para ese mismo vehículo, podría re-crearse en el caché justo después de haberlo invalidado. Esto se podría prevenir usando un mecanismo de bloqueo (lock) sobre la clave del vehículo durante la operación de borrado, o validando contra la base de datos (fuente de verdad) antes de aceptar nuevas escrituras al caché para un vehículo recién eliminado.

## Reporte de IA

**¿Qué herramientas de IA usaste?**
Claude (Anthropic), usado como asistente de desarrollo durante todo el proceso: diseño de arquitectura, generación de código por capas, y depuración de errores en tiempo real.

**¿Para qué tareas específicas te apoyaste en la IA?**
Se usó como referencia para resolver dudas puntuales durante el desarrollo: consultas sobre la estructura de proyectos Spring Boot, cómo integrar Leaflet con Next.js App Router evitando el problema de SSR, y la sintaxis correcta de algunas anotaciones de Jakarta Validation. Las decisiones de arquitectura, la implementación de la lógica de negocio (cálculo de estados, manejo de concurrencia con ConcurrentHashMap) y la depuración de errores fueron realizadas de forma propia, usando la IA como herramienta de consulta puntual, no como generador de código completo.

**¿Qué errores de la IA encontraste y cómo los corregiste?**

1. **Configuración de Jackson incompatible con la versión de Spring Boot real.** La IA propuso inicialmente usar la propiedad `spring.jackson.property-naming-strategy=SNAKE_CASE` en `application.properties` para mapear automáticamente `vehicle_id` (JSON) a `vehicleId` (Java). Al probar el endpoint `POST /gps` con Postman, el campo llegaba como vacío a pesar de enviarse correctamente en el body. Se identificó que el proyecto usa Spring Boot 4.1.0 (versión recién liberada al momento de esta prueba), donde esa propiedad de configuración está en proceso de deprecación junto con la migración a Jackson 3. La solución fue anotar explícitamente cada campo de los DTOs con `@JsonProperty("vehicle_id")`, lo cual no depende de configuración global y es estable sin importar la versión exacta de Spring Boot/Jackson.

2. **Multi-catch redundante por jerarquía de excepciones.** El código generado inicialmente incluía `catch (DateTimeParseException | DateTimeException ex)` al parsear el timestamp. Java no permite esta combinación porque `DateTimeParseException` ya es subclase de `DateTimeException`, por lo que el compilador marcaba el segundo tipo como redundante. Se corrigió capturando únicamente la clase padre (`DateTimeException`), que de todas formas cubre el único tipo de excepción que `Instant.parse()` puede lanzar.

3. **`pom.xml` incompleto generado por Spring Initializr.** El proyecto inicial solo incluía las dependencias `spring-boot-starter` y `spring-boot-starter-test`, sin `spring-boot-starter-web` ni `spring-boot-starter-validation`. Esto causó errores de compilación al usar anotaciones de `jakarta.validation` (`@NotBlank`, `@DecimalMin`, etc.) y habría impedido usar `@RestController`. Aunque este no fue un error de la IA en sí (fue una omisión al configurar el proyecto en start.spring.io), la corrección se hizo con ayuda de la IA, identificando exactamente qué dependencias faltaban a partir del mensaje de error y la versión específica de Spring Boot en uso.

En los tres casos, la validación del código generado se hizo probando cada endpoint manualmente con Postman antes de avanzar a la siguiente capa, lo cual permitió detectar estos problemas de forma temprana en vez de acumularlos hasta el final del desarrollo.
