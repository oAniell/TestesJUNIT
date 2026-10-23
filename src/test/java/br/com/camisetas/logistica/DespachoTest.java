package br.com.camisetas.logistica;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import br.com.camisetas.dominio.Lote;
import br.com.camisetas.excecao.DespachoInvalidoException;
import br.com.camisetas.fiscal.NotaFiscal;
import br.com.camisetas.suporte.CenarioFabrica;
import br.com.camisetas.suporte.Simbolos;
import br.com.camisetas.suporte.TesteDeDominio;

/**
 * Testes da classe {@link Despacho}.
 *
 * <p>Objetivo (item 4 do enunciado): validar que o despacho gera o código de
 * validação do lote, embala o volume e marca o lote como preparado para despacho,
 * sempre respeitando a ordem das etapas.</p>
 */
@Tag("logistica")
@DisplayName("Despacho: codigo de validacao, embalagem e liberacao do lote")
class DespachoTest {

    /** Formato esperado: VAL-<numeroLote>-<numeroNota>-<4 digitos>. */
    private static final Pattern FORMATO_CODIGO =
            Pattern.compile("^VAL-LOTE-2024-001-NF-0001-\\d{4}$");

    private NotaFiscal notaFiscal;
    private Despacho despacho;

    @BeforeAll
    static void informarInicioDaSuite() {
        System.out.printf("  %s @BeforeAll - preparando a suite DespachoTest%n", Simbolos.PREPARO);
    }

    @AfterAll
    static void informarFimDaSuite() {
        System.out.printf("  %s @AfterAll - suite DespachoTest encerrada%n", Simbolos.PREPARO);
    }

    @BeforeEach
    void prepararDespachoDoLoteFaturado() {
        notaFiscal = CenarioFabrica.notaFiscalDoLoteCompleto();
        despacho = new Despacho(notaFiscal);
    }

    @TesteDeDominio
    @DisplayName("Despacho recem-criado deve estar sem codigo, sem embalagem e nao liberado")
    void despachoRecemCriadoDeveEstarPendente() {
        assertAll("estado inicial do despacho",
                () -> assertNull(despacho.getCodigoValidacao(), "codigo so existe apos gerar"),
                () -> assertFalse(despacho.isEmbalado()),
                () -> assertFalse(despacho.isPreparadoParaDespacho()),
                () -> assertNull(despacho.getDataPreparacao()),
                () -> assertSame(notaFiscal, despacho.getNotaFiscal()),
                () -> assertSame(notaFiscal.getLote(), despacho.getLote()));
    }

    @Test
    @DisplayName("Nao deve criar despacho sem nota fiscal")
    void naoDeveCriarDespachoSemNotaFiscal() {
        DespachoInvalidoException erro =
                assertThrows(DespachoInvalidoException.class, () -> new Despacho(null));
        assertEquals("Nota fiscal e obrigatoria para preparar o despacho.", erro.getMessage());
    }

    @Nested
    @DisplayName("Geracao do codigo de validacao")
    class GeracaoDoCodigoDeValidacao {

        @Test
        @DisplayName("Deve gerar o codigo de validacao no formato VAL-<lote>-<nota>-<digito>")
        void deveGerarCodigoNoFormatoEsperado() {
            String codigo = despacho.gerarCodigoValidacao();

            assertAll("codigo de validacao",
                    () -> assertNotNull(codigo),
                    () -> assertTrue(FORMATO_CODIGO.matcher(codigo).matches(),
                            () -> "Codigo fora do formato esperado: " + codigo),
                    () -> assertTrue(codigo.startsWith("VAL-")),
                    () -> assertTrue(codigo.contains(CenarioFabrica.NUMERO_LOTE_PADRAO)),
                    () -> assertTrue(codigo.contains("NF-0001")),
                    () -> assertEquals(codigo, despacho.getCodigoValidacao(),
                            "o codigo gerado deve ficar guardado no despacho"));
        }

        @Test
        @DisplayName("O codigo deve ser deterministico para o mesmo lote e a mesma nota")
        void codigoDeveSerDeterministico() {
            String primeiro = despacho.gerarCodigoValidacao();
            String segundo = new Despacho(CenarioFabrica.notaFiscalDoLoteCompleto()).gerarCodigoValidacao();

            assertEquals(primeiro, segundo, "lotes e notas identicas devem gerar o mesmo codigo");
        }

        @Test
        @DisplayName("Lotes diferentes devem gerar codigos diferentes")
        void lotesDiferentesDevemGerarCodigosDiferentes() {
            Lote outroLote = new Lote("LOTE-2024-777", CenarioFabrica.DATA_FABRICACAO, 10)
                    .adicionarItem(CenarioFabrica.itemPadrao());
            Despacho outroDespacho =
                    new Despacho(new NotaFiscal("NF-0777", outroLote, LocalDate.now()));

            assertTrue(despacho.gerarCodigoValidacao().compareTo(outroDespacho.gerarCodigoValidacao()) != 0,
                    "codigos de lotes distintos nao devem coincidir");
        }

        @Test
        @DisplayName("Nao deve gerar o codigo duas vezes para o mesmo despacho")
        void naoDeveGerarCodigoDuasVezes() {
            despacho.gerarCodigoValidacao();

            DespachoInvalidoException erro =
                    assertThrows(DespachoInvalidoException.class, () -> despacho.gerarCodigoValidacao());
            assertTrue(erro.getMessage().contains("ja foi gerado"));
        }
    }

    @Nested
    @DisplayName("Embalagem e liberacao fora de ordem")
    class FluxoForaDeOrdem {

        @Test
        @DisplayName("Nao deve embalar antes de gerar o codigo de validacao")
        void naoDeveEmbalarAntesDeGerarCodigo() {
            DespachoInvalidoException erro =
                    assertThrows(DespachoInvalidoException.class, () -> despacho.embalar());

            assertAll(
                    () -> assertEquals("Gere o codigo de validacao antes de embalar o lote.", erro.getMessage()),
                    () -> assertFalse(despacho.isEmbalado()));
        }

        @Test
        @DisplayName("Nao deve embalar o mesmo lote duas vezes")
        void naoDeveEmbalarDuasVezes() {
            despacho.gerarCodigoValidacao();
            despacho.embalar();

            DespachoInvalidoException erro =
                    assertThrows(DespachoInvalidoException.class, () -> despacho.embalar());
            assertTrue(erro.getMessage().contains("ja esta embalado"));
        }

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

        @Test
        @DisplayName("Nao deve liberar o mesmo lote duas vezes")
        void naoDeveLiberarDuasVezes() {
            despacho.prepararParaEnvio();

            DespachoInvalidoException erro = assertThrows(DespachoInvalidoException.class,
                    () -> despacho.marcarComoPreparadoParaDespacho());
            assertTrue(erro.getMessage().contains("ja esta preparado para despacho"));
        }

        @Test
        @DisplayName("Nao deve emitir etiqueta antes da liberacao do lote")
        void naoDeveEmitirEtiquetaAntesDaLiberacao() {
            DespachoInvalidoException erro =
                    assertThrows(DespachoInvalidoException.class, () -> despacho.getEtiquetaEnvio());
            assertEquals("Lote ainda nao esta preparado para despacho.", erro.getMessage());
        }
    }

    @Nested
    @DisplayName("Fluxo completo de preparacao, etapa por etapa")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class FluxoCompletoOrdenado {

        private Despacho despachoCompartilhado;
        private String codigoGerado;

        @BeforeAll
        void prepararDespachoUnico() {
            // PER_CLASS permite compartilhar o mesmo despacho entre as etapas ordenadas
            despachoCompartilhado = new Despacho(CenarioFabrica.notaFiscalDoLoteCompleto());
        }

        @Test
        @Order(1)
        @DisplayName("Etapa 1: gerar o codigo de validacao do lote")
        void etapa1_gerarCodigoDeValidacao() {
            codigoGerado = despachoCompartilhado.gerarCodigoValidacao();

            assertAll(
                    () -> assertNotNull(codigoGerado),
                    () -> assertFalse(despachoCompartilhado.isEmbalado(), "ainda nao foi embalado"),
                    () -> assertFalse(despachoCompartilhado.isPreparadoParaDespacho()));
        }

        @Test
        @Order(2)
        @DisplayName("Etapa 2: embalar o lote")
        void etapa2_embalarLote() {
            despachoCompartilhado.embalar();

            assertAll(
                    () -> assertTrue(despachoCompartilhado.isEmbalado()),
                    () -> assertFalse(despachoCompartilhado.isPreparadoParaDespacho(),
                            "embalar nao libera o lote"));
        }

        @Test
        @Order(3)
        @DisplayName("Etapa 3: marcar o lote como preparado para despacho")
        void etapa3_marcarComoPreparado() {
            despachoCompartilhado.marcarComoPreparadoParaDespacho();

            assertAll(
                    () -> assertTrue(despachoCompartilhado.isPreparadoParaDespacho()),
                    () -> assertEquals(LocalDate.now(), despachoCompartilhado.getDataPreparacao()),
                    () -> assertEquals(codigoGerado, despachoCompartilhado.getCodigoValidacao()));
        }

        @Test
        @Order(4)
        @DisplayName("Etapa 4: emitir a etiqueta de envio do volume")
        void etapa4_emitirEtiquetaDeEnvio() {
            String etiqueta = despachoCompartilhado.getEtiquetaEnvio();

            assertAll("conteudo da etiqueta",
                    () -> assertTrue(etiqueta.startsWith("ENVIO |")),
                    () -> assertTrue(etiqueta.contains(CenarioFabrica.NUMERO_LOTE_PADRAO)),
                    () -> assertTrue(etiqueta.contains("NF-0001")),
                    () -> assertTrue(etiqueta.contains("100 camisetas")),
                    () -> assertTrue(etiqueta.contains(codigoGerado)));
        }
    }

    @Nested
    @DisplayName("Atalho prepararParaEnvio")
    class AtalhoPrepararParaEnvio {

        @Test
        @DisplayName("Deve executar as tres etapas em uma unica chamada")
        void deveExecutarAsTresEtapasDeUmaVez() {
            String codigo = despacho.prepararParaEnvio();

            assertAll("lote pronto para envio",
                    () -> assertNotNull(codigo),
                    () -> assertEquals(codigo, despacho.getCodigoValidacao()),
                    () -> assertTrue(despacho.isEmbalado()),
                    () -> assertTrue(despacho.isPreparadoParaDespacho()),
                    () -> assertEquals(LocalDate.now(), despacho.getDataPreparacao()));
        }

        @Test
        @DisplayName("toString deve expor o estado atual do despacho")
        void toStringDeveExporEstadoAtual() {
            String antes = despacho.toString();
            despacho.prepararParaEnvio();
            String depois = despacho.toString();

            assertAll(
                    () -> assertTrue(antes.contains("codigoValidacao='null'")),
                    () -> assertTrue(antes.contains("embalado=false")),
                    () -> assertTrue(depois.contains("embalado=true")),
                    () -> assertTrue(depois.contains("preparadoParaDespacho=true")),
                    () -> assertTrue(depois.contains(CenarioFabrica.NUMERO_LOTE_PADRAO)));
        }
    }
}
