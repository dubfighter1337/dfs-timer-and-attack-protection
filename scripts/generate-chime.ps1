# Original synthesized tone; no third-party audio or licensing dependency.
$ErrorActionPreference = 'Stop'
$outputPath = Join-Path $PSScriptRoot '..\src\main\resources\com\dfsready\soft-chime.wav'
New-Item -ItemType Directory -Force (Split-Path $outputPath) | Out-Null
$sampleRate = 44100
$sampleCount = [int]($sampleRate * 0.65)
$stream = [System.IO.File]::Create($outputPath)
$writer = [System.IO.BinaryWriter]::new($stream)
try {
    $writer.Write([Text.Encoding]::ASCII.GetBytes('RIFF'))
    $writer.Write([int](36 + $sampleCount * 2))
    $writer.Write([Text.Encoding]::ASCII.GetBytes('WAVEfmt '))
    $writer.Write([int]16)
    $writer.Write([int16]1)
    $writer.Write([int16]1)
    $writer.Write([int]$sampleRate)
    $writer.Write([int]($sampleRate * 2))
    $writer.Write([int16]2)
    $writer.Write([int16]16)
    $writer.Write([Text.Encoding]::ASCII.GetBytes('data'))
    $writer.Write([int]($sampleCount * 2))
    for ($i = 0; $i -lt $sampleCount; $i++) {
        $t = $i / [double]$sampleRate
        $attack = [Math]::Min(1.0, $t / 0.02)
        $release = [Math]::Min(1.0, ($sampleCount - 1 - $i) / ($sampleRate * 0.08))
        $envelope = $attack * $release * [Math]::Exp(-6.0 * $t)
        $tone = [Math]::Sin(2 * [Math]::PI * 660 * $t) + 0.22 * [Math]::Sin(2 * [Math]::PI * 990 * $t)
        $writer.Write([int16][Math]::Round(32767 * 0.25 * $envelope * $tone))
    }
}
finally { $writer.Dispose() }
