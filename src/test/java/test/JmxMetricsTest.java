package test;

import org.assertj.vavr.api.VavrAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Kind;
import test.model.Product;
import test.model.Service;
import test.model.StatefulSet;

import java.util.Map;

import static test.jackson.JsonNodeAssert.assertThat;

class JmxMetricsTest {

    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void expose_jmx_metrics_enabled_init_container(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "monitoring.exposeJmxMetrics", "true"
        ));

        String sharedHomePath = "/var/atlassian/application-data/shared-home";
        if (product.name().equals("crowd")) {
            sharedHomePath= "/var/atlassian/application-data/crowd/shared";
        }

        StatefulSet statefulSet = resources.getStatefulSet(product.getHelmReleaseName());

        // assert jmx_exporter init container
        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("image")).hasTextEqualTo("bitnami/jmx-exporter:0.18.0");
        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("command").get(0)).hasTextEqualTo("cp");
        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("args").get(0)).hasTextEqualTo("/opt/bitnami/jmx-exporter/jmx_prometheus_javaagent.jar");

        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("args").get(1)).hasTextEqualTo(sharedHomePath);
        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("volumeMounts").get(0).path("mountPath")).hasTextEqualTo(sharedHomePath);

        // assert jmx port
        assertThat(statefulSet.getContainer(product.name()).getPort("jmx").path("containerPort")).hasValueEqualTo(9999);
        assertThat(statefulSet.getContainer(product.name()).getPort("jmx").path("protocol")).hasTextEqualTo("TCP");

        // assert jvm configmap has javaagent
        final var jmvConfigMap = resources.getConfigMap(product.getHelmReleaseName() + "-jvm-config").getDataByKey("additional_jvm_args");
        assertThat(jmvConfigMap).hasTextContaining("-javaagent:"+sharedHomePath+"/jmx_prometheus_javaagent.jar=9999:/opt/atlassian/jmx/jmx-config.yaml");

        // assert jmx configmap created and has expected config
        final var jmxConfigMap = resources.getConfigMap(product.getHelmReleaseName() + "-jmx-config").getDataByKey("jmx-config.yaml");
        assertThat(jmxConfigMap).hasTextContaining("- pattern: \".*\"");

        if (product.name().equals("bitbucket")) {
            // assert jmx env var
            statefulSet.getContainer().getEnv().assertHasValue("JMX_ENABLED", "true");
        }
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void expose_jmx_metrics_enabled_custom_init_container(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "monitoring.exposeJmxMetrics", "true",
                "monitoring.jmxExporterImageRepo", "myregistry/myrepo",
                "monitoring.jmxExporterImageTag", "0.17.0"
        ));

        StatefulSet statefulSet = resources.getStatefulSet(product.getHelmReleaseName());

        // assert jmx_exporter init container
        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("image")).hasTextEqualTo("myregistry/myrepo:0.17.0");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void expose_jmx_metrics_enabled_custom_jar(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "monitoring.exposeJmxMetrics", "true",
                "monitoring.fetchJmxExporterJar", "false",
                "monitoring.jmxExporterCustomJarLocation", "/tmp/custom.jar",
                "monitoring.jmxExporterPort", "9000",
                "volumes.sharedHome.persistentVolumeClaim.create", "true"

        ));

        StatefulSet statefulSet = resources.getStatefulSet(product.getHelmReleaseName());

        // assert jmx_exporter init container does not exist
        assertThat(statefulSet.getInitContainers().path(0).path("name")).hasTextNotContaining("fetch-jmx-exporter");

        // assert jvm configmap has custom javaagent path and port
        final var jmvConfigMap = resources.getConfigMap(product.getHelmReleaseName() + "-jvm-config").getDataByKey("additional_jvm_args");
        assertThat(jmvConfigMap).hasTextContaining("-javaagent:/tmp/custom.jar=9000:/opt/atlassian/jmx/jmx-config.yaml");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bamboo_agent"}, mode = EnumSource.Mode.EXCLUDE)
    void jmx_service_test(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "monitoring.exposeJmxMetrics", "true",
                "monitoring.jmxExporterPort", "9000",
                "monitoring.jmxExporterPortType", "NodePort"
        ));

        final var service = resources.get(Kind.Service, Service.class, product.getHelmReleaseName()+"-jmx");

        assertThat(service.getType())
                .hasTextEqualTo("NodePort");
        VavrAssertions.assertThat(service.getPort("jmx"))
                .hasValueSatisfying(node -> assertThat(node.path("port")).hasValueEqualTo(9000));
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"bitbucket"}, mode = EnumSource.Mode.INCLUDE)
    void expose_jmx_metrics_bitbucket_mesh(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "monitoring.exposeJmxMetrics", "true",
                product+".mesh.enabled", "true"
        ));

        String localHomePath = "/var/atlassian/application-data/mesh";
        StatefulSet statefulSet = resources.getStatefulSet(product.getHelmReleaseName() + "-mesh");

        // assert jmx_exporter init container
        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("image")).hasTextEqualTo("bitnami/jmx-exporter:0.18.0");
        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("command").get(0)).hasTextEqualTo("cp");
        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("args").get(0)).hasTextEqualTo("/opt/bitnami/jmx-exporter/jmx_prometheus_javaagent.jar");

        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("args").get(1)).hasTextEqualTo(localHomePath);
        assertThat(statefulSet.getInitContainer("fetch-jmx-exporter").get().path("volumeMounts").get(0).path("mountPath")).hasTextEqualTo(localHomePath);

        // assert jmx port
        assertThat(statefulSet.getContainer(product.name()+"-mesh").getPort("jmx").path("containerPort")).hasValueEqualTo(9999);
        assertThat(statefulSet.getContainer(product.name()+"-mesh").getPort("jmx").path("protocol")).hasTextEqualTo("TCP");

        // assert jmx env var
        statefulSet.getContainer().getEnv().assertHasValue("JMX_ENABLED", "true");

        // assert jvm configmap has javaagent
        final var jmvConfigMap = resources.getConfigMap(product.getHelmReleaseName() + "-jvm-config-mesh").getDataByKey("additional_jvm_args");
        assertThat(jmvConfigMap).hasTextContaining("-javaagent:"+localHomePath+"/jmx_prometheus_javaagent.jar=9999:/opt/atlassian/jmx/jmx-config.yaml");

        // assert jmx configmap created and has expected config
        final var jmxConfigMap = resources.getConfigMap(product.getHelmReleaseName() + "-jmx-config").getDataByKey("jmx-config.yaml");
        assertThat(jmxConfigMap).hasTextContaining("- pattern: \".*\"");
    }
}
