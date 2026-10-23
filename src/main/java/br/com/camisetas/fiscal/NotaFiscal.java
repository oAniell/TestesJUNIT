package br.com.camisetas.fiscal;

import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import br.com.camisetas.dominio.Cor;
import br.com.camisetas.dominio.ItemLote;
import br.com.camisetas.dominio.Lote;
import br.com.camisetas.dominio.Tamanho;
import br.com.camisetas.excecao.NotaFiscalInvalidaException;

/**
 * Nota fiscal emitida para um lote já produzido.
 *
 * <p>Além do número e da data de emissão, a nota consolida a quantidade total de
 * camisetas agrupada por tamanho e por cor, totais calculados na emissão a partir
 * dos itens do lote.</p>
 */
public class NotaFiscal {

    private static final String PREFIXO_NUMERO = "NF-";

    private final String numero;
    private final LocalDate dataEmissao;
    private final Lote lote;
    private final Map<Tamanho, Integer> quantidadePorTamanho;
    private final Map<Cor, Integer> quantidadePorCor;

    /**
     * Emite a nota fiscal de um lote.
     *
     * @param numero      número da nota fiscal (obrigatório)
     * @param lote        lote faturado, precisa ter ao menos um item
     * @param dataEmissao data de emissão, não pode ser anterior à fabricação do lote
     * @throws NotaFiscalInvalidaException se alguma regra fiscal for violada
     */
    public NotaFiscal(String numero, Lote lote, LocalDate dataEmissao) {
        if (numero == null || numero.isBlank()) {
            throw new NotaFiscalInvalidaException("Numero da nota fiscal e obrigatorio.");
        }
        if (lote == null) {
            throw new NotaFiscalInvalidaException("Lote e obrigatorio para emissao da nota fiscal.");
        }
        if (lote.isVazio()) {
            throw new NotaFiscalInvalidaException(
                    "Nao e possivel emitir nota fiscal para o lote " + lote.getNumero() + " sem itens.");
        }
        if (dataEmissao == null) {
            throw new NotaFiscalInvalidaException("Data de emissao e obrigatoria.");
        }
        if (dataEmissao.isBefore(lote.getDataFabricacao())) {
            throw new NotaFiscalInvalidaException(
                    "Data de emissao (" + dataEmissao + ") nao pode ser anterior a fabricacao ("
                            + lote.getDataFabricacao() + ").");
        }
        this.numero = numero.trim();
        this.lote = lote;
        this.dataEmissao = dataEmissao;
        this.quantidadePorTamanho = Collections.unmodifiableMap(totalizarPorTamanho(lote));
        this.quantidadePorCor = Collections.unmodifiableMap(totalizarPorCor(lote));
    }

    /**
     * Emite a nota fiscal do lote na data de hoje, derivando o número a partir do lote.
     */
    public static NotaFiscal emitirPara(Lote lote) {
        if (lote == null) {
            throw new NotaFiscalInvalidaException("Lote e obrigatorio para emissao da nota fiscal.");
        }
        return new NotaFiscal(PREFIXO_NUMERO + lote.getNumero(), lote, LocalDate.now());
    }

    private static Map<Tamanho, Integer> totalizarPorTamanho(Lote lote) {
        Map<Tamanho, Integer> totais = new EnumMap<>(Tamanho.class);
        for (Tamanho tamanho : Tamanho.values()) {
            totais.put(tamanho, 0);
        }
        for (ItemLote item : lote.getItens()) {
            totais.merge(item.getTamanho(), item.getQuantidade(), Integer::sum);
        }
        return totais;
    }

    private static Map<Cor, Integer> totalizarPorCor(Lote lote) {
        Map<Cor, Integer> totais = new EnumMap<>(Cor.class);
        for (Cor cor : Cor.values()) {
            totais.put(cor, 0);
        }
        for (ItemLote item : lote.getItens()) {
            totais.merge(item.getCor(), item.getQuantidade(), Integer::sum);
        }
        return totais;
    }

    public String getNumero() {
        return numero;
    }

    public LocalDate getDataEmissao() {
        return dataEmissao;
    }

    public Lote getLote() {
        return lote;
    }

    /**
     * @return mapa imutável com todos os tamanhos e suas quantidades faturadas
     */
    public Map<Tamanho, Integer> getQuantidadePorTamanho() {
        return quantidadePorTamanho;
    }

    /**
     * @return mapa imutável com todas as cores e suas quantidades faturadas
     */
    public Map<Cor, Integer> getQuantidadePorCor() {
        return quantidadePorCor;
    }

    public int getQuantidadePorTamanho(Tamanho tamanho) {
        if (tamanho == null) {
            throw new NotaFiscalInvalidaException("Tamanho e obrigatorio para consulta.");
        }
        return quantidadePorTamanho.get(tamanho);
    }

    public int getQuantidadePorCor(Cor cor) {
        if (cor == null) {
            throw new NotaFiscalInvalidaException("Cor e obrigatoria para consulta.");
        }
        return quantidadePorCor.get(cor);
    }

    /**
     * @return quantidade total de camisetas faturadas na nota
     */
    public int getQuantidadeTotal() {
        return quantidadePorTamanho.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * @return texto detalhado da nota, usado na impressão/conferência
     */
    public String detalhar() {
        StringBuilder detalhe = new StringBuilder();
        detalhe.append("NOTA FISCAL ").append(numero)
                .append(" | Lote ").append(lote.getNumero())
                .append(" | Emissao ").append(dataEmissao)
                .append(" | Total ").append(getQuantidadeTotal()).append(" camisetas");
        for (Map.Entry<Tamanho, Integer> entrada : quantidadePorTamanho.entrySet()) {
            detalhe.append(System.lineSeparator())
                    .append("  Tamanho ").append(entrada.getKey().getSigla())
                    .append(": ").append(entrada.getValue());
        }
        for (Map.Entry<Cor, Integer> entrada : quantidadePorCor.entrySet()) {
            detalhe.append(System.lineSeparator())
                    .append("  Cor ").append(entrada.getKey().getDescricao())
                    .append(": ").append(entrada.getValue());
        }
        return detalhe.toString();
    }

    @Override
    public String toString() {
        return String.format("NotaFiscal{numero='%s', lote='%s', dataEmissao=%s, quantidadeTotal=%d}",
                numero, lote.getNumero(), dataEmissao, getQuantidadeTotal());
    }
}
