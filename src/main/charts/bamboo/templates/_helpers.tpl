{{/* vim: set filetype=mustache: */}}
{{/*
Deduce the base URL for bamboo.
*/}}
{{- define "bamboo.baseUrl" -}}
    {{- if .Values.ingress.host -}}
        {{ ternary "https" "http" .Values.ingress.https -}}
        ://
        {{- if .Values.ingress.path -}}
            {{ .Values.ingress.host}}{{.Values.ingress.path }}
        {{- else -}}
            {{ .Values.ingress.host}}
        {{- end }}
    {{- else -}}
        {{- print  "http://localhost:8085/" }}
    {{- end }}
{{- end }}

{{/*
Create default value for ingress port
*/}}
{{- define "bamboo.ingressPort" -}}
{{ default (ternary "443" "80" .Values.ingress.https) .Values.ingress.port -}}
{{- end }}

{{/*
Create default value for ingress path
*/}}
{{- define "bamboo.ingressPath" -}}
{{- if .Values.ingress.path -}}
{{- .Values.ingress.path -}}
{{- else -}}
{{ default ( "/" ) .Values.bamboo.service.contextPath -}}
{{- end }}
{{- end }}

{{/*
The name of the service account to be used.
If the name is defined in the chart values, then use that,
else if we're creating a new service account then use the name of the Helm release,
else just use the "default" service account.
*/}}
{{- define "bamboo.serviceAccountName" -}}
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
{{- define "bamboo.podLabels" -}}
{{ with .Values.podLabels }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
The command that should be run by the nfs-fixer init container to correct the permissions of the shared-home root directory.
*/}}
{{- define "sharedHome.permissionFix.command" -}}
{{- $securityContext := .Values.bamboo.securityContext }}
{{- with .Values.volumes.sharedHome.nfsPermissionFixer }}
    {{- if .command }}
        {{ .command }}
    {{- else }}
        {{- $gid := default $securityContext.fsGroup 2005 }}
        {{- printf "(chgrp %v %s; chmod g+w %s)" $gid .mountPath .mountPath }}
    {{- end }}
{{- end }}
{{- end }}

{{/*
The command that should be run to start the fluentd service
*/}}
{{- define "fluentd.start.command" -}}
{{- if .Values.fluentd.command }}
{{ .Values.fluentd.command }}
{{- else }}
{{- print "exec fluentd -c /fluentd/etc/fluent.conf -v" }}
{{- end }}
{{- end }}

{{- define "bamboo.image" -}}
{{- if .Values.image.registry -}}
{{ .Values.image.registry}}/{{ .Values.image.repository }}:{{ .Values.image.tag | default .Chart.AppVersion }}
{{- else -}}
{{ .Values.image.repository }}:{{ .Values.image.tag | default .Chart.AppVersion }}
{{- end }}
{{- end }}

{{/*
Defines the volume mounts used by the Bamboo container.
Note that the local-home volume is mounted twice, once for the local-home directory itself, and again
on Tomcat's logs directory. THis ensures that Tomcat+Bamboo logs get captured in the same volume.
*/}}
{{ define "bamboo.volumeMounts" }}
- name: local-home
  mountPath: {{ .Values.volumes.localHome.mountPath | quote }}
- name: local-home
  mountPath: {{ .Values.bamboo.accessLog.mountPath | quote }}
  subPath: {{ .Values.bamboo.accessLog.localHomeSubPath | quote }}
- name: shared-home
  mountPath: {{ .Values.volumes.sharedHome.mountPath | quote }}
  {{- if .Values.volumes.sharedHome.subPath }}
  subPath: {{ .Values.volumes.sharedHome.subPath | quote }}
  {{- end }}
{{- end }}

{{/*
Define pod annotations here to allow template overrides when used as a sub chart
*/}}
{{- define "bamboo.podAnnotations" -}}
{{- with .Values.podAnnotations }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Define additional init containers here to allow template overrides when used as a sub chart
*/}}
{{- define "bamboo.additionalInitContainers" -}}
{{- with .Values.additionalInitContainers }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Define additional containers here to allow template overrides when used as a sub chart
*/}}
{{- define "bamboo.additionalContainers" -}}
{{- with .Values.additionalContainers }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Define additional ports here instead of in values.yaml to allow template overrides
*/}}
{{- define "bamboo.additionalPorts" -}}
{{- with .Values.bamboo.additionalPorts }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Define additional volume mounts here to allow template overrides when used as a sub chart
*/}}
{{- define "bamboo.additionalVolumeMounts" -}}
{{- with .Values.bamboo.additionalVolumeMounts }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Define additional environment variables here to allow template overrides when used as a sub chart
*/}}
{{- define "bamboo.additionalEnvironmentVariables" -}}
{{- with .Values.bamboo.additionalEnvironmentVariables }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
For each additional library declared, generate a volume mount that injects that library into the Bamboo lib directory
*/}}
{{- define "bamboo.additionalLibraries" -}}
{{- range .Values.bamboo.additionalLibraries }}
- name: {{ .volumeName }}
  mountPath: "/opt/atlassian/bamboo/lib/{{ .fileName }}"
  {{- if .subDirectory }}
  subPath: {{ printf "%s/%s" .subDirectory .fileName | quote }}
  {{- else }}
  subPath: {{ .fileName | quote }}
  {{- end }}
{{- end }}
{{- end }}

{{/*
For each additional plugin declared, generate a volume mount that injects that library into the Bamboo plugins directory
*/}}
{{- define "bamboo.additionalBundledPlugins" -}}
{{- range .Values.bamboo.additionalBundledPlugins }}
- name: {{ .volumeName }}
  mountPath: "/opt/atlassian/bamboo/atlassian-bamboo/WEB-INF/atlassian-bundled-plugins/{{ .fileName }}"
  {{- if .subDirectory }}
  subPath: {{ printf "%s/%s" .subDirectory .fileName | quote }}
  {{- else }}
  subPath: {{ .fileName | quote }}
  {{- end }}
{{- end }}
{{- end }}

{{- define "bamboo.volumes" -}}
{{ if not .Values.volumes.localHome.persistentVolumeClaim.create }}
{{ include "bamboo.volumes.localHome" . }}
{{- end }}
{{ include "bamboo.volumes.sharedHome" . }}
{{- with .Values.volumes.additional }}
{{- toYaml . | nindent 0 }}
{{- end }}
{{- end }}

{{- define "bamboo.volumes.localHome" -}}
{{- if not .Values.volumes.localHome.persistentVolumeClaim.create }}
- name: local-home
{{ if .Values.volumes.localHome.customVolume }}
{{- toYaml .Values.volumes.localHome.customVolume | nindent 2 }}
{{ else }}
  emptyDir: {}
{{- end }}
{{- end }}
{{- end }}

{{- define "bamboo.volumes.sharedHome" -}}
- name: shared-home
{{- if .Values.volumes.sharedHome.persistentVolumeClaim.create }}
  persistentVolumeClaim:
    claimName: {{ include "common.names.fullname" . }}-shared-home
{{ else }}
{{ if .Values.volumes.sharedHome.customVolume }}
{{- toYaml .Values.volumes.sharedHome.customVolume | nindent 2 }}
{{ else }}
  emptyDir: {}
{{- end }}
{{- end }}
{{- end }}

{{- define "bamboo.volumeClaimTemplates" -}}
{{- if or .Values.volumes.localHome.persistentVolumeClaim.create .Values.bamboo.additionalVolumeClaimTemplates }}
volumeClaimTemplates:
{{- if .Values.volumes.localHome.persistentVolumeClaim.create }}
- metadata:
    name: local-home
  spec:
    accessModes: [ "ReadWriteOnce" ]
    {{- if .Values.volumes.localHome.persistentVolumeClaim.storageClassName }}
    storageClassName: {{ .Values.volumes.localHome.persistentVolumeClaim.storageClassName | quote }}
    {{- end }}
    {{- with .Values.volumes.localHome.persistentVolumeClaim.resources }}
    resources:
      {{- toYaml . | nindent 6 }}
    {{- end }}
{{- end }}
{{- range .Values.bamboo.additionalVolumeClaimTemplates }}
- metadata:
    name: {{ .name }}
  spec:
    accessModes: [ "ReadWriteOnce" ]
    {{- if .storageClassName }}
    storageClassName: {{ .storageClassName | quote }}
    {{- end }}
    {{- with .resources }}
    resources:
      {{- toYaml . | nindent 6 }}
    {{- end }}
{{- end }}
{{- end }}
{{- end }}

{{- define "bamboo.databaseEnvVars" -}}
{{ with .Values.database.type }}
- name: ATL_DB_TYPE
  value: {{ . | quote }}
{{ end }}
{{ with .Values.database.url }}
- name: ATL_JDBC_URL
  value: {{ . | quote }}
{{ end }}
{{ with .Values.database.credentials.secretName }}
- name: ATL_JDBC_USER
  valueFrom:
    secretKeyRef:
      name: {{ . }}
      key: {{ $.Values.database.credentials.usernameSecretKey }}
- name: ATL_JDBC_PASSWORD
  valueFrom:
    secretKeyRef:
      name: {{ . }}
      key: {{ $.Values.database.credentials.passwordSecretKey }}
{{ end }}
{{ end }}

{{- define "flooredCPU" -}}
    {{- if hasSuffix "m" (. | toString) }}
    {{- div (trimSuffix "m" .) 1000 | default 1 }}
    {{- else }}
    {{- . }}
    {{- end }}
{{- end}}
