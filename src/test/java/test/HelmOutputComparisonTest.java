package test;

import io.vavr.collection.Array;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.platform.commons.util.StringUtils;
import test.helm.Helm;
import test.model.Product;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class HelmOutputComparisonTest {

    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    /**
     * For each product, this test executes "helm template" and captures the output, comparing it against an expectation file.
     * This means that every time the Helm charts change, we need to change the expectation files.
     */
    @ParameterizedTest
    @EnumSource(Product.class)
    void helm_template_output_matches_expectations(final Product product) throws Exception {
        final var expectedHelmOutput = getExpectedHelmTemplateOutputFile(product);

        final var actualOutputFile = helm.captureHelmTemplateOutput(product, getHelmValuesFile(product));

        stripBlankLines(actualOutputFile);

        assertThat(expectedHelmOutput.toFile()).exists();
        assertThat(actualOutputFile.toFile()).exists();

        assertThat(actualOutputFile).hasSameTextualContentAs(expectedHelmOutput);
    }

    private static void stripBlankLines(Path file) throws IOException {
        Files.write(file, Array.ofAll(Files.readAllLines(file))
                .filter(StringUtils::isNotBlank));
    }

    private static Path testResources() {
        return Paths.get("src/test/resources");
    }

    private static Path expectationFiles(Product product) {
        return testResources().resolve("expected_helm_output").resolve(product.toString());
    }

    private static Path getExpectedHelmTemplateOutputFile(Product product) {
        return expectationFiles(product).resolve("output.yaml");
    }

    private static Path getHelmValuesFile(Product product) {
        return expectationFiles(product).resolve("values.yaml");
    }
}
