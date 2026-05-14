# GamerMood 2.0

Aplicación web full-stack para registrar sesiones de juego, analizar el estado emocional del usuario y generar recomendaciones personalizadas mediante reglas y OpenAI.

Proyecto de Trabajo de Fin de Grado (TFG) del ciclo formativo de Desarrollo de Aplicaciones Multiplataforma (DAM).

---

## Tecnologías

| Capa | Tecnología | Versión |
|------|-----------|---------|
| Frontend | Angular | 21 |
| Backend | Spring Boot | 3.5.13 |
| Lenguaje backend | Java | 17 |
| Build backend | Maven | 3.9+ |
| Base de datos | PostgreSQL | 17 |
| Contenedores | Docker + Docker Compose | — |
| IA | OpenAI API | gpt-4o-mini |

---

## Requisitos previos

Instalar las siguientes herramientas antes de levantar el proyecto:

| Herramienta | Versión mínima | Descarga |
|-------------|---------------|----------|
| Java JDK | 17 | https://adoptium.net |
| Maven | 3.9 | https://maven.apache.org/download.cgi |
| Node.js | 22 LTS | https://nodejs.org |
| npm | 11 | incluido con Node.js |
| Angular CLI | 21 | `npm install -g @angular/cli` |
| Docker Desktop | Cualquiera reciente | https://www.docker.com/products/docker-desktop |

Verificar instalaciones:

```bash
java -version
mvn -version
node -v
npm -v
ng version
docker -v
docker compose version
```

---

## Estructura del proyecto

```
gamermood-tfg/
├── backend/                  # API REST Spring Boot
│   ├── src/
│   │   └── main/
│   │       ├── java/com/gamermood/backend/
│   │       │   ├── controller/
│   │       │   ├── service/
│   │       │   ├── model/
│   │       │   ├── repository/
│   │       │   └── security/
│   │       └── resources/
│   │           ├── application.yaml
│   │           ├── application-dev.yaml
│   │           └── application-prod.yaml
│   └── pom.xml
│
├── frontend/                 # SPA Angular
│   ├── src/
│   │   └── app/
│   │       ├── components/
│   │       ├── services/
│   │       ├── models/
│   │       └── interceptors/
│   ├── package.json
│   └── angular.json
│
├── database/
│   └── schema/
│       └── 01_init.sql       # DDL inicial de la base de datos
│
├── docs/                     # Documentación técnica
├── docker-compose.yml        # Levanta PostgreSQL
├── .env.example              # Plantilla de variables de entorno
└── README.md
```

---

## Puertos

| Servicio | Puerto | URL |
|----------|--------|-----|
| Frontend Angular | 4200 | http://localhost:4200 |
| Backend Spring Boot | 8081 | http://localhost:8081/api |
| PostgreSQL | 5432 | localhost:5432 |

> El backend expone todos sus endpoints bajo el prefijo `/api`.

---

## Configuración de variables de entorno

Copiar el fichero de ejemplo y ajustar los valores:

```bash
cp .env.example .env
```

Editar `.env` con los valores reales para el entorno local. Como mínimo, proporcionar una clave válida de OpenAI si se quieren usar las recomendaciones de IA.

Ver [.env.example](.env.example) para la lista completa de variables.

> **El fichero `.env` nunca debe subirse al repositorio.** Ya está incluido en `.gitignore`.

---

## Arranque del proyecto

### Paso 1 — Levantar PostgreSQL con Docker

```bash
docker compose up -d
```

Verificar que el contenedor arrancó correctamente:

```bash
docker compose ps
docker compose logs postgres
```

La base de datos `gamermood` se crea automáticamente en el primer arranque.

### Paso 2 — Aplicar el esquema de base de datos

La primera vez (o tras un `docker compose down -v`), ejecutar el script de inicialización:

**Opción A — psql en terminal:**
```bash
docker exec -i postgres-2DAM-V psql -U postgres -d gamermood < database/schema/01_init.sql
```

**Opción B — desde DBeaver u otro cliente:**
Conectar a `localhost:5432` con usuario `postgres` / contraseña `postgres` y ejecutar `database/schema/01_init.sql` sobre la base de datos `gamermood`.

### Paso 3 — Arrancar el backend

```bash
cd backend
mvn spring-boot:run
```

Con variables de entorno desde el fichero `.env` (PowerShell):

```powershell
Get-Content ..\.env | ForEach-Object {
  if ($_ -match '^([^#=]+)=(.+)$') { $env:($matches[1]) = $matches[2] }
}
cd backend
mvn spring-boot:run
```

O bien en Linux/macOS:

```bash
export $(grep -v '^#' .env | xargs)
cd backend
mvn spring-boot:run
```

Verificar que arrancó:

```
http://localhost:8081/api/health
```

### Paso 4 — Arrancar el frontend

```bash
cd frontend
npm install
npm start
```

La aplicación estará disponible en http://localhost:4200.

---

## Flujo completo de arranque (resumen)

```bash
# 1. Base de datos
docker compose up -d

# 2. Backend (en una terminal separada)
cd backend
mvn spring-boot:run

# 3. Frontend (en otra terminal)
cd frontend
npm install   # solo la primera vez o tras cambios en package.json
npm start
```

---

## Equipo

| Rol | Responsabilidad |
|-----|----------------|
| Integrante 1 | Backend y seguridad (Spring Boot, JWT) |
| Integrante 2 | Base de datos y lógica de dominio |
| Integrante 3 | Frontend e integración (Angular) |

---

## Flujo de ramas Git

| Rama | Propósito |
|------|-----------|
| `main` | Versión estable |
| `develop` | Integración continua |
| `feature/*` | Desarrollo de funcionalidades |

---

## Troubleshooting

### Docker no arranca

- Asegurarse de que Docker Desktop está en ejecución.
- Verificar que el puerto 5432 no está ocupado:
  ```bash
  # Windows
  netstat -ano | findstr :5432
  # Linux/macOS
  lsof -i :5432
  ```
- Si el puerto está ocupado, detener el servicio PostgreSQL local o cambiar el puerto en `docker-compose.yml`.

### El backend no conecta con la base de datos

- Comprobar que el contenedor de PostgreSQL está corriendo: `docker compose ps`
- Verificar que las credenciales en `.env` coinciden con las del `docker-compose.yml`.
- Revisar el log del backend para ver el mensaje de error de conexión exacto.

### El backend arranca pero devuelve errores de esquema o faltan datos

- En perfil `dev` (por defecto) Hibernate usa `ddl-auto: update`: crea/actualiza las tablas automáticamente, pero **no inserta los datos de referencia** (roles, categorías, estados).
- Ejecutar `01_init.sql` tal y como se describe en el Paso 2 para sembrar esos datos iniciales.
- Si el script falla por claves duplicadas, los datos ya están presentes y puedes ignorar el error.

### npm install falla

- Comprobar la versión de Node.js: `node -v` (mínimo v22 LTS).
- Borrar `node_modules` y `package-lock.json` y volver a instalar:
  ```bash
  rm -rf node_modules package-lock.json
  npm install
  ```

### El frontend no puede conectar con el backend (CORS)

- Comprobar que el backend está corriendo en el puerto 8081.
- El backend tiene CORS configurado para `http://localhost:4200`. Si el frontend corre en otro puerto, ajustar la variable `CORS_ALLOWED_ORIGINS` en `.env`.

### Error `Port 4200 is already in use`

```bash
ng serve --port 4201
```

### El JWT no funciona o expira inesperadamente

- Comprobar que `JWT_SECRET` en `.env` tiene al menos 64 caracteres.
- El token de acceso expira en 24 h y el de refresco en 7 días (configurado en `application.yaml`).
