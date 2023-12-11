package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Product;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LifecycleHooksTest {

    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void default_pre_stop_hook(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of());
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        assertEquals("sh", statefulSet.getContainer().get("lifecycle").get("preStop").get("exec").get("command").get(0).asText());
        assertEquals("-c", statefulSet.getContainer().get("lifecycle").get("preStop").get("exec").get("command").get(1).asText());
        assertEquals("/shutdown-wait.sh", statefulSet.getContainer().get("lifecycle").get("preStop").get("exec").get("command").get(2).asText());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void custom_pre_stop_hook(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product.name() + ".shutdown.command", "echo hello"
        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        assertEquals("sh", statefulSet.getContainer().get("lifecycle").get("preStop").get("exec").get("command").get(0).asText());
        assertEquals("-c", statefulSet.getContainer().get("lifecycle").get("preStop").get("exec").get("command").get(1).asText());
        assertEquals("echo hello", statefulSet.getContainer().get("lifecycle").get("preStop").get("exec").get("command").get(2).asText());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void custom_post_start_hook(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product.name() + ".postStart.command", "echo hello"
        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        assertEquals("/bin/sh", statefulSet.getContainer().get("lifecycle").get("postStart").get("exec").get("command").get(0).asText());
        assertEquals("-c", statefulSet.getContainer().get("lifecycle").get("postStart").get("exec").get("command").get(1).asText());
        assertEquals("echo hello", statefulSet.getContainer().get("lifecycle").get("postStart").get("exec").get("command").get(2).asText());
    }
}
