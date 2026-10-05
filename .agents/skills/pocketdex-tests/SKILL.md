---
name: pocketdex-tests
description: "Escribir o revisar pruebas de dominio, repositorios y ViewModels de PocketDex con JUnit 4, Coroutines Test, Turbine y fakes; analizar resultados entregados sin ejecutar pruebas."
---

# Escritura de pruebas Android de PocketDex

## Propósito y entradas

Validar comportamiento mediante pruebas que ejecutará el desarrollador. Leer
`AGENTS.md`, `constitution.md`, spec, código y fixtures/pruebas existentes.
Recibir los criterios de aceptación o el log de ejecución aportado por el usuario.

## Proceso

- Elegir casos de comportamiento real: búsqueda y filtros AND, favoritos/reinicio
  y offline, duplicados/reemplazo del comparador, opciones/puntuación/bloqueo del
  quiz, y estados/errores/reintentos de ViewModels o casos de uso.
- Usar JUnit 4 y fakes deterministas como primera opción. Usar MockK para
  interacciones que aporten evidencia; no convertir todas las dependencias en
  mocks ni verificar exclusivamente llamadas que reflejan la implementación.
- Para coroutines usar `runTest` y dispatchers con el mismo scheduler; reemplazar
  Main mediante una regla cuando sea necesario y restaurarlo al finalizar.
  Evitar sleeps reales, dispatchers de producción y colectores que nunca terminan.
- Para StateFlow/Flow usar Turbine con suscripción activa antes del disparador.
  Recordar conflación e igualdad: no exigir emisiones transitorias no garantizadas
  por el contrato. Probar resultados/estados observables y cancelación pertinente.
- Desacoplar aleatoriedad y reloj cuando afecten el comportamiento. Reutilizar
  fixtures con propósito claro; no importar TestData, reglas o convenciones de
  TarjetaOne como si ya existieran en PocketDex.
- Los fakes no prueban SQL, migraciones o persistencia real. Escribir pruebas Room
  apropiadas y pasos de reapertura para complementar reglas de repositorios.
  Escribir una prueba Compose de flujo crítico cuando el alcance lo justifique.
- Para un log aportado, identificar el fallo causal y cambiar solo lo necesario;
  no debilitar aserciones para ocultar un defecto ni atribuir un PASS sin evidencia.

## Salidas y verificación

Entregar pruebas y fixtures pertinentes, escenarios cubiertos y pendientes, y
comandos PowerShell manuales: `:core:domain:test` para dominio JVM y
`:<módulo>:testDebugUnitTest` para Android, solo si el módulo existe. Las pruebas
instrumentadas requieren dispositivo/emulador y su tarea específica.

Nunca ejecutar pruebas, builds, app, cobertura o screenshots. No añadir Kotest,
Kover ni umbrales de cobertura no acordados. Revisar aserciones, aislamiento y
cleanup estáticamente; registrar ejecución como pendiente hasta recibir resultados.
