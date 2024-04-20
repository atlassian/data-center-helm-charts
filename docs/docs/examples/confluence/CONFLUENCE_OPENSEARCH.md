# Configuring OpenSearch for Confluence

!!!info "Confluence and Helm chart version"
    OpenSearch is supported in Confluence 8.9.0 and Helm chart 1.19 onwards.

As Confluence instances grow in size and scale, the default search engine, Lucene, may be slower to index and return search results. To address this, Confluence Data Center offers an alternative search engine as an opt-in feature — OpenSearch.

Find more information on advantages of using OpenSearch in the [official documentation](https://confluence.atlassian.com/doc/configuring-opensearch-for-confluence-1387594125.html){.external}

## Deploy OpenSearch Helm Chart with Confluence

!!!warning "Support disclaimer"
    Atlassian does not officially support OpenSearch Helm chart that can be installed with the Confluence Helm release. Should you encounter any issues with the deployment, maintenance and upgrades, reach out to the [vendor](https://github.com/opensearch-project/helm-charts/tree/main/charts/opensearch){.external}.
    Moreover, if you intend to deploy OpenSearch to a critical Kubernetes environment, make sure you follow all the best practices, i.e. deploy a multi node cluster, use taints and tolerations, affinity rules, sufficient resources requests, have DR and backup strategies etc. 

## Deploy with the default settings

To deploy OpenSearch Helm chart and automatically configure Confluence to use it as a search platform, set the following in your Helm values file:

```yaml
opensearch:
  enabled: true
```
This will:

* auto-generate the initial OpenSearch admin password and create a Kubernetes secret with `OPENSEARCH_INITIAL_ADMIN_PASSWORD` key
* deploy [OpenSearch Helm chart](https://github.com/opensearch-project/helm-charts/tree/main/charts/opensearch){.external} to the target namespace with the default settings: single node, 1Gi memory/1 vCPU resources requests, 10Gi storage request
* configure Confluence to use the deployed OpenSearch cluster by adding `-Dsearch.platform=opensearch -Dopensearch.http.url=http://opensearch-cluster-master:9200 -Dopensearch.username=admin -Dopensearch.password=yourPassword` to the [JVM ConfigMap](https://github.com/atlassian/data-center-helm-charts/blob/main/src/main/charts/confluence/templates/config-jvm.yaml){.external}

## Override OpenSearch Helm chart values

You can configure your OpenSearch cluster and the deployment options by overriding any values that the [Helm chart](https://github.com/opensearch-project/helm-charts/blob/main/charts/opensearch/values.yaml){.external} exposes. OpenSearch values must be nested under `opensearch` stanza in your Helm values file, for example:

```yaml
opensearch:
  singleNode: false
  replicas: 5
  config:
    opensearch.yml: |
      cluster.name: opensearch-cluster
```
