# Configure AWS OpenSearch with Bitbucket

## Configuration Example

```yaml
bitbucket:
  # Disable default OpenSearch deployment
  opensearch:
    enabled: false

  # Configure AWS OpenSearch connection
  additionalEnvironmentVariables:
    - name: SEARCH_ENABLED
      value: "true"
    - name: PLUGIN_SEARCH_CONFIG_BASEURL
      value: "https://your-opensearch-endpoint.region.es.amazonaws.com"
    - name: PLUGIN_SEARCH_CONFIG_USERNAME
      valueFrom:
        secretKeyRef:
          name: aws-opensearch-credentials
          key: username
    - name: PLUGIN_SEARCH_CONFIG_PASSWORD
      valueFrom:
        secretKeyRef:
          name: aws-opensearch-credentials
          key: password
```

## Create AWS OpenSearch Credentials Secret

```shell
kubectl create secret generic aws-opensearch-credentials \
  --from-literal=username=your-username \
  --from-literal=password=your-password \
  -n your-namespace
``` 