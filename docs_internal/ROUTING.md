# Routing: Ingress vs Gateway Internals

This document explains how the `common.gateway.*` template helpers abstract over
the `ingress` and `gateway` values sections, and why they are shaped this way.
Read this when you need to understand the full picture.

## The two values sections

Users configure external access via one of two values sections:

```yaml
ingress: # K8s Ingress API path
  host: ...
  https: ...
  path: ...

gateway: # K8s Gateway API path (or external proxy)
  hostnames: [ ... ]
  https: ...
  externalPort: ...
  path: ...
```

Both sections carry two kinds of settings:

1. **Product routing** — hostname, https, port, path. These tell the application
   how users reach it (proxy settings, base URL, etc.). Present at the top of
   each section.
2. **Resource creation** — className, annotations, gatewayName, filters, etc.
   These only matter when `create: true` and configure the Ingress/HTTPRoute
   resource. Present below the section divider in `values.yaml`.

The product routing settings are conceptually identical across both sections —
only the key names differ (`host` vs `hostnames`, `port` vs `externalPort`).

## Mode detection

The helpers use two concepts to decide which values to read:

| Helper           | Returns `"true"` when                        | Purpose                                            |
|------------------|----------------------------------------------|----------------------------------------------------|
| `useGatewayMode` | `gateway.hostnames` is non-empty             | Determines which section to read values from       |
| `isConfigured`   | `ingress.host` or `gateway.hostnames` is set | Determines if external access is configured at all |

Key design decisions:

- **Mode is implicit.** Setting `gateway.hostnames` activates gateway mode.
  There is no explicit toggle — `gateway.create` controls HTTPRoute creation,
  not mode selection.
- **Mutual exclusion.** Setting both `ingress.host` and `gateway.hostnames`
  is a validation error. The user must choose one path.
- **`gateway.create` only controls the HTTPRoute resource.** A user with a
  pre-existing Gateway or external load balancer can set `gateway.hostnames`
  without `gateway.create: true` and get correct product configuration
  (NOTE: only the first hostname will be used in such case).

## Helper dependency graph

```
isConfigured ──────────────────── used by: statefulset guards, NOTES.txt, bamboo.baseUrl
useGatewayMode ────┬───────────── used by: all helpers below
                   │
                   ├─ https ───── scheme
                   ├─ hostname    │
                   ├─ externalPort│
                   │              ▼
                   │           origin ── used by: product baseUrl, NOTES.txt, SETUP_BASEURL
                   │
                   └─ path ──────────── used by: product path helpers (jira.path, etc.)
```

## How values flow into the product

The helpers feed into two main outputs:

### 1. Environment variables (statefulset.yaml)

Guarded by `isConfigured` — only set when a hostname is configured.

| Env var                                | Helper            | Products  |
|----------------------------------------|-------------------|-----------|
| `ATL_PROXY_NAME` / `SERVER_PROXY_NAME` | `hostname`        | all       |
| `ATL_PROXY_PORT` / `SERVER_PROXY_PORT` | `externalPort`    | all       |
| `ATL_TOMCAT_SCHEME` / `SERVER_SCHEME`  | `scheme`          | all       |
| `ATL_TOMCAT_SECURE` / `SERVER_SECURE`  | `https`           | all       |
| `ATL_BASE_URL`                         | `origin` + `path` | bamboo    |
| `SETUP_BASEURL`                        | `origin`          | bitbucket |
| `SERVER_CONTEXT_PATH`                  | `path`            | bitbucket |

### 2. Tomcat server.xml (configmap-server-config.yaml)

Products that generate `server.xml` via Helm (jira, confluence, bamboo, crowd)
set the `<Connector>` proxy attributes using these helpers. The `proxyPort`
attribute uses `externalPort` as a default but can be overridden per-product
via `<product>.tomcatConfig.proxyPort`.

## Supported configurations

### Standard: Ingress with resource creation

```yaml
ingress:
  create: true
  host: app.example.com
  https: true
```

Creates an Ingress resource. Product configured via `ingress.*` values.

### Standard: Gateway API with HTTPRoute creation

```yaml
gateway:
  create: true
  hostnames: [ app.example.com ]
  https: true
  gatewayName: my-gateway
```

Creates an HTTPRoute. Product configured via `gateway.*` values.

### External proxy with gateway config (no resource created)

```yaml
gateway:
  create: false
  hostnames: [ app.example.com ]
  https: true
  externalPort: 8443
```

No K8s routing resource created. Product configured via `gateway.*` values.
Use this when traffic is routed by an external load balancer, a pre-existing
Gateway, or any proxy not managed by this chart.

### External proxy with ingress config (legacy, no resource created)

```yaml
ingress:
  create: false
  host: app.example.com
  https: true
```

Same as above but using `ingress.*` values. This is the legacy way to configure
an external proxy.

### Invalid: both configured

```yaml
ingress:
  host: app.example.com      # ← cannot set both
gateway:
  hostnames: [ app.example.com ]  # ← cannot set both
```

Fails validation with: "Cannot set both gateway.hostnames and ingress.host".

## Why "externalPort" instead of "port"?

The `ingress.port` field existed as an undocumented, partially working feature.
It was confusing for two reasons:

1. **Unclear purpose.** "Port" of what? Users would reasonably assume it
   configures the Ingress resource itself — but it doesn't. It never appeared
   in the generated Ingress spec. It only fed into the product's proxy env vars
   (`ATL_PROXY_PORT` / `SERVER_PROXY_PORT`).
2. **Inconsistent support.** It worked for the proxy port env var but was
   ignored by `bamboo.baseUrl` (so `ATL_BASE_URL` was wrong), ignored by all
   NOTES.txt outputs (displayed URL omitted the port), and ignored by the
   `configmap-server-config.yaml` `proxyPort` attribute (which had its own
   separate default).

The gateway section introduces `externalPort` to fix this:

- **Clear name.** "External port" immediately communicates: this is the port
  users hit to reach the application. Not an internal port, not a container
  port, not a gateway listener port.
- **Fully wired.** It flows consistently into env vars, `origin` (URL building),
  NOTES.txt output, and Tomcat server.xml configuration.
- **Documented.** It has a clear comment explaining that it does not change
  the Gateway or load balancer — it must match the actual port in use.

The `ingress.port` field is retained for backward compatibility but is not
documented in the default `values.yaml`. New users should use the `gateway`
section where `externalPort` provides a consistent, well-named alternative.

## Why "common.gateway" naming?

The helpers are namespaced under `common.gateway` even though they handle both
ingress and gateway cases. This is a forward-looking choice — the Gateway API
is the successor to the Ingress API in Kubernetes, and "gateway" as a general
concept ("the entry point for traffic") fits both use cases. Renaming to
something neutral like `common.routing` was considered but adds no clarity
for the extra churn.

## Product-specific helpers

Each product has a `<product>.path` helper that wraps `common.gateway.path`
with the product's own `contextPath` default. The pattern is:

```
include "common.gateway.path" (dict
    "useGatewayMode" (include "common.gateway.useGatewayMode" .)
    "gatewayPath"    .Values.gateway.path
    "ingressPath"    .Values.ingress.path
    "contextPath"    .Values.<product>.service.contextPath
)
```

Bamboo additionally has `bamboo.baseUrl` which combines `origin` + `path`
with a localhost fallback when no external access is configured.
