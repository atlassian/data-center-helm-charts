package test.postinstall;

import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.vavr.Lazy;
import io.vavr.collection.Array;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

class PostInstallStatusTest {
    private static String helmReleaseName;
    private static String namespaceName;

    private static final Lazy<KubernetesClient> clientRef = Lazy.of(DefaultKubernetesClient::new);

    @Test
    void applicationPodsShouldAllBeRunning() {
        forEachPodOfStatefulSet((idx, pod) ->
                assertThat(pod.getStatus().getPhase())
                        .describedAs("Pod %s should be running", pod.getMetadata().getName())
                        .isEqualToIgnoringCase("running"));
    }

    @Test
    void applicationPodContainersShouldAllBeReady() {
        forEachPodOfStatefulSet((idx, pod) -> {
            final var containerStatuses = Array.ofAll(pod.getStatus().getContainerStatuses());
            final var containerNames = containerStatuses.map(ContainerStatus::getName).mkCharSeq(",");

            assertThat(containerStatuses)
                    .extracting(ContainerStatus::getReady)
                    .describedAs("Containers %s of pod %s should all be ready", containerNames, pod.getMetadata().getName())
                    .containsOnly(true);
        });
    }

    @Test
    void allPersistentVolumeClaimsShouldBeBound() {
        final var volumeClaims = clientRef.get().persistentVolumeClaims()
                .withLabel("app.kubernetes.io/instance", helmReleaseName)
                .list()
                .getItems();

        for (var volumeClaim : volumeClaims) {
            assertThat(volumeClaim.getStatus().getPhase())
                    .isEqualToIgnoringCase("bound");
        }
    }

    private void forEachPodOfStatefulSet(BiConsumer<Integer, Pod> consumer) {
        final var statefulSetName = helmReleaseName;
        final var statefulSet = getStatefulSet(statefulSetName);

        assertThat(statefulSet)
                .describedAs("StatefulSet %s not found", statefulSetName)
                .isNotNull();

        final var replicaCount = statefulSet.getStatus().getReplicas();

        for (var idx = 0; idx < replicaCount; idx++) {
            consumer.accept(idx, getPod(statefulSetName + "-" + idx));
        }
    }

    @Nullable
    private StatefulSet getStatefulSet(String statefulSetName) {
        return clientRef.get()
                .apps().statefulSets()
                .inNamespace(namespaceName)
                .withName(statefulSetName)
                .get();
    }

    @Nullable
    private Pod getPod(final String podName) {
        return clientRef.get()
                .pods()
                .inNamespace(namespaceName)
                .withName(podName)
                .get();
    }

    @BeforeAll
    static void configure() throws Exception {
        final var helmParametersFileLocation = System.getProperty("helmParametersFileLocation");
        if (helmParametersFileLocation != null) {
            final var helmParameters = Utils.readPropertiesFile(Path.of(helmParametersFileLocation));

            helmReleaseName = Array.empty()
                    .appendAll(helmParameters.get("RELEASE_PREFIX"))
                    .appendAll(helmParameters.get("PRODUCT_NAME"))
                    .mkString("-");
            namespaceName = Array.empty()
                    .appendAll(helmParameters.get("TARGET_NAMESPACE"))
                    .mkString();
        } else {
            helmReleaseName = System.getProperty("helmRelease");
            namespaceName = System.getProperty("namespace");
        }

        assumeThat(helmReleaseName)
                .describedAs("Cannot run test without Helm release name")
                .isNotEmpty();
        assumeThat(namespaceName)
                .describedAs("Cannot run test without namespace name")
                .isNotEmpty();
    }

    @AfterAll
    static void disposeOfClient() {
        clientRef.forEach(KubernetesClient::close);
    }
}
