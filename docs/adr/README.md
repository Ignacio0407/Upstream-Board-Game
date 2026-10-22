# 🏛️ Architecture Decision Records (ADRs) - Upstream Board Game

Este directorio centraliza los **Registros de Decisiones de Arquitectura** (*Architecture Decision Records*) del proyecto Upstream. Cada documento describe una decisión técnica fundamental tomada durante la evolución y desarrollo del Trabajo de Fin de Grado (TFG), detallando el contexto del problema, las alternativas evaluadas, la justificación de la decisión adoptada y sus consecuencias tanto positivas como negativas.

---

## 📑 Índice de Decisiones Arquitectónicas

| Identificador | Título | Estado | Fecha | Área |
| :--- | :--- | :---: | :---: | :--- |
| [**ADR-001**](001-batch-movement-vs-per-step.md) | Procesamiento de Movimientos en Lote (*Batch Movement*) vs. Micro-movimientos Paso a Paso | **Aceptado** | Enero 2025 | Mecánicas / Red / UX |
| [**ADR-002**](002-websockets-stomp-vs-polling.md) | Sincronización Reactiva en Tiempo Real con WebSockets STOMP vs. *Polling* Continuo HTTP | **Aceptado** | Febrero 2025 | Comunicación / Rendimiento |
| [**ADR-003**](003-migracion-typescript-y-vite.md) | Migración Integral a TypeScript y Modernización del *Tooling* con Vite + Vitest | **Aceptado** | Febrero 2025 | Frontend / Calidad / Tooling |
| [**ADR-004**](004-capa-base-generica-y-dtos.md) | Capa Base Genérica (*Mini-Framework* CRUD) y Desacoplamiento mediante DTOs Tipados | **Aceptado** | Marzo 2025 | Backend / Arquitectura Limpia |

---

## 📐 Estructura de los Documentos

Cada ADR sigue el estándar de la industria (propuesto por Michael Nygard):
1. **Título e Identificador:** Numeración secuencial y descripción clara.
2. **Estado:** Propuesto, Aceptado, Rechazado o Reemplazado.
3. **Contexto del Problema:** Fuerzas y limitaciones técnicas que motivaron el cambio.
4. **Decisión Adoptada:** Solución técnica escogida y cómo se implementó.
5. **Consecuencias:** Impacto medible en rendimiento, mantenibilidad, complejidad y experiencia de usuario.
