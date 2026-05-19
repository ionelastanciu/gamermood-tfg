# GamerMood

GamerMood es una aplicación web desarrollada como proyecto de TFG para registrar sesiones de juego y relacionarlas con el estado de ánimo del usuario. La idea es sencilla: después de jugar, el usuario guarda cómo se ha sentido, la intensidad de la sesión y una breve experiencia; a partir de esos datos, el sistema genera una recomendación para la siguiente partida.

El proyecto está dividido en frontend, backend y base de datos. La autenticación se hace con JWT y la persistencia con PostgreSQL.

## Tecnologías usadas

| Parte | Tecnología |
| --- | --- |
| Frontend | Angular 21, TypeScript 5.9, RxJS |
| Backend | Java 17, Spring Boot 3.5.13, Spring Security, Spring Data JPA |
| Seguridad | JWT con JJWT 0.12.6 |
| Base de datos | PostgreSQL 17 |
| Entorno local | Docker Compose |
| Tests frontend | Vitest / Angular test builder |
| Build backend | Maven Wrapper |

## Requisitos

Para levantar el proyecto en local hace falta tener instalado:

- Java JDK 17
- Node.js 22 o compatible con Angular 21
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

No es necesario instalar Maven manualmente porque el backend incluye `mvnw` y `mvnw.cmd`.

## Variables de entorno

El repositorio incluye `.env.example`. Para trabajar en local se puede crear el archivo `.env` desde esa plantilla:

```powershell
Copy-Item .env.example .env
```

En Linux o macOS:

```bash
cp .env.example .env
```

Variables usadas:

| Variable | Descripción |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | Perfil de Spring. En local se usa `dev`. |
| `SERVER_PORT` | Puerto del backend. Por defecto `8081`. |
| `DB_HOST` | Host de PostgreSQL. En local, `localhost`. |
| `DB_PORT` | Puerto publicado de PostgreSQL. Por defecto `5432`. |
| `DB_NAME` | Nombre de la base de datos. |
| `DB_USER` | Usuario de PostgreSQL. |
| `DB_PASSWORD` | Contraseña de PostgreSQL. |
| `JWT_SECRET` | Clave usada para firmar los tokens JWT. Debe ser larga y privada. |
| `CORS_ALLOWED_ORIGINS` | Origen permitido para el frontend. En local, `http://localhost:4200`. |
| `GROQ_API_KEY` | Opcional. Si se configura, el backend intenta generar recomendaciones con Groq. |
| `GROQ_API_URL` | Endpoint de Groq compatible con Chat Completions. |
| `GROQ_MODEL` | Modelo utilizado para generar recomendaciones. |
| `GROQ_MAX_TOKENS` | Límite de tokens de la respuesta. |

No se deben subir secretos reales al repositorio.

El backend carga automáticamente el `.env` de la raíz del proyecto o de la carpeta `backend/`, por lo que no hace falta exportar las variables manualmente si se arranca con el Maven Wrapper.

## Puertos

| Servicio | Puerto | URL |
| --- | --- | --- |
| Frontend | 4200 | `http://localhost:4200` |
| Backend | 8081 | `http://localhost:8081/api` |
| PostgreSQL | 5432 | `localhost:5432` |

El backend tiene configurado el context path `/api`, así que todos los endpoints empiezan por `http://localhost:8081/api`.

## Arranque del proyecto

### 1. Levantar PostgreSQL

Desde la raíz del repositorio:

```bash
docker compose up -d
```

Comprobar el estado:

```bash
docker compose ps
```

El servicio de PostgreSQL usa la imagen `postgres:17` y monta el script `database/schema/01_init.sql` para crear el esquema inicial.

Importante: PostgreSQL solo ejecuta los scripts de inicialización la primera vez que se crea el volumen. Si ya existía un volumen anterior y se quiere recrear la base limpia:

```bash
docker compose down -v
docker compose up -d
```

También se puede aplicar el script manualmente si el contenedor ya existe:

```powershell
Get-Content database/schema/01_init.sql | docker exec -i postgres-2DAM-V psql -U postgres -d gamermood
```

### 2. Arrancar el backend

Desde la carpeta `backend`:

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

En local el perfil `dev` usa `spring.jpa.hibernate.ddl-auto=validate`, por lo que Hibernate valida que las entidades coincidan con el esquema de PostgreSQL al arrancar.

### 3. Arrancar el frontend

Desde la carpeta `frontend`:

```bash
cd frontend
npm install
npm start
```

La aplicación queda disponible en:

```text
http://localhost:4200
```

## Endpoints principales

| Método | Endpoint | Uso |
| --- | --- | --- |
| `GET` | `/api/health` | Comprobar que el backend responde. |
| `POST` | `/api/auth/register` | Registrar un usuario. |
| `POST` | `/api/auth/login` | Iniciar sesión y recibir tokens JWT. |
| `POST` | `/api/auth/refresh` | Renovar el token de acceso. |
| `POST` | `/api/sessions` | Crear una sesión de juego. |
| `GET` | `/api/sessions` | Listar sesiones del usuario autenticado. |
| `GET` | `/api/sessions/{id}` | Consultar una sesión propia. |
| `DELETE` | `/api/sessions/{id}` | Eliminar una sesión propia. |
| `POST` | `/api/recommendations/{sesionId}` | Obtener o generar recomendación. |
| `POST` | `/api/recommendations/{sesionId}/retry` | Regenerar recomendación. |
| `POST` | `/api/feedback/{recomendacionId}` | Guardar feedback sobre una recomendación. |

Los endpoints privados necesitan la cabecera:

```text
Authorization: Bearer <token>
```

## Sistema de recomendaciones

El backend genera recomendaciones de dos formas:

1. Si `GROQ_API_KEY` está configurada, llama a Groq mediante el endpoint `https://api.groq.com/openai/v1/chat/completions`.
2. Si la clave está vacía o Groq devuelve error, usa el sistema interno de reglas.

La palabra `openai` aparece en la URL porque Groq ofrece un endpoint compatible con Chat Completions. El proveedor usado por el proyecto es Groq.

El modelo configurado por defecto es `llama-3.3-70b-versatile`, aunque se puede cambiar con `GROQ_MODEL`.

Flujo real:

1. El usuario crea una sesión indicando juego, estado de ánimo, intensidad y experiencia.
2. El frontend solicita la recomendación al backend.
3. El backend intenta generar el texto con `GroqService`.
4. Si Groq responde bien, la recomendación se guarda con fuente `GROQ`.
5. Si no hay clave o falla la llamada externa, se guarda una recomendación con fuente `REGLAS`.

Así el proyecto puede funcionar sin servicios externos, pero cualquier integrante puede activar la IA añadiendo su propia clave de Groq en `.env`.

## Roles y seguridad

Aunque en la aplicación solo se usa `ROLE_USER`, el backend mantiene la entidad `Role` y la tabla `usuarios_roles`. Esto deja preparada la seguridad para trabajar con más permisos sin cambiar el modelo de usuarios.

Cuando un usuario se registra, `AuthServiceImpl` le asigna `ROLE_USER`. Después, `UserDetailsServiceImpl` carga esos roles y Spring Security los convierte en authorities. Al iniciar sesión, los roles también se incluyen en el JWT, de forma que el frontend recibe la información básica del usuario autenticado.

Mantener la relación `User` - `Role` como `ManyToMany` tiene sentido porque es una estructura habitual en seguridad: un usuario puede tener varios roles y un mismo rol puede pertenecer a muchos usuarios. En esta versión solo se usa el rol de usuario normal, pero el diseño no obliga a rehacer la autenticación si más adelante se añade un rol administrador.

## Estructura del repositorio

```text
gamermood-tfg/
├── backend/
│   ├── src/main/java/com/gamermood/backend/
│   │   ├── config/          # Configuración general y CORS
│   │   ├── controller/      # Controladores REST
│   │   ├── dto/             # Objetos de entrada y salida de la API
│   │   ├── entity/          # Entidades JPA
│   │   ├── exception/       # Excepciones y manejador global
│   │   ├── repository/      # Repositorios Spring Data
│   │   ├── security/        # JWT, filtros y configuración de seguridad
│   │   └── service/         # Lógica de aplicación
│   └── src/main/resources/  # application.yaml y perfiles
├── database/
│   └── schema/              # Script SQL inicial
├── docs/
│   ├── actas/               # Actas y documentación interna del equipo
│   └── estructura-memoria-tfg.md
├── frontend/
│   ├── public/
│   └── src/app/
│       ├── components/      # Pantallas de la aplicación
│       ├── guards/          # Protección de rutas privadas
│       ├── interceptors/    # Interceptor para añadir JWT
│       ├── models/          # Interfaces TypeScript
│       └── services/        # Servicios de API y autenticación
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

## Troubleshooting

Si PostgreSQL no arranca, suele ser porque Docker no está iniciado o porque el puerto `5432` ya lo está usando otra instalación local de PostgreSQL. En ese caso se puede cambiar `DB_PORT` en `.env`.

Si el backend falla al arrancar con errores de validación de Hibernate, normalmente el esquema real de la base no coincide con `database/schema/01_init.sql`. Para un entorno limpio, lo más directo es recrear el volumen:

```bash
docker compose down -v
docker compose up -d
```

Si el frontend devuelve errores 401 o 403, conviene cerrar sesión y volver a entrar para renovar los tokens. También hay que comprobar que `JWT_SECRET` sea el mismo mientras el backend está en ejecución.

Si el frontend no conecta con la API, revisar que el backend esté levantado en `8081` y que `CORS_ALLOWED_ORIGINS` incluya `http://localhost:4200`.

Si `npm install` falla, revisar la versión de Node. El proyecto se ha trabajado con Node 22 y npm 11.

## Estado de pruebas

Comandos usados para comprobar el proyecto:

```powershell
cd backend
.\mvnw.cmd test
```

```bash
cd frontend
npm test
```

También se puede lanzar el build del frontend con:

```bash
npm run build
```
