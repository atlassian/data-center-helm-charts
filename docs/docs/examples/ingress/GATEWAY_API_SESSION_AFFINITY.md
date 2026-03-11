# Session Affinity with Gateway API

Atlassian DC products require **sticky sessions** so that a user is consistently routed to the same pod. With the NGINX Ingress controller this was handled automatically via annotations:

```yaml
nginx.ingress.kubernetes.io/affinity: "cookie"
nginx.ingress.kubernetes.io/affinity-mode: "persistent"
```

With Gateway API, session affinity is **not** part of the standard `HTTPRoute` spec and must be configured separately.

## Cookie naming

Use a **dedicated routing cookie** (for example `ATLROUTE_<PRODUCT>`) rather than the application's own `JSESSIONID`. This avoids conflicts with the application's session cookie and matches the approach used by NGINX Ingress.

## Options at a glance

| Approach | Cookie-based | Standard channel |
|----------|:---:|:---:|
| Implementation policy (Option 1) | Yes | N/A (separate CRD) |
| Experimental `sessionPersistence` (Option 2) | Yes | No (experimental) |

---

## Implementation-specific policies (recommended)

Each Gateway API implementation provides its own policy resource for session affinity. Create the appropriate resource in the **same namespace** as your Helm release.

### Envoy Gateway

```yaml
apiVersion: gateway.envoyproxy.io/v1alpha1
kind: BackendTrafficPolicy
metadata:
  name: <product>-session-affinity
  namespace: <your-namespace>
spec:
  targetRefs:
  - group: gateway.networking.k8s.io
    kind: HTTPRoute
    name: <release-name>-<product>
  loadBalancer:
    type: ConsistentHash
    consistentHash:
      type: Cookie
      cookie:
        name: ATLROUTE_<PRODUCT>
        ttl: 10h
```

**Explore further:**

- Envoy Gateway load balancing & session persistence: <https://gateway.envoyproxy.io/latest/tasks/traffic/load-balancing/>
- Envoy Gateway `BackendTrafficPolicy` API: <https://gateway.envoyproxy.io/latest/api/extension_types/#backendtrafficpolicy>
- Istio consistent-hash via `DestinationRule`: <https://istio.io/latest/docs/reference/config/networking/destination-rule/#LoadBalancerSettings-ConsistentHashLB>

---

## Experimental `sessionPersistence` on HTTPRoute

The Gateway API project has an **experimental** `sessionPersistence` field on HTTPRoute rules, tracked in **GEP-1619**. This field is **not** included in the standard-channel CRDs and will cause validation errors if those are installed.

**Explore further:**

- GEP-1619: <https://gateway-api.sigs.k8s.io/geps/gep-1619/>
- `SessionPersistence` field reference: <https://gateway-api.sigs.k8s.io/reference/spec/#gateway.networking.k8s.io/v1.SessionPersistence>
- Experimental CRDs install (pick a release): <https://github.com/kubernetes-sigs/gateway-api/releases>

!!!warning "Implementation support varies"
    Even with experimental CRDs installed, not all Gateway implementations support `sessionPersistence`. Check your implementation's conformance/support documentation.
