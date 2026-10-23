package br.com.camisetas.integracao;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.camisetas.dominio.Cor;
import br.com.camisetas.dominio.ItemLote;
import br.com.camisetas.dominio.Lote;
import br.com.camisetas.dominio.Tamanho;
import br.com.camisetas.excecao.CapacidadeLoteExcedidaException;
import br.com.camisetas.fiscal.NotaFiscal;
import br.com.camisetas.logistica.Despacho;
import br.com.camisetas.suporte.TesteDeIntegracao;

/**
 * Teste de integração do fluxo completo de produção:
 * abertura do lote → cadastro de itens → emissão da nota fiscal → despacho.
 *
 * <p>Enquanto os testes unitários verificam cada classe isoladamente, este teste
 * garante que a colaboração entre {@link Lote}, {@link ItemLote},
 * {@link NotaFiscal} e {@link Despacho} produz o resultado esperado de ponta a ponta.</p>
 */
@DisplayName("Fluxo de producao: Lote -> ItemLote -> NotaFiscal -> Despacho")
class FluxoProducaoCamisetaTest {

    private static final LocalDate DATA_FABRICACAO = LocalDate.of(2024, 6, 1);

    @TesteDeIntegracao
    @DisplayName("Deve produzir, faturar e despachar um lote de 60 camisetas")
    void deveProduzirFaturarEDespacharLoteCompleto() {
        // ----- producao -----
        Lote lote = new Lote("LOTE-2024-050", DATA_FABRICACAO, 60)
                .adicionarItem(new ItemLote("IT-01", Tamanho.P, Cor.BRANCO, 10))
                .adicionarItem(new ItemLote("IT-02", Tamanho.M, Cor.PRETO, 20))
                .adicionarItem(new ItemLote("IT-03", Tamanho.G, Cor.AZUL, 30));

        // ----- faturamento -----
        NotaFiscal notaFiscal = new NotaFiscal("NF-2024-050", lote, DATA_FABRICACAO.plusDays(2));

        // ----- despacho -----
        Despacho despacho = new Despacho(notaFiscal);
        String codigoValidacao = despacho.prepararParaEnvio();

        assertAll("fluxo completo de producao",
                // lote
                () -> assertTrue(lote.isCompleto(), "o lote deve estar completo"),
                () -> assertEquals(3, lote.getQuantidadeDeItens()),
                () -> assertEquals(60, lote.getQuantidadeCadastrada()),
                // nota fiscal
                () -> assertEquals(60, notaFiscal.getQuantidadeTotal()),
                () -> assertEquals(10, notaFiscal.getQuantidadePorTamanho(Tamanho.P)),
                () -> assertEquals(20, notaFiscal.getQuantidadePorTamanho(Tamanho.M)),
                () -> assertEquals(30, notaFiscal.getQuantidadePorTamanho(Tamanho.G)),
                () -> assertEquals(10, notaFiscal.getQuantidadePorCor(Cor.BRANCO)),
                () -> assertEquals(20, notaFiscal.getQuantidadePorCor(Cor.PRETO)),
                () -> assertEquals(30, notaFiscal.getQuantidadePorCor(Cor.AZUL)),
                // despacho
                () -> assertNotNull(codigoValidacao),
                () -> assertTrue(despacho.isEmbalado()),
                () -> assertTrue(despacho.isPreparadoParaDespacho()),
                () -> assertTrue(despacho.getEtiquetaEnvio().contains(codigoValidacao)));
    }

    @TesteDeIntegracao
    @DisplayName("Lote parcialmente produzido deve ser faturado e despachado pelo que foi cadastrado")
    void deveFaturarEDespacharLoteParcial() {
        Lote lote = new Lote("LOTE-2024-051", DATA_FABRICACAO, 100)
                .adicionarItem(new ItemLote("IT-01", Tamanho.M, Cor.AZUL, 40));

        NotaFiscal notaFiscal = NotaFiscal.emitirPara(lote);
        Despacho despacho = new Despacho(notaFiscal);
        despacho.prepararParaEnvio();

        assertAll("lote parcial",
                () -> assertFalse(lote.isCompleto()),
                () -> assertEquals(60, lote.getQuantidadeRestante()),
                () -> assertEquals(40, notaFiscal.getQuantidadeTotal()),
                () -> assertEquals(0, notaFiscal.getQuantidadePorTamanho(Tamanho.P)),
                () -> assertTrue(despacho.isPreparadoParaDespacho()));
    }

    @TesteDeIntegracao
    @DisplayName("Producao acima da capacidade deve ser bloqueada antes do faturamento")
    void deveBloquearProducaoAcimaDaCapacidade() {
        Lote lote = new Lote("LOTE-2024-052", DATA_FABRICACAO, 30)
                .adicionarItem(new ItemLote("IT-01", Tamanho.P, Cor.BRANCO, 25));

        CapacidadeLoteExcedidaException erro = assertThrows(CapacidadeLoteExcedidaException.class,
                () -> lote.adicionarItem(new ItemLote("IT-02", Tamanho.G, Cor.PRETO, 10)));

        NotaFiscal notaFiscal = NotaFiscal.emitirPara(lote);

        assertAll(
                () -> assertEquals(30, erro.getCapacidade()),
                () -> assertEquals(35, erro.getQuantidadeSolicitada()),
                () -> assertEquals(25, notaFiscal.getQuantidadeTotal(),
                        "a nota reflete apenas o que foi efetivamente cadastrado"));
    }
}
