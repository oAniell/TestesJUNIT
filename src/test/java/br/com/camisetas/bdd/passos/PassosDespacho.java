package br.com.camisetas.bdd.passos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;

import br.com.camisetas.logistica.Despacho;

/**
 * Passos (step definitions) da funcionalidade de preparação do lote para despacho.
 */
public class PassosDespacho {

    private final ContextoProducao contexto;

    public PassosDespacho(ContextoProducao contexto) {
        this.contexto = contexto;
    }

    // ------------------------------------------------------------------
    // Dado / Quando
    // ------------------------------------------------------------------

    @Dado("que a expedicao recebeu o lote faturado")
    @Quando("a expedicao recebeu o lote faturado")
    public void queAExpedicaoRecebeuOLoteFaturado() {
        contexto.setDespacho(new Despacho(contexto.getNotaFiscal()));
    }

    @Quando("a expedicao gera o codigo de validacao do lote")
    public void aExpedicaoGeraOCodigoDeValidacao() {
        contexto.getDespacho().gerarCodigoValidacao();
    }

    @Quando("a expedicao embala o lote")
    public void aExpedicaoEmbalaOLote() {
        contexto.getDespacho().embalar();
    }

    @Quando("a expedicao marca o lote como preparado para despacho")
    public void aExpedicaoMarcaOLoteComoPreparado() {
        contexto.getDespacho().marcarComoPreparadoParaDespacho();
    }

    @Quando("a expedicao prepara o lote para envio")
    public void aExpedicaoPreparaOLoteParaEnvio() {
        contexto.getDespacho().prepararParaEnvio();
    }

    @Quando("a expedicao tenta gerar novamente o codigo de validacao do lote")
    public void aExpedicaoTentaGerarNovamenteOCodigo() {
        contexto.executarCapturandoErro(() -> contexto.getDespacho().gerarCodigoValidacao());
    }

    @Quando("a expedicao tenta embalar o lote")
    public void aExpedicaoTentaEmbalarOLote() {
        contexto.executarCapturandoErro(() -> contexto.getDespacho().embalar());
    }

    @Quando("a expedicao tenta marcar o lote como preparado para despacho")
    public void aExpedicaoTentaMarcarOLoteComoPreparado() {
        contexto.executarCapturandoErro(() -> contexto.getDespacho().marcarComoPreparadoParaDespacho());
    }

    @Quando("a expedicao tenta emitir a etiqueta de envio")
    public void aExpedicaoTentaEmitirAEtiquetaDeEnvio() {
        contexto.executarCapturandoErro(() -> contexto.getDespacho().getEtiquetaEnvio());
    }

    // ------------------------------------------------------------------
    // Entao
    // ------------------------------------------------------------------

    @Entao("um codigo de validacao deve ser gerado para o lote")
    public void umCodigoDeValidacaoDeveSerGerado() {
        assertNotNull(contexto.getDespacho().getCodigoValidacao(),
                "o codigo de validacao nao foi gerado");
    }

    @Entao("nenhum codigo de validacao deve ter sido gerado")
    public void nenhumCodigoDeValidacaoDeveTerSidoGerado() {
        assertNull(contexto.getDespacho().getCodigoValidacao());
    }

    @Entao("o codigo de validacao deve seguir o padrao {string}")
    public void oCodigoDeValidacaoDeveSeguirOPadrao(String padrao) {
        String codigo = contexto.getDespacho().getCodigoValidacao();
        assertTrue(codigo.matches(padrao),
                () -> "Codigo '" + codigo + "' nao corresponde ao padrao '" + padrao + "'");
    }

    @Entao("o lote deve estar embalado")
    public void oLoteDeveEstarEmbalado() {
        assertTrue(contexto.getDespacho().isEmbalado());
    }

    @Entao("o lote nao deve estar embalado")
    public void oLoteNaoDeveEstarEmbalado() {
        assertFalse(contexto.getDespacho().isEmbalado());
    }

    @Entao("o lote deve estar marcado como preparado para despacho")
    public void oLoteDeveEstarMarcadoComoPreparado() {
        assertTrue(contexto.getDespacho().isPreparadoParaDespacho());
    }

    @Entao("o lote nao deve estar marcado como preparado para despacho")
    public void oLoteNaoDeveEstarMarcadoComoPreparado() {
        assertFalse(contexto.getDespacho().isPreparadoParaDespacho());
    }

    @Entao("a data de preparacao do despacho deve ser a data de hoje")
    public void aDataDePreparacaoDeveSerHoje() {
        assertEquals(LocalDate.now(), contexto.getDespacho().getDataPreparacao());
    }

    @Entao("a etiqueta de envio deve conter {string}")
    public void aEtiquetaDeEnvioDeveConter(String trecho) {
        String etiqueta = contexto.getDespacho().getEtiquetaEnvio();
        assertTrue(etiqueta.contains(trecho),
                () -> "Trecho ausente na etiqueta. Esperado: " + trecho + " | Etiqueta: " + etiqueta);
    }

    @Entao("a etiqueta de envio deve conter o codigo de validacao do lote")
    public void aEtiquetaDeveConterOCodigoDeValidacao() {
        Despacho despacho = contexto.getDespacho();
        assertTrue(despacho.getEtiquetaEnvio().contains(despacho.getCodigoValidacao()));
    }
}
