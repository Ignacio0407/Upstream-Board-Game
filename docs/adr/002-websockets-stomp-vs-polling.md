# ADR-002: Sincronización en Tiempo Real mediante WebSockets STOMP vs. Polling Continuo HTTP

* **Estado:** Aceptado
* **Fecha:** Febrero 2025
* **Autores:** Ignacio Gutiérrez Serrera
* **Contexto Técnico:** Backend Spring WebSocket / STOMP (`WebSocketSecurityConfig`, `WebSocketEventListener`), Frontend SockJS + `@stomp/stompjs` (`webSocketManager.ts`, `Chat.tsx`)

---

## 1. Contexto y Planteamiento del Problema

En las primeras versiones del sistema, la sincronización del estado de la partida entre diferentes jugadores se gestionaba mediante **sondeo periódico continuo (*Polling HTTP*)**:
* El componente `Match.tsx` ejecutaba un intervalo permanente:
  ```javascript
  setInterval(() => fetchMatchData(matchId), 5000);
  ```
* Cada 5 segundos, cada pestaña o dispositivo conectado emitía una petición `GET /api/v1/matches/{id}` para averiguar si el otro jugador había colocado una loseta, movido un salmón o enviado un mensaje por el chat.

### Inconvenientes del Polling Continuo:
* **Falsa sensación de tiempo real:** Los jugadores experimentaban una demora de hasta 5 segundos desde que el contrincante realizaba una jugada hasta que se reflejaba en su pantalla.
* **Consumo masivo e ineficiente de ancho de banda:** El 95% de las peticiones GET respondían con exactamente los mismos datos (sin cambios), desperdiciando CPU del servidor y conexiones de red.
* **Fuga de recursos en cliente (*Memory Leaks*):** Al navegar entre vistas (del Lobby al Tablero `Game.tsx`), el temporizador continuaba ejecutándose en segundo plano si no se desmontaba con precisión extrema.
* **Inviabilidad para el Chat:** Un chat multijugador con retardo de 5 segundos resulta impracticable.

---

## 2. Decisión Adoptada

Se decidió sustituir el mecanismo de sondeo por una **arquitectura híbrida orientada a eventos basada en WebSockets con subprotocolo STOMP sobre SockJS**:

1. **Patrón Snapshot + Delta:**
   * **Snapshot inicial (REST):** Al ingresar en la sala (`Lobby.tsx`) o en el tablero (`Game.tsx`), el cliente realiza una única petición REST (`GET /api/v1/matches/{id}`) para hidratar el estado inicial.
   * **Deltas en tiempo real (STOMP Topics):** El cliente se suscribe a los canales de mensajería reactiva a través del gestor centralizado [`webSocketManager.ts`](../../frontend/src/components/webSocketManager/webSocketManager.ts):
     - `/topic/matches/{matchId}`: Difusión de cambios de fase, colocación de losetas y movimientos de salmones.
     - `/topic/matches/{matchId}/chat`: Mensajería instantánea de chat por partida.
     - `/topic/matches`: Actualización reactiva del catálogo público de partidas en el Dashboard.

2. **Seguridad y Control de Tasa (*Rate Limiting*):**
   * Canales protegidos con autenticación JWT y soporte de comunicaciones seguras mediante WSS (*WebSocket Secure* / TLS).
   * Interceptores en backend que limitan la frecuencia de emisión del chat para evitar ataques de denegación de servicio o saturación de mensajes.

---

## 3. Consecuencias

### Positivas
* **Eliminación total del polling de fondo:** 0 peticiones HTTP periódicas automáticas en segundo plano.
* **Latencia casi nula (<50 ms):** Las acciones de los rivales (colocación de casillas, saltos de salmón y mensajes) aparecen instantáneamente en pantalla.
* **Escalabilidad y ahorro de recursos:** El servidor solo procesa y emite información cuando ocurre un evento real en el juego.
* **Sincronización instantánea de sala:** Cuando el creador pulsa "Iniciar Partida", todos los clientes navegan simultáneamente al tablero sin recargar la página.

### Negativas / Compromisos asumidos
* **Gestión de la conexión:** Requiere lógica de reconexión automática (*heartbeats*, reintentos exponenciales) en caso de caída temporal de red o microcortes en dispositivos móviles.
* **Certificados SSL en desarrollo local:** En entornos de desarrollo con HTTPS local, el navegador requiere aceptar previamente el certificado autofirmado para autorizar la conexión del socket seguro `wss://localhost:8080`.
