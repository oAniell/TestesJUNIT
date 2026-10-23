package br.com.camisetas.excecao;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Testes da hierarquia de exceções personalizadas do domínio.
 *
 * <p>Garantem que toda recusa de regra de negócio pode ser capturada por
 * {@link CamisetasException} e que as mensagens e os dados de contexto
 * das exceções são preservados.</p>
 */
@Tag("dominio")
@DisplayName("Excecoes de negocio: hierarquia, mensagens e dados de contexto")
class ExcecoesDeNegocioTest {

    /** Fornece uma instância de cada exceção do domínio para o teste parametrizado. */
    static Stream<Arguments> excecoesDoDominio() {
        return Stream.of(
                Arguments.of("DadoInvalidoException", new DadoInvalidoException("dado invalido")),
                Arguments.of("ItemDuplicadoException", new ItemDuplicadoException("item duplicado")),
                Arguments.of("NotaFiscalInvalidaException", new NotaFiscalInvalidaException("nota invalida")),
                Arguments.of("DespachoInvalidoException", new DespachoInvalidoException("despacho invalido")),
                Arguments.of("CapacidadeLoteExcedidaException",
                        new CapacidadeLoteExcedidaException("capacidade excedida", 10, 15)));
    }

    @ParameterizedTest(name = "[{index}] {0} deve derivar de CamisetasException")
    @MethodSource("excecoesDoDominio")
    @DisplayName("Toda excecao do dominio deve derivar da excecao base e ser nao checada")
    void todaExcecaoDeveDerivarDaExcecaoBase(String nome, CamisetasException excecao) {
        assertAll("hierarquia de " + nome,
                () -> assertInstanceOf(CamisetasException.class, excecao),
                () -> assertInstanceOf(RuntimeException.class, excecao),
                () -> assertEquals(nome, excecao.getClass().getSimpleName()),
                () -> assertNull(excecao.getCause(), "sem causa encadeada"));
    }

    @Test
    @DisplayName("Excecao base deve preservar a mensagem e a causa encadeada")
    void excecaoBaseDevePreservarMensagemECausa() {
        IllegalStateException causaOriginal = new IllegalStateException("falha de infraestrutura");
        CamisetasException excecao = new CamisetasException("falha ao processar o lote", causaOriginal);

        assertAll(
                () -> assertEquals("falha ao processar o lote", excecao.getMessage()),
                () -> assertSame(causaOriginal, excecao.getCause()));
    }

    @Test
    @DisplayName("Excecao de capacidade deve expor capacidade e quantidade solicitada")
    void excecaoDeCapacidadeDeveExporDadosDeContexto() {
        CapacidadeLoteExcedidaException excecao =
                new CapacidadeLoteExcedidaException("capacidade excedida", 100, 130);

        assertAll(
                () -> assertEquals("capacidade excedida", excecao.getMessage()),
                () -> assertEquals(100, excecao.getCapacidade()),
                () -> assertEquals(130, excecao.getQuantidadeSolicitada()));
    }

    @Test
    @DisplayName("Lancar a excecao base deve ser capturavel pelo tipo raiz")
    void lancarExcecaoBaseDeveSerCapturavelPeloTipoRaiz() {
        CamisetasException erro = assertThrows(CamisetasException.class, () -> {
            throw new CamisetasException("erro generico de negocio");
        });
        assertEquals("erro generico de negocio", erro.getMessage());
    }
}
