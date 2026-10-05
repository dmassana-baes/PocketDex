# Referencias visuales del comparador

Inspección de archivos suministrados por el usuario: **2026-10-05**.
Estas referencias locales completan la inspección de COMPARE-T03, antes bloqueada
por la cuota de Figma. No son capturas de la aplicación implementada ni evidencia
de ejecución. El contrato funcional sigue en [SPEC.md](SPEC.md).

## Pantallas inspeccionadas

Las cinco imágenes tienen **360 × 800 píxeles**. Las medidas observadas describen
la referencia; no establecen por sí mismas dimensiones Android en dp.

| Archivo en `assets/screens-designs/` | Estado y lenguaje visual observado |
| --- | --- |
| `Comparator.png` | Preparación con dos slots vacíos: fondo blanco, título grande, texto introductorio azul grisáceo, dos contenedores grises redondeados, botones blancos de añadir y CTA amarillo deshabilitado; navegación inferior. |
| `Comparator - Choosing Pokemon.png` | Selector presentado como hoja con esquinas superiores redondeadas, indicador de arrastre, título, búsqueda con borde y texto de orientación. |
| `Comparator - Pokemon Chosen.png` | Búsqueda con teclado abierto y candidato compacto: tarjeta de color por tipo, nombre, número #008, etiqueta de tipo, sprite y decoración de Poké Ball. |
| `Comparator - Manual.png` | Dos slots ocupados con sprites centrados sobre gris, acción de reemplazo y CTA amarillo habilitado. |
| `Compare - Results.png` | Back, título, dos miniaturas sobre gris con nombres y seis filas compactas con etiqueta, ambos valores y barras amarillas/oscuras. |

## Aplicación y diferencias registradas

Reutilizar tema claro, tipografía, colores, radios, imágenes, etiquetas de tipos,
búsqueda y CTA del design system. La navegación corresponde al host adaptativo:
barra inferior en ventanas estrechas y lateral desde 600 dp de espacio útil.
Las barras de estado, navegación del sistema y teclado de las referencias son
elementos del sistema operativo, no recursos que deba reproducir la app.

Las decisiones funcionales acordadas prevalecen en estos elementos de referencia:

- El dado central corresponde a selección aleatoria, excluida del comparador.
- «Wartortle wins!» no se incorpora: solo existe resultado por estadística,
  con empate explícito y sin ganador global.
- Las barras usan la referencia visual fija de 255 compartida con detalle,
  conservan números reales e identifican el mayor también mediante texto.
- El corazón de resultados no introduce favoritos de comparaciones.
- Reemplazar y quitar requieren acciones accesibles; su función no depende
  únicamente del icono ni del color.

El incremento COMPARE-T10/T11/T12 sustituye la composición anterior: incorpora
título e introducción, slots Gray100 de mínimo 190 dp con sprites centrados,
reemplazo/quitar mediante iconos con etiquetas y CTA de ancho completo. El
selector presenta una superficie de hoja redondeada con indicador decorativo,
búsqueda y cuadrícula adaptativa de tarjetas de color por primer tipo, número,
ambas etiquetas de tipo y decoración vectorial de Poké Ball. Resultados incorpora
dos miniaturas sobre gris, nombres y seis filas compactas con barras 255.

Adaptaciones concretas: los iconos tienen áreas táctiles de 48 dp; la altura
crece si hay error/carga; los textos pueden envolver; la cuadrícula reduce
columnas con texto ampliado. La hoja mantiene el destino secundario y se cierra
por Back o su acción de cierre; el indicador no añade gesto de arrastre. El
botón reutilizado del DS conserva su apariencia deshabilitada gris, en lugar
del amarillo tenue de la captura. Las barras independientes muestran mayor
amarillo, menor oscuro y empate neutro, con texto adicional de resultado.

Estos cambios están incorporados al código, pendientes de cotejo en
**COMPARE-V02**; no se acredita fidelidad visual completa sin observar la app.
Las capturas estrechas tampoco acreditan horizontal, texto ampliado o foldables.

## SVG suministrado

`assets/dice.svg`: viewport y tamaño de origen **40 × 40**, un trazado de color
`#1C1B1F` y máscara rectangular sobre el viewport. Se conserva como fuente visual
aportada. No se convierte ni integra como control Android porque su acción está
excluida expresamente del spec; recibir el archivo no cambia esa decisión.
El SVG no declara autor ni licencia; no se atribuye una licencia supuesta.

## Verificación pendiente

COMPARE-V01 conserva compilación y pruebas por el desarrollador.
COMPARE-V02 conserva cotejo de la app con estas referencias y validación mobile,
incluyendo las adaptaciones y los estados registrados arriba. Los comandos y
escenarios están en [PLAN.md](PLAN.md). No se ejecutaron builds, pruebas ni la app.
