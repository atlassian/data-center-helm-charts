package test;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Product;

import java.util.Map;

import static test.jackson.JsonNodeAssert.assertThat;

class TopologySpreadConstraintsTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void test_topology_constraint_on_pods_in_statefulset(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".topologySpreadConstraints[0].maxSkew", "1",
                product + ".topologySpreadConstraints[0].topologyKey", "kubernetes.io/hostname",
                product + ".topologySpreadConstraints[0].whenUnsatisfiable", "ScheduleAnyway",
                product + ".topologySpreadConstraints[0].labelSelector.matchLabels.myLabel", "mySelector"));


        JsonNode topologySpreadConstraints = resources.getStatefulSet(product.getHelmReleaseName())
                .getPodSpec()
                .get("topologySpreadConstraints");
        assertThat(topologySpreadConstraints).isArrayWithNumberOfChildren(1);
        assertThat(topologySpreadConstraints.get(0).get("maxSkew")).hasValueEqualTo(1);
        assertThat(topologySpreadConstraints.get(0).get("topologyKey")).hasTextEqualTo("kubernetes.io/hostname");
        assertThat(topologySpreadConstraints.get(0).get("whenUnsatisfiable")).hasTextContaining("ScheduleAnyway");
        assertThat(topologySpreadConstraints.get(0).get("labelSelector").get("matchLabels").get("myLabel")).hasTextContaining("mySelector");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.INCLUDE)
    void test_topology_constraints_on_pods_in_agent_deployment(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "agent.topologySpreadConstraints[0].maxSkew", "1",
                "agent.topologySpreadConstraints[0].topologyKey", "kubernetes.io/hostname",
                "agent.topologySpreadConstraints[0].whenUnsatisfiable", "ScheduleAnyway",
                "agent.topologySpreadConstraints[0].labelSelector.matchLabels.myLabel", "mySelector"));


        JsonNode topologySpreadConstraints = resources.getDeployment(product.getHelmReleaseName())
                .getPodSpec()
                .get("topologySpreadConstraints");
        assertThat(topologySpreadConstraints).isArrayWithNumberOfChildren(1);
        assertThat(topologySpreadConstraints.get(0).get("maxSkew")).hasValueEqualTo(1);
        assertThat(topologySpreadConstraints.get(0).get("topologyKey")).hasTextEqualTo("kubernetes.io/hostname");
        assertThat(topologySpreadConstraints.get(0).get("whenUnsatisfiable")).hasTextContaining("ScheduleAnyway");
        assertThat(topologySpreadConstraints.get(0).get("labelSelector").path("matchLabels").path("myLabel")).hasTextContaining("mySelector");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void test_default_topology_doesnt_exist_in_statefulset(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of());

        assertThat(resources.getStatefulSet(product.getHelmReleaseName())
                .getPodSpec()
                .get("topologySpreadConstraints")).isNull();
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.INCLUDE)
    void test_default_topology_doesnt_exist_in_deployment(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of());

        assertThat(resources.getDeployment(product.getHelmReleaseName())
                .getPodSpec()
                .get("topologySpreadConstraints")).isNull();
    }
}
