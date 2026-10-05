# PocketDex

Aplicación Android de Pokédex definida en [el PDF](docs/pocket_dex_definition.pdf)
y [siete specs](specs/README.md), con diseño de referencia Figma. Estado actual:
design system, contratos compartidos, DI, host de navegación, Home, Detalle,
Favoritos, Comparador y Quiz incorporados.
Home usa catálogo real, búsqueda/filtro global, páginas, caché local y marcas de
favoritos persistentes. Detalle muestra caché primero, actualiza por especie ID,
observa favoritos y presenta seis estadísticas con faltantes explícitos.
Favoritos observa la lista local, actualiza al abrir y conserva orden/scroll.
Comparador permite dos especies distintas y seis estadísticas con empate.
Quiz prepara cinco preguntas con cuatro opciones, imágenes verificadas, pistas,
feedback, puntuación y recuperación de sesión. Ajuste visual según capturas locales
incorporado, con logo y confeti originales integrados.
Ejecución aún no verificada.
El icono de filtro suministrado está convertido a VectorDrawable e integrado.

## Preparar un entorno limpio

1. Instalar Android Studio compatible con AGP 9.2.1 y Android SDK Platform 37.
   Instalar los componentes solicitados por SDK Manager para ese SDK.
2. Usar JDK 21 para Gradle. El wrapper fija Gradle 9.4.1; el archivo de criterios
   del daemon y la toolchain JVM fijan Java 21, con objetivo de bytecode Java 11.
3. Abrir la raíz y configurar la ruta de SDK en Android Studio o en un
   `local.properties` local (`sdk.dir=...`). Ese archivo depende de la máquina y
   no forma parte de la entrega reproducible. No se necesitan tokens/credenciales.
4. Sincronizar Gradle con acceso a Google Maven/Maven Central/Gradle Plugin Portal.
   Las versiones están en [el catálogo](gradle/libs.versions.toml). No añadir
   Kotlin Android ni kapt: los módulos Android usan Kotlin integrado en AGP 9.

Los pasos y comandos siguientes corresponden al desarrollador. Los agentes no
compilan, empaquetan, ejecutan la app ni pruebas, según [AGENTS.md](AGENTS.md).
La revisión estática no acredita que el proyecto haya compilado.

## Compilar y verificar manualmente

Desde la raíz, en PowerShell, ejecutar secuencialmente:

```powershell
.\gradlew.bat :core:domain:test
.\gradlew.bat :core:data:testDebugUnitTest
.\gradlew.bat :feature:home:testDebugUnitTest
.\gradlew.bat :feature:detail:testDebugUnitTest
.\gradlew.bat :feature:favorites:testDebugUnitTest
.\gradlew.bat :feature:compare:testDebugUnitTest
.\gradlew.bat :feature:quiz:testDebugUnitTest
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :core:designsystem:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat :core:designsystem:connectedDebugAndroidTest
.\gradlew.bat :core:data:connectedDebugAndroidTest
.\gradlew.bat :feature:home:connectedDebugAndroidTest
.\gradlew.bat :feature:detail:connectedDebugAndroidTest
.\gradlew.bat :feature:favorites:connectedDebugAndroidTest
.\gradlew.bat :feature:compare:connectedDebugAndroidTest
.\gradlew.bat :feature:quiz:connectedDebugAndroidTest
```

Las tareas `connectedDebugAndroidTest` requieren dispositivo/emulador. La APK debug, si el build
termina correctamente, queda en `app/build/outputs/apk/debug/app-debug.apk`.
No hay APK generado ni resultados de ejecución acreditados en esta entrega.
Las pruebas de ejemplo heredadas no cubren requisitos del producto.

Las pruebas escritas de domain cubren consulta por nombre canónico/visible y número, AND de tipo y datos
parciales. App prueba reglas de navegación/snapshot/área útil y, con entradas de
fixture, conservación por destino y restauración del host. Dominio añade generación,
elegibilidad/pistas y evaluación del quiz; feature añade ViewModel, snapshot y UI.
Data añade pruebas de índice/DTO,
concurrencia/cancelación y Room en disco/reapertura/offline. Home añade Paging,
ViewModel (debounce, borrador, caché, favoritos, recuperación/reintento) y controles
Compose con callbacks de tarjeta/favorito separados. No se ejecutaron.

Home consulta PokéAPI y los CSV oficiales alojados en `raw.githubusercontent.com`
para buscar/filtrar antes de paginar; no precarga todos los detalles/imágenes.
Sin acceso a esos orígenes conserva lo descargado con cobertura parcial. La copia
privada de imágenes de favoritos se verifica antes de publicarse; Coil sirve las
imágenes de tarjetas. Room exporta su esquema v1 con KSP: después del build manual,
versionar `core/data/schemas/.../1.json` (ver [nota de esquema](core/data/schemas/README.md)).

Comprobaciones manuales del host: seleccionar/reseleccionar las cuatro áreas;
Back desde raíz no-Home a Home y salida normal desde Home; español, inglés y
otro idioma con respaldo inglés; tema claro con sistema oscuro; ventana de
599/600 dp; vertical/horizontal, redimensionamiento y foldables con bisagra
vertical/horizontal; barras del sistema/teclado, TalkBack y texto ampliado.
La recuperación de la misma tarea debe conservar el destino; una tarea nueva
empieza en Home. El resto del contexto se comprobará al integrar cada feature.
El modo «No conservar actividades» no equivale a matar/recuperar el proceso.

## Módulos y documentación

| Módulo | Responsabilidad actual |
| --- | --- |
| `:app` | Hilt Application/Activity, Navigation 3, snapshot de tarea, host adaptativo y recursos es/en |
| `:core:domain` | Kotlin/JVM puro, modelos/reglas de consulta, resultados y contratos de repositorios/ejecución |
| `:core:data` | Retrofit/Serialization, índice oficial, Room, repositorios y copias privadas de imágenes; DI de ejecución |
| `:core:designsystem` | Tema claro, tokens, componentes y recursos visuales; [procedencia](specs/design-system/ASSETS.md) |
| `:feature:home` | Catálogo paginado, búsqueda/filtro, favoritos observados, caché y recuperación de contexto |
| `:feature:detail` | Lectura local/actualización por ID, imagen/tipos/estadísticas, favoritos compartidos y regreso al origen |
| `:feature:favorites` | Lista reciente local, refresco acotado, eliminación confirmada, estado parcial/vacío/error y scroll por tarea |
| `:feature:compare` | Dos slots distintos, selector por nombre/número con páginas, caché/datos completos y seis resultados por clave |
| `:feature:quiz` | Cinco preguntas/cuatro opciones, silueta/pistas/feedback, resultado, snapshot y sesión offline con medios protegidos |

Los módulos de funcionalidades dependen de domain/designsystem. App
coordina sus callbacks; no se permiten dependencias entre funcionalidades ni
acceso directo a data desde ellas. Consultar [constitution.md](constitution.md),
[PLAN de base](specs/app-foundation/PLAN.md) y
[TASKS de base](specs/app-foundation/TASKS.md) para decisiones y pendientes.
Las skills reutilizables están en `skills/`; el
[harness](docs/CODEX_HARNESS.md) documenta su sincronización y restricciones.

Para validar Home: primera carga y varias páginas, coincidencias fuera de la
primera página, búsqueda #/ceros y AND, Aplicar/cancelar/restablecer, errores y
reintentos, red lenta con caché, reapertura offline, marcar/desmarcar y fallos de
descarga. Comprobar recuperación de tarea con scroll avanzado, idioma, teclado,
TalkBack, texto ampliado y posturas. Consultar [tareas Home](specs/home/TASKS.md).
Home→Detalle→Back está integrado. Detalle incluye pruebas escritas de ViewModel,
semántica Compose y lectura Room con reapertura offline. Verificar con red lenta,
imagen fallida, estadísticas parciales, favoritos, es/en, texto ampliado y foldables;
consultar [tareas Detalle](specs/pokemon-detail/TASKS.md). Figma agotó la cuota MCP;
cotejo visual en dispositivo pendiente. Favoritos→Detalle→Back y Explorar Home
están conectados. Pruebas adicionales escritas de ViewModel, controles, orden,
copias verificadas y pins de quiz independientes.
Consultar [tareas Favoritos](specs/favorites/TASKS.md). JSON Room v1 sigue pendiente
del build/manual versionado. No se ejecutaron compilación ni pruebas.

Comparador conserva par/slot en un ViewModel retenido por app y consulta/scroll
del selector en su entrada. Pruebas de reglas, ViewModels y semántica escritas;
[TASKS Comparador](specs/compare/TASKS.md) registra implementación y ajuste visual;
[referencias locales](specs/compare/ASSETS.md) inspeccionadas y diferencias visuales
documentadas. El cotejo visual de la app y la verificación de ejecución siguen pendientes.
El desarrollador ejecutará compilación/pruebas y generación de esquema después
de terminar los specs, según su decisión del 2026-10-05.

Quiz conserva hechos/opciones/respuestas/pistas en snapshot JSON v1 del owner
Activity y revalida los mismos archivos al recuperar tarea. La preparación
terminada en segundo plano conserva ambos owners y espera a RESUMED para publicar
el reemplazo y liberar el anterior. Back vuelve a Home, regresar ofrece
Continuar/Reiniciar y solo confirma reinicio si pierde progreso activo. Pistas
no muestran nombre/número; silueta usa descripción neutra y opciones solo se
habilitan con imagen lista. [TASKS Quiz](specs/quiz/TASKS.md) registra implementación,
pruebas escritas y revisión independiente; T07 y T12–T14 cierran inspección local y
ajuste visual. T15 incorpora logo/confeti originales. Validar el cotejo
visual en dispositivo, preparación online/finalización offline, pool local
suficiente/insuficiente, corrupción/reintento sin cambiar preguntas, proceso,
idiomas, texto ampliado/TalkBack y posturas según [PLAN Quiz](specs/quiz/PLAN.md).
