package test.postinstall;

import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetrics;
import io.vavr.collection.Array;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import org.assertj.core.description.Description;
import org.assertj.core.description.LazyTextDescription;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import test.model.Product;

import java.util.function.Consumer;

import static io.restassured.RestAssured.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.hamcrest.Matchers.*;
import static test.postinstall.Utils.*;

@EnabledIf("isESDeployed")
class ElasticSearchInstallTest {
    static boolean isESDeployed() {
        return productIs(Product.bitbucket) && esInstalled();
    }

    private static KubeClient client;
    private static String esIngressBase;

    @BeforeAll
    static void initKubeClient() {
        client = new KubeClient();

        // See helm_install.sh for where this host is generated.
        final var ingressDomain = getIngressDomain(client.getClusterType());
        esIngressBase = "https://" + getRelease() + "-elasticsearch-master-0."+ingressDomain;
    }

    @Test
    void elasticSearchIsRunning() {
        var esSetName = getRelease() + "-" + "elasticsearch-master";
        client.forEachPodOfStatefulSet(esSetName, pod -> {
            final var podPhase = pod.getStatus().getPhase();
            assertThat(podPhase)
                    .describedAs("Pod %s should be running", pod.getMetadata().getName())
                    .isEqualToIgnoringCase("Running");
        });
    }

    @Test
    void elasticSearchBeingUsed() {
        // Relies on the backdoor ingress controller installed by helm_install.sh.
        // If this changes an alternative would be to use the fabric8 client ExecWatch/ExecListener to
        // invoke curl from a pod.
        final var indexURL = esIngressBase + "/_cat/indices?format=json";
        when().get(indexURL).then()
                .body("findAll { it.index == 'bitbucket-index-version' }[0]", hasEntry("docs.count", "1"));
    }

    @AfterAll
    static void disposeOfClient() {
        client.close();
    }
}
