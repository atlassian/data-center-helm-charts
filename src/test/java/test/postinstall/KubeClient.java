package test.postinstall;

import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetrics;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.vavr.Lazy;
import io.vavr.Tuple;
import io.vavr.collection.Array;
import io.vavr.collection.Traversable;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.model.ClusterType;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static test.postinstall.Utils.getNS;
import static test.postinstall.Utils.getRelease;

final class KubeClient implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(KubeClient.class);

    private final Lazy<KubernetesClient> clientRef = Lazy.of(DefaultKubernetesClient::new);

    ClusterType getClusterType() {
        final var host = client().getMasterUrl().getHost();
        if (host.contains("eks.amazonaws.com")) {
            return ClusterType.EKS;
        } else if (host.contains("azmk8s.io")) {
            return ClusterType.AKS;
        } else if (host.contains("kitt-inf.net")) {
            return ClusterType.KITT;
        } else {
            return ClusterType.UNKNOWN;
        }
    }

    io.vavr.collection.Map<Node, Option<NodeMetrics>> getNodeMetrics(final PodSpec podSpec) {
        return getNodes(podSpec.getNodeSelector())
                .toMap(node -> Tuple.of(node, getNodeMetrics(node)));
    }

    private Option<NodeMetrics> getNodeMetrics(Node node) {
        return Try.of(() -> client().top().nodes().metrics(node.getMetadata().getName()))
                .onFailure(ex -> log.warn("Failed to call K8S Ketrics API: {}", ex.getMessage()))
                .toOption();
    }

    /**
     * @return all k8s custer nodes which are available for pod scheduling, based on the node selector in the
     * StatefulSet spec.
     */
    private Traversable<Node> getNodes(final Map<String, String> nodeSelector) {
        return Array.ofAll(client()
                .nodes()
                .withLabels(nodeSelector)
                .list()
                .getItems());
    }

    @Nullable
    StatefulSet getStatefulSet(String statefulSetName, final String namespaceName) {
        return client()
                .apps().statefulSets()
                .inNamespace(namespaceName)
                .withName(statefulSetName)
                .get();
    }

    @Nullable
    Pod getPod(final String podName, final String namespaceName) {
        return client()
                .pods()
                .inNamespace(namespaceName)
                .withName(podName)
                .get();
    }

    void forEachPodOfStatefulSet(Consumer<Pod> consumer) {
        forEachPodOfStatefulSet(getRelease(), consumer);
    }

    void forEachPodOfStatefulSet(String statefulSetName, Consumer<Pod> consumer) {
        final var statefulSet = getStatefulSet(statefulSetName);
        final var replicaCount = statefulSet.getStatus().getReplicas();

        for (var idx = 0; idx < replicaCount; idx++) {
            consumer.accept(getPod(statefulSet.getMetadata().getName() + "-" + idx, getNS()));
        }
    }

    StatefulSet getStatefulSet() {
        return getStatefulSet(getRelease());
    }

    StatefulSet getStatefulSet(String statefulSetName) {
        final var statefulSet = getStatefulSet(statefulSetName, getNS());

        assertThat(statefulSet)
                .describedAs("StatefulSet %s not found", statefulSetName)
                .isNotNull();
        return statefulSet;
    }

    private KubernetesClient client() {
        return clientRef.get();
    }

    @Override
    public void close() {
        if (clientRef.isEvaluated()) {
            clientRef.forEach(KubernetesClient::close);
        }
    }
}
