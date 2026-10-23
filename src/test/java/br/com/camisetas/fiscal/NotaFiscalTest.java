package br.com.camisetas.fiscal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import br.com.camisetas.dominio.Cor;
import br.com.camisetas.dominio.ItemLote;
import br.com.camisetas.dominio.Lote;
import br.com.camisetas.dominio.Tamanho;
import br.com.camisetas.excecao.NotaFiscalInvalidaException;
import br.com.camisetas.suporte.CenarioFabrica;
import br.com.camisetas.suporte.Simbolos;
import br.com.camisetas.suporte.TesteDeDominio;

/**
 * Testes da classe {@link NotaFiscal}.
 *
 * <p>Objetivo (item 3 do enunciado): confirmar que a nota fiscal é emitida
 * corretamente para o lote e que contabiliza a quantidade de camisetas por
 * tamanho e por cor.</p>
 *
 * <p>Massa de teste usada (lote de 100 camisetas):
 * P = 30, M = 45, G = 25 | Branco = 50, Preto = 30, Azul = 20.</p>
 */
@Tag("fiscal")
@DisplayName("NotaFiscal: emissao e totalizacao por tamanho e por cor")
class NotaFiscalTest {

    private static final LocalDate DATA_EMISSAO = CenarioFabrica.DATA_FABRICACAO.plusDays(1);

    private Lote loteCompleto;
    private NotaFiscal notaFiscal;

    @BeforeAll
    static void informarInicioDaSuite() {
        System.out.printf("  %s @BeforeAll - preparando a suite NotaFiscalTest%n", Simbolos.PREPARO);
    }

    @AfterAll
    static void informarFimDaSuite() {
        System.out.printf("  %s @AfterAll - suite NotaFiscalTest encerrada%n", Simbolos.PREPARO);
    }

    @BeforeEach
    void emitirNotaFiscalDoLoteCompleto() {
        loteCompleto = CenarioFabrica.loteCompleto();
        notaFiscal = new NotaFiscal("NF-0001", loteCompleto, DATA_EMISSAO);
    }

    @TesteDeDominio
    @DisplayName("Deve emitir a nota com numero, lote e data de emissao")
    void deveEmitirNotaComNumeroLoteEData() {
        assertAll("dados da nota fiscal",
                () -> assertEquals("NF-0001", notaFiscal.getNumero()),
                () -> assertSame(loteCompleto, notaFiscal.getLote()),
                () -> assertEquals(DATA_EMISSAO, notaFiscal.getDataEmissao()));
    }

    @Test
    @DisplayName("Deve contabilizar a quantidade total de camisetas do lote")
    void deveContabilizarQuantidadeTotal() {
        assertEquals(100, notaFiscal.getQuantidadeTotal());
        assertEquals(loteCompleto.getQuantidadeCadastrada(), notaFiscal.getQuantidadeTotal());
    }

    @Nested
    @DisplayName("Totalizacao por tamanho")
    class TotalizacaoPorTamanho {

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

        @ParameterizedTest(name = "[{index}] tamanho {0} deve totalizar {1} camisetas")
        @CsvSource({"P, 30", "M, 45", "G, 25"})
        @DisplayName("Consulta pontual por tamanho deve devolver o total correto")
        void consultaPontualPorTamanho(Tamanho tamanho, int quantidadeEsperada) {
            assertEquals(quantidadeEsperada, notaFiscal.getQuantidadePorTamanho(tamanho));
        }

        @Test
        @DisplayName("Tamanho nao produzido deve aparecer na nota com quantidade zero")
        void tamanhoNaoProduzidoDeveAparecerComZero() {
            Lote somenteP = CenarioFabrica.loteVazio(10)
                    .adicionarItem(new ItemLote("IT-01", Tamanho.P, Cor.BRANCO, 10));
            NotaFiscal nota = new NotaFiscal("NF-0002", somenteP, DATA_EMISSAO);

            assertAll(
                    () -> assertEquals(10, nota.getQuantidadePorTamanho(Tamanho.P)),
                    () -> assertEquals(0, nota.getQuantidadePorTamanho(Tamanho.M)),
                    () -> assertEquals(0, nota.getQuantidadePorTamanho(Tamanho.G)));
        }

        @Test
        @DisplayName("Consulta com tamanho nulo deve ser rejeitada")
        void consultaComTamanhoNuloDeveSerRejeitada() {
            NotaFiscalInvalidaException erro = assertThrows(NotaFiscalInvalidaException.class,
                    () -> notaFiscal.getQuantidadePorTamanho(null));
            assertEquals("Tamanho e obrigatorio para consulta.", erro.getMessage());
        }

        @Test
        @DisplayName("O mapa por tamanho deve ser imutavel")
        void mapaPorTamanhoDeveSerImutavel() {
            assertThrows(UnsupportedOperationException.class,
                    () -> notaFiscal.getQuantidadePorTamanho().put(Tamanho.P, 999));
        }
    }

    @Nested
    @DisplayName("Totalizacao por cor")
    class TotalizacaoPorCor {

        @Test
        @DisplayName("Deve somar as quantidades agrupadas por cor")
        void deveSomarQuantidadesPorCor() {
            Map<Cor, Integer> porCor = notaFiscal.getQuantidadePorCor();

            assertAll("quantidades por cor",
                    () -> assertEquals(3, porCor.size(), "todas as cores devem constar na nota"),
                    () -> assertEquals(50, porCor.get(Cor.BRANCO), "Branco = 20 P + 30 M"),
                    () -> assertEquals(30, porCor.get(Cor.PRETO), "Preto = 10 P + 20 G"),
                    () -> assertEquals(20, porCor.get(Cor.AZUL), "Azul = 15 M + 5 G"));
        }

        @ParameterizedTest(name = "[{index}] cor {0} deve totalizar {1} camisetas")
        @CsvSource({"BRANCO, 50", "PRETO, 30", "AZUL, 20"})
        @DisplayName("Consulta pontual por cor deve devolver o total correto")
        void consultaPontualPorCor(Cor cor, int quantidadeEsperada) {
            assertEquals(quantidadeEsperada, notaFiscal.getQuantidadePorCor(cor));
        }

        @ParameterizedTest(name = "[{index}] cor {0} deve constar na nota")
        @EnumSource(Cor.class)
        @DisplayName("Todas as cores do catalogo devem constar na nota emitida")
        void todasAsCoresDevemConstarNaNota(Cor cor) {
            assertNotNull(notaFiscal.getQuantidadePorCor().get(cor));
        }

        @Test
        @DisplayName("Consulta com cor nula deve ser rejeitada")
        void consultaComCorNulaDeveSerRejeitada() {
            NotaFiscalInvalidaException erro = assertThrows(NotaFiscalInvalidaException.class,
                    () -> notaFiscal.getQuantidadePorCor(null));
            assertEquals("Cor e obrigatoria para consulta.", erro.getMessage());
        }

        @Test
        @DisplayName("O mapa por cor deve ser imutavel")
        void mapaPorCorDeveSerImutavel() {
            assertThrows(UnsupportedOperationException.class,
                    () -> notaFiscal.getQuantidadePorCor().put(Cor.AZUL, 999));
        }

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
    }

    @Nested
    @DisplayName("Regras de emissao")
    class RegrasDeEmissao {

        @Test
        @DisplayName("Fabrica emitirPara deve derivar o numero do lote e usar a data de hoje")
        void fabricaEmitirParaDeveDerivarNumeroEData() {
            NotaFiscal nota = NotaFiscal.emitirPara(CenarioFabrica.loteCompleto());

            assertAll(
                    () -> assertEquals("NF-" + CenarioFabrica.NUMERO_LOTE_PADRAO, nota.getNumero()),
                    () -> assertEquals(LocalDate.now(), nota.getDataEmissao()),
                    () -> assertEquals(100, nota.getQuantidadeTotal()));
        }

        @Test
        @DisplayName("Nao deve emitir nota para lote nulo")
        void naoDeveEmitirNotaParaLoteNulo() {
            assertAll(
                    () -> assertEquals("Lote e obrigatorio para emissao da nota fiscal.",
                            assertThrows(NotaFiscalInvalidaException.class,
                                    () -> new NotaFiscal("NF-0003", null, DATA_EMISSAO)).getMessage()),
                    () -> assertEquals("Lote e obrigatorio para emissao da nota fiscal.",
                            assertThrows(NotaFiscalInvalidaException.class,
                                    () -> NotaFiscal.emitirPara(null)).getMessage()));
        }

        @Test
        @DisplayName("Nao deve emitir nota para lote sem itens")
        void naoDeveEmitirNotaParaLoteSemItens() {
            NotaFiscalInvalidaException erro = assertThrows(NotaFiscalInvalidaException.class,
                    () -> new NotaFiscal("NF-0004", CenarioFabrica.loteVazio(), DATA_EMISSAO));
            assertTrue(erro.getMessage().contains("sem itens"));
        }

        @ParameterizedTest(name = "[{index}] numero da nota nulo/vazio deve ser rejeitado")
        @NullAndEmptySource
        @DisplayName("Nao deve emitir nota sem numero")
        void naoDeveEmitirNotaSemNumero(String numeroInvalido) {
            NotaFiscalInvalidaException erro = assertThrows(NotaFiscalInvalidaException.class,
                    () -> new NotaFiscal(numeroInvalido, loteCompleto, DATA_EMISSAO));
            assertEquals("Numero da nota fiscal e obrigatorio.", erro.getMessage());
        }

        @Test
        @DisplayName("Nao deve emitir nota sem data de emissao")
        void naoDeveEmitirNotaSemDataDeEmissao() {
            NotaFiscalInvalidaException erro = assertThrows(NotaFiscalInvalidaException.class,
                    () -> new NotaFiscal("NF-0005", loteCompleto, null));
            assertEquals("Data de emissao e obrigatoria.", erro.getMessage());
        }

        @Test
        @DisplayName("Nao deve emitir nota com data anterior a fabricacao do lote")
        void naoDeveEmitirNotaAnteriorAFabricacao() {
            LocalDate dataInvalida = CenarioFabrica.DATA_FABRICACAO.minusDays(1);

            NotaFiscalInvalidaException erro = assertThrows(NotaFiscalInvalidaException.class,
                    () -> new NotaFiscal("NF-0006", loteCompleto, dataInvalida));
            assertTrue(erro.getMessage().contains("nao pode ser anterior a fabricacao"));
        }

        @Test
        @DisplayName("Deve aceitar emissao no mesmo dia da fabricacao")
        void deveAceitarEmissaoNoMesmoDiaDaFabricacao() {
            NotaFiscal nota = new NotaFiscal("NF-0007", loteCompleto, CenarioFabrica.DATA_FABRICACAO);
            assertEquals(CenarioFabrica.DATA_FABRICACAO, nota.getDataEmissao());
        }

        @Test
        @DisplayName("Deve remover espacos do numero informado")
        void deveRemoverEspacosDoNumero() {
            assertEquals("NF-0008", new NotaFiscal("  NF-0008 ", loteCompleto, DATA_EMISSAO).getNumero());
        }
    }

    @Nested
    @DisplayName("Apresentacao da nota")
    class ApresentacaoDaNota {

        @Test
        @DisplayName("detalhar deve listar totais por tamanho e por cor")
        void detalharDeveListarTotais() {
            String detalhe = notaFiscal.detalhar();

            assertAll("conteudo do detalhamento",
                    () -> assertTrue(detalhe.contains("NOTA FISCAL NF-0001")),
                    () -> assertTrue(detalhe.contains(CenarioFabrica.NUMERO_LOTE_PADRAO)),
                    () -> assertTrue(detalhe.contains("Total 100 camisetas")),
                    () -> assertTrue(detalhe.contains("Tamanho P: 30")),
                    () -> assertTrue(detalhe.contains("Tamanho M: 45")),
                    () -> assertTrue(detalhe.contains("Tamanho G: 25")),
                    () -> assertTrue(detalhe.contains("Cor Branco: 50")),
                    () -> assertTrue(detalhe.contains("Cor Preto: 30")),
                    () -> assertTrue(detalhe.contains("Cor Azul: 20")));
        }

        @Test
        @DisplayName("toString deve resumir numero, lote, data e total")
        void toStringDeveResumirANota() {
            String texto = notaFiscal.toString();

            assertAll(
                    () -> assertTrue(texto.contains("NF-0001")),
                    () -> assertTrue(texto.contains(CenarioFabrica.NUMERO_LOTE_PADRAO)),
                    () -> assertTrue(texto.contains("quantidadeTotal=100")));
        }
    }
}
