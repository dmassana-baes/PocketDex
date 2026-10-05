# Home: listado, búsqueda y filtros

Estado: **definición funcional acordada; avance y verificación en TASKS.md**. Fecha: **2026-10-01**.

Fuentes: [PDF](../../docs/pocket_dex_definition.pdf),
[Figma](https://www.figma.com/design/0M4IjLeiuGIFocB4qDQuRV/Pokedex-App---UI?node-id=13-285)
y [guías mobile](../../docs/MOBILE_GUIDELINES.md).
Documentos relacionados: [PLAN técnico](PLAN.md), [tareas](TASKS.md) e [índice](../README.md).

## Objetivo y alcance

Explorar el catálogo progresivamente, buscar por nombre/número, filtrar por un tipo,
abrir detalle y marcar/desmarcar favoritos. No incluye Random ni filtros de generación,
debilidad, peso, altura u orden. No garantiza todo el catálogo sin conexión.

## Reglas funcionales y decisiones confirmadas

- HOME-01: todas las especies disponibles en PokéAPI, una entrada por especie con
  variedad predeterminada, sin formas alternativas adicionales; orden nacional
  ascendente, sin máximo estático. La identidad de especie y su número nacional
  no se confunden con el identificador de una variedad o con otro número de Pokédex.
- HOME-02: buscar fragmentos de nombre, incluidos fragmentos interiores, sin distinguir
  mayúsculas y quitando espacios exteriores; considerar nombre visible del idioma
  y nombre canónico. Dígitos con # inicial opcional buscan número nacional exacto:
  25, 025 y #025 son equivalentes; nunca hay coincidencia por prefijo numérico.
  Texto vacío no restringe; ausencia de coincidencias es vacío, no error.
- HOME-03: máximo un tipo activo; un Pokémon de dos tipos coincide con cualquiera
  de los suyos. Búsqueda y tipo se combinan con AND sobre el catálogo acordado,
  no solo sobre las tarjetas o páginas visibles.
- HOME-04: buscar tras 300 ms sin cambios. Una edición reinicia la espera; la acción
  Buscar del teclado confirma el texto y puede ocultar el teclado sin repetir una
  consulta equivalente. Limpiar búsqueda/restablecer tipo activo actúan de inmediato.
- El panel edita un tipo provisional; Aplicar confirma. Back, cierre o toque exterior
  descartan lo provisional. Restablecer dentro del panel no afecta resultados hasta Aplicar.
- Cambiar criterios aplicados empieza desde el inicio de resultados; volver sin
  cambios conserva desplazamiento. Resultados de una búsqueda anterior no sustituyen la vigente.
- HOME-05: conservar datos ya descargados y consultarlos offline. Anunciar cobertura
  parcial y que la búsqueda se limita a lo guardado; un vacío parcial no afirma
  que no existan coincidencias remotas. Sin datos offline, mostrar falta recuperable
  de datos/conexión. No descargar preventivamente todo el catálogo al iniciar.
- Con datos guardados y conexión, mostrar local primero y actualizar al abrir;
  fallo de actualización conserva contenido con aviso/reintento. Fin del contenido
  guardado no significa fin del catálogo remoto. Desmarcar no borra inmediatamente
  datos/imágenes; la limpieza conserva lo protegido y actualiza la cobertura declarada.

## Flujo y estados visibles

1. Abrir → consulta vigente → carga inicial o datos locales disponibles.
2. Avanzar → cargar más → conservar tarjetas durante carga/fallo incremental.
3. Buscar/aplicar tipo/limpiar → resultados correspondientes, sin duplicados.
4. Abrir tarjeta → detalle; favorito → guardar estado sin abrir detalle.
5. Error inicial → reintento; error incremental → reintento sin perder tarjetas.

La marca solo se confirma si se guardó correctamente y se refleja en detalle/favoritos.
Una apertura nueva reinicia criterios temporales, conservando datos y favoritos.

## Requisitos trazados al producto

| ID | Fuente | Comportamiento |
| --- | --- | --- |
| HOME-R01 | 1.1 | Listado con carga incremental |
| HOME-R02 | 1.2 | Buscar por nombre o número sin distinguir mayúsculas/minúsculas |
| HOME-R03 | 1.3 | Filtrar por tipo; búsqueda y filtro se combinan mediante AND |
| HOME-R04 | 1.4 | Limpiar búsqueda y restablecer filtros; resultado visible acorde a criterios vigentes |
| HOME-R05 | 1.5 | Carga inicial, incremental, contenido, vacío, error y reintento explícitos |
| HOME-R06 | 1.6 | Cada tarjeta abre el detalle de su ID |
| HOME-R07 | 5.1 | Marcar/desmarcar favorito desde Home y reflejar su estado persistido |

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
| HOME-AC01 | Al recorrer un catálogo con varias páginas, se añade contenido sin IDs duplicados; fin detiene solicitudes y carga incremental no vacía las tarjetas previas |
| HOME-AC02 | Fragmentos de nombre, incluido fragmento interior, coinciden sin distinguir mayúsculas/espacios exteriores; 25, 025 y #025 encuentran solo número 25, sin coincidencias por prefijo |
| HOME-AC03 | Con búsqueda y un tipo elegido, solo aparece la intersección en todo el catálogo; un Pokémon de dos tipos coincide con cualquiera de los suyos y elegir otro sustituye el tipo anterior |
| HOME-AC04 | Limpiar búsqueda deja de aplicar texto; restablecer filtros elimina selección; limpiar ambos devuelve la consulta sin esos criterios |
| HOME-AC05 | Carga inicial, contenido y cero coincidencias se distinguen; fallo inicial muestra error/reintento y reintentar con éxito entrega contenido |
| HOME-AC06 | Fallo de página conserva contenido y permite reintentar; una respuesta de consulta antigua nunca reemplaza resultados/criterios nuevos |
| HOME-AC07 | Abrir tarjeta solicita navegación al ID correcto y regresar conserva criterios/scroll sin repetir navegación |
| HOME-AC08 | Marcar/desmarcar confirma estado persistido, actualiza detalle/favoritos y no abre detalle; fallo de escritura ofrece feedback y mantiene la verdad local |
| HOME-AC09 | Rotación, postura y recuperación de tarea mantienen criterios/scroll; apertura nueva reinicia criterios; ambos idiomas y texto ampliado conservan controles alcanzables |
| HOME-AC10 | El catálogo presenta una entrada por especie, variedad predeterminada y orden nacional ascendente; las formas alternativas no duplican tarjetas ni identidades |
| HOME-AC11 | Escribir no ejecuta búsqueda antes de 300 ms sin cambios; una nueva edición reinicia la espera y respuestas obsoletas no se aplican; limpiar/IME no duplica solicitudes equivalentes |
| HOME-AC12 | Editar el tipo dentro del panel no cambia resultados hasta Aplicar; cerrar por Back/exterior descarta cambios; limpiar texto y restablecer tipo actúan independientemente |
| HOME-AC13 | Tras descargar datos, reabrir offline permite consultarlos con búsqueda/tipo; cobertura parcial se anuncia y vacío parcial no afirma ausencia remota; primer uso sin datos ofrece recuperación |
| HOME-AC14 | Abrir con caché y conexión muestra local y después actualiza; fallo remoto conserva datos y cobertura declarada con reintento; recreación no duplica el refresco en curso |

## Decisiones y límites

Las decisiones funcionales están confirmadas por el usuario el 2026-09-30.
No quedan preguntas de producto propias de esta área. Nuevas dudas que alteren
comportamiento o aceptación deben consultarse; no cambiar requisitos para acomodar
una implementación. Pendientes técnicos y verificación se registran en [PLAN.md](PLAN.md);
trabajo desglosado y progreso en [TASKS.md](TASKS.md).
