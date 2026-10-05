# Tareas: Base de aplicación y navegación

Estado: **7/11 tareas de implementación realizadas; integración y verificación pendientes**. Fecha: **2026-10-01**.

Fuentes: [SPEC funcional](SPEC.md), [PLAN técnico](PLAN.md) e [índice](../README.md).

## Uso y dependencias

Design system antes de implementación de base. Contratos comunes antes de consumidores; data se incorpora incrementalmente. La integración de navegación final espera las entradas de las features.

Los IDs externos remiten a TASKS.md del área correspondiente mediante el índice.
Las dependencias indican entregables necesarios, no obligan a cerrar toda una
feature antes de consumir un contrato compartido. Respetar el orden dentro de
cada cadena; tareas que escriben archivos compartidos se ejecutan secuencialmente.
Las casillas marcadas acreditan implementación/revisión estática, no ejecución. Las tareas parciales permanecen abiertas y describen abajo su alcance pendiente.

## Trabajo desglosado

- [x] **APP-T01 — Cerrar modelos de identidad/resumen/detalle/fallos y firmas de catálogo/favoritos/quiz.**
  Alcance: core/domain previsto y PLAN.md. Dependencias: DS-T04; acuerdos de consumidores.
  Resultado de cierre: APP-AC01, APP-AC05; sin DTOs/Android/Paging en contratos.

- [x] **APP-T02 — Verificar compatibilidad de plugins y librerías requeridas por el primer incremento.**
  Alcance: gradle/libs.versions.toml y PLAN.md. Dependencias: APP-T01.
  Resultado de cierre: APP-AC07; documentación oficial, versiones base conservadas.

- [x] **APP-T03 — Crear :core:domain Kotlin/JVM y :core:data Android con límites acordados.**
  Alcance: settings y builds core. Dependencias: APP-T02, DS-T03.
  Resultado de cierre: APP-AC01; sin dependencias circulares ni Kotlin Android/kapt añadidos por rutina.

- [x] **APP-T04 — Implementar modelos, resultados/fallos y puntos de inyección de dispatchers.**
  Alcance: core/domain y data/di. Dependencias: APP-T03.
  Resultado de cierre: APP-AC05; cancelación propagada y dobles posibles.

- [x] **APP-T05 — Configurar Hilt/KSP y composición de dependencias en app/data.**
  Alcance: app/di, manifests, builds afectados. Dependencias: APP-T04.
  Resultado de cierre: APP-AC01, APP-AC07; composición fuera de dominio.

- [x] **APP-T06 — Implementar claves tipadas y coordinador Navigation 3 con IDs mínimos.**
  Alcance: app/navigation. Dependencias: APP-T05.
  Resultado de cierre: APP-AC02, APP-AC03, APP-AC04; Home raíz, Back/origen y sin pushes repetidos.

- [ ] **APP-T07 — Implementar conservación por destino y snapshot de tarea recuperada.**
  Alcance: app/navigation y adaptadores de estado. Dependencias: APP-T06.
  Resultado de cierre: APP-AC06, APP-AC08; tarea recuperada diferenciada de apertura nueva.

- [x] **APP-T08 — Integrar tema/locales/insets, barra inferior y lateral según ventana.**
  Alcance: app y componentes DS. Dependencias: APP-T06, DS-T08.
  Resultado de cierre: APP-AC06 y DS-AC12; cambio de postura no cambia ruta.

- [ ] **APP-T09 — Conectar entradas reales de cada feature conforme se implementen.**
  Alcance: app/navigation y composición. Dependencias: APP-T07, APP-T08; entradas de features disponibles.
  Resultado de cierre: APP-AC02–APP-AC04; sin placeholders entregados como funcionalidades.

- [ ] **APP-T10 — Escribir pruebas de contratos/navegación/restauración y decidir flujo UI crítico.**
  Alcance: domain/app tests y justificación. Dependencias: APP-T09.
  Resultado de cierre: APP-AC05, APP-AC08; flujo Home→detalle→favorito→favoritos o justificación de omisión.

- [ ] **APP-T11 — Revisar límites de módulos y entregar README reproducible/comandos aplicables.**
  Alcance: configuración, README de app y planes. Dependencias: APP-T10.
  Resultado de cierre: APP-AC01, APP-AC07; sin publicar ni generar APK durante sesión del agente.

## Verificación de ejecución

- [ ] **APP-V01 — El desarrollador ejecuta las tareas de PLAN.md aplicables al incremento.**
  Depende de implementación y pruebas escritas; registrar comandos/entorno/resultados
  y fallos pendientes. El agente no ejecuta build ni pruebas.

- [ ] **APP-V02 — El desarrollador verifica los escenarios mobile y visuales del spec.**
  Registrar conectividad/interrupciones/idiomas/texto ampliado/TalkBack/posturas que
  correspondan, evidencia y limitaciones; screenshots solo acreditan apariencia.

## Registro de avance y evidencia

Primer incremento 2026-10-01. La revisión estática no equivale a verificación de ejecución.

| Tarea | Evidencia real y límites |
| --- | --- |
| APP-T01/T04 | `core/domain/src/main/kotlin/.../model`, `repository`, `util`: identidad por especie, resumen/detalle parcial, seis estadísticas, criterios AND, páginas con origen/cobertura y contratos de favoritos/quiz. Fallos tipados, ausencia válida y cancelación fuera de resultados; no hay implementaciones que capturen cancelación. |
| APP-T02 | Catálogo y tabla de referencias oficiales/versiones en PLAN. Conservadas las versiones base; compatibilidad binaria conjunta pendiente del build manual. |
| APP-T03 | Settings incluye domain/data; builds JVM/Android separados y sin dependencia circular. No Kotlin Android/kapt. |
| APP-T05 | `PocketDexApplication`, `@AndroidEntryPoint` en MainActivity, manifiesto y `data/di/ExecutionModule`; Hilt/KSP solo fuera del dominio. Home añade `DataModule` con bindings reales de catálogo, detalle y favoritos. |
| APP-T06 | `app/.../navigation`: claves pequeñas, cuatro pilas, Back/origen, reselección sin pushes, callbacks de selector/resultados y exploración Home. Codec de snapshot validado y recuperación explícita. |
| APP-T07 parcial | Snapshot/pilas y decoradores SavedStateHolder/ViewModelStore; Home/Detalle/Favoritos conservan contexto. Compare owner Activity con SavedStateHandle conserva par/slot, selector conserva consulta/ancla y snapshot conserva ruta de resultados. Falta sesión/respuestas del quiz; no se acredita APP-AC08 completo. |
| APP-T08 | MainActivity/UI/adaptive: tema claro existente, es/default-en, ancho actual <600/≥600, WindowManager, región útil sin bisagra e insets/IME consumidos una vez. Pruebas de geometría escritas; comportamiento visual pendiente. |
| APP-T09 parcial | Home/Detalle/Favoritos/Comparador conectan rutas reales. Compare callbacks llevan ID/slot al owner retenido por app; selector/resultados con regreso al editor. Quiz sigue pendiente. |
| APP-T10 parcial | Tests domain/host/snapshot/geometría, Home/Paging, Detalle, Favoritos/Room/medios y Comparador/domain/ViewModels/semántica escritos. Flujos Home→Detalle→Favoritos y Comparador/selector/resultados integrados; recorrido manual pendiente. Quiz y ejecución faltan. |
| APP-T11 parcial | README reproducible y comandos disponibles, límites de módulos revisados estáticamente. El cierre final espera APP-T09/T10 y actualizar instrucciones al integrar features. |

APP-V01/APP-V02 siguen sin evidencia observada. Ejecutar los comandos y escenarios
de PLAN/README; registrar entorno, resultados y fallos aportados por el desarrollador.

Revisión independiente de `android-reviewer` y revisión del principal: corregidos
nombre visible/canónico con AND de tipo y Back/explorar de entradas salientes.
Incluidas regresiones escritas. Resultados del comparador tienen clave con dos IDs
distintos y Back a edición. Análisis estático de los 12 XML de app y manifiestos
afectados sin errores de parseo; búsqueda de imports de Android/infraestructura
en domain sin coincidencias y grafo Gradle sin ciclos. No se ejecutó Gradle,
lint, pruebas ni aplicación; no hay resultados de compilación o fidelidad visual.
