# Reparto inicial de responsabilidades

## Persona 1 - Backend y seguridad
Responsable de:
- Spring Boot
- estructura base de la API REST
- autenticación y autorización
- JWT
- validaciones
- controladores y servicios
- manejo de errores
- configuración segura y variables de entorno
- soporte a integración OpenAI

## Persona 2 - Base de datos y lógica de dominio
Responsable de:
- modelo relacional PostgreSQL
- entidades JPA/Hibernate
- relaciones entre entidades
- repositorios
- máquina de estados
- feedback
- trazabilidad del flujo

## Persona 3 - Frontend e integración
Responsable de:
- Angular
- formularios
- rutas
- guards
- servicios HTTP
- login en cliente
- formulario de sesión
- historial
- pantalla de recomendaciones conectada a API real

## Criterio de trabajo
Cada integrante trabaja sobre ramas feature.
La integración se realiza en develop.
La rama main solo contendrá versiones estables.