---
name: pocketdex-architecture
description: "Diseñar o implementar módulos, contratos de dominio, DI y configuración Gradle de PocketDex; analizar logs entregados por el desarrollador sin ejecutar builds."
---

# Arquitectura Android de PocketDex

## Propósito y entradas

Aplicar la constitución a una spec aprobada, una refactorización acotada o un error
de configuración aportado por el desarrollador. Leer `AGENTS.md`, `constitution.md`,
`settings.gradle.kts`, el catálogo, wrapper y Gradle de los módulos afectados desde
la raíz. No asumir que un módulo o biblioteca ya existe.

## Proceso

- Separar entrada/composición en `:app`, reglas/contratos en `:core:domain`,
  infraestructura en `:core:data` y componentes visuales en `:core:designsystem`.
  Home, detail, compare, quiz y favorites solo dependen de dominio y sistema visual.
- Mantener dominio Kotlin/JVM sin Android, Hilt, Room, Retrofit ni Paging. Admitir
  Coroutines/Flow. Implementar repositorios en data y proveer casos de uso desde
  DI externa al dominio. No crear casos de uso que solo añadan envoltorios vacíos.
- Definir navegación en app mediante identificadores, claves tipadas y callbacks.
  No introducir dependencias entre features ni exponer DTOs/entidades a presentación.
- Ubicar adaptadores `PagingSource`/`Pager` en presentación y recuperación de páginas
  en contratos de dominio. No importar DAOs ni APIs remotas desde features.
- Verificar versiones declaradas y APIs oficiales antes de añadir plugins. AGP 9
  integra Kotlin en módulos Android; conservar esa configuración. Usar Kotlin JVM
  en dominio y KSP para Hilt/Room. Distinguir toolchain Gradle y objetivo JVM.
- Centralizar nuevas versiones en el catálogo. No añadir build-logic, módulos
  adicionales, frameworks o upgrades sin que el alcance y el beneficio lo justifiquen.
- Para logs, localizar el primer error causal, contrastar configuración y proponer
  un cambio mínimo. No atribuir el fallo a red, versiones o entorno sin evidencia.

## Salidas y verificación

Entregar contratos/módulos afectados, justificación, cambios del alcance autorizado
y comandos manuales por módulo. Revisar imports, dependencias, aliases y ausencia
de ciclos. La revisión estática no prueba compilación ni compatibilidad binaria.

Nunca ejecutar Gradle para compilar, empaquetar, instalar, correr la app o pruebas.
Analizar resultados proporcionados sin repetirlos. Consultar al docs-researcher
cuando haya incertidumbre real sobre APIs, mediante delegación secuencial.
