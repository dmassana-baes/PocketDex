# Plan técnico: Comparador de estadísticas

Estado: **incremento implementado y revisado estáticamente; referencias locales inspeccionadas; validación de ejecución y fidelidad visual pendientes**. Fecha: **2026-10-05**.

Contrato funcional: [SPEC.md](SPEC.md). Seguimiento: [TASKS.md](TASKS.md).
Módulo principal objetivo: `:feature:compare`.
Referencias técnicas: [constitución](../../constitution.md) y [AGENTS.md](../../AGENTS.md).

## Punto de partida y dependencias

Existen app, tres core, Home, Detalle y Favoritos. Reutilizar versiones,
repositorios y componentes DS; no añadir tablas, dependencias nuevas ni migraciones.

Base y design system; contrato/reglas de consulta compartidos definidos con Home y lectura de estadísticas de core. No necesita importar las features Home/Detalle.

## Arquitectura y entrega técnica

Crear `:feature:compare` Android/Compose, con selector propio y dependencias
domain/designsystem. Reutilizar la regla común de búsqueda, nunca importar Home.
Reglas puras de selección de dos slots y comparación por seis claves viven en
domain; candidatos/detalle por repositorios. App coordina navegación secundaria
o la entrada de resultados acordada, sin transportar DTOs completos.

Ubicaciones objetivo: `feature/compare/.../presentation/{selection,selector,result}`,
resources/tests; `core/domain/.../compare` y reglas de consulta compartidas;
data reutiliza lectura de catálogo/detalle. No añadir tabla de comparaciones.

ViewModel StateFlow/UDF con dos slots, ID/generación de lectura y resultados
invalidados por cambio. Limitar dos lecturas activas para el par; cancelar/rechazar
lectura tardía; validar duplicado tanto al elegir como en regla pura de comparación.
Guardado de tarea con IDs/slot en edición/contexto, sin historial permanente.

## Contratos y decisiones de implementación

Incremento 2026-10-05: dominio contiene ComparePair (dos IDs opcionales distintos)
y comparación completa por seis claves, con empate explícito. CompareViewModel
es retenido por app en el owner de la Activity, fuera de entradas secundarias;
SavedStateHandle guarda IDs/slot y el snapshot de navegación conserva la ruta
de resultados por IDs. App transporta IDs
y coordina selector/Back/resultados; features no se importan entre sí. Restaurar
resultados valida el par contra su owner y relee detalles por los mismos IDs;
no usar otro par ni inventar datos. Máximo dos cargas de detalle, caché primero
y completar remoto cuando falten estadísticas; generación por slot rechaza
resultados antiguos. Cambiar selecciones invalida resultado solicitado.

Selector independiente con catálogo compartido, debounce 300 ms, IME inmediato,
idioma, caché local y páginas de 30 mediante cursor; acciones anterior/cargar más, errores
iniciales/incrementales conservando resultados. Sin filtro adicional. SavedStateHandle
conserva consulta/scroll por ID ancla en esa entrada; recuperar solicita su página
y permite cargar las anteriores. Los cursors usan la consulta confirmada, incluso
si se edita texto durante debounce. Regla duplicada en selector/owner/dominio.

La consulta Figma get_design_context 74:93424 fue rechazada por cuota Starter.
Las cinco capturas locales suministradas en `assets/screens-designs/` completan
la inspección de COMPARE-T03. [ASSETS.md](ASSETS.md) registra su composición,
las diferencias con la implementación y el SVG del dado suministrado.
El módulo reutiliza el DS aprobado; la inspección de referencias no acredita
fidelidad de la app ejecutada ni añade dado/ganador global al alcance funcional.
El desarrollador pospone compilación/pruebas/esquema hasta terminar los specs
por instrucción del 2026-10-05; no bloquea escribir implementación y pruebas.

- Compare obtiene candidatos mediante el contrato de catálogo común y detalle
  mediante dominio; puede reutilizar componentes visuales del selector, sin importar Home.
- Modelo de comparación en dominio: dos IDs distintos con valores de las seis
  claves → resultado por fila. COMPARE-01 confirmado: igualdad con texto «Empate»,
  ambos valores visibles y aspecto neutro, sin destacar ganador.
- Validación de duplicados en reglas, no solo en controles. Identidad consistente
  con HOME-01, incluyendo formas cuando corresponda.
- Screen recibe estado y callbacks de elegir, confirmar, cancelar, quitar,
  reemplazar, comparar/reintentar y volver. Navegación global coordinada por `:app`.
- El componente de comparación recibe números y resultado calculado; escala visual
  no sustituye la comparación numérica ni altera datos remotos. DS-08 confirmado:
  referencia fija 255 y fracción clamp(valor/255, 0, 1) compartida con detalle;
  conservar el número real incluso si supera la escala visual.

El selector incluye búsqueda sobre el catálogo compartido, con nombre por contenido,
número exacto y debounce 300 ms según HOME-02/HOME-04. No requiere filtro de tipo
adicional. COMPARE-03 confirmado: botón Comparar abre resultados; Back devuelve a
edición de selecciones. No comparar automáticamente ni navegar por recomposición.

## Incremento de ajuste visual autorizado

Solicitud del usuario del 2026-10-05: ajustar la implementación a las capturas
locales y registrar el trabajo en COMPARE-T10/T11/T12. Selección incorpora título
grande/introducción, contenedores Gray100 de altura mínima 190 dp, imágenes
centradas y acciones accesibles. Resultados incorporan dos miniaturas en gris
y filas compactas con escala 255 y mayor/empate explícitos.

Selector conserva su destino Navigation 3 y ViewModel; presenta una superficie
de hoja con handle decorativo y cierre por Back, sin introducir arrastre para
descartar. LazyVerticalGrid con columnas adaptativas sustituye filas verticales;
mantiene claves por ID, headers de ancho completo, cursor y scroll por ancla.
La hoja se limita en ventanas amplias y respeta el espacio útil del host y el
teclado. Reutilizar tokens, botones, búsqueda, badges, iconos e imágenes del DS.
Añadir únicamente composición de candidatos y estadísticas específica de compare;
no modificar globalmente componentes usados por otras funcionalidades.

## Estado, concurrencia y operación mobile

| Aspecto | Decisión o condición comprobable |
| --- | --- |
| Ciclo de vida | Rotación/postura no cambia par ni duplica consultas; cargas obsoletas canceladas o rechazadas; salida según COMPARE-02 |
| Estado temporal/persistente | IDs/slot en edición conservados al navegar/rotar y recuperar tarea según APP-02; apertura nueva con slots vacíos; sin comparación persistente como favorito |
| Conectividad | Selector/detalle remoto puede fallar; conservar selección válida, permitir reintento; datos locales solo según política compartida |
| Consistencia/migraciones | Sin almacenamiento propio requerido; mismo contrato de identidad/estadísticas que detalle; invalidar resultados al cambiar IDs |
| Estados | Slots vacíos/parcial/completo, selector, cargas y errores; comparación imposible claramente deshabilitada |
| Navegación/interrupciones | Cancelar selector no elimina selección; Back vuelve según APP-01; resultado tras quitar/reemplazar no queda obsoleto |
| Teclado/foco/repetición | Si hay búsqueda, IME accesible; abrir/confirmar repetidamente no duplica slots ni resultados |
| Pantallas/insets | Dos selecciones y seis filas accesibles en vertical/horizontal/foldable; redistribuir sin ocultar quitar/reemplazar ni cruzar zonas ocluidas |
| Accesibilidad | Por fila: etiqueta, ambos valores y quién es superior o texto «Empate» con aspecto neutro; acciones de slot etiquetadas con nombre/posición |
| Permisos/capacidades | Sin permisos runtime adicionales ni sensores para el dado del Figma |
| Rendimiento | Máximo dos lecturas de detalle activas para el par; selector incremental si catálogo grande, imágenes dimensionadas |
| Segundo plano | Sin trabajo programado; no recalcular/escribir por recolección de estado |
| Privacidad | Solo selecciones de datos públicos, sin cuenta/historial remoto requerido |
| Idiomas/formatos | Español/inglés según sistema; etiquetas traducibles, comparación por clave y valor independientes del idioma |
| Validación mobile | Duplicados, reemplazo en carga, quitar, cancelar, red lenta/offline, rotación/idioma y cambios de postura |

## Pendientes técnicos y riesgos

Contratos/pantallas/navegación implementados según el incremento anterior.
Inspección de selección, selector, estado ocupado y resultados completada con
referencias locales, sin depender de la cuota Figma. Pendientes: compilación,
ejecución/manual mobile y cotejo visual de la app con esas referencias.
COMPARE-T10/T11/T12 ajustan slots, presentación del selector/candidatos y
cabecera de resultados. ASSETS.md registra el incremento y sus adaptaciones;
cotejarlas en COMPARE-V02. No presentar la revisión estática como aprobación
de fidelidad completa.
El resultado referenciado no autoriza ganador global; el dado sigue excluido.

Cerrar estos puntos en el incremento afectado y actualizar este plan sin alterar
la spec funcional. La preparación de PLAN/TASKS no resuelve por sí sola esas verificaciones.

## Estrategia de pruebas y revisión

- Dominio: cero/uno/dos IDs, duplicados, reemplazar/quitar cada slot, seis claves
  y valores mayor/menor/igual; no depender de orden del JSON ni del idioma.
- ViewModel/Turbine con fakes: carga/error/reintento, cancelar selector, acciones
  repetidas y respuestas fuera de orden al reemplazar/quitar durante carga.
- Compose/UI: elegir slot, duplicado rechazado, reemplazo/eliminación accesibles,
  estado deshabilitado e igualdad según decisión.
- Manual: red lenta, navegar/volver, rotar, plegar/desplegar, texto ampliado y TalkBack.

Ejecución por el desarrollador según [el índice](../README.md).

Usar JUnit 4, Coroutines Test y Turbine, fakes deterministas; MockK solo si aporta
valor. Escribir pruebas significativas, sin ejecutarlas durante sesión del agente.
El desarrollador ejecuta compilación y pruebas y entrega evidencia; no afirmar
que un criterio pasa solo porque su prueba está escrita.

## Comandos y validación del desarrollador

Los módulos y sus pruebas están incorporados. Por decisión del usuario, ejecutar
estos comandos al terminar las implementaciones de los specs. El agente no los
ejecuta. Las pruebas instrumentadas requieren dispositivo/emulador preparado.

```powershell
.\gradlew.bat :core:domain:test
.\gradlew.bat :feature:compare:testDebugUnitTest
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :feature:compare:connectedDebugAndroidTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

Registrar entorno, comandos, resultados y limitaciones en TASKS.md. Verificar
cero/uno/dos slots, duplicado con aviso, cancelar selector por Back visible/sistema,
reemplazar/quitar durante red lenta y respuesta tardía, seis claves/mayor/empate,
valores >255 y comparación únicamente al pulsar; Back edita y nuevo par invalida
resultados previos. Consultas por nombre/número/ceros, IME/debounce, páginas fuera
de la primera, error incremental y reintento sin perder resultados; recuperar
tarea con selector/resultados y scroll, nueva tarea con par vacío. es/en/respaldo
inglés, texto 200 %, TalkBack, vertical/horizontal y foldables en región útil.

Para COMPARE-T10/T11/T12, cotejar las cinco capturas locales: slots vacíos/ocupados,
intro/CTA, hoja y búsqueda con teclado, candidatos de uno/dos tipos y seis filas
con miniaturas. Verificar cierre visible y Back sin cambiar el par; buscar,
paginar, entrar/salir y restaurar ancla tras rotación en cuadrícula. Confirmar que
reemplazar y quitar por icono actúan sobre el slot correcto. Probar nombres largos,
texto 200 %, pantalla estrecha y horizontal/foldable; no recortar tipos ni ocultar
controles al abrir teclado. El host consume insets safeDrawing/IME; el selector
respeta los insets ya consumidos, sin añadir barras del sistema de las capturas.

Revisión del 2026-10-05: limpiar aplica inmediatamente y normalización equivalente
(mayúsculas/espacios/IME tras debounce) conserva consulta/carga/scroll. El reintento
es explícito, sin convertir una confirmación equivalente en otra descarga. Pruebas
escritas de estos casos y recuperación de ancla; no ejecutadas.

## Secuencia de entrega

Concretar pendientes y contratos → implementar el incremento en los archivos
responsables → escribir pruebas → revisar estáticamente → entregar validación
al desarrollador. El detalle y dependencias de cada paso están en TASKS.md.
