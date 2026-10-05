# Favoritos

Estado: **definición funcional acordada**. Fecha: **2026-09-30**.

Fuentes: [PDF](../../docs/pocket_dex_definition.pdf),
[Figma](https://www.figma.com/design/0M4IjLeiuGIFocB4qDQuRV/Pokedex-App---UI?node-id=13-285)
y [guías mobile](../../docs/MOBILE_GUIDELINES.md).
Documentos relacionados: [PLAN técnico](PLAN.md), [tareas](TASKS.md) e [índice](../README.md).

## Objetivo y alcance

Guardar favoritos desde Home/Detalle, mantener una lista consistente y consultar
datos ya almacenados sin conexión. No incluye cuentas, sincronización, colecciones,
exportación ni catálogo completo offline. Conteo y lista reflejan favoritos reales.

## Reglas funcionales y decisiones confirmadas

- FAV-01: guardar inmediatamente marca y datos disponibles sin esperar detalle
  completo. Si hay datos completos previos, conservarlos. Una marca confirmada
  implica guardado exitoso; un fallo mantiene estado real y ofrece recuperación.
- Completar faltantes al acceder a favoritos/detalle con la app abierta. Fallo de
  descarga o interrupción conserva lo guardado y permite retomar; no simular
  estadísticas, imágenes ni una completitud inexistente.
- FAV-02: tras descarga correcta, la imagen permanece disponible localmente mientras
  sea favorito incluso después de limpiar caché normal. Si falta/falla, mostrar
  placeholder manteniendo marca/datos. Una descarga que termina tras desmarcar
  no vuelve a marcar ni mantiene una protección de favorito retirada.
- FAV-03: desmarcar elimina la marca y su protección, conservando datos e imagen en
  caché normal sujetos a limpieza posterior. No borrar otros favoritos ni referencias
  que todavía necesita una sesión activa/recuperable de quiz.
- FAV-04: incorporación más reciente primero. Remarcar tras desmarcar cuenta como
  nueva incorporación; refrescar o volver a marcar uno ya marcado no lo reordena.
  En instantes iguales, desempatar por identificador de especie ascendente.
- FAV-05: mostrar datos locales inmediatamente; actualizar al abrir con conexión
  y app abierta. Éxito actualiza contenido sin alterar marca/orden; fallo conserva
  contenido e imagen útil con aviso/reintento. No repetir actualización por rotación.
- Una imagen nueva sustituye a la anterior solo cuando está completa y utilizable.
- Marcas/datos confirmados sobreviven cierre, reinicio y actualizaciones compatibles;
  no borrarlos por actualización. Cambiar idioma no crea otra lista ni duplica Pokémon.
- No programar descargas fuera de la app abierta; se retoman faltantes al volver a acceder.

## Flujo y estados visibles

1. Abrir → carga local → lista o vacío; lectura fallida → error/reintento, no vacío.
2. Marcar/desmarcar → guardado → mismo estado en Home, Detalle y Favoritos.
3. Abrir tarjeta → detalle del Pokémon seleccionado → regreso a lista de origen.
4. Vacío → explorar Home conservando criterios/desplazamiento previos.
5. Cerrar/reabrir offline → favoritos y datos retenidos disponibles sin esperar red.

Imagen ausente y detalle parcial no convierten un favorito con datos en error total.
Conservar desplazamiento durante navegación/tarea recuperada; apertura nueva
reinicia contexto temporal, manteniendo marcas/datos guardados.

## Preferencia explícita

El usuario eligió incorporación reciente frente a la recomendación inicial de
orden por número nacional. Se registra su preferencia sin atribuirle otro motivo.

## Requisitos trazados al producto

| ID | Fuente | Comportamiento |
| --- | --- | --- |
| FAV-R01 | 5.1 | Sección de favoritos y marcar/desmarcar desde Home y Detalle |
| FAV-R02 | 5.2 | Persistencia local conservada tras cerrar/reiniciar la aplicación |
| FAV-R03 | 5.3 | Vacío con acción clara para volver a explorar Pokémon |
| FAV-R04 | 5.4 | Consulta sin conexión de datos favoritos ya almacenados |
| FAV-R05 | Acuerdo | Marca coherente entre pantallas y datos conservados tras actualizaciones compatibles |

## Condiciones mobile comunes

Aplicar [el sistema visual](../design-system/SPEC.md) y [la navegación](../app-foundation/SPEC.md):
tema claro; español/inglés según sistema con inglés de respaldo; vertical/horizontal,
redimensionamiento y foldables sin recortes ni acciones sobre bisagras o zonas del
sistema. Texto ampliado, TalkBack, foco lógico, teclado cuando aplique y acciones
etiquetadas; nunca comunicar selección/error/resultado solo con colores o gestos.
La interfaz permanece responsiva con red lenta y las acciones repetidas no duplican
efectos. No se requieren permisos runtime, cuenta, datos personales, analytics ni
trabajos programados. La recuperación de errores y conservación de estado siguen
las reglas específicas anteriores; los escenarios mobile de aceptación se verifican
según el plan, incluyendo reapertura, conectividad y cambios de idioma/postura.

## Criterios de aceptación funcional

| ID | Escenario y resultado esperado |
| --- | --- |
| FAV-AC01 | Marcar en Home o Detalle crea una sola marca por ID; desmarcar la retira; los tres consumidores observan estado coherente y fallos no se presentan como éxito |
| FAV-AC02 | Tras confirmar marcas, cerrar proceso y reabrir la app conserva exactamente los IDs marcados; no depende de estado temporal de la pantalla |
| FAV-AC03 | Sin favoritos, mostrar vacío con acción etiquetada que lleva a explorar; un fallo de lectura local muestra error, no ese vacío |
| FAV-AC04 | Tras almacenar datos, cerrar y reabrir sin red permite listar y consultar su detalle retenido; imágenes cumplen FAV-02 y faltantes tienen representación explícita |
| FAV-AC05 | Una escritura fallida mantiene el estado persistido previo y permite recuperar la acción; repeticiones/concurrencia no crean duplicados ni marcas falsas |
| FAV-AC06 | Reapertura y actualización desde cada versión de datos soportada conservan favoritos y datos válidos; nunca se borran por una actualización que elimine datos |
| FAV-AC07 | Quitar una marca aplica exactamente FAV-03; no elimina datos/imágenes necesarios para otros IDs y actualiza lista/conteo |
| FAV-AC08 | Rotación, idioma, ancho y postura conservan datos/scroll según APP-02; favoritos mantienen orden por incorporación descendente independiente del idioma |
| FAV-AC09 | Marcar desde Home con solo resumen guarda marca/resumen antes de completar detalle; fallo remoto o muerte de proceso conserva marca y permite recuperar datos faltantes |
| FAV-AC10 | Una imagen descargada correctamente permanece disponible offline mientras sea favorito aun si se limpia caché normal; falta/fallo de imagen muestra placeholder sin borrar datos/marca |
| FAV-AC11 | Desmarcar retira marca/protección sin borrar inmediatamente datos/imagen; limpieza normal no elimina datos/copias protegidas de otros favoritos y respeta referencias en uso |
| FAV-AC12 | Marcar A y luego B ordena B/A; refrescar o marcar B idempotentemente no reordena; quitar y volver a marcar A lo coloca primero, con desempate por ID estable |
| FAV-AC13 | Abrir con datos guardados muestra local antes de completar actualización; refresco exitoso actualiza contenido sin cambiar marca/orden, y fallo conserva datos con feedback/reintento sin borrar imágenes válidas |

## Decisiones y límites

Las decisiones funcionales están confirmadas por el usuario el 2026-09-30.
No quedan preguntas de producto propias de esta área. Nuevas dudas que alteren
comportamiento o aceptación deben consultarse; no cambiar requisitos para acomodar
una implementación. Pendientes técnicos y verificación se registran en [PLAN.md](PLAN.md);
trabajo desglosado y progreso en [TASKS.md](TASKS.md).
