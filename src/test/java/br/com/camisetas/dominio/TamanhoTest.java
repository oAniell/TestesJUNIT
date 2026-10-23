package br.com.camisetas.dominio;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import br.com.camisetas.excecao.DadoInvalidoException;

/**
 * Testes do enum {@link Tamanho} — garante que somente P, M e G são aceitos
 * como tamanho de camiseta.
 */
@Tag("dominio")
@DisplayName("Tamanho: dominio de tamanhos da camiseta (P, M, G)")
class TamanhoTest {

    @Test
    @DisplayName("Deve expor exatamente os tamanhos P, M e G")
    void deveExporExatamenteOsTamanhosPMG() {
        // assertArrayEquals confirma, de uma vez, conteudo e ordem do dominio
        assertArrayEquals(new Tamanho[] {Tamanho.P, Tamanho.M, Tamanho.G}, Tamanho.values());
        assertEquals(3, Tamanho.values().length, "O catalogo deve ter 3 tamanhos");
    }

    @ParameterizedTest(name = "[{index}] tamanho {0} possui sigla e descricao validas")
    @EnumSource(Tamanho.class)
    @DisplayName("Todo tamanho deve ter sigla igual ao nome e descricao preenchida")
    void todoTamanhoDeveTerSiglaEDescricao(Tamanho tamanho) {
        assertAll("atributos do tamanho " + tamanho,
                () -> assertNotNull(tamanho.getDescricao(), "descricao nao pode ser nula"),
                () -> assertFalse(tamanho.getDescricao().isBlank(), "descricao nao pode ser vazia"),
                () -> assertEquals(tamanho.name(), tamanho.getSigla(), "sigla deve ser o proprio nome"));
    }

    @ParameterizedTest(name = "[{index}] deSigla(\"{0}\") deve retornar {1}")
    @CsvSource({
            "P, P",
            "m, M",
            "' g ', G",
            "G, G"
    })
    @DisplayName("Deve converter sigla textual em enum ignorando caixa e espacos")
    void deveConverterSiglaTextualEmEnum(String entrada, Tamanho esperado) {
        // assertSame reforca que enum e comparado por identidade (singleton)
        assertSame(esperado, Tamanho.deSigla(entrada));
    }

    @ParameterizedTest(name = "[{index}] deSigla(\"{0}\") deve falhar")
    @ValueSource(strings = {"XG", "PP", "1", "branco", "   "})
    @DisplayName("Deve rejeitar siglas fora do dominio")
    void deveRejeitarSiglasForaDoDominio(String siglaInvalida) {
        DadoInvalidoException erro =
                assertThrows(DadoInvalidoException.class, () -> Tamanho.deSigla(siglaInvalida));
        assertNotNull(erro.getMessage());
    }

    @ParameterizedTest(name = "[{index}] deSigla de valor nulo/vazio deve falhar")
    @NullAndEmptySource
    @DisplayName("Deve rejeitar sigla nula ou vazia com mensagem de obrigatoriedade")
    void deveRejeitarSiglaNulaOuVazia(String siglaInvalida) {
        DadoInvalidoException erro =
                assertThrows(DadoInvalidoException.class, () -> Tamanho.deSigla(siglaInvalida));
        assertEquals("Sigla do tamanho e obrigatoria.", erro.getMessage());
    }

    @Test
    @DisplayName("valueOf deve recuperar a constante pelo nome exato")
    void valueOfDeveRecuperarConstantePeloNome() {
        assertSame(Tamanho.M, Tamanho.valueOf("M"));
        assertThrows(IllegalArgumentException.class, () -> Tamanho.valueOf("m"));
    }
}
