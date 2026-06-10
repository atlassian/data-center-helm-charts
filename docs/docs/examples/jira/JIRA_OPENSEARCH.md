# Configuring OpenSearch for Jira

!!!info "Jira and Helm chart version"
    OpenSearch is supported in Jira 11.2 and Helm chart 2.0.10 onwards.

As Jira instances grow in size and scale, the default search engine, Lucene, may be slower to index and return search results. To address this, Jira Data Center offers an alternative search engine as an opt-in feature — OpenSearch.

## Deploy OpenSearch Helm Chart with Jira

!!!warning "Support disclaimer"
    Atlassian does not officially support OpenSearch Helm chart that can be installed with the Jira Helm release. Should you encounter any issues with the deployment, maintenance and upgrades, reach out to the [vendor](https://github.com/opensearch-project/helm-charts/tree/main/charts/opensearch){.external}.
    Moreover, if you intend to deploy OpenSearch to a critical Kubernetes environment, make sure you follow all the best practices, i.e. deploy a multi node cluster, use taints and tolerations, affinity rules, sufficient resources requests, have DR and backup strategies etc.

## Deploy with the default settings

To deploy OpenSearch Helm chart and automatically configure Jira to use it as a search platform, set the following in your Helm values file:

```yaml
opensearch:
  enabled: true
```
This will:

* auto-generate the initial OpenSearch admin password and create a Kubernetes secret with `OPENSEARCH_INITIAL_ADMIN_PASSWORD` key
* deploy [OpenSearch Helm chart](https://github.com/opensearch-project/helm-charts/tree/main/charts/opensearch){.external} to the target namespace with the default settings: single node, 1Gi memory/1 vCPU resources requests, 10Gi storage request
* configure Jira to use the deployed OpenSearch cluster by setting opensearch properties, which are written to `jira-config.properties` at startup. The following properties are configured: `search.platform=opensearch`, `opensearch.http.url=http://opensearch-cluster-master:9200`, `opensearch.username=admin`, and `opensearch.password`.

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

## Use an existing OpenSearch secret

If you have a pre-created Kubernetes secret with the OpenSearch admin password, you can reference it instead of having the chart auto-generate one:

```yaml
opensearch:
  enabled: true
  credentials:
    createSecret: false
    existingSecretRef:
      name: my-opensearch-secret
```

The secret must contain a key named `OPENSEARCH_INITIAL_ADMIN_PASSWORD`.

## Connect to an external OpenSearch instance

If you already have an OpenSearch cluster running outside of Kubernetes (or
managed separately), you can configure Jira to use it without deploying the
bundled OpenSearch Helm chart. OpenSearch properties are written to
`jira-config.properties` at container startup via the
`additionalConfigProperties` mechanism (see
[Additional config properties](../../userguide/CONFIGURATION.md#additional-config-properties)).

First, create a Kubernetes Secret containing the OpenSearch password:

```bash
kubectl create secret generic opensearch-credentials \
  --from-literal=password='<your-opensearch-password>'
```

Then reference it in your Helm values file. The `additionalEnvironmentVariables`
entry injects the secret as an environment variable, and
`additionalConfigPropertiesExpandEnv` expands it into the property value at
startup:

```yaml
jira:
  additionalEnvironmentVariables:
    - name: MY_OPENSEARCH_PASSWORD
      valueFrom:
        secretKeyRef:
          name: opensearch-credentials
          key: password

  additionalConfigProperties:
    - "search.platform=opensearch"
    - "opensearch.http.url=http://opensearch-host:9200"
    - "opensearch.username=admin"

  additionalConfigPropertiesExpandEnv:
    - "opensearch.password={MY_OPENSEARCH_PASSWORD}"
```

For the full list of available OpenSearch properties, see
[Configuring OpenSearch for Jira](https://confluence.atlassian.com/adminjiraserver/configuring-opensearch-for-jira-1620511851.html){.external}.
