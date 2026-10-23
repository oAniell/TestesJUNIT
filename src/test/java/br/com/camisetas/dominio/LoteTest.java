package br.com.camisetas.dominio;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import br.com.camisetas.excecao.CamisetasException;
import br.com.camisetas.excecao.CapacidadeLoteExcedidaException;
import br.com.camisetas.excecao.DadoInvalidoException;
import br.com.camisetas.excecao.ItemDuplicadoException;
import br.com.camisetas.suporte.CenarioFabrica;
import br.com.camisetas.suporte.Simbolos;
import br.com.camisetas.suporte.TesteDeDominio;

/**
 * Testes da classe {@link Lote}.
 *
 * <p>Objetivo (item 1 do enunciado): verificar se o lote recebe corretamente o
 * número, a data de fabricação e o total de itens, e se adiciona itens
 * respeitando as regras de capacidade e de não duplicidade.</p>
 */
@Tag("dominio")
@DisplayName("Lote: abertura do lote e cadastro de itens de producao")
class LoteTest {

    private static final LocalDate DATA_FABRICACAO = LocalDate.of(2024, 5, 10);
    private static final String NUMERO_LOTE = "LOTE-2024-001";

    private Lote lote;

    @BeforeAll
    static void informarInicioDaSuite() {
        System.out.printf("  %s @BeforeAll - preparando a suite LoteTest%n", Simbolos.PREPARO);
    }

    @AfterAll
    static void informarFimDaSuite() {
        System.out.printf("  %s @AfterAll - suite LoteTest encerrada%n", Simbolos.PREPARO);
    }

    @BeforeEach
    void abrirLoteComCapacidadeDeCem() {
        lote = new Lote(NUMERO_LOTE, DATA_FABRICACAO, 100);
    }

    @AfterEach
    void descartarLote() {
        lote = null;
    }

    // ------------------------------------------------------------------
    // Abertura do lote
    // ------------------------------------------------------------------

    @TesteDeDominio
    @DisplayName("Deve receber numero, data de fabricacao e total de itens")
    void deveReceberNumeroDataFabricacaoETotalDeItens() {
        assertAll("dados de abertura do lote",
                () -> assertEquals(NUMERO_LOTE, lote.getNumero()),
                () -> assertEquals(DATA_FABRICACAO, lote.getDataFabricacao()),
                () -> assertEquals(100, lote.getTotalItens()));
    }

    @Test
    @DisplayName("Lote recem-aberto deve estar vazio e incompleto")
    void loteRecemAbertoDeveEstarVazio() {
        assertAll("estado inicial",
                () -> assertTrue(lote.isVazio(), "lote novo nao tem itens"),
                () -> assertFalse(lote.isCompleto(), "lote novo nao pode estar completo"),
                () -> assertEquals(0, lote.getQuantidadeDeItens()),
                () -> assertEquals(0, lote.getQuantidadeCadastrada()),
                () -> assertEquals(100, lote.getQuantidadeRestante()),
                () -> assertTrue(lote.getItens().isEmpty()));
    }

    @Test
    @DisplayName("Deve remover espacos em branco do numero do lote")
    void deveRemoverEspacosDoNumeroDoLote() {
        assertEquals("LOTE-2024-002", new Lote("  LOTE-2024-002  ", DATA_FABRICACAO, 10).getNumero());
    }

    @Test
    @DisplayName("Deve aceitar lote fabricado na data de hoje")
    void deveAceitarLoteFabricadoHoje() {
        assertDoesNotThrow(() -> new Lote("LOTE-HOJE", LocalDate.now(), 1));
    }

    @Nested
    @DisplayName("Validacoes na abertura do lote")
    class ValidacoesDeAbertura {

        @ParameterizedTest(name = "[{index}] numero nulo/vazio deve ser rejeitado")
        @NullAndEmptySource
        @ValueSource(strings = {"  "})
        @DisplayName("Deve rejeitar numero de lote nulo, vazio ou em branco")
        void deveRejeitarNumeroInvalido(String numeroInvalido) {
            DadoInvalidoException erro = assertThrows(DadoInvalidoException.class,
                    () -> new Lote(numeroInvalido, DATA_FABRICACAO, 10));
            assertEquals("Numero do lote e obrigatorio.", erro.getMessage());
        }

        @Test
        @DisplayName("Deve rejeitar data de fabricacao nula")
        void deveRejeitarDataDeFabricacaoNula() {
            DadoInvalidoException erro = assertThrows(DadoInvalidoException.class,
                    () -> new Lote(NUMERO_LOTE, null, 10));
            assertEquals("Data de fabricacao e obrigatoria.", erro.getMessage());
        }

        @Test
        @DisplayName("Deve rejeitar data de fabricacao futura")
        void deveRejeitarDataDeFabricacaoFutura() {
            LocalDate amanha = LocalDate.now().plusDays(1);
            DadoInvalidoException erro = assertThrows(DadoInvalidoException.class,
                    () -> new Lote(NUMERO_LOTE, amanha, 10));
            assertTrue(erro.getMessage().startsWith("Data de fabricacao nao pode ser futura"));
        }

        @ParameterizedTest(name = "[{index}] total de itens {0} deve ser rejeitado")
        @ValueSource(ints = {0, -1, -250})
        @DisplayName("Deve rejeitar total de itens menor ou igual a zero")
        void deveRejeitarTotalDeItensNaoPositivo(int totalInvalido) {
            DadoInvalidoException erro = assertThrows(DadoInvalidoException.class,
                    () -> new Lote(NUMERO_LOTE, DATA_FABRICACAO, totalInvalido));
            assertTrue(erro.getMessage().contains("maior que zero"));
        }
    }

    // ------------------------------------------------------------------
    // Cadastro de itens
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Cadastro de itens no lote")
    class CadastroDeItens {

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

        @Test
        @DisplayName("Deve permitir encadear o cadastro de varios itens")
        void devePermitirEncadearCadastroDeItens() {
            Lote retorno = lote
                    .adicionarItem(new ItemLote("IT-01", Tamanho.P, Cor.BRANCO, 10))
                    .adicionarItem(new ItemLote("IT-02", Tamanho.M, Cor.PRETO, 20))
                    .adicionarItem(new ItemLote("IT-03", Tamanho.G, Cor.AZUL, 30));

            assertAll(
                    () -> assertSame(lote, retorno, "adicionarItem deve devolver o proprio lote"),
                    () -> assertEquals(3, lote.getQuantidadeDeItens()),
                    () -> assertEquals(60, lote.getQuantidadeCadastrada()));
        }

        @Test
        @DisplayName("Deve preservar a ordem de cadastro dos itens")
        void devePreservarOrdemDeCadastro() {
            ItemLote primeiro = new ItemLote("IT-01", Tamanho.P, Cor.BRANCO, 10);
            ItemLote segundo = new ItemLote("IT-02", Tamanho.M, Cor.PRETO, 10);
            lote.adicionarItem(primeiro).adicionarItem(segundo);

            assertIterableEquals(List.of(primeiro, segundo), lote.getItens());
        }

        @Test
        @DisplayName("Lote deve ficar completo quando a quantidade atinge o total planejado")
        void loteDeveFicarCompletoAoAtingirOTotal() {
            lote.adicionarItem(new ItemLote("IT-01", Tamanho.P, Cor.BRANCO, 60))
                    .adicionarItem(new ItemLote("IT-02", Tamanho.G, Cor.AZUL, 40));

            assertAll(
                    () -> assertTrue(lote.isCompleto()),
                    () -> assertEquals(0, lote.getQuantidadeRestante()),
                    () -> assertEquals(lote.getTotalItens(), lote.getQuantidadeCadastrada()));
        }

        @RepeatedTest(value = 5, name = "Cadastro sequencial - repeticao {currentRepetition} de {totalRepetitions}")
        @DisplayName("Deve acumular corretamente a quantidade em cadastros sucessivos")
        void deveAcumularQuantidadeEmCadastrosSucessivos(RepetitionInfo info) {
            for (int indice = 1; indice <= info.getCurrentRepetition(); indice++) {
                lote.adicionarItem(new ItemLote("IT-" + indice, Tamanho.M, Cor.PRETO, 2));
            }
            assertEquals(info.getCurrentRepetition() * 2, lote.getQuantidadeCadastrada());
        }

        @Test
        @DisplayName("Deve rejeitar item nulo")
        void deveRejeitarItemNulo() {
            DadoInvalidoException erro =
                    assertThrows(DadoInvalidoException.class, () -> lote.adicionarItem(null));
            assertEquals("Item do lote nao pode ser nulo.", erro.getMessage());
        }

        @Test
        @DisplayName("Deve rejeitar dois itens com o mesmo codigo")
        void deveRejeitarItemDuplicado() {
            lote.adicionarItem(new ItemLote("IT-01", Tamanho.P, Cor.BRANCO, 10));

            ItemDuplicadoException erro = assertThrows(ItemDuplicadoException.class,
                    () -> lote.adicionarItem(new ItemLote("IT-01", Tamanho.G, Cor.AZUL, 5)));

            assertAll(
                    () -> assertTrue(erro.getMessage().contains("IT-01")),
                    () -> assertEquals(1, lote.getQuantidadeDeItens(), "o item duplicado nao entra no lote"),
                    () -> assertEquals(10, lote.getQuantidadeCadastrada()));
        }

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

        @Test
        @DisplayName("Excecoes do dominio devem derivar de CamisetasException")
        void excecoesDoDominioDevemDerivarDaExcecaoBase() {
            assertAll("hierarquia de excecoes",
                    () -> assertThrows(CamisetasException.class, () -> lote.adicionarItem(null)),
                    () -> assertThrows(CamisetasException.class,
                            () -> new Lote("LOTE-X", DATA_FABRICACAO, 0)));
        }
    }

    // ------------------------------------------------------------------
    // Remocao de itens e protecao do estado interno
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Remocao de itens e encapsulamento")
    class RemocaoEEncapsulamento {

        @Test
        @DisplayName("Deve remover o item pelo codigo, ignorando a caixa")
        void deveRemoverItemPeloCodigo() {
            lote.adicionarItem(new ItemLote("IT-01", Tamanho.P, Cor.BRANCO, 10));

            assertAll(
                    () -> assertTrue(lote.removerItem(" it-01 "), "deve remover ignorando caixa/espacos"),
                    () -> assertTrue(lote.isVazio()),
                    () -> assertEquals(0, lote.getQuantidadeCadastrada()));
        }

        @Test
        @DisplayName("Remocao de codigo inexistente deve retornar false")
        void remocaoDeCodigoInexistenteDeveRetornarFalse() {
            assertFalse(lote.removerItem("IT-NAO-EXISTE"));
        }

        @ParameterizedTest(name = "[{index}] remover codigo nulo/vazio deve falhar")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("Deve exigir codigo valido para remocao")
        void deveExigirCodigoValidoParaRemocao(String codigoInvalido) {
            DadoInvalidoException erro =
                    assertThrows(DadoInvalidoException.class, () -> lote.removerItem(codigoInvalido));
            assertEquals("Codigo do item e obrigatorio para remocao.", erro.getMessage());
        }

        @Test
        @DisplayName("A lista retornada por getItens deve ser imutavel")
        void listaDeItensDeveSerImutavel() {
            lote.adicionarItem(CenarioFabrica.itemPadrao());
            List<ItemLote> itens = lote.getItens();

            assertThrows(UnsupportedOperationException.class,
                    () -> itens.add(new ItemLote("IT-INVASOR", Tamanho.G, Cor.PRETO, 1)));
        }

        @Test
        @DisplayName("getItens deve devolver uma visao nova a cada chamada, mas com o mesmo conteudo")
        void getItensDeveDevolverVisaoConsistente() {
            lote.adicionarItem(CenarioFabrica.itemPadrao());

            assertAll(
                    () -> assertNotSame(lote.getItens(), lote.getItens()),
                    () -> assertIterableEquals(lote.getItens(), lote.getItens()));
        }
    }

    // ------------------------------------------------------------------
    // Identidade e desempenho
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Identidade do lote e desempenho do cadastro")
    class IdentidadeEDesempenho {

        @Test
        @DisplayName("Lotes com o mesmo numero devem ser iguais")
        void lotesComMesmoNumeroDevemSerIguais() {
            Lote mesmoNumero = new Lote(NUMERO_LOTE, DATA_FABRICACAO.minusDays(5), 999);
            assertAll(
                    () -> assertEquals(lote, mesmoNumero),
                    () -> assertEquals(lote.hashCode(), mesmoNumero.hashCode()),
                    () -> assertEquals(lote, lote));
        }

        @Test
        @DisplayName("Lotes com numeros diferentes nao devem ser iguais")
        void lotesComNumerosDiferentesNaoDevemSerIguais() {
            assertAll(
                    () -> assertNotEquals(lote, new Lote("LOTE-2024-999", DATA_FABRICACAO, 100)),
                    () -> assertNotEquals(null, lote),
                    () -> assertFalse(lote.equals(NUMERO_LOTE)));
        }

        @Test
        @DisplayName("toString deve expor numero, data e contagem de itens")
        void toStringDeveExporDadosDoLote() {
            lote.adicionarItem(CenarioFabrica.itemPadrao());
            String texto = lote.toString();

            assertAll(
                    () -> assertNotNull(texto),
                    () -> assertTrue(texto.contains(NUMERO_LOTE)),
                    () -> assertTrue(texto.contains("2024-05-10")),
                    () -> assertTrue(texto.contains("itensCadastrados=1")));
        }

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
    }
}
