# GamerMood

GamerMood es una aplicación web desarrollada como proyecto de TFG para registrar sesiones de juego y relacionarlas con el estado de ánimo del usuario.

La idea es bastante simple: después de jugar, el usuario guarda el juego, cómo se ha sentido, la intensidad de la sesión y una breve experiencia. Con esa información, el sistema genera una recomendación para la siguiente partida. No es una herramienta médica ni intenta diagnosticar nada; está pensada como una pequeña ayuda para reflexionar sobre hábitos de juego.

El proyecto está separado en tres partes:

- `frontend`: aplicación Angular.
- `backend`: API REST con Spring Boot.
- `database`: script SQL inicial para PostgreSQL.

La autenticación se hace con JWT, la base de datos es PostgreSQL y el entorno local usa Docker Compose para levantar la base de datos.

## Tecnologías

| Parte | Tecnologías |
| --- | --- |
| Frontend | Angular 21, TypeScript 5.9, RxJS |
| Backend | Java 17, Spring Boot 3.5.13 |
| Seguridad | Spring Security, JWT, JJWT 0.12.6 |
| Persistencia | PostgreSQL 17, Spring Data JPA, Hibernate |
| Entorno local | Docker Compose |
| Pruebas | Vitest en frontend, Maven Wrapper en backend |

## Requisitos

Para ejecutar el proyecto en local hace falta:

- Java JDK 17
- Node.js 22 o una versión compatible con Angular 21
- npm 11
- Docker Desktop o Docker Engine con Docker Compose

Comprobaciones rápidas:

```bash
java -version
node -v
npm -v
docker --version
docker compose version
```

No hace falta instalar Maven manualmente. El backend incluye `mvnw` y `mvnw.cmd`.

## Variables de entorno

El repositorio incluye `.env.example`. Para trabajar en local, se puede crear un `.env` a partir de esa plantilla.

En Windows:

```powershell
Copy-Item .env.example .env
```

En Linux o macOS:

```bash
cp .env.example .env
```

Variables usadas:

| Variable | Uso |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | Perfil de Spring. En local se usa `dev`. |
| `SERVER_PORT` | Puerto del backend. Por defecto, `8081`. |
| `DB_HOST` | Host de PostgreSQL. En local, `localhost`. |
| `DB_PORT` | Puerto de PostgreSQL. Por defecto, `5432`. |
| `DB_NAME` | Nombre de la base de datos. |
| `DB_USER` | Usuario de PostgreSQL. |
| `DB_PASSWORD` | Contraseña de PostgreSQL. |
| `JWT_SECRET` | Clave para firmar los JWT. Debe ser larga y privada. |
| `CORS_ALLOWED_ORIGINS` | Origen permitido para el frontend. En local, `http://localhost:4200`. |
| `GROQ_API_KEY` | Clave opcional para generar recomendaciones con Groq. |
| `GROQ_API_URL` | Endpoint de Groq compatible con Chat Completions. |
| `GROQ_MODEL` | Modelo usado para generar recomendaciones. |
| `GROQ_MAX_TOKENS` | Límite de tokens de la respuesta. |

No se debe subir nunca el `.env` real al repositorio.

El backend carga automáticamente el `.env` de la raíz del proyecto o de la carpeta `backend/`, así que normalmente no hace falta exportar variables manualmente.

## Puertos

| Servicio | Puerto | URL |
| --- | --- | --- |
| Frontend | 4200 | `http://localhost:4200` |
| Backend | 8081 | `http://localhost:8081/api` |
| PostgreSQL | 5432 | `localhost:5432` |

El backend tiene configurado el prefijo `/api`, por eso los endpoints empiezan por `http://localhost:8081/api`.

## Arranque en local

### 1. Levantar PostgreSQL

Desde la raíz del proyecto:

```bash
docker compose up -d
```

Comprobar que el contenedor está funcionando:

```bash
docker compose ps
```

PostgreSQL usa la imagen `postgres:17`. El esquema inicial está en `database/schema/01_init.sql`.

Hay que tener en cuenta una cosa importante: los scripts de inicialización de PostgreSQL solo se ejecutan la primera vez que se crea el volumen. Si se cambia el SQL y ya existía un volumen anterior, lo más limpio es recrearlo:

```bash
docker compose down -v
docker compose up -d
```

### 2. Arrancar el backend

En Windows:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

En Linux o macOS:

```bash
cd backend
./mvnw spring-boot:run
```

Comprobación rápida:

```text
http://localhost:8081/api/health
```

En el perfil `dev`, Hibernate usa `ddl-auto=validate`. Esto hace que el backend valide el esquema real de PostgreSQL al arrancar, sin modificar tablas automáticamente.

### 3. Arrancar el frontend

```bash
cd frontend
npm install
npm start
```

La aplicación queda disponible en:

```text
http://localhost:4200
```

## Recomendaciones e IA

El sistema de recomendaciones funciona de dos formas:

1. Si `GROQ_API_KEY` está configurada, el backend llama a Groq desde `GroqService`.
2. Si no hay clave o Groq devuelve error, el backend usa recomendaciones internas por reglas.

La fuente queda guardada en base de datos como:

- `GROQ`, si la recomendación viene de Groq.
- `REGLAS`, si se ha usado el fallback interno.

El endpoint configurado por defecto es:

```text
https://api.groq.com/openai/v1/chat/completions
```

Aunque la URL contiene `openai`, el proveedor usado en el proyecto final es Groq. Esa ruta existe porque Groq ofrece compatibilidad con el formato de Chat Completions.

OpenAI no forma parte del estado final del proyecto.

## Seguridad

El login devuelve un token JWT y un refresh token. El frontend guarda el token y lo envía en las peticiones privadas mediante la cabecera:

```text
Authorization: Bearer <token>
```

En backend, `JwtAuthenticationFilter` valida el token y Spring Security protege los endpoints privados.

Aunque ahora solo se usa `ROLE_USER`, existen la entidad `Role` y la tabla `usuarios_roles`. Se mantienen porque encajan con Spring Security y dejan el sistema preparado para añadir más roles sin cambiar el modelo principal de usuarios.

## Endpoints principales

| Método | Endpoint | Uso |
| --- | --- | --- |
| `GET` | `/api/health` | Comprueba que el backend responde. |
| `POST` | `/api/auth/register` | Registra un usuario. |
| `POST` | `/api/auth/login` | Inicia sesión. |
| `POST` | `/api/auth/refresh` | Renueva el token de acceso. |
| `POST` | `/api/sessions` | Crea una sesión de juego. |
| `GET` | `/api/sessions` | Lista las sesiones del usuario autenticado. |
| `GET` | `/api/sessions/{id}` | Consulta una sesión propia. |
| `DELETE` | `/api/sessions/{id}` | Elimina una sesión propia. |
| `POST` | `/api/recommendations/{sesionId}` | Obtiene o genera una recomendación. |
| `POST` | `/api/recommendations/{sesionId}/retry` | Regenera una recomendación. |
| `POST` | `/api/feedback/{recomendacionId}` | Guarda feedback sobre una recomendación. |

## Estructura del proyecto

```text
gamermood-tfg/
├── backend/
│   └── src/main/java/com/gamermood/backend/
│       ├── config/
│       ├── controller/
│       ├── dto/
│       ├── entity/
│       ├── exception/
│       ├── repository/
│       ├── security/
│       └── service/
├── database/
│   └── schema/
├── docs/
│   └── actas/
├── frontend/
│   └── src/app/
│       ├── components/
│       ├── guards/
│       ├── interceptors/
│       ├── models/
│       └── services/
├── docker-compose.yml
├── .env.example
└── README.md
```

## Comandos útiles

Backend:

```powershell
cd backend
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

Frontend:

```bash
cd frontend
npm test
npm run build
npm start
```

Docker:

```bash
docker compose up -d
docker compose ps
docker compose logs postgres
docker compose down
```

## Problemas habituales

Si PostgreSQL no arranca, normalmente Docker no está iniciado o el puerto `5432` ya está ocupado por otra instalación local de PostgreSQL.

Si el backend falla al arrancar con errores de Hibernate, suele significar que el esquema real de la base de datos no coincide con `database/schema/01_init.sql`. Para empezar desde cero:

```bash
docker compose down -v
docker compose up -d
```

Si el frontend devuelve 401 o 403, conviene cerrar sesión y volver a iniciar sesión. También hay que comprobar que el backend está usando el mismo `JWT_SECRET`.

Si Groq no genera recomendaciones, revisar que `GROQ_API_KEY` esté configurada en `.env` y reiniciar el backend. Si la clave está vacía, el sistema seguirá funcionando con `REGLAS`.

## Pruebas

Comandos usados durante la revisión final:

```powershell
cd backend
.\mvnw.cmd test
```

```bash
cd frontend
npm test
npm run build
```

Además se probaron manualmente registro, login, creación de sesión, generación de recomendación, regeneración, feedback y eliminación de sesiones.
