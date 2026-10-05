# Referencia visual y recursos del Quiz

Estado: **tres referencias locales inspeccionadas y dos ilustraciones originales integradas; cotejo ejecutado pendiente**. Fecha: **2026-10-05**.

La consulta `get_design_context` del nodo **31:2653** fue rechazada por cuota.
El usuario suministró tres PNG en `assets/screens_design/`, inspeccionados el
2026-10-05. Cada uno mide **360 × 800 píxeles**. Las medidas de referencia no
establecen por sí solas dimensiones Android en dp. Estos archivos sustituyen
la inspección antes bloqueada de QUIZ-T07; no son capturas de la app implementada.

| Archivo | Composición observada |
| --- | --- |
| `Quiz.png` | Fondo blanco, ilustración grande «Who’s that Pokémon?» centrada, CTA amarillo de ancho completo y navegación inferior. Sin app bar adicional. |
| `Quiz - Question.png` | Back, título centrado de 24, imagen/silueta grande, tres opciones blancas con borde fino y esquinas redondeadas, mucho espacio blanco. |
| `Quiz - Result.png` | Revelación de la misma imagen, decoración de confeti, nombre correcto sobre amarillo y acción Again con Refresh. Es feedback de una pregunta, no puntuación final. |

Los sprites de las capturas son ejemplos de Pokémon; en la app se usan los
medios verificados de la sesión, sin fijar Pidgeotto ni copiar sprites desde PNG.
Las barras del sistema y navegación pertenecen al SO/host adaptativo, sin copiar
sus píxeles. La navegación inferior se transforma en lateral desde 600 dp.

La implementación reutiliza tema claro, tipografía, espaciado, botones, consulta
de datos por dominio, imágenes/silueta, badges y barras del DS; no incorpora
assets estáticos inventados. Usa cinco imágenes locales verificadas del proveedor
de datos, la misma para silueta y revelación. Antes de responder la descripción
es neutra; nombres y número no se pasan a la descripción de la silueta.

PDF y decisiones aprobadas: cuatro opciones aunque el Figma muestre tres;
feedback explícito, puntuación final 0/5–5/5, Mostrar pistas para todos, sin
temporizador. La UI incluye inicio, preparación/error, pregunta, feedback,
Continuar/Reiniciar, confirmación y resultado final. En espacio amplio presenta
imagen/opciones en fila; en estrecho, en columna con scroll; texto puede envolver.

## Ajuste incorporado y límites

QUIZ-T12/T13/T14 ajustan portada centrada sin app bar, cabecera centrada con Back,
imagen 260 dp, cuatro opciones de mínimo 48 dp/borde Gray200, correcta amarilla
tras responder y seleccionada con estado accesible. Mantener opción incorrecta
seleccionada/feedback textual, orden fijo, pistas y avance Siguiente/Ver resultado;
no convertir Again en reinicio por pregunta. Cargar/error de imagen deshabilita
opciones y los taps posteriores a respuesta siguen bloqueados. Opciones
mantienen sus colores al bloquearse y no revelan el estado correcto antes de
responder. La puntuación final y reinicio usan pantalla separada centrada.

Adaptaciones funcionales: cuatro opciones en lugar de tres, progreso visible,
pistas para todos, texto de acierto/error, resultado de cinco preguntas y control
de avance deshabilitado antes de responder. Textos envuelven, scroll permite
alcanzar controles y disposición imagen/opciones cambia a fila con ancho suficiente.
El área táctil mínima crece de 44 de referencia a 48 dp.

## Ilustraciones suministradas e integradas (QUIZ-T15)

| Original suministrado | Recurso Android | Dimensiones |
| --- | --- | --- |
| [image 5.png](<assets/image 5.png>) | `feature/quiz/src/main/res/drawable-nodpi/quiz_intro_illustration.png` | 624 × 458, PNG transparente |
| [image 12.png](<assets/image 12.png>) | `feature/quiz/src/main/res/drawable-nodpi/quiz_confetti.png` | 720 × 480, PNG transparente |

Copias sin modificar, con hashes SHA-256 iguales al original. Son ilustraciones
raster suministradas, aptas para recursos Android; no necesitan conversión a XML.
`drawable-nodpi` evita el escalado de densidad implícito; Compose determina su
tamaño con `ContentScale.Fit`, conservando proporción y transparencia.

La portada sustituye el texto provisional por el logo de hasta 312 dp de ancho,
centrado y con descripción `quiz_intro` en es/en. El texto inglés dibujado dentro
del logo se conserva como parte del original; controles/descripción siguen el
idioma del sistema. El confeti es decorativo, sin descripción accesible, alineado
arriba detrás del Pokémon exclusivamente tras responder, tanto en columna como
en fila. No altera la misma imagen verificada, bloqueo, avance ni puntuación.
No se recortan screenshots ni se inventan réplicas. El cotejo real permanece pendiente.

QUIZ-V02 reserva cotejo de la app ejecutada y validación mobile. No se ejecutaron
app, renders, builds ni pruebas; estas referencias no prueban comportamiento.
