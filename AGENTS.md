# Instrucciones para agentes de PocketDex

## Contexto y referencias

PocketDex es una aplicación Android de Pokédex. Su alcance incluye Home con
listado, búsqueda y filtros, detalle, comparador, quiz y favoritos persistentes.
La base contiene `:app` con host Navigation 3/adaptativo, `:core:designsystem`
con tema/componentes/recursos, `:core:domain` con contratos y `:core:data` con DI.
Las pruebas están escritas y el filtro está incorporado; validación manual pendiente.
Existe `:feature:home` con catálogo real, búsqueda/filtro, caché Room y marcas
persistentes. `:feature:detail` integra caché/actualización por ID, seis estadísticas,
favoritos compartidos y Back al origen. `:feature:favorites` añade lista local,
refresco, eliminación y scroll; data implementa pins prepare/restore/release de
medios para sesión quiz. `:feature:compare` añade par/selector/resultados
con owner retenido por app. `:feature:quiz` añade cinco preguntas/cuatro opciones,
pistas, sesión protegida offline y snapshot; ajuste según capturas locales incorporado,
logo/confeti originales integrados y validación de ejecución pendiente.
El usuario pospone compilación,
pruebas y esquema hasta terminar los specs; agentes siguen sin ejecutarlos.
Consultar TASKS para evidencia de implementación y límites de verificación.

Antes de trabajar, leer las referencias aplicables:

- [Definición del producto](docs/pocket_dex_definition.pdf): requisitos y entregables.
- [Reglas generales](docs/GENERIC_RULES.md): prácticas de trabajo y comunicación.
- [Guías mobile](docs/MOBILE_GUIDELINES.md): aspectos que debe resolver cada especificación.
- [Constitución técnica](constitution.md): stack, arquitectura y límites de dependencias.
- [Índice de especificaciones](specs/README.md): siete specs, trazabilidad del
  PDF y decisiones de producto cerradas; leer SPEC.md, PLAN.md y TASKS.md del área antes de implementar.
- [Figma de referencia](https://www.figma.com/design/0M4IjLeiuGIFocB4qDQuRV/Pokedex-App---UI?node-id=13-285): fuente visual indicada en el PDF.

Las instrucciones explícitas del usuario y las instrucciones de mayor prioridad
prevalecen sobre estos documentos. La política de verificación manual de este
archivo prevalece sobre recomendaciones de ejecutar builds o pruebas en las
guías, skills y workflows. Si hay contradicciones entre requisitos funcionales,
diseño y decisiones técnicas, consultar la parte afectada antes de implementarla.
No modificar requisitos para acomodar una implementación incorrecta.

## Política de terminal y verificación manual

- Usar una sola sesión de terminal o exec. Ejecutar comandos secuencialmente y
  confirmar que terminaron antes de iniciar otro. No abrir sesiones en paralelo.
- Nunca compilar, ensamblar, empaquetar, instalar ni ejecutar la aplicación durante
  una sesión del agente.
- Nunca ejecutar pruebas unitarias, de integración, UI, screenshots, cobertura,
  instrumentación o end-to-end.
- No ejecutar tareas Gradle como `build`, `assemble*`, `compile*`, `test*`,
  `connected*`, `install*` o `kover*`.
- Lint estático y formato están permitidos cuando el usuario los solicite,
  incluyendo Android lint, `ktlintCheck` y `ktlintFormat`, si están configurados.
  No usar estas tareas para lanzar indirectamente compilaciones o pruebas.
- No delegar a otro agente, skill, hook o workflow para eludir estas restricciones.
- Dejar la ejecución de compilaciones y pruebas al desarrollador. Si proporciona
  resultados, analizarlos sin volver a ejecutar los comandos.
- Al entregar una implementación, proporcionar los comandos exactos que el
  desarrollador debe ejecutar manualmente y señalar lo que sigue sin verificar.

## Flujo de trabajo y especificaciones

1. Revisar los documentos, código y configuración relevantes. Inspeccionar los
   cambios existentes antes de editar; si no hay repositorio Git, revisar los
   archivos directamente sin inicializar uno por iniciativa propia.
2. Para funcionalidades, mantener en `specs/<feature>/SPEC.md` el contrato
   meramente funcional: requisitos, flujos/estados, supuestos, decisiones de producto
   y criterios de aceptación. Registrar módulos, contratos, datos, pruebas y
   verificación técnica en `PLAN.md`; desglosar implementación, dependencias y
   resultados de cierre en `TASKS.md` de la misma carpeta antes de cambiar comportamiento.
   Los cambios solo documentales no requieren pruebas artificiales ni una spec adicional.
3. Resolver con el usuario las dudas que afecten comportamiento o aceptación.
   Consultar primero el repositorio para los hechos que se puedan descubrir.
4. Implementar el alcance acordado respetando los límites de módulos. Actualizar
   SPEC ante cambios funcionales, PLAN ante cambios técnicos y TASKS con avance,
   revisión y evidencia; no marcar validación ejecutada sin resultados observados.
5. Revisar estáticamente código y documentos; entregar pasos de validación manual.
   Distinguir implementación terminada de verificación de ejecución pendiente.

Cada especificación debe aplicar [MOBILE_GUIDELINES.md](docs/MOBILE_GUIDELINES.md)
a su funcionalidad: ciclo de vida y operaciones en curso; estado temporal y
persistente; conectividad y recuperación; consistencia y migraciones; estados
de interfaz; navegación e interrupciones; teclado, foco y acciones repetidas;
tamaños de pantalla, orientación e insets; accesibilidad; permisos y capacidades;
rendimiento; trabajo en segundo plano; privacidad; idiomas y formatos; y
validación mobile. Registrar decisiones y criterios comprobables. Estos puntos
no amplían automáticamente el alcance ni autorizan a inventar comportamiento.

Registrar las recomendaciones rechazadas y su razón técnica en la especificación
afectada; los cambios de arquitectura se registran también en la constitución.
No inventar motivos del usuario ni presentar decisiones pendientes como aprobadas.

## Harness Codex y delegación

Consultar [CODEX_HARNESS.md](docs/CODEX_HARNESS.md) para roles, skills, activación,
procedencia y mantenimiento. Las seis skills se mantienen en `skills/` y se
sincronizan a `.agents/skills/` mediante `scripts/sync-codex-skills.ps1`.
Editar la fuente, nunca únicamente la copia generada. Los cuatro perfiles de
especialistas están en `.codex/agents/` y heredan el modelo de la sesión.

El usuario autoriza delegación automática cuando aporte valor. El principal puede
usar `pocketdex-architect` para explorar/planificar, `android-implementer` para un
alcance aprobado, `android-reviewer` para revisar y `android-docs-researcher` para
verificar APIs. Cargar solo las skills pertinentes, sin obligar a usar todos los
roles en cada tarea. Esta autorización no resuelve dudas de producto pendientes.

- Solo un thread de especialista abierto a la vez; los especialistas no delegan
  a otros. Al completar su tarea, cerrar/liberar el thread antes de crear uno de
  otro rol si la interfaz lo permite. Si no permite cerrarlo, reutilizar el thread
  disponible transmitiendo explícitamente las instrucciones y restricciones del
  nuevo rol, o continuar desde el principal. No aumentar el límite para evitarlo.
- Antes de delegar uso de terminal, confirmar que terminó todo comando/sesión.
  El principal cede su uso y no abre otra terminal ni ejecuta comandos mientras
  el especialista la tiene. Recuperar el control al terminar y confirmar que no
  dejó procesos pendientes antes de continuar.
- Delimitar objetivo, archivos asignados, decisiones aprobadas, restricciones y
  salida esperada. Delegados de lectura no escriben; el implementador solo cambia
  el alcance asignado. La revisión se realiza sobre archivos ya estables.
- El principal inspecciona e integra resultados, comunica dudas al usuario y
  mantiene la responsabilidad por el trabajo. No delegar para eludir las reglas
  de compilación, pruebas, permisos o alcance.

## Arquitectura y convenciones

- Seguir Clean Architecture y MVVM con flujo unidireccional, según
  [constitution.md](constitution.md). Su mapa de módulos es el objetivo aprobado,
  no una descripción de módulos ya creados.
- Mantener el dominio independiente de Android y de detalles de infraestructura.
  Composables y ViewModels no acceden directamente a servicios de red ni DAOs.
- Exponer estado inmutable mediante `StateFlow`; recibir acciones con métodos
  explícitos y recolectar estado con `collectAsStateWithLifecycle`.
- Mantener composables de pantalla que reciben estado y callbacks, con lógica de
  negocio fuera de la UI. Reutilizar componentes del sistema visual cuando su
  comportamiento encaje; no crear abstracciones especulativas.
- Comunicar funcionalidades mediante contratos de dominio y callbacks de
  navegación coordinados por `:app`, sin dependencias entre funcionalidades.
- Usar identificadores, módulos y paquetes en inglés. Documentación y reportes en
  español. Seguir el estilo oficial de Kotlin y las convenciones existentes.
- Usar recursos para textos visibles al usuario y ofrecer etiquetas accesibles.
  No comunicar resultados únicamente mediante colores o gestos.
- Usar inyección por constructor donde corresponda. No mantener referencias a
  `Activity`, `Context` o `Resources` en ViewModels.
- Propagar cancelación de coroutines; evitar trabajo bloqueante en el hilo
  principal, capturas vacías y resultados de éxito fabricados.
- Centralizar versiones y aliases en `gradle/libs.versions.toml`. Revisar las APIs
  oficiales y compatibilidad antes de añadir una dependencia. No actualizar
  versiones ni configurar herramientas ajenas al alcance por iniciativa propia.
- Mantener identidad genérica del proyecto y sus artefactos. No incorporar
  referencias institucionales, secretos, tokens privados ni datos personales.
  La aplicación debe poder configurarse desde un entorno limpio sin credenciales
  privadas. Preservar el trabajo existente y evitar operaciones destructivas.
- Aplicar los acuerdos visuales/mobile: tema claro según Figma, español e inglés
  según idioma del sistema, vertical/horizontal y adaptación para teléfonos
  foldables. Cerrar las decisiones abiertas de cada spec antes de implementar
  el comportamiento afectado.

## Pruebas y comandos para el desarrollador

Escribir pruebas significativas para reglas y comportamientos cambiados; su
ejecución corresponde exclusivamente al desarrollador. Priorizar búsqueda por
nombre/número sin distinguir mayúsculas y filtros AND; favoritos, persistencia y
consulta offline de datos almacenados; comparador sin duplicados y con reemplazo;
quiz de cinco preguntas, opciones únicas, bloqueo de respuestas y puntuación;
y estados, errores y reintentos de ViewModels o casos de uso.

Usar JUnit 4, Coroutines Test y Turbine. Preferir fakes; usar MockK cuando aporte
valor. Desacoplar red, almacenamiento, dispatchers y aleatoriedad/reloj cuando
aplique. La prueba de UI de un flujo crítico es valorada en el PDF, pero no
obligatoria si la cobertura y justificación son suficientes.

Comandos para ejecución manual desde la raíz en PowerShell, aplicables a `:app`
en la base actual; **el agente no debe ejecutarlos**:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

El último comando requiere un dispositivo o emulador preparado por el
desarrollador. Tras crear los módulos, indicar las tareas concretas de los módulos
afectados. El dominio Kotlin/JVM utilizará `:core:domain:test`; los módulos Android
utilizarán sus tareas `testDebugUnitTest` y, si corresponde, pruebas instrumentadas.
Esas tareas futuras todavía no existen. Los ejemplos actuales no prueban los
requisitos del producto.

## Entregables y reporte

El PDF requiere fuente en Git o ZIP, APK, README reproducible, `specs/`, `AGENTS.md`
y al menos una skill reutilizable en `skills/`, con propósito, entradas, proceso,
salidas y verificación. Ya existen seis skills, el harness local y siete specs
en `specs/`, cada una con SPEC.md funcional, PLAN.md técnico y TASKS.md desglosado.
Las decisiones de producto están cerradas; quedan pendientes técnicos antes de
implementar. README reproducible existe para el incremento de base; faltan actualización final, verificación y APK. Estos requisitos no autorizan publicación
o empaquetado por el agente. `constitution.md` recoge las decisiones técnicas acordadas.

En cada entrega, indicar qué cambió, qué revisión se realizó, qué permanece
pendiente y los comandos o pasos manuales pertinentes. Nunca afirmar que un build,
una prueba o un comportamiento pasó sin observar su resultado. No usar screenshots
como prueba de persistencia, red o manejo del ciclo de vida.
