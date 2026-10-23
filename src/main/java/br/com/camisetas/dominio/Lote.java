package br.com.camisetas.dominio;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import br.com.camisetas.excecao.CapacidadeLoteExcedidaException;
import br.com.camisetas.excecao.DadoInvalidoException;
import br.com.camisetas.excecao.ItemDuplicadoException;

/**
 * Lote de produção de camisetas.
 *
 * <p>Responsabilidades:</p>
 * <ul>
 *   <li>guardar o número identificador, a data de fabricação e o total de itens planejado;</li>
 *   <li>receber os {@link ItemLote} produzidos, respeitando a capacidade declarada;</li>
 *   <li>informar se a produção do lote foi concluída.</li>
 * </ul>
 */
public class Lote {

    private final String numero;
    private final LocalDate dataFabricacao;
    private final int totalItens;
    private final List<ItemLote> itens = new ArrayList<>();

    /**
     * Abre um novo lote de produção.
     *
     * @param numero         número identificador do lote (obrigatório)
     * @param dataFabricacao data de fabricação, não pode ser nula nem futura
     * @param totalItens     quantidade total de camisetas planejada, maior que zero
     * @throws DadoInvalidoException se qualquer informação for inválida
     */
    public Lote(String numero, LocalDate dataFabricacao, int totalItens) {
        if (numero == null || numero.isBlank()) {
            throw new DadoInvalidoException("Numero do lote e obrigatorio.");
        }
        if (dataFabricacao == null) {
            throw new DadoInvalidoException("Data de fabricacao e obrigatoria.");
        }
        if (dataFabricacao.isAfter(LocalDate.now())) {
            throw new DadoInvalidoException("Data de fabricacao nao pode ser futura: " + dataFabricacao);
        }
        if (totalItens <= 0) {
            throw new DadoInvalidoException("Total de itens do lote deve ser maior que zero. Informado: " + totalItens);
        }
        this.numero = numero.trim();
        this.dataFabricacao = dataFabricacao;
        this.totalItens = totalItens;
    }

    /**
     * Cadastra um item no lote.
     *
     * @param item item a ser cadastrado
     * @return o próprio lote, permitindo encadeamento de chamadas
     * @throws DadoInvalidoException              se o item for nulo
     * @throws ItemDuplicadoException             se já existir item com o mesmo código
     * @throws CapacidadeLoteExcedidaException    se a soma das quantidades passar do total do lote
     */
    public Lote adicionarItem(ItemLote item) {
        if (item == null) {
            throw new DadoInvalidoException("Item do lote nao pode ser nulo.");
        }
        if (itens.contains(item)) {
            throw new ItemDuplicadoException("Item ja cadastrado no lote " + numero + ": " + item.getCodigo());
        }
        int quantidadeFinal = getQuantidadeCadastrada() + item.getQuantidade();
        if (quantidadeFinal > totalItens) {
            throw new CapacidadeLoteExcedidaException(
                    "Capacidade do lote " + numero + " excedida. Total permitido: " + totalItens
                            + ", solicitado: " + quantidadeFinal,
                    totalItens, quantidadeFinal);
        }
        itens.add(item);
        return this;
    }

    /**
     * Remove do lote o item com o código informado.
     *
     * @return {@code true} se algum item foi removido
     */
    public boolean removerItem(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new DadoInvalidoException("Codigo do item e obrigatorio para remocao.");
        }
        String alvo = codigo.trim().toUpperCase();
        return itens.removeIf(item -> item.getCodigo().equals(alvo));
    }

    public String getNumero() {
        return numero;
    }

    public LocalDate getDataFabricacao() {
        return dataFabricacao;
    }

    public int getTotalItens() {
        return totalItens;
    }

    /**
     * @return lista imutável dos itens cadastrados, protegendo o estado interno do lote
     */
    public List<ItemLote> getItens() {
        return Collections.unmodifiableList(itens);
    }

    /**
     * @return quantidade de registros de item (não é a soma das quantidades)
     */
    public int getQuantidadeDeItens() {
        return itens.size();
    }

    /**
     * @return soma das quantidades de camisetas já cadastradas no lote
     */
    public int getQuantidadeCadastrada() {
        return itens.stream().mapToInt(ItemLote::getQuantidade).sum();
    }

    /**
     * @return quantas camisetas ainda faltam para fechar o lote
     */
    public int getQuantidadeRestante() {
        return totalItens - getQuantidadeCadastrada();
    }

    /**
     * @return {@code true} quando a quantidade cadastrada atinge o total planejado
     */
    public boolean isCompleto() {
        return getQuantidadeCadastrada() == totalItens;
    }

    /**
     * @return {@code true} quando nenhum item foi cadastrado ainda
     */
    public boolean isVazio() {
        return itens.isEmpty();
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Lote)) {
            return false;
        }
        return numero.equals(((Lote) outro).numero);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numero);
    }

    @Override
    public String toString() {
        return String.format("Lote{numero='%s', dataFabricacao=%s, totalItens=%d, itensCadastrados=%d}",
                numero, dataFabricacao, totalItens, itens.size());
    }
}
