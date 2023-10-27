{{/*
Jmx config volume
*/}}
{{- define "common.jmx.config.volume" -}}
{{- if .Values.monitoring.exposeJmxMetrics }}
- name: jmx-config
  configMap:
    name: {{ include "common.names.fullname" . }}-jmx-config
{{- end }}
{{- end }}

{{/*
Jmx config volume mount
*/}}

{{- define "common.jmx.config.volumeMounts" -}}
{{- if .Values.monitoring.exposeJmxMetrics }}
- name: jmx-config
  mountPath: /opt/atlassian/jmx
{{- end }}
{{- end }}


{{/*
Jmx init container
*/}}
{{- define "common.jmx.initContainer" -}}
{{- if and .Values.monitoring.exposeJmxMetrics .Values.monitoring.fetchJmxExporterJar }}
- name: fetch-jmx-exporter
  image: {{ .Values.monitoring.jmxExporterImageRepo}}:{{ .Values.monitoring.jmxExporterImageTag}}
  command: ["cp"]
  args: ["/opt/bitnami/jmx-exporter/jmx_prometheus_javaagent.jar", "{{ .Values.volumes.sharedHome.mountPath }}"]
  {{- if .Values.monitoring.jmxExporterInitContainer.resources }}
  resources:
  {{- with .Values.monitoring.jmxExporterInitContainer.resources }}
  {{- toYaml . | nindent 4 }}
  {{- end }}
  {{- end }}
  {{- if .Values.monitoring.jmxExporterInitContainer.runAsRoot }}
  securityContext:
    runAsUser: 0
  {{- else if .Values.monitoring.jmxExporterInitContainer.customSecurityContext }}
  securityContext:
  {{- with .Values.monitoring.jmxExporterInitContainer.customSecurityContext }}
  {{- toYaml .  | nindent 4 }}
  {{- end }}
  {{- end }}
  volumeMounts:
    - mountPath: {{ .Values.volumes.sharedHome.mountPath | quote }}
      name: shared-home
      {{- if .Values.volumes.sharedHome.subPath }}
      subPath: {{ .Values.volumes.sharedHome.subPath | quote }}
      {{- end }}
{{- end }}
{{- end }}

{{/*
Jmx port
*/}}
{{- define "common.jmx.port" -}}
{{- if .Values.monitoring.exposeJmxMetrics }}
- name: jmx
  containerPort: {{ .Values.monitoring.jmxExporterPort }}
  protocol: TCP
{{- end }}
{{- end }}

{{/*
Jmx javaagent
*/}}
{{- define "common.jmx.javaagent" -}}
{{- if .Values.monitoring.exposeJmxMetrics }}
-javaagent:{{ .Values.monitoring.jmxExporterCustomJarLocation | default (printf "%s/jmx_prometheus_javaagent.jar"  .Values.volumes.sharedHome.mountPath) }}={{ .Values.monitoring.jmxExporterPort}}:/opt/atlassian/jmx/jmx-config.yaml
{{- end }}
{{- end }}

{{/*
Jmx configuration yaml
*/}}
{{- define "common.jmx.config" -}}
{{ if .Values.monitoring.jmxExporterCustomConfig }}
{{- range $key, $value := .Values.monitoring.jmxExporterCustomConfig }}
{{ $key }}: |
{{ $value | indent 2 }}
{{- end }}
{{ else }}
jmx-config.yaml: |
  lowercaseOutputLabelNames: true
  lowercaseOutputName: true
  rules:
    - pattern: ".*"
{{- end }}
{{- end }}
