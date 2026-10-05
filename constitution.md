# Constitución técnica de PocketDex

Versión: **1.4.1**  
Fecha de aprobación: **2026-09-30**  
Estado: **decisiones acordadas; arquitectura objetivo parcialmente implementada**.

## Propósito y fuentes

Establecer un stack y una arquitectura mantenibles, testeables y explicables para
la Pokédex Android descrita en [la definición](docs/pocket_dex_definition.pdf).
Esta constitución fija decisiones técnicas. [AGENTS.md](AGENTS.md) define el
proceso de trabajo y las restricciones de ejecución; las
[reglas generales](docs/GENERIC_RULES.md) y
[guías mobile](docs/MOBILE_GUIDELINES.md) complementan ambos documentos.

El PDF es la fuente de requisitos funcionales y entregables. El
[Figma indicado en el PDF](https://www.figma.com/design/0M4IjLeiuGIFocB4qDQuRV/Pokedex-App---UI?node-id=13-285)
es la referencia visual de estructura, jerarquía, espaciado, componentes e imágenes;
no debe usarse para eliminar requisitos funcionales. Los comportamientos faltantes
se consultan y se documentan en las especificaciones antes de implementarlos.

## Estado actual y configuración base

Existen `:app` con host Navigation 3/adaptativo, `:core:designsystem` con tema claro,
tokens/componentes/recursos, `:core:domain` con modelos/contratos y `:core:data` con
DI de ejecución, Retrofit/Serialization, Room y repositorios de catálogo/favoritos.
Existe `:feature:home` con listado/búsqueda/filtro y marcas persistentes. Hay pruebas
escritas; filtro incorporado y validación manual pendiente. Detalle y Favoritos
incorporados; medios compartidos para quiz implementan prepare/restore/release.
Comparador incorpora owner retenido por app, dos slots/selector/resultados. Quiz
incorpora cinco preguntas/cuatro opciones, snapshot de tarea y medios protegidos
independientes de favoritos. JSON Room v1 y ejecución pospuestos por el usuario;
Quiz ajustado según capturas locales con logo/confeti originales integrados; cotejo en ejecución pendiente.

Valores observados en los archivos de configuración, no resultados de una
compilación ni comprobaciones del entorno instalado:

| Configuración | Valor declarado | Fuente |
| --- | --- | --- |
| Android Gradle Plugin | 9.2.1 | `gradle/libs.versions.toml` |
| Gradle Wrapper | 9.4.1 | `gradle/wrapper/gradle-wrapper.properties` |
| Versión del catálogo Kotlin / plugin Compose | 2.4.20 | `gradle/libs.versions.toml` |
| Compose BOM | 2026.09.00 | `gradle/libs.versions.toml` |
| SDK mínimo | 24 | `app/build.gradle.kts` |
| SDK objetivo | 37 | `app/build.gradle.kts` |
| SDK de compilación | 37 | Builds de app/core Android/features |
| Toolchain del daemon Gradle | JDK 21 | `gradle/gradle-daemon-jvm.properties` |
| Compatibilidad Java de origen y destino | 11 | `app/build.gradle.kts` |
| Namespace / application ID | `com.example.pocketdex` | `app/build.gradle.kts` |

Conservar esta base salvo que un cambio acordado requiera modificarla. Distinguir
el JDK que ejecuta Gradle del objetivo Java/JVM del código. No inferir la versión
del compilador Kotlin integrado en AGP únicamente a partir del alias del plugin
Compose. Al configurar nuevos módulos, alinear sus objetivos Java y Kotlin.

## Stack acordado

| Área | Tecnología y responsabilidad | Estado actual |
| --- | --- | --- |
| Lenguaje y UI | Kotlin, Jetpack Compose y Material 3 | Presentes |
| Presentación | MVVM con UDF, ViewModel y `UiState` inmutable en `StateFlow` | Patrón pendiente |
| Asincronía | Coroutines y Flow; APIs `suspend` y recolección con ciclo de vida | Contratos de dominio y WindowInfoTracker incorporados |
| Navegación | Navigation 3, claves tipadas y argumentos mínimos | Host implementado; integración con features pendiente |
| Inyección | Hilt, inyección por constructor y procesamiento con KSP | Configuración y DI de ejecución incorporadas |
| API | PokéAPI v2, sin credenciales privadas | Pendiente |
| Red | Retrofit y OkHttp | Pendiente |
| JSON | Kotlin Serialization | Snapshot de tarea; DTOs de red pendientes |
| Persistencia | Room y procesamiento con KSP | Pendiente |
| Imágenes | Coil con integración Compose | Pendiente |
| Carga incremental | Paging 3 | Pendiente |
| Pruebas unitarias | JUnit 4, Coroutines Test y Turbine; fakes y MockK cuando aporte valor | JUnit 4: dominio/navegación/DS; Coroutines Test/Turbine se incorporarán con comportamientos asíncronos |
| Pruebas de UI | Compose UI Test y AndroidX Test | DS y host con fixtures escritos; sin ejecutar |

Incorporar dependencias al implementar la funcionalidad que las necesita. Fijar
versiones compatibles en el catálogo después de consultar documentación oficial;
no usar versiones dinámicas ni duplicar bibliotecas para la misma responsabilidad.
La selección del stack no equivale a compatibilidad verificada por compilación.

AGP 9 proporciona Kotlin integrado en módulos Android: no añadir
`org.jetbrains.kotlin.android` sobre esa configuración ni `kotlin-kapt`.
Usar KSP para Hilt y Room. El módulo de dominio Kotlin/JVM utilizará el plugin
Kotlin JVM; los módulos Android usarán el plugin Android correspondiente y los
módulos Compose, además, el plugin Compose. Verificar compatibilidad de plugins,
procesadores y compilador al incorporarlos, sin alterar la base para ocultar fallos.

## Arquitectura y límites de módulos

Seguir Clean Architecture con MVVM y flujo unidireccional. Elegir módulos Gradle
por funcionalidad y tres módulos compartidos. El siguiente mapa es el objetivo
aprobado; actualmente existen app, los tres core y `:feature:home`.

| Módulo | Responsabilidad | Dependencias internas permitidas |
| --- | --- | --- |
| `:app` | Entrada, composición de dependencias y navegación global | Features y módulos core necesarios |
| `:core:domain` | Modelos, contratos de repositorios y reglas/casos de uso de negocio | Ninguna |
| `:core:data` | Repositorios concretos, fuentes remotas/locales, DTOs, entidades y mappers | `:core:domain` |
| `:core:designsystem` | Tema, tokens visuales y componentes Compose reutilizables | Ninguna |
| `:feature:home` | Listado, búsqueda, filtros y presentación de paginación | `:core:domain`, `:core:designsystem` |
| `:feature:detail` | Detalle y acciones sobre favoritos | `:core:domain`, `:core:designsystem` |
| `:feature:compare` | Selecciones y comparación de estadísticas | `:core:domain`, `:core:designsystem` |
| `:feature:quiz` | Presentación de preguntas, respuestas, avance y resultados | `:core:domain`, `:core:designsystem` |
| `:feature:favorites` | Lista de favoritos, vacío y acceso a detalles almacenados | `:core:domain`, `:core:designsystem` |

El dominio es Kotlin/JVM y no depende de Android, Compose, Hilt, Room, Retrofit ni
tipos de Paging. Coroutines y Flow son admisibles para sus contratos asíncronos.
Las implementaciones de datos dependen de los contratos del dominio. La
composición mediante Hilt ocurre fuera del dominio.

Las features no dependen de `:core:data`, de `:app` ni unas de otras. Exponen puntos
de entrada Compose con callbacks; `:app` coordina la navegación y provee las
implementaciones mediante inyección. No pasar objetos completos, DTOs ni entidades
de base de datos como argumentos de navegación; usar identificadores y parámetros
pequeños tipados.

Mantener `PagingSource`, `Pager`, `PagingData` y adaptadores de la UI en la capa
de presentación de la funcionalidad que los necesita. Acceder a páginas mediante
contratos de dominio y repositorios; no filtrar la infraestructura de Paging al
dominio ni abrir acceso directo a red o DAOs desde una feature.

Encapsular red y almacenamiento dentro de `:core:data`, con paquetes separados.
Mantener detalles de implementación `internal` o `private` cuando sea posible.
No crear por ahora módulos adicionales de red/base de datos ni frameworks de MVI.
Crear casos de uso que expresen reglas o coordinación real; evitar envoltorios sin
responsabilidad propia y abstracciones especulativas.

## Estado, datos y comportamiento mobile

- La UI representa estado y envía acciones explícitas al ViewModel. Los ViewModels
  exponen `StateFlow` de estado inmutable; los composables recolectan mediante
  `collectAsStateWithLifecycle`. No guardar referencias a Activity o Context.
- Representar carga inicial, carga incremental, contenido, vacío y errores donde
  corresponda. Conservar feedback observable y recuperación; no fabricar datos
  válidos ante un fallo. Traducir errores de infraestructura a resultados que el
  dominio y la presentación puedan manejar.
- Usar concurrencia estructurada, dispatchers inyectables donde se necesiten y
  operaciones de red/almacenamiento seguras para el hilo principal. Propagar
  cancelación y definir en cada spec qué ocurre al abandonar una pantalla.
- Room es la fuente de verdad para favoritos y datos persistidos. Observar cambios
  para mantener Home, detalle y favoritos consistentes. Preservar datos mediante
  migraciones explícitas; no usar migraciones destructivas que borren favoritos.
- Los favoritos sobreviven al reinicio y sus datos ya almacenados son consultables
  sin conexión. Definir en la spec qué datos e imágenes se retienen y qué se muestra
  si faltan; no asumir que una caché de imágenes garantiza disponibilidad permanente.
- Cada spec resuelve restauración al navegar, rotar, volver del segundo plano y
  recrear el proceso, distinguiendo estado temporal de persistencia. No persistir
  todas las pantallas ni introducir trabajo en segundo plano sin un requisito.
- Mantener fidelidad visual al Figma y aplicar adaptación, insets, escalado de texto,
  accesibilidad y recursos de texto. Se acuerda tema claro según Figma, español e
  inglés según el idioma del sistema y orientación vertical/horizontal con
  adaptación para teléfonos foldables. El design system concreta paleta, contraste,
  fuente local y componentes; verificar procedencia de iconos antes de incorporarlos.
  Usar el primer tipo por slot remoto como acento principal, mostrando ambos tipos.
  La escala visual de estadísticas es 255, sin alterar valores reales ni declararla
  máximo comprobado del catálogo. Navegación inferior con ancho de ventana <600 dp
  y lateral desde 600 dp, respetando bisagras y espacio disponible.

## Requisitos y estrategia de pruebas

Preservar los requisitos del PDF: búsqueda por nombre o número sin distinguir
mayúsculas y filtros combinados con AND; limpieza de criterios y estados de carga,
vacío, error y reintento; detalle con imagen, nombre, número, tipos, estadísticas
y favoritos; comparador de exactamente dos Pokémon distintos, seis estadísticas,
identificación del mayor valor y reemplazo/eliminación de selecciones; quiz de
cinco preguntas y cuatro opciones únicas con una correcta, respuesta bloqueada,
revelación, avance, puntuación y reinicio; favoritos persistentes con vacío
accionable y consulta offline de datos almacenados.

Los siete specs recogen las decisiones de producto aprobadas de búsqueda por
número exacto, catálogo por especie/variedad predeterminada, un tipo, caché,
restauración de tarea, empates y generación del quiz. Aplicar esas decisiones;
consultar nuevas dudas de comportamiento antes de implementar, sin supuestos silenciosos.

Los acuerdos compartidos incluyen número con `#` y mínimo tres dígitos, inglés
de respaldo, ajustes mínimos de accesibilidad y navegación inferior en ventanas
estrechas/lateral en amplias. Recuperar la misma tarea conserva contexto; apertura
nueva empieza en Home con estado temporal nuevo. Los favoritos se guardan de
inmediato, con imagen protegida cuando se descarga; desmarcar conserva datos e
imagen en caché normal. Su orden es incorporación más reciente primero y su
actualización muestra datos locales antes de refrescar con app abierta. El quiz
prepara/verifica cinco imágenes antes de iniciar, continúa offline y ofrece
pistas opcionales de tipos/estadísticas para todos. Nueva sesión offline requiere
datos y medios locales suficientes. Estos acuerdos no añaden trabajo programado
en segundo plano ni historial permanente de sesiones. Los detalles de contratos,
esquema, caché y restauración se concretan respetando los límites de módulos.

Escribir pruebas de reglas de negocio, estado y errores usando el stack acordado:

- Búsqueda y filtros: nombre/número, mayúsculas, AND, limpieza y ausencia de resultados.
- Favoritos: marcar/desmarcar, consistencia, persistencia y lectura de datos offline.
- Comparador: duplicados, reemplazo, eliminación y comparación de las seis estadísticas.
- Quiz: cinco preguntas, opciones únicas, puntuación, bloqueo de respuestas y reinicio.
- ViewModels/casos de uso: carga, éxito, vacío, error, reintento y cancelación pertinente.

Preferir fakes deterministas; inyectar red, persistencia, dispatchers y
aleatoriedad/reloj cuando aplique. Las pruebas reales de Room y de reapertura deben
complementar los fakes para demostrar persistencia. La prueba de UI de un flujo
crítico es valorada, pero opcional según la justificación admitida por el PDF.
Aplicar verificaciones manuales de conectividad, interrupciones, accesibilidad y
adaptación visual según los criterios de aceptación de cada funcionalidad.

Los agentes escriben y revisan pruebas, pero no las ejecutan, ni compilan o lanzan
la app. La ejecución y evidencia quedan a cargo del desarrollador, conforme a
[AGENTS.md](AGENTS.md). No afirmar cobertura ni validación de comportamiento a
partir de los ejemplos del proyecto o de comandos sin ejecutar.

## Gobierno de decisiones

Mantener documentación en español e identificadores de código en inglés. Ante
un cambio de stack o límites de módulos, acordar el cambio, actualizar esta
constitución y las specs afectadas, y registrar fecha, motivo y consecuencias.
Incrementar versión mayor por cambios incompatibles de arquitectura, menor por
nuevas decisiones compatibles y parche por aclaraciones sin cambio de decisión.

| Fecha | Versión | Decisión y motivo | Consecuencia |
| --- | --- | --- | --- |
| 2026-09-30 | 1.0.0 | Base aprobada: features, tres módulos core, MVVM/UDF y stack acordado | Guía para la implementación futura; esta etapa crea únicamente documentación |
| 2026-09-30 | 1.0.0 | Se descarta la propuesta inicial de un solo módulo a favor de la separación por funcionalidad elegida por el usuario | Más configuración Gradle a cambio de límites explícitos y aislamiento funcional |
| 2026-09-30 | 1.0.0 | Se acuerdan tres módulos core y Paging 3 | Red/base de datos se encapsulan en data; carga y reintentos usan Paging |
| 2026-09-30 | 1.1.0 | Harness local aprobado: seis skills y cuatro agents, con delegación automática cuando aporte valor | Fuentes en skills/, copias Codex en .agents/skills y perfiles nativos; trabajo y terminal secuenciales, sin builds ni pruebas ejecutadas por agentes |
| 2026-09-30 | 1.2.0 | Usuario confirma design system Compose primero, tema claro, español/inglés según sistema y vertical/horizontal con adaptación para teléfonos foldables | Siete specs por área y base compartida; bibliotecas y funcionalidades siguen pendientes de implementación; decisiones abiertas registradas en specs |
| 2026-09-30 | 1.3.0 | Usuario cierra decisiones de los siete specs, incluidas caché/orden/refresco de favoritos, recuperación de tarea, quiz offline y pistas accesibles | Producto definido; concretar contratos y verificaciones técnicas antes de cada incremento, comenzando por design system; implementación y ejecución pendientes |
| 2026-09-30 | 1.4.0 | Usuario confirma primer tipo como acento, referencia visual de barras 255 y navegación lateral desde 600 dp | Design system concreta foundations y estados; verificar procedencia de iconos, artefactos locales y ejecución al implementar; límites de módulos y stack se conservan |
| 2026-09-30 | 1.4.1 | Usuario separa documentación funcional, plan técnico y tareas por cada área | SPEC.md conserva comportamiento/aceptación; PLAN.md arquitectura/contratos/pruebas; TASKS.md trabajo y evidencia. Sin cambio de stack, arquitectura ni decisiones funcionales |

El harness local y seis skills reutilizables en `skills/` están creados; su
estructura, procedencia y activación se describen en
[CODEX_HARNESS.md](docs/CODEX_HARNESS.md). [El índice de specs](specs/README.md)
organiza siete specs con decisiones de producto cerradas y pendientes técnicos antes de implementar.
Cada área tiene SPEC.md funcional, PLAN.md técnico y TASKS.md de implementación.
Registrar decisiones técnicas y verificación en PLAN, sin mezclarlas con el contrato
funcional; conservar avance y evidencia de tareas en TASKS.
README reproducible existe para el incremento de base; actualización final,
verificación y APK generado por el desarrollador siguen pendientes.
La carpeta actual no es un repositorio Git; la entrega podrá usar
Git o ZIP según el PDF. Mantener identidad genérica y ausencia de credenciales,
secretos y datos personales en todos los artefactos.

## Referencias técnicas

- [Recomendaciones de arquitectura Android](https://developer.android.com/topic/architecture/recommendations).
- [Patrones de modularización](https://developer.android.com/topic/modularization/patterns).
- [Navigation 3](https://developer.android.com/guide/navigation/navigation-3).
- [Paging 3](https://developer.android.com/topic/libraries/architecture/paging/v3-overview).
- [Kotlin integrado en AGP](https://developer.android.com/build/migrate-to-built-in-kotlin).
- [Dagger/Hilt con KSP](https://dagger.dev/dev-guide/ksp.html).
- [Persistencia con Room](https://developer.android.com/training/data-storage/room).
- [Coil para Compose](https://coil-kt.github.io/coil/compose/).
- [Documentación de PokéAPI v2](https://pokeapi.co/docs/v2).
