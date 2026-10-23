package br.com.camisetas.dominio;

import java.util.Objects;

import br.com.camisetas.excecao.DadoInvalidoException;

/**
 * Item cadastrado dentro de um lote de produção: identifica o código do item,
 * o tamanho (P, M ou G), a cor (Branco, Preto ou Azul) e a quantidade de camisetas.
 *
 * <p>A classe é imutável: todos os campos são {@code final} e validados no construtor,
 * portanto não existe item em estado inconsistente dentro de um {@link Lote}.</p>
 */
public final class ItemLote {

    private final String codigo;
    private final Tamanho tamanho;
    private final Cor cor;
    private final int quantidade;

    /**
     * Cadastra um item de lote com quantidade explícita.
     *
     * @param codigo     identificador do item dentro do lote (obrigatório)
     * @param tamanho    tamanho da camiseta (obrigatório)
     * @param cor        cor da camiseta (obrigatória)
     * @param quantidade quantidade de camisetas, deve ser maior que zero
     * @throws DadoInvalidoException se qualquer informação for inválida
     */
    public ItemLote(String codigo, Tamanho tamanho, Cor cor, int quantidade) {
        if (codigo == null || codigo.isBlank()) {
            throw new DadoInvalidoException("Codigo do item e obrigatorio.");
        }
        if (tamanho == null) {
            throw new DadoInvalidoException("Tamanho do item e obrigatorio.");
        }
        if (cor == null) {
            throw new DadoInvalidoException("Cor do item e obrigatoria.");
        }
        if (quantidade <= 0) {
            throw new DadoInvalidoException("Quantidade do item deve ser maior que zero. Informado: " + quantidade);
        }
        this.codigo = codigo.trim().toUpperCase();
        this.tamanho = tamanho;
        this.cor = cor;
        this.quantidade = quantidade;
    }

    /**
     * Cadastra um item de lote representando uma única camiseta.
     */
    public ItemLote(String codigo, Tamanho tamanho, Cor cor) {
        this(codigo, tamanho, cor, 1);
    }

    /**
     * Fábrica que recebe tamanho e cor como texto, útil para integrações e cenários BDD.
     *
     * @throws DadoInvalidoException se a sigla do tamanho ou a cor não existirem
     */
    public static ItemLote de(String codigo, String sigla, String descricaoCor, int quantidade) {
        return new ItemLote(codigo, Tamanho.deSigla(sigla), Cor.deDescricao(descricaoCor), quantidade);
    }

    public String getCodigo() {
        return codigo;
    }

    public Tamanho getTamanho() {
        return tamanho;
    }

    public Cor getCor() {
        return cor;
    }

    public int getQuantidade() {
        return quantidade;
    }

    /**
     * @return descrição comercial do item, ex.: {@code "Camiseta M Azul"}
     */
    public String getDescricao() {
        return String.format("Camiseta %s %s", tamanho.getSigla(), cor.getDescricao());
    }

    /**
     * Dois itens são iguais quando possuem o mesmo código, o que sustenta a
     * regra de não duplicidade dentro do lote.
     */
    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof ItemLote)) {
            return false;
        }
        return codigo.equals(((ItemLote) outro).codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return String.format("ItemLote{codigo='%s', tamanho=%s, cor=%s, quantidade=%d}",
                codigo, tamanho, cor, quantidade);
    }
}
