package test;

import io.vavr.collection.Traversable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import test.helm.Helm;
import test.model.Kind;
import test.model.KubeResource;
import test.model.Product;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        final var replicaCount = 2;
        final var resources = helm.captureKubeResourcesFromHelmChart(product,
                Map.of(product + ".mesh.enabled", "true",
                        product + ".mesh.replicaCount", String.valueOf(replicaCount),
                        product + ".sysadminCredentials.secretName", "secret-name"));

        Traversable<KubeResource> all = resources.getAll(Kind.Job);

        KubeResource configureJob = resources.get(Kind.Job, product.getHelmReleaseName() + "-mesh-configure-job");
        assertEquals(configureJob.getAnnotations().path("helm.sh/hook").asText(), "post-install");
        Traversable<KubeResource> registerJobs = all.filter(j -> j.getName().contains("mesh-register-job"));
        assertEquals(replicaCount, registerJobs.size());
        for (var rJob : registerJobs) {
            assertEquals(rJob.getAnnotations().path("helm.sh/hook").asText(), "post-install");
        }
    }
}
