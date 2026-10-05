# Base de aplicación y navegación

Estado: **definición funcional acordada; avance en [TASKS.md](TASKS.md)**. Fecha: **2026-09-30**.

Fuentes: [PDF](../../docs/pocket_dex_definition.pdf),
[Figma](https://www.figma.com/design/0M4IjLeiuGIFocB4qDQuRV/Pokedex-App---UI?node-id=13-285)
y [guías mobile](../../docs/MOBILE_GUIDELINES.md).
Documentos relacionados: [PLAN técnico](PLAN.md), [tareas](TASKS.md) e [índice](../README.md).

## Objetivo y alcance

Dar acceso a Home, Comparador, Quiz y Favoritos y permitir consultar detalle desde
Home/Favoritos. Home es el destino inicial; detalle, selectores y resultados son
flujos secundarios. Se conserva el lenguaje visual del Figma dentro del alcance
del PDF. No se incluyen Random, filtros avanzados, ganador global, pestañas extra
de detalle, cuentas, deep links ni notificaciones.

## Reglas funcionales y decisiones confirmadas

- APP-01: cambiar o reseleccionar destino conserva su contenido y desplazamiento;
  no limpia búsquedas, reinicia sesiones ni desplaza automáticamente al inicio.
- Back desde un destino principal distinto de Home vuelve a Home; desde un flujo
  secundario vuelve al origen. En Home raíz se aplica la salida estándar del sistema.
- El vacío de favoritos permite explorar Home conservando sus criterios previos;
  la navegación funciona sin conexión, aunque el contenido disponible dependa de datos guardados.
- APP-02: rotación, redimensionamiento, plegado/desplegado y segundo plano conservan
  el contexto. Si Android recupera la misma tarea tras terminar el proceso, se
  recuperan destino, búsqueda, desplazamiento, selecciones y la misma sesión de quiz.
- Una apertura nueva sin tarea recuperada empieza en Home con estado temporal
  nuevo. Favoritos y datos descargados permanecen guardados.
- Volver al quiz ofrece Continuar/Reiniciar según su spec; salir no borra el progreso.
- APP-03: aplicar solo funcionalidades del PDF, respetando los acuerdos visuales
  compartidos. El quiz conserva cuatro opciones aunque Figma muestre tres.
- DS-09: barra inferior con ancho de ventana menor de 600 dp y lateral desde 600 dp;
  el cambio de patrón no cambia el destino ni coloca acciones sobre la bisagra.

## Flujo y estados visibles

1. Apertura nueva → Home; tarea recuperada → último contexto recuperable.
2. Seleccionar área → su contexto conservado; reseleccionar → mismo contexto.
3. Abrir detalle desde listado → Pokémon elegido → regresar al listado de origen.
4. Un fallo en una funcionalidad permite reintentar y seguir navegando; nunca se
   sustituye por datos de ejemplo ni se bloquea la salida esperando conexión.

La aplicación utiliza tema claro, español/inglés según sistema e inglés de respaldo;
el cambio de idioma traduce la interfaz sin cambiar identidades ni repetir acciones.

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
| APP-AC02 | Desde navegación se llega a cada área; cambio/reselección conserva estado/scroll y no apila duplicados; Back principal vuelve a Home, secundario al origen y Home raíz permite salida del sistema |
| APP-AC03 | Home/favoritos abren detalle del ID elegido; Back visible/sistema regresa al origen |
| APP-AC04 | El vacío de favoritos permite explorar sin red para navegar; Home conserva criterios/scroll previos, con limpieza accesible |
| APP-AC06 | Rotación, cambio de idioma y plegado/desplegado preservan estado conforme a specs, mantienen tema claro y no repiten escrituras o respuestas |
| APP-AC08 | Android recupera una tarea tras terminar proceso: se restauran ruta y contexto sin repetir acciones; una apertura nueva sin tarea recuperada inicia Home con estado temporal nuevo y favoritos intactos |

## Decisiones y límites

Las decisiones funcionales están confirmadas por el usuario el 2026-09-30.
No quedan preguntas de producto propias de esta área. Nuevas dudas que alteren
comportamiento o aceptación deben consultarse; no cambiar requisitos para acomodar
una implementación. Pendientes técnicos y verificación se registran en [PLAN.md](PLAN.md);
trabajo desglosado y progreso en [TASKS.md](TASKS.md).

Los IDs históricos APP-AC01, APP-AC05 y APP-AC07 describen verificaciones
técnicas y se conservan en PLAN.md, fuera de esta aceptación funcional.
