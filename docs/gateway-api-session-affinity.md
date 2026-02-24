# Session Affinity with Gateway API

Atlassian DC products (Jira, Confluence, Bitbucket, Bamboo, Crowd) require **sticky sessions** so that a user is consistently routed to the same pod. With the NGINX Ingress controller this was handled automatically via annotations:

```yaml
nginx.ingress.kubernetes.io/affinity: "cookie"
nginx.ingress.kubernetes.io/affinity-mode: "persistent"
```

With Gateway API, session affinity is **not** part of the standard HTTPRoute spec and must be configured separately.

### Cookie naming

Use a **dedicated routing cookie** per product (e.g. `ATLROUTE_JIRA`) rather than the application's own `JSESSIONID`. All Atlassian DC products run on Tomcat, which sets `JSESSIONID` to track the user's HTTP session. If the load balancer also uses `JSESSIONID` for routing, the first request creates a race condition: both Envoy and Tomcat emit `Set-Cookie: JSESSIONID` with different values, and on the next request the hash may route to a different pod -- breaking the session. A separate cookie avoids this entirely and mirrors how NGINX Ingress used its own `route` cookie.

Recommended cookie names:

| Product | Cookie |
|---|---|
| Jira | `ATLROUTE_JIRA` |
| Confluence | `ATLROUTE_CONFLUENCE` |
| Bitbucket | `ATLROUTE_BITBUCKET` |
| Bamboo | `ATLROUTE_BAMBOO` |
| Crowd | `ATLROUTE_CROWD` |

## Options at a glance

| Approach | Cookie-based | Standard channel | Production-ready |
|----------|:---:|:---:|:---:|
| Implementation policy (Option 1) | Yes | N/A (separate CRD) | Yes |
| Experimental `sessionPersistence` (Option 2) | Yes | No (experimental) | Depends on implementation |
| Service `sessionAffinity: ClientIP` (Option 3) | No (IP-based) | Yes | Limited |

---

## Option 1 -- Implementation-specific policies (recommended)

Each Gateway API implementation provides its own policy resource for session affinity. Create the appropriate resource in the **same namespace** as your Helm release. Replace `<release-name>` with your actual Helm release name.

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
        name: ATLROUTE_<PRODUCT>   # e.g. ATLROUTE_JIRA -- see cookie naming above
        ttl: 10h
```

**Prerequisite:** The Envoy Gateway policy CRDs must be installed. They are included when installing Envoy Gateway via its Helm chart (`gateway-helm`). If you installed Gateway API CRDs separately, you may need to ensure the Envoy Gateway CRDs are also present:

```bash
kubectl get crd backendtrafficpolicies.gateway.envoyproxy.io
```

**References:**
- [Envoy Gateway BackendTrafficPolicy](https://gateway.envoyproxy.io/latest/api/extension_types/#backendtrafficpolicy)
- [Envoy Gateway session persistence task](https://gateway.envoyproxy.io/latest/tasks/traffic/load-balancing/)

### Istio

```yaml
apiVersion: networking.istio.io/v1
kind: DestinationRule
metadata:
  name: <product>-session-affinity
  namespace: <your-namespace>
spec:
  host: <release-name>-<product>
  trafficPolicy:
    loadBalancer:
      consistentHash:
        httpCookie:
          name: ATLROUTE_<PRODUCT>   # e.g. ATLROUTE_CONFLUENCE -- see cookie naming above
          ttl: 36000s
```

> For Istio versions older than 1.22, use `apiVersion: networking.istio.io/v1beta1`.

**Reference:** [Istio DestinationRule -- ConsistentHashLB](https://istio.io/latest/docs/reference/config/networking/destination-rule/#LoadBalancerSettings-ConsistentHashLB)

### NGINX Gateway Fabric

NGINX Gateway Fabric does not currently expose cookie-based session affinity through a policy resource. Use Kubernetes Service-level affinity (Option 3) as a fallback, or check the [NGINX Gateway Fabric documentation](https://docs.nginx.com/nginx-gateway-fabric/) for updates.

---

## Option 2 -- Experimental `sessionPersistence` on HTTPRoute

The Gateway API project has an **experimental** `sessionPersistence` field on HTTPRoute rules, tracked in [GEP-1619](https://gateway-api.sigs.k8s.io/geps/gep-1619/). This field is **not** included in the standard-channel CRDs and will cause validation errors if those are installed.

### Step 1: Install experimental-channel CRDs

```bash
# Replace the version with the Gateway API release you want
kubectl apply -f https://github.com/kubernetes-sigs/gateway-api/releases/download/v1.2.1/experimental-install.yaml
```

Check available versions at <https://github.com/kubernetes-sigs/gateway-api/releases>.

### Step 2: Add `sessionPersistence` to HTTPRoute rules

Use the `gateway.additionalRules` value in your Helm values to inject a rule with session persistence:

```yaml
gateway:
  create: true
  additionalRules:
  - matches:
    - path:
        type: PathPrefix
        value: /
    sessionPersistence:
      sessionName: ATLROUTE_<PRODUCT>   # use a dedicated cookie, not JSESSIONID
      type: Cookie
      absoluteTimeout: 10h
      idleTimeout: 1h
    backendRefs:
    - name: <release-name>-<product>
      port: 80
```

> Not all Gateway implementations support this field yet even with experimental CRDs installed. Check your implementation's conformance report.

**References:**
- [GEP-1619 -- Session Persistence](https://gateway-api.sigs.k8s.io/geps/gep-1619/)
- [SessionPersistence spec](https://gateway-api.sigs.k8s.io/reference/spec/#gateway.networking.k8s.io/v1.SessionPersistence)

---

## Option 3 -- Kubernetes Service-level affinity (fallback)

All charts already support `sessionAffinity` on the Kubernetes Service. Set this in your Helm values:

```yaml
jira:
  service:
    sessionAffinity: ClientIP
    sessionAffinityConfig:
      clientIP:
        timeoutSeconds: 10800
```

Replace `jira` with the product you are deploying (`confluence`, `bitbucket`, `bamboo`, `crowd`).

This uses **IP-based** affinity, not cookies. It will not work correctly when multiple users share the same source IP (corporate NAT, proxies, cloud load balancers).

**Reference:** [Kubernetes Service session affinity](https://kubernetes.io/docs/reference/networking/virtual-ips/#session-affinity)
