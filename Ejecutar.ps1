param(
    [string]$Conector = '',
    [switch]$Pruebas,
    [switch]$Integracion
)
$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new()
$env:JAVA_TOOL_OPTIONS = '-Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8'
$java = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin\java.exe' } else { (Get-Command java -ErrorAction Stop).Source }
$javac = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin\javac.exe' } else { (Get-Command javac -ErrorAction Stop).Source }
$salida = Join-Path $PSScriptRoot 'out'
$fuentes = @(Get-ChildItem (Join-Path $PSScriptRoot 'src\*.java') | ForEach-Object FullName)
if ($Pruebas -or $Integracion) { $fuentes += @(Get-ChildItem (Join-Path $PSScriptRoot 'tests\*.java') | ForEach-Object FullName) }
& $javac -encoding UTF-8 --release 17 -d $salida $fuentes
if ($LASTEXITCODE -ne 0) { throw 'Falló la compilación.' }
if ($Pruebas -or $Integracion) {
    & $java -cp $salida PruebasModelo
    if ($LASTEXITCODE -ne 0) { throw 'Fallaron las pruebas del modelo.' }
    & $java -cp $salida PruebasConsola
    if ($LASTEXITCODE -ne 0) { throw 'Fallaron las pruebas de consola.' }
    if (!$Integracion) { return }
}
if (!(Test-Path -LiteralPath $Conector -PathType Leaf)) { throw 'Indicá la ruta del archivo .jar mediante -Conector.' }
$secreto = $null
try {
    if (!$env:HUELLAS_DB_PASSWORD) {
        $secreto = Read-Host 'Contraseña de MySQL' -AsSecureString
        $env:HUELLAS_DB_PASSWORD = [System.Net.NetworkCredential]::new('', $secreto).Password
    }
    $clase = if ($Integracion) { 'PruebasIntegracion' } else { 'HuellasApp' }
    & $java -cp "$salida;$Conector" $clase
    if ($LASTEXITCODE -ne 0) { throw 'La ejecución terminó con un error.' }
} finally {
    if ($null -ne $secreto) {
        Remove-Item Env:HUELLAS_DB_PASSWORD -ErrorAction SilentlyContinue
        $secreto.Dispose()
    }
}
