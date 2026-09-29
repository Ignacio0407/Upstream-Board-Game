<div align="center">

# 🐟 Upstream - Digital Board Game

**A full-stack multiplayer web platform for the Upstream board game developed with Spring Boot 3 & React 18.**

[🇪🇸 Versión en Español](#-versión-en-español) &nbsp;|&nbsp; [🇬🇧 English Version](#-english-version)

</div>

---

# 🇪🇸 Versión en Español

Plataforma web multijugador para el juego de mesa **Upstream**, desarrollada como Trabajo de Fin de Grado (TFG) en la Universidad de Sevilla (Grado en Ingeniería Informática - Ingeniería del Software). El sistema implementa una arquitectura desacoplada SPA (*Single Page Application*) con un backend reactivo en **Spring Boot 3 (Java 21)** y un frontend moderno en **React 18 con TypeScript y Vite**.

---

## 📑 Tabla de Contenidos (Español)
1. [Reglas del Juego](#1-reglas-del-juego)
   - [1.1. ¿Dónde encontrar las reglas oficiales?](#11-dónde-encontrar-las-reglas-oficiales)
2. [Guía de Uso e Instalación de la Aplicación](#2-guía-de-uso-e-instalación-de-la-aplicación)
   - [2.1. Requisitos Previos](#21-requisitos-previos)
   - [2.2. Puesta en Marcha del Backend (Spring Boot)](#22-puesta-en-marcha-del-backend-spring-boot)
   - [2.3. Puesta en Marcha del Frontend (React + Vite)](#23-puesta-en-marcha-del-frontend-react--vite)
   - [2.4. Modos de Juego y Conectividad](#24-modos-de-juego-y-conectividad)
   - [2.5. Flujo de Navegación y Funcionalidades del Juego](#25-flujo-de-navegación-y-funcionalidades-del-juego)
3. [Esquema de Cambios Relevantes Realizados (Evolución TFG)](#3-esquema-de-cambios-relevantes-realizados-evolución-tfg)
   - [3.1. Proyecto Base - Mini framework](#31-proyecto-base---mini-framework)
   - [3.2. Mejora de Calidad y Mantenibilidad del Código](#32-mejora-de-calidad-y-mantenibilidad-del-código)
   - [3.3. Mejoras de Seguridad](#33-mejoras-de-seguridad)
   - [3.4. Optimización de Rendimiento y Comunicación](#34-optimización-de-rendimiento-y-comunicación)
   - [3.5. Mejora de Documentación Técnica](#35-mejora-de-documentación-técnica)
4. [Anexo: Documentación Histórica Original del Proyecto Base (DP1 / Spring PetClinic)](#4-anexo-documentación-histórica-original-del-proyecto-base-dp1--spring-petclinic)
   - [4.1. Información sobre el Fork](#41-información-sobre-el-fork)
   - [4.2. Comprensión del Backend mediante Diagramas](#42-comprensión-del-backend-mediante-diagramas)
   - [4.3. Ejecución del Backend Localmente](#43-ejecución-del-backend-localmente)
   - [4.4. Configuración de Base de Datos](#44-configuración-de-base-de-datos)
   - [4.5. Trabajo con PetClinic en el IDE](#45-trabajo-con-petclinic-en-el-ide)
   - [4.6. Configuración de Spring Boot y Ficheros Clave](#46-configuración-de-spring-boot-y-ficheros-clave)
   - [4.7. Puesta en Marcha del Frontend Original](#47-puesta-en-marcha-del-frontend-original)
5. [Licencia, Autoría y Propiedad Intelectual](#5-licencia-autoría-y-propiedad-intelectual)

---

## 1. Reglas del Juego

El juego está basado en el juego de mesa **Upstream**, donde cada jugador controla un banco de salmones que debe remontar la corriente de un río salvaje sorteando obstáculos y depredadores (osos, garzas, águilas, cascadas y corrientes) para alcanzar las zonas de desove antes de que el río avance y se agote el tiempo.

### 1.1. ¿Dónde encontrar las reglas oficiales?

1. **Dentro de la propia aplicación web:**
   - En la barra de navegación superior (**Navbar**), los usuarios (tanto invitados como autenticados) disponen del acceso directo **"Rules"** (ruta `/rules`).
   - Esta vista integra un visor interactivo que renderiza el documento oficial de reglas completo en español.
   - *Nota:* Para usuarios registrados, acceder a las reglas desbloquea de forma automática un logro en su perfil.

2. **En el sistema de ficheros del proyecto:**
   - El documento original en PDF se encuentra disponible en:  
     `frontend/public/rules_ES.pdf`

---

## 2. Guía de Uso e Instalación de la Aplicación

### 2.1. Requisitos Previos

Asegúrate de contar con el siguiente software instalado en tu equipo:
* **Java Development Kit (JDK) 21** o superior.
* **Node.js v18** o superior (con **npm**).
* **Git** para clonar el repositorio.

---

### 2.2. Puesta en Marcha del Backend (Spring Boot)

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

### 2.3. Puesta en Marcha del Frontend (React + Vite)

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

### 2.4. Modos de Juego y Conectividad

La aplicación soporta tres modalidades de conexión:

* **Modo Local (Mismo Equipo):**
  Abre dos pestañas en tu navegador (una en modo normal y otra en modo incógnito, o usando dos navegadores distintos como Chrome y Firefox) accediendo a `http://localhost:3000`. Inicia sesión con usuarios diferentes para simular una partida multijugador local.

* **Modo Multidispositivo en Red Local (LAN - PC, Tablets y Móviles):**
  Gracias a que Vite está configurado con `host: true` (`0.0.0.0`) y a los estilos adaptables puedes conectar múltiples dispositivos (smartphones, tablets, portátiles) conectados a la misma red Wi-Fi:
  1. Averigua la dirección IPv4 local del ordenador que ejecuta el servidor. Al ejecutar `npm run dev`, Vite te la dice, pero también puedes ejecutar `ipconfig` en la terminal de Windows o `ip a` o `ifconfig` en Linux/Mac.
  2. Desde cualquier móvil o tablet de la red, abre el navegador y escribe:  
     `http://<IP_LOCAL>:3000` o  
     `https://<IP_LOCAL>:8080` si prefieres conectarte a la versión que se arranca en el servidor al ejecutar `mvn spring-boot:run`.

* **Modo Remoto por Internet (Túnel ngrok):**
  Para jugar a través de Internet sin abrir puertos en el router:
  1. En `src/main/resources/application.properties`, cambia la propiedad `ngrok.enabled=true`.
  2. Configura tu token de autenticación de ngrok mediante la variable de entorno `NGROK_AUTH_TOKEN`.
  3. Al iniciar el backend, se generará la URL pública segura (`https://*.ngrok-free.app`) visible en la consola.

---

### 2.5. Flujo de Navegación y Funcionalidades del Juego

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

## 3. Esquema de Cambios Relevantes Realizados (Evolución TFG)

A continuación se detallan las mejoras y reingenierías técnicas aplicadas al proyecto durante el desarrollo del Trabajo de Fin de Grado respecto al proyecto heredado inicial:

### 3.1. Proyecto Base - Mini framework
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

### 3.2. Mejora de Calidad y Mantenibilidad del Código
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

### 3.3. Mejoras de Seguridad
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

### 3.4. Optimización de Rendimiento y Comunicación
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

### 3.5. Mejora de Documentación Técnica
* **Diagramas de Arquitectura y Flujo UML:**
  - Creación de 22 diagramas exhaustivos ubicados en `docs/design-TFG/`, disponibles en formato editable `.puml` y formato vectorial `.svg` (dominio, ciclo de vida, coordinación de fases, algoritmos de movimiento, WebSockets, HTTP/1 vs HTTP/2, movimiento en lote vs previo, componentes de React y comparativas de red).
  - Manual explicativo complementario en `docs/design-TFG/DOCUMENTACION_DIAGRAMAS.md`.
* **Documentación Interactiva de la API:**
  - Especificación OpenAPI / Swagger integrada en `/docs` del frontend y `/swagger-ui/index.html` del backend.
* **Reestructuración de este README.md:**
  - Actualización completa del documento para servir como guía técnica unificada de instalación, uso del juego y memoria de ingeniería del software desarrollada en el TFG.

---

## 4. Anexo: Documentación Histórica Original del Proyecto Base (DP1 / Spring PetClinic)

A continuación se preserva íntegramente la documentación técnica original del proyecto base pedagógico heredado de la asignatura DP1 (fork de Spring PetClinic), mantenida como referencia histórica de la infraestructura inicial:

### 4.1. Información sobre el Fork
Este repositorio es un fork de https://github.com/spring-projects/spring-petclinic adaptado para docencia. Los cambios principales aplicados fueron:
* Simplificación y recorte de varias partes de la aplicación original.
* Reorganización del código conforme a las buenas prácticas de la asignatura.
* Adaptación de controladores como `@RestController`, creando endpoints API REST.
* Configuración de seguridad basada en JWT.
* Implementación de una interfaz web cliente en React (JavaScript).

### 4.2. Comprensión del Backend mediante Diagramas
Puedes consultar la presentación explicativa de la arquitectura PetClinic original en:  
[See the presentation here (Speaker Deck)](https://speakerdeck.com/michaelisvy/spring-petclinic-sample-application)

### 4.3. Ejecución del Backend Localmente
PetClinic es una aplicación [Spring Boot](https://spring.io/guides/gs/spring-boot) construida con [Maven](https://spring.io/guides/gs/maven/). Puedes compilar el empaquetado JAR y ejecutarlo desde la consola:

```bash
git clone https://github.com/gii-is-DP1/spring-petclinic.git
cd spring-petclinic
./mvnw package
java -jar target/*.jar
```

Una vez levantado, se puede acceder al backend en: [http://localhost:8080/](http://localhost:8080/swagger-ui/index.html)

También se puede ejecutar directamente con el plugin de Maven:
```bash
./mvnw spring-boot:run
```

### 4.4. Configuración de Base de Datos
En su configuración por defecto, PetClinic utiliza una base de datos en memoria (H2) que se inicializa al arrancar con los datos especificados en el fichero `data.sql`.

### 4.5. Trabajo con PetClinic en el IDE

#### Requisitos Previos
* Java 17 o superior.
* Node.js 18 o superior.
* Herramienta de línea de comandos Git.
* IDE recomendado: Eclipse con plugin m2e, Spring Tools Suite (STS), IntelliJ IDEA o VS Code.

#### Pasos de Configuración:
1. **Clonación del repositorio:**
   ```bash
   git clone https://github.com/gii-is-DP1/spring-petclinic.git
   ```
2. **Desde Eclipse o STS:**  
   `File -> Import -> Maven -> Existing Maven project`.  
   Genera los recursos CSS ejecutando `./mvnw generate-resources` o clic derecho `Run As -> Maven install`. Inicia la aplicación ejecutando la clase principal `PetClinicApplication` con `Run As -> Java Application`.
3. **Desde IntelliJ IDEA:**  
   `File -> Open` y selecciona el fichero `pom.xml`. Pulsa `Open`.  
   Genera los recursos ejecutando `./mvnw generate-resources` o mediante el panel de Maven `spring-petclinic -> Maven -> Generates sources and Update Folders`. Ejecuta la configuración generada `PetClinicApplication`.
4. **Acceso:**  
   Visita `http://localhost:8080/swagger-ui/index.html` en tu navegador.

### 4.6. Configuración de Spring Boot y Ficheros Clave

| Configuración Spring Boot | Clase o Fichero Java |
| :--- | :--- |
| Clase Principal | [PetClinicApplication](https://github.com/gii-is-DP1/spring-petclinic/blob/master/src/main/java/org/springframework/samples/petclinic/PetClinicApplication.java) |
| Fichero de Propiedades | [application.properties](https://github.com/gii-is-DP1/spring-petclinic/blob/master/src/main/resources) |

### 4.7. Puesta en Marcha del Frontend Original
El cliente PetClinic original se encuentra implementado en React en la carpeta `frontend`. Puedes iniciar el servidor de desarrollo ejecutando:
```bash
npm install
npm start
```
Posteriormente, accede a la interfaz en [http://localhost:3000](http://localhost:3000).

---

## 5. Licencia, Autoría y Propiedad Intelectual

### Autoría
* **Autor Original:** Ignacio Gutiérrez Serrera
* **GitHub:** [@Ignacio0407](https://github.com/Ignacio0407)
* **Contacto:** [ignaciogutierrezserrera@gmail.com](mailto:ignaciogutierrezserrera@gmail.com)

### Términos de Licencia (Uso No Comercial / Educativo)
* **Uso permitido gratuito:** Se autoriza libremente el uso, copia, estudio y adaptación de este software exclusivamente para **fines personales, académicos, de investigación o demostración en portfolios sin ánimo de lucro**, con la obligación de mantener visible el reconocimiento explícito al autor original.
* **Restricción comercial:** Queda terminantemente prohibido cualquier uso comercial, integración en software privativo o monetización por parte de consultoras o empresas sin la **autorización previa por escrito del autor y la adquisición de una licencia comercial**.
* Para consultas de licenciamiento comercial o contratación: contactar a `ignaciogutierrezserrera@gmail.com`.

---
---

# 🇬🇧 English Version

A full-stack multiplayer web platform for the **Upstream** board game, developed as a Bachelor's Thesis (*Trabajo de Fin de Grado - TFG*) in Software Engineering at the University of Seville. The system implements a decoupled Single Page Application (SPA) architecture featuring a reactive backend in **Spring Boot 3 (Java 21)** and a modern frontend in **React 18 with TypeScript and Vite**.

---

## 📑 Table of Contents (English)
1. [Game Rules](#1-game-rules)
   - [1.1. Where to Find Official Rules?](#11-where-to-find-official-rules)
2. [Application Installation and User Guide](#2-application-installation-and-user-guide)
   - [2.1. Prerequisites](#21-prerequisites)
   - [2.2. Backend Setup (Spring Boot)](#22-backend-setup-spring-boot)
   - [2.3. Frontend Setup (React + Vite)](#23-frontend-setup-react--vite)
   - [2.4. Game Modes and Connectivity](#24-game-modes-and-connectivity)
   - [2.5. Navigation Flow and Gameplay Features](#25-navigation-flow-and-gameplay-features)
3. [Key Architectural Changes and Enhancements (Bachelor's Thesis Evolution)](#3-key-architectural-changes-and-enhancements-bachelors-thesis-evolution)
   - [3.1. Base Project - Mini-Framework](#31-base-project---mini-framework)
   - [3.2. Code Quality and Maintainability Improvements](#32-code-quality-and-maintainability-improvements)
   - [3.3. Security Enhancements](#33-security-enhancements)
   - [3.4. Performance and Communication Optimization](#34-performance-and-communication-optimization)
   - [3.5. Technical Documentation Enhancements](#35-technical-documentation-enhancements)
4. [Annex: Original Historical Documentation of the Base Project (DP1 / Spring PetClinic)](#4-annex-original-historical-documentation-of-the-base-project-dp1--spring-petclinic)
   - [4.1. Information about the Fork](#41-information-about-the-fork)
   - [4.2. Understanding the PetClinic Backend with Diagrams](#42-understanding-the-petclinic-backend-with-diagrams)
   - [4.3. Running PetClinic Backend Locally](#43-running-petclinic-backend-locally)
   - [4.4. Database Configuration](#44-database-configuration)
   - [4.5. Working with PetClinic in your IDE](#45-working-with-petclinic-in-your-ide)
   - [4.6. Spring Boot Configuration and Key Files](#46-spring-boot-configuration-and-key-files)
   - [4.7. Starting the Original Frontend](#47-starting-the-original-frontend)
5. [License, Authorship and Intellectual Property](#5-license-authorship-and-intellectual-property)

---

## 1. Game Rules

The game is based on the board game **Upstream**, where each player controls a school of salmon swimming upstream along a wild river, dodging hazards and predators (bears, herons, eagles, waterfalls, and rapids) to reach the spawning grounds before the river advances and time runs out.

### 1.1. Where to Find Official Rules?

1. **Within the Web Application:**
   - On the top navigation bar (**Navbar**), all users (both guests and authenticated players) have direct access to **"Rules"** (route `/rules`).
   - This view embeds an interactive viewer that renders the complete official rulebook in Spanish.
   - *Note:* For authenticated players, reading the rules automatically unlocks an achievement in their profile.

2. **In the Project Filesystem:**
   - The original rulebook in PDF format is available at:  
     `frontend/public/rules_ES.pdf`

---

## 2. Application Installation and User Guide

### 2.1. Prerequisites

Ensure you have the following software installed on your machine:
* **Java Development Kit (JDK) 21** or higher.
* **Node.js v18** or higher (with **npm**).
* **Git** for repository cloning.

---

### 2.2. Backend Setup (Spring Boot)

The backend is built with Spring Boot 3 and Maven. By default, it is configured with SSL/TLS security (HTTPS and WSS) on port `8080` and uses an in-memory H2 database.

1. Open a terminal in the root directory of the project:
   ```bash
   # On Unix / Linux / macOS
   ./mvnw spring-boot:run

   # On Windows PowerShell or CMD
   .\mvnw.cmd spring-boot:run

   # General Maven command:
   mvn spring-boot:run
   ```
2. **Alternative via IDE:** You can open the main entry point `src/main/java/es/us/dp1/l4_01_24_25/upstream/Upstream.java` in your preferred IDE (IntelliJ IDEA, Eclipse, VS Code) and press `Run` / `F5`.
3. Once started, the backend will listen on:
   - **REST API / HTTPS:** `https://localhost:8080`
   - **H2 Database Console:** `https://localhost:8080/h2-console`
   - **Swagger / OpenAPI Documentation:** `https://localhost:8080/swagger-ui/index.html`

> ℹ️ **Local SSL Certificate:** When accessing `https://localhost:8080` for the first time, your browser will warn you about a self-signed development certificate (`localhost`). Accept it to allow secure local communication. If this step is omitted, actions performed in the web app will not be reflected. This happens because game events are broadcasted through WebSockets, which require the certificate to be accepted to establish or reject connections.

---

### 2.3. Frontend Setup (React + Vite)

The frontend lives in the `frontend/` directory and uses Vite with Hot Module Replacement (HMR).

1. Navigate to the client directory and install dependencies:
   ```bash
   cd frontend
   npm install
   ```
2. Start the development server:
   ```bash
   npm run dev
   ```
3. The Vite server will launch at `http://localhost:3000` (listening on `0.0.0.0`, enabling access across your local network) with preconfigured proxies pointing to the backend (`/api` and `/ws-upstream`).

---

### 2.4. Game Modes and Connectivity

The platform supports three distinct connectivity modes:

* **Local Mode (Single Machine):**
  Open two browser tabs (one in normal mode and another in incognito mode, or use two different browsers like Chrome and Firefox) navigating to `http://localhost:3000`. Sign in with different user accounts to simulate a local multiplayer game.

* **Multi-Device Local Area Network (LAN - PC, Tablets & Mobile Devices):**
  Because Vite is configured with `host: true` (`0.0.0.0`) and features fully responsive styles, multiple devices (smartphones, tablets, laptops) connected to the same Wi-Fi network can join seamlessly:
  1. Determine the local IPv4 address of the host machine running the server. Vite displays it upon executing `npm run dev`, or you can check it using `ipconfig` (Windows) or `ip a` / `ifconfig` (Linux/Mac).
  2. From any mobile phone or tablet on the network, open the browser and navigate to:  
     `http://<LOCAL_IP>:3000` or  
     `https://<LOCAL_IP>:8080` if you prefer connecting to the production bundle served directly by Spring Boot via `mvn spring-boot:run`.

* **Remote Mode over the Internet (ngrok Tunnel):**
  To play over the Internet without forwarding ports on your router:
  1. In `src/main/resources/application.properties`, set `ngrok.enabled=true`.
  2. Configure your ngrok auth token using the environment variable `NGROK_AUTH_TOKEN`.
  3. Upon starting the backend, a public secure URL (`https://*.ngrok-free.app`) will be output to the console.

---

### 2.5. Navigation Flow and Gameplay Features

1. **Registration & Login:**
   - Create an account from `/register` or sign in via `/login`.
   - User roles (`PLAYER` or `ADMIN`) are assigned automatically.
2. **Match Dashboard:**
   - View active and pending matches, updated in real time via WebSockets.
   - Create public or password-protected private games.
3. **Lobby (Waiting Room):**
   - Instant synchronization of joined players.
   - When the host clicks "Start Game", all players navigate automatically to the board via WebSockets without page reload.
4. **Live Match (Gameplay):**
   - **Tile Phase (TILES):** Draw, rotate, and place new river tiles according to the deck.
   - **Movement Phase (MOVING):** Batch salmon movements (swimming and leaping) with client-side visual simulation ("Ghost UI"), automated energy tracking, and obstacle/predator detection (bears, eagles, waterfalls).
   - **River Advancement:** Automated discarding of rear rows when tile decks are depleted, sweeping away stranded salmon.
   - **Real-Time Chat:** Integrated per-match messaging over WebSockets with rate limiting to prevent spam.
5. **Achievements:**
   - Unlocked dynamically through in-game feats (victories, points scored, matches completed, reading the rules).
   - Administrators can create, view, edit, and delete achievements using generic generative components.
6. **User Administration Panel:**
   - Dedicated administrative view (`/users`) featuring bulk editing, column sorting, reactive filtering, and role management.
7. **Responsive Design:**
   - The game board and control panels dynamically adapt across desktop monitors, tablets, and smartphone screens using relative units (`vw`, `vh`) and custom media queries.

---

## 3. Key Architectural Changes and Enhancements (Bachelor's Thesis Evolution)

The following sections detail the technical improvements and re-engineering implemented throughout the Bachelor's Thesis compared to the original legacy baseline:

### 3.1. Base Project - Mini-Framework
* **Generic Persistence & Logic Layer in Backend:**
  - Introduced generic abstract classes: `BaseEntity`, `BaseRepository<T, ID>`, `BaseService<T, ID>`, `BaseServiceWithDTO<T, DTO, ID>`, `BaseRestController`, and `BaseRestControllerWithDTO`.
  - Standardized CRUD operations through generic typing, cutting repetitive boilerplate code by over 60%.
  - Added support for atomic batch updates via the unified `/some` endpoint and the `UpdateSomeDTO` pattern.
  - Declarative bidirectional mapping between entities and DTOs using structured `EntityMapper` contracts.
* **Generic & Declarative UI Components in Frontend:**
  - `FormGenerator` and `EditFormGenerator`: Declarative form generators with automated validation, cancel button integration, and native select/dropdown handling.
  - `ListGenerator`: Highly modular table component centralizing column sorting (`ListSortBar`), pagination, authority-based access control, search bar integration, and a mass edit/delete wizard (`MassEditWizard`) with complete **Undo/Redo** support.
  - `SearchBar`: Decoupled intelligent search engine with a 3-tier architecture: React input debouncing, Level 2 LRU caching, and optimized Level 3 query filtering.
* **Domain Model Simplification:**
  - Replaced the relational `Authority` database entity with a native Java enum `Authority` (`PLAYER`, `ADMIN`), eliminating redundant joins and intermediary lookup tables.
* **Technology Stack Modernization:**
  - Upgraded the backend runtime to **Java 21 (LTS)** and **Spring Boot 3.3.3**.

### 3.2. Code Quality and Maintainability Improvements
* **Full Migration to TypeScript:**
  - Completely migrated frontend codebase from legacy JavaScript to **TypeScript** (`.ts`, `.tsx`), enforcing strict type contracts for entities, game states, and REST API payloads.
* **Tooling Transition (Vite + Vitest):**
  - Replaced *Create React App* (CRA) and *Jest* with **Vite** and **Vitest**, drastically accelerating startup, compilation, and in-memory test execution.
* **Clean Architecture Refactoring:**
  - Decoupled business logic previously embedded in controller endpoints, migrating it entirely into dedicated service layer classes (`MatchService`, `SalmonMatchService`, `MatchTileService`, etc.).
  - Replaced error-prone manual JSON serializers/deserializers with **strongly typed Projections and DTOs** (`DashboardMatchDTO`, `SalmonMatchDTO`, `MatchTileDTO`), resolving circular recursion bugs.
* **Comprehensive SonarQube Audit & Resolution:**
  - Zero critical flaws: **0 Reliability issues and 0 Security vulnerabilities** remaining across both backend and frontend.
  - Drastic reduction of technical debt: reduced inherited code smells from over 120 down to only 9 issues.
  - Systematic purge of dead code, obsolete `.less` stylesheets inherited from Spring PetClinic, and unused dependencies.
* **Exhaustive Testing Culture & Suite:**
  - Backend backed by more than **452 test methods** executing nearly **500 unit and integration tests**, surpassing **86.7% code coverage** validated through JaCoCo.
  - Frontend component test suite built with Vitest and *React Testing Library*, thoroughly covering form generators, game boards, lobbies, and utilities with another ~500 tests.

### 3.3. Security Enhancements
* **End-to-End Encryption (TLS / HTTPS / WSS):**
  - Configured **HTTP/2** and **WSS (Secure WebSockets over TLS)** on the Spring Boot server using a PKCS12 keystore (`keystore.p12`).
* **Robust WebSocket Security Interceptor (`WebSocketSecurityInterceptor`):**
  - **Strict Handshake Authentication:** Cryptographic validation of JWT Bearer tokens during the STOMP `CONNECT` phase.
  - **Route Tampering Protection:** Exhaustive prefix and parameter validation (`validateRouteAndExtractMatchId`) preventing unauthorized channel subscriptions or frame broadcasts.
  - **Match Isolation:** Active verification (`isUserInMatch`) ensuring the authenticated user belongs to the target game session before granting subscription privileges.
  - **Anti-DoS & Rate Limiting:** Enforced rate ceiling (60 messages/minute per user) and payload size caps (rejecting payloads over 64 KB).
* **"Do Not Hack My App" Defensive Mechanism:**
  - Client-side integrity checks that forcefully redirect users to `/do-not-hack-my-app` upon detecting ID discrepancies or unauthorized navigation attempts.
* **Input Validation & Secure Configuration:**
  - Defensive field constraints with `@ValidNumber` and strict coordinate bounds across `Coordinate`, `SalmonMatch`, and `MatchTile`.
  - Secure URI parameter encoding mitigating injection vectors.
  - Replaced plain-text credentials with protected environment variables (`NGROK_AUTH_TOKEN`).

### 3.4. Performance and Communication Optimization
*(Substantial reduction in data transmission volume and accelerated handshake and message delivery)*

* **Complete Elimination of Continuous Polling (Zero-Polling Architecture):**
  - In the baseline legacy system, `Match.tsx` executed a 5,000 ms `setInterval` polling `GET /api/v1/matches/{id}` indefinitely, generating high volumes of redundant network traffic.
  - Replaced entirely with a **100% reactive event-driven architecture** managed via `WebSocketManager`, reducing periodic background requests to zero.
* **Batch Movement Paradigm:**
  - Transitioned from step-by-step movement (which dispatched a synchronous REST `POST /move` request and triggered database writes for every single spent energy point) to consolidated atomic processing.
  - The new paradigm packages the complete turn trajectory into a single REST call (`PATCH /api/v1/salmonMatches/move-batch/{matchId}`), drastically reducing HTTP roundtrips and guaranteeing atomic persistence.
* **Client-Side Prediction & Ghost UI:**
  - Instantaneous trajectory calculation and ghost salmon previews in the frontend via `calculateValidMoves.ts`, offering zero perceived latency for the player along with automated rollback upon server discrepancies.
* **Network Payload Minimization:**
  - Introduced lightweight DTOs (`DashboardMatchDTO`, `MatchTileDTO`, `SalmonMatchDTO`) returning strictly necessary fields for rendering.
  - Mutation endpoints refactored to return `void` with state broadcasted over WebSockets, minimizing HTTP response size to mere bytes.
* **HTTP/2 Multiplexing:**
  - Enabled persistent single-connection multiplexing and HPACK header compression via `server.http2.enabled=true`, cutting handshake latency and resource transfer times.
* **Multimedia Asset Optimization:**
  - Converted graphical game assets to modern **WebP** format, drastically reducing image file size without loss of visual fidelity while removing unused legacy assets.

### 3.5. Technical Documentation Enhancements
* **UML Architecture and Flow Diagrams:**
  - Produced 22 comprehensive diagrams located in `docs/design-TFG/`, provided in both editable `.puml` and vector `.svg` formats (domain model, match lifecycle, phase coordination, movement algorithms, WebSockets, HTTP/1 vs HTTP/2, batch movement comparisons, React component trees, and network traffic benchmarks).
  - Detailed accompanying guide in `docs/design-TFG/DOCUMENTACION_DIAGRAMAS.md`.
* **Interactive API Documentation:**
  - Integrated OpenAPI / Swagger specifications available at `/docs` in the frontend and `/swagger-ui/index.html` on the backend.
* **Comprehensive README Overhaul:**
  - Completely restructured technical guide serving as a unified reference for installation, gameplay instructions, and software engineering dissertation documentation.

---

## 4. Annex: Original Historical Documentation of the Base Project (DP1 / Spring PetClinic)

The original pedagogical technical documentation inherited from the DP1 course baseline project (a fork of Spring PetClinic) is preserved below for historical context:

### 4.1. Information about the Fork
This repository is a fork of https://github.com/spring-projects/spring-petclinic tailored for teaching. The main changes performed were:
* Trimming several parts of the application to keep the example lightweight.
* Reorganizing code according to best practices taught in the course.
* Modifying controllers to operate as `@RestController` classes, exposing REST API endpoints.
* Configuring security based on JWT authentication.
* Creating a React frontend client (in JavaScript).

### 4.2. Understanding the PetClinic Backend with Diagrams
You can consult the original PetClinic architecture presentation here:  
[See the presentation here (Speaker Deck)](https://speakerdeck.com/michaelisvy/spring-petclinic-sample-application)

### 4.3. Running PetClinic Backend Locally
PetClinic is a [Spring Boot](https://spring.io/guides/gs/spring-boot) application built using [Maven](https://spring.io/guides/gs/maven/). You can build a JAR file and run it from the command line:

```bash
git clone https://github.com/gii-is-DP1/spring-petclinic.git
cd spring-petclinic
./mvnw package
java -jar target/*.jar
```

You can then access the PetClinic backend here: [http://localhost:8080/](http://localhost:8080/swagger-ui/index.html)

Or run it directly using the Spring Boot Maven plugin:
```bash
./mvnw spring-boot:run
```

### 4.4. Database Configuration
In its default configuration, PetClinic uses an in-memory database (H2) populated at startup with data specified in `data.sql`.

### 4.5. Working with PetClinic in your IDE

#### Prerequisites
* Java 17 or newer.
* Node.js 18 or newer.
* Git command line tool.
* Recommended IDE: Eclipse with m2e plugin, Spring Tools Suite (STS), IntelliJ IDEA, or VS Code.

#### Setup Steps:
1. **Clone the repository:**
   ```bash
   git clone https://github.com/gii-is-DP1/spring-petclinic.git
   ```
2. **From Eclipse or STS:**  
   `File -> Import -> Maven -> Existing Maven project`.  
   Build CSS on the command line `./mvnw generate-resources` or using the Eclipse launcher (right click on project and `Run As -> Maven install`). Run the application by right-clicking `PetClinicApplication` and choosing `Run As -> Java Application`.
3. **From IntelliJ IDEA:**  
   In the main menu, choose `File -> Open` and select the PetClinic `pom.xml`. Click `Open`.  
   Generate CSS resources using `./mvnw generate-resources` or via the Maven panel `spring-petclinic -> Maven -> Generates sources and Update Folders`. Run the generated `PetClinicApplication` configuration.
4. **Access:**  
   Visit `http://localhost:8080/swagger-ui/index.html` in your browser.

### 4.6. Spring Boot Configuration and Key Files

| Spring Boot Configuration | Class or Configuration File |
| :--- | :--- |
| Main Class | [PetClinicApplication](https://github.com/gii-is-DP1/spring-petclinic/blob/master/src/main/java/org/springframework/samples/petclinic/PetClinicApplication.java) |
| Properties File | [application.properties](https://github.com/gii-is-DP1/spring-petclinic/blob/master/src/main/resources) |

### 4.7. Starting the Original Frontend
The original Spring PetClinic frontend is implemented in React in the `frontend` folder. You can launch the development server with:
```bash
npm install
npm start
```
You can then access the PetClinic frontend at [http://localhost:3000](http://localhost:3000).

---

## 5. License, Authorship and Intellectual Property

### Authorship
* **Original Author:** Ignacio Gutiérrez Serrera
* **GitHub:** [@Ignacio0407](https://github.com/Ignacio0407)
* **Contact:** [ignaciogutierrezserrera@gmail.com](mailto:ignaciogutierrezserrera@gmail.com)

### License Terms (Non-Commercial / Educational Use)
* **Free Permitted Use:** Permission is granted to inspect, study, copy, and adapt this software exclusively for **personal development, academic, university, scientific research, or non-commercial portfolio demonstration purposes**, provided that explicit credit and authorship attribution are maintained.
* **Commercial Restrictions:** Any commercial exploitation, incorporation into closed-source or proprietary software, or monetization by companies/consultancies is strictly prohibited without **prior explicit written permission and the acquisition of a commercial license from the author**.
* For commercial licensing, enterprise inquiries, or hiring: contact `ignaciogutierrezserrera@gmail.com`.
