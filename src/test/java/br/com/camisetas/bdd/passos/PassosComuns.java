package br.com.camisetas.bdd.passos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.pt.Entao;

import br.com.camisetas.excecao.CamisetasException;
import br.com.camisetas.suporte.Simbolos;

/**
 * Passos comuns a todas as funcionalidades: verificação das recusas de operação
 * e ganchos ({@code hooks}) de início e fim de cenário.
 */
public class PassosComuns {

    private final ContextoProducao contexto;

    private Instant inicioDoCenario;

    public PassosComuns(ContextoProducao contexto) {
        this.contexto = contexto;
    }

    /** Marca o início do cenário para medir a duração. */
    @Before
    public void registrarInicioDoCenario() {
        inicioDoCenario = Instant.now();
    }

    /**
     * Registra no console o status e a duração do cenário encerrado.
     *
     * <p>O nome do cenário é impresso pelo {@code RelatorioConsoleListener}; aqui
     * fica só a informação que o listener não tem (tempo de execução), evitando
     * duplicar o texto no terminal.</p>
     */
    @After
    public void registrarFimDoCenario(Scenario cenario) {
        long duracaoEmMilissegundos = Duration.between(inicioDoCenario, Instant.now()).toMillis();
        String marcador = cenario.isFailed() ? Simbolos.REPROVADO : Simbolos.APROVADO;

        System.out.printf("        %s cenario %s em %d ms [%s]%n",
                Simbolos.CENARIO, marcador, duracaoEmMilissegundos, cenario.getStatus());
    }

    @Entao("a operacao deve ser recusada com a mensagem {string}")
    public void aOperacaoDeveSerRecusadaComAMensagem(String mensagemEsperada) {
        CamisetasException erro = contexto.getErroCapturado();
        assertNotNull(erro, "Era esperada uma recusa, mas a operacao foi concluida com sucesso.");
        assertEquals(mensagemEsperada, erro.getMessage());
    }

    @Entao("a operacao deve ser recusada contendo a mensagem {string}")
    public void aOperacaoDeveSerRecusadaContendoAMensagem(String trechoEsperado) {
        CamisetasException erro = contexto.getErroCapturado();
        assertNotNull(erro, "Era esperada uma recusa, mas a operacao foi concluida com sucesso.");
        assertTrue(erro.getMessage().contains(trechoEsperado),
                () -> "Mensagem recebida: " + erro.getMessage());
    }

    @Entao("a recusa deve ser do tipo {string}")
    public void aRecusaDeveSerDoTipo(String nomeSimplesDaExcecao) {
        CamisetasException erro = contexto.getErroCapturado();
        assertNotNull(erro, "Nenhuma excecao foi capturada no cenario.");
        assertEquals(nomeSimplesDaExcecao, erro.getClass().getSimpleName());
    }

    @Entao("a operacao deve ser concluida sem erros")
    public void aOperacaoDeveSerConcluidaSemErros() {
        assertNull(contexto.getErroCapturado(),
                () -> "Operacao recusada inesperadamente: " + contexto.getErroCapturado());
    }
}
