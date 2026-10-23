<#
.SYNOPSIS
    Executa os testes com os marcadores em emoji, prontos para apresentacao.

.DESCRIPTION
    O console do Windows em portugues usa a pagina de codigo 850, que nao contem emoji:
    ao imprimir um emoji ali, o Windows troca o caractere por '?'. Este script

      1. troca a pagina de codigo do terminal para 65001 (UTF-8);
      2. ajusta a codificacao de saida do PowerShell;
      3. executa os testes forcando os marcadores em emoji;
      4. restaura a pagina de codigo original ao terminar.

    Sem o script, 'mvn test' usa automaticamente os marcadores ASCII ([OK] / [X]),
    que aparecem corretamente em qualquer terminal.

.PARAMETER Completo
    Roda 'mvn clean verify site' (testes + cobertura + os tres relatorios web)
    em vez de apenas 'mvn test'.

.PARAMETER ComRelatorios
    Abre o painel relatorios.html no navegador ao final, se os testes passarem.

.PARAMETER SemEmoji
    Usa os marcadores ASCII mesmo com o terminal em UTF-8.

.PARAMETER ComInternet
    Permite que o Maven acesse os repositorios remotos. Por padrao roda com -o (offline),
    que e mais rapido e nao depende de rede.

.EXAMPLE
    .\demo.ps1
    Execucao rapida (~6 s) com emoji: o que usar ao vivo na apresentacao.

.EXAMPLE
    .\demo.ps1 -Completo -ComRelatorios
    Build completo com cobertura e relatorios, abrindo o painel no final.
#>
[CmdletBinding()]
param(
    [switch] $Completo,
    [switch] $ComRelatorios,
    [switch] $SemEmoji,
    [switch] $ComInternet
)

$ErrorActionPreference = 'Continue'

# ---------------------------------------------------------------------------
# Terminal em UTF-8
# ---------------------------------------------------------------------------
$paginaOriginal = $null
try {
    $paginaOriginal = [int](((chcp) -replace '[^0-9]', ''))
} catch {
    $paginaOriginal = $null
}

$codificacaoOriginal = [Console]::OutputEncoding

if (-not $SemEmoji) {
    try {
        chcp 65001 | Out-Null
        [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
    } catch {
        Write-Warning 'Nao foi possivel mudar o terminal para UTF-8; usando marcadores ASCII.'
        $SemEmoji = $true
    }
}

$marcadores = if ($SemEmoji) { 'ascii' } else { 'emoji' }
$ok   = if ($SemEmoji) { '[OK]' } else { [char]::ConvertFromUtf32(0x2705) }
$erro = if ($SemEmoji) { '[X] ' } else { [char]::ConvertFromUtf32(0x274C) }
$linha = '=' * 78

try {
    Clear-Host

    $argumentos = @('-B')
    if (-not $ComInternet) { $argumentos += '-o' }
    if ($Completo) { $argumentos += @('clean', 'verify', 'site') } else { $argumentos += 'test' }
    $argumentos += "-Dtestes.marcadores=$marcadores"

    Write-Host $linha -ForegroundColor DarkGray
    Write-Host '  CAMISETAS S.A. - TDD/BDD com JUnit 5 e Cucumber' -ForegroundColor Cyan
    Write-Host ("  Comando: mvn {0}" -f ($argumentos -join ' ')) -ForegroundColor DarkGray
    Write-Host $linha -ForegroundColor DarkGray

    $cronometro = [System.Diagnostics.Stopwatch]::StartNew()
    & mvn @argumentos
    $codigoSaida = $LASTEXITCODE
    $cronometro.Stop()

    Write-Host ''
    Write-Host $linha -ForegroundColor DarkGray
    if ($codigoSaida -eq 0) {
        Write-Host ("  {0} EXECUCAO CONCLUIDA EM {1:N1} s" -f $ok, $cronometro.Elapsed.TotalSeconds) `
            -ForegroundColor Green
        Write-Host $linha -ForegroundColor DarkGray

        if ($Completo) {
            Write-Host '  Relatorios gerados:' -ForegroundColor Green
            Write-Host '    target\site\surefire-report.html            execucao dos testes'
            Write-Host '    target\site\jacoco\index.html               cobertura (JaCoCo)'
            Write-Host '    target\cucumber-reports\relatorio-cucumber.html   cenarios BDD'
        } else {
            Write-Host '  Cobertura e relatorio BDD atualizados:' -ForegroundColor Green
            Write-Host '    target\site\jacoco\index.html               cobertura (JaCoCo)'
            Write-Host '    target\cucumber-reports\relatorio-cucumber.html   cenarios BDD'
            Write-Host '  Para o relatorio de execucao, rode: .\demo.ps1 -Completo' -ForegroundColor DarkGray
        }
        Write-Host $linha -ForegroundColor DarkGray

        if ($ComRelatorios) {
            $painel = Join-Path $PSScriptRoot 'relatorios.html'
            if (Test-Path $painel) {
                Start-Process $painel
                Write-Host ("  {0} Painel aberto no navegador." -f $ok) -ForegroundColor Green
            }
        }
    } else {
        Write-Host ("  {0} A EXECUCAO FALHOU (codigo {1}) - veja os detalhes acima" -f $erro, $codigoSaida) `
            -ForegroundColor Red
        Write-Host $linha -ForegroundColor DarkGray
    }
    Write-Host ''

    exit $codigoSaida
}
finally {
    # Restaura o terminal para nao afetar os comandos seguintes.
    if ($null -ne $paginaOriginal -and -not $SemEmoji) {
        chcp $paginaOriginal | Out-Null
    }
    [Console]::OutputEncoding = $codificacaoOriginal
}
