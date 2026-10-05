# Tareas: Detalle de Pokémon

Estado: **9/9 tareas implementadas y revisadas estáticamente; ejecución pendiente**. Fecha: **2026-10-05**.

Fuentes: [SPEC funcional](SPEC.md), [PLAN técnico](PLAN.md) e [índice](../README.md).

## Uso y dependencias

Base y design system; catálogo/lectura de detalle en core; contrato/persistencia de favoritos antes de cerrar marcado y offline. Home/Favoritos aportan callbacks de integración desde app, no dependencias Gradle.

Los IDs externos remiten a TASKS.md del área correspondiente mediante el índice.
Las dependencias indican entregables necesarios, no obligan a cerrar toda una
feature antes de consumir un contrato compartido. Respetar el orden dentro de
cada cadena; tareas que escriben archivos compartidos se ejecutan secuencialmente.
Todas las ubicaciones son objetivo y las casillas vacías no acreditan código escrito.

## Trabajo desglosado

- [x] **DETAIL-T01 — Cerrar lectura por especieID, detalle parcial/completo y favoritos compartidos.**
  Alcance: core/domain y PLAN.md. Dependencias: APP-T01, FAV-T01.
  Resultado de cierre: DETAIL-AC01, DETAIL-AC09; seis claves y faltantes explícitos.

- [x] **DETAIL-T02 — Crear módulo detalle y entrada por ID/callbacks.**
  Alcance: feature/detail y settings. Dependencias: APP-T03, DS-T03.
  Resultado de cierre: sin dependencias de Home/Favoritos/data.

- [x] **DETAIL-T03 — Implementar mappers/lectura por ID, claves estadísticas y errores locales/remotos.**
  Alcance: core/data/detail. Dependencias: DETAIL-T01, HOME-T04.
  Resultado de cierre: DETAIL-AC01, DETAIL-AC08; dato inválido no se vuelve cero.

- [x] **DETAIL-T04 — Implementar ViewModel: contenido local primero, refresco y rechazo de resultados obsoletos.**
  Alcance: feature/detail/presentation. Dependencias: DETAIL-T02, DETAIL-T03, FAV-T07.
  Resultado de cierre: DETAIL-AC04, DETAIL-AC09, DETAIL-AC10.

- [x] **DETAIL-T05 — Integrar favorito observado, estado deseado y feedback de escritura por ID.**
  Alcance: detail ViewModel. Dependencias: DETAIL-T04, FAV-T04.
  Resultado de cierre: DETAIL-AC02; fallo conserva contenido/marca real.

- [x] **DETAIL-T06 — Componer identidad, imagen, tipos, gradiente y seis estadísticas con DS.**
  Alcance: DetailScreen y recursos. Dependencias: DETAIL-T05, DS-T06, DS-T07.
  Resultado de cierre: DETAIL-AC01, DETAIL-AC05, DETAIL-AC06; faltante/imagen separada.

- [x] **DETAIL-T07 — Integrar Back/origen y conservación de ID/scroll en tarea.**
  Alcance: detalle y app/navigation. Dependencias: DETAIL-T06, APP-T07.
  Resultado de cierre: DETAIL-AC03, DETAIL-AC07; salir no espera red.

- [x] **DETAIL-T08 — Escribir pruebas de mappers/ViewModel/favoritos offline y semántica.**
  Alcance: tests domain/data/detail. Dependencias: DETAIL-T07.
  Resultado de cierre: DETAIL-AC01–DETAIL-AC10; reapertura integrada con persistencia real.

- [x] **DETAIL-T09 — Revisar gradiente/accesibilidad y entregar pasos de validación.**
  Alcance: PLAN.md/TASKS.md y cambios detalle. Dependencias: DETAIL-T08.
  Resultado de cierre: escenarios offline, texto ampliado y foldable documentados.

## Verificación de ejecución

- [ ] **DETAIL-V01 — El desarrollador ejecuta las tareas de PLAN.md aplicables al incremento.**
  Depende de implementación y pruebas escritas; registrar comandos/entorno/resultados
  y fallos pendientes. El agente no ejecuta build ni pruebas.

- [ ] **DETAIL-V02 — El desarrollador verifica los escenarios mobile y visuales del spec.**
  Registrar conectividad/interrupciones/idiomas/texto ampliado/TalkBack/posturas que
  correspondan, evidencia y limitaciones; screenshots solo acreditan apariencia.

## Registro de avance y evidencia

| Tarea | Evidencia incorporada el 2026-10-05 |
| --- | --- |
| T01 | `core/domain/.../repository/Repositories.kt`: caché, observación, actualización por ID y completado de favoritos separados. PLAN registra coordinación. |
| T02 | `feature/detail/build.gradle.kts`, manifest y `DetailRoute`; settings y app incluyen módulo. Solo domain/designsystem, sin dependencias entre features. |
| T03 | `CatalogRepositoryImpl` y DAO observable; resolución de especie/variedad predeterminada con PokéAPI sin CSV al abrir detalle; reutiliza mapper de seis claves y transacción que conserva archivo local. |
| T04 | `DetailViewModel`: caché antes de actualización, observación Room, fallos sin borrar contenido, cancelación/guardas por ID y regreso visible. |
| T05 | Estado observado de marcas y escritura deseada bloqueada hasta confirmación; completar imagen/datos separado. `FavoritesRepositoryImpl.complete`; Home reutiliza ese contrato. Desmarcado mantiene caché. |
| T06 | `DetailScreen`: componentes DS, gradiente del primer tipo, etiquetas de ambos, seis filas con null explícito y referencia 255, textos es/en, distribución por ancho útil/fontScale. |
| T07 | `DetailRoute` y entrada real de `PocketDexApp`: Back al callback del origen; sistema usa NavDisplay existente. SavedStateHandle conserva ID/offset y restauración tras contenido disponible. |
| T08 | `PokemonMappingTest`, `DetailViewModelTest`, `DetailScreenTest` y escenario nuevo de `PersistenceTest`: mapeo, identidad inválida, datos parciales/offline, favoritos confirmados, cancelación, scroll, controles/estadísticas y Room en disco/reapertura. Pruebas escritas, sin ejecutar. |
| T09 | Revisión del principal y android-reviewer: corregidas descarga duplicada al marcar durante carga y sobrescritura de detalle nuevo con snapshot antiguo. `FavoriteMergeTest`, prueba de marcado durante refresco y Room cubren esas regresiones. Revisión final del delta sin nuevos defectos concretos; XML es/en válido con 22 textos cada uno. Pasos y comandos en PLAN. |

No se ejecutaron builds, pruebas, aplicación ni capturas. DETAIL-V01/V02 permanecen
abiertas; no se acredita fidelidad renderizada, TalkBack, persistencia ejecutada o
restauración real de proceso. La cuota de Figma bloqueó nuevas lecturas después
de metadata; se reutiliza el DS aprobado y se requiere cotejo visual manual.
La pantalla de Favoritos se incorporó el 2026-10-05: Home→Detalle y
Favoritos→Detalle→Back están integrados mediante app; recorrido real manual pendiente.
