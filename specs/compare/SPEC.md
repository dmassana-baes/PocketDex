# Comparador de estadísticas

Estado: **definición funcional acordada**. Fecha: **2026-09-30**.

Fuentes: [PDF](../../docs/pocket_dex_definition.pdf),
[Figma](https://www.figma.com/design/0M4IjLeiuGIFocB4qDQuRV/Pokedex-App---UI?node-id=13-285)
y [guías mobile](../../docs/MOBILE_GUIDELINES.md).
Documentos relacionados: [PLAN técnico](PLAN.md), [tareas](TASKS.md) e [índice](../README.md).

## Objetivo y alcance

Elegir exactamente dos Pokémon distintos desde una pantalla dedicada y comparar
sus seis estadísticas base. No incluye dado/selección aleatoria, simulación de combate,
ganador global por suma/mayoría ni selección automática para completar el par.

## Reglas funcionales y decisiones confirmadas

- Cero/una selección son estados de preparación; Comparar solo está disponible
  con dos especies distintas y sus seis estadísticas completas.
- El selector busca con las reglas de nombre/número exacto y espera de 300 ms
  de HOME-02/HOME-04; no requiere un filtro de tipo adicional.
- Rechazar un Pokémon ya elegido en el otro slot con feedback, conservando
  selecciones válidas. Cancelar selector conserva ambas selecciones.
- COMPARE-01: por fila mostrar etiqueta y dos valores reales, indicar el mayor;
  igualdad muestra «Empate» y aspecto neutro. Se entiende sin distinguir colores.
- Las filas son HP, Attack, Defense, Sp. Atk, Sp. Def y Speed. DS-08 comparte escala
  visual 255 con detalle; el valor real permanece visible aunque supere la escala.
- COMPARE-03: pulsar Comparar abre resultados; no comparar automáticamente.
  Back desde resultados vuelve a editar; quitar/reemplazar cualquiera invalida
  el resultado anterior. Volver a Comparar usa el par actual.
- Una carga fallida no borra el otro slot. Respuestas tardías de un Pokémon
  sustituido no cambian el par actual ni muestran resultados antiguos.
- COMPARE-02 y APP-01/APP-02: conservar par, slot en edición y contexto al navegar,
  rotar, plegar/desplegar y recuperar tarea. Apertura nueva reinicia ambos slots.

## Flujo y estados visibles

1. Abrir → slots vacíos o selecciones conservadas.
2. Elegir slot → selector → confirmar candidato distinto → selección/carga.
3. Dos candidatos completos → Comparar → seis filas de resultado.
4. Back → editar; quitar → slot vacío; reemplazar → nuevo candidato válido.
5. Error de selector/datos → reintento conservando las selecciones útiles.

Acciones repetidas no crean un tercer slot ni abren dos resultados del mismo par.

## Requisitos trazados al producto

| ID | Fuente | Comportamiento |
| --- | --- | --- |
| COMPARE-R01 | 3.1 | Pantalla dedicada con exactamente dos slots; comparar solo con ambos ocupados por IDs distintos |
| COMPARE-R02 | 3.2 | Comparar HP, Attack, Defense, Sp. Atk, Sp. Def y Speed |
| COMPARE-R03 | 3.3 | Indicar quién tiene mayor valor por estadística; números/etiquetas complementan color |
| COMPARE-R04 | 3.4 | Reemplazar y quitar cualquiera de las dos selecciones |
| COMPARE-R05 | 3.5 | Impedir elegir el mismo ID en ambos slots y validar nuevamente al comparar |

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
| COMPARE-AC01 | Con cero/uno slots no se compara; con dos IDs distintos y datos completos, el botón Comparar abre resultados de ese par; eventos repetidos no apilan resultados duplicados |
| COMPARE-AC02 | Elegir ID ya ocupado en el otro slot se rechaza con feedback; conserva selecciones válidas y no permite comparar consigo mismo |
| COMPARE-AC03 | Se muestran seis claves y ambos valores reales; cada desigualdad identifica al mayor de forma accesible; igualdad muestra «Empate» y aspecto neutro sin ganador |
| COMPARE-AC04 | Back desde resultados permite quitar/reemplazar cualquiera e invalida resultado previo; cancelar edición conserva selección; volver a Comparar usa el nuevo par |
| COMPARE-AC05 | Fallo de candidato/detalle ofrece recuperación sin perder el otro slot; respuesta tardía de un ID sustituido no altera selección ni comparación vigente |
| COMPARE-AC06 | Pulsaciones repetidas no confirman dos veces ni agregan un tercer slot; IDs y resultados se mantienen conforme a COMPARE-02 al rotar/cambiar postura |
| COMPARE-AC07 | Ambos idiomas, texto ampliado y foldable plegado/desplegado mantienen seis filas y acciones visibles; el resultado se entiende sin distinguir colores |

## Decisiones y límites

Las decisiones funcionales están confirmadas por el usuario el 2026-09-30.
No quedan preguntas de producto propias de esta área. Nuevas dudas que alteren
comportamiento o aceptación deben consultarse; no cambiar requisitos para acomodar
una implementación. Pendientes técnicos y verificación se registran en [PLAN.md](PLAN.md);
trabajo desglosado y progreso en [TASKS.md](TASKS.md).
