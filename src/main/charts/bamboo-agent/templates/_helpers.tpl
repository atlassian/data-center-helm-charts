{{/* vim: set filetype=mustache: */}}
{{/*
The K8s DNS record for the Bamboo server service
*/}}
{{- define "agent.bambooServerServiceDns" -}}
{{- if .Values.agent.server }}
{{- printf "http://%s" .Values.agent.server }}
{{- end }}
{{- end }}

{{/*
The secret token with which to authenticate to the Bamboo server
*/}}
{{- define "agent.securityToken" -}}
{{- if .Values.agent.securityToken }}
{{- printf .Values.agent.securityToken }}
{{- end }}
{{- end }}

{{/*
The name of the service account to be used.
If the name is defined in the chart values, then use that,
else if we're creating a new service account then use the name of the Helm release,
else just use the "default" service account.
*/}}
{{- define "agent.serviceAccountName" -}}
{{- if .Values.serviceAccount.name -}}
{{- .Values.serviceAccount.name -}}
{{- else -}}
{{- if .Values.serviceAccount.create -}}
{{- include "common.names.fullname" . -}}
{{- else -}}
default
{{- end -}}
{{- end -}}
{{- end }}

{{/*
Pod labels
*/}}
{{- define "agent.podLabels" -}}
{{ with .Values.podLabels }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{- define "agent.image" -}}
{{- if .Values.image.registry -}}
{{ .Values.image.registry}}/{{ .Values.image.repository }}:{{ .Values.image.tag | default .Chart.AppVersion }}
{{- else -}}
{{ .Values.image.repository }}:{{ .Values.image.tag | default .Chart.AppVersion }}
{{- end }}
{{- end }}

{{/*
Define pod annotations here to allow template overrides when used as a sub chart
*/}}
{{- define "agent.podAnnotations" -}}
{{- range $key, $value := .Values.podAnnotations }}
{{ $key }}: {{ tpl $value $ | quote }}
{{- end }}
{{- end }}

{{/*
Define additional init containers here to allow template overrides when used as a sub chart
*/}}
{{- define "agent.additionalInitContainers" -}}
{{- with .Values.additionalInitContainers }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Define additional containers here to allow template overrides when used as a sub chart
*/}}
{{- define "agent.additionalContainers" -}}
{{- with .Values.additionalContainers }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Define additional ports here instead of in values.yaml to allow template overrides
*/}}
{{- define "agent.additionalPorts" -}}
{{- with .Values.agent.additionalPorts }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Define additional environment variables here to allow template overrides when used as a sub chart
*/}}
{{- define "agent.additionalEnvironmentVariables" -}}
{{- with .Values.agent.additionalEnvironmentVariables }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Define additional hosts here to allow template overrides when used as a sub chart
*/}}
{{- define "agent.additionalHosts" -}}
{{- with .Values.additionalHosts }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Define additional volumes here to allow template overrides when used as a sub chart
*/}}
{{- define "agent.additionalVolumes" -}}
{{- with .Values.volumes.additional }}
{{- toYaml . | nindent 0 }}
{{- end }}
{{- end }}

{{/*
Define additional volume mounts here to allow template overrides when used as a sub chart
*/}}
{{- define "agent.additionalVolumeMounts" -}}
{{- with .Values.agent.additionalVolumeMounts }}
{{- toYaml . }}
{{- end }}
{{- end }}
