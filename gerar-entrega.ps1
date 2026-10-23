<#
.SYNOPSIS
    Gera o pacote de entrega do trabalho de TDD/BDD + JUnit (Camisetas S.A.).

.DESCRIPTION
    Executa o build completo (testes + cobertura + relatorios), copia os relatorios
    web para caminhos estaveis, monta o painel de navegacao, empacota o codigo-fonte
    e gera um unico arquivo .zip pronto para envio.

    Cada etapa imprime seu proprio status no terminal, e o script aborta na
    primeira falha para que nenhuma entrega seja gerada a partir de um build quebrado.

    Estrutura produzida:

        entrega/
        |-- index.html                          painel com os 3 relatorios
        |-- RELATORIO.md                        documentacao da parte pratica
        |-- camisetas-sa-codigo-fonte.zip       pom.xml + src/ completo
        \-- relatorios/
            |-- site/                           Surefire Report + JaCoCo + CSS
            \-- cucumber/                       relatorio HTML do Cucumber

.PARAMETER PularBuild
    Reaproveita o conteudo atual de target/ em vez de rodar 'mvn clean verify site'.
    Use somente se o build acabou de rodar com sucesso.

.PARAMETER SemEmoji
    Usa marcadores ASCII ([OK] / [X]) em vez de emoji, para terminais antigos.

.EXAMPLE
    .\gerar-entrega.ps1

.EXAMPLE
    .\gerar-entrega.ps1 -PularBuild -SemEmoji
#>
[CmdletBinding()]
param(
    [switch] $PularBuild,
    [switch] $SemEmoji
)

$ErrorActionPreference = 'Stop'

# Barras de progresso do Compress-Archive poluem o log do terminal.
$ProgressPreference = 'SilentlyContinue'

# UTF-8 na saida para que os marcadores visuais apareçam corretamente.
try { [Console]::OutputEncoding = [System.Text.Encoding]::UTF8 } catch { $SemEmoji = $true }

$script:OK      = if ($SemEmoji) { '[OK]' } else { [char]::ConvertFromUtf32(0x2705) }
$script:ERRO    = if ($SemEmoji) { '[X] ' } else { [char]::ConvertFromUtf32(0x274C) }
$script:ETAPA   = if ($SemEmoji) { '>'    } else { [char]::ConvertFromUtf32(0x25B6) }
$script:PACOTE  = if ($SemEmoji) { '='    } else { [char]::ConvertFromUtf32(0x1F4E6) }
$script:RELATOR = if ($SemEmoji) { '='    } else { [char]::ConvertFromUtf32(0x1F4CA) }
$script:TEMPO   = if ($SemEmoji) { '~'    } else { [char]::ConvertFromUtf32(0x23F1) }

$script:Linha = '=' * 78
$script:NumeroEtapa = 0
$script:TotalEtapas = 5
$script:Cronometro = [System.Diagnostics.Stopwatch]::StartNew()

$raiz = $PSScriptRoot
$destino = Join-Path $raiz 'entrega'
$target = Join-Path $raiz 'target'

function Escrever-Cabecalho {
    Write-Host ''
    Write-Host $script:Linha -ForegroundColor DarkGray
    Write-Host "  $script:PACOTE CAMISETAS S.A. - GERACAO DO PACOTE DE ENTREGA" -ForegroundColor Cyan
    Write-Host $script:Linha -ForegroundColor DarkGray
}

function Escrever-Etapa([string] $texto) {
    $script:NumeroEtapa++
    Write-Host ''
    Write-Host ("  {0} Etapa {1}/{2}: {3}" -f $script:ETAPA, $script:NumeroEtapa, $script:TotalEtapas, $texto) `
        -ForegroundColor Cyan
}

function Escrever-Sucesso([string] $texto) {
    Write-Host ("     {0} {1}" -f $script:OK, $texto) -ForegroundColor Green
}

function Escrever-Falha([string] $texto) {
    Write-Host ("     {0} {1}" -f $script:ERRO, $texto) -ForegroundColor Red
}

function Confirmar-Arquivo([string] $caminhoRelativo, [string] $descricao) {
    $caminho = Join-Path $destino $caminhoRelativo
    if (Test-Path $caminho) {
        Escrever-Sucesso ("{0,-48} {1}" -f $descricao, $caminhoRelativo)
        return $true
    }
    Escrever-Falha ("{0,-48} AUSENTE: {1}" -f $descricao, $caminhoRelativo)
    return $false
}

Escrever-Cabecalho

try {
    # -----------------------------------------------------------------------
    # 1. Build completo: testes, cobertura e relatorios
    # -----------------------------------------------------------------------
    if ($PularBuild) {
        Escrever-Etapa 'Build (pulado por -PularBuild)'
        if (-not (Test-Path (Join-Path $target 'site\jacoco\index.html'))) {
            throw 'target/ nao contem os relatorios. Rode o script sem -PularBuild.'
        }
        Escrever-Sucesso 'Relatorios encontrados em target/'
    } else {
        Escrever-Etapa 'Executando mvn clean verify site (testes + cobertura + relatorios)'
        & mvn -B clean verify site
        if ($LASTEXITCODE -ne 0) {
            throw "O build falhou (codigo $LASTEXITCODE). Corrija os testes antes de gerar a entrega."
        }
        Escrever-Sucesso 'Build concluido: testes aprovados e cobertura dentro do minimo exigido'
    }

    # -----------------------------------------------------------------------
    # 2. Estrutura da pasta de entrega
    # -----------------------------------------------------------------------
    Escrever-Etapa 'Copiando os relatorios para caminhos estaveis'
    if (Test-Path $destino) {
        Remove-Item $destino -Recurse -Force
    }
    New-Item -ItemType Directory -Path $destino | Out-Null
    New-Item -ItemType Directory -Path (Join-Path $destino 'relatorios') | Out-Null

    # A pasta site/ vai inteira para que os links internos e o CSS continuem validos.
    Copy-Item (Join-Path $target 'site') (Join-Path $destino 'relatorios\site') -Recurse
    Escrever-Sucesso 'Surefire Report + JaCoCo copiados (com folhas de estilo)'

    New-Item -ItemType Directory -Path (Join-Path $destino 'relatorios\cucumber') | Out-Null
    Copy-Item (Join-Path $target 'cucumber-reports\relatorio-cucumber.html') `
              (Join-Path $destino 'relatorios\cucumber') -Force
    foreach ($extra in 'cucumber.json', 'cucumber-junit.xml') {
        $origem = Join-Path $target "cucumber-reports\$extra"
        if (Test-Path $origem) {
            Copy-Item $origem (Join-Path $destino 'relatorios\cucumber') -Force
        }
    }
    Escrever-Sucesso 'Relatorio do Cucumber copiado (HTML + JSON + XML)'

    # -----------------------------------------------------------------------
    # 3. Painel de navegacao com os caminhos da entrega
    # -----------------------------------------------------------------------
    Escrever-Etapa 'Gerando o painel entrega/index.html'
    $painel = Get-Content (Join-Path $raiz 'relatorios.html') -Raw -Encoding UTF8

    $painel = $painel.Replace('href="target/site/surefire-report.html"',
                              'href="relatorios/site/surefire-report.html"')
    $painel = $painel.Replace('href="target/site/jacoco/index.html"',
                              'href="relatorios/site/jacoco/index.html"')
    $painel = $painel.Replace('href="target/cucumber-reports/relatorio-cucumber.html"',
                              'href="relatorios/cucumber/relatorio-cucumber.html"')

    $secaoConteudo = @'
    <h2>Conteudo desta entrega</h2>
    <table>
        <thead>
        <tr>
            <th scope="col">Arquivo</th>
            <th scope="col">Descricao</th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <th scope="row"><code>index.html</code></th>
            <td>Este painel, com os indicadores e os links dos relatorios</td>
        </tr>
        <tr>
            <th scope="row"><code>RELATORIO.md</code></th>
            <td>Documentacao da parte pratica: estrutura, fragmentos de teste comentados, anotacoes, assertivas, BDD e cobertura</td>
        </tr>
        <tr>
            <th scope="row"><code>relatorios/site/surefire-report.html</code></th>
            <td>Resultado de cada teste JUnit e de cada cenario BDD</td>
        </tr>
        <tr>
            <th scope="row"><code>relatorios/site/jacoco/index.html</code></th>
            <td>Cobertura de codigo navegavel, com o fonte destacado</td>
        </tr>
        <tr>
            <th scope="row"><code>relatorios/cucumber/relatorio-cucumber.html</code></th>
            <td>Funcionalidades e cenarios Gherkin, passo a passo</td>
        </tr>
        <tr>
            <th scope="row"><code>camisetas-sa-codigo-fonte.zip</code></th>
            <td>Projeto Maven completo (<code>pom.xml</code> + <code>src/</code>), pronto para <code>mvn clean verify site</code></td>
        </tr>
        </tbody>
    </table>
</main>
'@

    $painel = $painel.Replace('</main>', $secaoConteudo)
    Set-Content -Path (Join-Path $destino 'index.html') -Value $painel -Encoding UTF8
    Escrever-Sucesso 'Painel gerado com os links reescritos para a pasta de entrega'

    # -----------------------------------------------------------------------
    # 4. Documentacao e codigo-fonte
    # -----------------------------------------------------------------------
    Escrever-Etapa 'Empacotando a documentacao e o codigo-fonte'
    Copy-Item (Join-Path $raiz 'README.md') (Join-Path $destino 'RELATORIO.md') -Force
    Escrever-Sucesso 'README.md copiado como RELATORIO.md'

    $itensFonte = @('pom.xml', 'src', 'README.md', 'relatorios.html', 'gerar-entrega.ps1', '.gitignore') |
            ForEach-Object { Join-Path $raiz $_ } |
            Where-Object { Test-Path $_ }

    Compress-Archive -Path $itensFonte `
                     -DestinationPath (Join-Path $destino 'camisetas-sa-codigo-fonte.zip') `
                     -Force
    Escrever-Sucesso ("Codigo-fonte compactado ({0} itens)" -f $itensFonte.Count)

    # -----------------------------------------------------------------------
    # 5. Conferencia e pacote unico
    # -----------------------------------------------------------------------
    Escrever-Etapa 'Conferindo o conteudo e gerando o pacote final'

    $conferencias = @(
        (Confirmar-Arquivo 'index.html'                                   'Painel de navegacao'),
        (Confirmar-Arquivo 'RELATORIO.md'                                 'Documentacao da parte pratica'),
        (Confirmar-Arquivo 'relatorios\site\surefire-report.html'         'Relatorio de execucao dos testes'),
        (Confirmar-Arquivo 'relatorios\site\jacoco\index.html'            'Relatorio de cobertura (JaCoCo)'),
        (Confirmar-Arquivo 'relatorios\cucumber\relatorio-cucumber.html'  'Relatorio dos cenarios BDD'),
        (Confirmar-Arquivo 'camisetas-sa-codigo-fonte.zip'                'Codigo-fonte do projeto')
    )

    if ($conferencias -contains $false) {
        throw 'A conferencia encontrou arquivos ausentes. O pacote nao foi gerado.'
    }

    # Valida tambem os links do painel, que sao o que o avaliador vai clicar.
    $linksQuebrados = 0
    foreach ($match in [regex]::Matches($painel, 'href="([^"]+\.html)"')) {
        $alvo = Join-Path $destino $match.Groups[1].Value.Replace('/', '\')
        if (-not (Test-Path $alvo)) {
            Escrever-Falha ("Link quebrado no painel: {0}" -f $match.Groups[1].Value)
            $linksQuebrados++
        }
    }
    if ($linksQuebrados -gt 0) {
        throw "$linksQuebrados link(s) do painel apontam para arquivos inexistentes."
    }
    Escrever-Sucesso 'Todos os links do painel resolvem para arquivos existentes'

    $nomePacote = 'camisetas-sa-tdd-bdd-junit.zip'
    $pacote = Join-Path $raiz $nomePacote
    if (Test-Path $pacote) {
        Remove-Item $pacote -Force
    }
    Compress-Archive -Path (Join-Path $destino '*') -DestinationPath $pacote -Force
    Escrever-Sucesso ("Pacote gerado: {0}" -f $nomePacote)

    # -----------------------------------------------------------------------
    # Resumo
    # -----------------------------------------------------------------------
    $script:Cronometro.Stop()
    $tamanho = (Get-Item $pacote).Length / 1MB

    Write-Host ''
    Write-Host $script:Linha -ForegroundColor DarkGray
    Write-Host ("  {0} ENTREGA PRONTA" -f $script:RELATOR) -ForegroundColor Green
    Write-Host $script:Linha -ForegroundColor DarkGray
    Write-Host ("  {0} Pacote ..... {1}" -f $script:OK, $pacote)
    Write-Host ("  {0} Tamanho .... {1:N1} MB" -f $script:OK, $tamanho)
    Write-Host ("  {0} Pasta ...... {1}" -f $script:OK, $destino)
    Write-Host ("  {0} Duracao .... {1:N1} s" -f $script:TEMPO, $script:Cronometro.Elapsed.TotalSeconds)
    Write-Host $script:Linha -ForegroundColor DarkGray
    Write-Host ("  {0} Antes de enviar, abra entrega\index.html no navegador e clique nos 3 relatorios." -f $script:OK) `
        -ForegroundColor Green
    Write-Host $script:Linha -ForegroundColor DarkGray
    Write-Host ''
}
catch {
    $script:Cronometro.Stop()
    Write-Host ''
    Write-Host $script:Linha -ForegroundColor DarkGray
    Write-Host ("  {0} ENTREGA NAO GERADA" -f $script:ERRO) -ForegroundColor Red
    Write-Host $script:Linha -ForegroundColor DarkGray
    Write-Host ("  Motivo: {0}" -f $_.Exception.Message) -ForegroundColor Red
    Write-Host $script:Linha -ForegroundColor DarkGray
    Write-Host ''
    exit 1
}
