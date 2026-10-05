---
name: pocketdex-review
description: "Revisar estáticamente cambios Android/Kotlin de PocketDex contra specs y arquitectura, identificando defectos con evidencia y verificación manual pendiente."
---

# Revisión Android de PocketDex

## Propósito y entradas

Revisar una modificación, funcionalidad o error con alcance concreto. Leer
`AGENTS.md`, `constitution.md`, spec y archivos afectados con sus callers.
Inspeccionar diffs si existe Git; de lo contrario revisar archivos directamente.

## Proceso

1. Contrastar requisitos y estado real antes de aplicar una checklist. No reportar
   arquitectura objetivo aún no implementada como defecto ajeno al alcance.
2. Revisar dependencias/imports y fugas de DTOs, DAOs, contexto o infraestructura;
   respetar dominio puro y features independientes.
3. Buscar fallos de cancelación, scopes, respuestas fuera de orden, carga duplicada,
   mutaciones de estado publicado, recolección/lifecycle y restauración prometida.
4. Revisar filtros AND, paginación, favoritos consistentes, almacenamiento offline,
   comparador sin duplicados y respuesta/puntuación del quiz según la spec vigente.
5. Revisar navegación, accesibilidad, recursos, permisos/componentes exportados y
   logs únicamente cuando afecten el cambio. No tratar toda Activity exportada como
   vulnerabilidad: evaluar su función y validación de entradas.
6. Revisar pruebas por su capacidad de detectar regresiones. Distinguir escenarios
   ausentes de escenarios escritos pero todavía sin ejecutar.

No considerar automáticamente incorrectos un lambda inline, un init de ViewModel
o la ausencia de withContext(IO). Sustentar hallazgos en las APIs/versiones y el
comportamiento concreto; consultar fuentes oficiales cuando haya incertidumbre.

## Salidas y verificación

Por hallazgo: severidad, archivo/línea, escenario disparador, evidencia,
consecuencia y corrección sugerida. Separar defectos confirmados, preguntas y
verificación manual pendiente. Si no hay hallazgos, decirlo con el alcance revisado;
no convertirlo en garantía de funcionamiento.

La skill de revisión no modifica archivos por sí sola. Nunca ejecutar builds,
pruebas, app, screenshots o cobertura. Preparar comandos/pasos manuales relevantes
y analizar resultados aportados sin repetir ejecuciones. Delegar revisión al
android-reviewer solo con el protocolo secuencial de AGENTS.md.
