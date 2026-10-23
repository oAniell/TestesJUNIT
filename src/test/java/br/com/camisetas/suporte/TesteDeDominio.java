package br.com.camisetas.suporte;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Anotação personalizada (meta-anotação) que combina {@link Test} com a tag
 * {@code dominio}.
 *
 * <p>Permite executar somente os testes de regra de negócio com
 * {@code mvn test -Dgroups=dominio}, sem precisar repetir {@code @Test @Tag("dominio")}
 * em cada método.</p>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Tag("dominio")
@Test
public @interface TesteDeDominio {
}
