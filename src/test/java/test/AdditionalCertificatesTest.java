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
import static test.model.Kind.ConfigMap;

public class AdditionalCertificatesTest {
    private Helm helm;
    String expectedCmd = "set -e; cp $JAVA_HOME/lib/security/cacerts /var/ssl/cacerts; chmod 664 /var/ssl/cacerts; for crt in /tmp/crt/*.*; do echo \"Adding $crt to keystore\"; keytool -import -keystore /var/ssl/cacerts -storepass changeit -noprompt -alias $(echo $(basename $crt)) -file $crt; done;";

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }


    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void additional_certificates_jvm_prop(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product.name() + ".additionalCertificates.secretName", "mycrt",
                "volumes.sharedHome.persistentVolumeClaim.create", "true"
        ));
        final var jvmConfigMap = resources.get(ConfigMap, product.getHelmReleaseName() + "-jvm-config");
        assertThat(jvmConfigMap.getConfigMapData().path("additional_jvm_args")).hasTextContaining("-Djavax.net.ssl.trustStore=/var/ssl/cacerts");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void additional_certificate_list_jvm_prop(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product.name() + ".additionalCertificates.secretList[0].name", "self-signed-ca",
                product.name() + ".additionalCertificates.secretList[0].keys[0]", "ca.crt",
                product.name() + ".additionalCertificates.secretList[0].keys[1]", "stg.crt",
                product.name() + ".additionalCertificates.secretList[1].name", "custom-ca",
                product.name() + ".additionalCertificates.secretList[1].keys[0]", "custom.crt",
                "volumes.sharedHome.persistentVolumeClaim.create", "true"
        ));
        final var jvmConfigMap = resources.get(ConfigMap, product.getHelmReleaseName() + "-jvm-config");
        assertThat(jvmConfigMap.getConfigMapData().path("additional_jvm_args")).hasTextContaining("-Djavax.net.ssl.trustStore=/var/ssl/cacerts");
    }


    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"}, mode = EnumSource.Mode.INCLUDE)
    void additional_certificates_jvm_prop_synchrony(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "synchrony.additionalCertificates.secretName", "mycrt"
        ));
        final var jvmConfigMap = resources.get(ConfigMap, product.getHelmReleaseName() + "-synchrony-entrypoint");
        assertThat(jvmConfigMap.getConfigMapData().path("start-synchrony.sh")).hasTextContaining("-Djavax.net.ssl.trustStore=/var/ssl/cacerts");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"}, mode = EnumSource.Mode.INCLUDE)
    void additional_certificate_list_jvm_prop_synchrony(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "synchrony.additionalCertificates.secretList[0].name", "self-signed-ca",
                "synchrony.additionalCertificates.secretList[0].keys[0]", "ca.crt",
                "synchrony.additionalCertificates.secretList[0].keys[1]", "stg.crt",
                "synchrony.additionalCertificates.secretList[1].name", "custom-ca",
                "synchrony.additionalCertificates.secretList[1].keys[0]", "custom.crt"
        ));
        final var jvmConfigMap = resources.get(ConfigMap, product.getHelmReleaseName() + "-synchrony-entrypoint");
        assertThat(jvmConfigMap.getConfigMapData().path("start-synchrony.sh")).hasTextContaining("-Djavax.net.ssl.trustStore=/var/ssl/cacerts");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket"}, mode = EnumSource.Mode.INCLUDE)
    void additional_certificates_jvm_prop_mesh(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product.name() + ".mesh.enabled", "true",
                product.name() + ".mesh.additionalCertificates.secretName", "mycrt"
        ));
        final var bitbucketMeshJvmConfigMap = resources.get(ConfigMap, product.getHelmReleaseName() + "-jvm-config-mesh");
        assertThat(bitbucketMeshJvmConfigMap.getConfigMapData().path("additional_jvm_args")).hasTextContaining("-Djavax.net.ssl.trustStore=/var/ssl/cacerts");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket"}, mode = EnumSource.Mode.INCLUDE)
    void additional_certificate_list_jvm_prop_mesh(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product.name() + ".mesh.enabled", "true",
                product.name() + ".mesh.additionalCertificates.secretList[0].name", "self-signed-ca",
                product.name() + ".mesh.additionalCertificates.secretList[0].keys[0]", "ca.crt",
                product.name() + ".mesh.additionalCertificates.secretList[0].keys[1]", "stg.crt",
                product.name() + ".mesh.additionalCertificates.secretList[1].name", "custom-ca",
                product.name() + ".mesh.additionalCertificates.secretList[1].keys[0]", "custom.crt"
        ));
        final var bitbucketMeshJvmConfigMap = resources.get(ConfigMap, product.getHelmReleaseName() + "-jvm-config-mesh");
        assertThat(bitbucketMeshJvmConfigMap.getConfigMapData().path("additional_jvm_args")).hasTextContaining("-Djavax.net.ssl.trustStore=/var/ssl/cacerts");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void additional_certificates_init_container(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product.name() + ".additionalCertificates.secretName", "mycrt",
                "volumes.sharedHome.persistentVolumeClaim.create", "true"
        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        assertThat(statefulSet.getInitContainers().get(1).path("name")).hasTextEqualTo("import-certs");
        assertThat(statefulSet.getInitContainers().get(1).path("volumeMounts").path(0).get("name")).hasTextEqualTo("keystore");
        assertThat(statefulSet.getInitContainers().get(1).path("volumeMounts").path(0).get("mountPath")).hasTextEqualTo("/var/ssl");
        assertThat(statefulSet.getInitContainers().get(1).path("args").path(1)).hasTextEqualTo(expectedCmd);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"}, mode = EnumSource.Mode.INCLUDE)
    void additional_certificates_init_container_synchrony(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "synchrony.additionalCertificates.secretName", "mycrt"
        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName()+"-synchrony");
        assertThat(statefulSet.getInitContainers().get(0).path("name")).hasTextEqualTo("import-certs");
        assertThat(statefulSet.getInitContainers().get(0).path("volumeMounts").path(0).get("name")).hasTextEqualTo("keystore");
        assertThat(statefulSet.getInitContainers().get(0).path("volumeMounts").path(0).get("mountPath")).hasTextEqualTo("/var/ssl");
        assertThat(statefulSet.getInitContainers().get(0).path("args").path(1)).hasTextEqualTo(expectedCmd);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket"}, mode = EnumSource.Mode.INCLUDE)
    void additional_certificates_init_container_bitbucket_mesh(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product.name() + ".mesh.enabled", "true",
                product.name() + ".mesh.additionalCertificates.secretName", "mycrt"
        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName()+"-mesh");
        assertThat(statefulSet.getInitContainers().get(0).path("name")).hasTextEqualTo("import-certs");
        assertThat(statefulSet.getInitContainers().get(0).path("volumeMounts").path(0).get("name")).hasTextEqualTo("keystore");
        assertThat(statefulSet.getInitContainers().get(0).path("volumeMounts").path(0).get("mountPath")).hasTextEqualTo("/var/ssl");
        assertThat(statefulSet.getInitContainers().get(0).path("args").path(1)).hasTextEqualTo(expectedCmd);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void additional_certificates_volumeMounts(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product.name() + ".additionalCertificates.secretName", "mycrt"
        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        JsonNode keystoreVolumeMount = statefulSet.getContainer(product.name()).getVolumeMount("keystore");
        assertThat(keystoreVolumeMount.path("mountPath")).hasTextEqualTo("/var/ssl");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void additional_certificate_list_volumeMounts(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product.name() + ".additionalCertificates.secretList[0].name", "self-signed-ca",
                product.name() + ".additionalCertificates.secretList[0].keys[0]", "ca.crt",
                product.name() + ".additionalCertificates.secretList[0].keys[1]", "stg.crt",
                product.name() + ".additionalCertificates.secretList[1].name", "custom-ca",
                product.name() + ".additionalCertificates.secretList[1].keys[0]", "custom.crt"
        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        JsonNode keystoreVolumeMount = statefulSet.getContainer(product.name()).getVolumeMount("keystore");
        assertThat(keystoreVolumeMount.path("mountPath")).hasTextEqualTo("/var/ssl");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket"}, mode = EnumSource.Mode.INCLUDE)
    void additional_certificates_volumeMounts_bitbucket_mesh(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product.name() + ".mesh.enabled", "true",
                product.name() + ".mesh.additionalCertificates.secretName", "mycrt"
        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName()+"-mesh");
        JsonNode keystoreVolumeMount = statefulSet.getContainer(product.name()+"-mesh").getVolumeMount("keystore");
        assertThat(keystoreVolumeMount.path("mountPath")).hasTextEqualTo("/var/ssl");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket"}, mode = EnumSource.Mode.INCLUDE)
    void additional_certificate_list_volumeMounts_bitbucket_mesh(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product.name() + ".mesh.enabled", "true",
                product.name() + ".mesh.additionalCertificates.secretList[0].name", "self-signed-ca",
                product.name() + ".mesh.additionalCertificates.secretList[0].keys[0]", "ca.crt",
                product.name() + ".mesh.additionalCertificates.secretList[0].keys[1]", "stg.crt",
                product.name() + ".mesh.additionalCertificates.secretList[1].name", "custom-ca",
                product.name() + ".mesh.additionalCertificates.secretList[1].keys[0]", "custom.crt"
        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName()+"-mesh");
        JsonNode keystoreVolumeMount = statefulSet.getContainer(product.name()+"-mesh").getVolumeMount("keystore");
        assertThat(keystoreVolumeMount.path("mountPath")).hasTextEqualTo("/var/ssl");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"}, mode = EnumSource.Mode.INCLUDE)
    void additional_certificates_volumeMounts_synchrony(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "synchrony.additionalCertificates.secretName", "mycrt"
        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName()+"-synchrony");
        JsonNode keystoreVolumeMount = statefulSet.getContainer("synchrony").getVolumeMount("keystore");
        assertThat(keystoreVolumeMount.path("mountPath")).hasTextEqualTo("/var/ssl");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"}, mode = EnumSource.Mode.INCLUDE)
    void additional_certificate_list_volumeMounts_synchrony(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "synchrony.additionalCertificates.secretList[0].name", "self-signed-ca",
                "synchrony.additionalCertificates.secretList[0].keys[0]", "ca.crt",
                "synchrony.additionalCertificates.secretList[0].keys[1]", "stg.crt",
                "synchrony.additionalCertificates.secretList[1].name", "custom-ca",
                "synchrony.additionalCertificates.secretList[1].keys[0]", "custom.crt"
        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName()+"-synchrony");
        JsonNode keystoreVolumeMount = statefulSet.getContainer("synchrony").getVolumeMount("keystore");
        assertThat(keystoreVolumeMount.path("mountPath")).hasTextEqualTo("/var/ssl");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void additional_certificates_volumes(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product.name() + ".additionalCertificates.secretName", "mycrt"
        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        assertThat(statefulSet.getVolume("keystore").get().path("emptyDir")).isEmpty();
        assertThat(statefulSet.getVolume("certs").get().path("secret").path("secretName")).hasTextEqualTo("mycrt");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket"}, mode = EnumSource.Mode.INCLUDE)
    void additional_certificates_volumes_bitbucket_mesh(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product.name() + ".mesh.enabled", "true",
                product.name() + ".mesh.additionalCertificates.secretName", "mycrt"
        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName()+"-mesh");
        assertThat(statefulSet.getVolume("keystore").get().path("emptyDir")).isEmpty();
        assertThat(statefulSet.getVolume("certs").get().path("secret").path("secretName")).hasTextEqualTo("mycrt");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"}, mode = EnumSource.Mode.INCLUDE)
    void additional_certificates_volumes_synchrony(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "synchrony.additionalCertificates.secretName", "mycrt"
        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName()+"-synchrony");
        assertThat(statefulSet.getVolume("keystore").get().path("emptyDir")).isEmpty();
        assertThat(statefulSet.getVolume("certs").get().path("secret").path("secretName")).hasTextEqualTo("mycrt");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void additional_certificates_custom_cmd(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product.name() + ".additionalCertificates.secretName", "mycrt",
                "volumes.sharedHome.persistentVolumeClaim.create", "true",
                product.name() + ".additionalCertificates.customCmd", "echo \"My custom command\""
        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        assertThat(statefulSet.getInitContainers().get(1).path("args").path(1)).hasTextEqualTo("echo \"My custom command\"");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket"}, mode = EnumSource.Mode.INCLUDE)
    void additional_certificates_custom_cmd_bitbucket_mesh(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product.name() + ".mesh.enabled", "true",
                product.name() + ".mesh.additionalCertificates.secretName", "mycrt",
                product.name() + ".mesh.additionalCertificates.customCmd", "echo \"My custom command\""
        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName()+"-mesh");
        assertThat(statefulSet.getInitContainers().get(0).path("args").path(1)).hasTextEqualTo("echo \"My custom command\"");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"}, mode = EnumSource.Mode.INCLUDE)
    void additional_certificates_synchrony_custom_cmd(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "synchrony.additionalCertificates.secretName", "mycrt",
                "synchrony.additionalCertificates.customCmd", "echo \"My custom command\""
        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName()+"-synchrony");
        assertThat(statefulSet.getInitContainers().get(0).path("args").path(1)).hasTextEqualTo("echo \"My custom command\"");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void additional_certificates_init_resources(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "volumes.sharedHome.persistentVolumeClaim.create", "true",
                product.name() + ".additionalCertificates.secretName", "mycrt",
                product.name() + ".additionalCertificates.initContainer.resources.requests.memory", "1Gi",
                product.name() + ".additionalCertificates.initContainer.resources.requests.cpu", "20m",
                product.name() + ".additionalCertificates.initContainer.resources.limits.memory", "1Gi",
                product.name() + ".additionalCertificates.initContainer.resources.limits.cpu", "20m"

        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        assertThat(statefulSet.getInitContainers().get(1).path("resources").path("requests").path("memory")).hasTextEqualTo("1Gi");
        assertThat(statefulSet.getInitContainers().get(1).path("resources").path("requests").path("cpu")).hasTextEqualTo("20m");
        assertThat(statefulSet.getInitContainers().get(1).path("resources").path("limits").path("memory")).hasTextEqualTo("1Gi");
        assertThat(statefulSet.getInitContainers().get(1).path("resources").path("limits").path("cpu")).hasTextEqualTo("20m");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"}, mode = EnumSource.Mode.INCLUDE)
    void additional_certificates_synchrony_init_resources(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "synchrony.additionalCertificates.secretName", "mycrt",
                "synchrony.additionalCertificates.initContainer.resources.requests.memory", "1Gi",
                "synchrony.additionalCertificates.initContainer.resources.requests.cpu", "20m",
                "synchrony.additionalCertificates.initContainer.resources.limits.memory", "1Gi",
                "synchrony.additionalCertificates.initContainer.resources.limits.cpu", "20m"
        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName()+"-synchrony");
        assertThat(statefulSet.getInitContainers().get(0).path("resources").path("requests").path("memory")).hasTextEqualTo("1Gi");
        assertThat(statefulSet.getInitContainers().get(0).path("resources").path("requests").path("cpu")).hasTextEqualTo("20m");
        assertThat(statefulSet.getInitContainers().get(0).path("resources").path("limits").path("memory")).hasTextEqualTo("1Gi");
        assertThat(statefulSet.getInitContainers().get(0).path("resources").path("limits").path("cpu")).hasTextEqualTo("20m");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket"}, mode = EnumSource.Mode.INCLUDE)
    void additional_certificates_bitbucket_mesh_init_resources(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product.name() + ".mesh.enabled", "true",
                product.name() + ".mesh.additionalCertificates.secretName", "mycrt",
                product.name() + ".mesh.additionalCertificates.initContainer.resources.requests.memory", "1Gi",
                product.name() + ".mesh.additionalCertificates.initContainer.resources.requests.cpu", "20m",
                product.name() + ".mesh.additionalCertificates.initContainer.resources.limits.memory", "1Gi",
                product.name() + ".mesh.additionalCertificates.initContainer.resources.limits.cpu", "20m"        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName()+"-mesh");
        assertThat(statefulSet.getInitContainers().get(0).path("resources").path("requests").path("memory")).hasTextEqualTo("1Gi");
        assertThat(statefulSet.getInitContainers().get(0).path("resources").path("requests").path("cpu")).hasTextEqualTo("20m");
        assertThat(statefulSet.getInitContainers().get(0).path("resources").path("limits").path("memory")).hasTextEqualTo("1Gi");
        assertThat(statefulSet.getInitContainers().get(0).path("resources").path("limits").path("cpu")).hasTextEqualTo("20m");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void additional_certificates_multi_volumes(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "volumes.sharedHome.persistentVolumeClaim.create", "true",
                product.name() + ".additionalCertificates.secretList[0].name", "self-signed-ca",
                product.name() + ".additionalCertificates.secretList[0].keys[0]", "ca.crt",
                product.name() + ".additionalCertificates.secretList[0].keys[1]", "stg.crt"

        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        assertThat(statefulSet.getVolume("keystore").get().path("emptyDir")).isEmpty();
        assertThat(statefulSet.getVolume("self-signed-ca").get().path("secret").path("secretName")).hasTextEqualTo("self-signed-ca");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void additional_certificates_multi_volume_mounts(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "volumes.sharedHome.persistentVolumeClaim.create", "true",
                product.name() + ".additionalCertificates.secretList[0].name", "self-signed-ca",
                product.name() + ".additionalCertificates.secretList[0].keys[0]", "ca.crt",
                product.name() + ".additionalCertificates.secretList[0].keys[1]", "stg.crt",
                product.name() + ".additionalCertificates.secretList[1].name", "custom-ca",
                product.name() + ".additionalCertificates.secretList[1].keys[0]", "custom.crt"

        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName());
        assertThat(statefulSet.getInitContainers().get(1).path("name")).hasTextEqualTo("import-certs");
        assertThat(statefulSet.getInitContainers().get(1).path("volumeMounts").path(1).get("name")).hasTextEqualTo("self-signed-ca");
        assertThat(statefulSet.getInitContainers().get(1).path("volumeMounts").path(1).get("mountPath")).hasTextEqualTo("/tmp/crt/self-signed-ca-ca.crt");
        assertThat(statefulSet.getInitContainers().get(1).path("volumeMounts").path(1).get("subPath")).hasTextEqualTo("ca.crt");
        assertThat(statefulSet.getInitContainers().get(1).path("volumeMounts").path(2).get("name")).hasTextEqualTo("self-signed-ca");
        assertThat(statefulSet.getInitContainers().get(1).path("volumeMounts").path(2).get("mountPath")).hasTextEqualTo("/tmp/crt/self-signed-ca-stg.crt");
        assertThat(statefulSet.getInitContainers().get(1).path("volumeMounts").path(2).get("subPath")).hasTextEqualTo("stg.crt");
        assertThat(statefulSet.getInitContainers().get(1).path("volumeMounts").path(3).get("name")).hasTextEqualTo("custom-ca");
        assertThat(statefulSet.getInitContainers().get(1).path("volumeMounts").path(3).get("mountPath")).hasTextEqualTo("/tmp/crt/custom-ca-custom.crt");
        assertThat(statefulSet.getInitContainers().get(1).path("volumeMounts").path(3).get("subPath")).hasTextEqualTo("custom.crt");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"}, mode = EnumSource.Mode.INCLUDE)
    void additional_certificates_multi_volume_mounts_synchrony(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "synchrony.additionalCertificates.secretList[0].name", "self-signed-ca",
                "synchrony.additionalCertificates.secretList[0].keys[0]", "ca.crt",
                "synchrony.additionalCertificates.secretList[0].keys[1]", "stg.crt",
                "synchrony.additionalCertificates.secretList[1].name", "custom-ca",
                "synchrony.additionalCertificates.secretList[1].keys[0]", "custom.crt"

        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName() + "-synchrony");
        assertThat(statefulSet.getInitContainers().get(0).path("name")).hasTextEqualTo("import-certs");
        assertThat(statefulSet.getInitContainers().get(0).path("volumeMounts").path(1).get("name")).hasTextEqualTo("self-signed-ca");
        assertThat(statefulSet.getInitContainers().get(0).path("volumeMounts").path(1).get("mountPath")).hasTextEqualTo("/tmp/crt/self-signed-ca-ca.crt");
        assertThat(statefulSet.getInitContainers().get(0).path("volumeMounts").path(1).get("subPath")).hasTextEqualTo("ca.crt");
        assertThat(statefulSet.getInitContainers().get(0).path("volumeMounts").path(2).get("name")).hasTextEqualTo("self-signed-ca");
        assertThat(statefulSet.getInitContainers().get(0).path("volumeMounts").path(2).get("mountPath")).hasTextEqualTo("/tmp/crt/self-signed-ca-stg.crt");
        assertThat(statefulSet.getInitContainers().get(0).path("volumeMounts").path(2).get("subPath")).hasTextEqualTo("stg.crt");
        assertThat(statefulSet.getInitContainers().get(0).path("volumeMounts").path(3).get("name")).hasTextEqualTo("custom-ca");
        assertThat(statefulSet.getInitContainers().get(0).path("volumeMounts").path(3).get("mountPath")).hasTextEqualTo("/tmp/crt/custom-ca-custom.crt");
        assertThat(statefulSet.getInitContainers().get(0).path("volumeMounts").path(3).get("subPath")).hasTextEqualTo("custom.crt");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"}, mode = EnumSource.Mode.INCLUDE)
    void additional_certificates_multi_volumes_synchrony(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "synchrony.additionalCertificates.secretList[0].name", "self-signed-ca",
                "synchrony.additionalCertificates.secretList[0].keys[0]", "ca.crt",
                "synchrony.additionalCertificates.secretList[0].keys[1]", "stg.crt"

        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName() + "-synchrony");
        assertThat(statefulSet.getVolume("keystore").get().path("emptyDir")).isEmpty();
        assertThat(statefulSet.getVolume("self-signed-ca").get().path("secret").path("secretName")).hasTextEqualTo("self-signed-ca");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket"}, mode = EnumSource.Mode.INCLUDE)
    void additional_certificates_multi_volumes_mesh(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "bitbucket.mesh.enabled", "true",
                product.name() + ".mesh.additionalCertificates.secretList[0].name", "self-signed-ca",
                product.name() + ".mesh.additionalCertificates.secretList[0].keys[0]", "ca.crt",
                product.name() + ".mesh.additionalCertificates.secretList[0].keys[1]", "stg.crt"

        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName() + "-mesh");
        assertThat(statefulSet.getVolume("keystore").get().path("emptyDir")).isEmpty();
        assertThat(statefulSet.getVolume("self-signed-ca").get().path("secret").path("secretName")).hasTextEqualTo("self-signed-ca");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket"}, mode = EnumSource.Mode.INCLUDE)
    void additional_certificates_multi_volume_mounts_mesh(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "bitbucket.mesh.enabled", "true",
                product.name() + ".mesh.additionalCertificates.secretList[0].name", "self-signed-ca",
                product.name() + ".mesh.additionalCertificates.secretList[0].keys[0]", "ca.crt",
                product.name() + ".mesh.additionalCertificates.secretList[0].keys[1]", "stg.crt",
                product.name() + ".mesh.additionalCertificates.secretList[1].name", "custom-ca",
                product.name() + ".mesh.additionalCertificates.secretList[1].keys[0]", "custom.crt"

        ));
        final var statefulSet = resources.getStatefulSet(product.getHelmReleaseName() + "-mesh");
        assertThat(statefulSet.getInitContainers().get(0).path("name")).hasTextEqualTo("import-certs");
        assertThat(statefulSet.getInitContainers().get(0).path("volumeMounts").path(1).get("name")).hasTextEqualTo("self-signed-ca");
        assertThat(statefulSet.getInitContainers().get(0).path("volumeMounts").path(1).get("mountPath")).hasTextEqualTo("/tmp/crt/self-signed-ca-ca.crt");
        assertThat(statefulSet.getInitContainers().get(0).path("volumeMounts").path(1).get("subPath")).hasTextEqualTo("ca.crt");
        assertThat(statefulSet.getInitContainers().get(0).path("volumeMounts").path(2).get("name")).hasTextEqualTo("self-signed-ca");
        assertThat(statefulSet.getInitContainers().get(0).path("volumeMounts").path(2).get("mountPath")).hasTextEqualTo("/tmp/crt/self-signed-ca-stg.crt");
        assertThat(statefulSet.getInitContainers().get(0).path("volumeMounts").path(2).get("subPath")).hasTextEqualTo("stg.crt");
        assertThat(statefulSet.getInitContainers().get(0).path("volumeMounts").path(3).get("name")).hasTextEqualTo("custom-ca");
        assertThat(statefulSet.getInitContainers().get(0).path("volumeMounts").path(3).get("mountPath")).hasTextEqualTo("/tmp/crt/custom-ca-custom.crt");
        assertThat(statefulSet.getInitContainers().get(0).path("volumeMounts").path(3).get("subPath")).hasTextEqualTo("custom.crt");
    }
}
