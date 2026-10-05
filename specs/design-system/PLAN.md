# Plan técnico: Sistema visual compartido

Estado: **biblioteca/componentes, iconos e ilustración implementados; validación de ejecución pendiente**. Fecha: **2026-10-05**.

Contrato funcional: [SPEC.md](SPEC.md). Seguimiento: [TASKS.md](TASKS.md).
Módulo principal objetivo: `:core:designsystem`.
Referencias técnicas: [constitución](../../constitution.md) y [AGENTS.md](../../AGENTS.md).

## Punto de partida y dependencias

La base al planificar era `:app` con pantalla/tema/pruebas de ejemplo. Ahora existe
`:core:designsystem`, registrado y consumido por el tema de `:app`. La pantalla de
ejemplo continúa; las funcionalidades aún no están implementadas. Revisión estática
no acredita compilación ni compatibilidad binaria.

Sin dependencia de otras áreas. Debe abrir el trabajo de implementación; la definición técnica previa no exige implementar la base primero.

## Arquitectura y entrega técnica

Crear `:core:designsystem` primero, como biblioteca Android/Compose sin dependencias
internas. No incluir dominio, ViewModels, navegación real, red ni persistencia.
Usar el stack Compose existente; centralizar aliases en el catálogo y comprobar
documentación oficial antes de añadir una dependencia necesaria. Ajustar nombres
de API durante implementación sin cambiar el contrato funcional.

Ubicación implementada: `core/designsystem/build.gradle.kts`, paquetes `theme`,
`components`, `model`, `preview`, recursos `res/font`, `res/drawable`, `res/values`
y `res/values-es`; `res/drawable` contiene 18 iconos de tipos convertidos desde SVG. Versiones base y toolchains
siguen la constitución; no actualizar dependencias por iniciativa propia.

Usar `PocketDexTheme`, tokens de espaciado/formas/tipos y adaptadores visuales sin
tipos del dominio. Dimensiones dp, texto sp; fuente con pesos 400/500 local para
SDK 24, sin fonts remotas. `PokemonImage` acepta modelo visual/fuente ya resuelta,
estado y descripción. La API actual recibe `Ready(Painter)` ya resuelto por el
consumidor; no instala Coil ni controla retención offline ni inicia repositorios.
Evitar altura fija para texto, targets solapados y padding de insets duplicado.

Las barras reciben valor/escala y estado faltante; fracción `clamp(valor/255, 0, 1)`.
El ganador/empate se recibe ya calculado. Las features eligen estilo del primer
tipo por slot; el módulo no lee DTOs. La app elige navegación según ancho <600/
≥600 dp y suministra callbacks, elementos y región útil; el componente solo dibuja.

## Contratos y decisiones de implementación

Valores de referencia, no una paleta accesible ya validada ni un catálogo completo:

| Familia | Valores observados | Uso visual |
| --- | --- | --- |
| Neutros | White `#FFFFFF`; Gray/100 `#F2F4F7`; Gray/200 `#DFE4EC`; Gray/300 `#8D9DB9`; Gray/400 `#4E5F7E`; Gray/500 `#283141`; Gray/600 `#171C26`; Black `#000000` | Superficies, bordes, texto y estados |
| Acento | Yellow `#FFCC00` | Acciones principales e indicadores de estadísticas |
| Tipos | Paleta completa en la tabla siguiente | Colores observados de iconos; uso como fondo por concretar según componente |
| Espaciado | 4, 8, 12, 16 y 24 unidades en los frames | Padding, gaps y margen horizontal de 24 en ancho 360 |
| Formas | Radio 16 en tarjetas/campos/botones; pills redondeadas | Contenedores reutilizables |
| Imágenes e iconos | Sprite de tarjeta 56; iconos habituales 24; sprite de detalle 260 | Referencias de escala; adaptar a restricciones reales |

La familia observada es **Roboto Condensed**, regular 400 y medium 500:

| Estilo Figma / rol Material 3 | Tamaño / interlineado / tracking, en unidades de referencia |
| --- | --- |
| Display/Small / `displaySmall` | 36 / 44 / 0 |
| Title/Large / `titleLarge` | 22 / 28 / 0 |
| Title/Medium / `titleMedium` | 16 / 24 / 0.15 |
| Body/Large / `bodyLarge` | 16 / 24 / 0.5 |
| Body/Medium / `bodyMedium` | 14 / 20 / 0.25 |
| Body/Small / `bodySmall` | 12 / 16 / 0.4 |
| Label/Large / `labelLarge` | 14 / 20 / 0.1 |
| Label/Medium / `labelMedium` | 12 / 16 / 0.5 |
| Label/Small / `labelSmall` | 10 / 16 / 0.5 |

Convertir tamaños de texto a sp y dimensiones a dp en Compose, verificando
resultado y escalado. No trasladar coordenadas absolutas de los frames como layout.

#### Roles semánticos y accesibilidad

Separar la paleta de referencia de los roles de uso; las features usan roles,
no literales hexadecimales. El tema claro conserva blanco para fondo/superficie,
Gray/100 para superficies suaves y Yellow para acción principal. Texto principal
Gray/500, texto secundario Gray/400 y texto sobre Yellow Gray/500. Reservar
Gray/200 para separadores decorativos; un borde que sea la única forma de
identificar un control requiere contraste suficiente y no usa ese gris tenue.

Mapeo del tema para impedir heredar colores morados del ejemplo:

| Roles Material 3 | Token |
| --- | --- |
| `primary`, `inversePrimary` | Yellow |
| `onPrimary`, `onPrimaryContainer`, `onSecondaryContainer`, `onTertiaryContainer` | Gray/500 |
| `secondary`, `tertiary` | Gray/400 |
| `onSecondary`, `onTertiary` | White |
| `primaryContainer`, `secondaryContainer`, `tertiaryContainer` | Gray/100 |
| `background`, `surface`, `surfaceBright`, `surfaceContainerLowest` | White |
| `onBackground`, `onSurface`, `onSurfaceVariant` | Gray/500 |
| `surfaceVariant`, `surfaceContainerLow`, `surfaceContainer` | Gray/100 |
| `surfaceDim`, `surfaceContainerHigh`, `surfaceContainerHighest` | Gray/200 |
| `outline` / `outlineVariant` | Gray/400 / Gray/200 (el segundo solo decorativo) |
| `inverseSurface` / `inverseOnSurface` | Gray/500 / White |
| `error` / `onError` | Gray/500 / White |
| `errorContainer` / `onErrorContainer` | Gray/100 / Gray/500 |
| `scrim` / `surfaceTint` | Gray/500 / Yellow |

El rol error conserva aspecto neutro y siempre incluye mensaje/icono accesible;
no introduce un rojo sin referencia. Éxito, error, empate y avisos llevan etiquetas
explícitas. No usar el tinte para recolorear automáticamente superficies blancas
de referencia. Scrim observado del sheet: Gray/500 al 16 %; la legibilidad del
contenido activo se comprueba sobre la superficie opaca del sheet.

Aplicar el acuerdo de ajustes mínimos con objetivos comprobables: texto normal
al menos 4.5:1, texto grande al menos 3:1 y controles/iconos informativos al menos
3:1 frente a su fondo; targets de al menos 48 × 48 dp, sin solaparse. Referencias:
[Android accesibilidad](https://developer.android.com/guide/topics/ui/accessibility/apps)
y [WCAG contraste no textual](https://www.w3.org/WAI/WCAG22/Understanding/non-text-contrast.html).

Medición estática de pares opacos mediante luminancia relativa sRGB; no equivale
a validar las pantallas renderizadas ni los gradientes:

| Par texto / fondo | Contraste calculado | Aplicación |
| --- | --- | --- |
| Gray/300 / White | 2.74:1 | No usar para placeholder/texto informativo; usar Gray/400 |
| Gray/400 / White | 6.44:1 | Texto secundario y placeholder legibles |
| Gray/500 / Yellow | 8.65:1 | Texto de acción principal |
| White / Grass | 2.41:1 | Texto normal insuficiente |
| Gray/500 / Grass | 5.43:1 | Alternativa legible conservando fondo |
| White / Water | 3.33:1 | Texto normal insuficiente |
| Gray/500 / Water | 3.92:1 | Tampoco cumple para texto normal |

Conservar el color de tipo como acento y usar el texto sobre acento de la tabla
para fondos sólidos opacos. Los ratios se muestran redondeados solo para lectura;
el cumplimiento se evalúa sin redondear. No aplicar estos pares automáticamente
sobre un gradiente, imagen o superficie con alpha. Elegir texto legible por componente;
si ningún color previsto cumple, colocar la etiqueta en superficie neutra o
ajustar mínimamente el tono de la superficie textual y registrar el cambio.
El icono de tipo puede ser decorativo cuando la etiqueta aporta la misma información.
No depender de sus trazos blancos para identificarlo. Mantener número, nombre,
ambos tipos y acción de favorito legibles; no aplicar una opacidad global para
simular estados si vuelve ilegible contenido informativo. El label de 10 sp del
Figma se eleva a 12 sp cuando comunica información necesaria; los textos y sus
contenedores admiten fontScale sin recortes.

#### Fuente y procedencia de assets

Roboto Condensed se incorporó como archivos estáticos oficiales de Android en
la publicación **3.016**, pesos 400/500. El artefacto variable de Google Fonts
requiere soporte de SDK 26; esta elección admite el mínimo 24 sin descarga en runtime.
Su OFL 1.1 del mismo tag conserva copyright y se incluye también como asset.
Fuente, archivos, hashes y licencia concreta están en el
[registro de fuentes](../../core/designsystem/licenses/README.md).

Los 18 iconos están presentes en Figma; su disponibilidad para exportar no prueba
su licencia. Inventariar también búsqueda, filtro, favorito, Back y destinos.
Cada asset estático tendrá nodo de origen, fuente/autor cuando conste, licencia
o permiso verificable y destino local. No sustituir iconos por otros parecidos
sin registrar la discrepancia. Las imágenes de Pokémon siguen siendo datos
dinámicos, no sprites de ejemplo empaquetados como catálogo ni fuentes temporales
de Figma. Procedencia de estos iconos/ilustraciones sigue pendiente de verificar.

Las siguientes APIs están implementadas en `core/designsystem/src/main/java/`;
los iconos de tipos, controles disponibles y decoración están incorporados:

| Componente | Entrada/salida mínima | Consumidores |
| --- | --- | --- |
| `PocketDexTheme` y tokens | Contenido, configuración visual acordada | App y todas las features |
| `PokemonCard` | Nombre, número formateado, tipos visuales, imagen, favorito y callbacks separados | Home y favoritos; selector cuando encaje |
| `PokemonTypeBadge` / chip seleccionable | Etiqueta, estilo de tipo, selección y callback cuando sea interactivo | Home, detalle y selector |
| `PokemonImage` | Modelo de imagen, descripción o carácter decorativo y estado visual | Tarjetas, detalle, comparador y quiz |
| Campo de búsqueda | Valor, cambio, limpieza, acción IME y habilitación | Home y selector del comparador |
| Botones y `FavoriteButton` | Etiqueta, acción, habilitación, progreso y estado marcado | Features; negocio en sus ViewModels |
| App bar / navegación visual | Título, acciones y elementos con selección/callbacks | App y pantallas; rutas fuera del módulo |
| Fila de estadística | Etiqueta, valor y escala visual | Detalle |
| Fila de comparación | Etiqueta, dos valores y resultado ya calculado | Comparador |
| Estado de pantalla | Mensaje, estado y acción opcional | Carga, vacío, error y reintento |

Las opciones del quiz reutilizan botones y estados; su máquina de estados permanece
en quiz. Extraer un componente adicional solo cuando su comportamiento lo justifique.
Las escalas de barras no deben inventar máximos ni cambiar el valor numérico mostrado.

Origen de la paleta: fills de los 18 iconos bajo nodo 18:2597; Gray/600 y Black son estilos locales inspeccionados.
Gradiente de detalle 31:2593: color principal en stops 0 y 0.5; White alpha 0 en stop 1, dirección vertical.
Paleta y tipografía funcional completa en SPEC.md; no copiar coordenadas absolutas ni URLs temporales a código.

Criterios técnicos: no dependencias internas, API visual sin negocio/IO/DI, previews deterministas y procedencia reproducible de recursos. Estos controles complementan DS-AC02/DS-AC10 del spec.

Condiciones técnicas históricas: DS-R02 exige componentes sin estado de negocio,
con Modifier, datos visuales, estado y callbacks explícitos; DS-R05 exige previews
deterministas sin red/DI para variantes y contenido largo. Se conservan aquí para
separar implementación de requisitos observables.

## Estado, concurrencia y operación mobile

| Aspecto | Decisión o condición comprobable |
| --- | --- |
| Ciclo de vida y estado | Componentes reflejan entradas; no contienen sesiones, favoritos ni operaciones persistentes |
| Conectividad | Previews y assets estáticos funcionan sin red; placeholders no convierten fallo de imagen en éxito |
| Persistencia y migraciones | No aplica al módulo; no almacena datos ni controla caché permanente |
| Estados de interfaz | Variantes de carga, error, vacío, seleccionado y ocupado; feedback textual accesible |
| Navegación e interrupciones | Callbacks; el componente no controla el back stack ni intercepta Back globalmente |
| Teclado, foco y repetición | Campo controlado, IME configurable y foco accesible; estado ocupado deshabilita acciones incompatibles |
| Pantallas, orientación e insets | Vertical/horizontal y foldables; layout según espacio disponible, sin alturas fijas para texto; consumir insets una vez y evitar controles sobre bisagras o zonas ocluidas |
| Accesibilidad | Targets mínimos de 48 × 48 dp sin solapamiento, semántica de favorito/selección y orden lógico; pares de contraste definidos arriba, verificación renderizada pendiente |
| Permisos y capacidades | Sin permisos de ejecución propios; sin fuentes remotas obligatorias |
| Rendimiento y recursos | Listas lazy en features; componentes sin IO bloqueante ni bitmaps completos innecesarios |
| Segundo plano | No aplica: biblioteca de presentación, sin trabajos programados |
| Privacidad y seguridad | Sin telemetría ni logs de datos; assets públicos/locales sin credenciales |
| Idiomas y formatos | Español e inglés según sistema, inglés de respaldo, recursos y pluralización; número con # y mínimo tres dígitos, sin truncamiento; probar cambio de idioma y textos largos |
| Validación mobile | Revisar ancho 360, móvil estrecho, horizontal, foldable plegado/desplegado, ventana redimensionada, texto ampliado y TalkBack |

## Pendientes técnicos y riesgos

Los 18 SVG aportados por el usuario se convirtieron e incorporaron con autorización
explícita, conservando trazados, recorte y colores; badges/chips/tarjetas los resuelven
localmente por defecto, sin tintado. Fuente y hashes en [ASSETS.md](ASSETS.md).
Autor/licencia externa no están declarados en los SVG y se registran sin bloquear
el cierre por acuerdo del usuario. La OFL de las fuentes continúa empaquetada.
La alternativa MIT con dibujo diferente permanece rechazada. Se aportaron 12
iconos de controles/destinos y Poké Ball, convertidos por `scripts/convert-app-icons.ps1`.
La exportación previa del filtro fue rechazada por cuota Starter. El 2026-10-05
el usuario suministró `specs/home/assets/filter.svg`: se incorpora como `pd_filter.xml`
y `PocketDexIconAsset.Filter`, conservando trazado, relleno y recorte rectangular.
El conversor admite `FilterSourcePath`; procedencia/hashes en ASSETS/app-icons.csv.
No se incorpora un sustituto. FavoriteButton usa Heart/HeartFilled, Back ArrowBack,
búsqueda Search/Close y reintento inline Refresh. Las previews muestran destinos con
sus variantes y siguen sin acreditar cierre visual. DS-T06 y DS-T01 terminadas.
Contraste sólido medido no acredita gradientes/renderizados ni adaptación integral
de features, bisagras/insets o continuidad de estados aún no implementados.

Cerrar estos puntos en el incremento afectado y actualizar este plan sin alterar
la spec funcional. La preparación de PLAN/TASKS no resuelve por sí sola esas verificaciones.

## Estrategia de pruebas y revisión

Escribir pruebas Compose de semántica de favorito/selección, callbacks independientes,
estados ocupado/deshabilitado y contenido largo. Previews sin red/DI para variantes
y idiomas. Revisión estática de imports/API/recursos/licencias y fórmulas. Validación
manual de contraste renderizado, gradientes, escalado, TalkBack, insets y foldables.
No imponer snapshots ni confundir render con prueba de persistencia.

Usar JUnit 4, Coroutines Test y Turbine, fakes deterministas; MockK solo si aporta
valor. Escribir pruebas significativas, sin ejecutarlas durante sesión del agente.
El desarrollador ejecuta compilación y pruebas y entrega evidencia; no afirmar
que un criterio pasa solo porque su prueba está escrita.

## Comandos y validación del desarrollador

El módulo y sus fuentes de pruebas ya existen. El agente no ejecuta estos comandos. `connectedDebugAndroidTest`
solo aplica si hay pruebas instrumentadas y dispositivo/emulador preparado.

```powershell
.\gradlew.bat :core:designsystem:testDebugUnitTest
.\gradlew.bat :core:designsystem:connectedDebugAndroidTest
.\gradlew.bat :app:assembleDebug
```

`:app` consume el nuevo tema; compilarla comprobará esa integración, no las features
ni el cierre visual. Para pruebas UI de feature indicar su tarea concreta únicamente cuando
se añadan. Registrar entorno, comandos, resultado real y limitaciones en TASKS.md.

## Secuencia de entrega

### Evidencia técnica del incremento

- Android library AGP 9 y Kotlin integrado, sin Kotlin Android/kapt; Java 11 y
  SDK mínimo 24 conservados. Solo se añaden alias library y Compose foundation
  resueltos por las versiones/BOM existentes, sin módulos internos adicionales.
  Referencias: [bibliotecas Android](https://developer.android.com/studio/projects/android-library),
  [Kotlin integrado](https://developer.android.com/build/migrate-to-built-in-kotlin) y
  [BOM](https://developer.android.com/develop/ui/compose/bom).
- Roboto Condensed **3.016 estático**, 400/500 desde la publicación oficial; OFL
  del mismo tag y hashes en [licenses/README.md](../../core/designsystem/licenses/README.md).
  FontFamily utiliza recursos locales compatibles con SDK 24, sin ejes variables
  que requieren SDK 26: [fuentes Compose](https://developer.android.com/develop/ui/compose/text/fonts).
- `PocketDexTheme` configura todos los roles de color definidos aquí y todos los
  roles tipográficos. `PokemonType` contiene 18 acentos/onAccent/etiquetas;
  desconocido utiliza aspecto neutro. Formato y escala residen en `model/VisualFormatting.kt`.
- MainActivity fija la apariencia clara de las barras del sistema con SystemBarStyle,
  conservando scrim oscuro de navegación para SDK 24 donde sus iconos son claros.
  Referencia: [implementación oficial de AndroidX Activity](https://github.com/androidx/androidx/blob/androidx-main/activity/activity/src/main/java/androidx/activity/EdgeToEdge.kt).
- Controles, imagen, tarjeta, estadísticas/comparación, estados y navegación visual
  usan datos/callbacks. No hay IO, permisos, ViewModels ni dependencias de features.
  Navegación recibe presentación y slots; la app futura mide ancho, evita bisagras
  y consume insets una vez. El componente suministra WindowInsets cero.
- Ajustes de referencia: etiqueta mínima 12 sp, texto onAccent legible, borde del
  campo Gray/400, tarjeta sin altura fija y favorito en superficie neutra separada.
  Botones/selección/favorito usan semántica explícita:
  [semántica Compose](https://developer.android.com/develop/ui/compose/accessibility/semantics).
- Previews en inglés/español/francés de respaldo, sistema oscuro/tema claro,
  horizontal y texto 2×; no se han renderizado. Pruebas JUnit de formato/escala/umbral
  y Compose de callbacks, bloqueo, selección, silueta y navegación escritas.
  Incluyen búsqueda controlada con IME/limpiar y favorito ocupado. Referencia:
  [APIs de pruebas Compose](https://developer.android.com/develop/ui/compose/testing/apis).

Validación manual: ejecutar los tres comandos anteriores; abrir las previews en
Android Studio y comparar con los nodos citados. Revisar SDK 24 y actual, español/
inglés/francés, noche, 360 dp y móvil más estrecho, 599/600 dp, horizontal con poca
altura, fontScale 2×, TalkBack, teclado y back. La validación integral de bisagras,
insets y restauración de destinos corresponde también al host de app y sus features;
no la acredita esta biblioteca aislada. Verificar el filtro incorporado y
comparar los 18 vectores con sus SVG antes del cierre visual, especialmente los
huecos evenOdd en Dark/Electric/Fairy/Ground/Normal/Rock/Steel y el recorte a 16.

Ilustración de tarjeta: `pd_pokeball_decoration.xml`, 88 dp, alineada abajo/derecha
con desplazamientos +11/+20 dp y alpha 0.12. Lectura directa de Figma 18:2069:
los tres vectores blancos tienen fill opacity 0.12. El SVG suministrado conserva
blanco opaco y se aplica alpha en Compose; no se duplica la transparencia.
Los iconos monocromos se tintan con el rol semántico accesible; tipos/Poké Ball
utilizan Image sin tintado. No hay nuevos permisos, versiones ni dependencias.
Las APIs de colores para IconToggleButton se contrastaron con
[IconButtonDefaults oficial](https://developer.android.com/reference/kotlin/androidx/compose/material3/IconButtonDefaults).

Concretar pendientes y contratos → implementar el incremento en los archivos
responsables → escribir pruebas → revisar estáticamente → entregar validación
al desarrollador. El detalle y dependencias de cada paso están en TASKS.md.
