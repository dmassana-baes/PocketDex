# Plan técnico: Quiz: Who is that Pokémon?

Estado: **referencias locales inspeccionadas, ajuste visual e ilustraciones incorporados; ejecución pendiente**. Fecha: **2026-10-05**.

Contrato funcional: [SPEC.md](SPEC.md). Seguimiento: [TASKS.md](TASKS.md).
Módulo principal objetivo: `:feature:quiz`.
Referencias técnicas: [constitución](../../constitution.md) y [AGENTS.md](../../AGENTS.md).

## Punto de partida y dependencias

Existen app, domain/data/designsystem y Home/Detalle/Favoritos/Comparador.
QuizMediaRepository ya verifica y protege archivos en Room; reutilizarlo sin
añadir tablas ni modificar versiones. Crear únicamente `:feature:quiz`.

Base y design system; identidad/catálogo/estadísticas de core y sistema de protección de archivos definido junto a favoritos. No importar features para reutilizar repositorios.

## Arquitectura y entrega técnica

Crear `:feature:quiz` Android/Compose con Route/Screen/ViewModel/UiState. Generación,
evaluación y transición de sesión son reglas puras en domain con random inyectable;
data provee candidatos con nombres, tipos/estadísticas y medios locales verificados.
Depende de domain/designsystem, no de Home/Favoritos. App gestiona salida/entrada.

Ubicaciones objetivo: `core/domain/.../quiz`, contratos de candidatos/medios;
`core/data/.../quiz` y `image` compartido; `feature/quiz/.../presentation` y tests.
No añadir tablas de historial, cuenta, reloj de partida ni WorkManager.

Preparación única de sesión: seleccionar candidatos con pistas distinguibles,
fijar IDs/opciones/orden y verificar cinco imágenes antes de exponer pregunta 1.
Pin independiente para medios de sesión activa/recuperable, separado de favorito;
la caché evictable de Coil no basta. Limitar concurrencia de descarga/preparación,
cancelar trabajos estructurados y limpiar temporales sin liberar referencias vigentes.
Snapshot pequeño versionado con preguntas, respuestas, índice, feedback y visibilidad
de pistas; recuperar los mismos hechos/orden sin RNG ni nueva evaluación. Concretar
retención/reconciliación de pins al descartar tarea para no dejar protección permanente.

## Contratos y decisiones de implementación

- Fuente de candidatos: catálogo aprobado y datos de imagen/nombre por ID. No usar
  etiquetas visuales de ejemplo como respuesta ni acoplar a DTOs.
- Generador con aleatoriedad inyectable: sesión de cinco preguntas, cada una con
  cuatro opciones distintas y una correcta. QUIZ-01 confirmado: cinco IDs correctos
  distintos dentro de la sesión, preparación al inicio y repetición permitida entre
  sesiones. Distractores pueden reaparecer en otras preguntas, manteniendo unicidad
  dentro de cada pregunta y sin colisiones entre etiquetas visibles.
- Evaluación recibe identidad de pregunta y opción; ignora/rechaza respuestas si
  ya está respondida o no corresponde a la pregunta vigente.
- QUIZ-05 confirmado: puntuación derivada de respuestas aceptadas, no de clicks;
  un punto por correcta y cero por incorrecta, mostrar aciertos sobre cinco (0/5 a
  5/5), sin penalizaciones ni temporizador.
- ViewModel UDF → reglas/repositorios de dominio → datos; Screen recibe estado y
  callbacks de inicio, respuesta, avance, reintento, reinicio y regreso.
- QUIZ-02 confirmado: silueta/revelación sobre la misma imagen/identidad; opciones
  no se barajan por recomposición/rotación. Siguiente requiere respuesta bloqueada;
  Ver resultado requiere quinta respuesta. Feedback permanece visible hasta avanzar.
- QUIZ-06 confirmado: descargar y verificar las cinco imágenes antes de mostrar
  la primera pregunta. Conservar copias locales utilizables durante la sesión,
  incluida la recuperación de la tarea; una URL o una entrada evictable de Coil
  no garantiza este requisito. Una sesión preparada continúa sin conexión.
  Una nueva sesión offline requiere datos locales suficientes para cinco correctas
  distintas, cuatro opciones distinguibles por pregunta, imágenes y pistas válidas.
  Si faltan, mostrar insuficiencia de datos locales y reintento con conexión;
  no reducir preguntas/opciones ni repetir correctas para simular disponibilidad.
- QUIZ-04 confirmado: ofrecer Mostrar pistas a todos los usuarios como alternativa
  textual opcional al reto visual, con tipos y estadísticas reales de la variedad
  predeterminada. No incluir el nombre correcto ni su número nacional en las pistas,
  ni identificar la opción correcta. Mantener las mismas cuatro opciones y puntuación.
  Preparar preguntas cuyas pistas permitan distinguir la correcta de los distractores;
  si los datos no lo permiten, elegir otra candidata antes de iniciar o informar
  insuficiencia. No inventar hechos ni asumir que una descripción genérica de silueta
  permite resolver el juego. Pistas y control disponibles con TalkBack y texto ampliado.

La limpieza normal de caché no elimina imágenes necesarias para una sesión activa
o recuperable. Liberar esa protección al sustituir/descartar la sesión conservando
la protección independiente de favoritos. Si un archivo se pierde o resulta
corrupto, ofrecer recuperación conservando IDs, orden, respuestas y puntuación;
no regenerar una pregunta a mitad de sesión. Guardar referencias y estado pequeño,
no bitmaps en el estado restaurable. Preservar pistas mostradas al recrear la misma
pregunta; una pregunta nueva comienza con pistas ocultas. Traducir sus etiquetas
sin cambiar los hechos ni generar candidatos nuevos.

No introducir reloj o temporizador sin requisito. Los errores no completan sesiones
con respuestas ficticias. Verificar proveedor de imágenes y su disponibilidad real
antes de fijar la generación/carga.

## Contratos concretados para el incremento

### Corrección de RecentTaskInfo nullable (2026-10-05)

El desarrollador reportó un error de compilación en `QuizMediaRepositoryImpl.reconcile`:
`AppTask.taskInfo` devuelve `RecentTaskInfo?`. Se sustituye `map` por `mapNotNull`,
se lee una sola vez en una referencia local y se descarta la entrada si es nula
antes de consultar `taskId` (API 29+) o `persistentId` (API 24–28).
Según [AppTask.getTaskInfo](https://developer.android.com/reference/android/app/ActivityManager.AppTask#getTaskInfo()),
null indica que esa tarea ya no existe. No se usa `!!` ni se fabrica un ID.
El conjunto conserva `+ taskId` para proteger la tarea actual; siguen intactas
las reglas de liberación de owners sustituidos/huérfanos y la protección de favoritos.

Revisión estática de ambas ramas y del tratamiento de null completada; compilación
y ejecución pendientes. El desarrollador debe ejecutar
`.\gradlew.bat :app:assembleDebug` y
`.\gradlew.bat :core:data:connectedDebugAndroidTest` (requiere dispositivo/emulador),
incluido el caso existente `reconcileRemovesOrphanAndReplacedOwnersWithoutReleasingFavorites`.

Elegibilidad: estadísticas completas, tipos conocidos, imagen disponible y
nombres distinguibles en ambos idiomas. Excluir colisiones de nombres, también
por mayúsculas/espacios. Las pistas son tipos y las seis estadísticas; la firma
tipos/estadísticas de la correcta difiere de cada distractor. RNG inyectado
elige cinco correctas distintas y ordena cuatro opciones una sola vez.

Datos: intentar primero el pool local con imágenes verificadas; si es insuficiente,
consultar una página de hasta 30 candidatos del catálogo nacional con offset
aleatorio y concurrencia existente acotada. Nunca descargar todo el catálogo para
quiz. Si el pool no permite cinco preguntas válidas, informar insuficiencia.

Snapshot JSON v1 en SavedStateHandle del ViewModel retenido por app/Activity:
ID de sesión, cinco preguntas con hechos/opciones, respuestas, índice, finalización
y pistas visibles. Validar formato e invariantes al restaurar, sin RNG ni evaluar
respuestas otra vez. No incluir bitmaps; archivos se revalidan mediante media.
Eventos incluyen ID de sesión e índice: respuestas/avance obsoletos se ignoran.

Owners de medios `quiz:<taskId>:<uuid>`. Reconciliar al asociar el owner de app:
liberar sesiones sustituidas de esa tarea y owners de tareas que ActivityManager
ya no registra; preservar sesiones de otras tareas recuperables y favoritos.
Al preparar reemplazo, mantener la sesión anterior hasta éxito y persistir nueva
sesión antes de liberar owner anterior. En cancelación liberar solo owner nuevo
no publicado. Error de imagen recupera las mismas cinco correctas y hechos,
sin regenerar preguntas. Guardar las copias verificadas también como caché de
datos compartidos para permitir otra sesión offline cuando haya elegibilidad.

App comunica lifecycle RESUMED/ON_PAUSE al owner sin pasar Activity/Context al
ViewModel. Si la preparación termina en segundo plano, conserva ambos owners y
espera a RESUMED antes de publicar snapshot/reemplazar/liberar el anterior; así
evita escrituras de SavedStateHandle después del guardado de la Activity que
perderían la protección de la sesión recuperable. Respuestas/avance/pistas se
ignoran fuera de foreground. La UI solo habilita opciones con imagen Ready,
además del bloqueo de la pregunta; Loading/Error no permiten puntuar.

Entrada/salida: Route notifica enter/leave; salir conserva sesión y volver presenta
Continuar/Reiniciar. Back visible/sistema vuelve a Home. Resultados son destino
secundario por ID; ViewModel compartido evita sesiones distintas por entrada.
Confirmación solo al reiniciar sesión activa con alguna respuesta, no en resultado.

Consulta `get_design_context` 31:2653 rechazada por cuota Starter el 2026-10-05.
Las tres capturas locales suministradas después completan esa inspección.
ASSETS registra referencias/adaptaciones y T15 incorpora los originales de portada
y confeti; no se acredita fidelidad completa en dispositivo ni ejecución.

## Incremento visual según capturas locales

El usuario suministró `assets/screens_design/Quiz.png`, `Quiz - Question.png`
y `Quiz - Result.png`. Inspeccionar los tres originales permite cerrar la parte
de referencia de T07. T12/T13/T14 ajustan portada sin cabecera adicional,
cabecera centrada, imagen 260 dp, opciones con borde Gray200, correcta amarilla,
feedback y avance con icono Refresh del DS. Resultado final permanece independiente
de la revelación de una sola pregunta. Cuatro opciones, pistas, progreso y
reintentos se conservan conforme a SPEC, aunque no aparezcan en las capturas.

Reutilizar tema/tokens, imágenes, iconos y botones del DS; componer una opción
local con Surface seleccionable para expresar borde/amarillo y mantener apariencia
tras bloqueo. No cambiar componentes globales ni ViewModel/datos/generación.
Conservar tamaño táctil 48 dp, texto que envuelve y scroll; columnas en estrecho
y distribución en fila cuando quepan imagen/opciones con texto ampliado.

T15 incorpora `assets/image 5.png` (624 × 458) como
`drawable-nodpi/quiz_intro_illustration.png` y `assets/image 12.png` (720 × 480)
como `quiz_confetti.png`, sin alterar los PNG transparentes suministrados.
Usar Image/painterResource y ContentScale.Fit: portada centrada hasta 312 dp,
proporción original y descripción localizada; confeti decorativo detrás del Pokémon
solo tras responder, sin semántica ni cambios de sesión. No recortar capturas ni
generar réplicas. ASSETS registra la procedencia. El cotejo real sigue en V02.

## Estado, concurrencia y operación mobile

| Aspecto | Decisión o condición comprobable |
| --- | --- |
| Ciclo de vida | Sin generar/responder/puntuar por recomposición; rotación/postura no repite respuestas; sesión restaurada conserva IDs/opciones/feedback |
| Estado temporal/persistente | Preservar sesión/opciones/orden/respuestas/posición al navegar/rotar y recuperar tarea según APP-02; apertura nueva sin sesión; no guardar historial permanente |
| Conectividad | Verificar las cinco imágenes y datos/pistas antes de iniciar; continuar offline; nueva sesión offline solo con candidatos/medios locales suficientes según QUIZ-06 |
| Consistencia/migraciones | IDs estables, una respuesta aceptada por pregunta y puntuación coherente; concretar formato de estado recuperable sin historial permanente y recuperación explícita de un estado incompatible |
| Estados | Inicio/carga/error, pregunta abierta, respondida con feedback/revelación y resultado; acciones coherentes con cada fase |
| Navegación/interrupciones | Back vuelve a Home conservando sesión; regresar ofrece Continuar/Reiniciar; confirmar antes de reiniciar progreso de una sesión activa |
| Teclado/foco/repetición | Sin formulario; tras responder opciones bloqueadas; avance/reinicio repetidos no crean saltos ni sesiones concurrentes |
| Pantallas/insets | Cuatro opciones alcanzables en vertical/horizontal/foldable con scroll si hace falta; cambios de postura no regeneran pregunta ni colocan controles sobre bisagra |
| Accesibilidad | Opciones etiquetadas y feedback anunciado; Mostrar pistas ofrece tipos/estadísticas reales a todos, sin identificar la correcta; accesible con TalkBack y texto ampliado según QUIZ-04 |
| Permisos/capacidades | Sin cámara, audio, sensores ni permisos runtime requeridos |
| Rendimiento | Imágenes acotadas y generación fuera del hilo principal si costosa; carga/precarga limitada según estrategia aprobada |
| Segundo plano | Sin trabajo programado ni temporizador implícito; operaciones en scopes cancelables |
| Privacidad | Datos públicos, respuestas temporales; sin analytics/historial remoto ni cuenta |
| Idiomas/formatos | Español/inglés según sistema; cambiar idioma traduce etiquetas/feedback sin regenerar IDs, opciones ni resultados |
| Validación mobile | Doble toque, respuesta tardía, fallo de imagen, Back, segundo plano, recreación, idioma y plegado/desplegado |

## Pendientes técnicos y riesgos

Contratos de candidatos/pistas, protección de medios, snapshot y recuperación
concretados e implementados en el incremento. Pendientes: cotejo visual, compilación y
ejecución mobile. 31:3078
es revelación, no resultado final. Elegibilidad insuficiente implica error
recuperable, no sesiones ambiguas o más cortas. Ver [ASSETS.md](ASSETS.md).

Cerrar estos puntos en el incremento afectado y actualizar este plan sin alterar
la spec funcional. La preparación de PLAN/TASKS no resuelve por sí sola esas verificaciones.

## Estrategia de pruebas y revisión

- Generación con random/fuentes fake: cinco preguntas, cuatro opciones distintas,
  inclusión única de correcta, cinco correctas distintas, catálogo insuficiente/
  faltantes y repetición válida entre sesiones. Si nombres visibles colisionan,
  nunca crear una pregunta ambigua.
- Dominio: correcta/incorrecta, segunda respuesta, opción ajena, pregunta obsoleta,
  avance, resultado y reinicio; puntuación no aumenta por doble evento.
- ViewModel con Coroutines Test/Turbine: preparación/éxito/error/reintento, imagen
  fallida, cancelación, respuestas repetidas, restauración, Continuar y confirmar/
  cancelar reinicio tras progreso activo.
- Compose/manual: cuatro opciones, bloqueo y feedback accesibles, ocultación antes
  de responder, rotación/idioma/postura y reapertura conforme a decisión.
- Preparación y medios: fallo en cualquiera de las cinco imágenes impide iniciar;
  sesión completa sin red, generación offline suficiente/insuficiente, limpieza de
  caché y restauración sin pérdida de imágenes protegidas; corrupción recuperable
  conserva progreso. Pistas verdaderas, distinguibles y sin revelar identidad,
  mismo orden/puntuación con y sin pistas y cambio de idioma sin regeneración.

Ejecución por desarrollador según [el índice](../README.md); no afirmar generación
válida o puntuación correcta únicamente a partir de un render.

Usar JUnit 4, Coroutines Test y Turbine, fakes deterministas; MockK solo si aporta
valor. Escribir pruebas significativas, sin ejecutarlas durante sesión del agente.
El desarrollador ejecuta compilación y pruebas y entrega evidencia; no afirmar
que un criterio pasa solo porque su prueba está escrita.

## Comandos y validación del desarrollador

Los módulos y pruebas están incorporados. El agente no ejecuta estos comandos.
El usuario pospuso compilación/pruebas hasta terminar implementaciones;
`connectedDebugAndroidTest` requiere dispositivo/emulador preparado.

```powershell
.\gradlew.bat :core:domain:test
.\gradlew.bat :core:data:testDebugUnitTest
.\gradlew.bat :core:data:connectedDebugAndroidTest
.\gradlew.bat :feature:quiz:testDebugUnitTest
.\gradlew.bat :feature:quiz:connectedDebugAndroidTest
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat :app:assembleDebug
```

Registrar entorno, comandos, resultado real y limitaciones en TASKS.md.
Comparar portada, pregunta y revelación con las tres capturas locales de ASSETS.md:
cabecera centrada, imagen de 260 dp, bordes finos y correcta amarilla solo después
de responder. Comprobar cuatro opciones bloqueadas, selección accesible, avance
explícito y resultado final separado. Repetir en es/en, texto al 200 %, horizontal
y foldable desplegado; confirmar scroll y ausencia de contenido oculto por insets.
Verificar cinco preguntas/cuatro opciones, doble toque/respuesta tardía/avance
repetido, 0/5–5/5, misma imagen antes/después y ausencia de identidad en TalkBack
antes de responder. Mostrar pistas, cambiar es/en, rotar, plegar/desplegar y texto
200 % sin alterar orden/hechos/respuesta/puntuación. Salir por Back/barra, volver
y Continuar; confirmar/cancelar reinicio con progreso y reiniciar resultado sin
confirmación. Matar proceso con tarea recuperable frente a apertura nueva; verificar
owner/taskId y revalidación de archivos. Preparar online y finalizar offline;
nueva sesión offline suficiente/insuficiente; corromper una copia local y reintentar
conservando las mismas preguntas/progreso. Reconciliar tarea descartada sin perder
favoritos ni imágenes de otra tarea recuperable. FAV-T02 conserva generación y
versionado manual de JSON Room v1.

El cotejo de ciclo de vida debe incluir reinicio de A a B cuya preparación termine
tras pausar la Activity: mientras esté pausada, snapshot A y sus pins permanecen;
al reanudar se publica B y libera A. Matar proceso antes de reanudar recupera A;
reconciliación elimina únicamente la preparación B no publicada. Comprobar opciones
deshabilitadas con imagen Loading/Error y callback saliente de Quiz sin cambiar
un destino nuevo. Hay regresiones escritas, no ejecutadas, para estos escenarios.

Fuentes de APIs consultadas: [SavedStateHandle](https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-savedstate),
[ActivityManager/recientes](https://developer.android.com/reference/android/app/ActivityManager.RecentTaskInfo),
[Compose/Coil](https://coil-kt.github.io/coil/compose/) y
[PokéAPI](https://pokeapi.co/docs/v2#pokemon). Reutilizar versiones declaradas;
la revisión documental no acredita compilación.

## Secuencia de entrega

Concretar pendientes y contratos → implementar el incremento en los archivos
responsables → escribir pruebas → revisar estáticamente → entregar validación
al desarrollador. El detalle y dependencias de cada paso están en TASKS.md.
