# ADR-004: Capa Base Genérica (Mini-Framework CRUD) y Desacoplamiento mediante DTOs Tipados

* **Estado:** Aceptado
* **Fecha:** Marzo 2025
* **Autores:** Ignacio Gutiérrez Serrera
* **Contexto Técnico:** Backend Spring Boot 3 (Java 21), Spring Data JPA, Hibernate, Proyecciones y DTOs

---

## 1. Contexto y Planteamiento del Problema

En la arquitectura original heredada:
* Existía una gran cantidad de código repetitivo (*boilerplate*) duplicado en controladores, repositorios y servicios para operaciones CRUD básicas (crear, buscar por ID, listar todos, actualizar, borrar).
* Se utilizaban serializadores y deserializadores manuales de Jackson (`@JsonSerialize`, `@JsonDeserialize`) directamente acoplados a las entidades JPA. Esto causaba con frecuencia excepciones de recursión infinita (*Circular Reference Exceptions*) y obligaba a mantener código complejo y frágil para ocultar contraseñas o campos sensibles.
* Mucha lógica de negocio residía dispersa dentro de los propios controladores en lugar de estar aislada en la capa de servicios.

---

## 2. Decisión Adoptada

Se diseñó e implementó un **mini-framework de persistencia y lógica base genérica** junto con un desacoplamiento estricto mediante **Data Transfer Objects (DTOs)** y **Mappers declarativos**:

1. **Jerarquía Genérica Base en Backend:**
   * `BaseEntity`: Superclase `@MappedSuperclass` con identificador único `Integer id`.
   * `BaseRepository<T, ID>`: Repositorio genérico que unifica consultas comunes y operaciones de borrado en cascada segura.
   * `BaseService<T, ID>` y `BaseServiceWithDTO<T, DTO, ID>`: Capa de servicio con lógica estandarizada para operaciones CRUD completas y mapeo automático hacia/desde DTOs.
   * `BaseRestController<T, ID>` y `BaseRestControllerWithDTO<T, DTO, ID>`: Controladores base que exponen automáticamente endpoints REST estandarizados (`GET /`, `GET /{id}`, `POST /`, `PUT /{id}`, `DELETE /{id}`, `PATCH /some`).
   * **Endpoint unificado `/some`:** Introducción de `UpdateSomeDTO` para permitir modificaciones y eliminaciones masivas o parciales de forma atómica en una única llamada.

2. **Desacoplamiento Estricto con DTOs y Mappers:**
   * Eliminación definitiva de todos los serializadores artesanales de Jackson.
   * Creación de DTOs planos y estructurados (`DashboardMatchDTO`, `SalmonMatchDTO`, `MatchTileDTO`) que aíslan las entidades JPA de la capa de presentación.
   * Implementación del patrón Mapper (`EntityMapper`) para transformaciones predecibles, seguras y libres de bucles circulares.

3. **Simplificación del Modelo de Dominio:**
   * Sustitución de la entidad relacional `Authority` por un enumerado nativo `Authority` (`PLAYER`, `ADMIN`), eliminando *joins* y tablas intermedias innecesarias en base de datos.

---

## 3. Consecuencias

### Positivas
* **Reducción de más del 60% de código repetitivo (*boilerplate*):** Añadir una nueva entidad al sistema requiere únicamente definir la clase y heredar de la tríada base, obteniendo funcionalidad completa y validada de inmediato.
* **Erradicación de referencias circulares:** Desaparición total de errores de serialización JSON en WebSockets y REST.
* **Mantenibilidad y Calidad:** Contribuyó decisivamente a reducir las incidencias de mantenibilidad en SonarQube de más de 120 a solo 9 *code smells*.
* **Estandarización de pruebas:** Creación de una suite de testing genérica basada en reflexión y tipos parametrizados (`BaseServiceTest`, `BaseControllerTest`), aumentando drásticamente la cobertura de tests con mínimo esfuerzo.

### Negativas / Compromisos asumidos
* **Mayor número de clases:** Aumenta el número total de ficheros al separar Entidad, DTO y Mapper para cada elemento del dominio.
