package test;

import com.fasterxml.jackson.databind.JsonNode;
import io.vavr.collection.Traversable;
import org.assertj.core.api.Assertions;
import org.assertj.vavr.api.VavrAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import test.helm.Helm;
import test.model.*;

import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static test.jackson.JsonNodeAssert.assertThat;
import static test.model.Kind.ConfigMap;

/**
 * Tests the various permutations of the "persistence" value structure in the
 * Helm charts
 */
class BitbucketMeshTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @Test
    void meshEnabledCreatesResources() throws Exception {
        final var product = Product.bitbucket;
        final var replicaCount = 2;
        final var resources = helm.captureKubeResourcesFromHelmChart(product,
                Map.of(product + ".mesh.enabled", "true",
                        product + ".mesh.replicaCount", String.valueOf(replicaCount)));

        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName() + "-mesh");
        final var env = statefulSet.getContainer().getEnv();

        assertEquals(replicaCount, statefulSet.getReplicas());
        env.assertHasValue("MESH_HOME", "/var/atlassian/application-data/mesh");

        // for each replica we have one service
        assertEquals(resources.getAll(Kind.Service).filter(s -> s.getName().contains("-mesh")).size(), replicaCount);
        resources.get(Kind.Service, product.getHelmReleaseName() + "-mesh-0");
        resources.get(Kind.Service, product.getHelmReleaseName() + "-mesh-1");
    }

    @Test
    void meshDisabledNoResources() throws Exception {
        final var product = Product.bitbucket;
        final var resources = helm.captureKubeResourcesFromHelmChart(product,
                Map.of(product + ".mesh.enabled", "false"));

        assertThrows(AssertionError.class, () -> {
            resources.getStatefulSet(product.getHelmReleaseName() + "-mesh");
        });

        assertEquals(resources.getAll(Kind.Service).filter(s -> s.getName().contains("-mesh")).size(), 0);
    }

    @Test
    void postInstallationHooks() throws Exception {
        final var product = Product.bitbucket;
        final var replicaCount = 3;
        final var resources = helm.captureKubeResourcesFromHelmChart(product,
                Map.of(product + ".mesh.enabled", "true",
                        product + ".mesh.nodeAutoRegistration", "true",
                        product + ".mesh.setByDefault", "true",
                        product + ".mesh.replicaCount", String.valueOf(replicaCount),
                        product + ".sysadminCredentials.secretName", "secret-name",
                        product + ".mesh.podAnnotations.annotation1", "mesh1",
                        product + ".mesh.podAnnotations.annotation2", "mesh2"
                        ));

        Traversable<KubeResource> all = resources.getAll(Kind.Job);

        KubeResource configureJob = resources.get(Kind.Job, product.getHelmReleaseName() + "-mesh-configure-job");
        assertEquals(configureJob.getAnnotations().path("helm.sh/hook").asText(), "post-install");
        Traversable<KubeResource> registerJobs = all.filter(j -> j.getName().contains("mesh-register-job"));
        assertEquals(replicaCount, registerJobs.size());
        for (var rJob : registerJobs) {
            assertEquals(rJob.getAnnotations().path("helm.sh/hook").asText(), "post-install");
            assertEquals(rJob.getSpec().path("template").path("metadata").path("annotations").path("annotation1").asText(), "mesh1");
            assertEquals(rJob.getSpec().path("template").path("metadata").path("annotations").path("annotation2").asText(), "mesh2");
        }
        // ensure current mesh node number in container args
        assertEquals(replicaCount -1, registerJobs.last().getSpec().path("template").path("spec").path("containers").get(0).path("args").get(0).asInt());
    }

    @Test
    void meshJVMConfig() throws Exception {
        final var product = Product.bitbucket;
        final var resources = helm.captureKubeResourcesFromHelmChart(product,
                Map.of(product + ".mesh.enabled", "true",
                        product + ".mesh.additionalJvmArgs[0]", "-Dhello=world",
                        product + ".mesh.additionalJvmArgs[1]", "-Dmesh=true"));

        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName() + "-mesh");
        final var env = statefulSet.getContainer().getEnv();
        env.assertHasConfigMapRef("JVM_MINIMUM_MEMORY", "unittest-bitbucket-jvm-config-mesh", "min_heap");
        env.assertHasConfigMapRef("JVM_MAXIMUM_MEMORY", "unittest-bitbucket-jvm-config-mesh", "max_heap");
        env.assertHasConfigMapRef("JVM_SUPPORT_RECOMMENDED_ARGS", "unittest-bitbucket-jvm-config-mesh", "additional_jvm_args");

        final KubeResource configMap = meshJvmConfigMap(resources);
        assertThat(configMap.getNode("data", "additional_jvm_args")).hasTextContaining("-Dhello=world");
        assertThat(configMap.getNode("data", "additional_jvm_args")).hasTextContaining("-Dmesh=true");
    }

    @Test
    void meshPodMetadata() throws Exception {
        final var product = Product.bitbucket;
        final var resources = helm.captureKubeResourcesFromHelmChart(product,
                Map.of(product + ".mesh.enabled", "true",
                        product + ".mesh.podAnnotations.podAnnotation1", "mesh1",
                        product + ".mesh.podAnnotations.podAnnotation2", "mesh2",
                        product + ".mesh.podLabels.podLabel1", "mesh1",
                        product + ".mesh.podLabels.podLabel2", "mesh2"
                ));

        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName() + "-mesh");
        final var annotations = statefulSet.getPodMetadata().get("annotations");
        final var labels = statefulSet.getPodMetadata().get("labels");
        assertThat(annotations).isObject(Map.of(
                "podAnnotation1", "mesh1",
                "podAnnotation2", "mesh2"
        ));
        assertThat(labels).isObject(Map.of(
                "podLabel1", "mesh1",
                "podLabel2", "mesh2"
        ));
    }

    @Test
    void meshEnvironmentVariables() throws Exception {
        final var product = Product.bitbucket;
        final var resources = helm.captureKubeResourcesFromHelmChart(product,
                Map.of(product + ".mesh.enabled", "true",
                        product + ".mesh.service.port", "9000",
                        product + ".mesh.additionalEnvironmentVariables[0].name", "GRPC_SERVER_ADDRESS",
                        product + ".mesh.additionalEnvironmentVariables[0].value", "127.0.0.1"
                ));

        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName() + "-mesh");
        statefulSet.getContainer("bitbucket-mesh").getEnv().assertHasValue("GRPC_SERVER_ADDRESS","127.0.0.1");
        statefulSet.getContainer("bitbucket-mesh").getEnv().assertHasValue("GRPC_SERVER_PORT","9000");
    }

    @Test
    void meshVolumeClaimTemplates() throws Exception {
        final var product = Product.bitbucket;
        final var resources = helm.captureKubeResourcesFromHelmChart(product,
                Map.of(product + ".mesh.enabled", "true",
                        product + ".mesh.volume.create", "true",
                        product + ".mesh.volume.storageClass", "gp2-mesh",
                        product + ".mesh.volume.resources.requests.storage", "100Gi"));

        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName() + "-mesh");

        assertThat(statefulSet.getVolumeClaimTemplates().get(0).path("metadata").path("name")).hasTextEqualTo("mesh-home");
        assertThat(statefulSet.getVolumeClaimTemplates().get(0).path("spec").path("resources").path("requests").path("storage")).hasTextEqualTo("100Gi");
        assertThat(statefulSet.getVolumeClaimTemplates().get(0).path("spec").path("storageClassName")).hasTextEqualTo("gp2-mesh");
    }

    @Test
    void meshAffinity() throws Exception {
        final var product = Product.bitbucket;
        final var resources = helm.captureKubeResourcesFromHelmChart(product,
                Map.of(product + ".mesh.enabled", "true",
                        product + ".sysadminCredentials.secretName", "secret-name",
                        product + ".mesh.nodeAutoRegistration", "true",
                        product + ".mesh.affinity.nodeAffinity.requiredDuringSchedulingIgnoredDuringExecution.nodeSelectorTerms[0].matchExpressions[0].key", "kubernetes.io/os",
                        product + ".mesh.affinity.nodeAffinity.requiredDuringSchedulingIgnoredDuringExecution.nodeSelectorTerms[0].matchExpressions[0].operator", "in",
                        product + ".mesh.affinity.nodeAffinity.requiredDuringSchedulingIgnoredDuringExecution.nodeSelectorTerms[0].matchExpressions[0].values[0]", "mesh-os"));

        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName() + "-mesh");
        KubeResource registerJob = resources.get(Kind.Job, product.getHelmReleaseName() + "-mesh-register-job-0");

        assertThat(statefulSet.getPodSpec().path("affinity").path("nodeAffinity").path("requiredDuringSchedulingIgnoredDuringExecution").path("nodeSelectorTerms").get(0).path("matchExpressions").get(0).path("values").get(0)).hasTextEqualTo("mesh-os");
        assertThat(registerJob.getSpec().path("template").path("spec").path("affinity").path("nodeAffinity").path("requiredDuringSchedulingIgnoredDuringExecution").path("nodeSelectorTerms").get(0).path("matchExpressions").get(0).path("values").get(0)).hasTextEqualTo("mesh-os");
    }

    @Test
    void meshTopologySpreadConstraints() throws Exception {
        final var product = Product.bitbucket;
        final var resources = helm.captureKubeResourcesFromHelmChart(product,
                Map.of(product + ".mesh.enabled", "true",
                        product + ".mesh.nodeAutoRegistration", "true",
                        product + ".sysadminCredentials.secretName", "secret-name",
                        product + ".mesh.topologySpreadConstraints[0].maxSkew", "1",
                        product + ".mesh.topologySpreadConstraints[0].topologyKey", "kubernetes.io/hostname",
                        product + ".mesh.topologySpreadConstraints[0].whenUnsatisfiable", "ScheduleAnyway",
                        product + ".mesh.topologySpreadConstraints[0].labelSelector.matchLabels.myLabel", "mySelector"));

        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName() + "-mesh");
        KubeResource registerJob = resources.get(Kind.Job, product.getHelmReleaseName() + "-mesh-register-job-0");

        JsonNode topologySpreadConstraints = statefulSet.getPodSpec()
                .get("topologySpreadConstraints");

        JsonNode jobTopologySpreadConstraints = registerJob.getSpec().path("template").path("spec").path("topologySpreadConstraints");
        JsonNode[] resourcesToCheck = {topologySpreadConstraints, jobTopologySpreadConstraints};
        for (var resource : resourcesToCheck) {
            assertThat(resource).isArrayWithNumberOfChildren(1);
            assertThat(resource.get(0).get("maxSkew")).hasValueEqualTo(1);
            assertThat(resource.get(0).get("topologyKey")).hasTextEqualTo("kubernetes.io/hostname");
            assertThat(resource.get(0).get("whenUnsatisfiable")).hasTextContaining("ScheduleAnyway");
            assertThat(resource.get(0).get("labelSelector").get("matchLabels").get("myLabel")).hasTextContaining("mySelector");
        }
    }

    @Test
    void meshTolerations() throws Exception {
        final var product = Product.bitbucket;
        final var resources = helm.captureKubeResourcesFromHelmChart(product,
                Map.of(product + ".mesh.enabled", "true",
                        product + ".mesh.nodeAutoRegistration", "true",
                        product + ".sysadminCredentials.secretName", "secret-name",
                        product + ".mesh.tolerations[0].key", "mesh",
                        product + ".mesh.tolerations[0].operator", "Exists",
                        product + ".mesh.tolerations[0].effect", "NoSchedule"));

        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName() + "-mesh");
        KubeResource registerJob = resources.get(Kind.Job, product.getHelmReleaseName() + "-mesh-register-job-0");

        JsonNode tolerations = statefulSet.getPodSpec()
                .get("tolerations");

        JsonNode jobTolerations = registerJob.getSpec().path("template").path("spec").path("tolerations");
        JsonNode[] resourcesToCheck = {tolerations, jobTolerations};
        for (var resource : resourcesToCheck) {
            assertThat(resource).isArrayWithNumberOfChildren(1);
            assertThat(resource.get(0).get("key")).hasTextEqualTo("mesh");
            assertThat(resource.get(0).get("operator")).hasTextEqualTo("Exists");
            assertThat(resource.get(0).get("effect")).hasTextContaining("NoSchedule");
        }
    }

    @Test
    void meshNodeSelector() throws Exception {
        final var product = Product.bitbucket;
        final var resources = helm.captureKubeResourcesFromHelmChart(product,
                Map.of(product + ".mesh.enabled", "true",
                        product + ".mesh.nodeAutoRegistration", "true",
                        product + ".sysadminCredentials.secretName", "secret-name",
                        product + ".mesh.nodeSelector.meshnode", "special-node"));

        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName() + "-mesh");
        KubeResource registerJob = resources.get(Kind.Job, product.getHelmReleaseName() + "-mesh-register-job-0");

        assertThat(statefulSet.getNodeSelector().path("meshnode")).hasTextEqualTo("special-node");
        assertThat(registerJob.getSpec().path("template").path("spec").path("nodeSelector").path("meshnode")).hasTextEqualTo("special-node");
    }

    @Test
    void meshSchedulerName() throws Exception {
        final var product = Product.bitbucket;
        final var resources = helm.captureKubeResourcesFromHelmChart(product,
                Map.of(product + ".mesh.enabled", "true",
                        product + ".mesh.schedulerName", "mesh"));

        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName() + "-mesh");

        assertEquals("mesh", statefulSet.getPodSpec().path("schedulerName").asText());
    }

    @Test
    void meshService() throws Exception {
        final var product = Product.bitbucket;
        final var resources = helm.captureKubeResourcesFromHelmChart(product,
                Map.of(product + ".mesh.enabled", "true",
                        product + ".mesh.service.annotations.podAnnotation1", "mesh1",
                        product + ".mesh.service.port", "7778",
                        product + ".mesh.service.type", "LoadBalancer"
                ));

        final var service = resources.get(Kind.Service, Service.class, product.getHelmReleaseName() + "-mesh-1");

        assertThat(service.getType())
                .hasTextEqualTo("LoadBalancer");
        VavrAssertions.assertThat(service.getPort("mesh"))
                .hasValueSatisfying(node -> assertThat(node.path("port")).hasValueEqualTo(7778));
        assertThat(service.getMetadata().get("annotations")).isObject(Map.of(
                "podAnnotation1", "mesh1"
        ));
    }

    @Test
    void meshRegistrationConfig() throws Exception {
        final var product = Product.bitbucket;
        final var resources = helm.captureKubeResourcesFromHelmChart(product,
                Map.of(product + ".mesh.enabled", "true",
                        product + ".mesh.service.port", "8888",
                        product + ".sysadminCredentials.secretName", "bb-admin"
                ));

        final KubeResource configMap = meshScriptsConfigMap(resources);
        assertThat(configMap.getNode("data", "register-mesh-node.sh")).hasTextContaining("Registering mesh node bitbucket-mesh-$1");
        assertThat(configMap.getNode("data", "register-mesh-node.sh")).hasTextContaining("8888");
        assertThat(configMap.getNode("data", "register-mesh-node.sh")).hasTextContaining("BITBUCKET_URL=http://unittest-bitbucket");
    }

    @Test
    void meshBitbucketUrlConfig() throws Exception {
        final var product = Product.bitbucket;
        final var resources = helm.captureKubeResourcesFromHelmChart(product,
                Map.of(product + ".mesh.enabled", "true",
                        product + ".service.contextPath", "/mybitbucket",
                        product + ".service.port", "81",
                        product + ".sysadminCredentials.secretName", "bb-admin"
                ));

        final KubeResource configMap = meshScriptsConfigMap(resources);
        assertThat(configMap.getNode("data", "register-mesh-node.sh")).hasTextContaining("BITBUCKET_URL=http://unittest-bitbucket:81/mybitbucket");
    }

    @Test
    void fluentd_sidecar_enabled() throws Exception {
        final var product = Product.bitbucket;
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".mesh.enabled", "true",
                "fluentd.enabled", "true",
                "fluentd.elasticsearch.hostname", "myelastic"));

        resources.getStatefulSet(product.getHelmReleaseName() +"-mesh")
                .getContainer("fluentd");

        final var fluentdConfigMap = resources.get(ConfigMap, product.getHelmReleaseName() + "-fluentd-config");
        final var config = fluentdConfigMap.getNode("data", "fluent.conf").asText();

        Assertions.assertThat(config).contains("atlassian-mesh.log");
        Assertions.assertThat(config).contains("tag mesh.log");
    }

    private KubeResource meshScriptsConfigMap(KubeResources resources) {
        final var configMaps = resources.getAll(Kind.ConfigMap)
                .find(map -> map.getName().endsWith("mesh-scripts"))
                .collect(Collectors.toList());
        Assertions.assertThat(configMaps).hasSize(1);
        return configMaps.get(0);
    }

    private KubeResource meshJvmConfigMap(KubeResources resources) {
        final var configMaps = resources.getAll(Kind.ConfigMap)
                .find(map -> map.getName().endsWith("jvm-config-mesh"))
                .collect(Collectors.toList());
        Assertions.assertThat(configMaps).hasSize(1);
        return configMaps.get(0);
    }
}
