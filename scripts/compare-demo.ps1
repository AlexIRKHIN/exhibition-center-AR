param([string]$Java = "java")

$ErrorActionPreference = "Stop"
$repoDir = Split-Path -Parent $PSScriptRoot
$dataDir = Join-Path $repoDir "algorithms/data"
$proceduralClasses = Join-Path $repoDir "algorithms/target/classes"
$coreClasses = Join-Path $repoDir "exhibition-core/target/classes"

# Перед запуском соберите оба модуля командой mvn test из корня проекта.
$procedural = @(& $Java '-Dstdout.encoding=UTF-8' -cp $proceduralClasses `
    ru.mirea.exhibition.procedural.Main $dataDir ST-01 2026-10-27T18:00:00)
if ($LASTEXITCODE -ne 0) { throw "Procedural Main failed: $LASTEXITCODE" }
$oop = @(& $Java '-Dstdout.encoding=UTF-8' -cp $coreClasses `
    ru.mirea.exhibition.core.app.Main $dataDir ST-01 2026-10-27T18:00:00)
if ($LASTEXITCODE -ne 0) { throw "Object-oriented Main failed: $LASTEXITCODE" }

# У объектной программы есть одна дополнительная строка с названием реализации.
$oopResults = @($oop | Select-Object -Skip 1)
if (($procedural -join "`n") -cne ($oopResults -join "`n")) {
    Compare-Object $procedural $oopResults -SyncWindow 0
    throw "Demo outputs differ"
}
Write-Output "MATCH: both implementations produce the same results at 2026-10-27T18:00:00"
