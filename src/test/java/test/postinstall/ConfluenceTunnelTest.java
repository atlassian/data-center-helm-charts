package test.postinstall;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.KubeResource;
import test.model.Product;

import java.util.Map;

import static test.jackson.JsonNodeAssert.assertThat;

public class ConfluenceTunnelTest {

    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"})
    void confluenceTunnelJvmArg(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(Product.confluence, Map.of(
                "confluence.tunnel.additionalConnector.port", "8093"));

        final var configMap = resources.getConfigMap(product.getHelmReleaseName() + "-jvm-config").getDataByKey("additional_jvm_args");
        assertThat(configMap).hasTextContaining("-Dsecure.tunnel.upstream.port=8093");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"})
    void confluenceAdditionalConnectorVarsDefaults(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "confluence.tunnel.additionalConnector.port", "8093"));

        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer()
                .getEnv()
                .assertHasValue("ATL_TOMCAT_ADDITIONAL_CONNECTOR_PORT", "8093")
                .assertHasValue("ATL_TOMCAT_ADDITIONAL_CONNECTOR_CONNECTION_TIMEOUT", "20000")
                .assertHasValue("ATL_TOMCAT_ADDITIONAL_CONNECTOR_MAX_THREADS", "50")
                .assertHasValue("ATL_TOMCAT_ADDITIONAL_CONNECTOR_MIN_SPARE_THREADS", "10")
                .assertHasValue("ATL_TOMCAT_ADDITIONAL_CONNECTOR_ENABLE_LOOKUPS", "false")
                .assertHasValue("ATL_TOMCAT_ADDITIONAL_CONNECTOR_ACCEPT_COUNT", "10")
                .assertHasValue("ATL_TOMCAT_ADDITIONAL_CONNECTOR_URI_ENCODING", "UTF-8");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"})
    void confluenceAdditionalConnectorVarsOverrides(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "confluence.tunnel.additionalConnector.port", "8093",
                "confluence.tunnel.additionalConnector.connectionTimeout", "30000",
                "confluence.tunnel.additionalConnector.maxThreads", "100",
                "confluence.tunnel.additionalConnector.minSpareThreads", "20",
                "confluence.tunnel.additionalConnector.enableLookups", "true",
                "confluence.tunnel.additionalConnector.acceptCount", "20",
                "confluence.tunnel.additionalConnector.URIEncoding", "ISO-8859-1",
                "confluence.tunnel.additionalConnector.secure", "true"

        ));

        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer()
                .getEnv()
                .assertHasValue("ATL_TOMCAT_ADDITIONAL_CONNECTOR_PORT", "8093")
                .assertHasValue("ATL_TOMCAT_ADDITIONAL_CONNECTOR_CONNECTION_TIMEOUT", "30000")
                .assertHasValue("ATL_TOMCAT_ADDITIONAL_CONNECTOR_MAX_THREADS", "100")
                .assertHasValue("ATL_TOMCAT_ADDITIONAL_CONNECTOR_MIN_SPARE_THREADS", "20")
                .assertHasValue("ATL_TOMCAT_ADDITIONAL_CONNECTOR_ENABLE_LOOKUPS", "true")
                .assertHasValue("ATL_TOMCAT_ADDITIONAL_CONNECTOR_ACCEPT_COUNT", "20")
                .assertHasValue("ATL_TOMCAT_ADDITIONAL_CONNECTOR_URI_ENCODING", "ISO-8859-1")
                .assertHasValue("ATL_TOMCAT_ADDITIONAL_CONNECTOR_SECURE", "true");
    }
}
