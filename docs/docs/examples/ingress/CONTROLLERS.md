# Provisioning a traffic entry controller

To expose an Atlassian DC product outside your Kubernetes cluster, you must run **one** of the following:

- an **Ingress controller** (to process Kubernetes `Ingress` resources), or
- a **Gateway API controller** (to process Kubernetes Gateway API resources, such as `HTTPRoute`).

The Helm charts can render either:

- a Kubernetes `Ingress` when `ingress.create: true`, or
- a Kubernetes Gateway API `HTTPRoute` when `gateway.create: true`.

These options are **mutually exclusive** (you cannot enable both `ingress.create` and `gateway.create`).

!!!note "Sticky sessions are required"
    Atlassian DC products require **session stickiness** ("sticky sessions") for high availability. With NGINX Ingress this is handled via controller annotations. With Gateway API you must configure stickiness using your chosen Gateway implementation.

## Example guides

- [NGINX Ingress Controller (Ingress)](INGRESS_NGINX.md)
- [Gateway API controller (Gateway API)](GATEWAY_API.md)
