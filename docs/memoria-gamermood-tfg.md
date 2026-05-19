# GamerMood

**Memoria del Proyecto de Fin de Ciclo**

**Ciclo formativo:** Desarrollo de Aplicaciones Multiplataforma  
**Centro:** IES El Cañaveral  
**Familia profesional:** Informática y Comunicaciones  
**Autores:** Ionela Daniela Stanciu Dutu, Mario Chamorro Gutierrez y Florin Ungureanu Dutu  
**Tutor:** Álvaro Bravo Pérez  
**Fecha de entrega:** 25 de mayo de 2026

[Insertar logotipo del centro en la portada, centrado sobre el título o bajo el nombre del instituto.]

## Índice

1. Introducción  
2. Objetivos del proyecto  
3. Alcance y limitaciones  
4. Tecnologías utilizadas  
5. Arquitectura general  
6. Organización del repositorio  
7. Desarrollo del frontend Angular  
8. Desarrollo del backend Spring Boot  
9. Seguridad y autenticación JWT  
10. Diseño de la base de datos  
11. Sistema de recomendaciones con Groq  
12. Docker y entorno local  
13. Endpoints principales  
14. Trabajo en equipo  
15. Problemas encontrados y soluciones  
16. Pruebas realizadas  
17. Decisiones técnicas  
18. Posibles mejoras futuras  
19. Conclusión  
20. Referencias

Nota: este índice está preparado como índice manual. Al pasar el documento a Word se pueden ajustar los números de página o sustituirlo por una tabla de contenido automática.

## 1. Introducción

GamerMood es una aplicación web pensada para registrar sesiones de juego y relacionarlas con el estado de ánimo del usuario. La idea surgió a partir de una situación bastante común: muchas personas juegan a diario, pero no siempre se paran a pensar cómo les afecta una sesión larga, una partida competitiva o el tipo de juego que eligen según su estado emocional.

El objetivo no ha sido crear una herramienta médica ni realizar diagnósticos, sino desarrollar una aplicación sencilla que ayude al usuario a reflexionar sobre sus hábitos de juego. Después de cada sesión, el usuario puede guardar el juego, su estado de ánimo, la intensidad con la que ha jugado y una breve descripción de la experiencia. Con esos datos, el sistema genera una recomendación para orientar la siguiente sesión.

El proyecto combina frontend, backend, base de datos, autenticación y entorno local con Docker. Para el equipo ha sido una forma de integrar varias competencias trabajadas durante el ciclo: desarrollo web, APIs REST, seguridad, persistencia, diseño de interfaces, pruebas y documentación técnica.

**Figura 1.** Insertar captura de la pantalla inicial de GamerMood en el navegador. Debe verse el nombre de la aplicación y el aspecto general de la interfaz.

## 2. Objetivos del proyecto

El objetivo principal ha sido desarrollar una aplicación full-stack funcional, mantenible y preparada para una entrega académica. Queríamos que el proyecto no se quedara solo en una maqueta visual, sino que tuviera registro de usuarios, base de datos real, autenticación y operaciones completas sobre las sesiones.

Como objetivos concretos nos planteamos permitir el registro e inicio de sesión de usuarios, proteger las rutas privadas mediante JWT, crear y consultar sesiones de juego, generar recomendaciones asociadas a cada sesión, guardar feedback sobre esas recomendaciones y mantener la información en PostgreSQL.

También nos interesaba que el entorno fuera reproducible. Por eso usamos Docker Compose para levantar PostgreSQL y dejamos documentadas las variables necesarias en un archivo de ejemplo. Esto facilita que cualquier integrante del equipo o el tribunal pueda levantar el proyecto con menos pasos manuales.

Objetivos concretos:

- Registrar usuarios e iniciar sesión de forma segura.
- Guardar sesiones de juego asociadas a un usuario.
- Generar recomendaciones mediante Groq cuando hay clave configurada.
- Mantener un fallback por reglas para que la aplicación funcione sin proveedor externo.
- Permitir feedback sobre las recomendaciones.
- Documentar la instalación y el uso del proyecto.

## 3. Alcance y limitaciones

GamerMood cubre el flujo principal de una aplicación de seguimiento de sesiones de juego: autenticación, registro de sesiones, recomendaciones, feedback y consulta del historial. No incluye todavía estadísticas avanzadas, gráficos, administración de usuarios ni despliegue en un servidor externo.

Durante el desarrollo se revisó la parte de inteligencia artificial. En la versión final no se usa OpenAI. El proveedor real integrado es Groq, mediante su endpoint compatible con Chat Completions. Aun así, el proyecto mantiene un sistema interno de reglas para que la aplicación pueda ejecutarse aunque no haya clave de Groq configurada o aunque el proveedor externo falle.

Esta decisión es importante porque afecta directamente a la reproducibilidad del proyecto. La aplicación puede probarse sin depender de servicios externos, pero también permite activar recomendaciones generadas por IA añadiendo una clave propia en el archivo `.env`.

## 4. Tecnologías utilizadas

En el frontend hemos utilizado Angular 21 con TypeScript. Elegimos Angular porque ofrece una estructura clara por componentes, servicios, rutas, guards e interceptors. Esto encaja bien con una aplicación donde hay varias pantallas y comunicación constante con una API REST.

En el backend hemos trabajado con Java 17 y Spring Boot 3.5.13. Spring Boot nos ha permitido crear una API REST organizada por capas, integrar Spring Security, trabajar con validaciones y conectar con PostgreSQL mediante Spring Data JPA e Hibernate.

La base de datos es PostgreSQL 17. Elegimos una base relacional porque el dominio del proyecto se organiza de forma natural en entidades relacionadas: usuarios, roles, sesiones, recomendaciones y feedback. Además, PostgreSQL permite trabajar con claves foráneas, restricciones e integridad referencial.

| Parte | Tecnologías |
| --- | --- |
| Frontend | Angular 21, TypeScript 5.9, RxJS, Angular Router y Reactive Forms |
| Backend | Java 17, Spring Boot 3.5.13, Spring Security y Spring Data JPA |
| Seguridad | JWT con JJWT 0.12.6 y BCrypt |
| Base de datos | PostgreSQL 17 |
| Entorno local | Docker Compose |
| Pruebas | Vitest en frontend y Maven Wrapper en backend |

**Figura 2.** Insertar captura del README o del árbol del repositorio mostrando `frontend`, `backend`, `database` y `docs`.

## 5. Arquitectura general

La aplicación sigue una arquitectura cliente-servidor. El frontend Angular se ejecuta en el navegador y se comunica con el backend Spring Boot mediante peticiones HTTP en formato JSON. El backend procesa las solicitudes, valida la autenticación, aplica la lógica de negocio y accede a PostgreSQL mediante repositorios JPA.

La separación principal del sistema es la siguiente: frontend Angular para la interfaz, backend Spring Boot para la API y PostgreSQL para la persistencia. Esta separación ayuda a que cada parte tenga una responsabilidad clara y facilita explicar el proyecto durante la defensa.

El flujo simplificado es: usuario, frontend Angular, API REST Spring Boot, JPA/Hibernate y PostgreSQL. Esta estructura nos ha permitido trabajar de forma más ordenada y repartir tareas entre los miembros del equipo.

**Figura 3.** Insertar diagrama de arquitectura con tres bloques: Angular, Spring Boot y PostgreSQL. Incluir flechas HTTP + JWT entre frontend y backend, y JPA/Hibernate entre backend y base de datos.

## 6. Organización del repositorio

El repositorio está dividido en carpetas principales. `backend` contiene la API REST desarrollada con Spring Boot. `frontend` contiene la aplicación Angular. `database` contiene el script SQL inicial. `docs` contiene documentación de apoyo, como actas y estructura para la memoria. En la raíz también están `docker-compose.yml`, `.env.example` y `README.md`.

Dentro del backend, el código se organiza en `config`, `controller`, `dto`, `entity`, `exception`, `repository`, `security` y `service`. Esta estructura responde a una división clásica por capas y ayuda a separar controladores, lógica de negocio, acceso a datos y seguridad.

Dentro del frontend, la aplicación se divide en `components`, `services`, `models`, `guards` e `interceptors`. Esta organización permite separar las pantallas, la comunicación con la API, los tipos de datos y la gestión de autenticación en rutas y peticiones.

**Figura 4.** Insertar captura del explorador del proyecto en el IDE mostrando la estructura raíz del repositorio.

## 7. Desarrollo del frontend Angular

El frontend es una SPA desarrollada con Angular. La aplicación se estructura mediante componentes standalone, servicios para comunicarse con el backend y rutas protegidas para las zonas que requieren inicio de sesión.

Los componentes principales son la pantalla inicial, login, registro, dashboard, formulario de sesión y recomendaciones. El dashboard permite ver las sesiones del usuario y eliminar sesiones propias. El formulario de sesión recoge los datos necesarios para generar una recomendación. La pantalla de recomendaciones muestra el resultado, permite regenerarlo y enviar feedback.

Los servicios principales son `AuthService` y `SessionService`. `AuthService` gestiona login, registro, tokens y cierre de sesión. `SessionService` centraliza las llamadas relacionadas con sesiones, recomendaciones y feedback, ya que funcionalmente forman parte del mismo flujo de uso.

El interceptor JWT añade automáticamente la cabecera `Authorization` con el token en las peticiones privadas. Esto evita repetir lógica de autenticación en cada servicio. Además, el guard de autenticación protege las rutas que no deberían estar disponibles para usuarios sin sesión iniciada.

**Figura 5.** Insertar captura de `app.component.routes.ts` mostrando las rutas protegidas con `authGuard`.

**Figura 6.** Insertar captura de `auth.interceptor.ts` mostrando cómo se añade `Authorization: Bearer` al request.

**Figura 7.** Insertar captura del formulario de creación de sesión en la aplicación.

## 8. Desarrollo del backend Spring Boot

El backend expone una API REST bajo el prefijo `/api`. La estructura sigue el flujo `controller`, `service`, `repository` y `database`. Los controladores reciben las peticiones HTTP, los servicios contienen la lógica principal y los repositorios se encargan del acceso a PostgreSQL mediante Spring Data JPA.

`AuthController` gestiona el registro, login y refresco de token. `SessionController` gestiona las sesiones de juego: crear, listar, consultar detalle y eliminar una sesión propia. `RecomendacionController` genera o regenera recomendaciones asociadas a una sesión. `FeedbackController` guarda la valoración de una recomendación. `HealthController` permite comprobar que el backend está levantado.

Una parte importante del backend es comprobar siempre la propiedad de los datos. Por ejemplo, al consultar o eliminar una sesión se busca por id de sesión y por id de usuario autenticado. Así evitamos que un usuario pueda acceder a sesiones que no le pertenecen.

**Figura 8.** Insertar captura de `SessionController.java` mostrando los endpoints principales de sesiones.

**Figura 9.** Insertar captura de `SessionServiceImpl.java` mostrando la búsqueda por id de sesión e id de usuario.

## 9. Seguridad y autenticación JWT

La seguridad se basa en JWT. Cuando el usuario inicia sesión correctamente, el backend genera un token de acceso y un token de refresco. El frontend guarda esos tokens y envía el token de acceso en las peticiones privadas.

El filtro JWT del backend intercepta las peticiones, extrae el token de la cabecera `Authorization`, lo valida y establece la autenticación en el contexto de Spring Security. Los endpoints públicos son los de autenticación, health y error. El resto de endpoints principales requieren autenticación.

También se configuró CORS para permitir la comunicación entre el frontend en `localhost:4200` y el backend en `localhost:8081`. Esta configuración es necesaria porque durante el desarrollo cada parte se ejecuta en un puerto distinto.

Aunque actualmente solo se usa `ROLE_USER`, el backend mantiene la entidad `Role` y la tabla `usuarios_roles`. Esta estructura encaja bien con Spring Security porque los roles se cargan como authorities. Además, permite añadir otros permisos en el futuro sin rehacer el modelo de usuarios.

**Figura 10.** Insertar captura de `SecurityConfig.java` mostrando `SessionCreationPolicy.STATELESS`, rutas públicas y filtro JWT.

**Figura 11.** Insertar captura de `JwtAuthenticationFilter.java` mostrando la lectura de la cabecera `Authorization`.

## 10. Diseño de la base de datos

La base de datos está diseñada en PostgreSQL y se inicializa con el script `database/schema/01_init.sql`. Las tablas principales son `usuarios`, `roles`, `usuarios_roles`, `sesiones_juego`, `recomendaciones`, `feedback_recomendacion` y `transiciones_estado`.

La relación más importante es `usuarios` con `sesiones_juego`, donde un usuario puede tener varias sesiones. Cada sesión puede tener una recomendación asociada, y cada recomendación puede tener feedback. Además, usuarios y roles se relacionan mediante una tabla intermedia.

Se ha buscado que las relaciones JPA coincidan con las claves foráneas reales de PostgreSQL. Esto es importante porque si Hibernate espera una relación y la base de datos tiene otra, pueden aparecer errores al insertar, borrar o validar el esquema. En desarrollo se usa `ddl-auto=validate` para detectar esos problemas al arrancar.

| Tabla | Descripción |
| --- | --- |
| `usuarios` | Usuarios registrados en la aplicación. |
| `roles` | Roles de seguridad, actualmente `ROLE_USER`. |
| `usuarios_roles` | Tabla intermedia para la relación N:M entre usuarios y roles. |
| `sesiones_juego` | Sesiones registradas por cada usuario. |
| `recomendaciones` | Recomendación asociada a una sesión. |
| `feedback_recomendacion` | Valoración del usuario sobre una recomendación. |
| `transiciones_estado` | Historial de cambios de estado de una sesión. |

**Figura 12.** Insertar diagrama entidad-relación. Debe mostrar `usuarios`, `roles`, `usuarios_roles`, `sesiones_juego`, `recomendaciones`, `feedback_recomendacion` y `transiciones_estado` con sus claves primarias y foráneas.

**Figura 13.** Insertar captura de `01_init.sql` mostrando las claves foráneas y restricciones principales.

## 11. Sistema de recomendaciones con Groq

El sistema de recomendaciones utiliza actualmente Groq como proveedor de IA cuando existe una clave configurada. El backend lee `GROQ_API_KEY`, `GROQ_API_URL`, `GROQ_MODEL` y `GROQ_MAX_TOKENS` desde las variables de entorno. El endpoint usado es `https://api.groq.com/openai/v1/chat/completions`. Aunque la URL contiene la palabra `openai`, el proveedor real utilizado por el proyecto es Groq, ya que Groq ofrece compatibilidad con ese formato de Chat Completions.

El flujo es sencillo: el usuario crea una sesión con juego, estado de ánimo, intensidad y descripción; el frontend solicita la recomendación; el backend intenta generar el texto con `GroqService`; si Groq responde correctamente, la recomendación se guarda con fuente `GROQ`.

Si no hay clave configurada o la llamada externa falla, el backend no rompe el flujo. En ese caso se utiliza un fallback por reglas internas dentro de `RecomendacionService` y la recomendación se guarda con fuente `REGLAS`. Esta decisión hace que el proyecto siga siendo reproducible incluso sin depender de una clave externa.

**Figura 14.** Insertar captura de `GroqService.java` mostrando la lectura de variables y la llamada HTTP a Groq.

**Figura 15.** Insertar captura de `RecomendacionService.java` mostrando la selección de fuente `GROQ` o `REGLAS` según la respuesta.

## 12. Docker y entorno local

Docker Compose se usa para levantar PostgreSQL. El archivo `docker-compose.yml` define el servicio de base de datos, las variables de entorno, el puerto, el volumen persistente y el script inicial. Esto evita depender de instalaciones locales distintas en cada ordenador.

Un punto importante es que PostgreSQL solo ejecuta los scripts de inicialización la primera vez que se crea el volumen. Si el volumen ya existe y se cambia el script SQL, hay que recrear el volumen con `docker compose down -v` y volver a levantar el contenedor.

Las variables de entorno principales están documentadas en `.env.example`. Entre ellas están `SPRING_PROFILES_ACTIVE`, `SERVER_PORT`, `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`, `GROQ_API_KEY`, `GROQ_API_URL`, `GROQ_MODEL` y `GROQ_MAX_TOKENS`.

**Figura 16.** Insertar captura de `docker-compose.yml` mostrando el servicio `postgres`, volumen, puerto y healthcheck.

**Figura 17.** Insertar captura de `.env.example`. No insertar nunca el archivo `.env` real porque contiene secretos.

## 13. Endpoints principales

| Método | Endpoint | Uso |
| --- | --- | --- |
| GET | `/api/health` | Comprueba que el backend responde. |
| POST | `/api/auth/register` | Registra un usuario. |
| POST | `/api/auth/login` | Inicia sesión y devuelve tokens JWT. |
| POST | `/api/auth/refresh` | Renueva el token de acceso. |
| POST | `/api/sessions` | Crea una sesión de juego. |
| GET | `/api/sessions` | Lista las sesiones del usuario autenticado. |
| GET | `/api/sessions/{id}` | Consulta una sesión propia. |
| DELETE | `/api/sessions/{id}` | Elimina una sesión propia. |
| POST | `/api/recommendations/{sesionId}` | Genera u obtiene una recomendación. |
| POST | `/api/recommendations/{sesionId}/retry` | Regenera una recomendación. |
| POST | `/api/feedback/{recomendacionId}` | Guarda feedback sobre una recomendación. |

## 14. Trabajo en equipo

El proyecto se ha planteado como un trabajo de tres personas. La organización se puede explicar por áreas: una parte centrada en frontend e integración con la API, otra en backend, seguridad y endpoints, y otra en base de datos, documentación y pruebas. Aunque cada persona haya tenido más peso en una parte, el proyecto se ha revisado de forma conjunta para que todas las piezas encajaran.

Mario se centró principalmente en backend y seguridad, incluyendo Spring Boot, JWT, validaciones, controladores, servicios y configuración. Florin se centró principalmente en base de datos y lógica de dominio, revisando entidades, relaciones, repositorios, feedback y persistencia. Ionela Daniela se centró principalmente en frontend e integración, trabajando con Angular, formularios, rutas, servicios HTTP, historial y pantalla de recomendaciones.

Durante el desarrollo fue importante mantener una estructura común y revisar que los nombres de endpoints, modelos del frontend, DTOs del backend y tablas de PostgreSQL coincidieran. En un proyecto full-stack, muchos errores aparecen precisamente cuando una parte espera un dato con un nombre o formato diferente al que envía otra.

## 15. Problemas encontrados y soluciones

Uno de los problemas principales fue mantener coherencia entre entidades JPA y el esquema real de PostgreSQL. Para resolverlo se revisaron nombres de tablas, columnas, claves foráneas y relaciones. Además, se dejó Hibernate en modo `validate` durante el desarrollo para detectar incoherencias al iniciar el backend.

También se detectó un problema al eliminar sesiones desde el frontend. El frontend llamaba a un endpoint `DELETE` y fue necesario asegurar que el backend tuviera la ruta correspondiente y que comprobara la propiedad de la sesión antes de borrarla.

Otro punto fue la regeneración de recomendaciones. Había que asegurar que al borrar una recomendación anterior no quedaran entidades relacionadas en un estado inválido. Se revisó el flujo para respetar las relaciones entre recomendación y feedback.

En seguridad apareció un problema con errores falsos 403. Spring podía redirigir algunos errores internos a `/error`, pero si esa ruta estaba protegida el resultado parecía un problema de permisos. Se permitió `/error` sin desproteger los endpoints privados.

Por último, la integración de IA pasó por una fase de aclaración. OpenAI no forma parte del proyecto final. La integración real se dejó con Groq y se mantuvo el fallback por reglas para asegurar estabilidad.

## 16. Pruebas realizadas

En backend se ejecutaron pruebas con el Maven Wrapper mediante `.\mvnw.cmd test` en Windows o `./mvnw test` en Linux/macOS. Estas pruebas validan que el contexto de Spring Boot carga correctamente y que la configuración principal no rompe el arranque.

En frontend se ejecutaron pruebas con `npm test` y también se comprobó la compilación con `npm run build`. Las pruebas revisan componentes principales y comportamientos básicos de la aplicación Angular.

Además de las pruebas automáticas, se realizaron pruebas manuales del flujo completo: registro, login, creación de sesión, listado, generación de recomendación con Groq, fallback por reglas sin clave, feedback, eliminación de sesión y protección de rutas privadas.

**Figura 18.** Insertar captura de terminal con `npm run build` correcto o tests del frontend.

**Figura 19.** Insertar captura de terminal con `.\mvnw.cmd test` correcto.

## 17. Decisiones técnicas que merece la pena defender

- Separar frontend y backend para mantener responsabilidades claras.
- Usar JWT porque encaja con una API REST stateless.
- Usar DTOs para no exponer entidades completas desde la API.
- Usar PostgreSQL por sus relaciones e integridad referencial.
- Usar Docker para que la base de datos sea reproducible.
- Mantener `ddl-auto=validate` para detectar diferencias entre entidades y esquema.
- Integrar Groq como proveedor IA real y mantener fallback por reglas para estabilidad.

## 18. Posibles mejoras futuras

Como mejoras futuras se podrían añadir gráficas de evolución emocional, filtros avanzados en el historial, estadísticas por juego, más reglas de recomendación, roles administrativos, mayor cobertura de tests y despliegue en un servidor externo.

También se podría estudiar una integración más completa con modelos de IA, siempre que no afecte a la reproducibilidad del proyecto ni obligue a depender de servicios de pago. Otra mejora interesante sería reforzar todavía más las comprobaciones de propiedad del usuario en el flujo de feedback.

## 19. Conclusión

GamerMood nos ha servido para desarrollar una aplicación full-stack completa y conectar varias áreas trabajadas durante el ciclo. El proyecto incluye interfaz web, API REST, autenticación, base de datos relacional, Docker, validación de datos, recomendaciones con Groq y documentación.

El resultado final es una aplicación funcional que permite al usuario registrar sesiones de juego, relacionarlas con su estado de ánimo y recibir recomendaciones. Aunque todavía tiene margen de mejora, creemos que cumple bien el objetivo de un TFG: demostrar que sabemos diseñar, desarrollar, integrar y documentar una solución realista.

Además, el proceso nos ha obligado a resolver problemas habituales de un proyecto real, como inconsistencias entre frontend y backend, diferencias entre entidades y base de datos, configuración de entorno, seguridad y pruebas. Esa parte ha sido una de las más importantes del aprendizaje.

## 20. Referencias

Angular. (2026). *Angular documentation*. https://angular.dev/

Docker. (2026). *Docker documentation*. https://docs.docker.com/

Groq. (2026). *Groq API documentation*. https://console.groq.com/docs

PostgreSQL Global Development Group. (2026). *PostgreSQL documentation*. https://www.postgresql.org/docs/

Spring. (2026). *Spring Boot reference documentation*. https://docs.spring.io/spring-boot/

Spring. (2026). *Spring Security reference documentation*. https://docs.spring.io/spring-security/
