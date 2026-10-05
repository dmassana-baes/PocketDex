# Plan técnico: Base de aplicación y navegación

Estado: **primer incremento implementado; integración de features y ejecución pendientes**. Fecha: **2026-10-01**.

Contrato funcional: [SPEC.md](SPEC.md). Seguimiento: [TASKS.md](TASKS.md).
Módulo principal objetivo: `:app`.
Referencias técnicas: [constitución](../../constitution.md) y [AGENTS.md](../../AGENTS.md).

## Punto de partida y dependencias

Existen `:app`, `:core:designsystem`, `:core:domain` y `:core:data`. App contiene
el host de navegación/adaptación. El incremento Home añade `:feature:home`, índice
de catálogo real, Retrofit/Serialization, Room y repositorios a data. Las demás
features siguen pendientes. Ver [PLAN Home](../home/PLAN.md).
Las referencias oficiales justifican configuración/API; no acreditan compilación.

Design system antes de implementación de base. Contratos comunes antes de consumidores; data se incorpora incrementalmente. La integración de navegación final espera las entradas de las features.

## Arquitectura y entrega técnica

Tras el design system crear `:core:domain` Kotlin/JVM y `:core:data` Android;
crear las features al abordar sus incrementos. `:app` compone Hilt y Navigation 3.
Domain admite Coroutines/Flow, sin Android/Hilt/Paging/DTOs. Data depende solo de
domain y encapsula Retrofit/Serialization/Room; cada feature depende de domain y
designsystem, nunca de data, app u otra feature.

Ubicaciones objetivo: `settings.gradle.kts`, `gradle/libs.versions.toml`, builds de
core/features; `app/.../navigation` y `di`; `core/domain/.../model`, `repository`,
`usecase`; `core/data/.../remote`, `local`, `repository`, `mapper`, `di`.
Crear Hilt/KSP y bibliotecas compatibles en el incremento que las necesita;
conservar AGP/Kotlin integrado, no añadir Kotlin Android ni kapt por rutina.

Las entradas reciben estado/callbacks; ViewModels exponen StateFlow inmutable,
recogido con collectAsStateWithLifecycle. Inyección por constructor y dispatchers
sustituibles; propagar cancelación. Registrar claves Navigation 3 tipadas con IDs
pequeños, y estado por destino; serialización/restauración sin bitmaps, DTOs,
entidades ni Activity/Context. No convertir una tarea nueva en restauración permanente.

## Contratos y decisiones de implementación

### Actualización de SDK solicitada (2026-10-05)

Por instrucción del usuario, `compileSdk` pasa de 36 con minor API 1 a
`version = release(37)` en los ocho módulos Android (app, designsystem, data y
las cinco features). `targetSdk` pasa de 36 a 37 en `:app`. El dominio Kotlin/JVM
no declara SDK Android. Se actualizan README y constitución para reflejar SDK 37.
No se modifican minSdk 24 ni versiones de plugins/dependencias.

Referencia oficial: [configuración de Android 17 / API 37](https://developer.android.com/about/versions/17/setup-sdk).
Instalar Platform 37 mediante SDK Manager y ejecutar manualmente
`.\gradlew.bat :app:assembleDebug`; compatibilidad del stack y comportamiento con
target 37 pendientes de validación. No se ejecutaron Gradle, builds ni pruebas.

### Primer incremento (2026-10-01)

Se crean domain/data y el host de app; las entradas de funcionalidades se conectan
en APP-T09 cuando existan. El host recibe entradas Compose y callbacks, sin
inventar un catálogo ni repositorios de éxito. Mientras tanto muestra disponibilidad
pendiente, distinta de una lista vacía o un fallo de red.

El ID compartido es `PokemonId` (ID positivo de especie). `PokemonIdentity`
mantiene nombre canónico, nombres localizados opcionales es/en y número nacional
positivo separados del formato visual. El visible usa español → inglés → canónico,
o inglés → canónico; búsqueda considera visible del idioma y canónico.
`PokemonSummary` conserva tipos en orden y referencias remota/local opcionales;
`PokemonDetail` agrega seis estadísticas identificadas y opcionales: ausencia no
equivale a cero. Domain tiene su propio `PokemonType`, sin depender del tipo visual.

`CatalogRepository.page(CatalogQuery, PageRequest)` devuelve `DataResult<CatalogPage>`:
items, cursor opaco, origen y cobertura. `PokemonSearch.parse` centraliza trim,
nombre sin mayúsculas y número exacto con #/ceros; números inválidos no coinciden.
`PokemonRepository.detail(PokemonId)` devuelve detalle opcional (ausencia válida).
`FavoritesRepository` observa IDs y registros y establece el estado deseado con
datos conocidos opcionales; registrar el marcado antecede a completar la descarga.
`QuizCandidateRepository` ofrece candidatos con pistas; `QuizMediaRepository`
prepara/verifica referencias locales protegidas por sesión, restaura y libera su
retención. El generador de cinco preguntas pertenece al incremento quiz.
`DataResult` separa éxito y fallo tipado; CancellationException se propaga, nunca
es un fallo de dominio. Dispatchers y aleatoriedad son interfaces sustituibles.

La navegación conserva cuatro pilas en un snapshot serializado mediante un
Saver de `rememberSaveable` y entrega sus listas a Navigation 3. Guardar selección
y pilas conjuntamente evita snapshots desincronizados; no exige NavController.
Decora cada pila con SavedStateHolder y ViewModelStore antes de seleccionar entradas.
Las claves serializadas llevan IDs/slot/sesión, nunca contenido. Back desapila
secundarios; en raíz principal vuelve a Home; Home raíz delega al sistema.
Reselección no cambia pilas. Back visible y explorar comprueban la ruta actual,
ignorando callbacks de entradas salientes; cambiar de área limpia foco y oculta IME.
Snapshot de UI y ViewModel de cada feature se
incorporará con sus entradas: infraestructura restaurable no acredita todavía
restauración de búsqueda, scroll, selecciones o preguntas inexistentes.

WindowManager informa bisagras separadoras/oclusivas. El host elige una región
rectangular útil sin bisagra y consume safeDrawing/IME una vez; usa el ancho total
actual de ventana para el umbral 600 dp. No obliga a presentar dos paneles.

Esta tabla describe interfaces mínimas propuestas; no fija nombres de métodos,
DTOs ni esquema Room antes de concretar su diseño técnico.

| Contrato de dominio | Entradas / salidas y responsabilidad |
| --- | --- |
| Identidad de Pokémon | ID estable de especie, nombre y número nacional mostrado; HOME-01 admite una entrada por especie, con variedad predeterminada; sin identidad por posición |
| Resumen y detalle | Resumen para listados; detalle con tipos, imagen y seis estadísticas identificadas; sin tipos Android |
| Consulta de catálogo | Criterios y posición de página; resúmenes, continuación/fin, origen y cobertura completa/parcial; semántica definida en Home |
| Lectura de detalle | ID → contenido o fallo explícito; integrar datos locales según favoritos |
| Favoritos observables | Observar IDs/datos/completitud persistidos y establecer estado deseado por ID; guardar marca/datos disponibles antes de completar faltantes; políticas en favorites |
| Generación del quiz | Fuente de candidatos con IDs/nombres/imágenes y tipos/estadísticas para pistas; preparación/protección de medios locales y aleatoriedad sustituible; política en quiz |
| Resultados/fallos | Distinguir ausencia válida, fallo y cancelación; mensajes visibles se traducen en presentación |

Flujo: pantalla → acción de ViewModel → regla/caso de uso o repositorio de dominio
→ implementación data → remoto/local → resultado/Flow → estado → pantalla.
PagingSource/Pager/PagingData pertenecen a presentación; el repositorio recibe
criterios y posición de página, sin conocer la UI.

La observación de favoritos permite actualizar Home/detalle sin copiar marcas
independientes en cada pantalla. Las acciones se realizan por ID estable, incluso
si cambia el idioma o la posición de un elemento.

| Entrada o callback de navegación | Parámetro mínimo y efecto |
| --- | --- |
| Abrir área principal | Identificador tipado del destino; no crea negocio ni selecciona Pokémon |
| Abrir detalle | ID del Pokémon; obtener datos mediante dominio |
| Volver a explorar | Acceso a Home desde vacío de favoritos, conservando sus criterios/scroll vigentes; la acción de limpiar sigue disponible en Home |
| Regresar | Back al origen; no transportar una copia de favorito como resultado |
| Selector del comparador | Slot y selección controlados por compare; no reutilizar Home mediante dependencia de feature |
| Resultados del comparador | Dos IDs distintos en clave secundaria; Back vuelve a editar y conserva selecciones en el owner de compare |

No añadir deep links, notificaciones, cuentas, sincronización remota ni selector
manual de idioma/tema sin requisito nuevo. Resultados internos de compare/quiz
pueden pertenecer a su estado; compare define si usa una vista de resultados.

### Criterios técnicos conservados

| ID histórico | Verificación técnica |
| --- | --- |
| APP-AC01 | Revisión de Gradle/imports confirma límites de la constitución; domain no contiene tipos Android/infraestructura y no hay dependencias entre features |
| APP-AC05 | Repositorios, dispatchers y aleatoriedad se sustituyen en pruebas sin Activity ni servicios reales; la cancelación no se transforma en éxito/error genérico |
| APP-AC07 | Desde entorno limpio no se requieren secretos; dependencias usan catálogo/versiones fijas y compatibilidad oficial documentada al incorporarlas |

- APP-R01: respetar el mapa y límites de dependencias de la constitución. Crear
  módulos conforme se necesitan, comenzando por `:core:designsystem`.
- APP-R02: `:app` coordina destinos mediante Navigation 3, claves tipadas e IDs;
  las features exponen entradas Compose y callbacks, sin importar otras features.
- APP-R03: acceso a Home, comparador, quiz y favoritos; detalle desde Home/favoritos;
  Back visible y del sistema permiten regresar al origen del detalle.
- APP-R04: composición Hilt/KSP fuera del dominio, inyección por constructor y
  dispatchers sustituibles para pruebas cuando corresponda.
- APP-R05: contratos de dominio independientes de Android, DTOs, Room, Retrofit,
  Hilt y Paging; implementaciones en data; UDF y estado inmutable en presentación.
- APP-R06: aplicar tema claro y recursos español/inglés según sistema. Admitir
  vertical, horizontal, redimensionamiento y continuidad al plegar/desplegar.
- APP-R07: ofrecer fallos recuperables sin credenciales privadas, datos fabricados
  ni operaciones bloqueantes en el hilo principal.
- APP-R08: alcance funcional limitado al PDF, conservando el lenguaje visual del
  Figma; sin Random, filtros avanzados, ganador global ni pestañas extra de detalle.
- APP-R09: Home inicial y navegación inferior/lateral según ancho disponible;
  cambio de patrón al redimensionar no cambia el destino seleccionado.

## Estado, concurrencia y operación mobile

| Aspecto | Decisión o condición comprobable |
| --- | --- |
| Ciclo de vida | Recolección lifecycle-aware, cancelación estructurada; recreación no dispara escrituras ni respuestas nuevas |
| Estado temporal/persistente | Restaurar contexto mínimo en tarea recuperada según APP-02; apertura nueva en Home; favoritos en Room; no guardar ViewModels completos |
| Conectividad | Cada feature define carga/error/reintento; navegación y favoritos locales no requieren red para abrirse |
| Consistencia y migraciones | Dominio común y Room como verdad; migraciones explícitas; versión del esquema definida en favorites |
| Estados de interfaz | Navegación disponible durante errores; no sustituir fallos por ejemplos de contenido |
| Navegación/interrupciones | Home inicial, cuatro destinos y barra inferior/lateral; reselección conserva contexto, Back principal vuelve a Home/secundario al origen; quiz vuelve a Home conservando sesión |
| Teclado/foco/repetición | Navegación repetida no apila destinos idénticos accidentalmente; foco pasa al contenido/título pertinente |
| Pantallas/insets | Tamaño actual de ventana, vertical/horizontal y foldables; bisagra/oclusión y barras del sistema sin padding duplicado |
| Accesibilidad | Destino seleccionado anunciado, acciones etiquetadas y rutas accesibles sin gestos exclusivos |
| Permisos/capacidades | Red requiere permiso de manifiesto al incorporarla; no hay requisito de permisos runtime, sensores ni acceso a archivos públicos |
| Rendimiento | Operaciones fuera del hilo principal, UI responsiva; no precargar todo el catálogo por arrancar |
| Segundo plano | Sin requisito de trabajos programados; no introducir sincronización ni servicios por defecto |
| Privacidad | PokéAPI sin credenciales; no almacenar datos personales ni incluirlos en logs |
| Idiomas/formatos | Cambio español/inglés reconstruye textos a partir de recursos, conservando IDs y reglas |
| Validación mobile | Recorrer destinos, Back, rotación, recreación, cambio de idioma, postura y tamaño; ejecución por desarrollador |

## Dependencias incorporadas y fuentes oficiales

Se mantienen AGP 9.2.1, Kotlin 2.4.20, Compose BOM 2026.09.00, Lifecycle 2.11.0,
Gradle 9.4.1, daemon/toolchain JDK 21 y objetivo JVM 11. Plugins nuevos se
declaran en el catálogo; Kotlin JVM se aplica solo a domain, Serialization solo
a app y Hilt/KSP a app/data. No se aplica Kotlin Android ni kapt.

| Dependencia | Versión / uso | Referencia oficial consultada 2026-10-01 |
| --- | --- | --- |
| Navigation 3 | 1.2.0 estable; runtime/ui, listas de entradas y decoradores | [Releases](https://developer.android.com/jetpack/androidx/releases/navigation3), [decoradores](https://developer.android.com/guide/navigation/navigation-3/naventrydecorators) |
| Lifecycle | 2.11.0 existente; runtime-compose y viewmodel-navigation3 | [Releases](https://developer.android.com/jetpack/androidx/releases/lifecycle) |
| Hilt | 2.60.1; Application, Activity y módulos externos al dominio | [Setup KSP](https://dagger.dev/hilt/gradle-setup), [soporte AGP 9 desde 2.59](https://github.com/google/dagger/releases) |
| KSP | 2.3.12; procesador Hilt, requiere AGP ≥8.12; incluye correcciones AGP 9/Kotlin 2.4 de 2.3.10 | [Releases](https://github.com/google/ksp/releases) |
| Coroutines | 1.11.0; Flow/dispatchers en domain, Main Android en data | [Repositorio Kotlin](https://github.com/Kotlin/kotlinx.coroutines) |
| Serialization | plugin Kotlin 2.4.20; runtime JSON 1.11.0 para snapshot de tarea | [Releases Kotlin](https://github.com/Kotlin/kotlinx.serialization/releases) |
| WindowManager | 1.5.1 estable; WindowInfoTracker/FoldingFeature | [Releases](https://developer.android.com/jetpack/androidx/releases/window), [fold-aware](https://developer.android.com/develop/ui/compose/layouts/adaptive/foldables/make-your-app-fold-aware) |

AGP conserva [Kotlin integrado](https://developer.android.com/build/migrate-to-built-in-kotlin).
La compatibilidad conjunta exacta se verifica con el build del desarrollador;
no se ha resuelto ni compilado el grafo durante esta sesión.

## Pendientes técnicos y riesgos

Los contratos comunes y APIs del incremento están concretados en código.
Faltan implementaciones de repositorios, entradas/estado de features y entrega de
selección a compare mediante su owner retenido. `FeatureEntries` transporta solo
IDs/callbacks: esa integración debe evitar reejecutar selecciones al restaurar.
El codec recupera snapshot incompatible en Home con aviso explícito traducido.
Las preguntas/respuestas/criterios/scroll aún no existen: APP-T07 sigue parcial.
La retención de ViewModels y SavedState de entradas debe verificarse en dispositivo.
El filtro del design system se incorporó el 2026-10-05 con el SVG suministrado
en Home; la validación renderizada continúa pendiente.

Cerrar estos puntos en el incremento afectado y actualizar este plan sin alterar
la spec funcional. La preparación de PLAN/TASKS no resuelve por sí sola esas verificaciones.

## Estrategia de pruebas y revisión

Pruebas de navegación e integración para destinos, regreso al origen, acciones
repetidas y recreación según políticas cerradas. Pruebas de mappers/contratos para
ausencia y errores, y revisión estática de límites de módulos. Proponer un flujo de
UI crítico Home → detalle → favorito → favoritos; si se omite, registrar
justificación y evidencia alternativa, como permite el PDF.

El desarrollador realiza builds y pruebas según [el índice](../README.md). Las
versiones fijadas actualmente no equivalen a compatibilidad del stack futuro
verificada por compilación.

Usar JUnit 4, Coroutines Test y Turbine, fakes deterministas; MockK solo si aporta
valor. Escribir pruebas significativas, sin ejecutarlas durante sesión del agente.
El desarrollador ejecuta compilación y pruebas y entrega evidencia; no afirmar
que un criterio pasa solo porque su prueba está escrita.

## Comandos y validación del desarrollador

Los módulos/pruebas de los siguientes comandos ya están escritos. El agente no
ejecuta estos comandos. `connectedDebugAndroidTest`
solo aplica si hay pruebas instrumentadas y dispositivo/emulador preparado.

```powershell
.\gradlew.bat :core:domain:test
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat :app:assembleDebug
```

La prueba instrumentada usa entradas de fixture y verifica el host, no las
funcionalidades del producto. El flujo Home → detalle → favorito → favoritos se
escribirá al disponer de entradas y repositorio reales (APP-T09/APP-T10); aplazarlo
evita que fixtures acrediten persistencia u offline inexistentes. No se declara
omitido definitivamente. Registrar resultados/limitaciones en TASKS.md.

## Secuencia de entrega

Concretar pendientes y contratos → implementar el incremento en los archivos
responsables → escribir pruebas → revisar estáticamente → entregar validación
al desarrollador. El detalle y dependencias de cada paso están en TASKS.md.
