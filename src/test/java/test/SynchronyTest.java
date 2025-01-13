package test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static test.jackson.JsonNodeAssert.assertThat;

class SynchronyTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void synchrony_enable(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "ingress.host", "atlassian.net",
                "ingress.path", "confluence",
                "ingress.https", "true"
        ));

        resources.assertContains(Kind.StatefulSet, product.getHelmReleaseName() + "-synchrony");
        resources.assertContains(Kind.Service, product.getHelmReleaseName() + "-synchrony");

        final var sysProps = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-jvm-config")
                .getNode("data", "additional_jvm_args");

        assertThat(sysProps)
                .hasTextContaining("-Dsynchrony.service.url=https://atlassian.net/synchrony/v1")
                .hasTextNotContaining("synchrony.btf.disabled");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void synchrony_disabled(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "false"
        ));

        final var sysProps = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-jvm-config")
                .getNode("data", "additional_jvm_args");

        assertThat(sysProps).hasTextContaining("synchrony.btf.disabled");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void synchrony_entrypoint(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true"
        ));

        final var entrypoint = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-synchrony-entrypoint")
                .getNode("data", "start-synchrony.sh");

        assertThat(entrypoint)
                .hasTextContaining("-Xss2048k")
                .hasTextContaining("-Xms1g")
                .hasTextContaining("-Xmx2g")
                .hasTextContaining("-XX:ActiveProcessorCount=2");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void synchrony_small_cpu_request(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "synchrony.resources.container.requests.cpu", "20m"
        ));

        final var entrypoint = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-synchrony-entrypoint")
                .getNode("data", "start-synchrony.sh");

        assertThat(entrypoint)
                .hasTextContaining("-XX:ActiveProcessorCount=1");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void synchrony_custom_cpu_request(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "synchrony.resources.container.requests.cpu", "5"
        ));

        final var entrypoint = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-synchrony-entrypoint")
                .getNode("data", "start-synchrony.sh");

        assertThat(entrypoint)
                .hasTextContaining("-XX:ActiveProcessorCount=5");
    }
    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void synchrony_replica_count(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "synchrony.replicaCount", "2"
        ));

        StatefulSet synchronySts = resources.getStatefulSet(product.getHelmReleaseName() + "-synchrony");

        IntNode expectedReplicas = new IntNode(2);

        assertThat(synchronySts.getSpec().path("replicas")).isEqualTo(expectedReplicas);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void synchrony_additionalJvmArgs(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "synchrony.additionalJvmArgs[0]", "-Dfoo=1",
                "synchrony.additionalJvmArgs[1]", "-Dbar=2"
        ));

        final var entrypoint = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-synchrony-entrypoint")
                .getNode("data", "start-synchrony.sh");

        assertThat(entrypoint)
                .hasSeparatedTextContaining("-Dfoo=1")
                .hasSeparatedTextContaining("-Dbar=2");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void synchrony_resources(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "synchrony.resources.container.requests.cpu", "10",
                "synchrony.resources.container.requests.memory", "10GB",
                "synchrony.resources.container.limits.cpu", "20",
                "synchrony.resources.container.limits.memory", "20GB"
        ));

        StatefulSet synchronySts = resources.getStatefulSet(product.getHelmReleaseName() + "-synchrony");

        // verify requests
        assertThat(synchronySts.getContainer("synchrony").getRequests().path("cpu")).hasValueEqualTo(10);
        assertThat(synchronySts.getContainer("synchrony").getRequests().path("memory")).hasTextEqualTo("10GB");

        // verify limits
        assertThat(synchronySts.getContainer("synchrony").getLimits().path("cpu")).hasValueEqualTo(20);
        assertThat(synchronySts.getContainer("synchrony").getLimits().path("memory")).hasTextEqualTo("20GB");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = "confluence")
    void synchrony_changesAnnotationChecksum(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true"
        ));

        final var metadata = resources.getStatefulSet(product.getHelmReleaseName() + "-synchrony").getPodMetadata();
        final var checksum = metadata.get("annotations").get("checksum/config-jvm");

        assertThat(checksum).isNotNull();

        final var resourcesWithChanges = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "synchrony.additionalJvmArgs[0]", "-Dfoo=1"));

        final var metadataWithChanges = resourcesWithChanges.getStatefulSet(product.getHelmReleaseName() + "-synchrony").getPodMetadata();
        final var checksumWithChanges = metadataWithChanges.get("annotations").get("checksum/config-jvm");

        assertThat(checksumWithChanges)
                .isNotNull()
                .isNotEqualTo(checksum);
    }

    @Test
    void synchrony_custom_ports_are_include_in_jvm_args() throws Exception {
        Product product = Product.confluence;
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "synchrony.ports.http", "1234",
                "synchrony.ports.hazelcast", "9876"
        ));

        JsonNode startScript = resources.get(
                Kind.ConfigMap,
                product.getHelmReleaseName() + "-synchrony-entrypoint").getNode("data").path("start-synchrony.sh");

        assertThat(startScript).hasTextContaining("-Dsynchrony.port=1234");
        assertThat(startScript).hasTextContaining("-Dcluster.listen.port=9876");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"}, mode = EnumSource.Mode.INCLUDE)
    void synchrony_custom_node_selector(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "synchrony.nodeSelector.nodename", "special-node"
        ));
        StatefulSet synchronySts = resources.getStatefulSet(product.getHelmReleaseName() + "-synchrony");
        assertThat(synchronySts.getPodSpec().path("nodeSelector").path("nodename")).hasTextEqualTo("special-node");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"}, mode = EnumSource.Mode.INCLUDE)
    void synchrony_confluence_node_selector(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "nodeSelector.nodename", "special-node"
        ));
        StatefulSet synchronySts = resources.getStatefulSet(product.getHelmReleaseName() + "-synchrony");
        assertThat(synchronySts.getPodSpec().path("nodeSelector").path("nodename")).hasTextEqualTo("special-node");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"}, mode = EnumSource.Mode.INCLUDE)
    void synchrony_custom_scheduler_name(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "synchrony.schedulerName", "my-scheduler"));
        StatefulSet synchronySts = resources.getStatefulSet(product.getHelmReleaseName() + "-synchrony");
        assertEquals("my-scheduler", synchronySts.getPodSpec().path("schedulerName").asText());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"}, mode = EnumSource.Mode.INCLUDE)
    void synchrony_confluence_scheduler_name(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "schedulerName", "my-scheduler1"));
        StatefulSet synchronySts = resources.getStatefulSet(product.getHelmReleaseName() + "-synchrony");
        assertEquals("my-scheduler1", synchronySts.getPodSpec().path("schedulerName").asText());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"}, mode = EnumSource.Mode.INCLUDE)
    void synchrony_custom_tolerations(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "synchrony.tolerations[0].key", "other-pod",
                "synchrony.tolerations[0].operator", "Exists",
                "synchrony.tolerations[0].effect", "NoSchedule"));

        StatefulSet synchronySts = resources.getStatefulSet(product.getHelmReleaseName() + "-synchrony");
        JsonNode tolerations = synchronySts.getPodSpec().get("tolerations");
        assertThat(tolerations).isArrayWithNumberOfChildren(1);
        assertThat(tolerations.get(0).get("key")).hasTextEqualTo("other-pod");
        assertThat(tolerations.get(0).get("operator")).hasTextEqualTo("Exists");
        assertThat(tolerations.get(0).get("effect")).hasTextContaining("NoSchedule");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"}, mode = EnumSource.Mode.INCLUDE)
    void synchrony_confluence_tolerations(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "tolerations[0].key", "another-pod",
                "tolerations[0].operator", "Exists",
                "tolerations[0].effect", "NoSchedule"));

        StatefulSet synchronySts = resources.getStatefulSet(product.getHelmReleaseName() + "-synchrony");
        JsonNode tolerations = synchronySts.getPodSpec().get("tolerations");
        assertThat(tolerations).isArrayWithNumberOfChildren(1);
        assertThat(tolerations.get(0).get("key")).hasTextEqualTo("another-pod");
        assertThat(tolerations.get(0).get("operator")).hasTextEqualTo("Exists");
        assertThat(tolerations.get(0).get("effect")).hasTextContaining("NoSchedule");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"}, mode = EnumSource.Mode.INCLUDE)
    void synchrony_custom_affinity(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "synchrony.affinity.nodeAffinity.requiredDuringSchedulingIgnoredDuringExecution.nodeSelectorTerms[0].matchExpressions[0].key", "kubernetes.io/os",
                "synchrony.affinity.nodeAffinity.requiredDuringSchedulingIgnoredDuringExecution.nodeSelectorTerms[0].matchExpressions[0].operator", "in",
                "synchrony.affinity.nodeAffinity.requiredDuringSchedulingIgnoredDuringExecution.nodeSelectorTerms[0].matchExpressions[0].values[0]", "centos"));
        StatefulSet synchronySts = resources.getStatefulSet(product.getHelmReleaseName() + "-synchrony");
        JsonNode affinity = synchronySts.getPodSpec().get("affinity");
        assertThat(affinity.path("nodeAffinity").path("requiredDuringSchedulingIgnoredDuringExecution").path("nodeSelectorTerms").get(0).path("matchExpressions").get(0).path("values").get(0)).hasTextEqualTo("centos");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"}, mode = EnumSource.Mode.INCLUDE)
    void synchrony_confluence_affinity(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "affinity.nodeAffinity.requiredDuringSchedulingIgnoredDuringExecution.nodeSelectorTerms[0].matchExpressions[0].key", "kubernetes.io/os",
                "affinity.nodeAffinity.requiredDuringSchedulingIgnoredDuringExecution.nodeSelectorTerms[0].matchExpressions[0].operator", "in",
                "affinity.nodeAffinity.requiredDuringSchedulingIgnoredDuringExecution.nodeSelectorTerms[0].matchExpressions[0].values[0]", "ubuntu"));
        StatefulSet synchronySts = resources.getStatefulSet(product.getHelmReleaseName() + "-synchrony");
        JsonNode affinity = synchronySts.getPodSpec().get("affinity");
        assertThat(affinity.path("nodeAffinity").path("requiredDuringSchedulingIgnoredDuringExecution").path("nodeSelectorTerms").get(0).path("matchExpressions").get(0).path("values").get(0)).hasTextEqualTo("ubuntu");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"}, mode = EnumSource.Mode.INCLUDE)
    void synchrony_custom_priority_class_names(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "synchrony.priorityClassName", "high"
        ));
        StatefulSet synchronySts = resources.getStatefulSet(product.getHelmReleaseName() + "-synchrony");
        JsonNode priorityClassName = synchronySts.getPodSpec().get("priorityClassName");
        assertEquals("high", priorityClassName.asText());
    }
    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"}, mode = EnumSource.Mode.INCLUDE)
    void synchrony_confluence_priority_class_names(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "priorityClassName", "high"
        ));
        StatefulSet synchronySts = resources.getStatefulSet(product.getHelmReleaseName() + "-synchrony");
        JsonNode priorityClassName = synchronySts.getPodSpec().get("priorityClassName");
        assertEquals("high", priorityClassName.asText());
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"}, mode = EnumSource.Mode.INCLUDE)
    void synchrony_custom_topology_constraints(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "synchrony.topologySpreadConstraints[0].maxSkew", "1",
                "synchrony.topologySpreadConstraints[0].topologyKey", "kubernetes.io/hostname",
                "synchrony.topologySpreadConstraints[0].whenUnsatisfiable", "ScheduleAnyway",
                "synchrony.topologySpreadConstraints[0].labelSelector.matchLabels.yourLabel", "yourSelector"));
        JsonNode topologySpreadConstraints = resources.getStatefulSet(product.getHelmReleaseName() + "-synchrony")
                .getPodSpec()
                .get("topologySpreadConstraints");

        assertThat(topologySpreadConstraints).isArrayWithNumberOfChildren(1);
        assertThat(topologySpreadConstraints.get(0).get("maxSkew")).hasValueEqualTo(1);
        assertThat(topologySpreadConstraints.get(0).get("topologyKey")).hasTextEqualTo("kubernetes.io/hostname");
        assertThat(topologySpreadConstraints.get(0).get("whenUnsatisfiable")).hasTextContaining("ScheduleAnyway");
        assertThat(topologySpreadConstraints.get(0).get("labelSelector").get("matchLabels").get("yourLabel")).hasTextContaining("yourSelector");
    }
    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"}, mode = EnumSource.Mode.INCLUDE)
    void synchrony_confluence_topology_constraints(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                product + ".topologySpreadConstraints[0].maxSkew", "1",
                product + ".topologySpreadConstraints[0].topologyKey", "kubernetes.io/hostname",
                product + ".topologySpreadConstraints[0].whenUnsatisfiable", "ScheduleAnyway",
                product + ".topologySpreadConstraints[0].labelSelector.matchLabels.myLabel", "mySelector"));


        JsonNode topologySpreadConstraints = resources.getStatefulSet(product.getHelmReleaseName() + "-synchrony")
                .getPodSpec()
                .get("topologySpreadConstraints");
        assertThat(topologySpreadConstraints).isArrayWithNumberOfChildren(1);
        assertThat(topologySpreadConstraints.get(0).get("maxSkew")).hasValueEqualTo(1);
        assertThat(topologySpreadConstraints.get(0).get("topologyKey")).hasTextEqualTo("kubernetes.io/hostname");
        assertThat(topologySpreadConstraints.get(0).get("whenUnsatisfiable")).hasTextContaining("ScheduleAnyway");
        assertThat(topologySpreadConstraints.get(0).get("labelSelector").get("matchLabels").get("myLabel")).hasTextContaining("mySelector");
    }
}
