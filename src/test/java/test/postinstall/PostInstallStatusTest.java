package test.postinstall;

import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.vavr.Lazy;
import io.vavr.collection.Array;
import io.vavr.collection.Traversable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assumptions.assumeThat;
import static test.postinstall.Utils.getRemainingNodeCapacityDescription;

class PostInstallStatusTest {
    private static String helmReleaseName;
    private static String namespaceName;

    private static final Lazy<KubernetesClient> clientRef = Lazy.of(DefaultKubernetesClient::new);

    @Test
    void applicationPodsShouldAllBeRunning() {
        forEachPodOfStatefulSet(pod -> {
            final var podPhase = pod.getStatus().getPhase();
            if ("pending".equalsIgnoreCase(podPhase)) {
                schedulingFailure(pod);
            } else {
                assertThat(podPhase)
                        .describedAs("Pod %s should be running", pod.getMetadata().getName())
                        .isEqualToIgnoringCase("Running");
            }
        });
    }

    private void schedulingFailure(Pod pod) {
        fail(String.format("Pod %s should be running, but has yet to be scheduled. Current remaining node capacity is %s.",
                pod.getMetadata().getName(), getRemainingNodeCapacityDescription(getSelectedNodes())));
    }

    /**
     * @return all k8s custer nodes which are available for pod scheduling, based on the node selector in the
     * StatefulSet spec.
     */
    private Traversable<Node> getSelectedNodes() {
        return Array.ofAll(clientRef.get()
                .nodes()
                .withLabels(getStatefulSet()
                        .getSpec()
                        .getTemplate()
                        .getSpec()
                        .getNodeSelector())
                .list()
                .getItems());
    }

    @Test
    void applicationPodContainersShouldAllBeReady() {
        forEachPodOfStatefulSet(pod -> {
            final var containerStatuses = Array.ofAll(pod.getStatus().getContainerStatuses());

            assumeThat(containerStatuses)
                    .describedAs("No container statuses present in pod %s", pod.getMetadata().getName())
                    .isNotEmpty();

            final var containerNames = containerStatuses.map(ContainerStatus::getName).mkCharSeq(",");

            assertThat(containerStatuses)
                    .extracting(ContainerStatus::getReady)
                    .describedAs("Containers %s of pod %s should all be ready", containerNames, pod.getMetadata().getName())
                    .containsOnly(true);
        });
    }

    private void forEachPodOfStatefulSet(Consumer<Pod> consumer) {
        final var statefulSet = getStatefulSet();

        final var replicaCount = statefulSet.getStatus().getReplicas();

        for (var idx = 0; idx < replicaCount; idx++) {
            consumer.accept(getPod(statefulSet.getMetadata().getName() + "-" + idx));
        }
    }

    private StatefulSet getStatefulSet() {
        final var statefulSetName = helmReleaseName;
        final var statefulSet = getStatefulSet(statefulSetName);

        assertThat(statefulSet)
                .describedAs("StatefulSet %s not found", statefulSetName)
                .isNotNull();
        return statefulSet;
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
