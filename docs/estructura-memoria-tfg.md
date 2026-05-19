# Estructura tecnica para la memoria del TFG

Este documento sirve como punto de partida para redactar la parte tecnica de GamerMood. Resume el estado actual del proyecto sin proponer cambios de arquitectura.

## 1. Vision general

GamerMood es una aplicacion web que permite al usuario registrar sesiones de juego y asociarlas a un estado emocional. A partir de esos datos, el sistema muestra recomendaciones relacionadas con bienestar digital y sugerencias de juego.

Arquitectura general:

```text
Angular 21
   |
   | HTTP + JWT Bearer
   v
Spring Boot 3.5.13
   |
   | JPA / Hibernate
   v
PostgreSQL 17
```

Puntos importantes para explicar:

- Separacion frontend/backend.
- API REST protegida con JWT.
- Persistencia relacional con PostgreSQL.
- Recomendaciones generadas por reglas internas.
- Integracion OpenAI implementada como opcion, pero no usada en local por requerir facturacion.

## 2. Frontend

### Tecnologias

| Tecnologia | Uso |
| --- | --- |
| Angular 21 | SPA y componentes standalone. |
| TypeScript 5.9 | Tipado de componentes, servicios y modelos. |
| Angular Router | Navegacion entre pantallas y rutas protegidas. |
| Reactive Forms | Formularios de login, registro y sesion. |
| HttpClient | Comunicacion con el backend. |
| RxJS | Manejo de peticiones asincronas. |
| Vitest/jsdom | Configuracion de tests del frontend. |

### Estructura Angular

```text
frontend/src/app/
├── app.component.*
├── app.component.config.ts
├── app.component.routes.ts
├── components/
│   ├── consejos-personalizados/
│   ├── dashboard/
│   ├── encuentra-tu-juego/
│   ├── expresa-emociones/
│   ├── index/
│   ├── login/
│   ├── recommendations/
│   ├── register/
│   ├── register-success/
│   └── session/
├── guards/
│   └── auth.guard.ts
├── interceptors/
│   └── auth.interceptor.ts
├── models/
│   ├── recommendation.model.ts
│   ├── session.model.ts
│   └── user.model.ts
└── services/
    ├── auth.service.ts
    └── session.service.ts
```

### Componentes principales

| Componente | Responsabilidad |
| --- | --- |
| `index` | Pantalla inicial. |
| `login` | Inicio de sesion. |
| `register` | Registro de usuario. |
| `register-success` | Confirmacion posterior al registro. |
| `dashboard` | Listado de sesiones del usuario autenticado. |
| `session` | Formulario para crear una nueva sesion de juego. |
| `recommendations` | Muestra recomendaciones, juegos sugeridos y feedback. |
| `expresa-emociones` | Pagina informativa. |
| `consejos-personalizados` | Pagina informativa. |
| `encuentra-tu-juego` | Pagina informativa. |

### Servicios

`AuthService`:

- Llama a `/api/auth/login`.
- Llama a `/api/auth/register`.
- Guarda `gm_token` y `gm_user` en `localStorage`.
- Expone `isLoggedIn()`, `getToken()` y `getCurrentUser()`.

`SessionService`:

- Crea sesiones con `POST /api/sessions`.
- Lista sesiones con `GET /api/sessions`.
- Solicita recomendaciones con `POST /api/recommendations/{sesionId}`.
- Envia feedback con `POST /api/feedback/{recomendacionId}`.
- Solicita una nueva recomendacion con `POST /api/recommendations/{sesionId}/retry`.

### Guard e interceptor

`auth.guard.ts`:

- Protege rutas privadas.
- Si no hay token, redirige a `/login`.

`auth.interceptor.ts`:

- Anade `Authorization: Bearer <token>` a las peticiones HTTP cuando existe token.

### Flujo frontend

1. El usuario entra en la aplicacion.
2. Se registra o inicia sesion.
3. El token JWT se guarda en `localStorage`.
4. El usuario accede al dashboard o crea una sesion.
5. Al guardar una sesion, se navega a recomendaciones con el `sessionId`.
6. El componente de recomendaciones llama al backend para obtener el consejo.
7. El usuario puede enviar feedback o pedir otra recomendacion.

Puntos a mencionar en la defensa:

- Uso de componentes standalone.
- Proteccion de rutas en cliente.
- Separacion entre modelos, servicios y componentes.
- Interceptor para centralizar el token.
- Manejo basico de errores de conexion y autenticacion.

## 3. Backend

### Tecnologias

| Tecnologia | Uso |
| --- | --- |
| Java 17 | Lenguaje backend. |
| Spring Boot 3.5.13 | Base de la API REST. |
| Spring Web | Controladores HTTP. |
| Spring Data JPA | Repositorios y persistencia. |
| Spring Security | Autenticacion y autorizacion. |
| JJWT 0.12.6 | Creacion y validacion de tokens JWT. |
| PostgreSQL Driver | Conexion a PostgreSQL. |
| Bean Validation | Validacion de DTOs con anotaciones. |

### Estructura Spring Boot

```text
backend/src/main/java/com/gamermood/backend/
├── config/
│   └── CorsConfig.java
├── controller/
│   ├── AuthController.java
│   ├── FeedbackController.java
│   ├── HealthController.java
│   ├── RecomendacionController.java
│   └── SessionController.java
├── dto/
├── entity/
├── exception/
├── repository/
├── security/
└── service/
```

### Controladores

| Controlador | Endpoints |
| --- | --- |
| `HealthController` | `GET /api/health` |
| `AuthController` | `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/refresh` |
| `SessionController` | `POST /api/sessions`, `GET /api/sessions`, `GET /api/sessions/{id}` |
| `RecomendacionController` | `POST /api/recommendations/{sesionId}`, `POST /api/recommendations/{sesionId}/retry` |
| `FeedbackController` | `POST /api/feedback/{recomendacionId}` |

### Servicios

| Servicio | Responsabilidad |
| --- | --- |
| `AuthServiceImpl` | Registro, login, refresh token y asignacion de rol de usuario. |
| `JwtServiceImpl` | Generacion y validacion de JWT. |
| `SessionServiceImpl` | Creacion, listado y detalle de sesiones. |
| `RecomendacionService` | Generacion y regeneracion de recomendaciones. |
| `OpenAiService` | Integracion opcional con OpenAI. |
| `FeedbackService` | Registro de feedback de recomendaciones. |
| `EstadoSesionService` | Registro e historial de transiciones de estado. |

### Seguridad JWT

Flujo backend:

1. `AuthController.login` recibe email y password.
2. `AuthServiceImpl` busca el usuario y valida la contrasena con BCrypt.
3. `JwtServiceImpl` genera un token con email, userId, username y roles.
4. El frontend envia el token en `Authorization: Bearer`.
5. `JwtAuthenticationFilter` valida el token en cada request privada.
6. Si el token es valido, Spring Security coloca la autenticacion en el contexto.

Configuracion destacable:

- CSRF desactivado porque se usa API stateless.
- Sesiones HTTP desactivadas (`SessionCreationPolicy.STATELESS`).
- `/health`, `/error`, `/auth/register`, `/auth/login` y `/auth/refresh` son publicos.
- El resto de endpoints requiere autenticacion.

### DTOs

DTOs de entrada:

- `RegisterRequestDto`
- `LoginRequestDto`
- `SessionRequestDto`
- `FeedbackRequestDto`

DTOs de salida:

- `AuthResponseDto`
- `SessionResponseDto`
- `RecomendacionResponseDto`
- `FeedbackResponseDto`

Los DTOs permiten separar el contrato HTTP de las entidades JPA.

### Manejo de errores

`GlobalExceptionHandler` centraliza:

- Errores de validacion.
- Email ya registrado.
- Credenciales invalidas.
- Recursos no encontrados.
- Argumentos incorrectos.
- Errores no controlados.

Esto evita devolver trazas internas al frontend y mantiene respuestas mas consistentes.

## 4. Base de datos

### Motor

PostgreSQL 17, levantado con Docker Compose.

Configuracion actual:

```text
POSTGRES_DB=gamermood
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
Puerto local: 5432
```

### Modelo segun entidades JPA actuales

Tablas principales esperadas por el codigo:

| Tabla | Entidad | Descripcion |
| --- | --- | --- |
| `usuarios` | `User` | Usuarios registrados. |
| `roles` | `Role` | Roles de seguridad. |
| `usuarios_roles` | relacion `User` - `Role` | Relacion N:M entre usuarios y roles. |
| `sesiones_juego` | `GameSession` | Sesiones de juego registradas. |
| `recomendaciones` | `Recomendacion` | Recomendacion asociada a una sesion. |
| `feedback_recomendacion` | `FeedbackRecomendacion` | Feedback del usuario sobre una recomendacion. |
| `transiciones_estado` | `TransicionEstado` | Historial de estados de una sesion. |

Relaciones:

- Un usuario puede tener muchas sesiones.
- Un usuario puede tener varios roles y un rol puede pertenecer a varios usuarios.
- Una sesion tiene una recomendacion.
- Una recomendacion puede tener un feedback.
- Una sesion puede tener varias transiciones de estado.

Indices y restricciones relevantes:

- `usuarios.email` y `usuarios.username` son unicos.
- `roles.nombre` es unico segun entidad JPA.
- `usuarios_roles` une usuarios y roles.
- `recomendaciones.sesion_id` es unico en la entidad, por lo que se espera una recomendacion activa por sesion.

### Script SQL

El script `database/schema/01_init.sql` esta alineado con las entidades JPA actuales y se monta en Docker para inicializar PostgreSQL en entornos limpios. En perfil `dev`, Hibernate valida el esquema al arrancar para detectar diferencias entre codigo y base de datos.

## 5. Integracion IA / recomendaciones

Estado real:

- No hay proveedor gratuito externo integrado.
- OpenAI existe en codigo como integracion opcional.
- La aplicacion funciona sin `OPENAI_API_KEY`.
- El sistema efectivo es un motor de reglas en `RecomendacionService`.

Reglas principales:

- Entrada: `mood` e `intensity`.
- Salida: texto de recomendacion.
- Fuente guardada: `REGLAS`.

Flujo:

1. `RecomendacionController` recibe `sesionId`.
2. Busca la sesion del usuario autenticado.
3. Si ya existe recomendacion, la devuelve.
4. Si no existe, intenta OpenAI.
5. Sin clave, OpenAI devuelve `null`.
6. Se genera texto por reglas y se guarda en base de datos.

Esto es importante en la defensa porque muestra que se penso en IA externa, pero se adapto el alcance a una solucion viable sin coste.

## 6. Propuesta de indice para la memoria

1. Introduccion
2. Objetivos del proyecto
3. Analisis de requisitos
4. Tecnologias utilizadas
5. Arquitectura general
6. Diseno de base de datos
7. Desarrollo del backend
8. Desarrollo del frontend
9. Seguridad y autenticacion JWT
10. Sistema de recomendaciones
11. Pruebas realizadas
12. Despliegue y ejecucion local
13. Problemas encontrados y decisiones tecnicas
14. Conclusiones y lineas futuras

## 7. Ideas fuertes para la defensa

- El proyecto esta separado en capas claras: interfaz, API y base de datos.
- La autenticacion usa JWT y backend stateless.
- El frontend protege rutas y centraliza el token con un interceptor.
- Las recomendaciones no dependen de servicios de pago para funcionar.
- El uso de Docker facilita reproducir PostgreSQL.
- La documentacion distingue entre lo implementado, lo opcional y lo pendiente.
- Hay una base clara para evolucionar el sistema hacia IA externa si se dispone de clave o proveedor gratuito.
