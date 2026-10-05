# Tareas: Sistema visual compartido

Estado: **10 tareas de implementación realizadas; ejecución pendiente**. Fecha: **2026-10-05**.

Fuentes: [SPEC funcional](SPEC.md), [PLAN técnico](PLAN.md) e [índice](../README.md).

## Uso y dependencias

Sin dependencia de otras áreas. Debe abrir el trabajo de implementación; la definición técnica previa no exige implementar la base primero.

Los IDs externos remiten a TASKS.md del área correspondiente mediante el índice.
Las dependencias indican entregables necesarios, no obligan a cerrar toda una
feature antes de consumir un contrato compartido. Respetar el orden dentro de
cada cadena; tareas que escriben archivos compartidos se ejecutan secuencialmente.
Las ubicaciones reales se registran abajo. Las casillas de implementación acreditan
código/documentación revisados estáticamente; DS-V01/DS-V02 separan la ejecución.
La procedencia de fuentes está cerrada. El usuario acordó registrar autor/licencia
de iconos no declarados sin usarlos como bloqueo de cierre; no se inventa una licencia.
El icono de filtro aportado en `specs/home/assets/filter.svg` se incorpora el 2026-10-05.

## Trabajo desglosado

- [x] **DS-T01 — Inventariar iconos por nodo, origen y licencia; registrar fuente local y copyright/OFL.**
  Alcance: documentación de assets y futuros recursos designsystem. Dependencias: ninguna.
  Resultado de cierre: DS-AC10; cada recurso incorporado tiene archivo/origen
  registrado. Autor/licencia no declarados quedan documentados sin bloquear el cierre,
  por decisión del usuario. La OFL de las fuentes se conserva.
  Resultado: 18 tipos, 13 iconos de app y Poké Ball convertidos, inventariados
  con hashes y nodos/exportaciones en [ASSETS.md](ASSETS.md) y CSV relacionados.
  El usuario aportó el filtro el 2026-10-05; se conserva SVG original, nodo de
  máscara 53:43672 y hashes. La exportación previa de Home 13:5437 fue rechazada
  por cuota Starter; la incorporación actual usa el archivo suministrado.

- [x] **DS-T02 — Verificar APIs de fuente SDK 24 y dependencias Compose necesarias, conservando versiones base.**
  Alcance: catálogo y plan técnico. Dependencias: DS-T01.
  Resultado de cierre: compatibilidad oficial documentada, sin dependencias especulativas.

- [x] **DS-T03 — Crear biblioteca :core:designsystem y registrar el módulo.**
  Alcance: settings.gradle.kts y core/designsystem/build.gradle.kts. Dependencias: DS-T02.
  Resultado de cierre: límites internos vacíos revisados estáticamente.

- [x] **DS-T04 — Implementar paleta, onType, typography, spacing, shapes y PocketDexTheme fijo claro.**
  Alcance: theme/model y recursos de fuentes. Dependencias: DS-T03.
  Resultado de cierre: DS-AC01, DS-AC06, DS-AC07, DS-AC08, DS-AC09.

- [x] **DS-T05 — Implementar botones primario/secundario/Ghost, FavoriteButton y búsqueda controlada.**
  Alcance: components y recursos bilingües. Dependencias: DS-T04.
  Resultado de cierre: DS-AC03 y DS-AC09; ocupado/deshabilitado y targets accesibles.

- [x] **DS-T06 — Implementar badges/chips de tipo, imagen con estados y PokemonCard.**
  Alcance: components/model/drawables autorizados. Dependencias: DS-T01, DS-T05.
  Resultado de cierre: DS-AC02, DS-AC05, DS-AC08; abrir/favorito independientes.
  Cierre de implementación: 18 tipos locales e ilustración Poké Ball integrada
  en tarjeta, con opacidad blanca 12 % comprobada en el nodo 18:2069.
  Abrir/favorito siguen siendo callbacks separados; la validación visual queda en DS-V02.

- [x] **DS-T07 — Implementar barras/fila estadística y comparación con datos faltantes explícitos.**
  Alcance: components. Dependencias: DS-T04.
  Resultado de cierre: DS-AC11; misma escala y valores reales sin regla de ganador en UI.

- [x] **DS-T08 — Implementar app bar, navegación visual y estados carga/vacío/error.**
  Alcance: components. Dependencias: DS-T05.
  Resultado de cierre: DS-AC04, DS-AC12; callbacks sin back stack ni repositorios.

- [x] **DS-T09 — Añadir previews deterministas y escribir pruebas significativas de interacción/semántica.**
  Alcance: preview y src/test o androidTest cuando corresponda. Dependencias: DS-T06, DS-T07, DS-T08.
  Resultado de cierre: variantes offline, textos largos, estados y callbacks cubiertos por pruebas escritas.

- [x] **DS-T10 — Revisar API/imports/licencias y entregar comandos y escenarios de validación visual.**
  Alcance: PLAN.md/TASKS.md y código afectado. Dependencias: DS-T09.
  Resultado de cierre: revisión estática registrada; ejecución pendiente explícita.

## Verificación de ejecución

- [ ] **DS-V01 — El desarrollador ejecuta las tareas de PLAN.md aplicables al incremento.**
  Depende de implementación y pruebas escritas; registrar comandos/entorno/resultados
  y fallos pendientes. El agente no ejecuta build ni pruebas.

- [ ] **DS-V02 — El desarrollador verifica los escenarios mobile y visuales del spec.**
  Registrar conectividad/interrupciones/idiomas/texto ampliado/TalkBack/posturas que
  correspondan, evidencia y limitaciones; screenshots solo acreditan apariencia.

## Registro de avance y evidencia

| ID | Entregable y evidencia del incremento |
| --- | --- |
| DS-T01 | `ASSETS.md`, `type-icons.csv`, `app-icons.csv`, fuentes/OFL y archivos suministrados identificados por nodo/hash; filtro incluido el 2026-10-05. Autor/licencia no declarados se registran sin bloquear por acuerdo. |
| DS-T02 | PLAN enlaza APIs oficiales: biblioteca/Kotlin integrado/BOM/FontFamily estático SDK 24; catálogo preserva todas las versiones base |
| DS-T03 | `settings.gradle.kts`, root/catalog y `core/designsystem/build.gradle.kts`/manifest; sin dependencia de otros módulos |
| DS-T04 | `theme/Tokens.kt`, `theme/PocketDexTheme.kt`, `model/PokemonType.kt`, fuentes 400/500; wrapper de tema de app y estilos claros de barras en MainActivity |
| DS-T05 | `components/Controls.kt`; FavoriteButton con Heart/HeartFilled y acción accesible; búsqueda con Search y limpiar con Close; estados ocupado/deshabilitado conservados |
| DS-T06 | `components/PokemonVisuals.kt`, `model/PokemonType.kt`, 18 tipos XML y `pd_pokeball_decoration.xml`; decoración sin semántica ni click, 12 % blanco; callbacks separados |
| DS-T07 | `components/Statistics.kt`, `model/VisualFormatting.kt`; escala 255, valor real/faltante, resultado recibido; semántica enlaza estadística, lado/nombre y valor |
| DS-T08 | `components/Scaffolding.kt`, `PocketDexIcon.kt` y previews; ArrowBack, Refresh y iconos de destinos disponibles con variantes; slots/selección externa y host adaptativo futuro separado |
| DS-T09 | `preview/DesignSystemPreviews.kt`, `src/test/.../VisualFormattingTest.kt` y `src/androidTest/.../DesignSystemInteractionTest.kt`; 3 pruebas unitarias y 9 instrumentadas escritas (incluyen búsqueda IME/limpiar y favorito ocupado), previews sin red, no ejecutadas/renderizadas |
| DS-T10 | Revisión estática principal/especialista de API, imports, Gradle, fuentes/licencia, semántica y pruebas. Se corrigió asociación accesible de valores del comparador. Comandos/escenarios en PLAN |

La revisión XML confirmó recursos referenciados definidos y claves traducibles
coincidentes; inspección de imports/Gradle no encontró IO ni dependencias internas.
Revisión del 2026-10-01: 18 SVG convertidos; comparación XML de 45 trazados con
datos idénticos, colores/reglas de relleno y recortes preservados. Las 18 referencias
de PokemonType apuntan a recursos existentes. No hay nuevas dependencias ni IO runtime.
La segunda revisión estática por especialista no encontró defectos materiales en
la conversión/integración ni divergencias con el inventario. No acredita renderizado.
Incremento de controles/ilustración: `scripts/convert-app-icons.ps1` genera 13 XML
desde 12 iconos y una Poké Ball; 15 trazados idénticos y colores originales preservados.
Las máscaras alpha rectangulares opacas se traducen a clip-path. Random y los tres
iconos del sistema quedan excluidos y registrados. La prueba escrita de limpiar
búsqueda identifica ahora su descripción accesible, conservando la misma acción.
La revisión independiente de este incremento no encontró defectos materiales
en XML, integración, semántica, callbacks ni estado documental. Sigue pendiente
la ejecución y el renderizado por el desarrollador.
No se ejecutaron Gradle, compilación, pruebas, app ni previews. Los criterios de
pantallas completas, contraste renderizado, TalkBack real, teclado, insets/bisagras,
rotación y continuidad requieren DS-V01/DS-V02 y los hosts/features futuros.
El 2026-10-05 se cierra DS-T01 al convertir/inventariar el SVG de filtro suministrado.
`PocketDexButton` admite leadingIcon opcional, conserva la etiqueta y el bloqueo
busy/disabled; Home muestra el filtro decorativo con su acción accesible. La
documentación no acredita fidelidad visual final; DS-V01/DS-V02 siguen pendientes.
La revisión independiente del filtro no encontró defectos materiales en XML,
inventario ni leadingIcon; callbacks y bloqueo busy/disabled se conservan.

Comandos manuales desde la raíz (el último requiere dispositivo/emulador):

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :core:designsystem:testDebugUnitTest
.\gradlew.bat :core:designsystem:connectedDebugAndroidTest
```
