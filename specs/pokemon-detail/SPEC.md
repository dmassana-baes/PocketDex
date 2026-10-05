# Detalle de Pokémon

Estado: **definición funcional acordada; implementación pendiente**. Fecha: **2026-09-30**.

Fuentes: [PDF](../../docs/pocket_dex_definition.pdf),
[Figma](https://www.figma.com/design/0M4IjLeiuGIFocB4qDQuRV/Pokedex-App---UI?node-id=13-285)
y [guías mobile](../../docs/MOBILE_GUIDELINES.md).
Documentos relacionados: [PLAN técnico](PLAN.md), [tareas](TASKS.md) e [índice](../README.md).

## Objetivo y alcance

Consultar imagen, nombre, número nacional, todos los tipos y seis estadísticas base
del Pokémon elegido; marcar/desmarcar favorito y regresar al origen. DETAIL-01
limita el contenido al PDF: sin About, Moves, Evolutions, historia ni medidas adicionales.

## Reglas funcionales y decisiones confirmadas

- Mostrar HP, Attack, Defense, Sp. Atk, Sp. Def y Speed asociados a su etiqueta correcta;
  no depender del orden recibido ni inventar ceros para estadísticas ausentes.
- DS-07: el primer tipo de la variedad predeterminada es el acento del gradiente;
  mostrar ambos tipos si existen. DS-08: escala visual 255 compartida con comparador;
  valor 100 ocupa aproximadamente 39.22 %, 255 llena la barra y un valor superior
  conserva su número real con barra llena. Una barra no sustituye el valor textual.
- FAV-01/FAV-02: con datos parciales mostrar contenido disponible y faltantes explícitos;
  ofrecer completarlos. Imagen ausente/fallida no oculta datos textuales ni elimina favorito.
- FAV-05: mostrar datos locales inmediatamente y actualizar al abrir con conexión;
  fallo de actualización conserva contenido, imagen útil y marca, con feedback/reintento.
- Guardar favorito sin exigir una descarga remota completa; fallo de escritura
  conserva la marca real y el contenido. Home/Favoritos reflejan la misma marca.
- DETAIL-02 y APP-01/APP-02: conservar Pokémon y desplazamiento al navegar, rotar,
  cambiar postura y recuperar tarea. Apertura nueva comienza en Home.
- Back visible y del sistema regresan al origen incluso durante carga/fallo;
  no esperar conexión para salir ni repetir escrituras al recrear la pantalla.

## Flujo y estados visibles

1. Abrir Pokémon elegido → contenido almacenado o carga.
2. Datos disponibles → identidad, imagen y estadísticas completas o parcial explícito.
3. Fallo sin contenido → error/reintento; fallo con contenido → aviso conservándolo.
4. Marcar/desmarcar → acción en curso → confirmación guardada o feedback de fallo.
5. Regresar → listado de origen, conservando su contexto.

Separar carga/error de detalle, carga/error de imagen y acción/error de favorito.

## Requisitos trazados al producto

| ID | Fuente | Comportamiento |
| --- | --- | --- |
| DETAIL-R01 | 2.1 | Mostrar imagen, nombre, número, todos los tipos y estadísticas base del Pokémon identificado |
| DETAIL-R02 | 2.2, 5.1 | Marcar y desmarcar favorito; reflejar persistencia y estado coherente entre pantallas |
| DETAIL-R03 | 2.3 | Regreso visible y del sistema, carga y error explícitos con recuperación |
| DETAIL-R04 | 2.4 | Fidelidad razonable al Figma con adaptación al espacio disponible |
| DETAIL-R05 | 5.4 | Consultar datos almacenados de favoritos sin conexión; no confundir fallo remoto con ausencia local |

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
| DETAIL-AC01 | Para un ID válido se muestra su imagen, nombre, número, todos sus tipos y seis estadísticas reales; la asociación etiqueta-valor no depende del orden remoto |
| DETAIL-AC02 | Marcar/desmarcar se confirma solo tras persistencia; Home/favoritos reflejan el mismo estado; pulsaciones repetidas no producen cambios contradictorios |
| DETAIL-AC03 | Con carga/fallo/contenido, Back visible/sistema regresa al origen sin esperar red; no apila otro detalle al reintentar |
| DETAIL-AC04 | Red lenta muestra carga; fallo sin datos muestra error y reintento; reintento exitoso recupera contenido; fallo de escritura conserva contenido y marca real |
| DETAIL-AC05 | La jerarquía del nodo citado se mantiene con adaptación aprobada; vertical/horizontal, foldable y texto ampliado conservan todas las acciones y valores |
| DETAIL-AC06 | Con favorito previamente almacenado y sin red, se consulta su detalle retenido; si no hay imagen local se aplica FAV-02 sin perder datos textuales |
| DETAIL-AC07 | Rotar, plegar/desplegar o cambiar idioma conserva el ID y el estado según política; ninguna escritura/navegación se dispara de nuevo al recrear la pantalla |
| DETAIL-AC08 | Datos inválidos o ausentes no se convierten en estadísticas cero ni en contenido de otro ID; feedback accesible permite recuperarse |
| DETAIL-AC09 | Un favorito con solo resumen se consulta offline como parcial, con estadísticas faltantes explícitas; completar online recupera datos sin quitar/recrear marca ni perder contenido previo |
| DETAIL-AC10 | Abrir con datos locales y red muestra contenido inmediato y actualiza; fallo conserva datos/imagen/marca con reintento, y rotación no duplica la actualización en curso |

## Decisiones y límites

Las decisiones funcionales están confirmadas por el usuario el 2026-09-30.
No quedan preguntas de producto propias de esta área. Nuevas dudas que alteren
comportamiento o aceptación deben consultarse; no cambiar requisitos para acomodar
una implementación. Pendientes técnicos y verificación se registran en [PLAN.md](PLAN.md);
trabajo desglosado y progreso en [TASKS.md](TASKS.md).
