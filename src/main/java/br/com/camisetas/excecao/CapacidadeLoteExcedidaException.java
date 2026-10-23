package br.com.camisetas.excecao;

/**
 * Lançada quando a soma das quantidades dos itens ultrapassa o total de itens
 * declarado na abertura do lote.
 */
public class CapacidadeLoteExcedidaException extends CamisetasException {

    private static final long serialVersionUID = 1L;

    private final int capacidade;
    private final int quantidadeSolicitada;

    public CapacidadeLoteExcedidaException(String mensagem, int capacidade, int quantidadeSolicitada) {
        super(mensagem);
        this.capacidade = capacidade;
        this.quantidadeSolicitada = quantidadeSolicitada;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public int getQuantidadeSolicitada() {
        return quantidadeSolicitada;
    }
}
