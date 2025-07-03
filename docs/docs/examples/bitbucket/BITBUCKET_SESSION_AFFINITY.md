# Configure Session Stickiness with NGINX Ingress

## Default Configuration

```yaml
ingress:
  create: true
  nginx: true  # Enables session stickiness automatically
  host: bitbucket.example.com

bitbucket:
  clustering:
    enabled: true
```

## Custom Configuration

```yaml
ingress:
  create: true
  nginx: true
  host: bitbucket.example.com
  annotations:
    "nginx.ingress.kubernetes.io/session-cookie-name": "BITBUCKET-SESSION"
    "nginx.ingress.kubernetes.io/session-cookie-max-age": "28800"  # 8 hours
```

## Verifying Session Stickiness

Test that session stickiness is working:

```shell
# Check ingress annotations
kubectl describe ingress bitbucket -n atlassian

# Test with curl (look for Set-Cookie header)
curl -I https://bitbucket.example.com/status
```

## Troubleshooting

If users are experiencing random logouts or Git operation failures:

1. **Verify NGINX annotations are applied**:
   ```shell
   kubectl get ingress bitbucket -n atlassian -o yaml
   ```

2. **Check if requests hit the same pod**:
   ```shell
   kubectl logs -f -l app.kubernetes.io/name=bitbucket -n atlassian
   ```

3. **Ensure clustering is enabled**:
   ```yaml
   bitbucket:
     clustering:
       enabled: true
   ```

!!!tip "Production recommendation"
    Set `session-cookie-max-age` to match your typical user session duration (e.g., 8 hours = 28800 seconds) to balance user experience with security. 