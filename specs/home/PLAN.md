# Plan técnico: Home: listado, búsqueda y filtros

Estado: **incremento y recursos implementados; fidelidad renderizada y ejecución pendientes**. Fecha: **2026-10-05**.

Contrato funcional: [SPEC.md](SPEC.md). Seguimiento: [TASKS.md](TASKS.md).
Módulo principal objetivo: `:feature:home`.
Referencias técnicas: [constitución](../../constitution.md) y [AGENTS.md](../../AGENTS.md).

## Punto de partida y dependencias

Existen app, domain, data, designsystem y `:feature:home`. Home es la entrada real
del host; las otras funcionalidades mantienen su estado de disponibilidad pendiente.
Data incorpora Retrofit/Serialization, índice oficial, Room y persistencia de
favoritos requerida por Home. Compatibilidad conjunta pendiente del build manual.

Base: contratos/infraestructura de catálogo; design system: tarjetas/búsqueda/chips; favoritos: contrato y persistencia compartida antes de cerrar sus acciones. Dependencias de entrega, nunca imports entre features.

## Arquitectura y entrega técnica

Crear `:feature:home` Android/Compose, dependiente de domain/designsystem. Implementar
`HomeRoute`, `HomeScreen`, `HomeViewModel`, `HomeUiState` y adaptador Paging en
presentación. Domain expone criterios, identidad de especie y páginas/resultados;
data obtiene catálogo remoto/local. La app conecta callbacks de detalle y destinos.

Ubicaciones objetivo: `feature/home/.../{presentation,paging}`, recursos/tests;
contratos/reglas de consulta en `core/domain`; resolución de especies/variedades,
consultas e índice/cobertura en `core/data`. No importar la feature de favoritos.

Normalizar búsqueda en regla pura compartida con comparador; combinar texto/tipo,
debounce de 300 ms y distinct sobre criterios normalizados; cambiar criterios
cancela/invalida paginación. Usar identidad/generación de consulta para rechazar
respuestas obsoletas, continuidad explícita y deduplicación por especie.
Guardar criterios/scroll y borrador separado para recuperación de tarea.

## Contratos y decisiones de implementación

### Decisiones del incremento Home (2026-10-01)

Crear Home y la infraestructura compartida de catálogo/favoritos requerida por
sus acciones. No crear todavía pantallas de detalle/favoritos/comparador/quiz.
Página de 30, prefetch 6 y concurrencia máxima de 4 lecturas de Pokémon mediante
semaphore compartido entre páginas y completado de favoritos. Reintentos de
completado de un mismo favorito mantienen una sola operación activa desde Home.

PokéAPI v2 no ofrece búsqueda parcial/AND. Usar un índice ligero: Pokédex nacional
v2 como autoridad de especies/números y CSV oficiales de PokeAPI (`pokemon`,
`pokemon_types`, `pokemon_species_names`) como metadatos de variedad predeterminada,
tipos y nombres es/en. No descargar estadísticas/imágenes de todo el catálogo.
Consultar metadatos antes de paginar; resolver especies nuevas sin fila CSV contra
v2, con concurrencia acotada. No confundir ID de variedad con ID/número de especie.
Si falla el índice remoto, conservar lo local y declarar cobertura parcial.

Room v1 guarda índice separado de los datos realmente descargados, resúmenes/
detalle por especie y marcas separadas (incluidas marcas con solo ID). Metadatos
del índice no acreditan que todo el catálogo esté disponible offline. Exponer
`cached(query)` para lectura inmediata y `page(query, request)` para página remota.
El cursor incorpora identidad de consulta/índice y offset. La primera página
actualiza datos incluso cuando está anclada fuera de la primera página; las
continuaciones pueden reutilizar registros completos previamente descargados.
Paging usa placeholders con conteos globales, saltos desde 90 elementos y claves
de posición independientes del cursor opaco. Recuperar scroll carga solo la página
del ancla; no descarga todas las páginas anteriores. `PageRequest` admite ancla u
offset y `CatalogPage` expone conteos y continuación anterior/siguiente.

Un snapshot de criterio produce primero PagingData local y luego Pager remoto
solo tras éxito. Fallo remoto conserva contenido local con aviso/reintento.
La generación local declara estados de carga terminados, también cuando sus
coincidencias están vacías; no queda como carga infinita al trabajar offline.
Consulta obsoleta se cancela mediante flatMapLatest; filtros se aplican al índice
completo, nunca solo a la lista visible. ViewModel conserva criterios/borrador y
scroll mínimo en SavedStateHandle; app ya conserva owner por destino. Durante
recuperación se emite la caché inmediatamente; cada fuente identifica su primera
instancia para restaurar sobre el snapshot correcto, conservando el ID del ancla.
La posición vuelve a guardarse cuando finaliza el refresco aunque no cambie la
geometría. Éxitos de Paging comunican cobertura, también después de reintentar.
Recreación
no repite marcado ni consultas en curso; nueva tarea recibe estado temporal nuevo.

Favoritos se confirman tras transacción de marca/datos conocidos; completar
detalle/imagen ocurre después y nunca reinstala una marca quitada. Copia de imagen
privada verificada, independiente de Coil; desmarcar conserva datos/archivo como
caché. Caché normal: 512 registros y 64 MiB de medios, excluyendo favoritos y
retenciones de sesión. Limpieza actualiza referencias/cobertura; no hay worker.
Esquema exportado por KSP al compilar manualmente; no migración destructiva.

La lectura previa de Figma MCP fue rechazada por cuota Starter. Se reutilizan
tema/componentes/assets del design system. El 2026-10-05 el usuario aportó
`assets/filter.svg`; el conversor existente incorpora `pd_filter.xml` de 24 dp,
con trazado/relleno originales y máscara rectangular como clip-path. Se registra
como `PocketDexIconAsset.Filter`; `PocketDexButton.leadingIcon` lo muestra junto
a la etiqueta traducida, sin duplicar su semántica ni cambiar el callback.
Fidelidad visual final y HOME-AC09 se mantienen pendientes de revisión manual.

- Entrada Compose de Home recibe estado y callbacks de consulta, filtro, limpieza,
  reintento, favorito y apertura de detalle. `:app` proporciona navegación.
- ViewModel expone `StateFlow` inmutable; consulta/repositorio por dominio. El
  adaptador Paging está en presentación; sin DAO/Retrofit desde Home.
- Consulta: texto y criterio de tipo aprobados más posición de página → resúmenes
  con IDs, nombre/número, tipos, imagen y continuación/fin explícitos; indicar origen
  local/remoto y cobertura completa/parcial, sin filtrar tipos de Room a dominio.
- Observación/escritura de favoritos reutiliza el contrato de favorites; el
  resultado de escritura se confirma contra Room, no contra una copia local.
- Definir estrategia de datos para búsqueda/filtro de catálogo completo conforme
  a HOME-01 a HOME-04. Verificar capacidades reales de PokéAPI antes de diseñar endpoints;
  no asumir un parámetro remoto de búsqueda parcial o filtros compuestos.

El debounce queda fijado en 300 ms y la página en 30. Cancelar trabajo obsoleto
y evitar fan-out sin límite; no introducir
descarga masiva en el arranque para ocultar limitaciones del endpoint.

## Estado, concurrencia y operación mobile

| Aspecto | Decisión o condición comprobable |
| --- | --- |
| Ciclo de vida | Al rotar/cambiar postura no repetir páginas ni escrituras; cancelar consulta obsoleta; conservar contexto por destino según APP-01/APP-02 |
| Estado temporal/persistente | Criterios/scroll en tarea recuperada; apertura nueva con criterios iniciales, pero conserva datos de catálogo descargados y favoritos |
| Conectividad | Sin datos offline: falta de datos/conexión recuperable; con caché: consulta limitada con aviso de cobertura; red lenta/error incremental conserva contenido |
| Persistencia/consistencia | Room para datos/cobertura/marcas; limpieza normal protege favoritos y actualiza cobertura; local primero con refresco al abrir, sin borrar contenido ante fallo |
| Estados | Distinguir inicial/incremental/vacío/error; no ocultar reintento detrás del teclado |
| Navegación/interrupciones | Regreso de detalle conserva criterios/scroll; cerrar panel sin Aplicar descarta borrador de tipo |
| Teclado/foco/repetición | Debounce 300 ms, limpieza independiente e IME que no duplica consulta; acciones accesibles y sin navegación/escrituras duplicadas |
| Pantallas/insets | Vertical/horizontal y foldables; grilla según ancho actual, sin columnas fijas que recorten nombres; IME y bisagra no ocultan acciones |
| Accesibilidad | Nombre, número y tipos legibles; abrir/favorito como acciones distinguibles; anunciar vacío/error y filtro seleccionado |
| Permisos/capacidades | Sin permisos runtime adicionales; usar red de infraestructura y teclado del sistema |
| Rendimiento | Lazy grid/lista, imágenes acotadas y paginación; cancelación de búsqueda; medir consultas costosas sobre catálogo acordado |
| Segundo plano | Sin tarea programada ni precarga persistente; operaciones visibles en scope estructurado |
| Privacidad | Sin historial de búsquedas persistido ni analytics requeridos; no registrar consultas del usuario innecesariamente |
| Idiomas/formatos | Español/inglés según sistema; ID estable y búsqueda sobre nombre visible/canónico; etiquetas y tipos traducibles sin cambiar las reglas |
| Validación mobile | Red lenta/offline, cambio de consulta en carga, teclado, volver de detalle, rotación/postura/idioma y texto ampliado |

## Pendientes técnicos y riesgos

Estrategia, tamaño, cobertura y concurrencia concretados arriba. El índice usa
también `raw.githubusercontent.com`: si ese origen falla se conserva caché parcial;
no se declara una búsqueda global exitosa. Se memoiza durante el proceso, sin TTL
inventado; un proceso nuevo vuelve a obtenerlo. Las páginas visibles se refrescan
al abrir/actualizar. Metadatos de revisiones distintas que produzcan resultados
inconsistentes se rechazan con error recuperable, sin inventar coincidencias.
CSV limitado a 4 MiB por archivo; imagen a 8 MiB y dimensiones de hasta 4096 px,
validación decodificada con muestreo hasta 1024 px. Descargas en curso cancelables.

Pendientes: fidelidad visual, exportar/versionar el JSON v1
de Room mediante compilación manual y comprobar compatibilidad binaria. La apertura
de detalle se entrega como callback con ID; pantalla real y contraste integrado
Home/Detalle/Favoritos se cierran al implementar esos specs.

Referencias oficiales consultadas: [PokéAPI v2](https://pokeapi.co/docs/v2/),
[CSV oficiales](https://github.com/PokeAPI/pokeapi/tree/master/data/v2/csv),
[Paging](https://developer.android.com/jetpack/androidx/releases/paging),
[pruebas Paging](https://developer.android.com/topic/libraries/architecture/paging/test),
[Room](https://developer.android.com/jetpack/androidx/releases/room),
[Retrofit](https://github.com/square/retrofit), [OkHttp](https://github.com/square/okhttp/tree/parent-4.12.0),
[Coil](https://coil-kt.github.io/coil/compose/),
[Hilt Compose](https://developer.android.com/jetpack/androidx/releases/hilt) y
[Turbine](https://github.com/cashapp/turbine/releases).
Versiones incorporadas: Room 2.8.5, Paging 3.5.1, Retrofit 3.0.0, OkHttp 4.12.0,
Coil 3.6.3, Turbine 1.2.1 y Hilt Compose 1.3.0; no se modificaron versiones base.
Hilt Compose usa su artefacto lifecycle para ViewModels, sin Navigation 2.

## Estrategia de pruebas y revisión

- Dominio/repositorio: fragmento interior y mayúsculas, trim, número exacto con #/ceros,
  AND y coincidencia con cualquiera de los dos tipos del Pokémon,
  limpieza, ausencia de coincidencias y coincidencia fuera de la primera página.
- Paging: continuación/fin, error incremental, reintento sin duplicados y
  invalidación al cambiar criterios, con fuente fake determinista; caché parcial
  frente a catálogo completo y reapertura offline de datos guardados.
- ViewModel con Coroutines Test/Turbine: carga/éxito/vacío/error, respuestas fuera de
  orden, cancelación propagada y favorito en curso/fallido; avance de tiempo virtual
  a 299/300 ms y edición durante debounce, sin esperas de reloj reales.
- Compose/UI: separar click de tarjeta/favorito, limpiar y aplicar/cancelar filtros
  según decisión; revisión manual de teclado, TalkBack y adaptación foldable.

Ejecución por el desarrollador; comandos presentes/futuros en [el índice](../README.md).

Usar JUnit 4, Coroutines Test y Turbine, fakes deterministas; MockK solo si aporta
valor. Escribir pruebas significativas, sin ejecutarlas durante sesión del agente.
El desarrollador ejecuta compilación y pruebas y entrega evidencia; no afirmar
que un criterio pasa solo porque su prueba está escrita.

## Comandos y validación del desarrollador

Las siguientes tareas corresponden a módulos/pruebas incorporados. El agente
no ejecuta estos comandos. `connectedDebugAndroidTest`
solo aplica si hay pruebas instrumentadas y dispositivo/emulador preparado.

```powershell
.\gradlew.bat :core:domain:test
.\gradlew.bat :core:data:testDebugUnitTest
.\gradlew.bat :feature:home:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :core:data:connectedDebugAndroidTest
.\gradlew.bat :feature:home:connectedDebugAndroidTest
```

Las dos últimas requieren dispositivo/emulador. Tras assemble, versionar
`core/data/schemas/.../1.json` generado por Room; no hay migración entre versiones
porque ésta es la primera base, y no se permite fallback destructivo. Registrar
entorno, comandos, resultado real y limitaciones en TASKS.md.

Validación manual: catálogo con varias páginas y filtros fuera de la primera;
299/300 ms, IME y limpiezas independientes; error inicial/incremental y reintentos;
caché con red lenta/offline, marcar/desmarcar y fallo de descarga; retorno al área
Home, recuperación de tarea con scroll avanzado y apertura nueva; es/en/otro
idioma, teclado, TalkBack, fuente ampliada, vertical/horizontal y foldables.
El flujo real de regreso desde detalle espera su spec; las pruebas UI actuales
comprueban callbacks distinguibles y controles accesibles de Home.

## Secuencia de entrega

Concretar pendientes y contratos → implementar el incremento en los archivos
responsables → escribir pruebas → revisar estáticamente → entregar validación
al desarrollador. El detalle y dependencias de cada paso están en TASKS.md.
