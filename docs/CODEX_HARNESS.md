# Harness Codex de PocketDex

## Alcance y activación

Harness local aprobado el 2026-09-30: seis skills y cuatro agents para el stack de
[constitution.md](../constitution.md), con delegación automática cuando aporte
valor y ejecución secuencial. Sus instrucciones respetan [AGENTS.md](../AGENTS.md).
No instala ECC globalmente, hooks, MCPs, proveedores ni modelos adicionales.

| Ubicación | Función |
| --- | --- |
| `skills/<nombre>/SKILL.md` | Fuente editable y entregable reutilizable |
| `skills/<nombre>/agents/openai.yaml` | Metadatos e invocación implícita habilitada |
| `.agents/skills/<nombre>/` | Copia generada para descubrimiento local de Codex |
| `.agents/pocketdex-skills-manifest.json` | Propiedad y hashes de las copias generadas |
| `.codex/agents/*.toml` | Perfiles nativos de especialistas |
| `.codex/config.toml` | Habilitación y límite de un thread de subagente abierto |
| `scripts/sync-codex-skills.ps1` | Sincronización y comprobación sin ejecutar la app |

Abrir una nueva sesión de Codex desde la raíz de PocketDex tras cambiar skills o
agents. Comprobar que las seis skills aparezcan en el selector; también pueden
invocarse explícitamente con `$pocketdex-spec`, por ejemplo. Los perfiles de agents
se descubren desde `.codex/agents`; no son slash commands ni skills.

Codex puede requerir que el usuario confíe en el proyecto para cargar la
configuración local. Si no aparecen, revisar confianza, versión y raíz de inicio;
no cambiar permisos globales para resolver el descubrimiento. La versión CLI
observada durante la preparación fue 0.159.2; no es un requisito de versión mínima.
La carga efectiva en una nueva sesión debe comprobarse manualmente.

## Skills y agentes

| Skill | Cuándo usarla |
| --- | --- |
| `pocketdex-spec` | Contexto, requisitos, preguntas de producto y criterios de aceptación |
| `pocketdex-architecture` | Módulos, dominio, contratos, DI, Gradle y análisis de logs |
| `pocketdex-compose` | UI, ViewModels, Navigation 3, ciclo de vida y accesibilidad |
| `pocketdex-data` | PokéAPI, repositorios, Room, filtros/paginación y offline |
| `pocketdex-tests` | Escritura/revisión de pruebas y análisis de resultados aportados |
| `pocketdex-review` | Hallazgos de corrección y cumplimiento con revisión estática |

| Agent | Especialidad | Permisos del perfil |
| --- | --- | --- |
| `pocketdex-architect` | Exploración, contratos y planes | Solo lectura |
| `android-implementer` | Código y pruebas de un alcance aprobado | Escritura en workspace |
| `android-reviewer` | Revisión independiente de cambios | Solo lectura |
| `android-docs-researcher` | APIs y compatibilidad documentadas | Solo lectura |

Las skills instruyen al agente que realiza una tarea; los agents crean contextos
especializados de trabajo. Todos heredan modelo/esfuerzo de la sesión, sin overrides
locales. Ninguno puede compilar, empaquetar, instalar o ejecutar la app, pruebas o
cobertura. Leer/revisar no equivale a demostrar comportamiento en ejecución.

## Delegación automática

El principal decide cuándo una exploración amplia, incertidumbre técnica, cambio
acotado o revisión independiente justifica delegar. Una edición simple se puede
resolver directamente; no es obligatorio recorrer los cuatro agentes en cada tarea.

1. Elegir un especialista y delimitar objetivo, archivos, spec, decisiones aprobadas,
   preguntas abiertas, salida esperada y permisos de escritura/terminal.
2. Confirmar que terminó todo comando o sesión anterior. Ceder la terminal al
   especialista si la necesita y suspender su uso por el principal mientras trabaja.
3. Mantener solo un thread de especialista abierto. Los especialistas no delegan.
   Al terminar, recuperar control de terminal e inspeccionar resultados. Cerrar/liberar
   su thread con la operación disponible antes de crear otro rol: el límite cuenta
   threads abiertos, incluso si están inactivos, y excluye al principal. Si la interfaz
   no permite cerrar threads, reutilizar el disponible transmitiendo las instrucciones
   completas y restricciones del nuevo rol, o continuar desde el principal. No subir
   concurrencia para evitar el límite. Resolver cualquier comando pendiente antes
   de otro traspaso.
4. El principal conserva responsabilidad por integrar cambios, resolver preguntas
   con el usuario y entregar estado y comandos manuales. El reviewer recibe cambios
   estables cuando el implementador haya terminado de escribir.

El límite de concurrencia es una ayuda de configuración; la exclusión de terminal
depende además de este protocolo. Una orden de shell de un subagente también
cuenta como uso de terminal. Si la integración de esta sesión no expone selección
de perfiles, el principal carga el TOML pertinente y transmite sus instrucciones
al delegar; no afirma que el runtime aplicó permisos del perfil. Para revisión
de solo lectura, el delegado debe respetar ese límite incluso en ese caso.

## Mantenimiento y verificación

Editar únicamente la fuente en `skills/`. Sincronizar y comprobar desde la raíz,
secuencialmente:

```powershell
.\scripts\sync-codex-skills.ps1
.\scripts\sync-codex-skills.ps1 -Check
```

Si la política de ejecución de PowerShell lo impide, consultar la política local
del desarrollador; no modificarla globalmente por iniciativa del agente.

El sincronizador verifica rutas y hashes antes de escribir. Preserva modificaciones
en copias detectadas y archivos ajenos; no elimina archivos automáticamente.
Si una fuente se retira o una copia diverge, revisar el conflicto y resolverlo
explícitamente antes de sincronizar. No editar solo `.agents/skills` porque se
perdería la relación con la fuente mantenida. Incluir fuentes, copias y manifest
en Git o ZIP para que el receptor pueda usar el harness sin regenerarlo.

La verificación estructural comprende frontmatter/metadata YAML, TOML, rutas,
enlaces locales, sincronización y lectura de las instrucciones. No requiere
builds ni pruebas de la app. Comprobar selección real en una nueva sesión con una
petición de planificación o revisión, sin lanzar comandos de aplicación.
Los comandos manuales de compilación/pruebas están en AGENTS.md y deben adaptarse
a módulos realmente existentes cuando se implemente código Android.

## Procedencia y adaptaciones

Referencia: [affaan-m/ECC](https://github.com/affaan-m/ECC), revisión inspeccionada
`c70874fae9eb0e5ad0365beb7e2955899fd1d30f` (2026-09-30).
Las instrucciones de PocketDex se redactaron para este proyecto, inspiradas en
la separación de roles, skills y contexto progresivo de ECC; no se ejecutó su
instalador ni se copió su configuración completa.

- [Android Clean Architecture de ECC](https://github.com/affaan-m/ECC/blob/c70874fae9eb0e5ad0365beb7e2955899fd1d30f/skills/android-clean-architecture/SKILL.md): adaptada a nuestros módulos y Retrofit/Hilt.
- [Kotlin testing de ECC](https://github.com/affaan-m/ECC/blob/c70874fae9eb0e5ad0365beb7e2955899fd1d30f/skills/kotlin-testing/SKILL.md): se descartan Kotest, Kover, cobertura impuesta y ejecución automática; usamos JUnit 4/Turbine y verificación manual.
- [Kotlin reviewer de ECC](https://github.com/affaan-m/ECC/blob/c70874fae9eb0e5ad0365beb7e2955899fd1d30f/agents/kotlin-reviewer.md): revisión con evidencia; sin reglas universales sobre lambdas, init, IO o anotaciones.
- [Configuración Codex de ECC](https://github.com/affaan-m/ECC/blob/c70874fae9eb0e5ad0365beb7e2955899fd1d30f/.codex/config.toml): se conserva organización local, pero se usa el formato nativo actual y concurrencia uno.

ECC publica bajo [licencia MIT](https://github.com/affaan-m/ECC/blob/c70874fae9eb0e5ad0365beb7e2955899fd1d30f/LICENSE).
Consultar las fuentes actuales antes de incorporar material adicional.

Fuentes de integración: [skills de Codex](https://learn.chatgpt.com/docs/build-skills)
y [agents de Codex](https://learn.chatgpt.com/docs/agent-configuration/subagents).
Las copias son archivos normales para evitar permisos de symlinks/junctions en
Windows y conservar portabilidad de los entregables.
