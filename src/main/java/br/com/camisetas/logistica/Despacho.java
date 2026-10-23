package br.com.camisetas.logistica;

import java.time.LocalDate;

import br.com.camisetas.dominio.Lote;
import br.com.camisetas.excecao.DespachoInvalidoException;
import br.com.camisetas.fiscal.NotaFiscal;

/**
 * Preparação do lote para envio.
 *
 * <p>O fluxo é sequencial e cada etapa só pode acontecer depois da anterior:</p>
 * <ol>
 *   <li>{@link #gerarCodigoValidacao()} – gera o código que identifica o volume;</li>
 *   <li>{@link #embalar()} – embala o lote (exige o código já gerado);</li>
 *   <li>{@link #marcarComoPreparadoParaDespacho()} – libera o lote para envio.</li>
 * </ol>
 *
 * <p>O código de validação é determinístico (derivado do lote, da nota e da
 * quantidade faturada), o que o torna reproduzível e, portanto, testável.</p>
 */
public class Despacho {

    private static final String PREFIXO_CODIGO = "VAL";

    private final NotaFiscal notaFiscal;
    private String codigoValidacao;
    private boolean embalado;
    private boolean preparadoParaDespacho;
    private LocalDate dataPreparacao;

    /**
     * @param notaFiscal nota fiscal do lote a ser despachado (obrigatória)
     * @throws DespachoInvalidoException se a nota fiscal não for informada
     */
    public Despacho(NotaFiscal notaFiscal) {
        if (notaFiscal == null) {
            throw new DespachoInvalidoException("Nota fiscal e obrigatoria para preparar o despacho.");
        }
        this.notaFiscal = notaFiscal;
    }

    /**
     * Gera o código de validação do lote no formato
     * {@code VAL-<numeroLote>-<numeroNota>-<digitoVerificador>}.
     *
     * @return o código gerado
     * @throws DespachoInvalidoException se o código já tiver sido gerado
     */
    public String gerarCodigoValidacao() {
        if (codigoValidacao != null) {
            throw new DespachoInvalidoException(
                    "Codigo de validacao ja foi gerado para o lote " + getLote().getNumero() + ".");
        }
        int digito = calcularDigitoVerificador();
        codigoValidacao = String.format("%s-%s-%s-%04d",
                PREFIXO_CODIGO, getLote().getNumero(), notaFiscal.getNumero(), digito);
        return codigoValidacao;
    }

    private int calcularDigitoVerificador() {
        int base = getLote().getNumero().hashCode() * 31
                + notaFiscal.getNumero().hashCode()
                + notaFiscal.getQuantidadeTotal() * 7;
        return Math.abs(base % 10_000);
    }

    /**
     * Embala o lote.
     *
     * @throws DespachoInvalidoException se o código de validação ainda não existir
     *                                   ou se o lote já estiver embalado
     */
    public void embalar() {
        if (codigoValidacao == null) {
            throw new DespachoInvalidoException("Gere o codigo de validacao antes de embalar o lote.");
        }
        if (embalado) {
            throw new DespachoInvalidoException("Lote " + getLote().getNumero() + " ja esta embalado.");
        }
        embalado = true;
    }

    /**
     * Marca o lote como preparado para despacho e registra a data da preparação.
     *
     * @throws DespachoInvalidoException se o lote não estiver embalado ou já estiver liberado
     */
    public void marcarComoPreparadoParaDespacho() {
        if (!embalado) {
            throw new DespachoInvalidoException("Embale o lote antes de marca-lo como preparado para despacho.");
        }
        if (preparadoParaDespacho) {
            throw new DespachoInvalidoException(
                    "Lote " + getLote().getNumero() + " ja esta preparado para despacho.");
        }
        preparadoParaDespacho = true;
        dataPreparacao = LocalDate.now();
    }

    /**
     * Executa, em ordem, as três etapas do despacho.
     *
     * @return o código de validação gerado
     */
    public String prepararParaEnvio() {
        String codigo = gerarCodigoValidacao();
        embalar();
        marcarComoPreparadoParaDespacho();
        return codigo;
    }

    public NotaFiscal getNotaFiscal() {
        return notaFiscal;
    }

    public Lote getLote() {
        return notaFiscal.getLote();
    }

    public String getCodigoValidacao() {
        return codigoValidacao;
    }

    public boolean isEmbalado() {
        return embalado;
    }

    public boolean isPreparadoParaDespacho() {
        return preparadoParaDespacho;
    }

    public LocalDate getDataPreparacao() {
        return dataPreparacao;
    }

    /**
     * @return etiqueta de envio do volume
     * @throws DespachoInvalidoException se o lote ainda não está preparado para despacho
     */
    public String getEtiquetaEnvio() {
        if (!preparadoParaDespacho) {
            throw new DespachoInvalidoException("Lote ainda nao esta preparado para despacho.");
        }
        return String.format("ENVIO | Lote %s | NF %s | %d camisetas | Codigo %s",
                getLote().getNumero(), notaFiscal.getNumero(),
                notaFiscal.getQuantidadeTotal(), codigoValidacao);
    }

    @Override
    public String toString() {
        return String.format("Despacho{lote='%s', codigoValidacao='%s', embalado=%s, preparadoParaDespacho=%s}",
                getLote().getNumero(), codigoValidacao, embalado, preparadoParaDespacho);
    }
}
