# GamerMood — Documentación Técnica Completa

**Proyecto de Fin de Grado (TFG)**
**Versión:** 1.0 — Rama `develop`
**Fecha:** Mayo 2026

---

## Tabla de Contenidos

1. [Introducción](#1-introducción)
2. [Arquitectura General](#2-arquitectura-general)
3. [Frontend — Angular](#3-frontend--angular)
4. [Backend — Spring Boot](#4-backend--spring-boot)
5. [Base de Datos — PostgreSQL](#5-base-de-datos--postgresql)
6. [Flujo Completo de la Aplicación](#6-flujo-completo-de-la-aplicación)
7. [Seguridad y Autenticación](#7-seguridad-y-autenticación)
8. [Integración con Inteligencia Artificial (OpenAI)](#8-integración-con-inteligencia-artificial-openai)
9. [Despliegue y Ejecución Local](#9-despliegue-y-ejecución-local)
10. [Mejoras Futuras](#10-mejoras-futuras)
11. [Conclusiones Técnicas](#11-conclusiones-técnicas)

---

## 1. Introducción

### 1.1 Objetivo del Proyecto

GamerMood es una aplicación web que permite a los jugadores registrar y analizar su estado emocional durante las sesiones de videojuegos. El objetivo es proporcionar una herramienta de introspección digital que, a partir de datos sencillos aportados por el usuario (juego, humor, intensidad y descripción libre), genere recomendaciones personalizadas orientadas al bienestar y al rendimiento gaming.

### 1.2 Problema que Resuelve

El gaming es una actividad emocionalmente intensa que puede influir significativamente en el estado anímico de quien la practica. Sin embargo, rara vez los jugadores disponen de herramientas que les ayuden a:

- Identificar patrones emocionales asociados a ciertos géneros o momentos del día.
- Recibir orientación personalizada cuando el estado emocional es negativo.
- Construir un historial que les permita tomar decisiones más conscientes sobre sus hábitos de juego.

GamerMood cubre esta brecha combinando un registro estructurado de sesiones con análisis asistido por inteligencia artificial.

### 1.3 Público Objetivo

- Jugadores habituales interesados en mejorar su bienestar digital.
- Personas que perciben que el gaming afecta a su estado de ánimo.
- Usuarios que quieren llevar un historial emocional de su actividad como gamer.

### 1.4 Tecnologías Utilizadas

| Capa | Tecnología | Versión |
|------|-----------|---------|
| Frontend | Angular | 21 |
| Backend | Spring Boot | 3.5 |
| Lenguaje backend | Java | 17 |
| Base de datos | PostgreSQL | 15+ |
| IA generativa | OpenAI API (GPT-4o-mini) | — |
| Autenticación | JWT (JJWT) | 0.12.6 |
| ORM | Hibernate / Spring Data JPA | — |
| Testing frontend | Vitest | 4.x |
| Build frontend | Angular CLI / npm | 21 / 11.6 |
| Build backend | Maven | 3.x |
| Contenedor BD | Docker | — |

### 1.5 Concepto de GamerMood

GamerMood se estructura en torno a un flujo lineal de cinco pasos:

```
Registro/Login → Registrar sesión → Recibir recomendación → Valorar la recomendación → Ver historial
```

Cada sesión captura cuatro dimensiones del estado del jugador: el juego concreto, el humor general (happy / neutral / sad), la intensidad emocional (escala 1-10) y una descripción libre. Con estos datos, la aplicación genera una recomendación que puede provenir de OpenAI o de un sistema de reglas propio, garantizando siempre una respuesta aunque la API externa no esté disponible.

---

## 2. Arquitectura General

### 2.1 Modelo Cliente-Servidor

GamerMood sigue una arquitectura cliente-servidor clásica de tres capas con separación total entre presentación, lógica de negocio y persistencia:

```
┌─────────────────────┐          HTTP/REST          ┌──────────────────────┐
│   Angular 21        │  ──── Bearer JWT ──────────▶ │  Spring Boot 3.5     │
│   (Frontend)        │                              │  (Backend REST API)  │
│   Puerto 4200       │ ◀──── JSON responses ─────── │  Puerto 8081         │
└─────────────────────┘                              └──────────┬───────────┘
                                                                │ JDBC / JPA
                                                     ┌──────────▼───────────┐
                                                     │  PostgreSQL 15+      │
                                                     │  (Docker container)  │
                                                     │  Puerto 5432         │
                                                     └──────────────────────┘
```

Las tres partes son completamente independientes entre sí: el frontend consume una API REST estándar, el backend no tiene conocimiento de la tecnología del cliente, y la base de datos solo interactúa con el backend a través de JPA/Hibernate.

### 2.2 Relación entre Componentes

- **Angular → Spring Boot:** El frontend realiza peticiones HTTP con el token JWT en la cabecera `Authorization: Bearer`. El backend valida el token en cada petición protegida antes de ejecutar la lógica de negocio.
- **Spring Boot → PostgreSQL:** El backend usa Spring Data JPA con Hibernate como ORM. En entorno de desarrollo, Hibernate gestiona el esquema automáticamente (`ddl-auto: update`). En producción, el esquema se aplica desde el script SQL inicial.
- **Spring Boot → OpenAI:** El backend realiza llamadas HTTP directas a `https://api.openai.com/v1/chat/completions` usando el cliente HTTP nativo de Java 11+. No se usa ninguna librería SDK adicional.

### 2.3 Diagrama de Arquitectura Funcional

```
                         ┌─────────────────────────────────────────────┐
                         │              USUARIO (Navegador)            │
                         └───────────────────┬─────────────────────────┘
                                             │
                         ┌───────────────────▼─────────────────────────┐
                         │             ANGULAR 21 (SPA)                │
                         │                                             │
                         │  ┌──────────┐  ┌──────────┐  ┌──────────┐ │
                         │  │  Login   │  │ Session  │  │Dashboard │ │
                         │  └────┬─────┘  └────┬─────┘  └────┬─────┘ │
                         │       │              │              │        │
                         │  ┌────▼──────────────▼──────────────▼─────┐ │
                         │  │          AuthService / SessionService   │ │
                         │  │          AuthInterceptor (JWT header)   │ │
                         │  └────────────────────┬────────────────────┘ │
                         └───────────────────────┼─────────────────────┘
                                                 │ HTTP REST
                         ┌───────────────────────▼─────────────────────┐
                         │         SPRING BOOT API  (/api/*)           │
                         │                                             │
                         │  ┌────────────┐  ┌────────────────────────┐ │
                         │  │JwtFilter   │  │  Controllers           │ │
                         │  │(validación)│  │  /auth /sessions       │ │
                         │  └────────────┘  │  /recommendations      │ │
                         │                  │  /feedback             │ │
                         │  ┌────────────┐  └───────────┬────────────┘ │
                         │  │ Services   │              │              │
                         │  │ RecomSvc   │◀─────────────┘              │
                         │  │ OpenAiSvc  │──── HTTPS ───▶ OpenAI API  │
                         │  └────────────┘                             │
                         └─────────────────────┬───────────────────────┘
                                               │ JPA/Hibernate
                         ┌─────────────────────▼───────────────────────┐
                         │            POSTGRESQL 15+ (Docker)          │
                         │                                             │
                         │  usuarios · sesiones_juego · recomendaciones│
                         │  feedback_recomendacion · roles · estados   │
                         └─────────────────────────────────────────────┘
```

### 2.4 Estructura del Repositorio

```
gamermood-tfg/
├── backend/                  # Spring Boot (Maven)
│   └── src/main/java/com/gamermood/backend/
│       ├── config/           # CORS, configuración global
│       ├── controller/       # Endpoints REST
│       ├── dto/              # Contratos de API (request/response)
│       ├── entity/           # Entidades JPA
│       ├── exception/        # Excepciones y handler global
│       ├── repository/       # Spring Data JPA
│       ├── security/         # JWT filter, SecurityConfig
│       └── service/          # Lógica de negocio
├── frontend/                 # Angular 21
│   └── src/app/
│       ├── components/       # Componentes standalone
│       ├── guards/           # authGuard
│       ├── interceptors/     # authInterceptor
│       ├── models/           # Interfaces TypeScript
│       └── services/         # AuthService, SessionService
├── database/
│   └── schema/
│       └── 01_init.sql       # Script de inicialización completa
├── docs/                     # Documentación del proyecto
└── .env.example              # Variables de entorno de referencia
```

---

## 3. Frontend — Angular

### 3.1 Tecnología y Configuración

El frontend está construido con **Angular 21** usando la arquitectura moderna de **componentes standalone** (sin NgModules). Esto simplifica la gestión de dependencias y reduce el boilerplate, permitiendo que cada componente declare sus propias importaciones.

**Principales elecciones técnicas:**
- Formularios reactivos con `FormBuilder` y `Validators`.
- Routing mediante `provideRouter()` con `withComponentInputBinding()`.
- Interceptor HTTP funcional para inyección automática de JWT.
- Testing con **Vitest** (no Jasmine/Karma), integrado vía `@angular/build:unit-test`.
- Fuentes tipográficas: **Orbitron** (títulos, estilo gaming) e **Inter** (texto general).

### 3.2 Estructura de Componentes

```
src/app/
├── components/
│   ├── login/                        # Pantalla de inicio de sesión
│   ├── register/                     # Formulario de registro
│   ├── register-success/             # Confirmación de registro
│   ├── dashboard/                    # Historial de sesiones
│   ├── session/                      # Formulario de nueva sesión
│   ├── recommendations/              # Resultado y recomendaciones
│   ├── index/                        # Landing page principal
│   ├── expresa-emociones/            # Sección informativa
│   ├── consejos-personalizados/      # Sección informativa
│   └── encuentra-tu-juego/           # Sección informativa
├── guards/
│   └── auth.guard.ts                 # Protección de rutas autenticadas
├── interceptors/
│   └── auth.interceptor.ts           # Inyección automática de JWT
├── models/
│   ├── user.model.ts                 # User, LoginRequest, AuthResponse
│   ├── session.model.ts              # SessionRequest, SessionResponse
│   └── recommendation.model.ts      # Recommendation, FeedbackRequest
└── services/
    ├── auth.service.ts               # Login, registro, logout
    └── session.service.ts            # CRUD sesiones, recomendaciones, feedback
```

### 3.3 Rutas de la Aplicación

| Ruta | Componente | Protegida |
|------|-----------|-----------|
| `/` | IndexComponent | No |
| `/login` | LoginComponent | No |
| `/register` | RegisterComponent | No |
| `/register-success` | RegisterSuccessComponent | No |
| `/expresa-emociones` | ExpresaEmocionesComponent | No |
| `/consejos-personalizados` | ConsejosPersonalizadosComponent | No |
| `/encuentra-tu-juego` | EncuentraTuJuegoComponent | No |
| `/dashboard` | DashboardComponent | **Sí** |
| `/session` | SessionComponent | **Sí** |
| `/recommendations` | RecommendationsComponent | **Sí** |
| `**` | (redirect a `/`) | — |

### 3.4 Guards e Interceptores

**`auth.guard.ts`**

Protege las rutas privadas comprobando si el usuario tiene un token JWT almacenado en `localStorage`. Si no lo tiene, redirige a `/login`.

```typescript
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.isLoggedIn() ? true : inject(Router).createUrlTree(['/login']);
};
```

**`auth.interceptor.ts`**

Intercepta todas las peticiones HTTP salientes y añade automáticamente la cabecera `Authorization: Bearer {token}`:

```typescript
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(AuthService).getToken();
  if (token) {
    req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }
  return next(req);
};
```

### 3.5 Servicios

#### AuthService (`services/auth.service.ts`)

Gestiona el ciclo de vida de la sesión del usuario en el navegador.

| Método | Descripción |
|--------|-------------|
| `login(body)` | POST `/auth/login` → guarda token y datos de usuario en `localStorage` |
| `register(body)` | POST `/auth/register` |
| `logout()` | Limpia `localStorage` y redirige |
| `isLoggedIn()` | `true` si existe un token en `localStorage` |
| `getToken()` | Devuelve el JWT almacenado (clave: `gm_token`) |
| `getCurrentUser()` | Devuelve el objeto `User` almacenado (clave: `gm_user`) |

#### SessionService (`services/session.service.ts`)

Centraliza toda la comunicación con el backend relacionada con sesiones, recomendaciones y feedback.

| Método | Endpoint | Descripción |
|--------|---------|-------------|
| `createSession(body)` | POST `/sessions` | Crea una nueva sesión de juego |
| `getSessions()` | GET `/sessions` | Obtiene el historial del usuario |
| `getRecommendation(id)` | POST `/recommendations/{id}` | Genera/recupera una recomendación |
| `retryRecommendation(id)` | POST `/recommendations/{id}/retry` | Regenera la recomendación |
| `sendFeedback(id, body)` | POST `/feedback/{id}` | Envía valoración del usuario |
| `saveLocalSession(data)` | — | Fallback a `localStorage` |
| `getLocalSession()` | — | Recupera sesión local |

La clave de la URL base es la constante `API = 'http://localhost:8081/api'`.

### 3.6 Modelos de Datos (TypeScript)

**`user.model.ts`**
```typescript
interface User           { id: number; username: string; email: string; roles: string[] }
interface LoginRequest   { email: string; password: string }
interface RegisterRequest { username: string; email: string; password: string }
interface AuthResponse   { token: string; refreshToken: string; userId: number;
                           username: string; email: string; roles: string[] }
```

**`session.model.ts`**
```typescript
interface SessionRequest  { game: string; mood: 'happy'|'neutral'|'sad';
                            intensity: number; experience: string }
interface SessionResponse { id: number; game: string; mood: string;
                            intensity: number; experience: string; createdAt: string }
```

**`recommendation.model.ts`**
```typescript
interface Recommendation { id: number; sesionId: number; texto: string;
                           fuente: 'REGLAS'|'OPENAI'; createdAt: string }
interface FeedbackRequest { util: boolean; comentario?: string }
```

### 3.7 Componentes Principales

#### LoginComponent

Formulario reactivo con campos `email` (validación de formato) y `password` (mínimo 6 caracteres). Muestra mensajes de error específicos para credenciales incorrectas (401) y errores de red. Incluye indicador de carga durante el proceso.

#### RegisterComponent

Además de los campos estándar, implementa un **validador personalizado** `passwordMatchValidator` que comprueba que `password` y `confirmPassword` coincidan. Gestiona el error 409 (email ya registrado) con un mensaje descriptivo.

#### SessionComponent

El formulario de registro de sesión es el núcleo funcional de la aplicación. Incluye:
- **Selector de juego:** desplegable con categorías predefinidas (FPS, MOBA, RPG, Indie, Simulación, Deportes, Lucha, Terror, MMO) más opción "Otro…" que activa un campo libre.
- **Selector de humor:** tres botones con iconos y estilos visuales diferenciados (happy/neutral/sad).
- **Intensidad:** slider numérico del 1 al 10 con segmentos visuales de color progresivo.
- **Descripción libre:** área de texto para que el usuario describa su experiencia.
- **Fallback offline:** si el backend no está disponible, guarda la sesión en `localStorage` y redirige igualmente a recomendaciones.

#### DashboardComponent

Vista del historial de sesiones. Las tarjetas incluyen nombre del juego, etiqueta de humor con color semántico (verde/amarillo/rojo), nivel de intensidad, fragmento de la descripción y fecha. El estado vacío muestra una llamada a la acción para registrar la primera sesión.

Las etiquetas de humor se traducen al español:

| Valor API | Texto mostrado |
|-----------|---------------|
| `happy` | Genial |
| `neutral` | Normal |
| `sad` | Frustrado |

#### RecommendationsComponent

Muestra el resultado del análisis de la sesión en tres bloques:

1. **Cabecera con snapshot emocional:** humor actual, barras de intensidad y juego.
2. **Consejos personalizados:** lista numerada de recomendaciones. Si hay `sessionId` disponible, se carga la recomendación real del backend; de lo contrario, usa datos mock locales.
3. **Juegos sugeridos:** tarjetas con efecto de "flip" que revelan la descripción del juego al hacer clic (implementado con `flippedCards: Set<number>` y el método `toggleCard(index)`).
4. **Sección de feedback:** botones "Útil" / "No útil" más área de texto para comentario. Tras valorar negativamente, aparece el botón de reintento.

### 3.8 Diseño Visual

La interfaz sigue una estética **gaming oscura** coherente en todos los componentes:
- Paleta de colores: fondo oscuro (`#0c061e`), acento principal morado-violeta (`#b84fff`), gradientes púrpura-fucsia en botones primarios.
- Fuente `Orbitron` para títulos, con `letter-spacing` ampliado para reforzar el estilo.
- Tarjetas con `backdrop-filter: blur()` (glassmorphism).
- Animaciones CSS de entrada (`translateY`, `scale`, `fade`).
- Los botones de navegación de vuelta atrás son de **posición fija** (`position: fixed`), visibles en todo momento durante el scroll.

---

## 4. Backend — Spring Boot

### 4.1 Tecnología y Configuración

El backend está implementado con **Spring Boot 3.5** sobre **Java 17**. Sigue la arquitectura estándar en capas: Controller → Service → Repository → Entity.

**Dependencias principales (pom.xml):**

| Dependencia | Rol |
|------------|-----|
| `spring-boot-starter-web` | API REST con Spring MVC |
| `spring-boot-starter-data-jpa` | ORM con Hibernate |
| `spring-boot-starter-security` | Autenticación y autorización |
| `spring-boot-starter-validation` | Validación de DTOs con Bean Validation |
| `postgresql` | Driver JDBC |
| `jjwt-api` / `jjwt-impl` / `jjwt-jackson` (v0.12.6) | Generación y validación de JWT |

**Configuración base (`application.yaml`):**
- Puerto: `8081` (configurable con `SERVER_PORT`)
- Context path: `/api`
- Sesión: `STATELESS` (sin estado de sesión en servidor)
- Perfiles: `dev` (por defecto) y `prod`

### 4.2 Estructura de Paquetes

```
com.gamermood.backend/
├── config/
│   └── CorsConfig.java              # Filtro CORS para Angular (localhost:4200)
├── controller/
│   ├── AuthController.java          # /auth/*
│   ├── SessionController.java       # /sessions/*
│   ├── RecomendacionController.java # /recommendations/*
│   ├── FeedbackController.java      # /feedback/*
│   └── HealthController.java        # /health
├── dto/
│   ├── LoginRequestDto.java
│   ├── RegisterRequestDto.java
│   ├── AuthResponseDto.java
│   ├── SessionRequestDto.java
│   ├── SessionResponseDto.java
│   ├── RecomendacionResponseDto.java
│   ├── FeedbackRequestDto.java
│   └── FeedbackResponseDto.java
├── entity/
│   ├── User.java
│   ├── Role.java
│   ├── GameSession.java
│   ├── Recomendacion.java
│   ├── FeedbackRecomendacion.java
│   ├── EstadoSesion.java            # Enum de estados
│   └── TransicionEstado.java        # Traza de auditoría
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ApiError.java
│   ├── CredencialesInvalidasException.java
│   ├── EmailYaRegistradoException.java
│   └── RecursoNoEncontradoException.java
├── repository/
│   ├── UserRepository.java
│   ├── SessionRepository.java
│   ├── RecomendacionRepository.java
│   ├── FeedbackRepository.java
│   ├── RoleRepository.java
│   └── TransicionEstadoRepository.java
├── security/
│   ├── SecurityConfig.java
│   ├── JwtAuthenticationFilter.java
│   └── UserDetailsServiceImpl.java
└── service/
    ├── AuthService.java / AuthServiceImpl.java
    ├── JwtService.java / JwtServiceImpl.java
    ├── SessionService.java / SessionServiceImpl.java
    ├── RecomendacionService.java
    ├── OpenAiService.java
    ├── FeedbackService.java
    └── EstadoSesionService.java
```

### 4.3 Endpoints REST

**URL base:** `http://localhost:8081/api`

#### Autenticación (`/auth`)

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| POST | `/auth/register` | Público | Registro de nuevo usuario |
| POST | `/auth/login` | Público | Login, devuelve JWT + refresh token |
| POST | `/auth/refresh` | Público | Renueva el JWT con el refresh token |

**POST `/auth/register` — Request:**
```json
{
  "username": "jugador123",
  "email": "jugador@email.com",
  "password": "contraseña8"
}
```

**POST `/auth/login` — Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "username": "jugador123",
  "email": "jugador@email.com",
  "roles": ["ROLE_USER"]
}
```

**POST `/auth/refresh` — Request:**
```json
{ "refreshToken": "eyJhbGciOiJIUzI1NiJ9..." }
```

#### Sesiones (`/sessions`)

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| POST | `/sessions` | Autenticado | Crea una nueva sesión de juego |
| GET | `/sessions` | Autenticado | Lista todas las sesiones del usuario |
| GET | `/sessions/{id}` | Autenticado | Obtiene una sesión específica |

**POST `/sessions` — Request:**
```json
{
  "game": "Counter-Strike 2",
  "mood": "neutral",
  "intensity": 8,
  "experience": "Partida muy reñida, cometí varios errores al final."
}
```

**POST `/sessions` — Response:**
```json
{
  "id": 42,
  "game": "Counter-Strike 2",
  "mood": "neutral",
  "intensity": 8,
  "experience": "Partida muy reñida, cometí varios errores al final.",
  "createdAt": "2026-05-13T19:45:00"
}
```

#### Recomendaciones (`/recommendations`)

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| POST | `/recommendations/{sesionId}` | Autenticado | Genera (o recupera) una recomendación |
| POST | `/recommendations/{sesionId}/retry` | Autenticado | Elimina la anterior y genera una nueva |

**POST `/recommendations/42` — Response:**
```json
{
  "id": 15,
  "sesionId": 42,
  "texto": "Sesión intensa con estado neutro. Descansa un poco antes de la siguiente.",
  "fuente": "REGLAS",
  "createdAt": "2026-05-13T19:46:10"
}
```

El campo `fuente` puede ser `"REGLAS"` (sistema propio) u `"OPENAI"` (GPT-4o-mini).

#### Feedback (`/feedback`)

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| POST | `/feedback/{recomendacionId}` | Autenticado | Guarda la valoración del usuario |

**POST `/feedback/15` — Request:**
```json
{
  "util": false,
  "comentario": "No se ajusta a lo que esperaba."
}
```

**POST `/feedback/15` — Response:**
```json
{
  "id": 8,
  "recomendacionId": 15,
  "util": false,
  "comentario": "No se ajusta a lo que esperaba.",
  "createdAt": "2026-05-13T19:47:05"
}
```

#### Salud (`/health`)

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| GET | `/health` | Público | Comprobación de estado del servidor |

### 4.4 Servicios — Lógica de Negocio

#### AuthServiceImpl

Gestiona el registro y el login de usuarios:

- **Registro:** valida que el email no esté duplicado, codifica la contraseña con BCrypt, asigna el rol `ROLE_USER` y persiste el usuario.
- **Login:** localiza el usuario por email, verifica la contraseña con `PasswordEncoder.matches()`, genera un JWT de acceso (24 horas) y un refresh token (7 días).
- **Refresh:** valida el refresh token, extrae el email del subject y emite un nuevo JWT de acceso.

#### SessionServiceImpl

CRUD de sesiones de juego. Todas las operaciones verifican que la sesión pertenezca al usuario autenticado mediante `findByIdAndUsuarioId()`, previniendo acceso cruzado entre usuarios.

#### RecomendacionService

Implementa la lógica de generación de recomendaciones con estrategia de cascada:

```
generarParaSesion(sesionId, userId)
  ├── Verificar que la sesión pertenece al usuario
  ├── Si ya existe una recomendación → devolverla (idempotente)
  └── Si no existe:
      ├── Intentar OpenAiService.generarRecomendacion()
      │   ├── Si responde → fuente = "OPENAI"
      │   └── Si falla/no hay key → fuente = "REGLAS"
      └── Persistir y devolver la recomendación
```

Para el reintento (`regenerarParaSesion`), elimina la recomendación existente, realiza `flush()` para garantizar la eliminación en base de datos antes de insertar la nueva, y aplica textos alternativos en el fallback por reglas para que el usuario perciba una respuesta diferente.

#### FeedbackService

Persiste la valoración del usuario (booleano `util` + comentario opcional) asociada a una recomendación. Valida previamente que la recomendación exista.

### 4.5 DTOs

Todos los DTOs del backend se implementan como **Java Records** (inmutables, sin boilerplate). Las validaciones usan anotaciones de Bean Validation:

```java
public record SessionRequestDto(
    @NotBlank String game,
    @NotBlank String mood,
    @Min(1) @Max(10) int intensity,
    String experience
) {}
```

### 4.6 Manejo de Errores

El `GlobalExceptionHandler` (anotado con `@RestControllerAdvice`) centraliza el manejo de excepciones y devuelve siempre una estructura `ApiError` consistente:

```json
{
  "timestamp": "2026-05-13T19:50:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Credenciales incorrectas",
  "path": "/api/auth/login"
}
```

| Excepción | HTTP Status |
|-----------|------------|
| `CredencialesInvalidasException` | 401 |
| `EmailYaRegistradoException` | 409 |
| `RecursoNoEncontradoException` | 404 |
| `MethodArgumentNotValidException` | 400 |
| `RuntimeException` (genérica) | 500 |

---

## 5. Base de Datos — PostgreSQL

### 5.1 Motor y Versión

El proyecto usa **PostgreSQL 15+**, ejecutándose actualmente en un contenedor Docker en desarrollo local. El esquema completo se inicializa con el script `database/schema/01_init.sql`.

### 5.2 Modelo Relacional

```
roles (id, name)
    ↑
usuarios_roles (usuario_id FK, rol_id FK) — tabla de unión N:M
    |
usuarios (id, username, email, password_hash, activo, created_at, updated_at)
    |
    └──1:N── sesiones_juego (id, usuario_id FK, game, mood, intensity, experience, created_at)
                  |
                  ├──1:1── recomendaciones_generadas (id, sesion_id FK, categoria_id FK,
                  │                                   runbook_id FK, contenido, fuente, created_at)
                  │             |
                  │             └──1:1── feedback_recomendacion (id, recomendacion_id FK,
                  │                                              util, comentario, created_at)
                  |
                  └──1:N── transiciones_flujo (id, sesion_id FK, estado_desde_id FK,
                                               estado_hasta_id FK, created_at)
                                    ↑
                            estados_flujo (id, nombre, descripcion)
```

### 5.3 Tablas

#### `roles`
| Columna | Tipo | Restricciones |
|---------|------|--------------|
| id | SERIAL | PK |
| name | VARCHAR(30) | UNIQUE, NOT NULL |

Datos iniciales: `USER`, `ADMIN`.

#### `usuarios`
| Columna | Tipo | Restricciones |
|---------|------|--------------|
| id | SERIAL | PK |
| username | VARCHAR(50) | UNIQUE, NOT NULL |
| email | VARCHAR(100) | UNIQUE, NOT NULL |
| password_hash | VARCHAR(255) | NOT NULL |
| activo | BOOLEAN | DEFAULT TRUE |
| created_at | TIMESTAMP | DEFAULT NOW() |
| updated_at | TIMESTAMP | DEFAULT NOW(), actualizado por trigger |

Un **trigger** `trg_usuarios_updated_at` actualiza automáticamente `updated_at` en cada modificación de la fila.

#### `usuarios_roles`
Tabla de unión para la relación N:M entre usuarios y roles. Clave primaria compuesta `(usuario_id, rol_id)` con `ON DELETE CASCADE` en ambas claves foráneas.

#### `sesiones_juego`
| Columna | Tipo | Restricciones |
|---------|------|--------------|
| id | SERIAL | PK |
| usuario_id | INTEGER | FK → usuarios(id) CASCADE |
| game | VARCHAR(150) | NOT NULL |
| mood | VARCHAR(20) | CHECK IN ('happy', 'neutral', 'sad') |
| intensity | SMALLINT | CHECK 1–10 |
| experience | TEXT | nullable |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() |

**Índice:** `idx_sesiones_usuario_fecha ON (usuario_id, created_at DESC)` — optimiza la consulta principal del dashboard.

#### `recomendaciones_generadas`
| Columna | Tipo | Restricciones |
|---------|------|--------------|
| id | SERIAL | PK |
| sesion_id | INTEGER | FK → sesiones_juego(id) CASCADE, UNIQUE |
| categoria_id | INTEGER | FK → categorias_recomendacion(id), nullable |
| runbook_id | INTEGER | FK → runbooks(id), nullable |
| contenido | TEXT | NOT NULL |
| fuente | VARCHAR(20) | DEFAULT 'REGLAS', CHECK IN ('REGLAS','OPENAI') |
| created_at | TIMESTAMP | DEFAULT NOW() |

La restricción `UNIQUE` en `sesion_id` garantiza la relación 1:1 con la sesión.

#### `feedback_recomendacion`
| Columna | Tipo | Restricciones |
|---------|------|--------------|
| id | SERIAL | PK |
| recomendacion_id | INTEGER | FK → recomendaciones_generadas(id) CASCADE, UNIQUE |
| util | BOOLEAN | NOT NULL |
| comentario | TEXT | nullable |
| created_at | TIMESTAMP | DEFAULT NOW() |

#### `estados_flujo` y `transiciones_flujo`

Implementan el registro de auditoría del ciclo de vida de las sesiones. `transiciones_flujo` actúa como un log inmutable: cada fila representa un cambio de estado, y el estado actual de una sesión es siempre la transición más reciente.

| Estado | Descripción |
|--------|------------|
| `SESION_REGISTRADA` | El usuario ha guardado la sesión |
| `SESION_CLASIFICADA` | La sesión ha sido analizada |
| `RECOMENDACION_GENERADA` | Se ha generado una recomendación |
| `FEEDBACK_RECIBIDO` | El usuario ha valorado la recomendación |
| `FLUJO_CERRADO` | El flujo finalizó con satisfacción |
| `REINTENTO_SOLICITADO` | El usuario solicita nueva recomendación |

#### Tablas de extensión (preparadas para uso futuro)

| Tabla | Propósito |
|-------|----------|
| `categorias_recomendacion` | Clasificación temática de recomendaciones |
| `prompts_ia` | Plantillas de prompts gestionables desde base de datos |
| `runbooks` | Agrupaciones de lógica de recomendación compleja |

Estas tablas están relacionadas con `recomendaciones_generadas` pero aún no se usan activamente en la lógica de negocio.

### 5.4 Diagrama Entidad-Relación (simplificado)

```
┌──────────┐       ┌──────────────────┐       ┌──────────────┐
│  roles   │◀──────│  usuarios_roles  │──────▶│   usuarios   │
│ id (PK)  │  N:M  │ usuario_id (FK)  │  1:N  │  id (PK)     │
│ name     │       │ rol_id (FK)      │       │  username    │
└──────────┘       └──────────────────┘       │  email       │
                                              │  password_   │
                                              │  hash        │
                                              └──────┬───────┘
                                                     │ 1:N
                                              ┌──────▼───────────────┐
                                              │  sesiones_juego      │
                                              │  id (PK)             │
                                              │  usuario_id (FK)     │
                                              │  game                │
                                              │  mood                │
                                              │  intensity           │
                                              │  experience          │
                                              └──────┬───────────────┘
                                                     │ 1:1
                                  ┌──────────────────▼────────────┐
                                  │  recomendaciones_generadas    │
                                  │  id (PK)                      │
                                  │  sesion_id (FK, UNIQUE)       │
                                  │  contenido                    │
                                  │  fuente ('REGLAS'|'OPENAI')   │
                                  └──────────────┬────────────────┘
                                                 │ 1:1
                                  ┌──────────────▼───────────┐
                                  │  feedback_recomendacion  │
                                  │  id (PK)                 │
                                  │  recomendacion_id (UNIQ) │
                                  │  util (BOOLEAN)          │
                                  │  comentario              │
                                  └──────────────────────────┘
```

---

## 6. Flujo Completo de la Aplicación

Este diagrama describe el flujo de usuario desde el registro hasta el historial, pasando por el ciclo completo de análisis emocional.

### 6.1 Registro e Inicio de Sesión

```
Usuario ──POST /auth/register──▶ Backend
         (username, email, password)
             │
             ▼
    Validar email no duplicado
    Hashear contraseña (BCrypt)
    Asignar ROLE_USER
    Persistir usuario
             │
             ▼
         200 OK ──▶ Frontend muestra /register-success

Usuario ──POST /auth/login──▶ Backend
         (email, password)
             │
             ▼
    Verificar credenciales
    Generar JWT (24h) + Refresh Token (7d)
    Devolver AuthResponse
             │
             ▼
    Frontend guarda token en localStorage
    Redirige a /dashboard
```

### 6.2 Creación de Sesión

```
Usuario (/session) rellena formulario:
  game | mood | intensity | experience
             │
             ▼
Frontend ──POST /sessions──▶ Backend
  Authorization: Bearer {JWT}
             │
             ▼
    Extraer userId del JWT (JwtFilter)
    Validar datos (Bean Validation)
    Persistir sesión en sesiones_juego
             │
             ▼
    SessionResponse (id, game, mood, ...)
             │
             ▼
    Frontend redirige a /recommendations
    Pasa sessionId por Router state
```

### 6.3 Generación de Recomendación

```
Frontend ──POST /recommendations/{sessionId}──▶ Backend
             │
             ▼
    Verificar que la sesión pertenece al usuario
    ¿Existe ya recomendación?
         SÍ ──▶ Devolver la existente (idempotente)
         NO ──▶
               ¿OPENAI_API_KEY configurada?
                    SÍ ──▶ Llamar GPT-4o-mini con prompt contextual
                               │
                          ¿Respuesta OK?
                               SÍ ──▶ fuente = "OPENAI"
                               NO ──▶ fuente = "REGLAS"
                    NO ──▶ fuente = "REGLAS"
                                    │
                    Generar texto por reglas (mood + intensity)
             │
             ▼
    Persistir recomendación
    Devolver RecomendacionResponseDto
             │
             ▼
    Frontend muestra texto + juegos sugeridos (mock)
```

### 6.4 Feedback y Reintento

```
Usuario valora la recomendación:
  [Útil] ──▶ POST /feedback/{recomendacionId} { util: true }
  [No útil] ──▶ POST /feedback/{recomendacionId} { util: false, comentario? }
             │
             ▼
    Backend persiste feedback
             │
             ▼
  Si util = false:
    Usuario puede pulsar "Reintentar"
         ──▶ POST /recommendations/{sessionId}/retry
             │
             ▼
    Backend elimina recomendación anterior
    Genera nueva recomendación (OpenAI → reglas alternativas)
    Devuelve nueva RecomendacionResponseDto
```

### 6.5 Historial (Dashboard)

```
Usuario navega a /dashboard
             │
Frontend ──GET /sessions──▶ Backend
             │
             ▼
    Listar sesiones del usuario (ORDER BY created_at DESC)
             │
             ▼
    Frontend renderiza grid de tarjetas
    Tarjeta por sesión: game, mood badge, intensity, experience, date
```

---

## 7. Seguridad y Autenticación

### 7.1 JSON Web Tokens (JWT)

GamerMood usa JWT (JSON Web Tokens) como mecanismo de autenticación **stateless**: el servidor no almacena ningún estado de sesión. Cada petición autenticada debe incluir un token válido en la cabecera HTTP.

**Estructura del payload del JWT de acceso:**
```json
{
  "sub": "jugador@email.com",
  "userId": 1,
  "username": "jugador123",
  "roles": ["ROLE_USER"],
  "iat": 1747165200,
  "exp": 1747251600
}
```

**Tipos de tokens:**

| Tipo | Expiración | Claim especial | Uso |
|------|-----------|---------------|-----|
| Access Token | 24 horas | — | Autenticar peticiones |
| Refresh Token | 7 días | `type: "refresh"` | Obtener nuevo access token |

**Algoritmo de firma:** HMAC-SHA256 con clave secreta de mínimo 64 caracteres.

La implementación usa **JJWT 0.12.6** (`JwtServiceImpl`):

```java
public String generarToken(String email, Long userId, String username, List<String> roles) {
    return Jwts.builder()
            .subject(email)
            .claim("userId", userId)
            .claim("username", username)
            .claim("roles", roles)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expirationMs))
            .signWith(getSigningKey())
            .compact();
}
```

### 7.2 Filtro de Autenticación JWT

El `JwtAuthenticationFilter` se ejecuta en cada petición antes del controlador:

```
Request entrante
      │
      ▼
Extraer cabecera Authorization
      │
¿Formato "Bearer {token}"?
   NO ──▶ Continuar sin autenticar
   SÍ ──▶
      │
      ▼
Extraer email del token (sub)
Cargar UserDetails desde BD
Validar token (firma + expiración)
      │
      ▼
Establecer SecurityContext
Continuar hacia el controlador
```

### 7.3 Configuración de Spring Security

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/health").permitAll()
    .requestMatchers("/auth/register", "/auth/login", "/auth/refresh").permitAll()
    .anyRequest().authenticated()
)
```

- CSRF deshabilitado (innecesario en API stateless).
- Sesiones HTTP deshabilitadas (`STATELESS`).
- Form login y HTTP Basic deshabilitados.
- CORS gestionado por `CorsConfig` independiente.

### 7.4 Codificación de Contraseñas

Las contraseñas se almacenan como hash BCrypt con factor de coste 10 (por defecto). BCrypt incluye sal aleatoria en cada hash, lo que lo hace resistente a ataques de tabla arcoíris.

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### 7.5 CORS

El `CorsConfig` permite peticiones desde `http://localhost:4200` (Angular en desarrollo), con todos los métodos HTTP y la cabecera `Authorization` incluida. Esta configuración debe revisarse antes de un despliegue en producción para restringir el origen permitido.

### 7.6 Variables de Entorno Sensibles

Las credenciales y claves nunca están en código fuente. Se gestionan mediante variables de entorno referenciadas desde `application.yaml`:

```yaml
jwt:
  secret: ${JWT_SECRET}          # mínimo 64 caracteres
  expiration-ms: 86400000        # 24 horas
  refresh-expiration-ms: 604800000  # 7 días
openai:
  api-key: ${OPENAI_API_KEY}
```

El archivo `.env.example` sirve de referencia para cada entorno sin exponer valores reales.

### 7.7 Protección de Rutas en Frontend

El `authGuard` protege las rutas privadas (`/dashboard`, `/session`, `/recommendations`). El `authInterceptor` añade automáticamente el JWT a todas las peticiones HTTP, evitando que el desarrollador tenga que gestionarlo manualmente en cada servicio.

---

## 8. Integración con Inteligencia Artificial (OpenAI)

### 8.1 Servicio OpenAI

El backend integra **OpenAI GPT-4o-mini** a través de `OpenAiService`, usando el cliente HTTP nativo de Java 11+ sin dependencias SDK adicionales. Esto mantiene el proyecto ligero y evita versiones de librerías que puedan quedar obsoletas rápidamente.

### 8.2 Prompt de Usuario

Cuando el usuario registra una sesión, `OpenAiService` construye el siguiente prompt contextual:

```
He jugado a {juego}. Mi estado de ánimo era '{mood}' con una intensidad 
de {intensidad} sobre 10. Descripción: {descripcion}.
Dame una recomendación breve y útil sobre mi sesión.
```

**Configuración del sistema (system message):**
```
Eres un asistente experto en bienestar digital y videojuegos. 
Da consejos breves, empáticos y útiles.
```

**Parámetros del modelo:**
- Modelo: `gpt-4o-mini`
- `max_tokens`: 500
- Endpoint: `https://api.openai.com/v1/chat/completions`

### 8.3 Estrategia de Fallback por Reglas

Si la clave de API no está configurada o la llamada falla, `RecomendacionService` genera automáticamente una recomendación mediante un sistema de reglas propio basado en `mood` e `intensity`:

| Mood | Intensidad | Recomendación (primera vez) |
|------|-----------|----------------------------|
| happy/excited | ≥ 7 | Invitar a jugar con otros para mantener la energía |
| happy/excited | < 7 | Explorar juegos nuevos aprovechando la motivación |
| neutral | ≥ 7 | Descansar antes de la siguiente sesión |
| neutral | < 7 | Probar un juego relajado para mejorar el humor |
| sad | ≥ 7 | Tomar un descanso y hablar con alguien |
| sad | < 7 | Juegos cooperativos o de historia para desconectar |
| angry | ≥ 7 | Parar y respirar antes de continuar |
| angry | < 7 | Juegos de puzzles o estrategia lenta |

Para el **reintento**, se aplica una segunda tabla de reglas con textos alternativos, asegurando que el usuario perciba una respuesta diferente a la anterior.

### 8.4 Identificación del Origen

Cada recomendación almacena su origen en el campo `fuente`:
- `"OPENAI"` — Generada por GPT-4o-mini.
- `"REGLAS"` — Generada por el sistema de reglas propio.

Esto permite en el futuro analizar qué porcentaje de recomendaciones proviene de cada fuente y medir la disponibilidad real de la API.

### 8.5 Limitaciones Actuales

- La extracción del texto de la respuesta JSON de OpenAI se realiza mediante parsing manual de cadenas (sin librería JSON), lo que depende del formato exacto de la respuesta.
- No se implementa caché de recomendaciones por contexto: cada sesión con los mismos parámetros podría generar textos distintos.
- No hay reintentos automáticos si la API falla por timeout o error transitorio; se cae directamente al fallback.
- El prompt es fijo y no es configurable desde base de datos (aunque las tablas `prompts_ia` y `runbooks` están diseñadas para ello en el futuro).

---

## 9. Despliegue y Ejecución Local

### 9.1 Requisitos del Sistema

| Componente | Versión mínima |
|-----------|----------------|
| Java JDK | 17 |
| Maven | 3.9+ |
| Node.js | 20+ |
| npm | 10+ |
| Docker | 24+ |
| Angular CLI | 21 |
| PostgreSQL | 15+ (via Docker) |

### 9.2 Variables de Entorno

Copiar `.env.example` a `.env` y configurar los valores:

```bash
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8081
DB_HOST=localhost
DB_PORT=5432
DB_NAME=gamermood
DB_USER=postgres
DB_PASSWORD=postgres
JWT_SECRET=gamermood_super_secret_key_2026_minimum_64_chars_secure
OPENAI_API_KEY=sk-...    # opcional; sin esta clave se usará el sistema de reglas
```

> **Importante:** El `JWT_SECRET` debe tener al menos 64 caracteres. La clave de ejemplo del repositorio es válida solo para desarrollo local.

### 9.3 Base de Datos con Docker

**Iniciar el contenedor PostgreSQL:**

```bash
docker run -d \
  --name gamermood-postgres \
  -e POSTGRES_DB=gamermood \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15
```

**Aplicar el esquema inicial:**

```bash
docker exec -i gamermood-postgres \
  psql -U postgres -d gamermood < database/schema/01_init.sql
```

Este script crea todas las tablas, índices, triggers y datos iniciales de roles, categorías y estados de flujo.

> En entorno `dev`, Hibernate está configurado con `ddl-auto: update`, por lo que puede actualizar el esquema automáticamente. El script SQL es necesario para el entorno `prod` donde Flyway gestiona las migraciones.

### 9.4 Arrancar el Backend

```bash
cd backend
mvn clean package -DskipTests
java -jar target/gamermood-backend-*.jar
```

O directamente en modo desarrollo:

```bash
cd backend
mvn spring-boot:run
```

El servidor arranca en `http://localhost:8081/api`. Se puede verificar con:

```bash
curl http://localhost:8081/api/health
```

### 9.5 Arrancar el Frontend

```bash
cd frontend
npm install
ng serve
```

La aplicación queda disponible en `http://localhost:4200`.

Para el **build de producción:**

```bash
cd frontend
npm run build
# Resultado en frontend/dist/
```

### 9.6 Orden de Arranque

```
1. docker run  ...  (PostgreSQL en Docker)
2. mvn spring-boot:run  (Backend Spring Boot)
3. ng serve  (Frontend Angular)
```

### 9.7 Ejecución de Tests

**Backend:**
```bash
cd backend
mvn test
```

**Frontend:**
```bash
cd frontend
npx vitest run
```

Los tests del frontend usan **Vitest** con la configuración integrada en `@angular/build:unit-test`.

---

### 9.8 Integración Futura con Docker Compose

#### Motivación

El flujo de arranque actual requiere que cada desarrollador configure manualmente Docker, la base de datos, el backend y el frontend. Esto genera fricción en el onboarding y posibles inconsistencias de entorno entre integrantes del equipo.

#### Plan Previsto

En una iteración futura se añadirá un archivo `docker-compose.yml` en la raíz del repositorio que permita levantar todo el entorno de desarrollo con un único comando:

```bash
docker compose up
```

El fichero orquestaría al menos estos servicios:

| Servicio | Imagen | Puerto |
|---------|--------|--------|
| `db` | `postgres:15` | 5432 |
| `backend` | Build local del JAR | 8081 |
| `frontend` | Build local Angular | 4200 |

#### Ventajas Esperadas

- **Reproducibilidad:** cualquier integrante del equipo obtendrá exactamente el mismo entorno con el mismo comando, independientemente del sistema operativo.
- **Reducción de problemas de configuración:** las variables de entorno se gestionarán mediante un archivo `.env` único leído por Docker Compose, eliminando configuraciones manuales redundantes.
- **Inicialización automática de la base de datos:** el contenedor PostgreSQL ejecutará el script `01_init.sql` en el primer arranque a través del mecanismo de `docker-entrypoint-initdb.d`.
- **Onboarding simplificado:** un nuevo desarrollador solo necesitará tener instalados Docker y Git para ejecutar el proyecto completo.
- **Aislamiento:** cada instancia del proyecto tendrá su propia red y volúmenes Docker, evitando conflictos con otros proyectos del equipo.

> **Estado actual:** esta integración está planificada pero no implementada. Actualmente PostgreSQL corre en Docker de forma independiente, mientras que el backend y el frontend se arrancan manualmente en local.

---

## 10. Mejoras Futuras

### 10.1 Funcionalidades de Usuario

- **Estadísticas personales:** gráficas de evolución del humor a lo largo del tiempo, distribución de moods por juego o género, racha de sesiones consecutivas.
- **Perfiles gaming avanzados:** configuración del estilo de juego preferido, géneros favoritos, horarios habituales, y cómo estos factores correlacionan con el estado emocional.
- **Más estados de ánimo:** ampliar los tres estados actuales (happy/neutral/sad) con matices como `excited`, `angry`, `anxious`, `focused`.
- **Funcionalidades sociales:** comparativa anónima con otros usuarios, grupos de amigos, sesiones compartidas.

### 10.2 Motor de Recomendaciones

- **Recomendaciones dirigidas por base de datos:** activar completamente las tablas `prompts_ia` y `runbooks` para que los prompts y la lógica de recomendación sean configurables sin cambios de código.
- **Aprendizaje automático (ML):** entrenar un modelo propio sobre el historial acumulado de sesiones y feedback para personalizar las recomendaciones según los patrones históricos del usuario.
- **Recomendaciones de juegos reales:** integrar con APIs de catálogos como RAWG o IGDB para sugerir títulos concretos basados en el mood y el historial del usuario.
- **Reintentos inteligentes:** si el usuario rechaza una recomendación repetidamente, ajustar el perfil de recomendación para ese contexto emocional.

### 10.3 Seguridad y Robustez

- **Rotación de refresh tokens:** invalidar el refresh token tras su uso y emitir uno nuevo (patrón Token Rotation).
- **Verificación de email:** flujo de verificación por correo tras el registro.
- **Recuperación de contraseña:** endpoint de reset mediante enlace por email.
- **Rate limiting:** proteger los endpoints públicos de `/auth` contra fuerza bruta.

### 10.4 Despliegue e Infraestructura

- **Docker Compose completo:** tal como se detalla en la sección [9.8](#98-integración-futura-con-docker-compose), integrar todos los servicios en un único `docker-compose.yml`.
- **Contenerización del backend:** crear un `Dockerfile` para el JAR de Spring Boot, permitiendo despliegues consistentes en cualquier entorno cloud.
- **Despliegue cloud:** considerar plataformas como Railway, Render o AWS para el backend, y Vercel o Netlify para el frontend estático.
- **CI/CD:** pipelines de integración continua con GitHub Actions para ejecutar tests automáticamente en cada push y generar builds de producción verificadas.

### 10.5 Observabilidad

- **Dashboard de administración:** visualización de métricas de uso, tasa de éxito de OpenAI vs reglas, distribución de moods registrados.
- **Logs estructurados:** implementar logging en JSON para facilitar la ingesta en sistemas de monitorización.
- **Registro activo del flujo de estados:** activar la escritura en `transiciones_flujo` para cada cambio de estado de sesión, aprovechando la infraestructura ya diseñada en base de datos.

---

## 11. Conclusiones Técnicas

### 11.1 Valoración del Resultado

GamerMood es un proyecto full-stack funcional que integra correctamente cuatro capas tecnológicas: interfaz de usuario Angular, API REST Spring Boot, base de datos PostgreSQL y servicio externo de IA (OpenAI). La separación de responsabilidades es clara y el flujo principal de la aplicación —registro, sesión, recomendación, feedback, historial— funciona de extremo a extremo.

La decisión de implementar un **sistema de fallback por reglas** resulta especialmente acertada desde el punto de vista de la resiliencia: la aplicación sigue siendo funcional aunque la API de OpenAI no esté disponible o no esté configurada, lo que la hace robusta en entornos de desarrollo sin clave de API.

### 11.2 Decisiones Técnicas Destacadas

| Decisión | Justificación |
|----------|--------------|
| Angular 21 con componentes standalone | Reducción de boilerplate y mejor alineación con la dirección futura del framework |
| Spring Boot 3.5 con Java 17 | LTS con soporte completo, records de Java para DTOs inmutables |
| JWT stateless | Escalabilidad horizontal sin estado compartido entre instancias del servidor |
| Vitest en lugar de Jasmine/Karma | Mayor rendimiento en ejecución de tests, mejor integración con el toolchain moderno |
| OpenAI sin SDK propio | Evita dependencias frágiles de SDKs en evolución rápida |
| Fallback por reglas | Garantía de respuesta al usuario independientemente de la disponibilidad de la API |
| Esquema SQL explícito (`01_init.sql`) | Control total del esquema, reproducibilidad y claridad para revisión académica |

### 11.3 Dificultades Encontradas

- **Integración frontend-backend:** la gestión de CORS y la correcta propagación del JWT requirieron ajustes en la configuración de Spring Security y en el interceptor de Angular.
- **Parsing de respuestas de OpenAI:** se optó por parsing manual de JSON para evitar dependencias adicionales, lo que implica fragilidad si el formato de respuesta de la API cambia.
- **Merge conflicts durante la integración de ramas:** el desarrollo en paralelo de frontend y backend generó conflictos en varios componentes que requirieron resolución cuidadosa para preservar funcionalidades de ambas ramas.
- **Configuración del entorno local:** la ausencia de Docker Compose unificado obligó a documentar un proceso de arranque en varios pasos que puede resultar complejo para nuevos colaboradores.

### 11.4 Escalabilidad Futura

La arquitectura actual sienta bases sólidas para escalar el proyecto:
- El backend stateless puede escalar horizontalmente sin cambios.
- La base de datos tiene índices diseñados para las consultas principales.
- Las tablas `prompts_ia`, `runbooks` y `categorias_recomendacion` permiten evolucionar el motor de recomendaciones sin modificar el esquema.
- El sistema de estados de flujo permite añadir análisis de comportamiento sin alterar la lógica existente.

GamerMood demuestra que es posible construir una aplicación web completa, con integración de IA y buenas prácticas de seguridad, en el marco de un Trabajo de Fin de Grado, manteniendo al mismo tiempo una arquitectura lo suficientemente limpia para servir de base a un producto real.

---

*Documentación generada a partir del código fuente de la rama `develop` — GamerMood TFG, Mayo 2026.*
