Documentación Técnica de Diagramas (UML & Flow)

Este documento centraliza y explica en profundidad los diagramas que modelan la arquitectura, el comportamiento y la lógica de negocio del sistema Upstream. Los diagramas están organizados de lo general a lo particular para facilitar la comprensión a cualquier nuevo desarrollador.

---

### 1. Modelo de Dominio y Capa de Transferencia (DTOs)
*   **Archivos:** `01_Clase_interaccion_partida.puml` / `.svg`
*   **Descripción:** Este diagrama es el mapa estructural del sistema. Muestra las entidades JPA (Dominio) y cómo se desacoplan de la vista mediante DTOs. 
*   **Detalles:** 
    *   **Entidades Maestro-Detalle:** Se observa cómo `Salmon` y `Tile` actúan como objetos inmutables de definición, mientras que `SalmonMatch` y `MatchTile` gestionan el estado vivo de la partida (coordenadas, energía, capacidad).
    *   **Desacoplamiento:** Resalta el uso de mappers para transformar entidades complejas en DTOs planos, optimizando el tráfico de red y evitando referencias circulares en WebSockets.

### 2. Ciclo de Vida de la Partida (Estados)
*   **Archivos:** `02_Estado_ciclo_partida.puml` / `.svg`
*   **Descripción:** Modelado de los estados de alto nivel por los que pasa una partida.
*   **Detalles:** Define las transiciones entre `WAITING` (Lobby), `ON_GOING` (En juego) y `FINALIZED`. Es la base para la seguridad y el control de acceso a los endpoints.

### 3. Coordinación de Fases y Turnos
*   **Archivos:** `03_Flujo_coordinacion_fases_partida.puml` / `.svg`
*   **Descripción:** Diagrama de flujo que detalla la lógica interna de `MatchService.changePhase`.
*   **Detalles:** 
    *   **Gestión de Rondas:** Controla el cambio de fase de `TILES` a `MOVING` tras colocar las losetas correspondientes.
    *   **Orquestación del Río:** Detalla el avance físico del tablero (eliminación de la fila inferior y desplazamiento) cuando se agota el mazo.

### 4. Motor de Reglas: Lógica de Movimiento y Peligros
*   **Archivos:** `04_Flujo_flujo_movimiento_salmones.puml` / `.svg`
*   **Descripción:** El "cerebro" algorítmico del juego. Desglosa cómo se procesa cada movimiento de un salmón.
*   **Detalles:** 
    *   **Cálculo de Penalizaciones:** Evalúa de forma recursiva u origen/destino si el salmón contacta con Osos, Águilas o Cascadas.
    *   **Distinción Nado vs Salto:** Refleja la regla crítica donde nadar es seguro frente a los osos, pero saltar activa su ataque.

### 5. Secuencia de Juego y Sincronización Real-Time
*   **Archivos:** `05_Secuencia_secuencia_jugar_partida.puml` / `.svg`
*   **Descripción:** Diagrama de secuencia profundo que muestra la interactividad total.
*   **Detalles:** Traza una acción desde que el Jugador A hace clic en el frontend, pasa por la validación en el backend, y termina en una actualización reactiva vía STOMP Broker que refresca los tableros de todos los jugadores simultáneamente.

### 6. Sincronización Reactiva del Tablero (Frontend)
*   **Archivos:** `06_Secuencia_secuencia_sincronizacion_tablero.puml` / `.svg`
*   **Descripción:** Flujo de conexión y actualización desde la perspectiva de React.
*   **Detalles:** Explica el patrón de **Snapshot + Delta**: Carga inicial por REST y mantenimiento del estado inmutable mediante suscripciones a WebSockets.

### 7. Casos de Uso del Administrador
*   **Archivos:** `07_CasosUso_casos_uso_admin.puml` / `.svg`
*   **Descripción:** Visión general de las capacidades de gestión masiva de datos.
*   **Detalles:** Enfocado en la eficiencia del administrador para gestionar usuarios, logros y partidas.

### 8. Motor de Búsqueda con Caché L3
*   **Archivos:** `08_Secuencia_secuencia_searchbar.puml` / `.svg`
*   **Descripción:** Detalla la arquitectura de búsqueda avanzada implementada en el frontend.
*   **Detalles:** Muestra el flujo entre el input del usuario, el Debounce de React, la comprobación en la Caché LRU (Nivel 2) y el procesamiento final en el SearchEngine (Nivel 3).

### 9. Edición Masiva (MassEditWizard)
*   **Archivos:** `09_Secuencia_secuencia_listado_filtrado_edicionMasiva.puml` / `.svg`
*   **Descripción:** Flujo de trabajo para la modificación en bloque de entidades.
*   **Detalles:** Resalta el soporte para **Undo/Redo** y la revisión de cambios antes de la persistencia atómica en el servidor.

### 10. Patrón Strategy: Sistema de Logros
*   **Archivos:** `10_Clase_estrategia_desbloqueo_logros.puml` / `.svg`
*   **Descripción:** Aplicación de patrones de diseño para la extensibilidad.
*   **Detalles:** Separa la lógica de "cuándo" se desbloquea un logro (Contexto) de "qué" métrica se evalúa (Estrategias: Victorias, Puntos, Partidas), permitiendo añadir nuevas métricas sin tocar el motor central.

### 11. Manejo Global de Excepciones
*   **Archivos:** `11_Secuencia_gestion_excepciones.puml` / `.svg`
*   **Descripción:** Diagrama de robustez del sistema.
*   **Detalles:** Muestra cómo el `ExceptionHandlerController` intercepta errores de negocio y los transforma en respuestas HTTP estructuradas que el frontend puede mostrar amigablemente al usuario.

### 12. Infraestructura de Testing Genérico
*   **Archivos:** `12_Clase_base_project_testing.puml` / `.svg`
*   **Descripción:** Arquitectura de las pruebas automatizadas (la "carpintería fina").
*   **Detalles:** Explica cómo el uso de clases abstractas genéricas para Servicios y Controladores permite garantizar una cobertura de calidad masiva con el mínimo código duplicado.