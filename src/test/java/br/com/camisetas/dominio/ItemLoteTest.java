package br.com.camisetas.dominio;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import br.com.camisetas.excecao.DadoInvalidoException;
import br.com.camisetas.suporte.Simbolos;
import br.com.camisetas.suporte.TesteDeDominio;

/**
 * Testes da classe {@link ItemLote}.
 *
 * <p>Objetivo (item 2 do enunciado): garantir que o item do lote é cadastrado
 * corretamente com as informações de tamanho e cor, rejeitando dados inválidos.</p>
 */
@Tag("dominio")
@DisplayName("ItemLote: cadastro de itens do lote com tamanho e cor")
class ItemLoteTest {

    private static int itensCriados;

    private ItemLote item;

    @BeforeAll
    static void prepararSuite() {
        itensCriados = 0;
        System.out.printf("  %s @BeforeAll - preparando a suite ItemLoteTest%n", Simbolos.PREPARO);
    }

    @AfterAll
    static void encerrarSuite() {
        System.out.printf("  %s @AfterAll - suite ItemLoteTest encerrada (%d itens criados)%n",
                Simbolos.PREPARO, itensCriados);
    }

    @BeforeEach
    void cadastrarItemPadrao() {
        // Cenario comum a varios testes: 1 item, tamanho M, cor Azul, 10 unidades
        item = new ItemLote("IT-001", Tamanho.M, Cor.AZUL, 10);
        itensCriados++;
    }

    @AfterEach
    void limparCenario() {
        item = null;
    }

    @TesteDeDominio
    @DisplayName("Deve cadastrar o item com tamanho, cor e quantidade informados")
    void deveCadastrarItemComTamanhoCorEQuantidade() {
        // assertAll avalia todas as assertivas e reporta todas as falhas de uma vez
        assertAll("item cadastrado",
                () -> assertEquals("IT-001", item.getCodigo()),
                () -> assertEquals(Tamanho.M, item.getTamanho()),
                () -> assertEquals(Cor.AZUL, item.getCor()),
                () -> assertEquals(10, item.getQuantidade()));
    }

    @Test
    @DisplayName("Deve normalizar o codigo removendo espacos e aplicando maiusculas")
    void deveNormalizarCodigoDoItem() {
        ItemLote itemComEspacos = new ItemLote("  it-099 ", Tamanho.P, Cor.PRETO, 5);
        assertEquals("IT-099", itemComEspacos.getCodigo());
    }

    @Test
    @DisplayName("Construtor simplificado deve assumir quantidade igual a 1")
    void construtorSimplificadoDeveAssumirQuantidadeUm() {
        ItemLote unitario = new ItemLote("IT-002", Tamanho.G, Cor.BRANCO);
        assertAll(
                () -> assertEquals(1, unitario.getQuantidade()),
                () -> assertEquals(Tamanho.G, unitario.getTamanho()),
                () -> assertEquals(Cor.BRANCO, unitario.getCor()));
    }

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

    @Test
    @DisplayName("Deve montar a descricao comercial no formato 'Camiseta <TAM> <Cor>'")
    void deveMontarDescricaoComercial() {
        assertEquals("Camiseta M Azul", item.getDescricao());
    }

    @Nested
    @DisplayName("Validacoes de dados obrigatorios")
    class ValidacoesDeDadosObrigatorios {

        @ParameterizedTest(name = "[{index}] codigo nulo/vazio deve ser rejeitado")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("Deve rejeitar codigo nulo, vazio ou em branco")
        void deveRejeitarCodigoInvalido(String codigoInvalido) {
            DadoInvalidoException erro = assertThrows(DadoInvalidoException.class,
                    () -> new ItemLote(codigoInvalido, Tamanho.P, Cor.AZUL, 1));
            assertEquals("Codigo do item e obrigatorio.", erro.getMessage());
        }

        @Test
        @DisplayName("Deve rejeitar tamanho nulo")
        void deveRejeitarTamanhoNulo() {
            DadoInvalidoException erro = assertThrows(DadoInvalidoException.class,
                    () -> new ItemLote("IT-003", null, Cor.AZUL, 1));
            assertEquals("Tamanho do item e obrigatorio.", erro.getMessage());
        }

        @Test
        @DisplayName("Deve rejeitar cor nula")
        void deveRejeitarCorNula() {
            DadoInvalidoException erro = assertThrows(DadoInvalidoException.class,
                    () -> new ItemLote("IT-004", Tamanho.P, null, 1));
            assertEquals("Cor do item e obrigatoria.", erro.getMessage());
        }

        @ParameterizedTest(name = "[{index}] quantidade {0} deve ser rejeitada")
        @ValueSource(ints = {0, -1, -100})
        @DisplayName("Deve rejeitar quantidade menor ou igual a zero")
        void deveRejeitarQuantidadeNaoPositiva(int quantidadeInvalida) {
            DadoInvalidoException erro = assertThrows(DadoInvalidoException.class,
                    () -> new ItemLote("IT-005", Tamanho.P, Cor.AZUL, quantidadeInvalida));
            assertTrue(erro.getMessage().contains("maior que zero"),
                    () -> "Mensagem inesperada: " + erro.getMessage());
        }

        @Test
        @DisplayName("Quantidade igual a 1 e o menor valor aceito")
        void quantidadeUmDeveSerAceita() {
            assertDoesNotThrow(() -> new ItemLote("IT-006", Tamanho.P, Cor.AZUL, 1));
        }
    }

    @Nested
    @DisplayName("Identidade do item (equals, hashCode e toString)")
    class IdentidadeDoItem {

        @Test
        @DisplayName("Itens com o mesmo codigo devem ser considerados iguais")
        void itensComMesmoCodigoDevemSerIguais() {
            ItemLote mesmoCodigo = new ItemLote("it-001", Tamanho.G, Cor.BRANCO, 99);
            assertAll(
                    () -> assertEquals(item, mesmoCodigo, "codigo igual => itens iguais"),
                    () -> assertEquals(item.hashCode(), mesmoCodigo.hashCode(),
                            "hashCode deve acompanhar equals"));
        }

        @Test
        @DisplayName("Itens com codigos diferentes nao devem ser iguais")
        void itensComCodigosDiferentesNaoDevemSerIguais() {
            assertNotEquals(item, new ItemLote("IT-002", Tamanho.M, Cor.AZUL, 10));
        }

        @Test
        @DisplayName("equals deve ser reflexivo e seguro contra null e outros tipos")
        void equalsDeveSerReflexivoESeguro() {
            assertAll(
                    () -> assertEquals(item, item, "reflexividade"),
                    () -> assertNotEquals(null, item, "comparacao com null"),
                    () -> assertFalse(item.equals("IT-001"), "comparacao com outro tipo"));
        }

        @Test
        @DisplayName("toString deve expor os dados do item para log e depuracao")
        void toStringDeveExporDadosDoItem() {
            String texto = item.toString();
            assertAll(
                    () -> assertNotNull(texto),
                    () -> assertTrue(texto.contains("IT-001")),
                    () -> assertTrue(texto.contains("M")),
                    () -> assertTrue(texto.contains("AZUL")),
                    () -> assertTrue(texto.contains("10")));
        }
    }

    @Test
    @Disabled("Regra de preco por item ainda nao foi especificada pelo cliente (equivale ao @Ignore do JUnit 4)")
    @DisplayName("Deve calcular o valor total do item")
    void deveCalcularValorTotalDoItem() {
        throw new UnsupportedOperationException("Aguardando definicao da tabela de precos.");
    }
}
