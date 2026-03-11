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

Disable `ingress.create` and enable `gateway.create`. The key inputs are the **Gateway name** and at least one **hostname**.

```yaml
ingress:
  create: false

gateway:
  create: true
  gatewayName: atlassian-gateway
  # gatewayNamespace: gateway-system   # optional, defaults to release namespace
  hostnames:
    - confluence.example.com
  https: true
  path: "/"
  pathType: PathPrefix
```

!!!info "TLS termination"
    With Gateway API, TLS termination is configured on the `Gateway` listeners (not on the `HTTPRoute`). The `gateway.https` value controls the product's proxy/URL settings (e.g., generating HTTPS links), but it does not provision certificates by itself.

## 4. Configure session affinity (sticky sessions)

Session affinity is **required** for Atlassian DC products and is **not** part of the standard `HTTPRoute` API.

See [Session affinity with Gateway API](GATEWAY_API_SESSION_AFFINITY.md) for implementation-specific examples (cookie-based) and links for further reading.

