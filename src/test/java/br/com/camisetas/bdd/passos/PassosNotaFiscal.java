package br.com.camisetas.bdd.passos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

import br.com.camisetas.dominio.Cor;
import br.com.camisetas.dominio.Tamanho;
import br.com.camisetas.fiscal.NotaFiscal;

/**
 * Passos (step definitions) da funcionalidade de emissão da nota fiscal do lote.
 */
public class PassosNotaFiscal {

    private final ContextoProducao contexto;

    public PassosNotaFiscal(ContextoProducao contexto) {
        this.contexto = contexto;
    }

    // ------------------------------------------------------------------
    // Dado / Quando
    // ------------------------------------------------------------------

    @Dado("que o faturamento emitiu a nota fiscal {string} em {string}")
    public void queOFaturamentoEmitiuANotaFiscal(String numero, String dataEmissao) {
        emitirNotaFiscal(numero, dataEmissao);
    }

    @Quando("o faturamento emite a nota fiscal {string} em {string}")
    public void oFaturamentoEmiteANotaFiscal(String numero, String dataEmissao) {
        emitirNotaFiscal(numero, dataEmissao);
    }

    @Quando("o faturamento tenta emitir a nota fiscal {string} em {string}")
    public void oFaturamentoTentaEmitirANotaFiscal(String numero, String dataEmissao) {
        contexto.executarCapturandoErro(() -> emitirNotaFiscal(numero, dataEmissao));
    }

    @Quando("o faturamento emite a nota fiscal do lote na data de hoje")
    public void oFaturamentoEmiteANotaFiscalDeHoje() {
        contexto.setNotaFiscal(NotaFiscal.emitirPara(contexto.getLote()));
    }

    // ------------------------------------------------------------------
    // Entao
    // ------------------------------------------------------------------

    @Entao("o numero da nota fiscal deve ser {string}")
    public void oNumeroDaNotaFiscalDeveSer(String numeroEsperado) {
        assertEquals(numeroEsperado, contexto.getNotaFiscal().getNumero());
    }

    @Entao("a data de emissao da nota fiscal deve ser {string}")
    public void aDataDeEmissaoDaNotaFiscalDeveSer(String dataEsperada) {
        assertEquals(LocalDate.parse(dataEsperada), contexto.getNotaFiscal().getDataEmissao());
    }

    @Entao("a nota fiscal deve estar vinculada ao lote {string}")
    public void aNotaFiscalDeveEstarVinculadaAoLote(String numeroLote) {
        assertEquals(numeroLote, contexto.getNotaFiscal().getLote().getNumero());
    }

    @Entao("a nota fiscal deve totalizar {int} camisetas")
    public void aNotaFiscalDeveTotalizarCamisetas(int totalEsperado) {
        assertEquals(totalEsperado, contexto.getNotaFiscal().getQuantidadeTotal());
    }

    @Entao("a nota fiscal deve registrar {int} camisetas do tamanho {string}")
    public void aNotaFiscalDeveRegistrarCamisetasDoTamanho(int quantidadeEsperada, String sigla) {
        assertEquals(quantidadeEsperada,
                contexto.getNotaFiscal().getQuantidadePorTamanho(Tamanho.deSigla(sigla)));
    }

    @Entao("a nota fiscal deve registrar {int} camisetas da cor {string}")
    public void aNotaFiscalDeveRegistrarCamisetasDaCor(int quantidadeEsperada, String cor) {
        assertEquals(quantidadeEsperada,
                contexto.getNotaFiscal().getQuantidadePorCor(Cor.deDescricao(cor)));
    }

    @Entao("a nota fiscal deve detalhar as quantidades por tamanho:")
    public void aNotaFiscalDeveDetalharPorTamanho(DataTable tabela) {
        List<Map<String, String>> linhas = tabela.asMaps();
        for (Map<String, String> linha : linhas) {
            Tamanho tamanho = Tamanho.deSigla(linha.get("tamanho"));
            int esperado = Integer.parseInt(linha.get("quantidade"));
            assertEquals(esperado, contexto.getNotaFiscal().getQuantidadePorTamanho(tamanho),
                    () -> "Divergencia no tamanho " + tamanho);
        }
    }

    @Entao("a nota fiscal deve detalhar as quantidades por cor:")
    public void aNotaFiscalDeveDetalharPorCor(DataTable tabela) {
        List<Map<String, String>> linhas = tabela.asMaps();
        for (Map<String, String> linha : linhas) {
            Cor cor = Cor.deDescricao(linha.get("cor"));
            int esperado = Integer.parseInt(linha.get("quantidade"));
            assertEquals(esperado, contexto.getNotaFiscal().getQuantidadePorCor(cor),
                    () -> "Divergencia na cor " + cor);
        }
    }

    @Entao("o detalhamento impresso da nota fiscal deve conter {string}")
    public void oDetalhamentoImpressoDeveConter(String trecho) {
        String detalhe = contexto.getNotaFiscal().detalhar();
        assertTrue(detalhe.contains(trecho),
                () -> "Trecho ausente no detalhamento. Esperado: " + trecho + System.lineSeparator() + detalhe);
    }

    @Entao("nenhuma nota fiscal deve ter sido emitida")
    public void nenhumaNotaFiscalDeveTerSidoEmitida() {
        assertFalse(contexto.temNotaFiscal(), "nao deveria existir nota fiscal no contexto");
    }

    // ------------------------------------------------------------------
    // Apoio
    // ------------------------------------------------------------------

    private void emitirNotaFiscal(String numero, String dataEmissao) {
        contexto.setNotaFiscal(new NotaFiscal(numero, contexto.getLote(), LocalDate.parse(dataEmissao)));
    }
}
