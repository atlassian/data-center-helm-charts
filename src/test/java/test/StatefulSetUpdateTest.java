package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import com.fasterxml.jackson.databind.JsonNode;
import test.model.Product;
import test.model.StatefulSet;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static test.jackson.JsonNodeAssert.assertThat;

public class StatefulSetUpdateTest {

    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void sts_on_delete_update(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "statefulSet.updateStrategy.type", "OnDelete"
        ));
        StatefulSet sts = resources.getStatefulSet(product.getHelmReleaseName());
        JsonNode updateStrategy = sts.getSpec().path("updateStrategy").path("type");
        assertEquals("OnDelete", updateStrategy.asText());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void sts_on_not_set(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of());
        StatefulSet sts = resources.getStatefulSet(product.getHelmReleaseName());
        JsonNode updateStrategy = sts.getSpec().path("updateStrategy").path("type");
        assertThat(updateStrategy).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"}, mode = EnumSource.Mode.INCLUDE)
    void sts_on_delete_update_synchrony(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "statefulSet.updateStrategy.type", "OnDelete",
                "synchrony.enabled", "true"
        ));
        StatefulSet sts = resources.getStatefulSet(product.getHelmReleaseName() + "-synchrony");
        JsonNode updateStrategy = sts.getSpec().path("updateStrategy").path("type");
        assertEquals("OnDelete", updateStrategy.asText());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"}, mode = EnumSource.Mode.INCLUDE)
    void sts_on_not_set_synchrony(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true"
        ));
        StatefulSet sts = resources.getStatefulSet(product.getHelmReleaseName() + "-synchrony");
        JsonNode updateStrategy = sts.getSpec().path("updateStrategy").path("type");
        assertThat(updateStrategy).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket"}, mode = EnumSource.Mode.INCLUDE)
    void sts_on_delete_update_mesh(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "statefulSet.updateStrategy.type", "OnDelete",
                "bitbucket.mesh.enabled", "true"
        ));
        StatefulSet sts = resources.getStatefulSet(product.getHelmReleaseName() + "-mesh");
        JsonNode updateStrategy = sts.getSpec().path("updateStrategy").path("type");
        assertEquals("OnDelete", updateStrategy.asText());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket"}, mode = EnumSource.Mode.INCLUDE)
    void sts_on_not_set_mesh(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "bitbucket.mesh.enabled", "true"
        ));
        StatefulSet sts = resources.getStatefulSet(product.getHelmReleaseName() + "-mesh");
        JsonNode updateStrategy = sts.getSpec().path("updateStrategy").path("type");
        assertThat(updateStrategy).isEmpty();
    }
}
