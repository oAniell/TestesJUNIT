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

import br.com.camisetas.dominio.ItemLote;
import br.com.camisetas.dominio.Lote;

/**
 * Passos (step definitions) das funcionalidades de abertura do lote e
 * cadastro de itens de produção.
 */
public class PassosLote {

    private final ContextoProducao contexto;

    /** O PicoContainer injeta o contexto compartilhado do cenário. */
    public PassosLote(ContextoProducao contexto) {
        this.contexto = contexto;
    }

    // ------------------------------------------------------------------
    // Dado
    // ------------------------------------------------------------------

    @Dado("que a empresa abriu o lote {string} fabricado em {string} com capacidade para {int} camisetas")
    public void queAEmpresaAbriuOLote(String numero, String dataFabricacao, int capacidade) {
        contexto.setLote(new Lote(numero, LocalDate.parse(dataFabricacao), capacidade));
    }

    @Dado("que o lote possui os seguintes itens cadastrados:")
    public void queOLotePossuiOsSeguintesItens(DataTable tabela) {
        cadastrarItensDaTabela(tabela);
    }

    // ------------------------------------------------------------------
    // Quando
    // ------------------------------------------------------------------

    @Quando("a producao cadastra o item {string} de tamanho {string} e cor {string} com {int} camisetas")
    public void aProducaoCadastraOItem(String codigo, String tamanho, String cor, int quantidade) {
        contexto.getLote().adicionarItem(ItemLote.de(codigo, tamanho, cor, quantidade));
    }

    @Quando("a producao cadastra os seguintes itens:")
    public void aProducaoCadastraOsSeguintesItens(DataTable tabela) {
        cadastrarItensDaTabela(tabela);
    }

    @Quando("a producao tenta cadastrar o item {string} de tamanho {string} e cor {string} com {int} camisetas")
    public void aProducaoTentaCadastrarOItem(String codigo, String tamanho, String cor, int quantidade) {
        contexto.executarCapturandoErro(
                () -> contexto.getLote().adicionarItem(ItemLote.de(codigo, tamanho, cor, quantidade)));
    }

    @Quando("a empresa tenta abrir o lote {string} fabricado em {string} com capacidade para {int} camisetas")
    public void aEmpresaTentaAbrirOLote(String numero, String dataFabricacao, int capacidade) {
        contexto.executarCapturandoErro(
                () -> contexto.setLote(new Lote(numero, LocalDate.parse(dataFabricacao), capacidade)));
    }

    @Quando("a producao remove o item {string} do lote")
    public void aProducaoRemoveOItemDoLote(String codigo) {
        contexto.getLote().removerItem(codigo);
    }

    // ------------------------------------------------------------------
    // Entao
    // ------------------------------------------------------------------

    @Entao("o numero do lote deve ser {string}")
    public void oNumeroDoLoteDeveSer(String numeroEsperado) {
        assertEquals(numeroEsperado, contexto.getLote().getNumero());
    }

    @Entao("a data de fabricacao do lote deve ser {string}")
    public void aDataDeFabricacaoDoLoteDeveSer(String dataEsperada) {
        assertEquals(LocalDate.parse(dataEsperada), contexto.getLote().getDataFabricacao());
    }

    @Entao("a capacidade total do lote deve ser de {int} camisetas")
    public void aCapacidadeTotalDoLoteDeveSerDe(int capacidadeEsperada) {
        assertEquals(capacidadeEsperada, contexto.getLote().getTotalItens());
    }

    @Entao("o lote deve conter {int} registros de item")
    public void oLoteDeveConterRegistrosDeItem(int quantidadeEsperada) {
        assertEquals(quantidadeEsperada, contexto.getLote().getQuantidadeDeItens());
    }

    @Entao("o lote deve ter {int} camisetas cadastradas")
    public void oLoteDeveTerCamisetasCadastradas(int quantidadeEsperada) {
        assertEquals(quantidadeEsperada, contexto.getLote().getQuantidadeCadastrada());
    }

    @Entao("o lote deve ter capacidade restante de {int} camisetas")
    public void oLoteDeveTerCapacidadeRestanteDe(int restanteEsperado) {
        assertEquals(restanteEsperado, contexto.getLote().getQuantidadeRestante());
    }

    @Entao("o lote deve estar completo")
    public void oLoteDeveEstarCompleto() {
        assertTrue(contexto.getLote().isCompleto(),
                () -> "Lote nao esta completo: " + contexto.getLote());
    }

    @Entao("o lote nao deve estar completo")
    public void oLoteNaoDeveEstarCompleto() {
        assertFalse(contexto.getLote().isCompleto());
    }

    @Entao("o lote deve estar vazio")
    public void oLoteDeveEstarVazio() {
        assertTrue(contexto.getLote().isVazio());
    }

    @Entao("o item {string} deve constar no lote com tamanho {string} e cor {string}")
    public void oItemDeveConstarNoLote(String codigo, String tamanho, String cor) {
        ItemLote esperado = ItemLote.de(codigo, tamanho, cor, 1);
        ItemLote encontrado = contexto.getLote().getItens().stream()
                .filter(item -> item.equals(esperado))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Item nao encontrado no lote: " + codigo));

        assertEquals(esperado.getTamanho(), encontrado.getTamanho());
        assertEquals(esperado.getCor(), encontrado.getCor());
    }

    // ------------------------------------------------------------------
    // Apoio
    // ------------------------------------------------------------------

    private void cadastrarItensDaTabela(DataTable tabela) {
        List<Map<String, String>> linhas = tabela.asMaps();
        for (Map<String, String> linha : linhas) {
            contexto.getLote().adicionarItem(ItemLote.de(
                    linha.get("codigo"),
                    linha.get("tamanho"),
                    linha.get("cor"),
                    Integer.parseInt(linha.get("quantidade"))));
        }
    }
}
