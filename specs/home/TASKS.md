# Tareas: Home: listado, búsqueda y filtros

Estado: **11 tareas de implementación realizadas; verificación de ejecución pendiente**. Fecha: **2026-10-05**.

Fuentes: [SPEC funcional](SPEC.md), [PLAN técnico](PLAN.md) e [índice](../README.md).

## Uso y dependencias

Base: contratos/infraestructura de catálogo; design system: tarjetas/búsqueda/chips; favoritos: contrato y persistencia compartida antes de cerrar sus acciones. Dependencias de entrega, nunca imports entre features.

Los IDs externos remiten a TASKS.md del área correspondiente mediante el índice.
Las dependencias indican entregables necesarios, no obligan a cerrar toda una
feature antes de consumir un contrato compartido. Respetar el orden dentro de
cada cadena; tareas que escriben archivos compartidos se ejecutan secuencialmente.
Todas las ubicaciones son objetivo y las casillas vacías no acreditan código escrito.

## Trabajo desglosado

- [x] **HOME-T01 — Definir estrategia de catálogo completo, página, cobertura y concurrencia acotada.**
  Alcance: core/domain/data y PLAN.md. Dependencias: APP-T01.
  Resultado de cierre: HOME-AC03, HOME-AC10, HOME-AC13; consulta no limitada a páginas visibles.

- [x] **HOME-T02 — Crear módulo Home y enlazar contratos/core/designsystem.**
  Alcance: feature/home build y settings. Dependencias: APP-T03, DS-T03.
  Resultado de cierre: dependencias revisadas, sin data ni feature:favorites.

- [x] **HOME-T03 — Implementar normalización y coincidencia por nombre/número/tipo AND compartida.**
  Alcance: core/domain/query y tests. Dependencias: HOME-T01.
  Resultado de cierre: HOME-AC02–HOME-AC04; #/ceros, fragmento interior, dos tipos.

- [x] **HOME-T04 — Implementar resolución especie/variedad y almacenamiento de catálogo/cobertura.**
  Alcance: core/data/catalog y mappers/local. Dependencias: HOME-T01, HOME-T03, APP-T05.
  Resultado de cierre: HOME-AC10, HOME-AC13; identidades distintas y cobertura honesta.

- [x] **HOME-T05 — Implementar paginación de presentación y continuación/error/reintento sin duplicados.**
  Alcance: feature/home/paging. Dependencias: HOME-T02, HOME-T04.
  Resultado de cierre: HOME-AC01, HOME-AC05, HOME-AC06.

- [x] **HOME-T06 — Implementar ViewModel con debounce, invalidación y filtro provisional.**
  Alcance: feature/home/presentation. Dependencias: HOME-T03, HOME-T05.
  Resultado de cierre: HOME-AC06, HOME-AC11, HOME-AC12; cancelación y 299/300 ms.

- [x] **HOME-T07 — Integrar lectura local primero, refresco y favoritos observados por ID.**
  Alcance: Home ViewModel y contratos data. Dependencias: HOME-T06, FAV-T04, FAV-T07.
  Resultado de cierre: HOME-AC08, HOME-AC13, HOME-AC14; fallos conservan contenido.

- [x] **HOME-T08 — Componer Home con grilla, búsqueda, filtro, estados y callbacks de detalle.**
  Alcance: HomeScreen y recursos bilingües. Dependencias: HOME-T07, DS-T06, DS-T08.
  Resultado de cierre: HOME-AC04, HOME-AC07, HOME-AC09; teclado y favorito independientes.

- [x] **HOME-T09 — Conservar criterios/scroll por destino y recuperar tarea sin repetir acciones.**
  Alcance: Home estado y app integración. Dependencias: HOME-T08, APP-T07.
  Resultado de cierre: HOME-AC09; apertura nueva reinicia criterios sin borrar datos.

- [x] **HOME-T10 — Escribir pruebas de reglas, Paging, ViewModel y escenarios de accesibilidad.**
  Alcance: tests domain/data/home. Dependencias: HOME-T09.
  Resultado de cierre: HOME-AC01–HOME-AC14 trazados; ejecución solo por desarrollador.

- [x] **HOME-T11 — Revisar estáticamente consulta completa y entregar validación manual.**
  Alcance: PLAN.md/TASKS.md y cambios Home/core. Dependencias: HOME-T10.
  Resultado de cierre: cobertura parcial no presentada como completa; comandos aplicables.

## Verificación de ejecución

- [ ] **HOME-V01 — El desarrollador ejecuta las tareas de PLAN.md aplicables al incremento.**
  Depende de implementación y pruebas escritas; registrar comandos/entorno/resultados
  y fallos pendientes. El agente no ejecuta build ni pruebas.

- [ ] **HOME-V02 — El desarrollador verifica los escenarios mobile y visuales del spec.**
  Registrar conectividad/interrupciones/idiomas/texto ampliado/TalkBack/posturas que
  correspondan, evidencia y limitaciones; screenshots solo acreditan apariencia.

## Registro de avance y evidencia

Incremento Home 2026-10-01. Casillas de implementación acreditan archivos escritos y
revisión estática, nunca resultados de build/pruebas. HOME-V01/V02 pendientes.

| Tarea | Evidencia real y límites |
| --- | --- |
| HOME-T01/T04 | `core/data/catalog`, `remote`, `local`: índice nacional + CSV oficiales, una variedad predeterminada por especie, consulta global previa a páginas de 30, máximo cuatro cargas simultáneas, Room separado de metadatos. Cobertura offline según registros descargados. |
| HOME-T02 | `feature/home/build.gradle.kts`, settings y app: feature solo depende de domain/designsystem; Hilt lifecycle Compose, Paging y Coil. Versiones centralizadas, sin cambios a versiones base. |
| HOME-T03 | `core/domain/model/Catalog.kt` y `CatalogQueryTest`: fragmentos canónicos/visibles, trim/case, número exacto con #/ceros y AND con cualquiera de los dos tipos. |
| HOME-T05 | `HomePagingSource`: cursor opaco, conteos/continuaciones, error/reintento, saltos por offset y cancelación propagada. |
| HOME-T06/T07 | `HomeViewModel`: debounce 300 ms, query normalizada, flatMapLatest, borrador Aplicar/descartar, local primero y aviso ante fallo. Favoritos observados por ID; marca confirmada en Room y completado posterior; error no cambia la verdad persistida. |
| HOME-T08 | `HomeScreen`, es/en: grilla adaptativa, búsqueda, chips, panel dentro del área sin bisagra, estados y callbacks independientes. El 2026-10-05 se incorpora `assets/filter.svg` aportado por el usuario como `core/designsystem/.../drawable/pd_filter.xml`, `PocketDexIconAsset.Filter` y leadingIcon del botón. Se conserva etiqueta traducida y acción existente; el icono decorativo no duplica el anuncio accesible. Fidelidad renderizada pendiente de HOME-V02. |
| HOME-T09 | `HomeRoute` y SavedStateHandle: criterios/borrador/scroll/ancla por destino, restauración local/remota, sin descargar prefijos. App conecta Home real y ID de detalle; pantalla real de detalle y contraste entre features esperan sus specs. |
| HOME-T10 | `CsvIndexTest`, `ConcurrencyTest`, `HomePagingSourceTest`, `HomeViewModelTest`, `PersistenceTest` y `HomeScreenTest`, más tests domain existentes. Casos: índice global/variedad/estadísticas, límites/cancelación, debounce/filtros, fallos/favoritos, caché antes de red lenta, recuperación/retry, Room en disco/reapertura/offline/orden y acciones accesibles separadas. Sin ejecución. |
| HOME-T11 | Tres revisiones independientes de android-reviewer y revisión del principal. Corregidos local-first con scroll, refresco anclado, guardado al finalizar refresco y cobertura después de retry; añadida recuperación tras fallo inicial sin caché y retry exitoso. Semaphore compartido y completado por ID; carga incremental visible en el viewport. Cinco XML afectados leídos sin error de parseo; sin imports de Android/infraestructura en domain ni data/otras features desde Home. PLAN/README incluyen comandos, límites y escenarios manuales. |

Pendientes concretos: exportar/versionar JSON v1 de Room
con build manual; compatibilidad/ejecución y revisión visual/mobile. La infraestructura
compartida de favoritos avanzó sin crear su pantalla ni cerrar toda esa spec.
El agente no ejecutó Gradle, lint, pruebas, app ni empaquetado; no generó APK ni
observó resultados de compilación, persistencia real o TalkBack/foldables.

Revisión del cierre de asset (2026-10-05): ya no faltan recursos de implementación
propios de Home. HOME-V01 requiere resultados reales de build y pruebas, más el
JSON v1 exportado. HOME-V02 requiere apariencia, es/en, texto ampliado/TalkBack,
teclado, rotación/foldables, offline/reintentos y recuperación de tarea. El flujo
integrado de HOME-AC07/AC08 con pantallas reales de detalle/favoritos espera esos
specs; el callback por ID y la persistencia compartida ya están incorporados.
Conversión ejecutada con el script existente: XML legible, datos de trazado,
relleno #283141 y recorte 24×24 preservados; los 14 recursos convertidos coinciden
con los hashes del inventario, incluidos los 13 anteriores. Es una revisión
estática de recursos, sin ejecutar builds, pruebas ni renderizar la UI.
Revisión independiente de android-reviewer del cierre de asset: sin defectos
materiales en conversión, integración, semántica, callers y registro de tareas.
