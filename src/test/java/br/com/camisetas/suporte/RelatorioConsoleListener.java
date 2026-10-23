package br.com.camisetas.suporte;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * Listener do JUnit Platform que imprime o andamento dos testes no terminal
 * com marcadores visuais de aprovado, reprovado e ignorado, encerrando com um
 * resumo da execução.
 *
 * <p>É registrado automaticamente via {@code ServiceLoader}, pelo arquivo
 * {@code src/test/resources/META-INF/services/org.junit.platform.launcher.TestExecutionListener},
 * portanto vale para os testes JUnit e para os cenários Cucumber.</p>
 */
public class RelatorioConsoleListener implements TestExecutionListener {

    private static final String LINHA = "=".repeat(78);
    private static final int LARGURA_ROTULO = 16;

    private final AtomicInteger aprovados = new AtomicInteger();
    private final AtomicInteger reprovados = new AtomicInteger();
    private final AtomicInteger ignorados = new AtomicInteger();

    private TestPlan planoDeTestes;
    private Instant inicio;

    @Override
    public void testPlanExecutionStarted(TestPlan planoDeTestes) {
        this.planoDeTestes = planoDeTestes;
        this.inicio = Instant.now();

        System.out.println();
        System.out.println(LINHA);
        System.out.println("  CAMISETAS S.A. - EXECUCAO DOS TESTES (JUnit 5 + Cucumber)");
        System.out.println(LINHA);
    }

    @Override
    public void executionStarted(TestIdentifier identificador) {
        if (identificador.isContainer() && !ehRaiz(identificador)) {
            System.out.printf("%s%s %s%n",
                    recuo(identificador), Simbolos.GRUPO, identificador.getDisplayName());
        }
    }

    @Override
    public void executionFinished(TestIdentifier identificador, TestExecutionResult resultado) {
        if (!identificador.isTest()) {
            return;
        }
        switch (resultado.getStatus()) {
            case SUCCESSFUL -> {
                aprovados.incrementAndGet();
                imprimirTeste(identificador, Simbolos.APROVADO, null);
            }
            case FAILED, ABORTED -> {
                reprovados.incrementAndGet();
                imprimirTeste(identificador, Simbolos.REPROVADO, motivoDaFalha(resultado));
            }
            default -> { /* nenhum outro status e produzido pelo engine */ }
        }
    }

    @Override
    public void executionSkipped(TestIdentifier identificador, String motivo) {
        if (identificador.isTest()) {
            ignorados.incrementAndGet();
            imprimirTeste(identificador, Simbolos.IGNORADO, motivo);
        }
    }

    @Override
    public void testPlanExecutionFinished(TestPlan planoDeTestes) {
        Duration duracao = Duration.between(inicio, Instant.now());
        int total = aprovados.get() + reprovados.get() + ignorados.get();

        System.out.println();
        System.out.println(LINHA);
        System.out.printf("  %s RESUMO DA EXECUCAO%n", Simbolos.RELATORIO);
        System.out.println(LINHA);
        imprimirIndicador(Simbolos.APROVADO, "Aprovados", String.valueOf(aprovados.get()));
        imprimirIndicador(Simbolos.REPROVADO, "Reprovados", String.valueOf(reprovados.get()));
        imprimirIndicador(Simbolos.IGNORADO, "Ignorados", String.valueOf(ignorados.get()));
        imprimirIndicador(Simbolos.TEMPO, "Tempo total",
                String.format("%.1f s", duracao.toMillis() / 1000.0));
        imprimirIndicador(Simbolos.RELATORIO, "Total", String.valueOf(total));
        System.out.println(LINHA);

        if (reprovados.get() == 0) {
            System.out.printf("  %s TODOS OS TESTES PASSARAM%n", Simbolos.APROVADO);
        } else {
            System.out.printf("  %s %d TESTE(S) REPROVADO(S) - veja os detalhes acima%n",
                    Simbolos.REPROVADO, reprovados.get());
        }
        System.out.println(LINHA);
        System.out.println();
    }

    // ------------------------------------------------------------------
    // Apoio
    // ------------------------------------------------------------------

    private void imprimirTeste(TestIdentifier identificador, String marcador, String detalhe) {
        System.out.printf("%s%s %s%n", recuo(identificador), marcador, identificador.getDisplayName());
        if (detalhe != null && !detalhe.isBlank()) {
            System.out.printf("%s    -> %s%n", recuo(identificador), primeiraLinha(detalhe));
        }
    }

    private void imprimirIndicador(String marcador, String rotulo, String valor) {
        String pontilhado = ".".repeat(Math.max(1, LARGURA_ROTULO - rotulo.length()));
        System.out.printf("  %s %s %s %s%n", marcador, rotulo, pontilhado, valor);
    }

    /** Recuo proporcional à profundidade do teste na árvore de execução. */
    private String recuo(TestIdentifier identificador) {
        int profundidade = 0;
        Optional<TestIdentifier> pai = planoDeTestes.getParent(identificador);
        while (pai.isPresent()) {
            profundidade++;
            pai = planoDeTestes.getParent(pai.get());
        }
        return "  ".repeat(Math.max(0, profundidade));
    }

    private boolean ehRaiz(TestIdentifier identificador) {
        return planoDeTestes.getParent(identificador).isEmpty();
    }

    private String motivoDaFalha(TestExecutionResult resultado) {
        return resultado.getThrowable()
                .map(erro -> erro.getClass().getSimpleName()
                        + (erro.getMessage() == null ? "" : ": " + erro.getMessage()))
                .orElse("sem detalhes");
    }

    private String primeiraLinha(String texto) {
        int quebra = texto.indexOf(System.lineSeparator());
        if (quebra < 0) {
            quebra = texto.indexOf('\n');
        }
        String linha = quebra < 0 ? texto : texto.substring(0, quebra);
        return linha.length() > 140 ? linha.substring(0, 137) + "..." : linha;
    }
}
