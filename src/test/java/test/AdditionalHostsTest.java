package test;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Deployment;
import test.model.Product;
import test.model.StatefulSet;

import java.util.Map;

import static test.jackson.JsonNodeAssert.assertThat;

class AdditionalHostsTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @ParameterizedTest
    @EnumSource(value = Product.class)
    void additional_hosts_included_in_confluence_and_synchrony(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                "synchrony.enabled", "true",
                "bitbucket.mesh.enabled", "true",
                "additionalHosts[0].ip", "127.0.0.1",
                "additionalHosts[0].hostnames[0]", "foo.local",
                "additionalHosts[0].hostnames[1]", "bar.local"
        ));

        if (product.name().equals("bamboo_agent")) {
            Deployment bambooAgent = resources.getDeployment(product.getHelmReleaseName());
            JsonNode hostAliases = bambooAgent.getPodSpec().get("hostAliases");
            assertThat(hostAliases.get(0).get("hostnames")).isArrayWithChildren("foo.local", "bar.local");
            assertThat(hostAliases.get(0).get("ip")).hasTextEqualTo("127.0.0.1");
        } else {
            StatefulSet dcProduct = resources.getStatefulSet(product.getHelmReleaseName());
            JsonNode hostAliases = dcProduct.getPodSpec().get("hostAliases");
            assertThat(hostAliases.get(0).get("hostnames")).isArrayWithChildren("foo.local", "bar.local");
            assertThat(hostAliases.get(0).get("ip")).hasTextEqualTo("127.0.0.1");
        }

        if (product.name().equals("confluence")) {
            StatefulSet synchronySts = resources.getStatefulSet("unittest-confluence-synchrony");
            JsonNode synchronyHostAliases = synchronySts.getPodSpec().get("hostAliases");
            assertThat(synchronyHostAliases.get(0).get("hostnames")).isArrayWithChildren("foo.local", "bar.local");
            assertThat(synchronyHostAliases.get(0).get("ip")).hasTextEqualTo("127.0.0.1");
        }

        if (product.name().equals("bitbucket")) {
            StatefulSet bitbucketMeshSts = resources.getStatefulSet("unittest-bitbucket-mesh");
            JsonNode bitbucketMeshHostAliases = bitbucketMeshSts.getPodSpec().get("hostAliases");
            assertThat(bitbucketMeshHostAliases.get(0).get("hostnames")).isArrayWithChildren("foo.local", "bar.local");
            assertThat(bitbucketMeshHostAliases.get(0).get("ip")).hasTextEqualTo("127.0.0.1");
        }
    }
}
