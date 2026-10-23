package br.com.camisetas.excecao;

/**
 * Lançada quando a emissão da nota fiscal viola alguma regra fiscal:
 * lote inexistente, lote sem itens ou data de emissão anterior à fabricação.
 */
public class NotaFiscalInvalidaException extends CamisetasException {

    private static final long serialVersionUID = 1L;

    public NotaFiscalInvalidaException(String mensagem) {
        super(mensagem);
    }
}
