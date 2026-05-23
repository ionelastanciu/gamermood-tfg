# Documentación técnica breve

Este documento es una guía para desarrolladores, para entender cómo está organizad la aplicación por dentro.

## 1. Visión general

Está dividido en tres bloques:

- Frontend Angular.
- Backend Spring Boot.
- Base de datos PostgreSQL.

El frontend se comunica con el backend mediante HTTP. El backend expone una API REST bajo `/api`, valida la autenticación con JWT y persiste la información en PostgreSQL mediante JPA/Hibernate.

La aplicación permite registrar usuarios, iniciar sesión, crear sesiones de juego, generar recomendaciones, enviar feedback y consultar el historial.

## 2. Frontend

El frontend está en `frontend/src/app`.

Carpetas principales:

- `components`: pantallas de la aplicación.
- `services`: comunicación con el backend y gestión de autenticación.
- `models`: interfaces TypeScript usadas por los componentes y servicios.
- `guards`: protección de rutas privadas.
- `interceptors`: añadido automático del JWT a las peticiones HTTP.

Los componentes más importantes son:

- `login`: inicio de sesión.
- `register`: registro de usuario.
- `dashboard`: historial de sesiones.
- `session`: formulario para crear una sesión.
- `recommendations`: pantalla donde se muestra la recomendación, se puede regenerar y se envía feedback.

`AuthService` guarda el token y los datos básicos del usuario. `SessionService` centraliza las llamadas relacionadas con sesiones, recomendaciones y feedback.

## 3. Backend

El backend está en `backend/src/main/java/com/gamermood/backend`.

La estructura sigue una organización por capas:

- `controller`: endpoints REST.
- `service`: lógica principal.
- `repository`: acceso a base de datos con Spring Data JPA.
- `entity`: entidades persistidas.
- `dto`: objetos de entrada y salida de la API.
- `security`: configuración JWT y filtros.
- `exception`: excepciones y manejador global.
- `config`: configuración general, como CORS.

Controladores principales:

- `AuthController`: registro, login y refresh token.
- `SessionController`: creación, consulta y borrado de sesiones.
- `RecomendacionController`: generación y regeneración de recomendaciones.
- `FeedbackController`: feedback de recomendaciones.
- `HealthController`: comprobación básica de estado.

## 4. Seguridad

La seguridad se basa en JWT.

El flujo es:

1. El usuario inicia sesión.
2. El backend valida las credenciales.
3. Se genera un token JWT.
4. El frontend guarda el token.
5. El interceptor lo añade a las peticiones privadas.
6. `JwtAuthenticationFilter` valida el token en backend.

La API es stateless, por lo que no se guarda sesión de servidor.

Rutas públicas:

- `/api/health`
- `/api/error`
- `/api/auth/register`
- `/api/auth/login`
- `/api/auth/refresh`

El resto de endpoints requiere autenticación.

## 5. Base de datos

La base de datos es PostgreSQL 17 y se levanta con Docker Compose.

Tablas principales:

- `usuarios`
- `roles`
- `usuarios_roles`
- `sesiones_juego`
- `recomendaciones`
- `feedback_recomendacion`
- `transiciones_estado`

Relaciones importantes:

- Un usuario puede tener muchas sesiones.
- Una sesión pertenece a un usuario.
- Una sesión puede tener una recomendación.
- Una recomendación puede tener feedback.
- Usuarios y roles se relacionan mediante `usuarios_roles`.

El script base está en `database/schema/01_init.sql`.

En desarrollo, Hibernate usa `ddl-auto=validate`, por lo que valida el esquema al arrancar pero no lo modifica automáticamente.

## 6. Recomendaciones

El sistema de recomendaciones intenta usar Groq si hay clave configurada.

Variables relacionadas:

- `GROQ_API_KEY`
- `GROQ_API_URL`
- `GROQ_MODEL`
- `GROQ_MAX_TOKENS`

Si Groq responde correctamente, la recomendación se guarda con fuente `GROQ`.

Si no hay clave o la llamada falla, el backend usa el sistema interno de reglas y guarda la fuente como `REGLAS`.

La regeneración de recomendaciones sigue existiendo. El endpoint es:

```text
POST /api/recommendations/{sesionId}/retry
```

En frontend lo llama `SessionService.retryRecommendation()`, y la pantalla de recomendaciones tiene el método `retryRecommendation()`.

## 7. Docker

Docker se usa solo para PostgreSQL.

Comandos básicos:

```bash
docker compose up -d
docker compose ps
docker compose down
```

Si el volumen ya existía, `01_init.sql` no se vuelve a ejecutar automáticamente. Para recrear la base desde cero:

```bash
docker compose down -v
docker compose up -d
```

Esto borra los datos del volumen, así que conviene usarlo solo cuando se quiera reiniciar el entorno.


## 8. Tests 

Backend:

```powershell
cd backend
.\mvnw.cmd test
```

Frontend:

```bash
cd frontend
npm test
npm run build
```

Healthcheck:

```text
http://localhost:8081/api/health
```

## 10. Notas para futuros cambios

Antes de tocar entidades o SQL, revisar siempre:

- entidad JPA;
- DTOs relacionados;
- repositorio;
- `database/schema/01_init.sql`;
- constraints reales en PostgreSQL.

Antes de tocar autenticación, revisar:

- `SecurityConfig`;
- `JwtAuthenticationFilter`;
- `JwtServiceImpl`;
- `AuthServiceImpl`;
- `auth.interceptor.ts`;
- `auth.guard.ts`.

Antes de tocar recomendaciones, revisar:

- `RecomendacionController`;
- `RecomendacionService`;
- `GroqService`;
- `SessionService` en frontend;
- constraint de `fuente` en la tabla `recomendaciones`.
