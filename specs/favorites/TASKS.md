# Tareas: Favoritos

Estado: **10/11 tareas implementadas y revisadas estáticamente; exportación de esquema y ejecución pendientes**. Fecha: **2026-10-05**.

Fuentes: [SPEC funcional](SPEC.md), [PLAN técnico](PLAN.md) e [índice](../README.md).

## Uso y dependencias

Base: core/domain y core/data; design system para lista; completar infraestructura de favoritos antes de dar por terminadas acciones Home/Detalle. Quiz reutiliza protección de archivos por contrato compartido.

Los IDs externos remiten a TASKS.md del área correspondiente mediante el índice.
Las dependencias indican entregables necesarios, no obligan a cerrar toda una
feature antes de consumir un contrato compartido. Respetar el orden dentro de
cada cadena; tareas que escriben archivos compartidos se ejecutan secuencialmente.
Todas las ubicaciones son objetivo y las casillas vacías no acreditan código escrito.

## Trabajo desglosado

- [x] **FAV-T01 — Cerrar contrato favorito/estado deseado/completitud y coordinación de imagen con quiz.**
  Alcance: core/domain y PLAN.md. Dependencias: APP-T01.
  Resultado de cierre: FAV-AC01, FAV-AC09–FAV-AC11; marca independiente de red.

- [ ] **FAV-T02 — Diseñar esquema Room, índices, instante de incorporación y exportación.**
  Alcance: core/data/local y PLAN.md. Dependencias: FAV-T01, APP-T03.
  Resultado de cierre: FAV-AC02, FAV-AC06, FAV-AC12; protección y migraciones explícitas.

- [x] **FAV-T03 — Incorporar Room/KSP compatible y crear DAO/entities/database/mappers.**
  Alcance: core/data y catálogo. Dependencias: FAV-T02, APP-T02.
  Resultado de cierre: schema sin fallback destructivo ni entidades en dominio.

- [x] **FAV-T04 — Implementar transacción marca+datos disponibles y observación consistente.**
  Alcance: core/data/repository y favoritos domain. Dependencias: FAV-T03.
  Resultado de cierre: FAV-AC01, FAV-AC05, FAV-AC09; idempotencia y coordinación por ID.

- [x] **FAV-T05 — Implementar descargas privadas temporales, validación y publicación de copia protegida.**
  Alcance: core/data/image. Dependencias: FAV-T04.
  Resultado de cierre: FAV-AC04, FAV-AC10; fallo/desmarcado no reinstala marca.

- [x] **FAV-T06 — Implementar retención tras desmarcar y limpieza acotada con referencias compartidas.**
  Alcance: core/data/image/local. Dependencias: FAV-T05; contrato de pins quiz acordado.
  Resultado de cierre: FAV-AC07, FAV-AC11; proteger favoritos/quiz y actualizar cobertura Home.

- [x] **FAV-T07 — Implementar local primero, refresco coordinado y completado de faltantes con app abierta.**
  Alcance: core/data/repository. Dependencias: FAV-T04, FAV-T05.
  Resultado de cierre: FAV-AC09, FAV-AC13; imagen anterior conservada y sin workers.

- [x] **FAV-T08 — Crear feature favoritos, ViewModel/lista/vacío/error y navegación por callbacks.**
  Alcance: feature/favorites y app. Dependencias: FAV-T06, FAV-T07, DS-T06, APP-T06.
  Resultado de cierre: FAV-AC03, FAV-AC08; orden reciente y conservación de scroll.

- [x] **FAV-T09 — Escribir pruebas con reloj fake, errores, orden e idempotencia.**
  Alcance: domain/data/favorites tests. Dependencias: FAV-T08.
  Resultado de cierre: FAV-AC01, FAV-AC05, FAV-AC12, FAV-AC13.

- [x] **FAV-T10 — Escribir pruebas de Room en disco/reapertura, migraciones y protección de imágenes.**
  Alcance: core/data/androidTest y escenarios manuales. Dependencias: FAV-T09.
  Resultado de cierre: FAV-AC02, FAV-AC04, FAV-AC06, FAV-AC10, FAV-AC11; memoria no prueba persistencia.

- [x] **FAV-T11 — Revisar consistencia Home/Detalle/Favoritos y entregar validación reinicio/offline.**
  Alcance: app integración y documentos. Dependencias: FAV-T10; HOME-T07, DETAIL-T05 para revisión integrada.
  Resultado de cierre: FAV-AC01–FAV-AC13; comandos y evidencia pendiente identificados.

## Verificación de ejecución

- [ ] **FAV-V01 — El desarrollador ejecuta las tareas de PLAN.md aplicables al incremento.**
  Depende de implementación y pruebas escritas; registrar comandos/entorno/resultados
  y fallos pendientes. El agente no ejecuta build ni pruebas.

- [ ] **FAV-V02 — El desarrollador verifica los escenarios mobile y visuales del spec.**
  Registrar conectividad/interrupciones/idiomas/texto ampliado/TalkBack/posturas que
  correspondan, evidencia y limitaciones; screenshots solo acreditan apariencia.

## Registro de avance y evidencia

Infraestructura inicial incorporada el 2026-10-01 y lista Favoritos el 2026-10-05.
Existe `:feature:favorites` conectado desde app; ejecución todavía no verificada.

| Tarea | Evidencia y límites |
| --- | --- |
| FAV-T01 | Contratos domain separan marca/datos/completitud, estado deseado idempotente y retención de medios de quiz. |
| FAV-T02 parcial | Room v1, marcas sin FK, instante estable de incorporación y relaciones por especie. Exportación KSP configurada; JSON v1 pendiente de generación/versionado por desarrollador. No hay fallback destructivo. |
| FAV-T03/T04 | Database/DAO/entities/mappers, bindings Hilt y `FavoritesRepositoryImpl`: transacción marca/datos conocidos, Flow por ID y lista por incorporación DESC/ID ASC; desmarcar solo borra marca. |
| FAV-T05 | `PrivateImageStore`: HTTPS, temporal privado, límites, decodificación antes de publicar, concurrencia cuatro/cancelación; error conserva archivo anterior y finalización no reinstala marca. |
| FAV-T06 | DAO prepare/restore/release de referencias por sesión; `QuizMediaRepositoryImpl` valida imágenes e IDs y publica pins independientes de marca. `PrivateImageStore` coordina limpieza/publicación/referencias con mutex y protección temporal contada. Desmarcar/liberar conserva caché normal. No implementa preguntas ni sesión de quiz. |
| FAV-T07 | `FavoritesViewModel` observa lista local antes del refresco por ID, máximo cuatro; progreso/fallo por Pokémon, oculta cancela descargas y retorno retoma; rotación/idioma y emisiones Room no reinician refresco. Contratos complete/refresh compartidos con Home/Detalle. |
| FAV-T08 | `feature/favorites` gradle/manifest, Route/Screen/ViewModel, plurales es/en, grid adaptativo y scroll con ID ancla; tarjeta incompleta sin número inventado; abrir detalle/explorar mediante callbacks de app. |
| FAV-T09 | `FavoritesViewModelTest` cubre local antes remoto, fallos lectura/escritura, confirmación observada, cancelación/retorno, concurrencia cuatro y scroll. `PersistenceTest` amplía reloj con remarcado/desempate/idempotencia. `FavoritesScreenTest` cubre vacío, error y acciones separadas sin datos. |
| FAV-T10 | `FavoriteMediaTest` usa Room en disco/PNG válido/HTTP interceptado: copia protegida ante limpieza, refresco inválido conserva imagen, pin independiente, prepare fallido preserva pins, reapertura de pins, media ausente y desmarcado durante descarga. `PersistenceTest` cubre marcas/datos en disco y consulta offline. Solo existe v1: no hay salto de versión que migrar; añadir pruebas de migración cuando exista un esquema posterior. Ninguna prueba ejecutada. |
| FAV-T11 | Integración real `FeatureEntries.favorites`→FavoritesRoute y callbacks existentes de Detalle/Explorar. Revisión independiente android-reviewer sin defectos materiales confirmados; revisión final del principal. Tres XML válidos, domain sin Android/infraestructura y feature sin dependencia de data/Home/Detalle. Comandos y escenarios mobile en PLAN/README. Tests de host con fixtures no sustituyen el recorrido real manual. |

FAV-T02 permanece parcial: no existe aún el JSON v1 generado/versionado. Su
generación requiere el build manual del desarrollador y no se fabrica como
evidencia. FAV-T11 cerrado por revisión estática. FAV-V01/V02 no tienen resultados observados.
La cuota MCP de Figma impidió contexto adicional; cotejo visual/manual pendiente.
Pruebas escritas no acreditan persistencia ejecutada; comandos en PLAN.
