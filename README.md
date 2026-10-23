# Camisetas S.A. — Parte Prática (TDD/BDD + JUnit)

Implementação dos testes JUnit 5 + Cucumber para o cenário da empresa têxtil "Camisetas S.A.":
produção de camisetas por lote, emissão da nota fiscal e preparação do lote para despacho.

| Item | Valor |
|---|---|
| Stack | Java 17, Maven, JUnit 5.10.2, Cucumber 7.15.0, JaCoCo 0.8.12 |
| Testes executados | **182** (145 JUnit + 37 cenários BDD) |
| Falhas / erros | **0** |
| Ignorados | 1 (`@Disabled`, intencional) |
| Cobertura de instruções | **100%** (1.152 / 1.152) |
| Cobertura de desvios (branches) | **100%** (100 / 100) |
| Cobertura de linhas | **100%** (236 / 236) |
| Cobertura de métodos | **100%** (73 / 73) |

### Índice

1. [Como executar](#1-como-executar)
2. [Estrutura dos pacotes](#2-estrutura-dos-pacotes)
3. [Classes testadas × objetivos do enunciado](#3-classes-testadas--objetivos-do-enunciado)
4. [Fragmentos de código de testes e descrição](#4-fragmentos-de-código-de-testes-e-descrição)
5. [Anotações JUnit utilizadas](#5-anotações-junit-utilizadas)
6. [Assertivas utilizadas](#6-assertivas-utilizadas)
7. [Estrutura BDD (Cucumber)](#7-estrutura-bdd-cucumber)
8. [Cobertura dos testes (Coverage)](#8-cobertura-dos-testes-coverage)
9. [Relatórios web](#9-relatórios-web)
10. [Logs de execução no terminal](#10-logs-de-execução-no-terminal)
11. [Pacote de entrega](#11-pacote-de-entrega)
12. [Roteiro de apresentação](#12-roteiro-de-apresentação)

---

## 1. Como executar

```powershell
# roda os 182 testes, mede a cobertura, valida o mínimo de 95% e gera os relatórios web
mvn clean verify site
```

Para a execução com os marcadores visuais em emoji (terminal Windows), use o script:

```powershell
.\demo.ps1                        # testes com emoji, offline, ~6 s
.\demo.ps1 -Completo -ComRelatorios   # build completo + abre o painel no navegador
```

O script ajusta o terminal para UTF-8 antes de rodar — sem isso o Windows substitui os emoji
por `?` (detalhes na [seção 10.3](#103-codificação-do-terminal-e-os-marcadores)).

Depois abra o painel `relatorios.html` na raiz do projeto, que concentra os três relatórios:

| Relatório | Arquivo |
|---|---|
| Execução dos testes (Surefire) | `target/site/surefire-report.html` |
| Cobertura de código (JaCoCo) | `target/site/jacoco/index.html` |
| Cenários BDD (Cucumber) | `target/cucumber-reports/relatorio-cucumber.html` |

Comandos auxiliares:

```powershell
mvn test                      # só os testes (gera JaCoCo e o relatório do Cucumber)
mvn test -Dgroups=dominio     # só os testes de regra de negócio (tag @dominio)
mvn test -Dgroups=integracao  # só o fluxo de integração
mvn test -Dtest=LoteTest      # uma suíte específica
```

### Execução sem internet

Com o repositório local (`~/.m2/repository`) já populado por um build anterior, tudo roda
offline com a flag `-o`:

```powershell
mvn -o test                   # 182 testes em ~6 s
mvn -o clean verify site      # testes + cobertura + relatórios em ~14 s
```

A flag `-o` é necessária: sem ela o Maven tenta revalidar os artefatos nos repositórios
remotos e fica esperando timeout quando não há rede. Em uma máquina que **nunca** rodou o
build, as dependências precisam ser baixadas uma vez — nesse caso use os relatórios HTML já
gerados em `entrega/`, que abrem em qualquer navegador sem Java nem Maven.

---

## 2. Estrutura dos pacotes

### 2.1 Código de produção (`src/main/java`)

```
br.com.camisetas
├── dominio
│   ├── Lote.java        // número, data de fabricação, total de itens, cadastro de itens
│   ├── ItemLote.java    // item do lote: código, tamanho, cor, quantidade (imutável)
│   ├── Tamanho.java     // enum P, M, G
│   └── Cor.java         // enum BRANCO, PRETO, AZUL
├── fiscal
│   └── NotaFiscal.java  // número, data de emissão, totais por tamanho e por cor
├── logistica
│   └── Despacho.java    // código de validação, embalagem, liberação para envio
└── excecao
    ├── CamisetasException.java               // raiz da hierarquia (não checada)
    ├── DadoInvalidoException.java
    ├── CapacidadeLoteExcedidaException.java  // carrega capacidade e quantidade solicitada
    ├── ItemDuplicadoException.java
    ├── NotaFiscalInvalidaException.java
    └── DespachoInvalidoException.java
```

### 2.2 Testes (`src/test/java`)

```
br.com.camisetas
├── dominio
│   ├── LoteTest.java        // 36 testes
│   ├── ItemLoteTest.java    // 21 testes (1 @Disabled)
│   ├── TamanhoTest.java     // 16 testes
│   └── CorTest.java         // 14 testes
├── fiscal
│   └── NotaFiscalTest.java  // 30 testes
├── logistica
│   └── DespachoTest.java    // 17 testes
├── excecao
│   └── ExcecoesDeNegocioTest.java      // 8 testes
├── integracao
│   └── FluxoProducaoCamisetaTest.java  // 3 testes de ponta a ponta
├── suporte
│   ├── CenarioFabrica.java   // Object Mother: massa de teste reaproveitável
│   ├── TesteDeDominio.java   // anotação personalizada = @Test + @Tag("dominio")
│   ├── TesteDeIntegracao.java// anotação personalizada = @Test + @Tag("integracao")
│   ├── RelatorioConsoleListener.java // logs de ✅/❌ no terminal + resumo final
│   └── Simbolos.java         // marcadores visuais com fallback ASCII
└── bdd
    ├── ExecutaCenariosBddTest.java  // runner @Suite do engine Cucumber
    └── passos
        ├── ContextoProducao.java   // estado compartilhado do cenário (injetado)
        ├── PassosComuns.java       // hooks @Before/@After + recusas de operação
        ├── PassosLote.java
        ├── PassosNotaFiscal.java
        └── PassosDespacho.java
```

### 2.3 Recursos de teste (`src/test/resources`)

```
junit-platform.properties          // glue, plugins e relatórios do Cucumber
META-INF/services
└── org.junit.platform.launcher.TestExecutionListener  // registra o listener de console
br/com/camisetas/bdd/features
├── lote.feature                   // 13 cenários
├── nota_fiscal.feature            // 13 cenários
├── nota_fiscal_lote_vazio.feature //  1 cenário
├── despacho.feature               //  8 cenários
└── fluxo_producao.feature         //  2 cenários de ponta a ponta
```

### 2.4 Arquivos na raiz do projeto

```
pom.xml               // dependências, plugins, regra de cobertura mínima
README.md             // esta documentação
relatorios.html       // painel com indicadores e links dos 3 relatórios
demo.ps1              // execução com emoji, para a apresentação (ver seção 10.3)
gerar-entrega.ps1     // build + empacotamento da entrega (ver seção 11)
.gitignore            // ignora target/, entrega/ e *.zip
```

### 2.5 Convenções adotadas

| Elemento | Convenção | Exemplo |
|---|---|---|
| Pacote | minúsculo, sem acento, separado por responsabilidade | `br.com.camisetas.fiscal` |
| Classe testada | `PascalCase`, substantivo do domínio | `NotaFiscal` |
| Classe de teste | nome da classe testada + sufixo `Test` | `NotaFiscalTest` |
| Classe aninhada | `@Nested` + `PascalCase` descrevendo o grupo | `TotalizacaoPorTamanho` |
| Método de teste | `deve` + comportamento esperado, em `camelCase` | `deveSomarQuantidadesPorCor()` |
| Método de etapa ordenada | `etapaN_acao` | `etapa2_embalarLote()` |
| Classe de passos BDD | prefixo `Passos` + funcionalidade | `PassosDespacho` |
| Arquivo `.feature` | `snake_case`, uma funcionalidade por arquivo | `nota_fiscal.feature` |

---

## 3. Classes testadas × objetivos do enunciado

| # | Objetivo | Classe testada | Suíte | Testes |
|---|---|---|---|---|
| 1 | Lote recebe número, data de fabricação e adiciona itens | `Lote` | `LoteTest` | 36 |
| 2 | ItemLote cadastra itens com tamanho e cor | `ItemLote` | `ItemLoteTest` | 21 |
| 3 | NotaFiscal emite e contabiliza por tamanho e cor | `NotaFiscal` | `NotaFiscalTest` | 30 |
| 4 | Despacho gera código de validação e libera o lote | `Despacho` | `DespachoTest` | 17 |
| — | Domínio de tamanhos e cores | `Tamanho`, `Cor` | `TamanhoTest`, `CorTest` | 30 |
| — | Exceções personalizadas | pacote `excecao` | `ExcecoesDeNegocioTest` | 8 |
| — | Fluxo integrado 1→4 | todas | `FluxoProducaoCamisetaTest` | 3 |
| — | Comportamento (BDD) | todas | `ExecutaCenariosBddTest` | 37 |

---

## 4. Fragmentos de código de testes e descrição

### 4.1 Objetivo 1 — Lote recebe número, data e total de itens

```java
@BeforeEach
void abrirLoteComCapacidadeDeCem() {
    lote = new Lote(NUMERO_LOTE, DATA_FABRICACAO, 100);
}

@TesteDeDominio
@DisplayName("Deve receber numero, data de fabricacao e total de itens")
void deveReceberNumeroDataFabricacaoETotalDeItens() {
    assertAll("dados de abertura do lote",
            () -> assertEquals(NUMERO_LOTE, lote.getNumero()),
            () -> assertEquals(DATA_FABRICACAO, lote.getDataFabricacao()),
            () -> assertEquals(100, lote.getTotalItens()));
}
```

**O que verifica:** o construtor de `Lote` guarda corretamente os três dados de abertura.
`@BeforeEach` recria o lote antes de cada teste, garantindo isolamento. `assertAll` agrupa as
três assertivas e reporta **todas** as falhas de uma só vez, em vez de parar na primeira.
`@TesteDeDominio` é a anotação personalizada do projeto (equivale a `@Test @Tag("dominio")`).

### 4.2 Objetivo 1 — Cadastro de itens e atualização dos totais

```java
@Test
@DisplayName("Deve adicionar um item e atualizar os totais do lote")
void deveAdicionarItemEAtualizarTotais() {
    lote.adicionarItem(new ItemLote("IT-01", Tamanho.P, Cor.BRANCO, 30));

    assertAll("totais apos 1 item",
            () -> assertEquals(1, lote.getQuantidadeDeItens()),
            () -> assertEquals(30, lote.getQuantidadeCadastrada()),
            () -> assertEquals(70, lote.getQuantidadeRestante()),
            () -> assertFalse(lote.isVazio()),
            () -> assertFalse(lote.isCompleto()));
}
```

**O que verifica:** adicionar um item altera *todos* os indicadores derivados do lote
(contagem de registros, soma das quantidades, saldo restante e os dois estados booleanos).
É o teste que prova que `adicionarItem` não deixa o objeto em estado inconsistente.

### 4.3 Objetivo 1 — Regra de capacidade com exceção personalizada

```java
@Test
@DisplayName("Deve rejeitar cadastro que excede a capacidade do lote")
void deveRejeitarCadastroQueExcedeCapacidade() {
    Lote lotePequeno = new Lote("LOTE-PEQ", DATA_FABRICACAO, 50);
    lotePequeno.adicionarItem(new ItemLote("IT-01", Tamanho.P, Cor.BRANCO, 40));

    CapacidadeLoteExcedidaException erro = assertThrows(CapacidadeLoteExcedidaException.class,
            () -> lotePequeno.adicionarItem(new ItemLote("IT-02", Tamanho.M, Cor.AZUL, 20)));

    assertAll("detalhes da excecao personalizada",
            () -> assertEquals(50, erro.getCapacidade()),
            () -> assertEquals(60, erro.getQuantidadeSolicitada()),
            () -> assertTrue(erro.getMessage().contains("Capacidade do lote")),
            () -> assertEquals(40, lotePequeno.getQuantidadeCadastrada(),
                    "o lote permanece no estado anterior"));
}
```

**O que verifica:** além de confirmar que a exceção certa é lançada, o teste inspeciona os
dados de contexto carregados por ela (`getCapacidade`, `getQuantidadeSolicitada`) e garante
a **atomicidade** da operação: o item recusado não entra no lote.

### 4.4 Objetivo 2 — Cadastro de item com tamanho e cor (teste parametrizado)

```java
@ParameterizedTest(name = "[{index}] ItemLote.de(\"{0}\",\"{1}\") -> {2} / {3}")
@CsvSource({
        "P, Branco, P, BRANCO",
        "m, preto,  M, PRETO",
        "G, Azul,   G, AZUL"
})
@DisplayName("Fabrica textual deve converter tamanho e cor corretamente")
void fabricaTextualDeveConverterTamanhoECor(String sigla, String cor,
                                            Tamanho tamanhoEsperado, Cor corEsperada) {
    ItemLote convertido = ItemLote.de("IT-TXT", sigla, cor, 3);
    assertAll(
            () -> assertEquals(tamanhoEsperado, convertido.getTamanho()),
            () -> assertEquals(corEsperada, convertido.getCor()),
            () -> assertEquals(3, convertido.getQuantidade()));
}
```

**O que verifica:** a conversão de texto para os enums `Tamanho` e `Cor`, ignorando caixa e
espaços. Um único método cobre 3 combinações; o `name` personalizado faz cada linha aparecer
identificada no relatório web.

### 4.5 Objetivo 2 — Rejeição de quantidade inválida

```java
@ParameterizedTest(name = "[{index}] quantidade {0} deve ser rejeitada")
@ValueSource(ints = {0, -1, -100})
@DisplayName("Deve rejeitar quantidade menor ou igual a zero")
void deveRejeitarQuantidadeNaoPositiva(int quantidadeInvalida) {
    DadoInvalidoException erro = assertThrows(DadoInvalidoException.class,
            () -> new ItemLote("IT-005", Tamanho.P, Cor.AZUL, quantidadeInvalida));
    assertTrue(erro.getMessage().contains("maior que zero"),
            () -> "Mensagem inesperada: " + erro.getMessage());
}
```

**O que verifica:** o limite inferior da quantidade. O valor `0` testa a fronteira e os
negativos testam a classe de equivalência inválida. A mensagem da assertiva é passada como
`Supplier`, então só é construída se o teste falhar.

### 4.6 Objetivo 3 — Totalização por tamanho e por cor

```java
@BeforeEach
void emitirNotaFiscalDoLoteCompleto() {
    loteCompleto = CenarioFabrica.loteCompleto();   // 100 camisetas
    notaFiscal = new NotaFiscal("NF-0001", loteCompleto, DATA_EMISSAO);
}

@Test
@DisplayName("Deve somar as quantidades agrupadas por tamanho")
void deveSomarQuantidadesPorTamanho() {
    Map<Tamanho, Integer> porTamanho = notaFiscal.getQuantidadePorTamanho();

    assertAll("quantidades por tamanho",
            () -> assertEquals(3, porTamanho.size(), "todos os tamanhos devem constar na nota"),
            () -> assertEquals(30, porTamanho.get(Tamanho.P), "P = 20 brancas + 10 pretas"),
            () -> assertEquals(45, porTamanho.get(Tamanho.M), "M = 30 brancas + 15 azuis"),
            () -> assertEquals(25, porTamanho.get(Tamanho.G), "G = 20 pretas + 5 azuis"));
}
```

**O que verifica:** a nota fiscal agrega corretamente itens de tamanhos repetidos. A massa vem
de `CenarioFabrica.loteCompleto()` (P=30, M=45, G=25 e Branco=50, Preto=30, Azul=20), então os
valores esperados são conhecidos e as mensagens explicam de onde cada total sai.

```java
@Test
@DisplayName("A soma por cor deve ser igual a soma por tamanho")
void somaPorCorDeveSerIgualASomaPorTamanho() {
    int somaPorCor = notaFiscal.getQuantidadePorCor().values().stream()
            .mapToInt(Integer::intValue).sum();
    int somaPorTamanho = notaFiscal.getQuantidadePorTamanho().values().stream()
            .mapToInt(Integer::intValue).sum();

    assertEquals(somaPorTamanho, somaPorCor);
    assertEquals(notaFiscal.getQuantidadeTotal(), somaPorCor);
}
```

**O que verifica:** uma invariante contábil — qualquer que seja a composição do lote, as duas
totalizações precisam fechar no mesmo número. Esse teste pega erros de agregação que os
valores fixos poderiam mascarar.

### 4.7 Objetivo 4 — Código de validação e fluxo ordenado do despacho

```java
@Nested
@DisplayName("Fluxo completo de preparacao, etapa por etapa")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FluxoCompletoOrdenado {

    private Despacho despachoCompartilhado;
    private String codigoGerado;

    @BeforeAll
    void prepararDespachoUnico() {
        despachoCompartilhado = new Despacho(CenarioFabrica.notaFiscalDoLoteCompleto());
    }

    @Test @Order(1)
    @DisplayName("Etapa 1: gerar o codigo de validacao do lote")
    void etapa1_gerarCodigoDeValidacao() {
        codigoGerado = despachoCompartilhado.gerarCodigoValidacao();
        assertAll(
                () -> assertNotNull(codigoGerado),
                () -> assertFalse(despachoCompartilhado.isEmbalado(), "ainda nao foi embalado"),
                () -> assertFalse(despachoCompartilhado.isPreparadoParaDespacho()));
    }

    @Test @Order(3)
    @DisplayName("Etapa 3: marcar o lote como preparado para despacho")
    void etapa3_marcarComoPreparado() {
        despachoCompartilhado.marcarComoPreparadoParaDespacho();
        assertAll(
                () -> assertTrue(despachoCompartilhado.isPreparadoParaDespacho()),
                () -> assertEquals(LocalDate.now(), despachoCompartilhado.getDataPreparacao()),
                () -> assertEquals(codigoGerado, despachoCompartilhado.getCodigoValidacao()));
    }
}
```

**O que verifica:** o despacho é uma máquina de estados (gerar código → embalar → liberar).
`@TestMethodOrder(OrderAnnotation)` com `@Order` executa as etapas na ordem do processo real e
`@TestInstance(PER_CLASS)` mantém o mesmo objeto `Despacho` entre elas, permitindo validar a
transição de estado de uma etapa para a seguinte.

```java
private static final Pattern FORMATO_CODIGO =
        Pattern.compile("^VAL-LOTE-2024-001-NF-0001-\\d{4}$");

@Test
@DisplayName("Deve gerar o codigo de validacao no formato VAL-<lote>-<nota>-<digito>")
void deveGerarCodigoNoFormatoEsperado() {
    String codigo = despacho.gerarCodigoValidacao();

    assertAll("codigo de validacao",
            () -> assertNotNull(codigo),
            () -> assertTrue(FORMATO_CODIGO.matcher(codigo).matches(),
                    () -> "Codigo fora do formato esperado: " + codigo),
            () -> assertTrue(codigo.contains("NF-0001")),
            () -> assertEquals(codigo, despacho.getCodigoValidacao()));
}
```

**O que verifica:** o formato do código de validação via expressão regular (prefixo, número do
lote, número da nota e 4 dígitos verificadores) e a persistência do código dentro do objeto.

### 4.8 Objetivo 4 — Bloqueio de etapas fora de ordem

```java
@Test
@DisplayName("Nao deve liberar para despacho antes de embalar")
void naoDeveLiberarAntesDeEmbalar() {
    despacho.gerarCodigoValidacao();

    DespachoInvalidoException erro = assertThrows(DespachoInvalidoException.class,
            () -> despacho.marcarComoPreparadoParaDespacho());

    assertAll(
            () -> assertEquals("Embale o lote antes de marca-lo como preparado para despacho.",
                    erro.getMessage()),
            () -> assertFalse(despacho.isPreparadoParaDespacho()));
}
```

**O que verifica:** o caminho negativo da máquina de estados. Confirma a mensagem exata e que o
estado do despacho **não** avançou após a recusa.

### 4.9 Desempenho e tempo limite

```java
@Test
@DisplayName("Cadastro de 1.000 itens deve concluir em menos de 2 segundos")
void cadastroDeMuitosItensDeveSerRapido() {
    Lote loteGrande = new Lote("LOTE-CARGA", DATA_FABRICACAO, 1_000);

    assertTimeout(Duration.ofSeconds(2), () -> {
        for (int indice = 1; indice <= 1_000; indice++) {
            loteGrande.adicionarItem(new ItemLote("IT-" + indice, Tamanho.M, Cor.PRETO, 1));
        }
    });
    assertTrue(loteGrande.isCompleto());
}
```

**O que verifica:** a verificação de duplicidade e o recálculo dos totais continuam viáveis em
volume de produção real, protegendo contra regressões de desempenho.

### 4.10 Integração ponta a ponta

```java
@TesteDeIntegracao
@DisplayName("Deve produzir, faturar e despachar um lote de 60 camisetas")
void deveProduzirFaturarEDespacharLoteCompleto() {
    Lote lote = new Lote("LOTE-2024-050", DATA_FABRICACAO, 60)
            .adicionarItem(new ItemLote("IT-01", Tamanho.P, Cor.BRANCO, 10))
            .adicionarItem(new ItemLote("IT-02", Tamanho.M, Cor.PRETO, 20))
            .adicionarItem(new ItemLote("IT-03", Tamanho.G, Cor.AZUL, 30));

    NotaFiscal notaFiscal = new NotaFiscal("NF-2024-050", lote, DATA_FABRICACAO.plusDays(2));
    Despacho despacho = new Despacho(notaFiscal);
    String codigoValidacao = despacho.prepararParaEnvio();

    assertAll("fluxo completo de producao",
            () -> assertTrue(lote.isCompleto()),
            () -> assertEquals(60, notaFiscal.getQuantidadeTotal()),
            () -> assertEquals(10, notaFiscal.getQuantidadePorTamanho(Tamanho.P)),
            () -> assertEquals(30, notaFiscal.getQuantidadePorCor(Cor.AZUL)),
            () -> assertTrue(despacho.isPreparadoParaDespacho()),
            () -> assertTrue(despacho.getEtiquetaEnvio().contains(codigoValidacao)));
}
```

**O que verifica:** a colaboração entre as quatro classes. Os testes unitários provam cada peça
isoladamente; este prova que, encadeadas, elas entregam o resultado de negócio esperado.

---

## 5. Anotações JUnit utilizadas

| Anotação | Papel | Onde está |
|---|---|---|
| `@Test` | marca um método de teste | todas as suítes |
| `@DisplayName` | nome legível no relatório web | todas as classes e métodos |
| `@BeforeAll` / `@AfterAll` | preparação e encerramento únicos da suíte | `LoteTest`, `ItemLoteTest`, `NotaFiscalTest`, `DespachoTest` |
| `@BeforeEach` / `@AfterEach` | cenário recriado e descartado a cada teste | `LoteTest`, `ItemLoteTest`, `NotaFiscalTest`, `DespachoTest` |
| `@Nested` | agrupa testes por comportamento | 11 grupos (ex.: `TotalizacaoPorCor`, `FluxoForaDeOrdem`) |
| `@Disabled` | desativa um teste (equivale ao `@Ignore` do JUnit 4) | `ItemLoteTest.deveCalcularValorTotalDoItem` |
| `@Tag` | categoriza para execução seletiva | `dominio`, `fiscal`, `logistica`, `integracao` |
| `@ParameterizedTest` | mesmo teste com vários dados | 16 métodos |
| `@ValueSource` | valores simples | quantidades e totais inválidos |
| `@CsvSource` | múltiplas colunas por caso | conversão de tamanho/cor, totais por tamanho |
| `@EnumSource` | varre todas as constantes do enum | `TamanhoTest`, `CorTest`, `NotaFiscalTest` |
| `@NullAndEmptySource` | cobre `null` e `""` | validação de código, número e cor |
| `@MethodSource` | fábrica de argumentos complexos | `ExcecoesDeNegocioTest.excecoesDoDominio` |
| `@RepeatedTest` + `RepetitionInfo` | repete variando a carga | `LoteTest.deveAcumularQuantidadeEmCadastrosSucessivos` |
| `@TestMethodOrder` + `@Order` | executa etapas na ordem do processo | `DespachoTest.FluxoCompletoOrdenado` |
| `@TestInstance(PER_CLASS)` | compartilha estado entre etapas ordenadas | `DespachoTest.FluxoCompletoOrdenado` |
| **`@TesteDeDominio`** | **anotação personalizada** = `@Test` + `@Tag("dominio")` | `suporte/TesteDeDominio.java` |
| **`@TesteDeIntegracao`** | **anotação personalizada** = `@Test` + `@Tag("integracao")` | `suporte/TesteDeIntegracao.java` |
| `@Before` / `@After` (Cucumber) | hooks de início e fim de cenário | `PassosComuns` |
| `@Suite` / `@IncludeEngines` | runner do engine Cucumber | `ExecutaCenariosBddTest` |

---

## 6. Assertivas utilizadas

| Assertiva | Uso no projeto |
|---|---|
| `assertEquals` | igualdade de números, datas, textos e enums |
| `assertNotEquals` | lotes e itens com identificadores diferentes |
| `assertTrue` / `assertFalse` | estados booleanos (`isCompleto`, `isEmbalado`, `isPreparadoParaDespacho`) |
| `assertNull` / `assertNotNull` | código de validação antes e depois de ser gerado |
| `assertSame` / `assertNotSame` | identidade de enums e nova visão da lista de itens |
| `assertAll` | agrupa assertivas relacionadas e reporta todas as falhas juntas |
| `assertThrows` | **exceções personalizadas** (`CapacidadeLoteExcedidaException`, `ItemDuplicadoException`, `NotaFiscalInvalidaException`, `DespachoInvalidoException`, `DadoInvalidoException`) |
| `assertDoesNotThrow` | casos de fronteira válidos (quantidade 1, fabricação hoje) |
| `assertInstanceOf` | hierarquia das exceções de negócio |
| `assertIterableEquals` | ordem de cadastro dos itens do lote |
| `assertArrayEquals` | catálogo completo de tamanhos |
| `assertTimeout` | cadastro de 1.000 itens em menos de 2 s |
| Mensagens `Supplier<String>` | diagnóstico construído apenas quando o teste falha |

---

## 7. Estrutura BDD (Cucumber)

### 7.1 Organização

- **5 arquivos `.feature`** em Gherkin **português** (`# language: pt`), com `Funcionalidade`,
  `Contexto`, `Cenario`, `Esquema do Cenario` + `Exemplos`, `Regra` e tags
  (`@lote`, `@nota_fiscal`, `@despacho`, `@regra_negocio`, `@fluxo_completo`).
- **4 classes de passos** por responsabilidade, mais `PassosComuns` com os hooks e as
  verificações de recusa.
- **`ContextoProducao`** é injetado pelo PicoContainer em todas as classes de passos: cada
  cenário recebe uma instância nova, o que garante isolamento sem estado estático.

### 7.2 Fragmento de feature — Given-When-Then

```gherkin
# language: pt
@nota_fiscal
Funcionalidade: Emissao da nota fiscal do lote
  Para faturar a producao
  Como analista fiscal da Camisetas S.A.
  Quero emitir a nota fiscal do lote com o total de camisetas por tamanho e por cor

  Contexto:
    Dado que a empresa abriu o lote "LOTE-2024-001" fabricado em "2024-05-10" com capacidade para 100 camisetas
    E que o lote possui os seguintes itens cadastrados:
      | codigo | tamanho | cor    | quantidade |
      | IT-01  | P       | Branco | 20         |
      | IT-02  | P       | Preto  | 10         |
      | IT-03  | M       | Branco | 30         |
      | IT-04  | M       | Azul   | 15         |
      | IT-05  | G       | Preto  | 20         |
      | IT-06  | G       | Azul   | 5          |

  @totalizacao
  Cenario: Contabilizar a quantidade de camisetas por tamanho
    Quando o faturamento emite a nota fiscal "NF-0001" em "2024-05-11"
    Entao a nota fiscal deve detalhar as quantidades por tamanho:
      | tamanho | quantidade |
      | P       | 30         |
      | M       | 45         |
      | G       | 25         |
```

**Leitura do cenário:** o `Contexto` (**Dado**) estabelece o estado inicial — lote aberto e
itens produzidos. O **Quando** executa uma única ação de negócio: emitir a nota. O **Então**
verifica o resultado observável, usando uma tabela que é a própria especificação do total
esperado por tamanho. A linguagem é a do negócio ("faturamento", "nota fiscal", "camisetas"),
sem referência a classes ou métodos.

### 7.3 Fragmento de feature — regra de negócio recusada

```gherkin
  @regra_negocio
  Cenario: Recusar cadastro acima da capacidade do lote
    Quando a producao cadastra o item "IT-01" de tamanho "P" e cor "Branco" com 90 camisetas
    E a producao tenta cadastrar o item "IT-02" de tamanho "G" e cor "Azul" com 20 camisetas
    Entao a recusa deve ser do tipo "CapacidadeLoteExcedidaException"
    E a operacao deve ser recusada com a mensagem "Capacidade do lote LOTE-2024-001 excedida. Total permitido: 100, solicitado: 110"
    E o lote deve ter 90 camisetas cadastradas
```

O verbo "tenta" sinaliza, na própria linguagem do cenário, que a operação pode falhar — é o
passo que captura a exceção no `ContextoProducao`. O último **Então** confirma que o lote
permaneceu consistente depois da recusa.

### 7.4 Fragmento de step definition

```java
public class PassosNotaFiscal {

    private final ContextoProducao contexto;

    /** O PicoContainer injeta o contexto compartilhado do cenário. */
    public PassosNotaFiscal(ContextoProducao contexto) {
        this.contexto = contexto;
    }

    @Quando("o faturamento emite a nota fiscal {string} em {string}")
    public void oFaturamentoEmiteANotaFiscal(String numero, String dataEmissao) {
        contexto.setNotaFiscal(
                new NotaFiscal(numero, contexto.getLote(), LocalDate.parse(dataEmissao)));
    }

    @Entao("a nota fiscal deve detalhar as quantidades por tamanho:")
    public void aNotaFiscalDeveDetalharPorTamanho(DataTable tabela) {
        for (Map<String, String> linha : tabela.asMaps()) {
            Tamanho tamanho = Tamanho.deSigla(linha.get("tamanho"));
            int esperado = Integer.parseInt(linha.get("quantidade"));
            assertEquals(esperado, contexto.getNotaFiscal().getQuantidadePorTamanho(tamanho),
                    () -> "Divergencia no tamanho " + tamanho);
        }
    }
}
```

Os passos usam as anotações em português (`@Dado`, `@Quando`, `@Entao`) do pacote
`io.cucumber.java.pt`, Cucumber Expressions (`{string}`, `{int}`) e `DataTable` para as
tabelas. As assertivas são as mesmas do JUnit 5, então uma falha de cenário aparece com o
mesmo diagnóstico de um teste unitário.

### 7.5 Cenários por funcionalidade

| Funcionalidade | Arquivo | Cenários |
|---|---|---|
| Abertura do lote e cadastro dos itens | `lote.feature` | 13 |
| Emissão da nota fiscal do lote | `nota_fiscal.feature` | 13 |
| Restrição fiscal para lote sem produção | `nota_fiscal_lote_vazio.feature` | 1 |
| Preparação do lote para despacho | `despacho.feature` | 8 |
| Fluxo completo de produção | `fluxo_producao.feature` | 2 |
| **Total** | | **37** |

---

## 8. Cobertura dos testes (Coverage)

Medida pelo JaCoCo 0.8.12 em `mvn clean verify`. O build **falha** se a cobertura cair abaixo
de 95% de instruções/linhas ou 90% de desvios (regra `jacoco-check-cobertura-minima` no `pom.xml`).

| Pacote | Classes | Instruções | Desvios | Linhas | Métodos |
|---|---|---|---|---|---|
| `br.com.camisetas.dominio` | 4 | 100% | 100% | 105 / 105 | 36 / 36 |
| `br.com.camisetas.fiscal` | 1 | 100% | 100% | 66 / 66 | 14 / 14 |
| `br.com.camisetas.logistica` | 1 | 100% | 100% | 47 / 47 | 14 / 14 |
| `br.com.camisetas.excecao` | 6 | 100% | n/a | 18 / 18 | 9 / 9 |
| **Total** | **12** | **100%** (1.152) | **100%** (100) | **236 / 236** | **73 / 73** |

Como a cobertura de 100% foi alcançada: além dos caminhos de sucesso, cada validação de
entrada tem um teste de recusa (`assertThrows`), cada `equals`/`hashCode`/`toString` tem teste
próprio, os enums são varridos com `@EnumSource` e a hierarquia de exceções tem suíte dedicada
(`ExcecoesDeNegocioTest`). A cobertura de **desvios em 100%** indica que os dois lados de todo
`if` do código de produção foram exercitados.

> Cobertura alta não é garantia de qualidade por si só: ela mostra o que foi *executado*, não o
> que foi *verificado*. Por isso cada teste aqui carrega assertivas explícitas sobre o
> comportamento esperado, e não apenas chamadas aos métodos.

---

## 9. Relatórios web

Abra `relatorios.html` (raiz do projeto) para o painel com os indicadores e os links. Direto:

1. **`target/site/surefire-report.html`** — resultado de cada teste JUnit e de cada cenário BDD,
   agrupado por classe, com tempos e o teste ignorado destacado.
2. **`target/site/jacoco/index.html`** — cobertura navegável por pacote → classe → método, com o
   código-fonte colorido (verde = coberto, amarelo = desvio parcial, vermelho = não coberto).
3. **`target/cucumber-reports/relatorio-cucumber.html`** — relatório do Cucumber com as
   funcionalidades, cenários em Gherkin e o status passo a passo.

Também são gerados `target/cucumber-reports/cucumber.json` e
`target/cucumber-reports/cucumber-junit.xml` para integração com ferramentas de CI.

---

## 10. Logs de execução no terminal

A execução imprime o andamento com marcadores visuais, o que torna a conferência ao vivo
imediata — sem precisar abrir relatório para saber se passou.

Três peças sustentam isso:

| Peça | Arquivo | Papel |
|---|---|---|
| `RelatorioConsoleListener` | `suporte/RelatorioConsoleListener.java` | `TestExecutionListener` do JUnit Platform: imprime cada teste e o resumo final |
| `Simbolos` | `suporte/Simbolos.java` | detecta a codificação do terminal e troca emoji por `[OK]`/`[X]` quando não há suporte |
| Hooks Cucumber | `bdd/passos/PassosComuns.java` | `@Before`/`@After` medindo a duração de cada cenário |

O listener é registrado automaticamente por `ServiceLoader`, pelo arquivo
`src/test/resources/META-INF/services/org.junit.platform.launcher.TestExecutionListener`,
então vale igualmente para os testes JUnit e para os cenários Cucumber.

### 10.1 Execução bem-sucedida

```text
==============================================================================
  CAMISETAS S.A. - EXECUCAO DOS TESTES (JUnit 5 + Cucumber)
==============================================================================
  ▶️ Lote: abertura do lote e cadastro de itens de producao
  🔧 @BeforeAll - preparando a suite LoteTest
    ✅ Deve receber numero, data de fabricacao e total de itens
    ✅ Lote recem-aberto deve estar vazio e incompleto
    ▶️ Cadastro de itens no lote
      ✅ Deve adicionar um item e atualizar os totais do lote
      ✅ Deve rejeitar cadastro que excede a capacidade do lote
  🔧 @AfterAll - suite LoteTest encerrada

  ▶️ Preparacao do lote para despacho
        🥒 cenario ✅ em 27 ms [PASSED]
        ✅ Gerar o codigo de validacao do lote
        🥒 cenario ✅ em 5 ms [PASSED]
        ✅ Embalar o lote depois de gerar o codigo de validacao

==============================================================================
  📊 RESUMO DA EXECUCAO
==============================================================================
  ✅ Aprovados ....... 181
  ❌ Reprovados ...... 0
  ⏭️ Ignorados ....... 1
  ⏱️ Tempo total ..... 2,2 s
  📊 Total ........... 182
==============================================================================
  ✅ TODOS OS TESTES PASSARAM
==============================================================================
```

### 10.2 Execução com falha

A linha reprovada já traz o tipo da exceção e a mensagem da assertiva, sem precisar
procurar o stack trace:

```text
  ▶️ Demonstracao de falha
    ❌ Teste que falha de proposito
        -> AssertionFailedError: o total do lote deveria ser 100 ==> expected: <100> but was: <42>
    ✅ Teste que passa

==============================================================================
  📊 RESUMO DA EXECUCAO
==============================================================================
  ✅ Aprovados ....... 1
  ❌ Reprovados ...... 1
==============================================================================
  ❌ 1 TESTE(S) REPROVADO(S) - veja os detalhes acima
==============================================================================
```

### 10.3 Codificação do terminal e os marcadores

O console do Windows em português usa a **página de código 850**, que não contém emoji. Ao
imprimir `✅` nesse terminal, o Windows troca o caractere por `?` — e um log cheio de `?` é
pior do que um log sem emoji nenhum.

Por isso o conjunto de marcadores é escolhido pela propriedade `testes.marcadores`:

| Valor | Efeito |
|---|---|
| `auto` (padrão) | só usa emoji quando há console interativo capaz de representá-los; em qualquer dúvida, ASCII |
| `emoji` | força `✅` / `❌` — exige terminal em UTF-8 |
| `ascii` | força `[OK]` / `[X]` |

Na prática:

```powershell
mvn -o test                              # [OK] / [X]  - funciona em qualquer terminal
.\demo.ps1                               # ✅ / ❌      - o script troca o terminal para UTF-8
chcp 65001; mvn -o test -Dtestes.marcadores=emoji   # equivalente, na mão
```

A detecção automática é deliberadamente conservadora: o Surefire executa os testes em uma JVM
filha cuja saída vai para um *pipe*, então de dentro dela não há como saber a página de código
do terminal de verdade. Entre arriscar `?` e garantir `[OK]`, o padrão garante `[OK]`.

Duas observações relacionadas:

- O `pom.xml` define `-Dfile.encoding=UTF-8` na propriedade `argLine` (à qual o
  `jacoco:prepare-agent` acrescenta o seu `-javaagent`), para que os emoji saiam como bytes
  UTF-8 válidos quando forem solicitados.
- O plugin `pretty` do Cucumber foi retirado do console para que os marcadores não se perdessem
  no meio do texto Gherkin. Para reativá-lo, acrescente `pretty,` em `cucumber.plugin`, em
  `src/test/resources/junit-platform.properties`.

---

## 11. Pacote de entrega

```powershell
.\gerar-entrega.ps1              # build completo + empacotamento
.\gerar-entrega.ps1 -PularBuild  # reaproveita o target/ atual
.\gerar-entrega.ps1 -SemEmoji    # marcadores ASCII, para terminais antigos
```

O script roda `mvn clean verify site`, copia os relatórios para caminhos estáveis, reescreve os
links do painel, empacota o código-fonte, **confere item por item** e só então gera
`camisetas-sa-tdd-bdd-junit.zip`. Se qualquer etapa falhar, ele aborta sem produzir pacote.

```text
  ▶ Etapa 5/5: Conferindo o conteudo e gerando o pacote final
     ✅ Painel de navegacao                              index.html
     ✅ Documentacao da parte pratica                    RELATORIO.md
     ✅ Relatorio de execucao dos testes                 relatorios\site\surefire-report.html
     ✅ Relatorio de cobertura (JaCoCo)                  relatorios\site\jacoco\index.html
     ✅ Relatorio dos cenarios BDD                       relatorios\cucumber\relatorio-cucumber.html
     ✅ Codigo-fonte do projeto                          camisetas-sa-codigo-fonte.zip
     ✅ Todos os links do painel resolvem para arquivos existentes
     ✅ Pacote gerado: camisetas-sa-tdd-bdd-junit.zip
```

Estrutura gerada em `entrega/`:

```
entrega/
├── index.html                      painel com indicadores e links
├── RELATORIO.md                    esta documentação
├── camisetas-sa-codigo-fonte.zip   pom.xml + src/ completo
└── relatorios/
    ├── site/                       Surefire Report + JaCoCo + CSS
    └── cucumber/                   HTML + cucumber.json + cucumber-junit.xml
```

O motivo de copiar os relatórios: eles são gerados em `target/`, que é descartável e está no
`.gitignore`. Zipar apenas o projeto entregaria um painel com todos os links quebrados.

---

## 12. Roteiro de apresentação

A apresentação segue a ordem dos critérios de avaliação, e não a ordem do código, para que
cada item possa ser pontuado sem precisar procurar.

| Bloco | Critério avaliado | Tempo | O que mostrar |
|---|---|---|---|
| Abertura | — | 1 min | `relatorios.html`: 182 testes, 0 falhas, 100% de cobertura |
| 1. Cenário e estrutura | Organização e Estrutura dos Testes | 2 min | árvore de pastas: 4 classes do enunciado e os testes espelhando os pacotes |
| 2. Anotações JUnit | Uso de Anotações JUnit | 3 min | `LoteTest` e `DespachoTest` (seções 4.1 a 4.3 e 4.7) |
| 3. Assertivas | Assertivas | 3 min | seções 4.3, 4.6 e 4.9 |
| 4. BDD | Estrutura BDD | 3 min | `nota_fiscal.feature` ao lado de `PassosNotaFiscal` (seções 7.2 a 7.4) |
| 5. Execução e cobertura | Cobertura dos Testes | 2 min | `mvn -o test` ao vivo + JaCoCo navegando até uma classe |
| 6. Relatórios | Entrega do Relatório | 1 min | os três relatórios a partir do painel |

### 12.1 Pontos que decidem a nota

- **Bloco 2:** citar explicitamente a anotação personalizada `@TesteDeDominio`
  (`suporte/TesteDeDominio.java`) — é o que diferencia o nível máximo do critério.
- **Bloco 3:** mostrar `assertThrows` sobre **exceções do próprio domínio** e a inspeção dos
  dados de contexto (`getCapacidade()`, `getQuantidadeSolicitada()`), não apenas o tipo da
  exceção.
- **Bloco 4:** ler um cenário em voz alta. O Gherkin em português se explica sozinho, e é
  exatamente isso que o critério avalia.
- **Bloco 5:** entrar em uma classe no JaCoCo e mostrar o código verde linha a linha. O número
  100% convence menos que o fonte colorido.

### 12.2 Demonstração ao vivo

```powershell
mvn -o test        # ~6 s, imprime os logs com ✅ e o resumo final
```

Preparação, na ordem:

1. Rodar `mvn -o clean verify site` **antes** da apresentação, para popular `target/` e deixar
   os relatórios prontos.
2. Abrir as abas: `relatorios.html`, JaCoCo, Surefire Report e Cucumber.
3. Aumentar a fonte do terminal e rodar `cls` antes da demo.
4. Deixar abertos na IDE: `LoteTest`, `DespachoTest`, `NotaFiscalTest`,
   `nota_fiscal.feature`, `PassosNotaFiscal` e `pom.xml`.

Plano B: se o build ao vivo falhar, os relatórios já estão abertos nas abas e a apresentação
continua sem interrupção. Nunca apresente dependendo de um build que ainda não rodou naquela
máquina — em máquina sem `~/.m2` populado e sem internet, nada compila (ver seção 1).

### 12.3 Divisão entre três apresentadores

| Pessoa | Blocos |
|---|---|
| 1 | Abertura, estrutura e anotações JUnit |
| 2 | Assertivas e BDD |
| 3 | Execução ao vivo, cobertura e relatórios |

Quem apresenta o bloco 5 deve ser quem rodou o build antes — é quem sabe reagir se algo falhar.

### 12.4 Perguntas prováveis

**"Por que JUnit 5, se o enunciado cita `@BeforeClass` e `@Ignore`?"**
Essas anotações são do JUnit 4. Os equivalentes no JUnit 5 são `@BeforeAll` e `@Disabled`, e
ambos estão no projeto (`ItemLoteTest`).

**"Onde está o TDD, se o código já existe?"**
Explicar o ciclo Red-Green-Refactor com um caso concreto: o teste
`deveRejeitarCadastroQueExcedeCapacidade` foi escrito esperando a exceção (red), depois veio a
validação em `Lote.adicionarItem` (green) e por fim a extração de `getQuantidadeCadastrada()`
(refactor).

**"Como chegaram a 100% de cobertura?"**
Toda validação de entrada tem teste de recusa, `equals`/`hashCode`/`toString` têm teste
próprio, os enums são varridos com `@EnumSource` e a hierarquia de exceções tem suíte dedicada
(`ExcecoesDeNegocioTest`). A cobertura de desvios em 100% indica que os dois lados de cada
`if` foram exercitados.

**"Qual a diferença entre os testes JUnit e os cenários Cucumber aqui?"**
JUnit verifica unidade e contrato técnico; Cucumber descreve comportamento de negócio na
linguagem do cliente. Os 37 cenários cobrem os mesmos 4 objetivos por outra lente.

**"Cobertura de 100% significa que não há bugs?"**
Não. Cobertura mostra o que foi executado, não o que foi verificado. Por isso cada teste do
projeto carrega assertivas explícitas de comportamento, e não apenas chamadas aos métodos.
