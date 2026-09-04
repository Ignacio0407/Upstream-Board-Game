# Upstream - Juego de Mesa Digital

Plataforma web multijugador para el juego de mesa **Upstream**, desarrollada como Trabajo de Fin de Grado (TFG) en la Universidad de Sevilla (Grado en Ingeniería Informática - Ingeniería del Software). El sistema implementa una arquitectura desacoplada SPA (*Single Page Application*) con un backend reactivo en **Spring Boot 3 (Java 21)** y un frontend moderno en **React 18 con TypeScript y Vite**.

---

## 📖 Reglas del Juego

El juego está basado en el juego de mesa **Upstream**, donde cada jugador controla un banco de salmones que debe remontar la corriente de un río salvaje sorteando obstáculos y depredadores (osos, garzas, águilas, cascadas y corrientes) para alcanzar las zonas de desove antes de que el río avance y se agote el tiempo.

### ¿Dónde encontrar las reglas oficiales?

1. **Dentro de la propia aplicación web:**
   - En la barra de navegación superior (**Navbar**), los usuarios (tanto invitados como autenticados) disponen del acceso directo **"Rules"** (ruta `/rules`).
   - Esta vista integra un visor interactivo que renderiza el documento oficial de reglas completo en español.
   - *Nota:* Para usuarios registrados, acceder a las reglas desbloquea de forma automática un logro en su perfil.

2. **En el sistema de ficheros del proyecto:**
   - El documento original en PDF se encuentra disponible en:  
     `frontend/public/rules_ES.pdf`

---

## 🚀 Guía de Uso e Instalación de la Aplicación

### Requisitos Previos

Asegúrate de contar con el siguiente software instalado en tu equipo:
* **Java Development Kit (JDK) 21** o superior.
* **Node.js v18** o superior (con **npm**).
* **Git** para clonar el repositorio.

---

### 1. Puesta en Marcha del Backend (Spring Boot)

El backend utiliza Spring Boot 3 y Maven. Está configurado por defecto con seguridad SSL/TLS (HTTPS y WSS) en el puerto `8080` y base de datos en memoria H2.

1. Abre una terminal en la raíz del proyecto:
   ```bash
   # En sistemas Unix / Linux / macOS
   ./mvnw spring-boot:run

   # En Windows PowerShell o CMD
   .\mvnw.cmd spring-boot:run

   # En general:
   mvn spring-boot:run
   ```
2. **Alternativa desde IDE:** Puedes abrir la clase principal `src/main/java/es/us/dp1/l4_01_24_25/upstream/Upstream.java` en tu entorno (IntelliJ IDEA, Eclipse, VS Code) y presionar `Run` / `F5`.
3. Una vez iniciado, el backend responderá en:
   - **API REST / HTTPS:** `https://localhost:8080`
   - **Consola H2 Database:** `https://localhost:8080/h2-console`
   - **Documentación Swagger / OpenAPI:** `https://localhost:8080/swagger-ui/index.html`

> ℹ️ **Certificado SSL Local:** Al acceder por primera vez a `https://localhost:8080`, el navegador alertará de un certificado autofirmado para desarrollo (`localhost`). Acéptalo para permitir la comunicación segura local. Si no se hace este paso, al intentar usar la aplicación, verás que no se reflejan las acciones que haces. Esto es porque los eventos se comunican a través de web sockets, y estos dependen de que el certificado esté aceptado para establecer las conexiones o rechazarlas.

---

### 2. Puesta en Marcha del Frontend (React + Vite)

El frontend se encuentra en la carpeta `frontend/` y utiliza Vite con soporte de recarga rápida (HMR).

1. Navega al directorio del cliente e instala las dependencias:
   ```bash
   cd frontend
   npm install
   ```
2. Inicia el servidor de desarrollo:
   ```bash
   npm run dev
   ```
3. El servidor Vite arrancará en `http://localhost:3000` (con escucha en `0.0.0.0`, lo que permite acceso desde la red local) y un proxy configurado hacia el backend (`/api` y `/ws-upstream`).

---

### 3. Modos de Juego y Conectividad

La aplicación soporta tres modalidades de conexión:

* **Modo Local (Mismo Equipo):**
  Abre dos pestañas en tu navegador (una en modo normal y otra en modo incógnito, o usando dos navegadores distintos como Chrome y Firefox) accediendo a `http://localhost:3000`. Inicia sesión con usuarios diferentes para simular una partida multijugador local.

* **Modo Multidispositivo en Red Local (LAN - PC, Tablets y Móviles):**
  Gracias a que Vite está configurado con `host: true` (`0.0.0.0`) y a los estilos adaptables puedes conectar múltiples dispositivos (smartphones, tablets, portátiles) conectados a la misma red Wi-Fi:
  1. Averigua la dirección IPv4 local del ordenador que ejecuta el servidor. Al ejecutar npm run dev, Vite te la dice, pero también puedes ejecutar `ipconfig` en la terminal de Windows o `ip a` o `ifconfig` en Linux/Mac.
  2. Desde cualquier móvil o tablet de la red, abre el navegador y escribe:  
     `http://<IP_LOCAL>:3000` o
     `https://<IP_LOCAL>:8080` si prefieres conectarte a la versión que se arranca en el servidor al ejecutar mvn spring-boot:run.

* **Modo Remoto por Internet (Túnel ngrok):**
  Para jugar a través de Internet sin abrir puertos en el router:
  1. En `src/main/resources/application.properties`, cambia la propiedad `ngrok.enabled=true`.
  2. Configura tu token de autenticación de ngrok mediante la variable de entorno `NGROK_AUTH_TOKEN`.
  3. Al iniciar el backend, se generará la URL pública segura (`https://*.ngrok-free.app`) visible en la consola.

---

### 4. Flujo de Navegación y Funcionalidades del Juego

1. **Registro y Login:**
   - Creación de cuenta desde `/register` o acceso desde `/login`.
   - Se asignan roles de usuario (`PLAYER` o `ADMIN`).
2. **Dashboard de Partidas:**
   - Visualización de partidas activas y en espera, actualizadas en tiempo real mediante WebSockets.
   - Creación de partidas públicas o privadas con contraseña.
3. **Sala de Espera (Lobby):**
   - Sincronización instantánea de jugadores.
   - Cuando el creador pulsa "Start Game", todos los jugadores navegan automáticamente al tablero a través de WebSockets sin recargar la página.
4. **Partida en Curso (Juego):**
   - **Fase de Losetas (TILES):** Colocación y rotación de nuevas piezas del río según el mazo.
   - **Fase de Movimiento (MOVING):** Desplazamiento por lotes de salmones (nado y saltos) con previsualización en cliente ("Ghost UI"), cálculo automático de energía y detección de peligros (osos, águilas, cascadas).
   - **Avance del Río:** Eliminación automática de filas traseras cuando se agotan las losetas, arrastrando a los salmones rezagados.
   - **Chat en tiempo real:** Mensajería integrada por partida vía WebSockets con límite de tasa de envío para evitar saturación.
5. **Logros (Achievements):**
   - Desbloqueo dinámico por acciones en el juego (victorias, puntos, partidas jugadas o lectura de reglas).
   - Los administradores pueden crear, listar, editar y eliminar logros usando componentes base generativos.
6. **Panel de Administración de Usuarios:**
   - Vista para administradores (`/users`) con edición masiva, ordenación, filtrado reactivo y gestión de roles.
7. **Diseño Responsive:**
   - La interfaz del tablero y controles se adapta automáticamente a pantallas de ordenadores, tablets y teléfonos móviles utilizando dimensiones relativas (`vw`, `vh`) y *media queries* dedicadas.

---

## 🛠️ Esquema de Cambios Relevantes Realizados (Evolución TFG)

A continuación se detallan las mejoras y reingenierías técnicas aplicadas al proyecto durante el desarrollo del Trabajo de Fin de Grado respecto al proyecto heredado inicial:

### 1. Proyecto Base - Mini framework
* **Capa Base de Persistencia y Lógica en Backend:**
  - Introducción de clases abstractas genéricas: `BaseEntity`, `BaseRepository<T, ID>`, `BaseService<T, ID>`, `BaseServiceWithDTO<T, DTO, ID>`, `BaseRestController` y `BaseRestControllerWithDTO`.
  - Estandarización de operaciones CRUD con tipado genérico, reduciendo código repetitivo (*boilerplate*) en más de un 60%.
  - Soporte para actualizaciones parciales atómicas mediante el endpoint unificado `/some` y el objeto `UpdateSomeDTO`.
  - Mapeo declarativo entre entidades y DTOs mediante mappers estructurados (`EntityMapper`).
* **Componentes Genéricos y Declarativos de UI en Frontend:**
  - `FormGenerator` y `EditFormGenerator`: Generadores declarativos de formularios con validación automática de campos, soporte integrado de botones de cancelación y manejo nativo de selectores desplegables (*dropdowns*).
  - `ListGenerator`: Componente de tabla altamente modular que centraliza ordenación (`ListSortBar`), paginación, filtros de permisos por autoridad, barra de búsqueda y un asistente de edición/eliminación masiva (`MassEditWizard`) con soporte completo para deshacer/rehacer (**Undo/Redo**).
  - `SearchBar`: Motor de búsqueda inteligente desacoplado con arquitectura de 3 niveles: *debounce* de entrada en React, memoria caché LRU (Nivel 2) y procesamiento optimizado de consultas (Nivel 3).
* **Simplificación del Modelo de Dominio:**
  - Sustitución de la entidad de base de datos relacional `Authority` por un enumerado nativo `Authority` (`PLAYER`, `ADMIN`), eliminando joins innecesarios y tablas intermedias en la base de datos.
* **Modernización del Stack Tecnológico:**
  - Actualización del runtime backend a **Java 21 (LTS)** y **Spring Boot 3.3.3**.

### 2. Mejora de Calidad y Mantenibilidad del Código
* **Migración Integral a TypeScript:**
  - Migración completa del código frontend desde JavaScript a **TypeScript** (`.ts`, `.tsx`), con interfaces tipadas para todas las entidades, estados de juego y contratos de la API REST.
* **Transición de Tooling (Vite + Vitest):**
  - Sustitución de *Create React App* (CRA) y *Jest* por **Vite** y **Vitest**, multiplicando la velocidad de arranque, compilación y ejecución de tests en memoria.
* **Refactorización hacia Arquitectura Limpia:**
  - Desacoplamiento de la lógica de negocio que residía en los controladores, trasladándola íntegramente a la capa de servicios (`MatchService`, `SalmonMatchService`, `MatchTileService`, etc.).
  - Reemplazo de serializadores y deserializadores manuales propensos a bucles de recursión infinita por **Proyecciones y DTOs bien tipados** (`DashboardMatchDTO`, `SalmonMatchDTO`, `MatchTileDTO`).
* **Auditoría y Resolución Integral en SonarQube:**
  - Erradicación de errores críticos: **0 problemas de fiabilidad (*Reliability*) y seguridad (*Security*)** restantes en el backend y en el frontend.
  - Reducción drástica de incidencias de mantenibilidad: de más de 120 *code smells* heredados a solo 9 issues.
  - Eliminación sistemática de código muerto (*dead code*), estilos `.less` en desuso heredados del Petclinic original y dependencias no utilizadas.
* **Cultura y Batería Exhaustiva de Pruebas:**
  - Backend con más de **452 métodos de prueba** que ejecutan cerca de **500 tests unitarios y de integración**, superando el **86.7% de cobertura de código (coverage)** validado mediante JaCoCo.
  - Suite de pruebas de componentes frontend configurada con Vitest y *React Testing Library*, cubriendo generadores de formularios, vistas de juego, tablero, lobbies y utilidades, cerca de 500 tests también.

### 3. Mejoras de Seguridad
* **Cifrado de Comunicaciones de Extremo a Extremo (TLS / HTTPS / WSS):**
  - Implementación de **HTTP/2** y **WSS (Secure WebSockets over TLS)** en el servidor Spring Boot mediante almacén de certificados PKCS12 (`keystore.p12`).
* **Interceptor de Seguridad Robusto para WebSockets (`WebSocketSecurityInterceptor`):**
  - **Autenticación estricta:** Validación del token JWT en el comando STOMP `CONNECT`.
  - **Protección contra manipulación de rutas (*Route Tampering*):** Validación exhaustiva de prefijos y parámetros de canal (`validateRouteAndExtractMatchId`) para evitar suscripciones o emisiones no autorizadas.
  - **Aislamiento de partida (*Match Isolation*):** Verificación activa (`isUserInMatch`) de que el usuario autenticado pertenece a la partida antes de permitir suscripciones a eventos de la misma.
  - **Prevención de Denegación de Servicio (Anti-DoS / Rate Limiting):** Límite máximo de tasa de mensajes (60 mensajes/minuto por usuario) y control de tamaño de carga útil (rechazo de *payloads* superiores a 64 KB).
* **Mecanismo Defensivo "Do Not Hack My App":**
  - Detección de manipulaciones ilícitas en el cliente y navegación forzada a la vista `/do-not-hack-my-app` ante discrepancias en IDs o accesos no autorizados.
* **Seguridad en Entrada de Datos y Configuración:**
  - Validación defensiva de datos mediante `@ValidNumber` y restricciones de coordenadas en `Coordinate`, `SalmonMatch` y `MatchTile`.
  - Codificación segura de parámetros en URIs para evitar inyecciones.
  - Sustitución de credenciales en texto plano por variables de entorno protegidas (`NGROK_AUTH_TOKEN`).

### 4. Optimización de Rendimiento y Comunicación
*(Reducción sustancial del volumen de datos transmitidos y mayor rapidez en el establecimiento y entrega de mensajes)*

* **Eliminación Total del Polling Continuo (*Zero-Polling Architecture*):**
  - En la versión original previa al TFG, el componente `Match.tsx` ejecutaba un `setInterval` cada 5.000 ms enviando peticiones continuas `GET /api/v1/matches/{id}` sin desmontarse, acumulando tráfico redundante constante.
  - Se sustituyó por completo por una **arquitectura reactiva 100% orientada a eventos** gestionada por `WebSocketManager`, reduciendo las peticiones periódicas en segundo plano a cero.
* **Paradigma de Movimiento por Lotes (*Batch Movement*):**
  - Reemplazo del paradigma anterior paso a paso (que emitía una petición síncrona REST `POST /move` y una transacción en base de datos por cada punto de energía consumido) por un procesamiento atómico consolidado.
  - La nueva operativa agrupa toda la secuencia del turno en una única llamada REST (`PATCH /api/v1/salmonMatches/move-batch/{matchId}`), disminuyendo drásticamente el número de conexiones cliente-servidor y garantizando atomicidad en la persistencia.
* **Predicción en el Cliente (*Client-Side Prediction & Ghost UI*):**
  - Renderizado instantáneo de trayectorias y salmones fantasma en el frontend mediante `calculateValidMoves.ts` y actualización de estado optimista, proporcionando respuesta visual sin latencia percibida por el usuario y con mecanismo de reversión (*rollback*) ante discrepancias.
* **Minimización de Payloads en Red:**
  - Introducción de DTOs ligeros (`DashboardMatchDTO`, `MatchTileDTO`, `SalmonMatchDTO`) que devuelven únicamente los campos indispensables para el renderizado.
  - Métodos de creación y mutación redefinidos con retorno `void` donde el estado se propaga a través de WebSockets, recortando el tamaño de las respuestas HTTP a mínimos bytes.
* **Multiplexación con HTTP/2:**
  - Conexiones persistentes únicas y compresión de cabeceras HPACK gracias a `server.http2.enabled=true`, reduciendo el tiempo de *handshake* y transferencia de recursos.
* **Optimización de Activos Multimedia:**
  - Conversión de imágenes del juego a formato moderno **WebP**, comprimiendo el peso de los gráficos sin pérdida visual y purgando imágenes obsoletas del servidor.

### 5. Mejora de Documentación Técnica
* **Diagramas de Arquitectura y Flujo UML:**
  - Creación de 22 diagramas exhaustivos ubicados en `docs/design-TFG/`, disponibles en formato editable `.puml` y formato vectorial `.svg` (dominio, ciclo de vida, coordinación de fases, algoritmos de movimiento, WebSockets, HTTP/1 vs HTTP/2, movimiento en lote vs previo, componentes de React y comparativas de red).
  - Manual explicativo complementario en `docs/design-TFG/DOCUMENTACION_DIAGRAMAS.md`.
* **Documentación Interactiva de la API:**
  - Especificación OpenAPI / Swagger integrada en `/docs` del frontend y `/swagger-ui/index.html` del backend.
* **Reestructuración de este README.md:**
  - Actualización completa del documento para servir como guía técnica unificada de instalación, uso del juego y memoria de ingeniería del software desarrollada en el TFG.

---

## 🏛️ Anexo: Documentación Histórica Original del Proyecto Base (DP1 / Spring PetClinic)

A continuación se preserva íntegramente la documentación técnica original del proyecto base pedagógico heredado de la asignatura DP1 (fork de Spring PetClinic), mantenida como referencia histórica de la infraestructura inicial:

### Information about the fork
Therefore, this is a fork of https://github.com/spring-projects/spring-petclinic.  The main changes performed are:
- Trimming several parts of the application to keep the example low
- Reorganize some parts of the code according to best practices introduced in the course
- Modifying the Controllers to work as RestControllers creating several API endpoints
- Modifying the security configuration to use JWT
- Creating a React frontend in js.

### Understanding the Spring Petclinic application  backend with a few diagrams
<a href="https://speakerdeck.com/michaelisvy/spring-petclinic-sample-application">See the presentation here</a>

### Running petclinic backend locally
Petclinic is a [Spring Boot](https://spring.io/guides/gs/spring-boot) application built using [Maven](https://spring.io/guides/gs/maven/). You can build a jar file and run it from the command line:


```
git clone https://github.com/gii-is-DP1/spring-petclinic.git
cd spring-petclinic
./mvnw package
java -jar target/*.jar
```

You can then access petclinic backend here: [http://localhost:8080/](http://localhost:8080/swagger-ui/index.html)



Or you can run it from Maven directly using the Spring Boot Maven plugin. If you do this it will pick up changes that you make in the project immediately (changes to Java source files require a compile as well - most people use an IDE for this):

```
./mvnw spring-boot:run
```
### Database configuration

In its default configuration, Petclinic uses an in-memory database (H2) which
gets populated at startup with data. The INSERTs are specified in the file data.sql.

### Working with React Petclinic in your IDE

#### Prerequisites
The following items should be installed in your system:
* Java 17 or newer.
* Node.js 18 or newer.
* git command line tool (https://help.github.com/articles/set-up-git)
* Your preferred IDE 
  * Eclipse with the m2e plugin. Note: when m2e is available, there is an m2 icon in `Help -> About` dialog. If m2e is
  not there, just follow the install process here: https://www.eclipse.org/m2e/
  * [Spring Tools Suite](https://spring.io/tools) (STS)
  * IntelliJ IDEA
  * [VS Code](https://code.visualstudio.com)

#### Steps:

1) On the command line
```
git clone https://github.com/gii-is-DP1/spring-petclinic.git
```
2) Inside Eclipse or STS
```
File -> Import -> Maven -> Existing Maven project
```

Then either build on the command line `./mvnw generate-resources` or using the Eclipse launcher (right click on project and `Run As -> Maven install`) to generate the css. Run the application main method by right clicking on it and choosing `Run As -> Java Application`.

3) Inside IntelliJ IDEA

In the main menu, choose `File -> Open` and select the Petclinic [pom.xml](pom.xml). Click on the `Open` button.

CSS files are generated from the Maven build. You can either build them on the command line `./mvnw generate-resources`
or right click on the `spring-petclinic` project then `Maven -> Generates sources and Update Folders`.

A run configuration named `PetClinicApplication` should have been created for you if you're using a recent Ultimate
version. Otherwise, run the application by right clicking on the `PetClinicApplication` main class and choosing
`Run 'PetClinicApplication'`.

4) Navigate to Petclinic
Visit [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) in your browser.


### Looking for something in particular?

|Spring Boot Configuration | Class or Java property files  |
|--------------------------|---|
|The Main Class | [PetClinicApplication](https://github.com/gii-is-DP1/spring-petclinic/blob/master/src/main/java/org/springframework/samples/petclinic/PetClinicApplication.java) |
|Properties Files | [application.properties](https://github.com/gii-is-DP1/spring-petclinic/blob/master/src/main/resources) |


### Starting the frontend

The Spring Petclinic is implemented with a React frontend in the folder named "frontend".
You can start the development server to see frontend using the command (maybe you should use the command npm insall prior to this):
```
npm start
```
You can then access the PetClinic frontend at [http://localhost:3000](http://localhost:3000)
