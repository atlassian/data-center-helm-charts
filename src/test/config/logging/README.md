For aggregation of all logs deployed by the DC products in a given namespace,
we use shared deployments of Kibana and Elasticsearch in that namespace.

Each DC product is configured with a `fluentd` sidecar which forwards the application logs
on to Elasticsearch. Since the standard fluent docker image doesn't contain the Elasticsearch
plugin, we need to build our own image which does, using [this Dockerfile](fluentd/Dockerfile)

    docker build ./fluentd -t docker.atl-paas.net/kmacleod/fluentd:1

This image needs to be named appropriately, and pushed to docker.atl-paas.net so that it can be pulled by K8s. It is
referred to in the DC product helm chart values file.

Kibana is installed via Helm chart, using the appropriate values file, e.g. for KITT:

    helm install kibana --version 7.10.2 elastic/kibana --values kibana/values-KITT.yaml

Similarly for Elasticsearch:

    helm install kibana --version 7.10.2 elastic/elasticsearch --values elasticsearch/values-KITT.yaml

For KITT, you also need to define the HTTPProxy ingress for access to the Kibana UI:
   
    kubectl apply -f kibana/ingress-kitt.yaml