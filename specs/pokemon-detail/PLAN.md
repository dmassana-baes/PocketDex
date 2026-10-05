# Plan técnico: Detalle de Pokémon

Estado: **incremento implementado y revisado estáticamente; ejecución pendiente**. Fecha: **2026-10-05**.

Contrato funcional: [SPEC.md](SPEC.md). Seguimiento: [TASKS.md](TASKS.md).
Módulo principal objetivo: `:feature:detail`.
Referencias técnicas: [constitución](../../constitution.md) y [AGENTS.md](../../AGENTS.md).

## Punto de partida y dependencias

Existen app, los tres core y Home. Room, Retrofit/Serialization, Coil, Hilt/KSP,
Navigation 3 y componentes de imagen/estadísticas ya incorporados. Reutilizar
sus versiones; no añadir dependencias ni modificar el esquema v1 para este incremento.

Base y design system; catálogo/lectura de detalle en core; contrato/persistencia de favoritos antes de cerrar marcado y offline. Home/Favoritos aportan callbacks de integración desde app, no dependencias Gradle.

## Arquitectura y entrega técnica

Crear `:feature:detail` Android/Compose con Route/Screen/ViewModel/UiState y entrada
por especieID. Depende de domain/designsystem, sin DAO/servicio directo. La app
coordina Back/origen. Data resuelve variedad predeterminada y seis claves, no el
orden incidental del JSON; tipos de infraestructura se quedan en data.

Ubicaciones objetivo: `feature/detail/.../presentation`, recursos/tests; lectura y
modelos compartidos en domain; mapper/lectura/refresco en data. Reutilizar favorito,
imagen, gradiente y fila de estadística del design system sin nuevas cachés.

Observar contenido local y marca, coordinar actualización por ID, separar errores
de imagen/detalle/escritura. Controlar acción ocupada de favorito por ID y establecer
estado deseado; rechazo de respuestas obsoletas si cambia ID. Snapshot guarda
ID/scroll/contexto mínimo; restaurar no escribe ni repite refresco en curso.

## Contratos y decisiones de implementación

### Corrección de smart cast entre módulos (2026-10-05)

El desarrollador reportó que `loadRemote` en `DetailViewModel` no compilaba:
`DataResult.Success.value` es una propiedad pública de `:core:domain` y Kotlin
no permite su smart cast tras comprobarla desde `:feature:detail`.
Se captura una sola vez en `val detail = result.value`; la comprobación de nulo
y `accept(id, detail)` operan sobre esa referencia local estable.
Se mantienen el rechazo de respuestas de otro ID, contenido en caché y estados
de ausencia/error. No cambia el contrato funcional ni se fuerza un cast no nulo.

Referencia: [condiciones del smart cast en Kotlin](https://kotlinlang.org/docs/typecasts.html#smart-cast-prerequisites).
Revisión estática realizada; confirmar compilación y pruebas existentes con
`.\gradlew.bat :app:assembleDebug` y
`.\gradlew.bat :feature:detail:testDebugUnitTest`, ejecutados por el desarrollador.

### Incremento Detalle (2026-10-05)

Añadir a `PokemonRepository` lectura exclusivamente local `cachedDetail(id)`,
observación `observeDetail(id)` y `refresh(id)` remoto explícito. `detail(id)`
conserva su lectura local con fallback remoto para consumidores existentes.
Un null local es falta de caché, no ausencia remota verificada. Room observa por
especie; tipos/estadísticas siguen los mappers compartidos. Cada resultado debe
corresponder al ID solicitado antes de publicarse; cancelación propagada.

Refresco reutiliza metadatos de índice ya conocidos; si faltan, obtiene Pokédex
nacional y especie/variedad predeterminada directamente desde v2, sin exigir CSV
ni descargar otros detalles. Guardar en transacción, conservando archivo útil.
Ausencia solo tras consultar el catálogo nacional con éxito; fallo no se transforma
en null ni borra contenido. Concurrencia existente de cuatro descargas.

Añadir `FavoritesRepository.complete(id)` para completar faltantes y medios
privados reutilizando datos completos locales. `refresh(id)` conserva el refresco
forzado para la futura lista. Home/Detalle usan complete tras persistir marca;
Detalle coordina su actualización con el completado si es favorito, sin iniciarlos
a la vez. Con contenido local completo, complete solo descarga medios faltantes;
con contenido parcial intenta completar datos mediante el contrato compartido. Separa
fallos de lectura/refresco, imagen y escritura; completado no reinstala una marca
quitada. Sin worker ni caché adicional, sin cambios de orden/retención acordados.

ViewModel se vincula al ID desde Route; SavedStateHandle conserva ID y scroll.
Cambio de ID cancela lectura/observación/completado y rechaza respuestas obsoletas.
Observación de favoritos confirma marcas; bloqueo por operación en curso evita
doble escritura. Recreación conserva ViewModel; retorno visible refresca cuando
no hay operación activa, sin disparar escrituras por idioma/postura.

Back fijo y accesible en todos los estados; el host coordina origen y Back sistema.
Encabezado con gradiente del primer tipo, imagen dinámica, identidad y ambos tipos;
seis filas DS con valor nullable/escala 255. En ancho útil amplio y fuente que lo
permita, imagen/identidad y estadísticas comparten fila; en compacto, columna
desplazable. Usar la región sin bisagra/insets ya suministrada por app.

Figma: get_design_context del canvas no entregó código; metadata mostró estructura
de Details (gradiente, sprite 260 y datos), luego la cuota Starter bloqueó nuevas
lecturas. No se acredita fidelidad desde esa metadata. Reutilizar DS aprobado;
revisión visual/manual pendiente, sin incorporar pestañas/contenido fuera del PDF.
Fuentes: [PokéAPI v2](https://pokeapi.co/docs/v2/),
[Room y consultas observables](https://developer.android.com/training/data-storage/room/async-queries).

- Entrada Compose con estado/callbacks de reintento, favorito y regreso. `:app`
  transmite solo ID estable y coordina navegación.
- ViewModel → contrato de lectura de detalle por ID y contrato observable de
  favoritos definidos en base/favorites; sin Retrofit/DAO/Context en presentación.
- Data mapea identidad, tipos e imagen y valida las seis claves estadísticas;
  distingue inexistencia, conectividad, fallo local y respuesta no utilizable.
- Cambiar favorito usa estado deseado y resultado observable; persistencia,
  transacción y datos retenidos son responsabilidad de favorites.
- Imagen a través del componente compartido, con recursos/semántica adecuados;
  el detalle no inventa su propia garantía de disponibilidad offline.

## Estado, concurrencia y operación mobile

Coordinación final: si se marca durante la carga/actualización, diferir el
completado hasta su término. La observación tardía de la marca puede activar ese
completado una sola vez; el reintento explícito sigue disponible. No iniciar una
actualización mientras se completan medios. El marcado lee la fila vigente en
la transacción y rellena únicamente sus faltantes con el snapshot conocido;
preserva estadísticas y archivo publicados después de capturar la UI.
La comprobación de archivos ocurre en IO. Coil limita la solicitud a 512 px y
recurre a la URL si falla la copia local; un reintento recrea la solicitud.

| Aspecto | Decisión o condición comprobable |
| --- | --- |
| Ciclo de vida | Rotación/postura no reinicia escrituras; operaciones estructuradas por ID; cambio de ID cancela/rechaza resultados antiguos |
| Estado temporal/persistente | ID/scroll conservados al volver y restaurar tarea según APP-02; apertura nueva en Home; favorito en Room; sin pestañas extra |
| Conectividad | Consultar favorito almacenado offline; primer acceso sin datos muestra error recuperable; imagen fallida no oculta textos disponibles |
| Persistencia/consistencia | Datos/copia protegida según FAV-01/FAV-02; local primero y refresco al abrir según FAV-05, con conservación ante fallo; observar cambios de otras pantallas |
| Estados | Carga, contenido, fallo/reintento; acción de favorito y fallo de imagen separados del fallo de detalle |
| Navegación/interrupciones | Back siempre accesible y devuelve al origen; no esperar red para permitir salir |
| Teclado/foco/repetición | Sin formulario; foco al título y acciones etiquetadas; impedir repetición conflictiva de favorito mientras se escribe |
| Pantallas/insets | Imagen y estadísticas redistribuidas en horizontal/foldable; scroll y tamaño flexible; ninguna acción sobre bisagra o barras del sistema |
| Accesibilidad | Nombre/número/tipos y etiqueta-valor de cada estadística; favorito anuncia acción/estado; barras decorativas si duplican valor textual |
| Permisos/capacidades | Sin permisos runtime; datos e imágenes mediante infraestructura existente |
| Rendimiento | Imagen dimensionada a ventana, IO fuera del hilo principal; no consultar todos los detalles por abrir uno |
| Segundo plano | Sin trabajo programado; persistencia en curso consistente o fallo observable según favorites |
| Privacidad | Solo datos públicos y marcas locales; no incluir respuestas completas o datos innecesarios en logs |
| Idiomas/formatos | Español/inglés según sistema, inglés de respaldo, ID estable; etiquetas traducibles sin cambiar claves estadísticas; número # con mínimo tres dígitos conforme a DS-R08 |
| Validación mobile | Red lenta/offline, imagen ausente, favoritos fallidos, Back, recreación, idioma y plegado/desplegado |

## Pendientes técnicos y riesgos

Contratos y coordinación implementados según el incremento anterior. Permanecen
exportación del esquema Room v1 mediante build del desarrollador, compilación,
ejecución de pruebas y cotejo visual/mobile. La cuota MCP de Figma impidió lecturas
adicionales: verificar encabezado/gradiente y fidelidad en dispositivo.

Cerrar estos puntos en el incremento afectado y actualizar este plan sin alterar
la spec funcional. La preparación de PLAN/TASKS no resuelve por sí sola esas verificaciones.

## Estrategia de pruebas y revisión

- Mapper/repositorio: claves estadísticas desordenadas, tipos dobles, ID diferente,
  ausencia y respuesta inválida; remoto/local conforme a la política aprobada.
- ViewModel con fakes/Turbine: carga, éxito, error, reintento, cancelación, detalle
  local offline, favorito en curso/fallo y cambios observados desde otras pantallas.
- Compose: Back, semántica de favorito y estadística; inspección manual de imagen,
  texto ampliado y continuidad en horizontal/foldables.
- Integración con Room real y reapertura según favorites para demostrar persistencia;
  las capturas no la prueban.

El desarrollador ejecuta los comandos del [índice](../README.md) tras implementar.

Usar JUnit 4, Coroutines Test y Turbine, fakes deterministas; MockK solo si aporta
valor. Escribir pruebas significativas, sin ejecutarlas durante sesión del agente.
El desarrollador ejecuta compilación y pruebas y entrega evidencia; no afirmar
que un criterio pasa solo porque su prueba está escrita.

## Comandos y validación del desarrollador

El módulo y sus pruebas están incorporados. El agente no ejecuta estos comandos.
Ejecutar secuencialmente desde la raíz; `connectedDebugAndroidTest` requiere un
dispositivo/emulador preparado.

```powershell
.\gradlew.bat :core:domain:test
.\gradlew.bat :core:data:testDebugUnitTest
.\gradlew.bat :feature:detail:testDebugUnitTest
.\gradlew.bat :feature:home:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :core:data:connectedDebugAndroidTest
.\gradlew.bat :feature:detail:connectedDebugAndroidTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

Registrar entorno, comandos, resultado real y limitaciones en TASKS.md. Verificar:
Home→Detalle→Back con scroll previo; Back durante red lenta/carga/error; contenido
descargado y parcial offline; imagen ausente/corrupta; marcar/desmarcar antes de
descargas y escritura fallida; marcar durante red lenta sin petición concurrente,
snapshot antiguo sin degradar la fila nueva; reintento y actualización sin borrar datos; ID
estable al rotar/recuperar la misma tarea y tarea nueva en Home; es/en/otro idioma;
TalkBack, texto 200 %, horizontal y foldable con bisagra. El regreso a Favoritos
ya está integrado con la pantalla de Favoritos desde el 2026-10-05; ejecución pendiente.

## Secuencia de entrega

Concretar pendientes y contratos → implementar el incremento en los archivos
responsables → escribir pruebas → revisar estáticamente → entregar validación
al desarrollador. El detalle y dependencias de cada paso están en TASKS.md.
