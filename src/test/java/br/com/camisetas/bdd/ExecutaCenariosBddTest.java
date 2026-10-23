package br.com.camisetas.bdd;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Runner dos cenários BDD.
 *
 * <p>Usa o JUnit Platform Suite para acionar o engine do Cucumber e executar
 * todos os arquivos {@code .feature} do diretório
 * {@code src/test/resources/br/com/camisetas/bdd/features}.</p>
 *
 * <p>O mapeamento dos passos (glue) e os relatórios gerados estão configurados em
 * {@code src/test/resources/junit-platform.properties}.</p>
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("br/com/camisetas/bdd/features")
public class ExecutaCenariosBddTest {
}
