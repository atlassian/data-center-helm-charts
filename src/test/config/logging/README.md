For aggregation of all logs deployed by the DC products in a given namespace,
we use shared deployments of Kibana and Elasticsearch in that namespace.

Each DC product is configured with a `fluentd` sidecar which forwards the application logs
on to Elasticsearch. 

Kibana is installed via Helm chart, using the appropriate values file, e.g. for KITT:

    helm install kibana --version 7.10.2 elastic/kibana --values kibana/values-KITT.yaml

Similarly for Elasticsearch:

    helm install elasticsearch --version 7.10.2 elastic/elasticsearch --values elasticsearch/values-KITT.yaml

For KITT, you also need to define the HTTPProxy ingress for access to the Kibana UI:
   
    kubectl apply -f kibana/ingress-kitt.yaml