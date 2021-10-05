{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "jira.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "jira.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "jira.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create default value for ingress port
*/}}
{{- define "jira.ingressPort" -}}
{{ default (ternary "443" "80" .Values.ingress.https) .Values.ingress.port -}}
{{- end }}

{{/*
Create default value for ingress path
*/}}
{{- define "jira.ingressPath" -}}
{{- if .Values.ingress.path -}}
{{- .Values.ingress.path -}}
{{- else -}}
{{ default ( "/" ) .Values.jira.service.contextPath -}}
{{- end }}
{{- end }}

{{/*
The name of the service account to be used.
If the name is defined in the chart values, then use that,
else if we're creating a new service account then use the name of the Helm release,
else just use the "default" service account.
*/}}
{{- define "jira.serviceAccountName" -}}
{{- if .Values.serviceAccount.name -}}
{{- .Values.serviceAccount.name -}}
{{- else -}}
{{- if .Values.serviceAccount.create -}}
{{- include "jira.fullname" . -}}
{{- else -}}
default
{{- end -}}
{{- end -}}
{{- end }}

{{/*
Common labels
*/}}
{{- define "jira.labels" -}}
helm.sh/chart: {{ include "jira.chart" . }}
{{ include "jira.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{ with .Values.additionalLabels }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "jira.selectorLabels" -}}
app.kubernetes.io/name: {{ include "jira.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
The command that should be run by the nfs-fixer init container to correct the permissions of the shared-home root directory.
*/}}
{{- define "sharedHome.permissionFix.command" -}}
{{- if .Values.volumes.sharedHome.nfsPermissionFixer.command }}
{{ .Values.volumes.sharedHome.nfsPermissionFixer.command }}
{{- else }}
{{- $securityContext := .Values.jira.securityContext | default dict}}
{{- if $securityContext.fsGroup }}
{{- printf "(chgrp %v %s; chmod g+w %s)" .Values.jira.securityContext.fsGroup .Values.volumes.sharedHome.nfsPermissionFixer.mountPath .Values.volumes.sharedHome.nfsPermissionFixer.mountPath }}
{{- else }}
{{- printf "(chgrp 2001 %s; chmod g+w %s)" .Values.volumes.sharedHome.nfsPermissionFixer.mountPath .Values.volumes.sharedHome.nfsPermissionFixer.mountPath }}
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

{{- define "jira.image" -}}
{{- if .Values.image.registry -}}
{{ .Values.image.registry}}/{{ .Values.image.repository }}:{{ .Values.image.tag | default .Chart.AppVersion }}
{{- else -}}
{{ .Values.image.repository }}:{{ .Values.image.tag | default .Chart.AppVersion }}
{{- end }}
{{- end }}

{{/*
Defines the volume mounts used by the Jira container.
Note that the local-home volume is mounted twice, once for the local-home directory itself, and again
on Tomcat's logs directory. THis ensures that Tomcat+Jira logs get captured in the same volume.
*/}}
{{ define "jira.volumeMounts" }}
- name: local-home
  mountPath: {{ .Values.volumes.localHome.mountPath | quote }}
- name: local-home
  mountPath: {{ .Values.jira.accessLog.mountPath | quote }}
  subPath: {{ .Values.jira.accessLog.localHomeSubPath | quote }}
- name: shared-home
  mountPath: {{ .Values.volumes.sharedHome.mountPath | quote }}
  {{- if .Values.volumes.sharedHome.subPath }}
  subPath: {{ .Values.volumes.sharedHome.subPath | quote }}
  {{- end }}
{{- end }}

{{/*
Defining additional init containers here instead of in values.yaml to allow template overrides
*/}}
{{- define "jira.additionalInitContainers" -}}
{{- with .Values.additionalInitContainers }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Defining additional containers here instead of in values.yaml to allow template overrides
*/}}
{{- define "jira.additionalContainers" -}}
{{- with .Values.additionalContainers }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Defining additional volume mounts here instead of in values.yaml to allow template overrides
*/}}
{{- define "jira.additionalVolumeMounts" -}}
{{- with .Values.jira.additionalVolumeMounts }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Defining additional environment variables here instead of in values.yaml to allow template overrides
*/}}
{{- define "jira.additionalEnvironmentVariables" -}}
{{- with .Values.jira.additionalEnvironmentVariables }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
For each additional library declared, generate a volume mount that injects that library into the Jira lib directory
*/}}
{{- define "jira.additionalLibraries" -}}
{{- range .Values.jira.additionalLibraries }}
- name: {{ .volumeName }}
  mountPath: "/opt/atlassian/jira/lib/{{ .fileName }}"
  {{- if .subDirectory }}
  subPath: {{ printf "%s/%s" .subDirectory .fileName | quote }}
  {{- else }}
  subPath: {{ .fileName | quote }}
  {{- end }}
{{- end }}
{{- end }}

{{/*
For each additional plugin declared, generate a volume mount that injects that library into the Jira plugins directory
*/}}
{{- define "jira.additionalBundledPlugins" -}}
{{- range .Values.jira.additionalBundledPlugins }}
- name: {{ .volumeName }}
  mountPath: "/opt/atlassian/jira/atlassian-jira/WEB-INF/atlassian-bundled-plugins/{{ .fileName }}"
  {{- if .subDirectory }}
  subPath: {{ printf "%s/%s" .subDirectory .fileName | quote }}
  {{- else }}
  subPath: {{ .fileName | quote }}
  {{- end }}
{{- end }}
{{- end }}

{{- define "jira.volumes" -}}
{{ if not .Values.volumes.localHome.persistentVolumeClaim.create }}
{{ include "jira.volumes.localHome" . }}
{{- end }}
{{ include "jira.volumes.sharedHome" . }}
{{- with .Values.volumes.additional }}
{{- toYaml . | nindent 0 }}
{{- end }}
{{- end }}

{{- define "jira.volumes.localHome" -}}
{{- if not .Values.volumes.localHome.persistentVolumeClaim.create }}
- name: local-home
{{ if .Values.volumes.localHome.customVolume }}
{{- toYaml .Values.volumes.localHome.customVolume | nindent 2 }}
{{ else }}
  emptyDir: {}
{{- end }}
{{- end }}
{{- end }}

{{- define "jira.volumes.sharedHome" -}}
- name: shared-home
{{- if .Values.volumes.sharedHome.persistentVolumeClaim.create }}
  persistentVolumeClaim:
    claimName: {{ include "jira.fullname" . }}-shared-home
{{ else }}
{{ if .Values.volumes.sharedHome.customVolume }}
{{- toYaml .Values.volumes.sharedHome.customVolume | nindent 2 }}
{{ else }}
  emptyDir: {}
{{- end }}
{{- end }}
{{- end }}

{{- define "jira.volumeClaimTemplates" -}}
{{ if .Values.volumes.localHome.persistentVolumeClaim.create }}
volumeClaimTemplates:
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
{{- end }}

{{- define "jira.databaseEnvVars" -}}
{{ with .Values.database.type }}
- name: ATL_DB_TYPE
  value: {{ . | quote }}
{{ end }}
{{ with .Values.database.driver }}
- name: ATL_DB_DRIVER
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

{{- define "jira.clusteringEnvVars" -}}
{{ if .Values.jira.clustering.enabled }}
- name: CLUSTERED
  value: "true"
- name: JIRA_NODE_ID
  valueFrom:
    fieldRef:
      fieldPath: metadata.name
- name: EHCACHE_LISTENER_HOSTNAME
  valueFrom:
    fieldRef:
      fieldPath: status.podIP
- name: EHCACHE_LISTENER_PORT
  value: {{ .Values.jira.ports.ehcache | quote }}
- name: EHCACHE_OBJECT_PORT
  value: {{ .Values.jira.ports.ehcacheobject | quote }}
{{ end }}
{{ end }}

{{- define "jira.sysprop.fluentdAppender" -}}
-Datlassian.logging.cloud.enabled={{.Values.fluentd.enabled}}
{{- end }}
