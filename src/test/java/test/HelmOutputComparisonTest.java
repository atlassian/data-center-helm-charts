package test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class HelmOutputComparisonTest {

    /**
     * For each product, this test executes "helm template" and captures the output, comparing it against an expectation file.
     * his means that every time the Helm charts change, we need to change the expectation files.
     */
    @ParameterizedTest
    @ValueSource(strings = {"confluence", "jira", "bitbucket"})
    void helm_template_output_matches_expectations(final String product) throws Exception {
        final Path actualOutputFile = getHelmTemplateOutputFile(product);
        final Path expectedHelmOutput = getExpectedHelmTemplateOutputFile(product);

        captureHelmTemplateOutput(product, actualOutputFile);

        assertThat(expectedHelmOutput.toFile()).exists();
        assertThat(actualOutputFile.toFile()).exists();

        assertThat(actualOutputFile).hasSameTextualContentAs(expectedHelmOutput);
    }

    @ParameterizedTest
    @ValueSource(strings = {"confluence", "jira", "bitbucket"})
    void helm_lint(final String product) throws Exception {
        final Process process = new ProcessBuilder()
                .command("helm", "lint",
                        getHelmChartPath(product).toString(),
                        "--values",
                        getHelmValuesFile(product).toString())
                .start();
        final int exitCode = process.waitFor();
        assertThat(exitCode).isEqualTo(0);
    }

    private void captureHelmTemplateOutput(String product, Path outputFile) throws Exception {
        final Process process = new ProcessBuilder()
                .command("helm", "template",
                        getHelmReleaseName(product),
                        getHelmChartPath(product).toString(),
                        "--values",
                        getHelmValuesFile(product).toString())
                .redirectOutput(outputFile.toFile())
                .redirectError(outputFile.toFile())
                .start();
        final int exitCode = process.waitFor();
        assertThat(exitCode).isEqualTo(0);
    }

    private static Path testResources() {
        return Paths.get("src/test/resources");
    }

    private static Path expectationFiles(String product) {
        return testResources().resolve("expected_helm_output").resolve(product);
    }

    private static Path getExpectedHelmTemplateOutputFile(String product) {
        return expectationFiles(product).resolve("output.yaml");
    }

    private static Path getHelmValuesFile(String product) {
        return expectationFiles(product).resolve("values.yaml");
    }

    private static Path getHelmTemplateOutputFile(String product) {
        return Paths.get(String.format("build/helm_output_%s.yaml", product));
    }

    private static String getHelmReleaseName(String product) {
        return String.format("unittest-%s", product);
    }

    private static Path getHelmChartPath(String product) {
        return Paths.get(String.format("src/main/charts/%s", product));
    }
}
