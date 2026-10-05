# Plan técnico: Favoritos

Estado: **incremento implementado y revisado estáticamente; esquema exportado y ejecución pendientes**. Fecha: **2026-10-05**.

Contrato funcional: [SPEC.md](SPEC.md). Seguimiento: [TASKS.md](TASKS.md).
Módulo principal objetivo: `:feature:favorites`.
Referencias técnicas: [constitución](../../constitution.md) y [AGENTS.md](../../AGENTS.md).

## Punto de partida y dependencias

Al iniciar este incremento existen app, domain, data, designsystem, Home y Detalle. El incremento Home incorpora
marcas/datos en Room, transacciones/Flow y copias privadas de imágenes. La pantalla
de favoritos se incorpora en este incremento. Ver [PLAN Home](../home/PLAN.md) para versiones,
política de caché, referencias oficiales y comandos actuales. Exportación del JSON
v1 y verificación de ejecución pendientes; no hay migración destructiva.

Base: core/domain y core/data; design system para lista; completar infraestructura de favoritos antes de dar por terminadas acciones Home/Detalle. Quiz reutiliza protección de archivos por contrato compartido.

## Arquitectura y entrega técnica

Incremento Favoritos 2026-10-05: crear módulo independiente Route/Screen/ViewModel,
conservar versiones de Detalle y conectar callbacks existentes de app. Observar
lista Room antes de refrescar; refresco por especie una vez por apertura visible,
con máximo cuatro operaciones y sin dispararlo por cambios de datos/idioma o
rotación. Cancelar descargas al ocultarse, conservar marcas y reintentar al volver.
Guardado de scroll por ID ancla/índice/offset en SavedStateHandle; errores de
lectura conservan lista útil y deshabilitan escrituras hasta recuperar verdad local.
Conteo solo con lectura confirmada y recursos plurales es/en; tarjetas sin datos
muestran faltantes sin convertir especie ID en número nacional.

Cerrar retenciones compartidas con QuizMediaRepository prepare/restore/release
sin implementar quiz. Coordinar publicaciones/pins/marcado/limpieza mediante un
mutex del almacén privado y proteger archivos entre descarga y publicación de
referencias. Sin cambio del esquema v1; JSON generado pendiente del desarrollador,
sin migración artificial para una versión que todavía no existe.

Figma MCP rechazó get_design_context (canvas 13:285 con screenshot solicitado)
por cuota Starter agotada. Reutilizar DS aprobado; cotejo visual renderizado queda
pendiente, sin inventar recursos de Figma ni incorporar URLs temporales.

Incremento Detalle 2026-10-05: el contrato separa `complete` (reutiliza contenido y
archivo completos) de `refresh` (actualiza datos/imagen). Home usa `complete` tras
marcar; Detalle pospone ese completado hasta terminar su actualización inicial.
El marcado conserva el detalle vigente de Room y solo completa sus faltantes con
el snapshot de UI, para impedir que una escritura retrasada degrade datos nuevos.
Pruebas de merge y Room/reapertura escritas; ejecución pendiente. No se crea la
pantalla de Favoritos ni cambia el esquema v1 o la política de retención.

Ownership: core/domain declara favoritos/lectura de datos; core/data implementa
Room, mappers, reloj, descargas y protección de archivos; feature/favorites presenta
lista/acciones. Home/Detalle consumen dominio sin importar esta feature. La app
conecta abrir detalle/explorar. Las rutas son objetivo, no implementación existente.

Ubicaciones: `core/data/.../local/{entity,dao,database,migration}`, `repository`,
`image`, `mapper`, `di`; domain `model/repository/usecase`; feature/favorites
`presentation`, recursos/tests. Preferir entidades separadas de resumen/detalle,
marca con instante de incorporación, completitud y referencias a imagen.

Una transacción Room guarda marca y datos disponibles; red fuera de transacción.
Índices únicos por especie, orden incorporación desc/ID asc, escritura idempotente
de estado deseado y coordinación por ID; no mantener marcas paralelas en ViewModels.
Descargar en archivo temporal privado, validar y publicar referencia final solo al
completar; mantener copia anterior en refresco. Proteger mediante referencias
independientes de favorito y sesión de quiz; retirar una no retira la otra.
Limpieza normal acotada afecta solo archivos/datos no protegidos y actualiza cobertura.
Exportar esquema y crear migraciones explícitas; no usar fallback destructivo.

## Contratos y decisiones de implementación

- Dominio: observar IDs favoritos y resúmenes disponibles; establecer estado
  deseado por ID; consultar datos guardados por ID. Resultados sin entidades Room.
- Data: operaciones suspendibles y Flows; claves estables sin duplicados. Propuesta:
  escritura idempotente del estado deseado en vez de toggle remoto/local ambiguo.
- Marca y datos disponibles se guardan en una transacción; la descarga remota ocurre
  fuera de ella y completa datos luego. Diseñar esquema de resumen/detalle y su
  completitud sin hacer depender una marca de una operación de red exitosa.
- Errores locales se traducen sin ocultar cancelación. Evitar carreras entre Home
  y Detalle; propuesta técnica: serializar escrituras por ID y confirmar mediante
  Room, sin almacenar marcas paralelas en cada ViewModel.
- Registrar versión inicial, exportación de esquema y migraciones al implementar.
  Migrar conservando marcas y datos válidos; no usar fallback destructivo.
- Copias locales protegidas necesitan referencias estables y política de limpieza
  al quitar marca; no confiar en expulsión normal de caché Coil para FAV-02.

Esta spec es responsable del contrato de favoritos consumido por Home y detalle;
esas features no importan `:feature:favorites` ni `:core:data`.

## Estado, concurrencia y operación mobile

| Aspecto | Decisión o condición comprobable |
| --- | --- |
| Ciclo de vida | Escritura completada sobrevive recreación; cancelación/interrupción no deja estado parcialmente confirmado; observar al volver |
| Estado temporal/persistente | Marcas/datos retenidos en Room; scroll conservado en navegación/tarea restaurada según APP-02; nueva apertura reinicia contexto temporal |
| Conectividad | Leer datos almacenados offline, incluido resumen parcial; completar faltantes con app abierta y reintento; fallo remoto no retira marca |
| Consistencia/migraciones | IDs únicos, orden por incorporación reciente, actualización observable y migraciones preservadoras; desmarcar conserva datos/imagen como caché normal |
| Estados | Carga/contenido/vacío/error local/reintento; error de imagen separado del contenido textual |
| Navegación/interrupciones | Detalle por ID y regreso al origen; vacío ofrece explorar; acción en curso no bloquea Back indefinidamente |
| Teclado/foco/repetición | Sin formulario; acciones repetidas no duplican favoritos ni invierten estado inesperadamente; feedback accesible |
| Pantallas/insets | Tarjetas adaptativas en vertical/horizontal y foldables; postura/tamaño no borra marcas; bisagra/insets sin ocluir acciones |
| Accesibilidad | Tarjetas y favorito etiquetados; vacío y explorar accesibles; conteo pluralizado, no solo iconografía |
| Permisos/capacidades | Persistencia en almacenamiento privado; no solicitar acceso general a archivos ni permisos runtime para favoritos |
| Rendimiento | Consultas/IO fuera del hilo principal; observar cambios y usar lista lazy; política de imágenes evita almacenamiento sin límite |
| Segundo plano | Sin trabajos programados; descarga incompleta cancelable, marca/datos confirmados preservados y faltantes retomables al acceder con app abierta |
| Privacidad | Guardar IDs/datos públicos necesarios; marcas locales sin cuenta ni envío a servicio externo |
| Idiomas/formatos | Español/inglés según sistema; cambiar idioma no crea otra lista ni duplica IDs |
| Validación mobile | Guardar, cerrar proceso, reabrir offline y consultar; simular escritura fallida, migración y cambios de postura/idioma |

## Pendientes técnicos y riesgos

Room 2.8.5/esquema v1 y completitud por datos presentes ya incorporados. Caché
normal de 512 registros/64 MiB excluye favoritos y retenciones de sesión; sin
workers, JSON v1 generado por KSP pendiente del build manual. Cada descarga privada
se valida antes de publicar y desmarcar no borra datos/imagen. La feature
de favoritos, refresco, pins y pruebas están incorporados en este incremento, todavía sin
ejecutar. Pendientes: JSON v1 exportado/versionado, compilación/pruebas,
verificación mobile/visual y consumo de pins desde la futura sesión de quiz;
migraciones preservadoras al introducir otra versión.

Cerrar estos puntos en el incremento afectado y actualizar este plan sin alterar
la spec funcional. La preparación de PLAN/TASKS no resuelve por sí sola esas verificaciones.

## Estrategia de pruebas y revisión

- Dominio/repositorio con fakes: marcar/desmarcar, ID repetido, estado deseado,
  errores, emisión consistente y dato almacenado/ausente offline; reloj fake para
  orden reciente, remarcado, desempate e idempotencia; local antes de remoto y
  refresco fallido conservando contenido/marca/instante.
- Room real: índices/claves únicas, observación, atomicidad según política y
  **base en disco cerrada y reabierta**. Una base solo en memoria no demuestra
  persistencia tras reinicio.
- Migraciones: insertar marcas/datos en esquemas anteriores y verificar
  conservación tras migrar; añadir pruebas al introducir cada versión de esquema.
- Imágenes: recuperar copia protegida con red deshabilitada después de limpiar
  caché normal; descarga fallida/parcial y desmarcar durante descarga sin reinstalar marca.
- ViewModel/Turbine: carga/lista/vacío/error/reintento y acciones fallidas.
- UI/manual: flujo crítico propuesto en base; reapertura offline real, TalkBack,
  ambos idiomas/orientaciones y transición de foldable plegado a desplegado.

El desarrollador ejecuta pruebas/builds según [el índice](../README.md); ninguna
captura aislada acredita persistencia o consulta offline.

Usar JUnit 4, Coroutines Test y Turbine, fakes deterministas; MockK solo si aporta
valor. Escribir pruebas significativas, sin ejecutarlas durante sesión del agente.
El desarrollador ejecuta compilación y pruebas y entrega evidencia; no afirmar
que un criterio pasa solo porque su prueba está escrita.

## Comandos y validación del desarrollador

Los módulos y pruebas están incorporados. El agente no ejecuta estos comandos.
Ejecutar secuencialmente desde la raíz. Las pruebas instrumentadas requieren
dispositivo/emulador preparado.

```powershell
.\gradlew.bat :core:domain:test
.\gradlew.bat :core:data:testDebugUnitTest
.\gradlew.bat :core:data:connectedDebugAndroidTest
.\gradlew.bat :feature:favorites:testDebugUnitTest
.\gradlew.bat :feature:home:testDebugUnitTest
.\gradlew.bat :feature:detail:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :feature:favorites:connectedDebugAndroidTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

Tras compilar, versionar `core/data/schemas/com.example.pocketdex.core.data.local.PocketDexDatabase/1.json`
generado por KSP y aportar resultados para cerrar FAV-T02/V01. No generar uno a mano.
Registrar entorno, comandos, resultados y limitaciones en TASKS.md.

Validación manual: marcar desde Home/resumen y Detalle/parcial; observar coherencia
de lista/conteo; quitar/reincorporar y desempates; abrir detalle/Back y Explorar
con contexto Home; cerrar proceso/reabrir offline para marcas, datos e imágenes;
red lenta, descarga fallida/imagen dañada, desmarcar mientras descarga, reintentos;
limpiar caché normal manteniendo copias favoritas/pins; rotación/recuperación de
tarea conserva scroll, nueva tarea inicia Home; es/en/respaldo inglés, TalkBack,
texto 200 %, horizontal y foldables sin bisagra/insets sobre controles.
Los tests de host usan fixtures: el flujo real integrado debe recorrerse en app;
no se afirma que esos fixtures prueben por sí solos red o persistencia.

## Secuencia de entrega

Concretar pendientes y contratos → implementar el incremento en los archivos
responsables → escribir pruebas → revisar estáticamente → entregar validación
al desarrollador. El detalle y dependencias de cada paso están en TASKS.md.
