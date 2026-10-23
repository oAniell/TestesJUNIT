package br.com.camisetas.dominio;

import br.com.camisetas.excecao.DadoInvalidoException;

/**
 * Cores de camiseta produzidas pela Camisetas S.A.
 */
public enum Cor {

    BRANCO("Branco"),
    PRETO("Preto"),
    AZUL("Azul");

    private final String descricao;

    Cor(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /**
     * Converte uma descrição textual (ex.: "azul", " Branco ") no enum correspondente.
     *
     * @param descricao nome da cor, sem diferenciar maiúsculas/minúsculas
     * @return a {@link Cor} correspondente
     * @throws DadoInvalidoException se a descrição for nula, vazia ou desconhecida
     */
    public static Cor deDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            throw new DadoInvalidoException("Descricao da cor e obrigatoria.");
        }
        String normalizada = descricao.trim().toUpperCase();
        for (Cor cor : values()) {
            if (cor.name().equals(normalizada)) {
                return cor;
            }
        }
        throw new DadoInvalidoException("Cor invalida: " + descricao + ". Use Branco, Preto ou Azul.");
    }
}
