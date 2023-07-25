package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.*;

import java.util.Map;

import static test.jackson.JsonNodeAssert.assertThat;

class ServerConfigTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"jira", "confluence"}, mode = EnumSource.Mode.INCLUDE)
    void enable_server_xml_config_map(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product.name() + ".tomcatConfig.generateByHelm", "true",
                product.name() + ".seraphConfig.generateByHelm", "true",
                product.name() + ".tomcatConfig.port", "1234",
                product.name() + ".tomcatConfig.acceptCount", "5678",
                product.name() + ".tomcatConfig.maxHttpHeaderSize", "9876",
                product.name() + ".tomcatConfig.mgmtPort", "4321",
                product.name() + ".tomcatConfig.maxThreads", "1000",
                product.name() + ".tomcatConfig.minSpareThreads", "200",
                product.name() + ".tomcatConfig.connectionTimeout", "30000",
                "ingress.host", String.format("%s.com", product.name())
                ));

        StatefulSet statefulSet = resources.getStatefulSet(product.getHelmReleaseName());

        // assert server.xml volumeMount
        String expectedServerXmlPath = String.format("/opt/atlassian/%s/conf/server.xml", product.name());
        assertThat(statefulSet.getContainer().getVolumeMount("server-xml").path("mountPath"))
                .hasTextEqualTo(expectedServerXmlPath);
        assertThat(statefulSet.getContainer().getVolumeMount("server-xml").path("subPath")).hasTextEqualTo("server.xml");

        // assert seraph-config.xml volumeMount
        String productDirectory = product.name();
        if (product.name().equals("jira")) {
            productDirectory = "atlassian-jira";
        }
        String expectedSeraphXmlPath = String.format("/opt/atlassian/%s/%s/WEB-INF/classes/seraph-config.xml",
                product.name(), productDirectory);
        assertThat(statefulSet.getContainer().getVolumeMount("seraph-config-xml").path("mountPath"))
                .hasTextEqualTo(expectedSeraphXmlPath);
        assertThat(statefulSet.getContainer().getVolumeMount("seraph-config-xml").path("subPath")).hasTextEqualTo("seraph-config.xml");

        //assert server.xml volume
        assertThat(statefulSet.getVolume("server-xml").get().path("configMap").get("name")).hasTextEqualTo(product.getHelmReleaseName() + "-server-config");
        assertThat(statefulSet.getVolume("server-xml").get().path("configMap").get("items").path(0).path("key")).hasTextEqualTo("server.xml");
        assertThat(statefulSet.getVolume("server-xml").get().path("configMap").get("items").path(0).path("path")).hasTextEqualTo("server.xml");

        //assert seraph-config.xml volume
        assertThat(statefulSet.getVolume("seraph-config-xml").get().path("configMap").get("name")).hasTextEqualTo(product.getHelmReleaseName() + "-server-config");
        assertThat(statefulSet.getVolume("seraph-config-xml").get().path("configMap").get("items").path(0).path("key")).hasTextEqualTo("seraph-config.xml");
        assertThat(statefulSet.getVolume("seraph-config-xml").get().path("configMap").get("items").path(0).path("path")).hasTextEqualTo("seraph-config.xml");

        // assert a few server.xml elements in server-config ConfigMap
        KubeResource serverConfigMap = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-server-config");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("Connector port=\"1234\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("acceptCount=\"5678\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("maxHttpHeaderSize=\"9876\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("Server port=\"4321\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("proxyPort=\"443\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("proxyName=\"" + String.format("%s.com", product.name()));
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"jira", "confluence"}, mode = EnumSource.Mode.INCLUDE)
    void use_custom_server_xml(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product.name() + ".tomcatConfig.generateByHelm", "true",
                product.name() + ".seraphConfig.generateByHelm", "true",
                product.name() + ".tomcatConfig.customServerXml", "<xml><Server>proxyName=\"example.com\"</Server></xml>"
        ));

        // assert custom server.xml is used in configmap data
        KubeResource serverConfigMap = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-server-config");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("proxyName=\"example.com\"");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"jira", "confluence"}, mode = EnumSource.Mode.INCLUDE)
    void enable_server_xml_config_map_ingress_defaults(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product.name() + ".tomcatConfig.generateByHelm", "true",
                "ingress.host", String.format("%s.com", product.name())
        ));

        // assert custom server.xml is used in configmap data
        KubeResource serverConfigMap = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-server-config");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("proxyPort=\"443\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("secure=\"true\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("scheme=\"https\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("proxyName=\"" + String.format("%s.com", product.name()));
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"jira", "confluence"}, mode = EnumSource.Mode.INCLUDE)
    void enable_server_xml_config_map_ingress_http(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product.name() + ".tomcatConfig.generateByHelm", "true",
                "ingress.https", "false",
                "ingress.host", String.format("%s.com", product.name())
        ));

        // assert that ingress related properties are correctly set
        KubeResource serverConfigMap = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-server-config");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("proxyPort=\"80\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("secure=\"false\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("scheme=\"http\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("proxyName=\"" + String.format("%s.com", product.name()));
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"jira", "confluence"}, mode = EnumSource.Mode.INCLUDE)
    void server_xml_proxy_secure_overrides(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product.name() + ".tomcatConfig.generateByHelm", "true",
                product.name() + ".tomcatConfig.proxyPort", "1234",
                product.name() + ".tomcatConfig.secure", "true",
                product.name() + ".tomcatConfig.scheme", "https",
                product.name() + ".tomcatConfig.proxyName", "foo.bar"
        ));

        // assert that ingress related properties are correctly set
        KubeResource serverConfigMap = resources.get(Kind.ConfigMap, product.getHelmReleaseName() + "-server-config");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("proxyPort=\"1234\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("secure=\"true\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("scheme=\"https\"");
        assertThat(serverConfigMap.getConfigMapData().path("server.xml")).hasTextContaining("proxyName=\"foo.bar\"");
    }
}
