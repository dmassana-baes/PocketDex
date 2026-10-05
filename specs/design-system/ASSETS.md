# Inventario de recursos del sistema visual

Fecha de revisión: 2026-10-01. Referencia: [Figma original](https://www.figma.com/design/0M4IjLeiuGIFocB4qDQuRV/Pokedex-App---UI?node-id=13-285).

## Fuente incorporada

Roboto Condensed 3.016, regular 400/medium 500, archivos estáticos oficiales en
`core/designsystem/src/main/res/font/`. Procedencia, hashes y licencia exacta:
[registro de fuentes](../../core/designsystem/licenses/README.md).
OFL 1.1 se conserva también como asset empaquetable. No hay fuente remota en runtime.

## Iconos de tipos: SVG aportados y XML incorporados

El usuario aportó los 18 SVG y autorizó su conversión/incorporación. Se localizaron
en `docs/assets/Pokedex App - Type Icons/Type=<Tipo>.svg`; la carpeta indicada
`core/designsystem/src/main/assets` contenía únicamente la licencia tipográfica.
Se conservaron los SVG originales y se generaron
`core/designsystem/src/main/res/drawable/pd_type_<clave>.xml`.
Los IDs de recorte de los SVG corresponden a los nodos del component set `18:2521`.
Los archivos no declaran autor/licencia de terceros; no se les atribuye una licencia
inventada. La autorización del usuario para esta incorporación se registra por
separado de la autoría/licencia externa no declarada. Por acuerdo del usuario,
esta ausencia se registra sin bloquear el cierre; se conserva la OFL tipográfica.

Conversión reproducible desde la raíz: `./scripts/convert-type-icons.ps1`.
Se conservan viewport y tamaño 16, orden y datos de los 45 trazados, colores
opacos, reglas nonZero/evenOdd y recorte rectangular. El script rechaza elementos
o atributos fuera del subconjunto suministrado antes de escribir archivos.
Inventario con hashes SVG/XML: [type-icons.csv](type-icons.csv).
La revisión estática compara los 18 pares; no se han renderizado ni compilado.
VectorDrawable y fillType están soportados por el SDK mínimo 24:
[referencia Android](https://developer.android.com/reference/android/graphics/drawable/VectorDrawable).

| Clave | Nodo original | Origen / autor / permiso | Estado |
| --- | --- | --- | --- |
| bug | 18:2508 | SVG aportado por usuario; autor/licencia no declarados | XML incorporado |
| dark | 18:2520 | SVG aportado por usuario; autor/licencia no declarados | XML incorporado |
| dragon | 18:2519 | SVG aportado por usuario; autor/licencia no declarados | XML incorporado |
| electric | 18:2518 | SVG aportado por usuario; autor/licencia no declarados | XML incorporado |
| fairy | 18:2517 | SVG aportado por usuario; autor/licencia no declarados | XML incorporado |
| fighting | 18:2515 | SVG aportado por usuario; autor/licencia no declarados | XML incorporado |
| fire | 18:2514 | SVG aportado por usuario; autor/licencia no declarados | XML incorporado |
| flying | 18:2512 | SVG aportado por usuario; autor/licencia no declarados | XML incorporado |
| ghost | 18:2511 | SVG aportado por usuario; autor/licencia no declarados | XML incorporado |
| grass | 18:2509 | SVG aportado por usuario; autor/licencia no declarados | XML incorporado |
| ground | 18:2507 | SVG aportado por usuario; autor/licencia no declarados | XML incorporado |
| ice | 18:2516 | SVG aportado por usuario; autor/licencia no declarados | XML incorporado |
| normal | 18:2506 | SVG aportado por usuario; autor/licencia no declarados | XML incorporado |
| poison | 18:2513 | SVG aportado por usuario; autor/licencia no declarados | XML incorporado |
| psychic | 18:2505 | SVG aportado por usuario; autor/licencia no declarados | XML incorporado |
| rock | 18:2510 | SVG aportado por usuario; autor/licencia no declarados | XML incorporado |
| steel | 18:2504 | SVG aportado por usuario; autor/licencia no declarados | XML incorporado |
| water | 18:2503 | SVG aportado por usuario; autor/licencia no declarados | XML incorporado |

Decisión del usuario: **mantener los iconos exactos de Figma; aportará su fuente o
permiso de uso**. Se rechazó la alternativa
[James Watkins / MIT](https://github.com/partywhale/pokemon-type-icons)
porque su dibujo difiere de la referencia que el usuario quiere conservar.
No se infiere una licencia del mero acceso/exportación de Figma.
El set [duiker101](https://github.com/duiker101/pokemon-type-svg-icons) no acredita
que los dibujos de este Figma procedan de él; no se le atribuye una licencia MIT.

## Controles, destinos y decoración

Fuentes: `docs/assets/Pokedex App - App Icons/` y `specs/home/assets/filter.svg`;
autor/licencia no declarados.
Autorización de incorporación del usuario y acuerdo de no bloquear el cierre por
esa ausencia. No se atribuyen licencias inventadas ni se elimina la OFL de las fuentes.
Se conservan todos los originales. Inventario/hashes: [app-icons.csv](app-icons.csv).

| SVG | Nodo indicado por máscara / referencia | Recurso / estado |
| --- | --- | --- |
| Name=ArrowBack.svg | 101:6239 | pd_arrow_back.xml |
| Name=battery.svg | No declarado en SVG | Excluido: Random fuera del PDF / iconos de sistema |
| Name=ChevronDown.svg | 101:6243 | pd_chevron_down.xml |
| Name=Close.svg | 101:6241 | pd_close.xml |
| Name=Comparator.svg | 101:6242 | pd_comparator.xml |
| Name=Heart.svg | 101:6250 | pd_heart.xml |
| Name=HeartFilled.svg | 101:6251 | pd_heart_filled.xml |
| Name=Home.svg | 101:6247 | pd_home.xml |
| Name=HomeFilled.svg | 101:6248 | pd_home_filled.xml |
| Name=Quiz.svg | 101:6249 | pd_quiz.xml |
| Name=QuizFilled.svg | 101:6246 | pd_quiz_filled.xml |
| Name=Random.svg | 101:6240 | Excluido: Random fuera del PDF / iconos de sistema |
| Name=Refresh.svg | 101:6245 | pd_refresh.xml |
| Name=Search.svg | 101:6244 | pd_search.xml |
| Name=signal.svg | No declarado en SVG | Excluido: Random fuera del PDF / iconos de sistema |
| Name=wifi.svg | No declarado en SVG | Excluido: Random fuera del PDF / iconos de sistema |
| pokeball 1.svg | 18:2069 | pd_pokeball_decoration.xml |
| specs/home/assets/filter.svg | Máscara SVG 53:43672; control Home 13:5437 como referencia previa | pd_filter.xml; suministrado por el usuario el 2026-10-05 |

Conversión reproducible: `./scripts/convert-app-icons.ps1`. Se generan 12 iconos
monocromos originales, el filtro suministrado en `specs/home/assets/filter.svg`
y una ilustración: 14 XML/16 trazados. Se preservan tamaño/viewports
24 o 88, datos/orden de paths y colores originales. Las máscaras son alpha opaco
sobre rectángulo completo; se conservan como recorte rectangular, sin sustituir
máscaras complejas por clips. El script rechaza atributos/geometría no soportados.
No se convierten Random ni batería/señal/Wi-Fi: el alcance del PDF excluye el
primero y Android dibuja las barras del dispositivo.

PocketDexIcon aplica tintado semántico del tema para controles (Gray/500 o
Gray/400), conservando geometría y contraste. Los iconos de tipos conservan sus
colores originales mediante Image. ChevronDown queda disponible, sin añadir un
menú ajeno al PDF. Las previews de navegación suministran Home/HomeFilled,
Comparator, Quiz/QuizFilled y Heart/HeartFilled según selección externa.

FavoriteButton usa corazón lleno/vacío y descripción traducida de acción/estado,
con target 48 dp; Search/Close y ArrowBack sustituyen los controles textuales
provisionales. Refresh acompaña reintento inline. Los callbacks y el bloqueo
ocupado/deshabilitado continúan controlados por el consumidor.

Poké Ball: SVG blanco opaco convertido a pd_pokeball_decoration.xml; en tarjeta
se dibuja como decoración sin descripción/click, 88 dp y desplazamientos +11/+20 dp.
La lectura de Figma 18:2069 confirmó fill opacity 0.12 en sus tres vectores blancos;
se aplica alpha 0.12 una sola vez en Compose, sin modificar la geometría SVG.

La revisión estática verifica equivalencia de trazados/rellenos y referencias
locales; no acredita renderizado ni cierre visual. El filtro suministrado se convierte
sin redibujar; autor/licencia no declarados, con autorización de incorporación del
usuario. `FilterSourcePath` permite reproducir su conversión con el mismo script.
No se presentan las previews como una validación ejecutada. El placeholder de
fallo de imagen conserva estado explícito y no representa un sprite disponible.
## Imágenes dinámicas

No se empaquetaron sprites del Figma ni del catálogo. PokemonImage recibe
Loading/Error/Ready(Painter) del consumidor y no realiza IO ni promete retención
offline. La resolución Coil y la protección de imágenes de favoritos/quiz
pertenecen a los incrementos de datos/features. Los fixtures de pruebas usan un
ColorPainter local solo para comprobar descripción/silueta; no son assets del producto.
