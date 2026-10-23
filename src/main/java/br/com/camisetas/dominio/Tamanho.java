package br.com.camisetas.dominio;

import br.com.camisetas.excecao.DadoInvalidoException;

/**
 * Tamanhos de camiseta produzidos pela Camisetas S.A.
 *
 * <p>O uso de enum em vez de {@code String} garante, em tempo de compilação, que
 * nenhum tamanho fora de P, M ou G entre no sistema.</p>
 */
public enum Tamanho {

    P("Pequeno"),
    M("Medio"),
    G("Grande");

    private final String descricao;

    Tamanho(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getSigla() {
        return name();
    }

    /**
     * Converte uma sigla textual (ex.: "p", " M ") no enum correspondente.
     *
     * @param sigla sigla do tamanho, sem diferenciar maiúsculas/minúsculas
     * @return o {@link Tamanho} correspondente
     * @throws DadoInvalidoException se a sigla for nula, vazia ou desconhecida
     */
    public static Tamanho deSigla(String sigla) {
        if (sigla == null || sigla.isBlank()) {
            throw new DadoInvalidoException("Sigla do tamanho e obrigatoria.");
        }
        String normalizada = sigla.trim().toUpperCase();
        for (Tamanho tamanho : values()) {
            if (tamanho.name().equals(normalizada)) {
                return tamanho;
            }
        }
        throw new DadoInvalidoException("Tamanho invalido: " + sigla + ". Use P, M ou G.");
    }
}
