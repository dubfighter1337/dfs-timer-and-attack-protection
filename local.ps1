param(
    [ValidateSet('test', 'run', 'smoke')]
    [string]$Action = 'test'
)
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
$previousJava = $env:JAVA_HOME
$previousGradle = $env:GRADLE_USER_HOME
try {
    $localJdk = Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot '.tools\jdk') -Directory -ErrorAction SilentlyContinue |
        Where-Object { Test-Path (Join-Path $_.FullName 'bin\javac.exe') } | Select-Object -First 1
    if ($localJdk) { $env:JAVA_HOME = $localJdk.FullName }
    if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME 'bin\javac.exe'))) {
        throw 'Set JAVA_HOME to a JDK 17 installation or restore the project-local .tools\jdk folder.'
    }
    $env:GRADLE_USER_HOME = Join-Path $PSScriptRoot '.gradle'
    switch ($Action) {
        'test' { & .\gradlew.bat test jar launcherSmoke --rerun-tasks --console=plain }
        'run' { & .\gradlew.bat run --console=plain }
        'smoke' { & .\gradlew.bat launcherSmoke --console=plain }
    }
    if ($LASTEXITCODE -ne 0) { throw "Gradle failed with exit code $LASTEXITCODE" }
}
finally {
    $env:JAVA_HOME = $previousJava
    $env:GRADLE_USER_HOME = $previousGradle
    Pop-Location
}
