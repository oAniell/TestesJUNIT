package br.com.camisetas.suporte;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Locale;

/**
 * Marcadores visuais usados nos logs de console dos testes.
 *
 * <h2>Por que isso não é só decoração</h2>
 *
 * <p>O console do Windows em português costuma usar a página de código 850 (ou 1252),
 * que não contém emoji. Ao imprimir {@code ✅} nesse terminal, o caractere é
 * substituído por {@code ?} e o log fica pior do que se nunca tivesse tido emoji.</p>
 *
 * <h2>Como a escolha é feita</h2>
 *
 * <p>A propriedade de sistema {@code testes.marcadores} decide o conjunto usado:</p>
 *
 * <ul>
 *   <li>{@code emoji} &mdash; força os emoji. Use quando o terminal já está em UTF-8
 *       (no Windows: {@code chcp 65001}); é o que o {@code demo.ps1} faz.</li>
 *   <li>{@code ascii} &mdash; força os marcadores {@code [OK]} / {@code [X]}.</li>
 *   <li>{@code auto} (padrão) &mdash; só usa emoji quando há console interativo capaz de
 *       representá-los, ou quando a saída é UTF-8 fora do Windows. Em qualquer dúvida,
 *       cai para ASCII.</li>
 * </ul>
 *
 * <p>O padrão é conservador de propósito: um log inteiro em ASCII é legível, um log
 * cheio de {@code ?} não é.</p>
 */
public final class Simbolos {

    /** Nome da propriedade de sistema que escolhe o conjunto de marcadores. */
    public static final String PROPRIEDADE = "testes.marcadores";

    /** Marcador de teste aprovado. */
    public static final String APROVADO;

    /** Marcador de teste reprovado. */
    public static final String REPROVADO;

    /** Marcador de teste ignorado. */
    public static final String IGNORADO;

    /** Marcador de grupo/suíte de testes. */
    public static final String GRUPO;

    /** Marcador de método de preparação (@BeforeAll / @AfterAll). */
    public static final String PREPARO;

    /** Marcador de cenário BDD. */
    public static final String CENARIO;

    /** Marcador de tempo. */
    public static final String TEMPO;

    /** Marcador de resumo/relatório. */
    public static final String RELATORIO;

    static {
        if (usarEmoji()) {
            // Sem o seletor de variacao U+FE0F: em alguns terminais ele vira um
            // quadrado vazio ao lado do simbolo e desalinha as colunas.
            APROVADO = "\u2705";          // marca de verificação verde
            REPROVADO = "\u274C";         // cruz vermelha
            IGNORADO = "\u23ED";          // avançar
            GRUPO = "\u25B6";             // triângulo
            PREPARO = "\uD83D\uDD27";     // chave inglesa
            CENARIO = "\uD83E\uDD52";     // pepino (Cucumber)
            TEMPO = "\u23F1";             // cronômetro
            RELATORIO = "\uD83D\uDCCA";   // gráfico
        } else {
            APROVADO = "[OK]";
            REPROVADO = "[X] ";
            IGNORADO = "[--]";
            GRUPO = ">";
            PREPARO = "*";
            CENARIO = "#";
            TEMPO = "~";
            RELATORIO = "=";
        }
    }

    private Simbolos() {
        throw new AssertionError("Classe utilitaria nao deve ser instanciada.");
    }

    /**
     * @return {@code true} quando os marcadores devem ser emoji
     */
    private static boolean usarEmoji() {
        String escolha = System.getProperty(PROPRIEDADE, "auto").trim().toLowerCase(Locale.ROOT);

        switch (escolha) {
            case "emoji":
                return true;
            case "ascii":
                return false;
            default:
                return detectarSuporteAEmoji();
        }
    }

    /**
     * Detecção conservadora: só aprova emoji quando há evidência de que o destino
     * consegue representá-los.
     *
     * <p>No Windows, a JVM de teste é iniciada pelo Surefire com a saída ligada a um
     * {@code pipe}, portanto não há console para consultar e a página de código real do
     * terminal é desconhecida. Nesse caso a resposta é {@code false}: é melhor imprimir
     * {@code [OK]} do que arriscar um {@code ?}.</p>
     */
    private static boolean detectarSuporteAEmoji() {
        if (!charsetDaSaidaRepresentaEmoji()) {
            return false;
        }
        // Console interativo: a codificação informada é a do terminal de verdade.
        if (System.console() != null) {
            return true;
        }
        // Fora do Windows, UTF-8 na saída é indicativo suficiente.
        return !ehWindows();
    }

    private static boolean charsetDaSaidaRepresentaEmoji() {
        try {
            return charsetDaSaida().newEncoder().canEncode("\u2705");
        } catch (IllegalCharsetNameException | UnsupportedCharsetException excecao) {
            return false;
        }
    }

    private static Charset charsetDaSaida() {
        String nome = System.getProperty("stdout.encoding");
        if (nome == null) {
            nome = System.getProperty("sun.stdout.encoding");
        }
        if (nome == null) {
            nome = System.getProperty("file.encoding", "UTF-8");
        }
        return Charset.forName(nome);
    }

    private static boolean ehWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }
}
