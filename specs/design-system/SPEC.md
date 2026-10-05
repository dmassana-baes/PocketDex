# Sistema visual compartido

Estado: **definición funcional acordada; implementación parcial, cierre visual y validación pendientes**. Fecha: **2026-09-30**.

Fuentes: [PDF](../../docs/pocket_dex_definition.pdf),
[Figma](https://www.figma.com/design/0M4IjLeiuGIFocB4qDQuRV/Pokedex-App---UI?node-id=13-285)
y [guías mobile](../../docs/MOBILE_GUIDELINES.md).
Documentos relacionados: [PLAN técnico](PLAN.md), [tareas](TASKS.md) e [índice](../README.md).

## Objetivo y alcance

Definir apariencia y comportamiento visual consistentes para todas las áreas de
PocketDex según Figma y el PDF. Es la primera entrega de implementación; no se
creará una biblioteca nueva en Figma. No añade funcionalidades fuera del PDF.

## Reglas funcionales y decisiones confirmadas

- DS-04: conservar lenguaje visual con ajustes mínimos de tamaño/contraste.
- DS-06: tema claro incluso con sistema oscuro; español/inglés según idioma del
  sistema, inglés de respaldo; etiquetas de tipos/estadísticas y plurales traducidos.
- Número visible con # y mínimo tres dígitos: #001, #025, #1000, sin truncar.
- DS-07: primer tipo ordenado por su slot en la fuente es el color principal de
  tarjeta/gradiente; mostrar las etiquetas de ambos tipos. Un tipo sin estilo
  conocido conserva su etiqueta con aspecto neutro, sin fingir otro tipo.
- DS-08: barras con referencia visual 255 compartida entre detalle/comparador;
  0 no rellena, 100 ocupa 39.22 %, 255 llena y valores superiores conservan su
  número con barra llena. No afirmar que 255 sea máximo comprobado del catálogo.
- DS-09: barra inferior bajo 600 dp de ancho de ventana, lateral desde 600 dp;
  considerar espacio útil, bisagras/insets, no solo orientación/dispositivo.

## Apariencia visual de referencia

Fondo/superficie White #FFFFFF; superficies suaves Gray/100 #F2F4F7; separadores
decorativos Gray/200 #DFE4EC; Gray/300 #8D9DB9 no sirve como texto informativo sobre
blanco. Texto principal Gray/500 #283141, secundario Gray/400 #4E5F7E; acción Yellow
#FFCC00 con texto Gray/500. Gray/600 #171C26 y Black #000000 para texto sobre tipos
cuando se necesitan. Conservar color principal sin texto ilegible sobre imágenes/gradientes.

| Tipo | Español / inglés | Color observado del icono / acento | Texto sobre acento sólido / contraste |
| --- | --- | --- | --- |
| bug | Bicho / Bug | #83C300 | Gray/500 / 6.08:1 |
| dark | Siniestro / Dark | #5B5466 | White / 7.23:1 |
| dragon | Dragón / Dragon | #006FC9 | White / 5.10:1 |
| electric | Eléctrico / Electric | #FBD100 | Gray/500 / 8.86:1 |
| fairy | Hada / Fairy | #FB89EB | Gray/500 / 6.15:1 |
| fighting | Lucha / Fighting | #E0306A | Black / 4.80:1 |
| fire | Fuego / Fire | #FF9741 | Gray/500 / 6.07:1 |
| flying | Volador / Flying | #89AAE3 | Gray/500 / 5.56:1 |
| ghost | Fantasma / Ghost | #4C6AB2 | White / 5.23:1 |
| grass | Planta / Grass | #38BF4B | Gray/500 / 5.43:1 |
| ground | Tierra / Ground | #E87236 | Gray/600 / 5.60:1 |
| ice | Hielo / Ice | #4CD1C0 | Gray/500 / 6.97:1 |
| normal | Normal / Normal | #919AA2 | Gray/500 / 4.58:1 |
| poison | Veneno / Poison | #B567CE | Gray/600 / 4.71:1 |
| psychic | Psíquico / Psychic | #FF6675 | Gray/500 / 4.61:1 |
| rock | Roca / Rock | #C8B686 | Gray/500 / 6.53:1 |
| steel | Acero / Steel | #5A8EA2 | Gray/600 / 4.73:1 |
| water | Agua / Water | #3692DC | Gray/600 / 5.12:1 |

Roboto Condensed, regular 400 y medio 500, disponible sin conexión:

| Jerarquía visual | Tamaño / interlineado / espaciado de letras (sp) |
| --- | --- |
| Encabezado principal | 36 / 44 / 0 |
| Título de pantalla secundaria | 22 / 28 / 0 |
| Título medio | 16 / 24 / 0.15 |
| Cuerpo grande | 16 / 24 / 0.5 |
| Cuerpo medio | 14 / 20 / 0.25 |
| Cuerpo pequeño | 12 / 16 / 0.4 |
| Etiqueta grande | 14 / 20 / 0.1 |
| Etiqueta media | 12 / 16 / 0.5 |
| Etiqueta pequeña accesible | 12 / 16 / 0.5 (ajuste del original de 10 sp) |


Espaciados de referencia 4/8/12/16/24 dp; margen 24 dp en ventana de 360 dp.
Radio 16 dp en tarjetas/campos/botones, 8 dp en chips y 28 dp arriba del sheet.
Iconos habituales 24 dp y de tipo 16 dp; targets interactivos mínimos 48 × 48 dp,
sin solapamiento. Tarjetas y textos crecen/refluyen; los 152 × 100 dp de Figma no
son un tamaño universal. Sprite de tarjeta 56 dp y detalle 260 dp son referencias,
adaptadas al espacio útil. Gradiente vertical: acento opaco al inicio y mitad,
transparente al final sobre superficie blanca; verificar legibilidad por región.

## Estados y comportamiento visual

- Tarjeta: nombre/número/tipos/imagen y favorito; abrir y cambiar favorito son
  acciones separadas. Tipo seleccionado/favorito tienen etiqueta/semántica accesible.
- Botón primario Yellow, secundario neutro con borde y Ghost sin fondo; habilitado,
  deshabilitado y ocupado. Ocupado muestra progreso conservando etiqueta/espacio;
  deshabilitado/ocupado no ejecutan la acción incompatible.
- Campo de búsqueda: vacío/contenido/foco/deshabilitado y limpiar; tipo seleccionado
  o no seleccionado. No confundir texto provisional y búsqueda aplicada de la feature.
- Imagen: carga/disponible/error; conservar espacio y placeholder, sin éxito ficticio
  ni ocultar datos útiles. Silueta/revelación del quiz no filtra respuesta correcta.
- Estadística/comparación: valores reales o faltante explícito; empate/valor superior
  comunicado con texto, no solo color. Barras acompañan el número, no lo reemplazan.
- Carga/vacío/error/reintento y aviso sobre contenido son distinguibles; error de
  actualización no elimina automáticamente contenido válido.
- App bar y navegación inferior/lateral etiquetan acciones y destino seleccionado.

## Requisitos funcionales identificados

| ID | Comportamiento |
| --- | --- |
| DS-R01 | Apariencia compartida de tema, tipos, jerarquía y espaciado |
| DS-R03 | Estados de carga, vacío, error, selección, deshabilitado y progreso distinguibles |
| DS-R04 | Textos traducidos y acciones/resultados accesibles sin depender del color |
| DS-R06 | Imagen dinámica con carga/error; tipografía e iconos estáticos disponibles offline |
| DS-R07 | Tema claro, español/inglés y adaptación vertical/horizontal/foldable |
| DS-R08 | Número con # y mínimo tres dígitos; inglés de respaldo |
| DS-R09 | Ajustes mínimos de tamaño/contraste conservando el lenguaje visual |
| DS-R10 | Navegación inferior/lateral según ancho disponible sin pérdida de contexto |

DS-R02 y DS-R05 son condiciones técnicas conservadas en PLAN.md.

## Accesibilidad y adaptación

Texto normal ≥4.5:1, texto grande ≥3:1 y controles/iconos informativos ≥3:1 frente
a su fondo. Los ratios de la tabla se muestran redondeados; cumplimiento sin redondear.
Placeholder Gray/400 sobre blanco. Sobre fondos con alpha, gradiente o imagen,
verificar región real y, si hace falta, superficie textual neutra o ajuste mínimo.
Texto ampliado sin recortes, orden de foco lógico y TalkBack; ningún resultado solo
por color o gesto. Vertical/horizontal, foldables y ventanas redimensionadas mantienen
acciones alcanzables sin bisagras, teclado o barras del sistema superpuestos.

No solicita permisos runtime ni añade cuentas, telemetría o trabajos en segundo plano.
Las acciones y el estado pertenecen a cada funcionalidad, con continuidad según sus specs.

## Referencia y discrepancias

Se mantienen Home 13:450, filtros 18:2042, detalle 31:1845, comparador 74:94826,
quiz 31:3012/revelación 31:3078 y favoritos 31:3186 del Figma. Los fixtures visuales
no son datos reales. Se excluyen Random, filtros avanzados, dado/ganador global,
secciones extra de detalle; el quiz utiliza cuatro opciones aunque el frame muestre tres.
El detalle de inspección, tokens de código y procedencia de assets está en PLAN.md.

Se conservan los iconos exactos del Figma. El usuario aportó los 18 iconos de tipos
y autorizó incorporarlos; se rechazó reemplazarlos por el set MIT recomendado cuyo dibujo difiere.
Los controles suministrados y la Poké Ball están incorporados; filtro y
comprobación visual permanecen pendientes. No se presentan las
previews parciales como cumplimiento de la apariencia final.

## Criterios de aceptación funcional

| ID | Escenario y resultado esperado |
| --- | --- |
| DS-AC01 | En las pantallas de las áreas, colores, jerarquía, formas y espaciado corresponden a los nodos citados; toda adaptación queda documentada |
| DS-AC02 | Home/favoritos usan la misma tarjeta y acciones independientes de abrir/marcar |
| DS-AC03 | Una acción ocupada/deshabilitada no ejecuta otra acción incompatible; seleccionado, error y favorito se reconocen por semántica/texto |
| DS-AC04 | En vertical/horizontal, foldable plegado/desplegado y al cambiar postura/tamaño, el contenido se redistribuye sin recortes ni controles sobre zonas ocluidas; validar texto ampliado e insets |
| DS-AC05 | Sin conexión, tipografía e iconos estáticos se muestran; imágenes dinámicas fallidas muestran estado previsto sin bloquear la pantalla |
| DS-AC06 | El tema permanece claro incluso con sistema oscuro; locales español/inglés muestran sus recursos y el cambio de idioma no pierde acciones en curso; verificar contraste, fuente e iconos antes del cierre visual |
| DS-AC07 | Números 1, 25 y 1000 se muestran como #001, #025 y #1000 en todos los consumidores; un idioma del sistema distinto de español/inglés utiliza recursos en inglés |
| DS-AC08 | Los 18 tipos usan su clave/etiqueta/acento definido; un Pokémon con dos tipos muestra ambos y usa como color principal el tipo de menor slot remoto; un estilo desconocido conserva etiqueta con aspecto neutro |
| DS-AC09 | Textos normales alcanzan 4.5:1, grandes 3:1 y controles informativos 3:1 según aplicación; placeholder Gray/400, targets 48 × 48 dp sin solapamiento y texto ampliado sin clipping; verificar fondos sólidos y gradientes separadamente |
| DS-AC10 | La tipografía y los iconos estáticos conservan su apariencia y están disponibles sin conexión; pesos regular/medio legibles |
| DS-AC11 | Detalle/comparador usan referencia visual fija de 255: 0 → 0 %, 100 → 39.22 %, 255 → 100 %; un valor superior conserva su número y llena la barra; faltante no se representa como cero ni como barra llena |
| DS-AC12 | Con ancho de ventana 599 dp hay barra inferior y desde 600 dp lateral; redimensionar o plegar conserva destino/estado y no sitúa controles sobre bisagra ni zonas ocluidas |

## Decisiones y límites

Las decisiones funcionales están confirmadas por el usuario el 2026-09-30.
No quedan preguntas de producto propias de esta área. Nuevas dudas que alteren
comportamiento o aceptación deben consultarse; no cambiar requisitos para acomodar
una implementación. Pendientes técnicos y verificación se registran en [PLAN.md](PLAN.md);
trabajo desglosado y progreso en [TASKS.md](TASKS.md).
