{{/* vim: set filetype=mustache: */}}

{{/*
Returns "true" if external access is configured via either ingress or gateway.
True when ingress.host is set or gateway.hostnames is non-empty.
*/}}
{{- define "common.gateway.isConfigured" -}}
    {{- ternary "true" "false" (or (not (empty .Values.ingress.host)) (not (empty .Values.gateway.hostnames))) -}}
{{- end -}}

{{/*
Returns "true" if gateway mode should be used (vs ingress mode) for product setup.
True when gateway.hostnames is non-empty, regardless of whether an HTTPRoute is created.
This allows using gateway config with a pre-existing Gateway/proxy without gateway.create.
*/}}
{{- define "common.gateway.useGatewayMode" -}}
    {{- ternary "true" "false" (not (empty .Values.gateway.hostnames)) -}}
{{- end -}}

{{/*
Validates gateway/ingress configuration.
Ensures mutual exclusion and required fields.
*/}}
{{- define "common.gateway.validateConfig" -}}
    {{- if and .Values.gateway.create .Values.ingress.create -}}
        {{- fail "ERROR: Cannot enable both gateway.create and ingress.create" -}}
    {{- end -}}
    {{- if and (not (empty .Values.gateway.hostnames)) (not (empty .Values.ingress.host)) -}}
        {{- fail "ERROR: Cannot set both gateway.hostnames and ingress.host — use one or the other" -}}
    {{- end -}}
    {{- if and .Values.gateway.create (not .Values.gateway.parentRef.name) -}}
        {{- fail "ERROR: gateway.parentRef.name is required when gateway.create is true" -}}
    {{- end -}}
    {{- if and .Values.gateway.create (not .Values.gateway.hostnames) -}}
        {{- fail "ERROR: gateway.hostnames must contain at least one hostname when gateway.create is true" -}}
    {{- end -}}
{{- end -}}

{{/*
Returns "true" or "false" string for whether HTTPS is enabled.
Uses gateway.https if gateway config is active, otherwise ingress.https.
*/}}
{{- define "common.gateway.https" -}}
    {{- $useGateway := eq (include "common.gateway.useGatewayMode" .) "true" -}}
    {{- ternary .Values.gateway.https .Values.ingress.https $useGateway -}}
{{- end -}}

{{/*
Returns "https" or "http" based on the current ingress/gateway HTTPS setting.
*/}}
{{- define "common.gateway.scheme" -}}
    {{- ternary "https" "http" (eq (include "common.gateway.https" .) "true") -}}
{{- end -}}

{{/*
Returns the canonical hostname for the service.
Uses first gateway hostname if gateway config is active, otherwise ingress.host.
*/}}
{{- define "common.gateway.hostname" -}}
    {{- if eq (include "common.gateway.useGatewayMode" .) "true" -}}
        {{- index .Values.gateway.hostnames 0 -}}
    {{- else -}}
        {{- .Values.ingress.host -}}
    {{- end -}}
{{- end -}}

{{/*
Returns the external port the application is accessed on.
Defaults to "443" (https) or "80" (http).
*/}}
{{- define "common.gateway.externalPort" -}}
    {{- if eq (include "common.gateway.useGatewayMode" .) "true" -}}
        {{- default (ternary "443" "80" .Values.gateway.https) .Values.gateway.externalPort -}}
    {{- else -}}
        {{- default (ternary "443" "80" .Values.ingress.https) .Values.ingress.port -}}
    {{- end -}}
{{- end -}}

{{/*
Returns the service path. Handles gateway vs ingress mode with a contextPath fallback.
Usage:
include "common.gateway.path" (dict
    "useGatewayMode" (include "common.gateway.useGatewayMode" .)
    "gatewayPath" .Values.gateway.path
    "ingressPath" .Values.ingress.path
    "contextPath" .Values.<product>.service.contextPath
)
*/}}
{{- define "common.gateway.path" -}}
{{- $explicitPath := ternary .gatewayPath .ingressPath (eq .useGatewayMode "true") -}}
{{- if $explicitPath -}}
    {{- $explicitPath -}}
{{- else -}}
    {{- .contextPath | default "/" -}}
{{- end -}}
{{- end -}}

{{/*
Returns the origin (scheme + host + port) for the service.
Default ports (443/https, 80/http) are omitted from the URL.
Usage: include "common.gateway.origin" .
*/}}
{{- define "common.gateway.origin" -}}
{{- $scheme := include "common.gateway.scheme" . -}}
{{- $host := include "common.gateway.hostname" . -}}
{{- $port := include "common.gateway.externalPort" . -}}
{{- printf "%s://%s" $scheme $host -}}
{{- if ne $port (ternary "443" "80" (eq $scheme "https")) -}}:{{ $port }}{{- end -}}
{{- end -}}
