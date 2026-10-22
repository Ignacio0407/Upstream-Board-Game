# ADR-003: Migración Integral a TypeScript y Modernización del Tooling con Vite + Vitest

* **Estado:** Aceptado
* **Fecha:** Febrero 2025
* **Autores:** Ignacio Gutiérrez Serrera
* **Contexto Técnico:** Frontend React 18, Vite, Vitest, TypeScript 5, React Testing Library

---

## 1. Contexto y Planteamiento del Problema

El frontend heredado del proyecto grupal previo estaba construido con el ecosistema clásico de **Create React App (CRA)**, **Webpack**, **Jest** y **JavaScript estándar (`.js`, `.jsx`)**:
* Create React App fue oficialmente abandonado y declarado en desuso (*deprecated*) por el equipo central de React.
* El tiempo de arranque del servidor de desarrollo superaba los 20-30 segundos, y la recarga en caliente (*HMR*) con Webpack era lenta y propensa a perder el estado de la partida.
* La suite de tests con Jest tardaba varios minutos en ejecutarse en local y en entornos CI debido a la sobrecarga de transformación de Babel.
* Al carecer de tipado estático en JavaScript, los errores de acceso a propiedades nulas o contratos de API desincronizados solo se manifestaban en tiempo de ejecución (*runtime crashes*).

---

## 2. Decisión Adoptada

Se decidió acometer una **modernización tecnológica completa del cliente web**:

1. **Migración a TypeScript Estricto (`strict: true`):**
   * Conversión del 100% de los ficheros fuente a `.ts` y `.tsx`.
   * Modelado formal de interfaces tipadas para el dominio del juego ([`Match.ts`](../../frontend/src/interfaces/Match.ts), [`Salmon.ts`](../../frontend/src/interfaces/Salmon.ts), [`Tile.ts`](../../frontend/src/interfaces/Tile.ts)), DTOs de transporte, payloads de WebSockets y contratos de autenticación.

2. **Sustitución de Create React App / Webpack por Vite:**
   * Empaquetado nativo basado en módulos ES (*ESM*) en desarrollo y compilación optimizada con Rollup y ESBuild para producción.
   * Configuración de proxy inverso transparente en `vite.config.ts` hacia `/api` y `/ws-upstream` en `localhost:8080`.
   * Habilitación de `host: 0.0.0.0` para permitir pruebas multidispositivo instantáneas desde móviles y tablets en la red local.

3. **Migración de Jest a Vitest:**
   * Adopción de **Vitest** como motor de pruebas unitarias y de integración de componentes, reutilizando la misma configuración de Vite y ejecutando los tests en memoria con **jsdom**.

---

## 3. Consecuencias

### Positivas
* **Tiempo de arranque:** De más de 25 segundos con Webpack a **menos de 300 milisegundos con Vite**.
* **Velocidad de pruebas:** Cerca de 500 pruebas frontend ejecutadas en pocos segundos con Vitest.
* **Seguridad y fiabilidad de tipos:** Prevención sistemática de *undefined errors*, errores tipográficos en propiedades y desajustes de tipos entre DTOs del backend y estados de React.
* **Autocompletado y refactorización guiada:** El IDE asiste de forma interactiva en todas las estructuras de datos y props de componentes.

### Negativas / Compromisos asumidos
* **Esfuerzo de migración:** Requirió reescribir y tipar manualmente decenas de componentes, generadores de formularios y hooks existentes.
