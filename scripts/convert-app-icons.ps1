param(
    [string]$SourceDirectory = 'docs/assets/Pokedex App - App Icons',
    [string]$DestinationDirectory = 'core/designsystem/src/main/res/drawable',
    [string]$FilterSourcePath = 'specs/home/assets/filter.svg'
)

# Strict converter for the supplied exports: opaque alpha masks become rectangular clips.
# Random and OS status icons are intentionally outside the application scope.
$ErrorActionPreference = 'Stop'
$androidNamespace = 'http://schemas.android.com/apk/res/android'
$mapping = [ordered]@{
    'Name=ArrowBack.svg' = 'pd_arrow_back'; 'Name=ChevronDown.svg' = 'pd_chevron_down'
    'Name=Close.svg' = 'pd_close'; 'Name=Comparator.svg' = 'pd_comparator'
    'Name=Heart.svg' = 'pd_heart'; 'Name=HeartFilled.svg' = 'pd_heart_filled'
    'Name=Home.svg' = 'pd_home'; 'Name=HomeFilled.svg' = 'pd_home_filled'
    'Name=Quiz.svg' = 'pd_quiz'; 'Name=QuizFilled.svg' = 'pd_quiz_filled'
    'Name=Refresh.svg' = 'pd_refresh'; 'Name=Search.svg' = 'pd_search'
    'pokeball 1.svg' = 'pd_pokeball_decoration'
    'filter.svg' = 'pd_filter'
}
function Assert-Attributes($Element, [string[]]$Allowed) {
    foreach ($attribute in $Element.Attributes) {
        if ($attribute.Name -notin $Allowed) { throw "Unsupported attribute $($attribute.Name)" }
    }
}
function Set-AndroidAttribute($Element, [string]$Name, [string]$Value) {
    $attribute = $Element.OwnerDocument.CreateAttribute('android', $Name, $androidNamespace)
    $attribute.Value = $Value
    [void]$Element.Attributes.Append($attribute)
}
function Convert-Fill([string]$Fill) {
    if ($Fill -eq 'white') { return '#FFFFFFFF' }
    if ($Fill -cmatch '^#[0-9a-fA-F]{6}$') { return '#FF' + $Fill.Substring(1).ToUpperInvariant() }
    throw "Unsupported color $Fill"
}
$outputs = @()
foreach ($name in $mapping.Keys) {
    $sourcePath = if ($name -eq 'filter.svg') { $FilterSourcePath } else { Join-Path $SourceDirectory $name }
    $svg = [xml](Get-Content -Raw -Encoding UTF8 -LiteralPath $sourcePath)
    $root = $svg.DocumentElement
    if ($root.LocalName -ne 'svg' -or $root.NamespaceURI -ne 'http://www.w3.org/2000/svg') { throw 'Expected SVG' }
    Assert-Attributes $root @('width','height','viewBox','fill','xmlns')
    $size = if ($name -eq 'pokeball 1.svg') { '88' } else { '24' }
    if ($root.width -ne $size -or $root.height -ne $size -or $root.viewBox -ne "0 0 $size $size") { throw 'Unexpected viewport' }
    $elements = @($root.ChildNodes | Where-Object { $_.NodeType -eq 'Element' })
    $masked = $name -ne 'pokeball 1.svg'
    if ($masked) {
        $masks = @($elements | Where-Object { $_.LocalName -eq 'mask' })
        $groups = @($elements | Where-Object { $_.LocalName -eq 'g' })
        if ($elements.Count -ne 2 -or $masks.Count -ne 1 -or $groups.Count -ne 1) { throw 'Expected mask and group' }
        $mask = $masks[0]; $group = $groups[0]
        Assert-Attributes $mask @('id','style','maskUnits','x','y','width','height')
        Assert-Attributes $group @('mask')
        if ($mask.style -ne 'mask-type:alpha' -or $mask.maskUnits -ne 'userSpaceOnUse' -or $mask.x -ne '0' -or $mask.y -ne '0' -or $mask.width -ne $size -or $mask.height -ne $size) { throw 'Unsupported mask' }
        if ($group.mask -ne "url(#$($mask.id))") { throw 'Unresolved mask' }
        $rects = @($mask.ChildNodes | Where-Object { $_.NodeType -eq 'Element' })
        if ($rects.Count -ne 1 -or $rects[0].LocalName -ne 'rect') { throw 'Expected opaque rectangle' }
        Assert-Attributes $rects[0] @('width','height','fill')
        if ($rects[0].width -ne $size -or $rects[0].height -ne $size -or $rects[0].fill -ne '#D9D9D9') { throw 'Unsupported mask rectangle' }
        $paths = @($group.ChildNodes | Where-Object { $_.NodeType -eq 'Element' })
    } else { $paths = $elements }
    $vector = New-Object Xml.XmlDocument
    [void]$vector.AppendChild($vector.CreateXmlDeclaration('1.0','utf-8',$null))
    $vectorRoot = $vector.CreateElement('vector'); [void]$vector.AppendChild($vectorRoot)
    Set-AndroidAttribute $vectorRoot 'width' ($size + 'dp')
    Set-AndroidAttribute $vectorRoot 'height' ($size + 'dp')
    Set-AndroidAttribute $vectorRoot 'viewportWidth' $size
    Set-AndroidAttribute $vectorRoot 'viewportHeight' $size
    $container = $vectorRoot
    if ($masked) {
        $container = $vector.CreateElement('group'); [void]$vectorRoot.AppendChild($container)
        $clip = $vector.CreateElement('clip-path')
        Set-AndroidAttribute $clip 'pathData' "M0,0H${size}V${size}H0Z"
        [void]$container.AppendChild($clip)
    }
    foreach ($path in $paths) {
        if ($path.LocalName -ne 'path') { throw 'Unsupported geometry' }
        Assert-Attributes $path @('d','fill')
        if (-not $path.d) { throw 'Missing path data' }
        $converted = $vector.CreateElement('path')
        Set-AndroidAttribute $converted 'pathData' $path.d
        Set-AndroidAttribute $converted 'fillColor' (Convert-Fill $path.fill)
        Set-AndroidAttribute $converted 'fillType' 'nonZero'
        [void]$container.AppendChild($converted)
    }
    $outputs += [PSCustomObject]@{ Name = $mapping[$name]; Document = $vector }
}
New-Item -ItemType Directory -Force -Path $DestinationDirectory | Out-Null
$settings = New-Object Xml.XmlWriterSettings
$settings.Indent = $true; $settings.Encoding = New-Object Text.UTF8Encoding($false)
$settings.NewLineChars = "`n"
foreach ($output in $outputs) {
    $writer = [Xml.XmlWriter]::Create((Join-Path $DestinationDirectory ($output.Name + '.xml')), $settings)
    try { $output.Document.Save($writer) } finally { $writer.Dispose() }
    $output.Name
}
