param(
    [string]$SourceDirectory = 'docs/assets/Pokedex App - Type Icons',
    [string]$DestinationDirectory = 'core/designsystem/src/main/res/drawable'
)

# Conversion of the supplied Figma export subset, not a general SVG importer.
# Unsupported attributes/elements fail before any output is written.
$ErrorActionPreference = 'Stop'
$androidNamespace = 'http://schemas.android.com/apk/res/android'
$svgNamespace = 'http://www.w3.org/2000/svg'
$expectedTypes = @('bug','dark','dragon','electric','fairy','fighting','fire','flying','ghost','grass','ground','ice','normal','poison','psychic','rock','steel','water')

function Assert-Attributes($Element, [string[]]$Allowed) {
    foreach ($attribute in $Element.Attributes) {
        if ($attribute.Name -notin $Allowed) { throw "Unsupported attribute $($attribute.Name) on $($Element.LocalName)" }
    }
}

function Convert-Fill([string]$Fill) {
    switch ($Fill) {
        'none' { return '#00000000' }
        'white' { return '#FFFFFFFF' }
        'black' { return '#FF000000' }
        default {
            if ($Fill -cmatch '^#[0-9A-Fa-f]{6}$') { return "#FF$($Fill.Substring(1).ToUpperInvariant())" }
            throw "Unsupported color $Fill"
        }
    }
}

function Set-AndroidAttribute($Element, [string]$Name, [string]$Value) {
    $attribute = $Element.OwnerDocument.CreateAttribute('android', $Name, $androidNamespace)
    $attribute.Value = $Value
    [void]$Element.Attributes.Append($attribute)
}

$sources = @(Get-ChildItem -LiteralPath $SourceDirectory -Filter '*.svg' | Sort-Object Name)
if ($sources.Count -ne 18) { throw 'Expected all 18 supplied type icons' }
$outputs = @()
foreach ($source in $sources) {
    if ($source.BaseName -cnotmatch '^Type=([A-Za-z]+)$') { throw "Unexpected filename $($source.Name)" }
    $type = $Matches[1].ToLowerInvariant()
    if ($type -notin $expectedTypes) { throw "Unknown type $type" }
    $svg = [xml](Get-Content -Raw -Encoding UTF8 -LiteralPath $source.FullName)
    $root = $svg.DocumentElement
    if ($root.LocalName -ne 'svg' -or $root.NamespaceURI -ne $svgNamespace) { throw 'Expected SVG document' }
    Assert-Attributes $root @('width','height','viewBox','fill','xmlns')
    if ($root.viewBox -ne '0 0 16 16' -or $root.width -ne '16' -or $root.height -ne '16') { throw 'Expected 16 by 16 viewport' }
    $groups = @($root.ChildNodes | Where-Object { $_.NodeType -eq 'Element' -and $_.LocalName -eq 'g' })
    $definitions = @($root.ChildNodes | Where-Object { $_.NodeType -eq 'Element' -and $_.LocalName -eq 'defs' })
    $unexpected = @($root.ChildNodes | Where-Object { $_.NodeType -eq 'Element' -and $_.LocalName -notin @('g','defs') })
    if ($groups.Count -ne 1 -or $definitions.Count -ne 1 -or $unexpected.Count) { throw 'Unexpected SVG structure' }
    Assert-Attributes $groups[0] @('clip-path')
    Assert-Attributes $definitions[0] @()
    $clip = @($definitions[0].ChildNodes | Where-Object { $_.NodeType -eq 'Element' })
    if ($clip.Count -ne 1 -or $clip[0].LocalName -ne 'clipPath') { throw 'Expected single clipping definition' }
    Assert-Attributes $clip[0] @('id')
    if ($groups[0].GetAttribute('clip-path') -ne "url(#$($clip[0].id))") { throw 'Unresolved clip reference' }
    $rect = @($clip[0].ChildNodes | Where-Object { $_.NodeType -eq 'Element' })
    if ($rect.Count -ne 1 -or $rect[0].LocalName -ne 'rect') { throw 'Expected rectangular clipping path' }
    Assert-Attributes $rect[0] @('width','height','fill')
    if ($rect[0].width -ne '16' -or $rect[0].height -ne '16') { throw 'Unexpected clipping bounds' }

    $vector = New-Object System.Xml.XmlDocument
    [void]$vector.AppendChild($vector.CreateXmlDeclaration('1.0','utf-8',$null))
    $vectorRoot = $vector.CreateElement('vector')
    [void]$vector.AppendChild($vectorRoot)
    Set-AndroidAttribute $vectorRoot 'width' '16dp'
    Set-AndroidAttribute $vectorRoot 'height' '16dp'
    Set-AndroidAttribute $vectorRoot 'viewportWidth' '16'
    Set-AndroidAttribute $vectorRoot 'viewportHeight' '16'
    $vectorGroup = $vector.CreateElement('group')
    [void]$vectorRoot.AppendChild($vectorGroup)
    $vectorClip = $vector.CreateElement('clip-path')
    Set-AndroidAttribute $vectorClip 'pathData' 'M0,0H16V16H0Z'
    [void]$vectorGroup.AppendChild($vectorClip)
    $paths = @($groups[0].ChildNodes | Where-Object { $_.NodeType -eq 'Element' })
    foreach ($path in $paths) {
        if ($path.LocalName -ne 'path') { throw "Unsupported element $($path.LocalName)" }
        Assert-Attributes $path @('d','fill','fill-rule','clip-rule')
        if (-not $path.d) { throw 'Missing path data' }
        $rule = $path.GetAttribute('fill-rule')
        if ($rule -and $rule -notin @('evenodd','nonzero')) { throw "Unsupported fill rule $rule" }
        $converted = $vector.CreateElement('path')
        Set-AndroidAttribute $converted 'pathData' $path.d
        Set-AndroidAttribute $converted 'fillColor' (Convert-Fill $path.fill)
        Set-AndroidAttribute $converted 'fillType' $(if ($rule -eq 'evenodd') { 'evenOdd' } else { 'nonZero' })
        [void]$vectorGroup.AppendChild($converted)
    }
    $outputs += [PSCustomObject]@{ Type = $type; Document = $vector; Source = $source; Paths = $paths.Count }
}
if (@($outputs.Type | Sort-Object -Unique).Count -ne 18) { throw 'Duplicate or missing types' }
New-Item -ItemType Directory -Force -Path $DestinationDirectory | Out-Null
$settings = New-Object System.Xml.XmlWriterSettings
$settings.Indent = $true
$settings.Encoding = New-Object System.Text.UTF8Encoding($false)
$settings.NewLineChars = "`n"
foreach ($output in $outputs) {
    $destination = Join-Path $DestinationDirectory "pd_type_$($output.Type).xml"
    $writer = [Xml.XmlWriter]::Create($destination, $settings)
    try { $output.Document.Save($writer) } finally { $writer.Dispose() }
    "$($output.Source.Name) -> $destination ($($output.Paths) paths)"
}
