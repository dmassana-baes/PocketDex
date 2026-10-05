---
name: pocketdex-data
description: "Implementar acceso a PokéAPI, Retrofit/Serialization, repositorios, Room, favoritos offline y contratos de paginación de PocketDex según decisiones de persistencia aprobadas."
---

# Datos y persistencia de PocketDex

## Propósito y entradas

Implementar datos para una spec aprobada. Leer `AGENTS.md`, `constitution.md`,
contratos existentes, catálogo y decisiones de búsqueda/caché/persistencia. Revisar
documentación oficial de PokéAPI y de las versiones de las bibliotecas utilizadas.

## Proceso

- Mantener DTOs, servicios Retrofit/OkHttp, entidades Room, DAOs y mappers dentro
  de core:data. Los repositorios implementan contratos de dominio con modelos
  Kotlin; no filtrar tipos de red/base de datos ni PagingData al dominio.
- Confirmar capacidades reales de PokéAPI: no asumir un endpoint público de
  búsqueda parcial o filtros combinados. Definir el origen del índice/candidatos
  según la spec; buscar solo en páginas descargadas no garantiza búsqueda global.
- Combinar nombre/número y tipo con AND antes de paginar los candidatos que la
  spec establezca. Evitar huecos o fin prematuro al filtrar cada página aislada.
  Recuperar detalles necesarios con concurrencia acotada, deduplicación y
  cancelación; no descargar todos los detalles por defecto.
- Exponer operaciones seguras para el hilo principal. No añadir Dispatchers.IO
  indiscriminadamente a APIs suspend que ya desplazan trabajo; aislar IO bloqueante
  real. Propagar cancelación y distinguir error, vacío y contenido disponible.
- Room mantiene favoritos y datos almacenados. Usar actualizaciones consistentes
  y transacciones cuando una operación cambie varios registros relacionados.
  Refrescar datos remotos sin borrar decisiones locales de favoritos.
- Implementar lectura offline de datos ya guardados según la spec. No prometer
  imágenes permanentes por usar caché Coil. Definir primer uso, datos parciales y
  recuperación sin inventar TTL, sincronización en segundo plano o descarga global.
- Versionar esquema y preparar migraciones que preserven favoritos. No usar
  migraciones destructivas como solución a un fallo. Mantener HTTPS y evitar
  credenciales o información sensible en logs, fixtures y mensajes de error.

## Salidas y verificación

Entregar contratos, infraestructura, cambios de esquema y escenarios manuales de
desconexión, reapertura, refresh y consistencia aplicables. Revisar SQL/mappers y
política offline; escribir pruebas pertinentes con pocketdex-tests.

No compilar, ejecutar pruebas, abrir la app ni generar esquemas mediante tareas
de compilación durante la sesión. La revisión estática no demuestra persistencia
ni disponibilidad de red. Indicar los comandos manuales del módulo afectado.
