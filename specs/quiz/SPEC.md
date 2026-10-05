# Quiz: Who is that Pokémon?

Estado: **definición funcional acordada; implementación incorporada; validación pendiente**. Fecha: **2026-10-05**.

Fuentes: [PDF](../../docs/pocket_dex_definition.pdf),
[Figma](https://www.figma.com/design/0M4IjLeiuGIFocB4qDQuRV/Pokedex-App---UI?node-id=13-285)
y [guías mobile](../../docs/MOBILE_GUIDELINES.md).
Documentos relacionados: [PLAN técnico](PLAN.md), [tareas](TASKS.md) e [índice](../README.md).

## Objetivo y alcance

Completar cinco preguntas identificando Pokémon con silueta, cuatro opciones,
feedback y puntuación; continuar o reiniciar una sesión. No incluye límite de
tiempo, dificultad, ranking, cuenta, premios ni historial permanente.

## Reglas funcionales y decisiones confirmadas

- QUIZ-01: preparar las cinco preguntas al inicio, con cinco Pokémon correctos
  distintos. Otras sesiones pueden repetirlos. Cada pregunta tiene cuatro opciones
  únicas, distinguibles y exactamente una correcta; distractores pueden reaparecer
  entre preguntas. Nombres ambiguos no producen preguntas válidas.
- QUIZ-02: silueta antes de responder y revelación de la misma imagen al responder.
  Orden de opciones fijo. Ningún texto/descripción expuesto señala cuál opción es
  correcta antes de responder; su nombre sí aparece entre las cuatro opciones.
- La primera respuesta válida bloquea la pregunta; ninguna respuesta posterior,
  doble toque o acción tardía cambia la respuesta ni puntúa nuevamente.
- Feedback permanece hasta Siguiente en preguntas 1–4 y Ver resultado en quinta.
  No avanzar sin responder ni saltar preguntas por toques repetidos.
- QUIZ-05: resultado 0/5–5/5, +1 por correcta y +0 por incorrecta, sin penalización
  ni temporizador. Reiniciar crea cinco preguntas y borra respuestas/progreso previo.
- QUIZ-03: Back activo vuelve a Home conservando sesión. Al regresar ofrecer
  Continuar/Reiniciar; Continuar recupera misma pregunta/opciones/feedback/progreso.
  Confirmar reinicio si pierde progreso activo; cancelar conserva todo. Reiniciar
  desde resultado final no requiere esa confirmación.
- APP-01/APP-02: navegación, rotación, postura, segundo plano y tarea recuperada
  conservan sesión/orden/respuestas/puntuación; apertura nueva empieza sin sesión.
- QUIZ-04: Mostrar pistas es opcional para todos, accesible con TalkBack/texto
  ampliado: tipos y estadísticas reales, sin nombrar la correcta ni dar su número.
  Las pistas permiten distinguir la correcta de distractores, conservan cuatro
  opciones/puntuación y no inventan hechos. Su visibilidad se conserva al recrear
  la misma pregunta; una pregunta nueva comienza con pistas ocultas.
- QUIZ-06: verificar cinco imágenes y datos/pistas suficientes antes de empezar;
  una sesión preparada continúa sin red. Otra sesión offline requiere datos locales
  elegibles para cinco correctas distintas y cuatro opciones por pregunta; si no
  hay suficientes, informar y ofrecer reintento con conexión, sin reducir preguntas/opciones.
- La limpieza normal no elimina imágenes de una sesión activa o recuperable.
  Pérdida/corrupción de imagen ofrece recuperación conservando progreso, sin
  reemplazar preguntas a mitad de sesión. Reiniciar no elimina imágenes de favoritos.

## Flujo y estados visibles

1. Inicio → preparar cinco preguntas e imágenes → carga o error/reintento.
2. Pregunta sin responder → cuatro opciones y pistas opcionales → elegir una.
3. Pregunta bloqueada → feedback/revelación → Siguiente o Ver resultado.
4. Quinta respondida → Ver resultado → puntuación final → Reiniciar.
5. Salir/regresar → Continuar o Reiniciar con confirmación cuando corresponda.

El cambio de idioma traduce etiquetas/pistas sin cambiar hechos, opciones o resultado.
Figma muestra tres opciones; el producto mantiene cuatro. Su frame Result muestra
revelación de una pregunta, no la puntuación final de cinco preguntas.

## Requisitos trazados al producto

| ID | Fuente | Comportamiento |
| --- | --- | --- |
| QUIZ-R01 | 4.1 | Sesión de cinco preguntas, cada una con silueta o imagen parcialmente oculta |
| QUIZ-R02 | 4.2 | Cuatro opciones por pregunta, únicas y con exactamente una respuesta correcta |
| QUIZ-R03 | 4.3 | Tras elegir una respuesta, bloquear nuevas respuestas a esa pregunta |
| QUIZ-R04 | 4.4 | Mostrar feedback, revelar correcta y reflejar avance de sesión |
| QUIZ-R05 | 4.5 | Tras quinta pregunta, mostrar puntuación y permitir reiniciar |

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
| QUIZ-AC01 | Preparar al inicio exactamente cinco preguntas con IDs correctos distintos; cada pregunta muestra silueta sin revelar identidad antes de responder; una nueva sesión puede repetir IDs de sesiones anteriores |
| QUIZ-AC02 | Cada pregunta ofrece cuatro opciones con IDs/etiquetas distinguibles, sin duplicados y exactamente una correcta; fallo de datos no produce opciones ficticias |
| QUIZ-AC03 | La primera selección válida queda bloqueada; pulsaciones siguientes, doble toque o eventos de otra pregunta no modifican respuesta ni puntuación |
| QUIZ-AC04 | Tras responder se comunica acierto/error, se revela correcta y avance; Siguiente en 1–4 y Ver resultado en quinta avanzan una vez sin reevaluar; antes de responder no permiten avanzar |
| QUIZ-AC05 | Tras quinta respuesta y Ver resultado se muestran aciertos sobre cinco (0/5 a 5/5), un punto por correcta/cero por incorrecta sin penalizaciones ni temporizador; reiniciar elimina avance/respuestas anteriores y comienza cinco preguntas |
| QUIZ-AC06 | Fallo de preparación/imagen ofrece recuperación sin puntuar ni revelar; pérdida de red tras preparar las cinco imágenes permite finalizar la misma sesión según QUIZ-06 |
| QUIZ-AC07 | Rotación, postura e idioma no aceptan respuestas nuevas ni duplican puntuación; Back, proceso y reapertura cumplen exactamente QUIZ-03 |
| QUIZ-AC08 | Con texto ampliado/horizontal/foldable se alcanzan cuatro opciones; TalkBack anuncia feedback sin filtrar correcta antes de responder y satisface la alternativa acordada QUIZ-04 |
| QUIZ-AC09 | Back conserva sesión al volver a Home; Continuar recupera pregunta/opciones/respuesta/progreso; cancelar Reiniciar conserva todo, confirmar reinicia y solo se pide confirmación si se perderá progreso activo |
| QUIZ-AC10 | Antes de primera pregunta existen cinco imágenes verificadas y datos/pistas suficientes; nueva sesión offline funciona con datos locales elegibles o muestra insuficiencia recuperable, sin fabricar opciones ni repetir correctas |
| QUIZ-AC11 | Mostrar pistas está disponible para todos y con TalkBack; ofrece tipos/estadísticas verdaderas que distinguen la correcta de los distractores sin nombrarla ni dar su número; conserva cuatro opciones y puntuación, y su visibilidad al recrear la pregunta |

## Decisiones y límites

Las decisiones funcionales están confirmadas por el usuario el 2026-09-30.
No quedan preguntas de producto propias de esta área. Nuevas dudas que alteren
comportamiento o aceptación deben consultarse; no cambiar requisitos para acomodar
una implementación. Pendientes técnicos y verificación se registran en [PLAN.md](PLAN.md);
trabajo desglosado y progreso en [TASKS.md](TASKS.md).
