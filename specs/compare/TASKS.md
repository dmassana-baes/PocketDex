# Tareas: Comparador de estadísticas

Estado: **12/12 tareas completadas y revisadas estáticamente; validación de ejecución y fidelidad visual pendientes**. Fecha: **2026-10-05**.

Fuentes: [SPEC funcional](SPEC.md), [PLAN técnico](PLAN.md) e [índice](../README.md).

## Uso y dependencias

Base y design system; contrato/reglas de consulta compartidos definidos con Home y lectura de estadísticas de core. No necesita importar las features Home/Detalle.

Los IDs externos remiten a TASKS.md del área correspondiente mediante el índice.
Las dependencias indican entregables necesarios, no obligan a cerrar toda una
feature antes de consumir un contrato compartido. Respetar el orden dentro de
cada cadena; tareas que escriben archivos compartidos se ejecutan secuencialmente.
Todas las ubicaciones son objetivo y las casillas vacías no acreditan código escrito.

## Trabajo desglosado

- [x] **COMPARE-T01 — Cerrar contratos de dos slots, resultado por clave y navegación secundaria.**
  Alcance: core/domain y PLAN.md. Dependencias: APP-T01.
  Resultado de cierre: COMPARE-AC01–COMPARE-AC04; sin ganador global.

- [x] **COMPARE-T02 — Implementar reglas puras de identidad distinta, reemplazo, eliminación y empate.**
  Alcance: core/domain/compare. Dependencias: COMPARE-T01.
  Resultado de cierre: COMPARE-AC02–COMPARE-AC04; comparación numérica independiente de UI.

- [x] **COMPARE-T03 — Inspeccionar contexto Figma de selección/selector y crear módulo compare.**
  Alcance: feature/compare/settings y referencia en PLAN.md. Dependencias: APP-T03, DS-T03.
  Resultado de cierre: lenguaje visual documentado sin dado ni imports de Home.

- [x] **COMPARE-T04 — Implementar selector con búsqueda común y lectura de dos detalles por dominio.**
  Alcance: compare selector y adaptadores. Dependencias: COMPARE-T03, HOME-T03, HOME-T04, DETAIL-T03.
  Resultado de cierre: COMPARE-AC01, COMPARE-AC05; sin endpoint ficticio.

- [x] **COMPARE-T05 — Implementar ViewModel con cargas por slot, rechazo obsoleto e invalidación de resultados.**
  Alcance: compare/presentation. Dependencias: COMPARE-T02, COMPARE-T04.
  Resultado de cierre: COMPARE-AC02, COMPARE-AC05, COMPARE-AC06.

- [x] **COMPARE-T06 — Componer slots y resultados con escala 255/empate y acciones accesibles.**
  Alcance: compare screens/recursos. Dependencias: COMPARE-T05, DS-T06, DS-T07.
  Resultado de cierre: COMPARE-AC03, COMPARE-AC04, COMPARE-AC07.

- [x] **COMPARE-T07 — Integrar Comparar/Back y snapshot de par/slot en edición.**
  Alcance: compare y app/navigation. Dependencias: COMPARE-T06, APP-T07.
  Resultado de cierre: COMPARE-AC01, COMPARE-AC04, COMPARE-AC06; nueva apertura vacía.

- [x] **COMPARE-T08 — Escribir pruebas puras y ViewModel de duplicados, reemplazo y respuestas tardías.**
  Alcance: domain/compare tests y feature tests. Dependencias: COMPARE-T07.
  Resultado de cierre: COMPARE-AC01–COMPARE-AC07 trazados.

- [x] **COMPARE-T09 — Revisar accesibilidad, límites y entregar comandos/manuales.**
  Alcance: documentos y cambios compare. Dependencias: COMPARE-T08.
  Resultado de cierre: seis filas verificables; ejecución por desarrollador.

- [x] **COMPARE-T10 — Ajustar selección a las referencias locales.**
  Alcance: CompareScreen y recursos es/en. Dependencias: COMPARE-T03, COMPARE-T06.
  Resultado: título e introducción, slots grises con sprites centrados, añadir/reemplazar/quitar accesibles y CTA amarillo; apilados o en fila según espacio útil.

- [x] **COMPARE-T11 — Ajustar selector a hoja y candidatos compactos por tipo.**
  Alcance: SelectorScreen/Route. Dependencias: COMPARE-T10.
  Resultado: superficie con esquinas superiores y handle decorativo, búsqueda, cuadrícula adaptativa con nombre/número/tipos/sprite/Poké Ball; conservar paginación, errores y ancla de scroll. Back cancela; no añadir gesto de arrastre.

- [x] **COMPARE-T12 — Ajustar resultados y revisar el incremento visual.**
  Alcance: CompareResultsScreen/Route, documentos y pruebas existentes. Dependencias: COMPARE-T11.
  Resultado: miniaturas con nombres y seis filas compactas, valores/barras 255, mayor/empate explícitos; sin dado ni ganador global. Revisión estática y comandos manuales, sin afirmar fidelidad ejecutada.

## Verificación de ejecución

- [ ] **COMPARE-V01 — El desarrollador ejecuta las tareas de PLAN.md aplicables al incremento.**
  Depende de implementación y pruebas escritas; registrar comandos/entorno/resultados
  y fallos pendientes. El agente no ejecuta build ni pruebas.

- [ ] **COMPARE-V02 — El desarrollador verifica los escenarios mobile y visuales del spec.**
  Registrar conectividad/interrupciones/idiomas/texto ampliado/TalkBack/posturas que
  correspondan, evidencia y limitaciones; screenshots solo acreditan apariencia.

## Registro de avance y evidencia

| Tarea | Evidencia incorporada el 2026-10-05 |
| --- | --- |
| T01/T02 | `core/domain/.../compare/PokemonComparison.kt`: dos IDs opcionales distintos, elegir/quitar y comparación completa de seis claves con mayor/menor/empate; sin Android ni ganador global. |
| T03 | Módulo `feature/compare`, settings y dependencias domain/DS creados con versiones existentes. Tras el bloqueo de cuota Figma, se inspeccionaron las cinco capturas locales de `assets/screens-designs/` y `assets/dice.svg` suministrados por el usuario. [ASSETS.md](ASSETS.md) registra estados, lenguaje visual, exclusiones funcionales y diferencias de composición con la implementación. Inspección de referencia completada; fidelidad de la app pendiente en V02. |
| T04 | `SelectorViewModel` usa CatalogRepository y regla común nombre/número/idioma, debounce 300 ms e IME; caché/páginas de 30/cursors anterior y siguiente con reintento que conserva resultados. `CompareViewModel` usa PokemonRepository local/remoto sin importar Home/Detalle/data. |
| T05 | Cargas por slot con cancelación, generación y semáforo dos; completar estadísticas faltantes, mantener otro slot y datos parciales ante fallo; quitar/reemplazar invalida resultados. |
| T06 | `CompareScreens`, DS imágenes/tipos/botones/comparación escala 255 y empate; recursos es/en, dos slots apilados o en fila según ancho útil y fuente; Back fuera del scroll, etiquetas con posición/nombre y mayor por fila identificado por texto. |
| T07 | `PocketDexApp` retiene CompareViewModel en owner Activity/SavedStateHandle fuera de entradas; callbacks app llevan ID/slot a selector/resultados. Snapshot de rutas existente protege pushes repetidos y regreso al editor. Par/slot/consulta/scroll recuperables; ninguna tabla de comparaciones. |
| T08 | `PokemonComparisonTest`, `CompareViewModelTest`, `CompareScreenTest`: duplicados, selección incompleta, ambos slots, claves/mayor/empate/0/>255, respuesta tardía, offline/reintento, pair recuperado, debounce/IME/cursor de consulta confirmada, acciones/semántica de filas y Back. Pruebas escritas, no ejecutadas. |
| T09 | Principal y android-reviewer revisaron dominio/presentación/owner/navegación. Corregida limpieza diferida y repetición de consultas equivalentes; regresiones escritas para IME/normalización/scroll y ancla restaurada. Revisión final del delta sin defectos adicionales confirmados. Tres XML válidos; sin imports de data/features entre compare y resto ni Android/infraestructura en dominio. Comandos/escenarios mobile en PLAN. |
| T10 | `CompareScreen`: título 36 sp del DS e introducción es/en; slots Gray100 de mínimo 190 dp con sprites 128 dp, reemplazar/quitar por iconos exactos Refresh/Close con áreas 48 dp y etiquetas; CTA de ancho completo, distribución por ancho/fuente y scroll. |
| T11 | `SelectorScreen`: superficie de hoja limitada a 640 dp, esquinas/handle y cierre accesible, búsqueda y LazyVerticalGrid adaptativa; candidatos por primer tipo con número, ambos tipos, sprite y decoración vectorial. `SelectorRoute` usa LazyGridState conservando headers/IDs/ancla/cursor. No gesto de arrastre ni cambio de reglas de búsqueda. |
| T12 | `CompareResultsScreen/Route`: título centrado, imágenes reales por Coil en dos miniaturas Gray100 con nombres, seis filas compactas con valores reales/barras 255, mayor/empate textual y semántica por nombre. Prueba Compose adicional de reemplazar/quitar el slot correcto escrita, no ejecutada. Principal y android-reviewer revisaron el incremento; sin defectos adicionales confirmados. ASSETS/PLAN/índice actualizados; cotejo ejecutado pendiente. |

T09 cerrada por revisión estática. T03 cerrada mediante las referencias locales:
no depende de recuperar la cuota de Figma. El dado se conserva como fuente y
no se integra porque el spec excluye selección aleatoria.
COMPARE-V01/V02 abiertos: no se ejecutaron build, pruebas, app o capturas de la app.
T10/T11/T12 incorporan el ajuste visual solicitado; no se difiere su implementación a V02.
V02 incluye cotejar en ejecución el incremento y adaptaciones documentadas en ASSETS.md.
El usuario pospuso esas ejecuciones y la generación de esquema hasta terminar los
specs (2026-10-05). FAV-T02 continúa parcial por JSON Room v1, independiente de compare.
