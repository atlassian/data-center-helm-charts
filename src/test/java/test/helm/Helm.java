package test.helm;

import io.vavr.collection.HashMap;
import org.junit.jupiter.api.TestInfo;
import test.model.KubeResources;
import test.model.Product;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A wrapper around the helm CLI tool, which we use for rendering a chart's templates and capturing the output.
 */
public final class Helm {
    private final TestInfo testInfo;

    public Helm(TestInfo testInfo) {
        this.testInfo = testInfo;
    }

    public Path captureHelmTemplateOutput(Product product, Path valuesFile) throws Exception {
        final var outputFile = getHelmTemplateOutputFile(product);
        captureHelmTemplateOutput(product, valuesFile, outputFile);
        return outputFile;
    }

    public void assertLintedHelm(Product product) throws Exception {
        final var process = new ProcessBuilder()
                .command("helm", "lint",
                        getHelmChartPath(product).toString())
                .start();
        final var exitCode = process.waitFor();
        if (exitCode != 0) {
            System.out.println(new String(process.getInputStream().readAllBytes()));
        }
        assertThat(exitCode).isEqualTo(0);
    }

    private static void captureHelmTemplateOutput(Product product, Path valuesFile, Path outputFile) throws Exception {
        final var process = new ProcessBuilder()
                .command("helm", "template",
                        product.getHelmReleaseName(),
                        getHelmChartPath(product).toString(),
                        "--debug",
                        "-n mynamespace",
                        "--values",
                        valuesFile.toString())
                .redirectOutput(outputFile.toFile())
                .redirectError(outputFile.toFile())
                .start();
        final var exitCode = process.waitFor();
        assertThat(exitCode).isEqualTo(0);
    }

    public KubeResources captureKubeResourcesFromHelmChart(Product product, Map<String, String> values) throws Exception {
        final var outputFile = getHelmTemplateOutputFile(product);
        captureHelmTemplateOutput(product, outputFile, values);
        return KubeResources.parse(outputFile);
    }

    private static void captureHelmTemplateOutput(Product product, Path outputFile, Map<String, String> values) throws Exception {
        final var process = new ProcessBuilder()
                .command("helm", "template",
                        product.getHelmReleaseName(),
                        getHelmChartPath(product).toString(),
                        "--debug",
                        "-n mynamespace",
                        "--set",
                        HashMap.ofAll(values).map(pair -> pair.toSeq().mkString("=")).mkString(","))
                .redirectOutput(outputFile.toFile())
                .redirectError(outputFile.toFile())
                .start();
        final var exitCode = process.waitFor();

        assertThat(exitCode)
                .withFailMessage(() -> {
                    try {
                        return String.join("\n", Files.readAllLines(outputFile));
                    } catch (IOException e) {
                        return e.getMessage();
                    }
                }).isEqualTo(0);
    }

    public static String getHelmReleaseName(Product product) {
        return String.format("unittest-%s", product);
    }

    private static Path getHelmChartPath(Product product) {
        return Paths.get(String.format("src/main/charts/%s", product));
    }

    private Path getHelmTemplateOutputFile(Product product) throws IOException {
        final var dir = Paths.get("target/test-output");
        Files.createDirectories(dir);
        return dir.resolve(String.format("%s_%s.yaml",
                testInfo.getTestMethod().map(Method::getName).orElse(""),
                product));
    }
}
