package br.com.camisetas.bdd.passos;

import br.com.camisetas.dominio.Lote;
import br.com.camisetas.excecao.CamisetasException;
import br.com.camisetas.fiscal.NotaFiscal;
import br.com.camisetas.logistica.Despacho;

/**
 * Objeto de contexto ("world") compartilhado entre as classes de passos de um
 * mesmo cenário.
 *
 * <p>O PicoContainer cria uma instância nova a cada cenário e a injeta em todas
 * as classes de passos, garantindo isolamento entre cenários sem o uso de
 * estado estático.</p>
 */
public class ContextoProducao {

    private Lote lote;
    private NotaFiscal notaFiscal;
    private Despacho despacho;
    private CamisetasException erroCapturado;

    /**
     * Executa uma ação de negócio guardando a exceção lançada, para que o passo
     * {@code Então} possa inspecionar a mensagem recusada.
     */
    public void executarCapturandoErro(Runnable acao) {
        try {
            acao.run();
            erroCapturado = null;
        } catch (CamisetasException excecao) {
            erroCapturado = excecao;
        }
    }

    public Lote getLote() {
        if (lote == null) {
            throw new IllegalStateException("Nenhum lote foi aberto neste cenario.");
        }
        return lote;
    }

    public void setLote(Lote lote) {
        this.lote = lote;
    }

    public NotaFiscal getNotaFiscal() {
        if (notaFiscal == null) {
            throw new IllegalStateException("Nenhuma nota fiscal foi emitida neste cenario.");
        }
        return notaFiscal;
    }

    public void setNotaFiscal(NotaFiscal notaFiscal) {
        this.notaFiscal = notaFiscal;
    }

    public boolean temNotaFiscal() {
        return notaFiscal != null;
    }

    public Despacho getDespacho() {
        if (despacho == null) {
            throw new IllegalStateException("Nenhum despacho foi iniciado neste cenario.");
        }
        return despacho;
    }

    public void setDespacho(Despacho despacho) {
        this.despacho = despacho;
    }

    public CamisetasException getErroCapturado() {
        return erroCapturado;
    }
}
