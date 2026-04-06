# Normas de trabajo del repositorio

## Ramas
- main: versión estable
- develop: rama de integración
- feature/*: ramas de desarrollo por funcionalidad

## Reglas
- No trabajar directamente en main
- No subir secretos al repositorio
- No guardar claves reales en archivos versionados
- Cada funcionalidad debe desarrollarse en una rama feature
- Antes de integrar en develop, revisar que compila y funciona
- Los cambios deben ir acompañados de commits claros y descriptivos

## Convención de commits
- chore: tareas de mantenimiento o estructura
- feat: nuevas funcionalidades
- fix: correcciones
- docs: documentación
- refactor: reestructuración sin cambio funcional

## Seguridad
- El archivo .env no se sube nunca
- El archivo .env.example solo contiene placeholders
- JWT secret, credenciales de BD y API keys se gestionan mediante variables de entorno