package br.com.camisetas.suporte;

import java.time.LocalDate;

import br.com.camisetas.dominio.Cor;
import br.com.camisetas.dominio.ItemLote;
import br.com.camisetas.dominio.Lote;
import br.com.camisetas.dominio.Tamanho;
import br.com.camisetas.fiscal.NotaFiscal;

/**
 * Fábrica de massa de teste (<em>Object Mother</em>).
 *
 * <p>Concentra a criação dos objetos usados nos testes para que os cenários fiquem
 * curtos e legíveis, e para que uma mudança de construtor seja ajustada em um
 * único lugar.</p>
 */
public final class CenarioFabrica {

    public static final LocalDate DATA_FABRICACAO = LocalDate.of(2024, 5, 10);
    public static final String NUMERO_LOTE_PADRAO = "LOTE-2024-001";

    private CenarioFabrica() {
        throw new AssertionError("Classe utilitaria nao deve ser instanciada.");
    }

    /**
     * @return lote vazio com capacidade para 100 camisetas
     */
    public static Lote loteVazio() {
        return new Lote(NUMERO_LOTE_PADRAO, DATA_FABRICACAO, 100);
    }

    /**
     * @return lote vazio com a capacidade informada
     */
    public static Lote loteVazio(int totalItens) {
        return new Lote(NUMERO_LOTE_PADRAO, DATA_FABRICACAO, totalItens);
    }

    /**
     * Lote completo de 100 camisetas:
     * <ul>
     *   <li>por tamanho: P = 30, M = 45, G = 25;</li>
     *   <li>por cor: Branco = 50, Preto = 30, Azul = 20.</li>
     * </ul>
     */
    public static Lote loteCompleto() {
        return loteVazio(100)
                .adicionarItem(new ItemLote("IT-01", Tamanho.P, Cor.BRANCO, 20))
                .adicionarItem(new ItemLote("IT-02", Tamanho.P, Cor.PRETO, 10))
                .adicionarItem(new ItemLote("IT-03", Tamanho.M, Cor.BRANCO, 30))
                .adicionarItem(new ItemLote("IT-04", Tamanho.M, Cor.AZUL, 15))
                .adicionarItem(new ItemLote("IT-05", Tamanho.G, Cor.PRETO, 20))
                .adicionarItem(new ItemLote("IT-06", Tamanho.G, Cor.AZUL, 5));
    }

    /**
     * @return nota fiscal emitida para {@link #loteCompleto()}
     */
    public static NotaFiscal notaFiscalDoLoteCompleto() {
        return new NotaFiscal("NF-0001", loteCompleto(), DATA_FABRICACAO.plusDays(1));
    }

    /**
     * @return item padrão (camiseta M Azul, 10 unidades)
     */
    public static ItemLote itemPadrao() {
        return new ItemLote("IT-PADRAO", Tamanho.M, Cor.AZUL, 10);
    }
}
