# Session Stickiness for Bitbucket with NGINX Ingress

When running Bitbucket Data Center in High Availability (HA) mode with multiple pods, session stickiness ensures that user requests are consistently routed to the same Bitbucket pod throughout their session.

!!!info "Why session stickiness is important for Bitbucket"
    * **Git operations**: Prevents Git push/pull failures when routed to different pods mid-operation
    * **User sessions**: Avoids unexpected logouts and session data loss
    * **Performance**: Maintains repository cache locality for better performance

## Automatic Session Stickiness (Default)

The Bitbucket Helm chart automatically enables session stickiness when using NGINX Ingress Controller:

```yaml
ingress:
  create: true
  nginx: true  # Automatically enables session stickiness
  host: bitbucket.example.com

bitbucket:
  clustering:
    enabled: true  # Required for HA deployments
```

This automatically applies these NGINX annotations:
```yaml
"nginx.ingress.kubernetes.io/affinity": "cookie"
"nginx.ingress.kubernetes.io/affinity-mode": "persistent"
```

## Custom Session Stickiness Configuration

You can customize the session behavior by adding additional annotations:

```yaml
ingress:
  create: true
  nginx: true
  host: bitbucket.example.com
  annotations:
    # Custom session cookie name (default: INGRESSCOOKIE)
    "nginx.ingress.kubernetes.io/session-cookie-name": "BITBUCKET-SESSION"
    # Cookie expiration time in seconds (default: 86400 = 24 hours)
    "nginx.ingress.kubernetes.io/session-cookie-max-age": "28800"
    # Whether to change cookie on backend failure (default: false)
    "nginx.ingress.kubernetes.io/session-cookie-change-on-failure": "true"

bitbucket:
  clustering:
    enabled: true
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