# Sincroniza solo las skills del proyecto. No ejecuta Codex, Gradle ni pruebas.
[CmdletBinding()]
param([switch]$Check)

$ErrorActionPreference = 'Stop'
$projectRoot = [IO.Path]::GetFullPath((Split-Path -Parent $PSScriptRoot))
$sourceRoot = Join-Path $projectRoot 'skills'
$agentsRoot = Join-Path $projectRoot '.agents'
$destinationRoot = Join-Path $agentsRoot 'skills'
$manifestPath = Join-Path $agentsRoot 'pocketdex-skills-manifest.json'

function Assert-LocalPath([string]$Path) {
    $fullPath = [IO.Path]::GetFullPath($Path)
    $prefix = $projectRoot.TrimEnd([IO.Path]::DirectorySeparatorChar) + [IO.Path]::DirectorySeparatorChar
    if (-not $fullPath.StartsWith($prefix, [StringComparison]::OrdinalIgnoreCase)) {
        throw "La ruta sale del proyecto: $Path"
    }
    $candidate = $fullPath
    while ($candidate -and $candidate -ne $projectRoot) {
        if (Test-Path -LiteralPath $candidate) {
            $item = Get-Item -LiteralPath $candidate -Force
            if ($item.Attributes -band [IO.FileAttributes]::ReparsePoint) {
                throw "No se sincronizan enlaces o junctions: $candidate"
            }
        }
        $candidate = Split-Path -Parent $candidate
    }
}

Assert-LocalPath $sourceRoot
Assert-LocalPath $destinationRoot
Assert-LocalPath $manifestPath
if (-not (Test-Path -LiteralPath $sourceRoot -PathType Container)) {
    throw 'No existe la carpeta skills del proyecto.'
}

$previousHashes = @{}
if (Test-Path -LiteralPath $manifestPath) {
    $previous = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
    if ($previous.owner -ne 'pocketdex' -or $previous.schemaVersion -ne 1) {
        throw 'Manifest no reconocido; revisar antes de sincronizar.'
    }
    foreach ($entry in $previous.files) {
        if ($previousHashes.ContainsKey($entry.path)) { throw 'Manifest con rutas duplicadas.' }
        $previousHashes[$entry.path] = $entry.sha256
    }
}

$inventory = @()
$skillDirectories = @(Get-ChildItem -LiteralPath $sourceRoot -Directory | Sort-Object Name)
if ($skillDirectories.Count -eq 0) { throw 'No hay skills para sincronizar.' }
foreach ($skill in $skillDirectories) {
    Assert-LocalPath $skill.FullName
    if ($skill.Name -notmatch '^[a-z0-9]+(?:-[a-z0-9]+)*$' -or
        -not (Test-Path -LiteralPath (Join-Path $skill.FullName 'SKILL.md') -PathType Leaf)) {
        throw "Carpeta de skill inválida: $($skill.Name)"
    }
    # Rechazar enlaces antes de enumerar archivos para no seguir contenido externo.
    foreach ($child in Get-ChildItem -LiteralPath $skill.FullName -Recurse -Force) {
        Assert-LocalPath $child.FullName
    }
    foreach ($file in Get-ChildItem -LiteralPath $skill.FullName -Recurse -File -Force) {
        $relative = $file.FullName.Substring($sourceRoot.Length + 1).Replace('\', '/')
        $destination = Join-Path $destinationRoot $relative
        Assert-LocalPath $destination
        $hash = (Get-FileHash -LiteralPath $file.FullName -Algorithm SHA256).Hash
        $inventory += [PSCustomObject]@{
            path = $relative
            sha256 = $hash
            source = $file.FullName
            destination = $destination
        }
    }
}

# Preflight completo: preservar copias modificadas y archivos sin propiedad demostrada.
$outOfSync = @()
foreach ($entry in $inventory) {
    if (Test-Path -LiteralPath $entry.destination) {
        if (-not (Test-Path -LiteralPath $entry.destination -PathType Leaf)) {
            throw "El destino no es un archivo: $($entry.path)"
        }
        $currentHash = (Get-FileHash -LiteralPath $entry.destination -Algorithm SHA256).Hash
        if ($currentHash -ne $entry.sha256) {
            if (-not $previousHashes.ContainsKey($entry.path) -or
                $currentHash -ne $previousHashes[$entry.path]) {
                throw "Copia modificada o no administrada: $($entry.path). Revisar sin sobrescribir."
            }
            $outOfSync += $entry.path
        }
    } else {
        $outOfSync += $entry.path
    }
}
$currentPaths = @($inventory | ForEach-Object { $_.path })
foreach ($oldPath in $previousHashes.Keys) {
    if ($currentPaths -notcontains $oldPath) {
        throw "La fuente eliminó $oldPath. Revisar la copia antigua; no se elimina automáticamente."
    }
}

if ($Check) {
    if ($outOfSync.Count -gt 0) { throw "Copias desactualizadas o ausentes: $($outOfSync -join ', ')" }
    Write-Output "Sincronización correcta: $($skillDirectories.Count) skills, $($inventory.Count) archivos."
    return
}

foreach ($entry in $inventory) {
    if ($outOfSync -contains $entry.path) {
        $parentDirectory = Split-Path -Parent $entry.destination
        New-Item -ItemType Directory -Path $parentDirectory -Force | Out-Null
        Copy-Item -LiteralPath $entry.source -Destination $entry.destination
    }
}
New-Item -ItemType Directory -Path $agentsRoot -Force | Out-Null
$manifest = [ordered]@{
    owner = 'pocketdex'
    schemaVersion = 1
    files = @($inventory | Select-Object path, sha256)
}
$manifest | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath $manifestPath -Encoding UTF8
Write-Output "Sincronizadas $($skillDirectories.Count) skills; actualizados $($outOfSync.Count) archivos."
