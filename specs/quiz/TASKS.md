# Tareas: Quiz: Who is that Pokémon?

Estado: **15/15 tareas completadas y revisadas estáticamente; validación de ejecución pendiente**. Fecha: **2026-10-05**.

Fuentes: [SPEC funcional](SPEC.md), [PLAN técnico](PLAN.md) e [índice](../README.md).

## Uso y dependencias

Base y design system; identidad/catálogo/estadísticas de core y sistema de protección de archivos definido junto a favoritos. No importar features para reutilizar repositorios.

Los IDs externos remiten a TASKS.md del área correspondiente mediante el índice.
Las dependencias indican entregables necesarios, no obligan a cerrar toda una
feature antes de consumir un contrato compartido. Respetar el orden dentro de
cada cadena; tareas que escriben archivos compartidos se ejecutan secuencialmente.
Todas las ubicaciones son objetivo y las casillas vacías no acreditan código escrito.

## Trabajo desglosado

- [x] **QUIZ-T01 — Cerrar elegibilidad de candidatos/pistas, contratos de medios y snapshot de sesión.**
  Alcance: core/domain y PLAN.md. Dependencias: APP-T01, FAV-T01.
  Resultado de cierre: QUIZ-AC01, QUIZ-AC02, QUIZ-AC10, QUIZ-AC11; sin pistas ambiguas.

- [x] **QUIZ-T02 — Implementar generador puro con RNG inyectable y cuatro opciones únicas.**
  Alcance: core/domain/quiz. Dependencias: QUIZ-T01.
  Resultado de cierre: QUIZ-AC01, QUIZ-AC02; cinco correctas distintas y repetición entre sesiones válida.

- [x] **QUIZ-T03 — Implementar evaluación/avance/bloqueo/puntuación/reinicio puros.**
  Alcance: core/domain/quiz. Dependencias: QUIZ-T02.
  Resultado de cierre: QUIZ-AC03–QUIZ-AC05; eventos obsoletos y dobles no puntúan.

- [x] **QUIZ-T04 — Implementar datos locales elegibles y verificación de cinco imágenes antes de inicio.**
  Alcance: core/data/quiz e image. Dependencias: QUIZ-T01, HOME-T04, FAV-T05.
  Resultado de cierre: QUIZ-AC06, QUIZ-AC10; insuficiencia offline recuperable.

- [x] **QUIZ-T05 — Implementar pins de sesión/restauración y liberación independiente de favoritos.**
  Alcance: core/data/image y contrato de snapshot. Dependencias: QUIZ-T04, FAV-T06.
  Resultado de cierre: QUIZ-AC06, QUIZ-AC07; limpieza no elimina sesión ni protege historial eterno.

- [x] **QUIZ-T06 — Crear feature/quiz y ViewModel de preparación, error y sesión bloqueada.**
  Alcance: feature/quiz/settings/presentation. Dependencias: APP-T03, QUIZ-T03, QUIZ-T05.
  Resultado de cierre: QUIZ-AC03–QUIZ-AC07; snapshot sin reevaluación.

- [x] **QUIZ-T07 — Inspeccionar inicio Figma y componer inicio/pregunta/feedback/resultado final.**
  Alcance: quiz screens y recursos bilingües. Dependencias: QUIZ-T06, DS-T05, DS-T06, DS-T08.
  Resultado de cierre: QUIZ-AC01, QUIZ-AC04, QUIZ-AC05, QUIZ-AC08; cuatro opciones.

- [x] **QUIZ-T08 — Implementar Mostrar pistas accesible y estado de visibilidad por pregunta.**
  Alcance: quiz presentación. Dependencias: QUIZ-T07.
  Resultado de cierre: QUIZ-AC11; hechos reales sin nombre/número ni cambio de puntuación.

- [x] **QUIZ-T09 — Integrar Back a Home, Continuar/Reiniciar y confirmación con recuperación de tarea.**
  Alcance: quiz y app/navigation. Dependencias: QUIZ-T08, APP-T07.
  Resultado de cierre: QUIZ-AC07, QUIZ-AC09; apertura nueva sin sesión.

- [x] **QUIZ-T10 — Escribir pruebas deterministas de reglas/ViewModel/pistas/imágenes/pins y offline.**
  Alcance: domain/data/quiz tests y escenarios manuales. Dependencias: QUIZ-T09.
  Resultado de cierre: QUIZ-AC01–QUIZ-AC11; corrupción conserva progreso.

- [x] **QUIZ-T11 — Revisar ocultación accesible, cancelación y entregar comandos/escenarios.**
  Alcance: documentos y cambios quiz. Dependencias: QUIZ-T10.
  Resultado de cierre: sin identidad filtrada antes de responder ni ejecución afirmada.

- [x] **QUIZ-T12 — Ajustar portada y cabecera a las referencias locales.**
  Alcance: QuizScreens y documentación de recursos. Dependencias: QUIZ-T07.
  Resultado: portada sin app bar adicional, contenido/CTA centrados; cabecera de pregunta centrada con Back accesible. Mantener Continuar/Reiniciar/error/confirmación.

- [x] **QUIZ-T13 — Ajustar pregunta, opciones y revelación.**
  Alcance: QuizScreens. Dependencias: QUIZ-T12.
  Resultado: imagen 260 dp, cuatro opciones con borde Gray200 y selección bloqueada; correcta amarilla tras responder, texto de acierto/error y avance. Pistas y Ready requerido; no nueva aleatoriedad ni cambios de puntuación.

- [x] **QUIZ-T14 — Adaptar resultado final y revisar el incremento visual.**
  Alcance: QuizResultsScreen, pruebas UI y documentos. Dependencias: QUIZ-T13.
  Resultado: resultado 0/5–5/5 separado de revelación, composición centrada y reinicio; scroll/texto ampliado/ventanas amplias; revisión estática y comandos manuales.

- [x] **QUIZ-T15 — Incorporar ilustración original de portada y decoración de revelación.**
  Alcance: assets de quiz y callsites Android. Dependencias: recurso original suministrado.
  Resultado: originales suministrados incorporados como PNG Android en drawable-nodpi, proporciones conservadas y confeti decorativo detrás de la imagen únicamente tras responder. Portada con descripción es/en; sin usar capturas como recursos.

## Verificación de ejecución

- [ ] **QUIZ-V01 — El desarrollador ejecuta las tareas de PLAN.md aplicables al incremento.**
  Depende de implementación y pruebas escritas; registrar comandos/entorno/resultados
  y fallos pendientes. El agente no ejecuta build ni pruebas.

- [ ] **QUIZ-V02 — El desarrollador verifica los escenarios mobile y visuales del spec.**
  Registrar conectividad/interrupciones/idiomas/texto ampliado/TalkBack/posturas que
  correspondan, evidencia y limitaciones; screenshots solo acreditan apariencia.

## Registro de avance y evidencia

| Tarea | Evidencia incorporada el 2026-10-05 |
| --- | --- |
| T01 | PLAN concreta elegibilidad en ambos idiomas, firma real tipos/estadísticas distinta de distractores, pool acotado, snapshot JSON v1 y owners por tarea. Contratos de candidatos/medios reutilizados y reconcile añadido sin nuevas entidades Room. |
| T02/T03 | `core/domain/.../quiz/QuizSession.kt`: RNG inyectado, cinco correctas distintas y cuatro opciones únicas con nombres no ambiguos. Respuesta/avance reciben sesión e índice; primera respuesta bloquea, puntuación derivada, avance explícito y pistas ocultas en pregunta nueva. |
| T04 | `QuizCandidateRepositoryImpl`: pool local con archivos verificados; fallback a página nacional de hasta 30 con offset aleatorio, datos completos, sin descargar catálogo entero. `QuizMediaRepositoryImpl.prepare` verifica/protege las cinco imágenes antes de exponer pregunta. |
| T05 | Media guarda copias en caché compartida y conserva pins independientes de favoritos. reconcile por taskId conserva owners de otras tareas recuperables y libera sustituidos/huérfanos. Restore valida mismos IDs y archivos; corrupción recupera mismas correctas. Reemplazo publica snapshot antes de liberar owner previo; cancelación libera solo nuevo owner no publicado. |
| T06 | `:feature:quiz` creado con versiones existentes y dependencias domain/DS; `QuizViewModel`/StateFlow con preparación única, errores/reintentos, sesión/archivos y validación de snapshot en SavedStateHandle. |
| T07 | Inspeccionadas las tres capturas locales de 360 × 800: portada, pregunta y revelación. Sustituyen la inspección remota bloqueada por cuota. `QuizScreens` incluye los cuatro estados; la captura Result representa una respuesta revelada, no el resultado final de cinco preguntas. Ver ASSETS.md para diferencias aprobadas e ilustraciones integradas. |
| T08 | Pistas disponibles para todos: tipos y seis estadísticas reales, sin nombre/número, guardadas en snapshot y ocultas al avanzar. Silueta usa descripción neutra y misma imagen local al revelar. |
| T09 | App retiene QuizViewModel en owner Activity y suministra taskId real; Quiz/QuizResults conectados por ID y callbacks. Enter/leave conserva sesión; Back visible/sistema a Home, regreso Continuar/Reiniciar y confirmación solo si pierde progreso activo. |
| T10 | `QuizSessionTest`, `QuizViewModelTest`, `QuizScreenTest` y casos añadidos a `FavoriteMediaTest`: generación, ambigüedad/pistas, lock/stale events, 0/5–5/5, snapshots/idiomas, cinco medios verificados, recuperación/corrupción, offline y pins/reconciliación/favoritos. Pruebas escritas, no ejecutadas. |
| T11 | Dos revisiones del principal/android-reviewer; tres defectos corregidos y regresiones escritas. XML de manifest y recursos válidos, 35 claves es/en iguales y referencias de strings existentes; 93 enlaces Markdown locales comprobados. Sin imports de infraestructura/Android en las reglas de dominio ni de data/otras features en quiz. PLAN contiene comandos reales y escenarios manuales. |
| T12 | Portada sin app bar adicional, CTA amarillo centrado y cabecera con Back de 48 dp/título centrado. Conservados recuperación, errores y confirmación. Texto provisional de portada explícito hasta incorporar el original en T15. |
| T13 | Imagen de 260 dp; cuatro opciones blancas con borde Gray200 y altura mínima de 48 dp. Correcta amarilla solo tras responder; selección y bloqueo con semántica bilingüe. Pistas/progreso y avance explícito conservados, opciones requieren imagen Ready. No cambia generación ni puntuación. |
| T14 | Resultado final centrado y desplazable con puntuación de cinco preguntas/reinicio. Pruebas UI ampliadas para semántica de respuesta y resultado 5/5/reinicio, escritas sin ejecutar. Recursos XML válidos, 38 claves únicas iguales es/en y referencias de strings existentes. Revisión independiente sobre capturas/código estable sin defectos confirmados; cotejo en dispositivo pendiente. |
| T15 | `assets/image 5.png` (624 × 458) y `assets/image 12.png` (720 × 480), inspeccionados y copiados sin alteración a `drawable-nodpi/quiz_intro_illustration.png` y `quiz_confetti.png`. PNG con transparencia, hashes origen/destino iguales. Portada hasta 312 dp de ancho con proporción original y descripción localizada; confeti sin descripción, detrás del Pokémon y solo tras responder. Revisión estática de recursos y callsites; sin build, pruebas ni render. |

T11 cerrada por revisión estática del principal y android-reviewer. Se corrigieron
publicación del reemplazo en segundo plano (espera a RESUMED y conserva ambos pins),
respuestas habilitadas mientras Coil carga/falla (Ready requerido) y Back saliente
sin guardia de destino (AppNavigator.exploreHomeFromQuiz). Regresiones escritas
para esos tres escenarios. Segunda revisión independiente sin defectos nuevos
confirmados; compilación, lifecycle real y accesibilidad no ejecutados.
T07 se cierra con la inspección de las referencias locales; T12–T14 incorporan
el ajuste visual y su revisión estática. T15 incorpora logo y confeti originales.
La coincidencia visual completa en dispositivo no está acreditada.
QUIZ-V01/V02 siguen abiertos: no se ejecutaron compilación, pruebas, app ni renders.
FAV-T02 (JSON Room v1) sigue pendiente de compilación manual, sin cambios de entidades
o versión de esquema en este incremento.
