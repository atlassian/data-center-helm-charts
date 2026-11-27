package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.*;

import java.util.Map;

import static test.jackson.JsonNodeAssert.assertThat;

public class HorizontalPodAutoscalerTest {

    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void autoscaling_enabled_default_metrics(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "autoscaling.enabled", "true"
        ));

        KubeResource hpa = resources.get(Kind.HorizontalPodAutoscaler, product.getHelmReleaseName());
        assertThat(hpa.getSpec().get("minReplicas")).hasValueEqualTo(1);
        assertThat(hpa.getSpec().get("maxReplicas")).hasValueEqualTo(5);
        assertThat(hpa.getSpec().get("scaleTargetRef").get("kind")).hasTextEqualTo("StatefulSet");
        assertThat(hpa.getSpec().get("scaleTargetRef").get("name")).hasTextEqualTo(product.getHelmReleaseName());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void autoscaling_custom_replicas_and_metrics(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "autoscaling.enabled", "true",
                "autoscaling.minReplicas", "2",
                "autoscaling.maxReplicas", "10",
                "autoscaling.targetCPUUtilizationPercentage", "75",
                "autoscaling.targetMemoryUtilizationPercentage", "80"
        ));

        KubeResource hpa = resources.get(Kind.HorizontalPodAutoscaler, product.getHelmReleaseName());
        assertThat(hpa.getSpec().get("minReplicas")).hasValueEqualTo(2);
        assertThat(hpa.getSpec().get("maxReplicas")).hasValueEqualTo(10);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void autoscaling_annotations_and_labels(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "autoscaling.enabled", "true",
                "autoscaling.annotations.foo", "bar",
                "autoscaling.labels.test", "custom"
        ));

        KubeResource hpa = resources.get(Kind.HorizontalPodAutoscaler, product.getHelmReleaseName());
        assertThat(hpa.getAnnotations().path("foo")).hasTextEqualTo("bar");
        assertThat(hpa.getMetadata().path("labels").path("test")).hasTextEqualTo("custom");
    }
}
