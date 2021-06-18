{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "crowd.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "crowd.fullname" -}}
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
{{- define "crowd.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
The name of the service account to be used.
If the name is defined in the chart values, then use that,
else if we're creating a new service account then use the name of the Helm release,
else just use the "default" service account.
*/}}
{{- define "crowd.serviceAccountName" -}}
{{- if .Values.serviceAccount.name -}}
{{- .Values.serviceAccount.name -}}
{{- else -}}
{{- if .Values.serviceAccount.create -}}
{{- include "crowd.fullname" . -}}
{{- else -}}
default
{{- end -}}
{{- end -}}
{{- end }}

{{/*
The name of the ClusterRole that will be created.
If the name is defined in the chart values, then use that,
else use the name of the Helm release.
*/}}
{{- define "crowd.clusterRoleName" -}}
{{- if .Values.serviceAccount.clusterRole.name }}
{{- .Values.serviceAccount.clusterRole.name }}
{{- else }}
{{- include "crowd.fullname" . -}}
{{- end }}
{{- end }}

{{/*
The name of the ClusterRoleBinding that will be created.
If the name is defined in the chart values, then use that,
else use the name of the ClusterRole.
*/}}
{{- define "crowd.clusterRoleBindingName" -}}
{{- if .Values.serviceAccount.clusterRoleBinding.name }}
{{- .Values.serviceAccount.clusterRoleBinding.name }}
{{- else }}
{{- include "crowd.clusterRoleName" . -}}
{{- end }}
{{- end }}

{{/*
These labels will be applied to all Crowd resources in the chart
*/}}
{{- define "crowd.labels" -}}
helm.sh/chart: {{ include "crowd.chart" . }}
{{ include "crowd.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{ with .Values.additionalLabels }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Selector labels for finding Crowd resources
*/}}
{{- define "crowd.selectorLabels" -}}
app.kubernetes.io/name: {{ include "crowd.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{- define "crowd.sysprop.hazelcastListenPort" -}}
-Dcrowd.cluster.hazelcast.listenPort={{ .Values.crowd.ports.hazelcast }}
{{- end }}

{{- define "crowd.sysprop.clusterNodeName" -}}
-Dcrowd.clusterNodeName.useHostname={{ .Values.crowd.clustering.usePodNameAsClusterNodeName }}
{{- end }}

{{- define "crowd.sysprop.fluentdAppender" -}}
-Datlassian.logging.cloud.enabled={{.Values.fluentd.enabled}}
{{- end }}

{{/*
The command that should be run by the nfs-fixer init container to correct the permissions of the shared-home root directory.
*/}}
{{- define "sharedHome.permissionFix.command" -}}
{{- if .Values.volumes.sharedHome.nfsPermissionFixer.command }}
{{ .Values.volumes.sharedHome.nfsPermissionFixer.command }}
{{- else }}
{{- printf "(chgrp %s %s; chmod g+w %s)" .Values.crowd.securityContext.gid .Values.volumes.sharedHome.nfsPermissionFixer.mountPath .Values.volumes.sharedHome.nfsPermissionFixer.mountPath }}
{{- end }}
{{- end }}

{{- define "crowd.image" -}}
{{- if .Values.image.registry -}}
{{ .Values.image.registry}}/{{ .Values.image.repository }}:{{ .Values.image.tag | default .Chart.AppVersion }}
{{- else -}}
{{ .Values.image.repository }}:{{ .Values.image.tag | default .Chart.AppVersion }}
{{- end }}
{{- end }}

{{/*
Defines the volume mounts used by the Crowd container.
Note that the local-home volume is mounted twice, once for the local-home directory itself, and again
on Tomcat's logs directory. THis ensures that Tomcat+Crowd logs get captured in the same volume.
*/}}
{{ define "crowd.volumeMounts" }}
- name: local-home
  mountPath: {{ .Values.volumes.localHome.mountPath | quote }}
- name: local-home
  mountPath: {{ .Values.crowd.accessLog.mountPath | quote }}
  subPath: {{ .Values.crowd.accessLog.localHomeSubPath | quote }}
- name: shared-home
  mountPath: {{ .Values.volumes.sharedHome.mountPath | quote }}
  {{- if .Values.volumes.sharedHome.subPath }}
  subPath: {{ .Values.volumes.sharedHome.subPath | quote }}
  {{- end }}
{{ end }}

{{/*
For each additional library declared, generate a volume mount that injects that library into the Crowd lib directory
*/}}
{{- define "crowd.additionalLibraries" -}}
{{- range .Values.crowd.additionalLibraries }}
- name: {{ .volumeName }}
  mountPath: "/opt/atlassian/crowd/crowd-webapp/WEB-INF/lib/{{ .fileName }}"
  {{- if .subDirectory }}
  subPath: {{ printf "%s/%s" .subDirectory .fileName | quote }}
  {{- else }}
  subPath: {{ .fileName | quote }}
  {{- end }}
{{- end }}
{{- end }}

{{/*
Defining additional init containers here instead of in values.yaml to allow template overrides
*/}}
{{- define "crowd.additionalInitContainers" -}}
{{- range .Values.additionalInitContainers }}
{{- end }}
{{- end }}

{{/*
Defining additional containers here instead of in values.yaml to allow template overrides
*/}}
{{- define "crowd.additionalContainers" -}}
{{- range .Values.additionalContainers }}
{{- end }}
{{- end }}

{{/*
Defining additional volume mounts here instead of in values.yaml to allow template overrides
*/}}
{{- define "crowd.additionalVolumeMounts" -}}
{{- range .Values.crowd.additionalVolumeMounts }}
{{- end }}
{{- end }}

{{/*
Defining additional environment variables here instead of in values.yaml to allow template overrides
*/}}
{{- define "crowd.additionalEnvironmentVariables" -}}
{{- range .Values.crowd.additionalEnvironmentVariables }}
{{- end }}
{{- end }}

{{/*
For each additional plugin declared, generate a volume mount that injects that library into the Crowd plugins directory
*/}}
{{- define "crowd.additionalBundledPlugins" -}}
{{- range .Values.crowd.additionalBundledPlugins }}
- name: {{ .volumeName }}
  mountPath: "/var/atlassian/application-data/crowd/shared/plugins/{{ .fileName }}"
  {{- if .subDirectory }}
  subPath: {{ printf "%s/%s" .subDirectory .fileName | quote }}
  {{- else }}
  subPath: {{ .fileName | quote }}
  {{- end }}
{{- end }}
{{- end }}

{{- define "crowd.volumes" -}}
{{ if not .Values.volumes.localHome.persistentVolumeClaim.create }}
{{ include "crowd.volumes.localHome" . }}
{{- end }}
{{ include "crowd.volumes.sharedHome" . }}
{{- with .Values.volumes.additional }}
{{- toYaml . | nindent 0 }}
{{- end }}
{{- end }}

{{- define "crowd.volumes.localHome" -}}
{{- if not .Values.volumes.localHome.persistentVolumeClaim.create }}
- name: local-home
{{ if .Values.volumes.localHome.customVolume }}
{{- toYaml .Values.volumes.localHome.customVolume | nindent 2 }}
{{ else }}
  emptyDir: {}
{{- end }}
{{- end }}
{{- end }}

{{- define "crowd.volumes.sharedHome" -}}
- name: shared-home
{{- if .Values.volumes.sharedHome.persistentVolumeClaim.create }}
  persistentVolumeClaim:
    claimName: {{ include "crowd.fullname" . }}-shared-home
{{ else }}
{{ if .Values.volumes.sharedHome.customVolume }}
{{- toYaml .Values.volumes.sharedHome.customVolume | nindent 2 }}
{{ else }}
  emptyDir: {}
{{- end }}
{{- end }}
{{- end }}

{{- define "crowd.volumeClaimTemplates" -}}
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

{{- define "crowd.clusteringEnvVars" -}}
{{ if .Values.crowd.clustering.enabled }}
- name: KUBERNETES_NAMESPACE
  valueFrom:
    fieldRef:
      fieldPath: metadata.namespace
- name: HAZELCAST_KUBERNETES_SERVICE_NAME
  value: {{ include "crowd.fullname" . | quote }}
- name: ATL_CLUSTER_TYPE
  value: "kubernetes"
- name: ATL_CLUSTER_NAME
  value: {{ include "crowd.fullname" . | quote }}
{{ end }}
{{ end }}
