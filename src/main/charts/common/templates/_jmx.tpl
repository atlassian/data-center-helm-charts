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
  mountPath: /opt/atlassian/confluence/jmx
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
  volumeMounts:
    - mountPath: {{ .Values.volumes.sharedHome.mountPath | quote }}
      name: shared-home
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
-javaagent:{{ .Values.monitoring.jmxExporterCustomJarLocation | default (printf "%s/jmx_prometheus_javaagent.jar" ( .Values.volumes.sharedHome.mountPath)) }}={{ .Values.monitoring.jmxExporterPort}}:/opt/atlassian/confluence/jmx/jmx-config.yaml
{{- end }}
{{- end }}