package br.com.camisetas.dominio;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;

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
 * Testes do enum {@link Cor} — garante que somente Branco, Preto e Azul
 * são aceitos como cor de camiseta.
 */
@Tag("dominio")
@DisplayName("Cor: dominio de cores da camiseta (Branco, Preto, Azul)")
class CorTest {

    @Test
    @DisplayName("Deve expor exatamente as cores Branco, Preto e Azul")
    void deveExporExatamenteAsCoresDoCatalogo() {
        List<Cor> esperadas = List.of(Cor.BRANCO, Cor.PRETO, Cor.AZUL);
        assertIterableEquals(esperadas, Arrays.asList(Cor.values()));
    }

    @ParameterizedTest(name = "[{index}] cor {0} possui descricao valida")
    @EnumSource(Cor.class)
    @DisplayName("Toda cor deve ter descricao preenchida e iniciada em maiuscula")
    void todaCorDeveTerDescricaoValida(Cor cor) {
        assertAll("atributos da cor " + cor,
                () -> assertNotNull(cor.getDescricao()),
                () -> assertFalse(cor.getDescricao().isBlank()),
                () -> assertEquals(cor.name(), cor.getDescricao().toUpperCase()));
    }

    @ParameterizedTest(name = "[{index}] deDescricao(\"{0}\") deve retornar {1}")
    @CsvSource({
            "Branco, BRANCO",
            "preto, PRETO",
            "' azul ', AZUL"
    })
    @DisplayName("Deve converter descricao textual em enum ignorando caixa e espacos")
    void deveConverterDescricaoTextualEmEnum(String entrada, Cor esperada) {
        assertSame(esperada, Cor.deDescricao(entrada));
    }

    @ParameterizedTest(name = "[{index}] deDescricao(\"{0}\") deve falhar")
    @ValueSource(strings = {"Verde", "Vermelho", "G", "  "})
    @DisplayName("Deve rejeitar cores fora do catalogo")
    void deveRejeitarCoresForaDoCatalogo(String corInvalida) {
        DadoInvalidoException erro =
                assertThrows(DadoInvalidoException.class, () -> Cor.deDescricao(corInvalida));
        assertNotNull(erro.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Deve rejeitar cor nula ou vazia com mensagem de obrigatoriedade")
    void deveRejeitarCorNulaOuVazia(String corInvalida) {
        DadoInvalidoException erro =
                assertThrows(DadoInvalidoException.class, () -> Cor.deDescricao(corInvalida));
        assertEquals("Descricao da cor e obrigatoria.", erro.getMessage());
    }

    @Test
    @DisplayName("valueOf deve recuperar a constante pelo nome exato")
    void valueOfDeveRecuperarConstantePeloNome() {
        assertSame(Cor.AZUL, Cor.valueOf("AZUL"));
    }
}
