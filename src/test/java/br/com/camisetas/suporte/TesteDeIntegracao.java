package br.com.camisetas.suporte;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Anotação personalizada para os testes que percorrem o fluxo completo
 * (Lote → ItemLote → NotaFiscal → Despacho).
 *
 * <p>Equivalente a {@code @Test @Tag("integracao")}.</p>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Tag("integracao")
@Test
public @interface TesteDeIntegracao {
}
