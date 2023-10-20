{{/* vim: set filetype=mustache: */}}
{{/*
Create default value for ingress port
*/}}
{{- define "crowd.ingressPort" -}}
{{ default (ternary "443" "80" .Values.ingress.https) .Values.ingress.port -}}
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
{{- include "common.names.fullname" . -}}
{{- else -}}
default
{{- end -}}
{{- end -}}
{{- end }}

{{/*
Pod labels
*/}}
{{- define "crowd.podLabels" -}}
{{ with .Values.podLabels }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{- define "crowd.sysprop.clusterNodeName" -}}
-Dcluster.node.name=${KUBE_POD_NAME}
{{- end }}

{{- define "crowd.sysprop.fluentdAppender" -}}
-Datlassian.logging.cloud.enabled={{.Values.fluentd.enabled}}
{{- end }}

{{/*
The command that should be run by the nfs-fixer init container to correct the permissions of the shared-home root directory.
*/}}
{{- define "crowd.sharedHome.permissionFix.command" -}}
{{- $securityContext := .Values.crowd.securityContext }}
{{- with .Values.volumes.sharedHome.nfsPermissionFixer }}
    {{- if .command }}
        {{ .command }}
    {{- else }}
        {{- if and $securityContext.gid $securityContext.enabled }}
            {{- printf "(chgrp %v %s; chmod g+w %s)" $securityContext.gid .mountPath .mountPath }}
        {{- else if $securityContext.fsGroup }}
            {{- printf "(chgrp %v %s; chmod g+w %s)" $securityContext.fsGroup .mountPath .mountPath }}
        {{- else }}
            {{- printf "(chgrp 2004 %s; chmod g+w %s)" .mountPath .mountPath }}
        {{- end }}
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
{{- if .Values.crowd.additionalCertificates.secretName }}
- name: keystore
  mountPath: /var/ssl
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
Define pod annotations here to allow template overrides when used as a sub chart
*/}}
{{- define "crowd.podAnnotations" -}}
{{- range $key, $value := .Values.podAnnotations }}
{{ $key }}: {{ tpl $value $ | quote }}
{{- end }}
{{- end }}

{{/*
Define additional init containers here to allow template overrides when used as a sub chart
*/}}
{{- define "crowd.additionalInitContainers" -}}
{{- with .Values.additionalInitContainers }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Define additional containers here to allow template overrides when used as a sub chart
*/}}
{{- define "crowd.additionalContainers" -}}
{{- with .Values.additionalContainers }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Define additional ports here instead of in values.yaml to allow template overrides
*/}}
{{- define "crowd.additionalPorts" -}}
{{- with .Values.crowd.additionalPorts }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Define additional volume mounts here to allow template overrides when used as a sub chart
*/}}
{{- define "crowd.additionalVolumeMounts" -}}
{{- with .Values.crowd.additionalVolumeMounts }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Define additional environment variables here to allow template overrides when used as a sub chart
*/}}
{{- define "crowd.additionalEnvironmentVariables" -}}
{{- with .Values.crowd.additionalEnvironmentVariables }}
{{- toYaml . }}
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
{{- if .Values.crowd.additionalCertificates.secretName }}
- name: keystore
  emptyDir: {}
- name: certs
  secret:
    secretName: {{ .Values.crowd.additionalCertificates.secretName }}
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
    claimName: {{ include "common.names.fullname" . }}-shared-home
{{ else }}
{{ if .Values.volumes.sharedHome.customVolume }}
{{- toYaml .Values.volumes.sharedHome.customVolume | nindent 2 }}
{{ else }}
  emptyDir: {}
{{- end }}
{{- end }}
{{- end }}

{{- define "crowd.volumeClaimTemplates" -}}
{{- if or .Values.volumes.localHome.persistentVolumeClaim.create .Values.crowd.additionalVolumeClaimTemplates }}
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
{{- range .Values.crowd.additionalVolumeClaimTemplates }}
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

{{- define "flooredCPU" -}}
    {{- if hasSuffix "m" (. | toString) }}
    {{- div (trimSuffix "m" .) 1000 | default 1 }}
    {{- else }}
    {{- . }}
    {{- end }}
{{- end}}


{{/*
Define additional hosts here to allow template overrides when used as a sub chart
*/}}
{{- define "crowd.additionalHosts" -}}
{{- with .Values.additionalHosts }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{- define "crowd.addCrtToKeystoreCmd" }}
{{- if .Values.crowd.additionalCertificates.customCmd}}
{{ .Values.crowd.additionalCertificates.customCmd}}
{{- else }}
set -e; cp $JAVA_HOME/lib/security/cacerts /var/ssl/cacerts; for crt in /tmp/crt/*.*; do echo "Adding $crt to keystore"; keytool -import -keystore /var/ssl/cacerts -storepass changeit -noprompt -alias $(echo $(basename $crt)) -file $crt; done;
{{- end }}
{{- end }}
