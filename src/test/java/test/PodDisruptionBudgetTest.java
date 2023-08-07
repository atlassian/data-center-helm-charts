package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.*;

import java.util.Map;

import static test.jackson.JsonNodeAssert.assertThat;

public class PodDisruptionBudgetTest {

    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void pod_disruption_budget_enabled_min_available(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "podDisruptionBudget.enabled", "true",
                "podDisruptionBudget.minAvailable", "1"
        ));

        KubeResource pdb = resources.get(Kind.PodDisruptionBudget, product.getHelmReleaseName());
        assertThat(pdb.getSpec().get("minAvailable")).hasValueEqualTo(1);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void pod_disruption_budget_min_available_max_unavailable(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "podDisruptionBudget.enabled", "true",
                "podDisruptionBudget.minAvailable", "1",
                "podDisruptionBudget.maxUnavailable", "2"
        ));

        // assert that when both mixAvailable and maxUnavailable are defined, only maxUnavailable is set in pdb
        KubeResource pdb = resources.get(Kind.PodDisruptionBudget, product.getHelmReleaseName());
        assertThat(pdb.getSpec().get("maxUnavailable")).hasValueEqualTo(2);
        assertThat(pdb.getSpec().path("minAvailable")).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void pod_disruption_budget_annotations(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "podDisruptionBudget.enabled", "true",
                "podDisruptionBudget.minAvailable", "1",
                "podDisruptionBudget.annotations.foo", "bar"
        ));

        KubeResource pdb = resources.get(Kind.PodDisruptionBudget, product.getHelmReleaseName());
        assertThat(pdb.getAnnotations().path("foo")).hasTextEqualTo("bar");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void pod_disruption_budget_labels(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "podDisruptionBudget.enabled", "true",
                "podDisruptionBudget.minAvailable", "1",
                "podDisruptionBudget.labels.foo", "bar"
        ));

        KubeResource pdb = resources.get(Kind.PodDisruptionBudget, product.getHelmReleaseName());
        assertThat(pdb.getMetadata().path("labels").path("foo")).hasTextEqualTo("bar");
    }
}
