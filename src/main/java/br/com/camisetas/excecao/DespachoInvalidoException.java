package br.com.camisetas.excecao;

/**
 * Lançada quando o fluxo de despacho é executado fora de ordem, por exemplo:
 * embalar sem código de validação ou liberar para envio sem ter embalado.
 */
public class DespachoInvalidoException extends CamisetasException {

    private static final long serialVersionUID = 1L;

    public DespachoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
