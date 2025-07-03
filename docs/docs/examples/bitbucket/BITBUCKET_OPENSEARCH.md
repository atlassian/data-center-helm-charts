# Configure OpenSearch with Bitbucket

## Deploy OpenSearch Helm Chart

```yaml
bitbucket:
  clustering:
    enabled: true
  opensearch:
    enabled: true
    install: true
```

## Use AWS OpenSearch Service

```yaml
bitbucket:
  clustering:
    enabled: true
  opensearch:
    enabled: false  # Disable Helm chart deployment
  
  additionalEnvironmentVariables:
    - name: SEARCH_ENABLED
      value: "false"
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

## Create Credentials Secret

```shell
kubectl create secret generic aws-opensearch-credentials \
  --from-literal=username=your-username \
  --from-literal=password=your-password \
  -n your-namespace
```

## Common Issues

**Connection failures**: Verify the OpenSearch endpoint URL and network connectivity from your EKS cluster.

**Authentication errors**: Check credentials and ensure the OpenSearch domain access policy allows your EKS cluster.

**Test connectivity**:
```shell
kubectl exec -ti bitbucket-0 -n atlassian -- curl -u username:password https://your-opensearch-domain/_cluster/health
```
