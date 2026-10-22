# ADR-001: Procesamiento de Movimientos en Lote (Batch Movement) vs. Micro-movimientos Paso a Paso

* **Estado:** Aceptado
* **Fecha:** Enero 2025
* **Autores:** Ignacio Gutiérrez Serrera
* **Contexto Técnico:** Backend Spring Boot 3 (`SalmonMatchService`, `MatchController`), Frontend React 18 (`calculateValidMoves.ts`, `Game.tsx`)

---

## 1. Contexto y Planteamiento del Problema

En la implementación inicial del juego (etapa DP1), el desplazamiento de los bancos de salmones seguía un modelo **Per-Step (Paso a Paso síncrono)**:
1. Cada jugador dispone de 5 puntos de energía por turno.
2. Cada vez que el usuario hacía clic para mover un salmón una casilla (o saltar), el cliente enviaba una petición HTTP REST individual (`POST /api/v1/salmonMatches/move`) al servidor.
3. El backend validaba la casilla inmediata, descontaba la energía, persistía el estado en base de datos y devolvía la respuesta.
4. Si un jugador realizaba 5 movimientos individuales en su turno, se emitían **5 peticiones HTTP síncronas sucesivas** y se ejecutaban **5 transacciones y escrituras independientes** en la base de datos por cada turno de juego.

### Inconvenientes del modelo previo:
* **Latencia y degradación de la experiencia de usuario (UX):** Cada paso sufría el retardo de ida y vuelta de red (RTT). El jugador percibía interrupciones y retrasos (*lag*) entre cada casilla.
* **Sobrecarga de red y servidor:** La multiplicación de peticiones REST por jugador y turno saturaba innecesariamente los recursos del backend y las conexiones a base de datos.
* **Riesgo de estados inconsistentes:** Si el jugador sufría una desconexión en el paso 3 de 5, la partida quedaba bloqueada en un estado intermedio que violaba la regla de gasto obligatorio de los 5 puntos de energía.
* **Imposibilidad de previsualizar o rectificar:** El jugador no podía ver el coste acumulado ni deshacer un paso erróneo antes de gastar su turno.

---

## 2. Decisión Adoptada

Se decidió sustituir el modelo de micro-movimientos síncronos por un **paradigma de Procesamiento Atómico por Lotes (*Batch Movement*)**:

1. **Cálculo y Previsualización Local Optimista en Cliente (Ghost UI):**
   * El cliente React calcula de manera determinista las trayectorias legales mediante el motor cliente [`calculateValidMoves.ts`](../../frontend/src/match/calculateValidMoves.ts).
   * Al hacer clic en casillas, el cliente renderiza "salmones fantasma" (*Ghost UI*) previsualizando el camino, evaluando el consumo proyectado de energía y mostrando las amenazas atravesadas (osos, águilas, cascadas).
   * El usuario dispone de controles completos para rectificar o deshacer pasos (*Undo/Reset*) localmente sin coste de red.

2. **Confirmación Atómica Transaccional en Servidor:**
   * Una vez consumidos los 5 puntos de energía (o agotados los movimientos legales posibles), el cliente envía una única petición atómica consolidada:
     ```http
     PATCH /api/v1/salmonMatches/move-batch/{matchId}
     Content-Type: application/json
     ```
   * El backend procesa todo el lote dentro de una única transacción `@Transactional`, valida la veracidad del camino frente a las reglas del tablero, aplica los peligros y persiste el resultado en bloque.

3. **Notificación Push en Tiempo Real:**
   * Tras la persistencia atómica, el servidor emite un único mensaje de difusión por WebSockets STOMP hacia el topic `/topic/matches/{matchId}`, actualizando el estado de todos los jugadores conectados de forma instantánea.

---

## 3. Consecuencias

### Positivas
* **Reducción de tráfico de red del 80%:** Se pasa de 5 peticiones REST por turno a exactamente **1 petición única**.
* **Persistencia Atómica e Integridad:** O se valida y persiste el turno completo o se revierte íntegramente (principio ACID), imposibilitando estados huérfanos a medio turno.
* **Fluidez absoluta en la interfaz:** Cero latencia percibida al planificar el movimiento en pantalla.
* **Mecanismo de Rollback:** Si falla la red en la confirmación final, el cliente React restaura el estado previo (*Snapshot*) sin corromper la partida.

### Negativas / Compromisos asumidos
* **Duplicación de lógica:** Obliga a mantener las reglas de movimiento y costes tanto en TypeScript para el cálculo optimista en el cliente como en Java para la validación defensiva en el backend.
