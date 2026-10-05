---
name: pocketdex-spec
description: "Explorar el contexto de PocketDex y redactar o actualizar specs de funcionalidades con requisitos del PDF, decisiones mobile y criterios de aceptación antes de implementar comportamiento."
---

# Contexto y especificaciones de PocketDex

## Propósito y entradas

Convertir una solicitud de funcionalidad en una spec implementable. Recibir el
objetivo, restricciones del usuario y, cuando exista, `specs/<feature>/SPEC.md`.
Para cambios solo documentales o correcciones ya especificadas, ajustar el proceso
al alcance sin crear una especificación artificial.

## Proceso

1. Localizar la raíz por `AGENTS.md` y `constitution.md`. Leer ambos, las guías de
   `docs` y la sección relevante del PDF. Inspeccionar implementación y configuración
   con búsquedas dirigidas; distinguir código presente de arquitectura objetivo.
2. Identificar requisitos verificados, decisiones acordadas y preguntas pendientes.
   Consultar al usuario solo por comportamientos no resolubles mediante documentos
   o código. No decidir por iniciativa propia catálogo, búsqueda parcial/número,
   tipos múltiples, restauración del quiz, empates ni retención offline de imágenes.
3. Aplicar los aspectos pertinentes de `docs/MOBILE_GUIDELINES.md`: estado temporal
   y persistente, proceso/recreación, operaciones en curso, navegación, conectividad,
   recuperación, accesibilidad, tamaños/insets, recursos y permisos si aplican.
4. Redactar requisitos observables, criterios de aceptación con escenarios,
   decisiones/supuestos aprobados, contratos mínimos y plan por dependencias.
   Añadir casos de errores, interrupciones y verificación manual pertinentes.
5. Registrar recomendaciones rechazadas y sus razones reales. Vincular decisiones
   arquitectónicas a la constitución y evitar duplicar su catálogo completo.

## Salidas y verificación

Entregar `specs/<feature>/SPEC.md` o su propuesta si el alcance es de planificación,
con dudas bloqueantes claramente separadas. Comprobar que cada requisito tenga
criterios verificables y que los escenarios no amplíen silenciosamente el producto.
No marcar como resuelta una duda por silencio del usuario.

No ejecutar builds, aplicaciones ni pruebas. Proponer comandos manuales solo para
módulos que existan, o identificar explícitamente las tareas futuras. El principal
puede delegar exploración al architect siguiendo el protocolo de AGENTS.md.
