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
{{- if and .Values.monitoring.exposeJmxMetrics (not .Values.monitoring.jmxExporterCustomJarLocation) }}
- name: fetch-jmx-exporter
  image: curlimages/curl:latest
  command: ["/bin/sh"]
  args:
    - -ec
    - |
      AGENT_PATH="{{ .Values.volumes.sharedHome.mountPath }}/jmx_prometheus_javaagent.jar"
      VERSION="{{ .Values.monitoring.jmxExporter.version }}"
      BASE_URL="{{ .Values.monitoring.jmxExporter.mavenBaseUrl }}"
      EXPECTED_SHA256="{{ .Values.monitoring.jmxExporter.sha256 }}"

      # Download the agent
      curl -L "${BASE_URL}/${VERSION}/jmx_prometheus_javaagent-${VERSION}.jar" -o $AGENT_PATH

      # Verify SHA256
      echo "${EXPECTED_SHA256} ${AGENT_PATH}" | sha256sum -c -

      # Set permissions
      chmod 644 $AGENT_PATH

      # Verify JAR structure
      if ! unzip -l $AGENT_PATH | grep -q "META-INF/MANIFEST.MF"; then
        echo "Invalid JAR file"
        exit 1
      fi
      if ! unzip -l $AGENT_PATH | grep -q "JavaAgent.class"; then
        echo "Not a valid Java agent JAR"
        exit 1
      fi
  {{- if .Values.monitoring.jmxExporterInitContainer.resources }}
  resources:
  {{- with .Values.monitoring.jmxExporterInitContainer.resources }}
  {{- toYaml . | nindent 4 }}
  {{- end }}
  {{- end }}
  {{- if .Values.openshift.runWithRestrictedSCC }}
  {{- else}}
  {{- if .Values.monitoring.jmxExporterInitContainer.runAsRoot }}
  securityContext:
    runAsUser: 0
  {{- else if .Values.monitoring.jmxExporterInitContainer.customSecurityContext }}
  securityContext:
  {{- with .Values.monitoring.jmxExporterInitContainer.customSecurityContext }}
  {{- toYaml .  | nindent 4 }}
  {{- end }}
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
