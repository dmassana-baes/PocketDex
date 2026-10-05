---
name: pocketdex-compose
description: "Implementar pantallas y ViewModels de PocketDex con Compose, Material 3, MVVM/UDF y Navigation 3; resolver lifecycle, accesibilidad y adaptación visual según la spec."
---

# Presentación Compose de PocketDex

## Propósito y entradas

Implementar la presentación de una funcionalidad especificada. Leer `AGENTS.md`,
`constitution.md`, la spec, la pantalla existente y componentes reutilizables.
Consultar el Figma pertinente cuando haya una tarea visual; utilizar las skills
Figma disponibles que exija la herramienta, sin incorporar convenciones de otros
proyectos como Limitless o TarjetaOne.

## Proceso

- Separar conexión de ViewModel y pantalla: la pantalla recibe estado inmutable
  y callbacks. El ViewModel expone `StateFlow` y acciones explícitas; la UI usa
  `collectAsStateWithLifecycle`. La navegación global pertenece a app.
- Modelar los estados indicados por la spec, incluidos errores de carga incremental
  sin ocultar contenido existente. No lanzar red o persistencia desde recomposición.
- Usar concurrencia estructurada. No tragar `CancellationException`, usar GlobalScope
  ni mutar colecciones ya publicadas en UiState. Elegir `stateIn` y política de
  suscripción según la vida útil necesaria, sin prohibir `init` universalmente.
- Distinguir estado de UI, estado de negocio y datos persistidos. Aplicar
  `rememberSaveable` o SavedStateHandle solo al estado que la spec exige restaurar;
  no prometer persistencia de proceso con `remember` o un ViewModel.
- Usar Navigation 3 con claves y argumentos pequeños tipados; asegurar retención
  del ViewModel/estado por destino conforme a las APIs de la versión instalada.
  No trasladar ejemplos de NavController/NavHost como si fueran Navigation 3.
- Reutilizar tema/tokens/componentes de designsystem, textos en recursos e imágenes
  con Coil. Incluir placeholders/errores y etiquetas accesibles según el contenido.
- Considerar insets, texto ampliado, scroll, tamaños de ventana, teclado, foco y
  acciones repetidas. Usar claves estables en listas. No imponer remember en toda
  lambda ni anotaciones de estabilidad sin verificar sus contratos.

## Salidas y verificación

Entregar pantallas/ViewModels, estados cubiertos, diferencias visuales justificadas
y pasos manuales de interacción, navegación, recreación y accesibilidad aplicables.
Revisar efectos, scopes, recursos y callbacks estáticamente; no afirmar fidelidad
visual a partir de código sin observar la UI.

Nunca ejecutar la app, builds, pruebas o screenshots. Escribir previews es
admisible; renderizarlas o ejecutarlas queda al desarrollador. Usar pocketdex-tests
cuando se escriban pruebas, cargando solo las referencias necesarias.
