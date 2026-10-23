package br.com.camisetas.excecao;

/**
 * Lançada quando um dado obrigatório é nulo, vazio ou está fora do domínio aceito
 * (por exemplo: quantidade zero/negativa ou data de fabricação no futuro).
 */
public class DadoInvalidoException extends CamisetasException {

    private static final long serialVersionUID = 1L;

    public DadoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
