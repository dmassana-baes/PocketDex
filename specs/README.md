# Especificaciones de PocketDex

Fecha: **2026-10-05**. Existen `:app`, `:core:designsystem`, `:core:domain` y
`:core:data`, `:feature:home`, `:feature:detail`, `:feature:favorites`, `:feature:compare` y `:feature:quiz`.
El design system contiene tema/componentes, iconos suministrados y
la ilustración y el filtro suministrado; validación de ejecución pendiente. La base incorpora
contratos, DI y host Navigation 3/adaptativo; integración con features y restauración
de las features restantes aún pendientes. Home incorpora catálogo, búsqueda/filtro,
caché y marcas de favoritos mediante infraestructura compartida. Avance en
[TASKS del design system](design-system/TASKS.md),
[TASKS de base](app-foundation/TASKS.md), [TASKS Home](home/TASKS.md) y
[TASKS Detalle](pokemon-detail/TASKS.md). Detalle integra lectura local/actualización,
seis estadísticas, favoritos y regreso. Favoritos añade lista local/refresco y
pins de medios; [TASKS Favoritos](favorites/TASKS.md) registra JSON v1 pendiente.
Comparador integra dos slots/selector/resultados; [TASKS Comparador](compare/TASKS.md)
registra inspección con capturas locales y ajuste visual implementado en T10–T12;
fidelidad visual en ejecución pendiente.
Quiz incorpora sesión/pistas/offline/restauración; [TASKS Quiz](quiz/TASKS.md)
registra inspección local y ajuste visual incorporados en T12–T14; T15 incorpora
logo/confeti originales. Validación de ejecución pendiente.

## Fuentes y acuerdos

- [PDF de producto](../docs/pocket_dex_definition.pdf): requisitos y entregables.
- [Constitución técnica](../constitution.md): stack y arquitectura acordados.
- [AGENTS.md](../AGENTS.md), [reglas generales](../docs/GENERIC_RULES.md) y
  [guías mobile](../docs/MOBILE_GUIDELINES.md): proceso y restricciones.
- [Figma de referencia](https://www.figma.com/design/0M4IjLeiuGIFocB4qDQuRV/Pokedex-App---UI?node-id=13-285): fuente visual.

El usuario eligió un spec por área funcional del PDF: Home incluye listado,
búsqueda y filtros. El design system será una biblioteca Compose basada en el
Figma existente, y se implementará antes que las funcionalidades. No se creará
una biblioteca adicional en Figma.

Acuerdos visuales/mobile confirmados el 2026-09-30: **solo tema claro**;
**español e inglés según el idioma del sistema**; **vertical y horizontal con
adaptación para teléfonos foldables**. Aplican a todos los specs. Los cambios de
idioma, tamaño de ventana y postura no deben producir acciones de negocio nuevas.
En el cierre de decisiones se confirmó número visible con `#` y mínimo tres
dígitos (`#001`, `#025`, `#1000`) e inglés de respaldo para otros idiomas del sistema.
También se confirmaron ajustes mínimos de tamaño/contraste para accesibilidad,
alcance funcional del PDF conservando el lenguaje visual del Figma y Home inicial
con barra inferior en ventanas estrechas/lateral en ventanas amplias, incluido
foldable desplegado. Random, filtros avanzados, ganador global y secciones extra de
detalle quedan fuera de esta versión.
Se confirmó conservar estado/scroll al cambiar o reseleccionar destinos, Back
principal a Home/secundario al origen y restaurar contexto cuando Android recupera
la tarea. Una apertura nueva inicia Home con estado temporal nuevo. El catálogo
incluye todas las especies de PokéAPI con variedad predeterminada, una entrada por
especie y orden por número nacional ascendente.
HOME-02 a HOME-04 confirmadas: nombre por contenido y número exacto (`25`/`025`/`#025`),
sin distinguir mayúsculas/espacios exteriores, un tipo seleccionado, búsqueda con
debounce 300 ms y panel de filtros con Aplicar/cierre que descarta el borrador.
La búsqueda del selector del comparador comparte estas reglas.
HOME-05 y FAV-01/FAV-02 confirmadas: datos descargados disponibles offline con
aviso de cobertura parcial; marca y datos disponibles de favorito guardados
inmediatamente, completado con app abierta y copia local protegida de su imagen
cuando se descarga correctamente. Fallos remotos conservan marca/datos y muestran
faltantes explícitos. FAV-03/FAV-04/FAV-05 confirmadas: desmarcar conserva datos e
imagen en caché normal; favoritos por incorporación más reciente primero; lectura
local inmediata y actualización al abrir con conexión, conservando contenido ante fallo.
COMPARE-01/COMPARE-03 y QUIZ-05 confirmadas: empate textual con aspecto neutro,
botón Comparar que abre resultados y regreso para editar selecciones; quiz con
aciertos sobre cinco, un punto por correcta/cero por incorrecta, sin temporizador.
QUIZ-01 a QUIZ-03 confirmadas: cinco correctas distintas, preparación al inicio,
silueta, Siguiente/Ver resultado después de feedback y Back conservando sesión.
Al regresar, Continuar o Reiniciar; confirmar si el reinicio pierde progreso activo.
QUIZ-04/QUIZ-06 confirmadas: pistas textuales opcionales de tipos y estadísticas
para todos, sin revelar nombre correcto ni número; descargar/verificar las cinco
imágenes antes de iniciar y continuar sin red. Otra sesión offline requiere
datos, pistas e imágenes locales suficientes.
Precisiones visuales DS-07/DS-08/DS-09 confirmadas: primer tipo como acento principal,
referencia fija de 255 para barras sin alterar valores reales y navegación lateral
desde 600 dp. El spec del design system incluye los 18 colores de tipo, pares de
contraste y variantes visuales; su PLAN.md conserva roles Material 3, APIs,
fuente oficial/OFL y procedencia de recursos.

Los requisitos del PDF y las decisiones de la constitución se distinguen de las
propuestas de implementación. **Producto cerrado** significa que el usuario ha
resuelto las preguntas de comportamiento de los siete specs; no acredita código
implementado, contratos técnicos definitivos ni validación de ejecución. Las
verificaciones técnicas pendientes se concretan antes del incremento afectado.

## Organización de cada área

Cada una de las siete carpetas contiene tres documentos:

- `SPEC.md`: objetivo, alcance, reglas, flujos/estados observables, decisiones
  funcionales, condiciones mobile y criterios de aceptación. Describe qué hace la app.
- `PLAN.md`: módulos/rutas objetivo, contratos, datos, estado/concurrencia,
  decisiones técnicas, pendientes/riesgos, estrategia de pruebas y comandos manuales.
  Describe cómo se realizará técnicamente, sin acreditar compatibilidad o ejecución.
- `TASKS.md`: trabajo desglosado con IDs, casillas, alcance, dependencias y resultado
  de cierre; verificación del desarrollador separada y registro de avance/evidencia.

SPEC es el contrato funcional. Los detalles técnicos se modifican en PLAN y el
progreso se registra en TASKS; no ajustar el contrato para ocultar una implementación
incorrecta. PLAN/TASKS se prepararon como planificación; ahora el design system
registra código y revisión estática, al igual que el primer incremento de base,
separando integración/assets pendientes y ejecución manual.

## Índice y dependencias

| Orden | Área / SPEC funcional | PLAN técnico | TASKS | Responsabilidad |
| --- | --- | --- | --- | --- |
| 1 | [Design system](design-system/SPEC.md) | [PLAN](design-system/PLAN.md) | [TASKS](design-system/TASKS.md) | Apariencia, componentes y adaptación compartidos |
| 2 | [Base de aplicación](app-foundation/SPEC.md) | [PLAN](app-foundation/PLAN.md) | [TASKS](app-foundation/TASKS.md) | Acceso a áreas, navegación y continuidad |
| 3 | [Home](home/SPEC.md) | [PLAN](home/PLAN.md) | [TASKS](home/TASKS.md) | Listado incremental, búsqueda y filtros |
| 4 | [Detalle](pokemon-detail/SPEC.md) | [PLAN](pokemon-detail/PLAN.md) | [TASKS](pokemon-detail/TASKS.md) | Información y estadísticas de un Pokémon |
| 5 | [Favoritos](favorites/SPEC.md) | [PLAN](favorites/PLAN.md) | [TASKS](favorites/TASKS.md) | Marcas persistentes y consulta offline |
| 6 | [Comparador](compare/SPEC.md) | [PLAN](compare/PLAN.md) | [TASKS](compare/TASKS.md) | Dos selecciones distintas y seis estadísticas |
| 7 | [Quiz](quiz/SPEC.md) | [PLAN](quiz/PLAN.md) | [TASKS](quiz/TASKS.md) | Cinco preguntas, respuestas y puntuación |

Este orden es de redacción. En implementación, preparar el design system primero;
después los contratos y la infraestructura necesarios para cada incremento.
Completar persistencia de favoritos antes de dar por terminadas sus acciones en
Home y Detalle. Ninguna feature depende de otra: los cruces de esta tabla son
dependencias de contratos/comportamientos, resueltos mediante core y `:app`.
El orden ejecutable está en TASKS: entregar infraestructura común de favoritos
antes de cerrar marcado/offline en Home y Detalle. El plan de base prepara core
primero y conecta las entradas reales de features conforme estén disponibles.

## Trazabilidad del PDF

Los identificadores de aceptación tienen prefijos únicos por área. Los IDs
técnicos históricos APP-AC01, APP-AC05 y APP-AC07 están en el PLAN de base; los
restantes criterios permanecen en los SPEC funcionales. Los requisitos sin numeración de las preguntas 6 y 7 reciben aquí
identificadores documentales; no son numeración original del PDF.

| Requisito | Spec responsable | Criterios de aceptación |
| --- | --- | --- |
| 1.1 Listado paginado/incremental | Home | HOME-AC01 |
| 1.2 Búsqueda por nombre/número sin distinguir mayúsculas | Home | HOME-AC02 |
| 1.3 Tipo y combinación AND | Home | HOME-AC03 |
| 1.4 Limpieza de búsqueda y filtros | Home | HOME-AC04 |
| 1.5 Carga inicial/incremental, vacío, error y reintento | Home | HOME-AC05, HOME-AC06 |
| 1.6 Abrir detalle | Home / base | HOME-AC07, APP-AC03 |
| 2.1 Imagen, nombre, número, tipos y estadísticas | Detalle | DETAIL-AC01 |
| 2.2 Marcar/desmarcar favorito | Detalle / favoritos | DETAIL-AC02, FAV-AC01 |
| 2.3 Regreso, carga y error | Detalle / base | DETAIL-AC03, DETAIL-AC04, APP-AC03 |
| 2.4 Fidelidad Figma y adaptación | Detalle / design system | DETAIL-AC05, DS-AC01, DS-AC04 |
| 3.1 Seleccionar exactamente dos desde pantalla dedicada | Comparador | COMPARE-AC01 |
| 3.2 Comparar seis estadísticas | Comparador | COMPARE-AC03 |
| 3.3 Indicar valor superior por estadística | Comparador | COMPARE-AC03 |
| 3.4 Reemplazar/quitar selecciones | Comparador | COMPARE-AC04 |
| 3.5 Impedir comparación consigo mismo | Comparador | COMPARE-AC02 |
| 4.1 Cinco preguntas con imagen oculta | Quiz | QUIZ-AC01 |
| 4.2 Cuatro opciones únicas, una correcta | Quiz | QUIZ-AC02 |
| 4.3 Bloquear respuesta tras selección | Quiz | QUIZ-AC03 |
| 4.4 Feedback, revelación y progreso | Quiz | QUIZ-AC04 |
| 4.5 Puntuación y reinicio | Quiz | QUIZ-AC05 |
| 5.1 Sección y acciones en Home/Detalle | Favoritos / Home / detalle | FAV-AC01, HOME-AC08, DETAIL-AC02 |
| 5.2 Persistencia al reiniciar | Favoritos | FAV-AC02 |
| 5.3 Vacío con acción para explorar | Favoritos / base | FAV-AC03, APP-AC04 |
| 5.4 Consulta offline de datos almacenados | Favoritos / detalle | FAV-AC04, DETAIL-AC06 |
| P6-BUS Pruebas de búsqueda/filtros | [PLAN Home](home/PLAN.md) | Estrategia de pruebas y HOME-T10 |
| P6-FAV Pruebas de favoritos/persistencia | [PLAN Favoritos](favorites/PLAN.md) | Room real/reapertura, FAV-T09/FAV-T10 |
| P6-COMP Pruebas de duplicados/reemplazo | [PLAN Comparador](compare/PLAN.md) | Estrategia de pruebas y COMPARE-T08 |
| P6-QUIZ Pruebas de puntuación/bloqueo | [PLAN Quiz](quiz/PLAN.md) | Estrategia de pruebas y QUIZ-T10 |
| P6-STATE Pruebas de ViewModels/casos de uso y errores | PLAN/TASKS de cada área | Estados funcionales trazados a pruebas técnicas |
| P6-UI Prueba de UI valorada, opcional con justificación | [PLAN Base](app-foundation/PLAN.md) / favoritos | Home → detalle → favorito → favoritos o justificación, APP-T10 |
| P6-DOUBLES Dobles coherentes para red, persistencia y aleatoriedad | [PLAN Base](app-foundation/PLAN.md) y planes funcionales | APP-AC05 técnico y pruebas con fakes deterministas |
| P7-SDD Requisitos, aceptación, supuestos, decisiones y plan | Este índice y siete conjuntos SPEC/PLAN/TASKS | Revisión documental descrita abajo |
| P7-AGENTS Contexto, arquitectura, comandos y límites | [AGENTS.md](../AGENTS.md) | Artefacto existente; mantener coherencia |
| P7-SKILL Al menos una skill reutilizable | [Harness](../docs/CODEX_HARNESS.md) y `skills/` | Seis skills existentes; conservar fuentes y copias sincronizadas |
| P7-CONTROL Código explicable, revisión de sugerencias, decisiones rechazadas | Todos los specs | Registro de decisiones y evidencia real al implementar |

Criterios adicionales de acuerdos confirmados: DS-AC07 para número/idioma de
respaldo, APP-AC08 para recuperación de tarea frente a apertura nueva y HOME-AC10
para catálogo por especie/variedad predeterminada. HOME-AC11 y HOME-AC12 verifican
debounce y Aplicar/descartar el borrador del filtro.
HOME-AC13 cubre caché parcial offline; FAV-AC09/FAV-AC10 guardado inmediato e imagen
protegida, y DETAIL-AC09 consulta parcial sin estadísticas fabricadas.
FAV-AC11 a FAV-AC13 cubren limpieza/orden reciente/refresco; HOME-AC14 y DETAIL-AC10
comprueban local primero y conservación de contenido al fallar la actualización.
QUIZ-AC10/QUIZ-AC11 cubren preparación de imágenes, sesiones offline y pistas
textuales opcionales accesibles sin identificar la respuesta correcta.

## Cierre de producto y pendientes técnicos

No quedan preguntas de producto pendientes tras las recomendaciones aprobadas
por el usuario el 2026-09-30. Los siguientes puntos son trabajo técnico previo a
la implementación del incremento correspondiente:

| Grupo | Pendiente técnico | Documento responsable |
| --- | --- | --- |
| DS | Verificar procedencia/licencias de iconos, artefactos locales y ejecución | [PLAN Design system](design-system/PLAN.md) |
| APP | Concretar contratos coordinados con consumidores y verificar APIs/compatibilidad | [PLAN Base](app-foundation/PLAN.md) |
| HOME | Exportación de esquema y verificación de ejecución/mobile | [PLAN Home](home/PLAN.md) |
| DETAIL | Contratos y pantalla incorporados; compilación, pruebas y verificación visual/mobile pendientes | [PLAN Detalle](pokemon-detail/PLAN.md) |
| FAV | Lista/pins/refresco incorporados; JSON v1, ejecución/mobile y futuras migraciones pendientes | [PLAN Favoritos](favorites/PLAN.md) |
| COMPARE | Implementación y ajuste visual incorporados; referencias locales inspeccionadas; fidelidad en ejecución, compilación/pruebas y mobile pendientes | [PLAN Comparador](compare/PLAN.md) |
| QUIZ | Sesión/pistas/offline/restauración, ajuste visual e ilustraciones incorporados; cotejo visual y ejecución pendientes | [PLAN Quiz](quiz/PLAN.md) |

Cada spec conserva las decisiones con prefijo propio. Si aparece una nueva duda
de producto, resolverla con el usuario y actualizar requisitos, escenarios y
fecha/fuente de la decisión. Los contratos de dominio deben cerrarse de forma coordinada antes de
implementar sus consumidores. Un cambio de arquitectura también exige actualizar
la constitución; no cambiarla para justificar una implementación incompatible.

## Entregables y revisión documental

| Entregable del PDF | Estado observado |
| --- | --- |
| Código fuente en Git o ZIP | Fuente local existente; carpeta sin repositorio Git; formato de entrega pendiente |
| APK | Pendiente; generación y verificación por el desarrollador |
| README reproducible de la aplicación | [README](../README.md) actualizado con módulos/comandos de las cinco funcionalidades; ejecución pendiente |
| `specs/` | Siete conjuntos SPEC/PLAN/TASKS y este índice; incrementos implementados, esquema/validación de ejecución pendientes |
| `AGENTS.md` | Existente |
| `skills/` | Seis skills existentes y harness documentado |

Antes de cerrar un conjunto: comprobar requisitos del PDF y aceptación funcional
en SPEC, contratos compatibles en PLAN, tareas/dependencias/cobertura en TASKS,
enlaces locales, éxito/error/interrupciones y aspectos mobile aplicables
y ausencia de decisiones pendientes presentadas como aprobadas. Marcar como no
aplicable un aspecto con su razón. No afirmar que el comportamiento pasa por tener
un criterio o un test escrito.

La creación de estos documentos no requiere pruebas artificiales. En la futura
implementación se escribirán pruebas con JUnit 4, Coroutines Test y Turbine,
preferentemente con fakes. La ejecución pertenece exclusivamente al desarrollador.

Comandos actuales desde la raíz, en PowerShell, a ejecutar secuencialmente por el desarrollador:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
.\gradlew.bat :core:domain:test
.\gradlew.bat :core:data:testDebugUnitTest
.\gradlew.bat :core:data:connectedDebugAndroidTest
.\gradlew.bat :core:designsystem:testDebugUnitTest
.\gradlew.bat :core:designsystem:connectedDebugAndroidTest
.\gradlew.bat :feature:home:testDebugUnitTest
.\gradlew.bat :feature:home:connectedDebugAndroidTest
```

Los módulos y sus pruebas ya están incorporados; ejecutar estos comandos
secuencialmente. Las tareas instrumentadas requieren un dispositivo o emulador:

```powershell
.\gradlew.bat :feature:detail:testDebugUnitTest
.\gradlew.bat :feature:favorites:testDebugUnitTest
.\gradlew.bat :feature:compare:testDebugUnitTest
.\gradlew.bat :feature:quiz:testDebugUnitTest
.\gradlew.bat :feature:detail:connectedDebugAndroidTest
.\gradlew.bat :feature:favorites:connectedDebugAndroidTest
.\gradlew.bat :feature:compare:connectedDebugAndroidTest
.\gradlew.bat :feature:quiz:connectedDebugAndroidTest
```

Los agentes no ejecutan ninguno de estos comandos ni empaquetan o publican la app.
Conservar resultados reales, versión y entorno al validar; las capturas únicamente
sirven como evidencia visual.
