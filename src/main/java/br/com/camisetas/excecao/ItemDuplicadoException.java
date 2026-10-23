package br.com.camisetas.excecao;

/**
 * Lançada ao tentar cadastrar no mesmo lote dois itens com o mesmo código.
 */
public class ItemDuplicadoException extends CamisetasException {

    private static final long serialVersionUID = 1L;

    public ItemDuplicadoException(String mensagem) {
        super(mensagem);
    }
}
