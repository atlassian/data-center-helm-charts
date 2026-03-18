# Gateway API controller (HTTPRoute)

The Atlassian DC Helm charts support exposing products via the **Kubernetes Gateway API** by rendering a `HTTPRoute` resource when `gateway.create: true`.

To use this, your cluster must have:

- Gateway API CRDs installed
- a Gateway API controller installed (for example, Envoy Gateway, Istio, etc.)
- a `Gateway` resource that allows routes from your product namespace

!!!note "What the charts create"
    The charts create a **`HTTPRoute`** only. You must provision the **`GatewayClass`**, **`Gateway`**, and (optionally) **TLS certificates** in your cluster.

## 1. Install a Gateway API controller

Follow your chosen implementation's installation instructions:

- Gateway API overview: <https://gateway-api.sigs.k8s.io/>
- Implementations: <https://gateway-api.sigs.k8s.io/implementations/>

## 2. Create a Gateway

Create a `Gateway` that will accept `HTTPRoute` attachments from the namespace where you install the Atlassian product.

The exact `gatewayClassName`, listener configuration, and TLS configuration depend on your chosen implementation.

## 3. Configure the Helm chart

Disable `ingress.create` and enable `gateway.create`. Provide a **parentRef** pointing to your `Gateway` and at least one **hostname**.

```yaml
ingress:
  create: false

gateway:
  create: true
  hostnames:
    - confluence.example.com
  https: true
  parentRefs:
    - name: atlassian-gateway
      namespace: gateway-system   # optional, defaults to release namespace
      sectionName: https          # optional, target a specific Gateway listener
```

!!!info "TLS termination"
    With Gateway API, TLS termination is configured on the `Gateway` listeners (not on the `HTTPRoute`). The `gateway.https` value controls the product's proxy/URL settings (e.g., generating HTTPS links), but it does not provision certificates by itself.

## Gateway values reference

The `gateway` stanza is split into two groups:

**Product configuration** (always active when `gateway.hostnames` is set):

| Value | Description | Default |
|-------|-------------|---------|
| `gateway.create` | Create an `HTTPRoute` resource | `false` |
| `gateway.hostnames` | Hostnames to route; first entry is used as the canonical hostname for base URL and proxy settings | `[]` |
| `gateway.https` | Whether users access the application over HTTPS | `true` |
| `gateway.externalPort` | Port users connect on; only set for non-standard ports | `443` (https) / `80` (http) |
| `gateway.path` | Base path; falls back to `<product>.service.contextPath` when empty | (empty) |

**HTTPRoute configuration** (only applies when `gateway.create: true`):

| Value | Description | Default |
|-------|-------------|---------|
| `gateway.parentRefs` | List of [ParentReference](https://gateway-api.sigs.k8s.io/reference/spec/#gateway.networking.k8s.io/v1.ParentReference){.external} objects (`name`, `namespace`, `sectionName`, etc.) | `[]` (required) |
| `gateway.pathType` | Path matching type: `PathPrefix`, `Exact`, or `RegularExpression` | `PathPrefix` |
| `gateway.annotations` | Annotations to add to the HTTPRoute | `{}` |
| `gateway.labels` | Labels to add to the HTTPRoute | `{}` |
| `gateway.filters` | [HTTPRouteFilter](https://gateway-api.sigs.k8s.io/reference/spec/#gateway.networking.k8s.io/v1.HTTPRouteFilter){.external} list (header modification, redirects, URL rewrites) | `[]` |
| `gateway.timeouts.request` | Total request timeout | `60s` |
| `gateway.timeouts.backendRequest` | Backend request timeout | `60s` |
| `gateway.additionalRules` | Extra [HTTPRouteRule](https://gateway-api.sigs.k8s.io/reference/spec/#gateway.networking.k8s.io/v1.HTTPRouteRule){.external} entries for advanced routing | `[]` |

!!!note "Using gateway config without creating an HTTPRoute"
    Setting `gateway.hostnames` activates gateway mode for the product's proxy and base-URL configuration **even when `gateway.create` is false**. This is useful when you have a pre-existing Gateway or external proxy/load balancer and only need the Helm chart to configure the product itself, without creating any Kubernetes routing resource.

## 4. Timeouts

The `gateway.timeouts` block replaces the Ingress-style `proxyReadTimeout` / `proxySendTimeout` settings:

```yaml
gateway:
  timeouts:
    request: "60s"        # total request timeout
    backendRequest: "60s" # backend request timeout
```

!!!warning "No Gateway API equivalents"
    There is no standard Gateway API equivalent for `proxyConnectTimeout` or `maxBodySize`. If you need those, configure them through controller-specific policies (e.g. Envoy Gateway `BackendTrafficPolicy`).


## Configure session affinity (sticky sessions)

Session affinity is **required** for Atlassian DC products and is **not** part of the standard `HTTPRoute` API.

See [Session affinity with Gateway API](GATEWAY_API_SESSION_AFFINITY.md) for implementation-specific examples (cookie-based) and links for further reading.

