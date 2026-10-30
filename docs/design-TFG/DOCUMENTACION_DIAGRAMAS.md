# Documentación Técnica de Diagramas de Arquitectura y Diseño (UML & Flow)
## Proyecto: Upstream Board Game (Trabajo de Fin de Grado - Grado en Ingeniería Informática)

Este documento centraliza y describe la totalidad de los 29 diagramas que modelan la arquitectura, el comportamiento dinámico, la infraestructura y las reglas de negocio del sistema Upstream. Los diagramas se encuentran estrictamente numerados en su **orden secuencial óptimo de lectura**, agrupados en cuatro bloques conceptuales para facilitar la comprensión progresiva a cualquier nuevo desarrollador o miembro del tribunal académico.

Cada diagrama responde al principio de **"Un diagrama, un concepto"**, asegurando una estricta delimitación de responsabilidades y máxima cohesión.

---

## BLOQUE I: FUNDAMENTOS DEL DOMINIO, ESTADOS Y REQUISITOS (01 - 04)

### 01. Modelo de Dominio JPA - Núcleo de Partida y Entidades
* **Archivos:** `01_Clase_interaccion_partida.puml` / `svg/01_Clase_interaccion_partida.svg`
* **Tipo UML:** Diagrama de Clases
* **Concepto:** Estructura relacional del núcleo del juego. Modela las entidades JPA (`User`, `Match`, `Player`, `SalmonMatch`, `MatchTile`, `Salmon`, `Tile`, `Message`), su generalización a partir de `BaseEntity` y `NamedEntity`, la clase embebida `@Embeddable Coordinate` y los tipos enumerados tipados (`State`, `Phase`, `Color`, `TileType`, `Authority`).

### 02. Máquina de Estados: Ciclo de Vida de la Partida
* **Archivos:** `02_Estado_ciclo_partida.puml` / `svg/02_Estado_ciclo_partida.svg`
* **Tipo UML:** Diagrama de Máquina de Estados
* **Concepto:** Ciclo de vida formal de la entidad `Match`. Modela las transiciones entre los estados persistentes `WAITING`, `ON_GOING` y `FINALIZED`, detallando la alternancia entre `Phase.TILES` y `Phase.MOVING` y la transición en bucle de la fase de movimiento cuando el mazo de losetas se agota.

### 03. Casos de Uso del Jugador y Dinámica de Partida
* **Archivos:** `03_CasosUso_Jugador.puml` / `svg/03_CasosUso_Jugador.svg`
* **Tipo UML:** Diagrama de Casos de Uso
* **Concepto:** Visión funcional centrada en los actores Jugador Registrado, Anfitrión y Visitante. Modela el registro, autenticación, creación de salas, unión a salas de espera, selección de color exclusivo, turnos de colocación y rotación, turnos de movimiento en lote, chat de partida y consulta de podio y logros.

### 04. Casos de Uso del Administrador
* **Archivos:** `04_CasosUso_admin.puml` / `svg/04_CasosUso_admin.svg`
* **Tipo UML:** Diagrama de Casos de Uso
* **Concepto:** Requisitos funcionales y capacidades de gestión del actor Administrador: gestión de usuarios, auditoría de partidas, configuración de logros, filtrado con ordenación dinámica y edición masiva.

---

## BLOQUE II: DINÁMICA DE PARTIDA, FASES Y TIEMPO REAL (05 - 10)

### 05. Sala de Espera (Lobby): Entrada Concurrente, Asignación de Color y Arranque de Partida
* **Archivos:** `05_Secuencia_Ciclo_Lobby_ColorPicker.puml` / `svg/05_Secuencia_Ciclo_Lobby_ColorPicker.svg`
* **Tipo UML:** Diagrama de Secuencia
* **Concepto:** Orquestación previa al inicio del juego. Modela la incorporación de jugadores a la sala, la selección de color exclusivo mediante el componente modal `ColorHandler`, la creación del jugador con `createPlayerInMatch` (`POST /api/v1/players/match/{matchId}`), la inicialización de sus salmones en el mar (`POST /api/v1/salmonMatches/player/{id}`), la notificación reactiva a los demás participantes (`/topic/match/newPlayer/{matchId}`), la validación de quórum de 2 a 5 participantes y el arranque coordinado de la partida mediante `startGame`.

### 06. Motor de Juego: Coordinación de Fases y Turnos
* **Archivos:** `06_Flujo_coordinacion_fases_partida.puml` / `svg/06_Flujo_coordinacion_fases_partida.svg`
* **Tipo UML:** Diagrama de Actividad / Flujo
* **Concepto:** Algoritmo orquestador de `MatchService.changePhase`. Describe la verificación de turnos, el cambio entre colocación de losetas y movimiento, el avance y purga del río (desplazamiento vertical y eliminación en `y == round - 8`), la regla de purga de salmones rezagados en el océano en Ronda 2 y las condiciones de cierre.

### 07. Fase de Losetas (Phase.TILES): Rotación en Tiempo Real y Colocación en el Tablero
* **Archivos:** `07_Secuencia_Colocacion_Rotacion_Losetas.puml` / `svg/07_Secuencia_Colocacion_Rotacion_Losetas.svg`
* **Tipo UML:** Diagrama de Secuencia
* **Concepto:** Flujo interactivo de la fase de preparación y avance del río. Modela la selección de loseta del mazo, la rotación concurrente de casillas `BEAR` o `JUMP` transmitida en tiempo real (`/topic/tiles/rotation/{id}`), la validación de colocación vertical según la ronda (`validateTilePlacement`), la mutación de casillas especiales (`eagleToWater`) y la colocación efectiva (`/topic/tiles/place/{id}`).

### 08. Motor de Reglas: Validación de Movimiento y Cálculo de Amenazas
* **Archivos:** `08_Flujo_movimiento_salmones.puml` / `svg/08_Flujo_movimiento_salmones.svg`
* **Tipo UML:** Diagrama de Actividad / Flujo
* **Concepto:** Algoritmo puro de reglas de negocio para evaluar un paso de salmón. Modela el cómputo de energía (nado vs salto sobre casillas llenas) y la resolución de obstáculos (osos, águilas, cascadas y garzas). Este motor se ejecuta tanto en el cliente (`calculateValidMoves.ts`) como en el servidor (`validateMoveRules`).

### 09. Secuencia de Turno de Juego: Ejecución de Movimiento y Sincronización Real-Time
* **Archivos:** `09_Secuencia_jugar_partida.puml` / `svg/09_Secuencia_jugar_partida.svg`
* **Tipo UML:** Diagrama de Secuencia
* **Concepto:** Orquestación holística de un turno completo de juego. Muestra la interacción entre el Jugador, el frontend en React, el controlador REST (`PATCH /move-batch`), la delegación de validación de reglas al Diagrama 08, el servicio transaccional, los repositorios JPA, la difusión reactiva mediante STOMP a `/topic/tilesAndSalmons/{matchId}` y la conclusión del turno o cambio de fase mediante `PATCH /matches/{matchId}/changePhase/{playerId}`, delegando la coordinación de fases en el Diagrama 06.

### 10. Finalización de Partida: Cálculo de Puntuaciones, Podio y Desbloqueo de Logros
* **Archivos:** `10_Secuencia_Fin_Partida_Podio_Logros.puml` / `svg/10_Secuencia_Fin_Partida_Podio_Logros.svg`
* **Tipo UML:** Diagrama de Secuencia
* **Concepto:** Cierre de partida y concesión de recompensas. Describe la transición a `State.FINALIZED`, el cálculo de puntos y bonificaciones de desove en `MatchService.calculateFinalScore`, la actualización acumulada de estadísticas en base de datos (`playedgames`, `victories`, `totalpoints`) y la delegación directa en `UserAchievementService` para la evaluación polimórfica de logros mediante el patrón Strategy (`AchievementUnlocker`).

---

## BLOQUE III: ARQUITECTURA TÉCNICA, FRONTEND Y COMUNICACIÓN (11 - 17)

### 11. Arquitectura Global del Sistema y Flujo de Comunicación
* **Archivos:** `11_Componente_Arquitectura_Sistema.puml` / `svg/11_Componente_Arquitectura_Sistema.svg`
* **Tipo UML:** Diagrama de Componentes
* **Concepto:** Visión holística de componentes y protocolos del sistema. Modela el cliente React 18 (Bootstrap 5 y Reactstrap), el túnel perimetral Ngrok (HTTPS/WSS), la capa de seguridad backend (JWT y WebSocket Interceptor), controladores REST, Message Broker STOMP, servicios transaccionales, repositorios JPA y la base de datos relacional.

### 12. Arquitectura en Capas Limpia del Backend (N-Tier Pattern)
* **Archivos:** `12_Clase_Arquitectura_Capas_Backend.puml` / `svg/12_Clase_Arquitectura_Capas_Backend.svg`
* **Tipo UML:** Diagrama de Clases
* **Concepto:** Organización arquitectónica en capas desacopladas de Spring Boot 3. Modela la interacción vertical entre Controladores REST (`BaseRestControllerWithDTO`), Capa de Mapeo y DTOs (`EntityMapper`, records y payloads), Servicios de Negocio (`BaseServiceWithDTO`), Repositorios de Acceso a Datos (`BaseRepository`) y Entidades de Dominio (`NamedEntity`, `BaseEntity`).

### 13. Arquitectura Modular de Componentes del Frontend (React + TypeScript)
* **Archivos:** `13_Componentes_React_Frontend.puml` / `svg/13_Componentes_React_Frontend.svg`
* **Tipo UML:** Diagrama de Componentes
* **Concepto:** Mapa estructural de la Single Page Application (SPA). Organiza los módulos de enrutamiento y guardias (`App.tsx`, `PrivateRoute.tsx`), módulos de autenticación (`Login`, `Register`, `Logout`), vistas principales de juego y administración, motor de cálculo cliente (`calculateValidMoves.ts`, `matchUtil.tsx`), sistema de tablas y búsqueda, y servicios de red.

### 14. Sincronización Reactiva del Tablero (Ciclo en Cliente)
* **Archivos:** `14_Secuencia_sincronizacion_tablero.puml` / `svg/14_Secuencia_sincronizacion_tablero.svg`
* **Tipo UML:** Diagrama de Secuencia
* **Concepto:** Ciclo de vida de la conexión y renderizado en `Game.tsx`. Detalla la carga inicial vía REST (`useFetchState`), la conexión SockJS/STOMP, la recepción reactiva de payloads consolidados, la reconstrucción inmutable de la cuadrícula con `buildGridFromSnapshot` y la desconexión limpia al desmontar la vista.

### 15. Integración del Chat: Ingesta REST y Difusión STOMP
* **Archivos:** `15_Secuencia_Chat.puml` / `svg/15_Secuencia_Chat.svg`
* **Tipo UML:** Diagrama de Secuencia
* **Concepto:** Flujo real de mensajería instantánea de la partida. Modela la ingesta del mensaje mediante una petición HTTP REST `POST /api/v1/messages/send`, la sanitización anti-XSS y guardado en `MessageRepository`, y la posterior difusión reactiva a todos los clientes conectados a través de `SimpMessagingTemplate` hacia `/topic/chat/{matchId}`.

### 16. Ciclo Global de Seguridad: Autenticación JWT y Autorización Multi-Protocolo
* **Archivos:** `16_Secuencia_Autenticacion_JWT_Global.puml` / `svg/16_Secuencia_Autenticacion_JWT_Global.svg`
* **Tipo UML:** Diagrama de Secuencia
* **Concepto:** Flujo transversal de identidad y seguridad. Modela el inicio de sesión (`/api/v1/auth/signin`), el almacenamiento seguro en `tokenService`, la inyección de cabeceras `Authorization: Bearer` en peticiones REST mediante `AuthTokenFilter`, y el apretón de manos con autenticación de sesión STOMP en `WebSocketSecurityInterceptor`.

### 17. Seguridad en WebSockets: Autenticación JWT y Defensas DoS
* **Archivos:** `17_Secuencia_Seguridad_WebSocket.puml` / `svg/17_Secuencia_Seguridad_WebSocket.svg`
* **Tipo UML:** Diagrama de Secuencia
* **Concepto:** Mecanismos de seguridad a nivel de canal STOMP implementados en `WebSocketSecurityInterceptor`. Modela la autenticación en el comando `CONNECT`, la asignación del usuario a la sesión y las defensas contra ataques DoS: Rate Limiting (máximo 60 peticiones/minuto por usuario), validación de tamaño de payload (límite de 64 KB) y comprobación de pertenencia a la partida en caché concurrente.

---

## BLOQUE IV: OPTIMIZACIÓN, INFRAESTRUCTURA Y TESTING AVANZADO (18 - 29)

### 18. Procesamiento de Movimientos en Lote (Batch Movement Paradigm)
* **Archivos:** `18_Secuencia_Procesamiento_Lote_Movimientos.puml` / `svg/18_Secuencia_Procesamiento_Lote_Movimientos.svg`
* **Tipo UML:** Diagrama de Secuencia
* **Concepto:** Diagrama canónico del paradigma de movimiento por lotes. Detalla la selección y acumulación local de acciones en el cliente, la confirmación agrupada mediante `PATCH /api/v1/salmonMatches/move-batch/{matchId}`, la validación y ejecución en memoria en el servidor, y la persistencia consolidada en repositorios.

### 19. Arquitectura Histórica: Cuello de Botella Transaccional Per-Step
* **Archivos:** `19_Secuencia_Paradigma_Previo_Per_Step.puml` / `svg/19_Secuencia_Paradigma_Previo_Per_Step.svg`
* **Tipo UML:** Diagrama de Secuencia Comparativo
* **Concepto:** Análisis del diseño previo a la refactorización por lotes. Documenta el problema de latencia y sobrecarga transaccional generado al ejecutar una petición HTTP POST y una transacción en base de datos independiente por cada punto de energía consumido.

### 20. Patrón Command en Cliente: Flujo de Historial, Deshacer (Undo) y Rollback Local
* **Archivos:** `20_Flujo_Patron_Command_Historial_UndoRedo.puml` / `svg/20_Flujo_Patron_Command_Historial_UndoRedo.svg`
* **Tipo UML:** Diagrama de Actividad / Flujo
* **Concepto:** Algoritmo y máquina de decisiones del patrón Command en `Game.tsx`. Modela la captura de snapshots inmutables (`structuredClone`), el encolado en `pendingMoves`, la reversión de pasos inmediatos (`handleUndoLastMove`, `handleUndoAll`) sin sobrecarga de red y la garantía de resiliencia mediante Rollback local al snapshot inicial en caso de error HTTP o caída de conexión.

### 21. Comparativa de Rendimiento y Tráfico de Red: Polling Continuo vs Lote Atómico
* **Archivos:** `21_Secuencia_Funcionamiento_Previo_Polling_Vs_Actual.puml` / `svg/21_Secuencia_Funcionamiento_Previo_Polling_Vs_Actual.svg`
* **Tipo UML:** Diagrama de Secuencia Comparativo
* **Concepto:** Comparativa cuantitativa global del TFG. Contrasta la sobrecarga de la versión previa (5 peticiones GET cada 5 segundos de polling continuo más peticiones por paso, sumando ~19 peticiones/turno) frente al modelo optimizado actual (0 peticiones de polling en reposo y 1 única petición REST atómica por turno), reduciendo el tráfico de 19 peticiones a 1 única petición por turno.

### 22. Comparativa de Patrones de Transporte: Señalización vs Payload Directo
* **Archivos:** `22_Secuencia_WebSockets_vs_Hybrid_Polling.puml` / `svg/22_Secuencia_WebSockets_vs_Hybrid_Polling.svg`
* **Tipo UML:** Diagrama de Secuencia Comparativo
* **Concepto:** Evaluación arquitectónica entre el patrón Signal + Pull histórico (implementado en el prototipo inicial `tfg-initial` con `Lobby.jsx` y `Game.jsx`, donde el servidor enviaba una señal de refresco a `/topic/refresh` o `/topic/players` sin datos obligando al cliente a realizar peticiones REST `GET` adicionales) frente al patrón reactivo Server Push implementado en el TFG (donde `Lobby.tsx` y `Game.tsx` reciben los DTOs consolidados directamente en el payload del frame STOMP, eliminando por completo las peticiones HTTP secundarias).

### 23. Optimización de Capa de Transporte Web: HTTP/1.1 vs HTTP/2
* **Archivos:** `23_Secuencia_HTTP1_vs_HTTP2.puml` / `svg/23_Secuencia_HTTP1_vs_HTTP2.svg`
* **Tipo UML:** Diagrama de Secuencia Didáctico
* **Concepto:** Justificación técnica de la configuración `server.http2.enabled=true` en Spring Boot. Modela el bloqueo Head-of-Line (HOL) de HTTP/1.1 frente a la multiplexación binaria sobre una única conexión TLS persistente en HTTP/2.

### 24. Diagrama de Despliegue Físico y Topología de Red
* **Archivos:** `24_Despliegue_Infraestructura_Ngrok.puml` / `svg/24_Despliegue_Infraestructura_Ngrok.svg`
* **Tipo UML:** Diagrama de Despliegue
* **Concepto:** Arquitectura de despliegue e infraestructura física del sistema. Modela los nodos Dispositivo Cliente (Navegador con SPA React), la red pública con el túnel seguro Ngrok (dominio `*.ngrok-free.app`), el Servidor de Ejecución Host (JVM 17, Spring Boot 3 con Tomcat embebido SSL y HTTP/2 activado) y el motor relacional de base de datos (H2/PostgreSQL).

### 25. Arquitectura de Pruebas Unitarias para Componentes Genéricos Base
* **Archivos:** `25_Clase_base_project_testing.puml` / `svg/25_Clase_base_project_testing.svg`
* **Tipo UML:** Diagrama de Clases
* **Concepto:** Infraestructura de test unitario para las clases base de la aplicación (`BaseService`, `BaseRestController`). Muestra el empleo de dobles de prueba (clases Dummy: `TestEntity`, `TestService`, `TestRepository`) con Mockito para validar las operaciones CRUD genéricas sin levantar el contexto de base de datos.

### 26. Manejo Centralizado de Excepciones en Spring Boot
* **Archivos:** `26_Secuencia_gestion_excepciones.puml` / `svg/26_Secuencia_gestion_excepciones.svg`
* **Tipo UML:** Diagrama de Secuencia
* **Concepto:** Robustez y arquitectura transversal de errores. Demuestra cómo las excepciones lanzadas en la capa de servicio burbujean hacia el `DispatcherServlet` y son interceptadas por el `@RestControllerAdvice` (`ExceptionHandlerController`) para devolver respuestas HTTP normalizadas (400 Bad Request) al cliente.

### 27. Motor de Búsqueda con Caché LRU
* **Archivos:** `27_Secuencia_searchbar.puml` / `svg/27_Secuencia_searchbar.svg`
* **Tipo UML:** Diagrama de Secuencia
* **Concepto:** Arquitectura de búsqueda reactiva en cliente. Detalla la fase 1 (captura de entrada y ventana de debounce de 300 ms en React State), la fase 2 (consulta y desalojo en la caché LRU en memoria) y la fase 3 (ejecución algorítmica de búsqueda y filtrado por propiedades anidadas).

### 28. Asistente de Edición en Lote (MassEditWizard)
* **Archivos:** `28_Secuencia_listado_filtrado_edicionMasiva.puml` / `svg/28_Secuencia_listado_filtrado_edicionMasiva.svg`
* **Tipo UML:** Diagrama de Secuencia
* **Concepto:** Flujo de modificación simultánea de entidades en tablas genéricas (`ListGenerator`). Describe el mantenimiento del histórico estructurado para soporte de Undo/Redo, la revisión previa diferencial y la persistencia atómica en el endpoint genérico `PUT /api/v1/{recurso}/some` con `UpdateSomeDTO`.

### 29. Patrón Strategy: Sistema de Desbloqueo de Logros
* **Archivos:** `29_Clase_estrategia_desbloqueo_logros.puml` / `svg/29_Clase_estrategia_desbloqueo_logros.svg`
* **Tipo UML:** Diagrama de Clases
* **Concepto:** Aplicación del patrón de diseño Strategy (GoF) en la arquitectura de estadísticas y recompensas. Modela `UserAchievementService` (en `upstream.userachievement`) delegando en el contexto `AchievementUnlocker`, el cual indexa mediante el enum tipado `Metric` las estrategias concretas `AUGamesPlayed`, `AUTotalPoints` y `AUVictories` para evaluar los umbrales de `Achievement` sobre los datos acumulados de `User`.
