package br.com.camisetas.excecao;

/**
 * Exceção base (não checada) de todas as regras de negócio da Camisetas S.A.
 *
 * <p>Centralizar a hierarquia em uma única raiz permite que o chamador capture
 * qualquer violação de regra de negócio com um único {@code catch} e facilita a
 * escrita de assertivas do tipo {@code assertThrows(CamisetasException.class, ...)}.</p>
 */
public class CamisetasException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CamisetasException(String mensagem) {
        super(mensagem);
    }

    public CamisetasException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
