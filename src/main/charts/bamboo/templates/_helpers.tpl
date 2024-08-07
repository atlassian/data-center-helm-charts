{{/* vim: set filetype=mustache: */}}

{{/* Define a sanitized list of additionalEnvironmentVariables */}}
{{- define "bamboo.sanitizedAdditionalEnvVars" -}}
{{- range .Values.bamboo.additionalEnvironmentVariables }}
- name: {{ .name }}
  value: {{ if regexMatch "(?i)(secret|token|password)" .name }}"Sanitized by Support Utility"{{ else}}{{ .value }}{{ end }}
{{- end }}
{{- end }}

{{/* Define a sanitized list of additionalJvmArgs */}}
{{- define "bamboo.sanitizedAdditionalJvmArgs" -}}
{{- range .Values.bamboo.additionalJvmArgs }}
 {{- $jvmArgs := regexSplit "=" . -1 }}
   {{- if regexMatch "(?i)(secret|token|password).*$" (first $jvmArgs) }}
-  {{ first $jvmArgs }}=Sanitized by Support Utility{{ else}}
-  {{ . }}
{{ end }}
{{- end }}
{{- end }}

{{/* Define sanitized Helm values */}}
{{- define "bamboo.sanitizedValues" -}}
{{- $sanitizedAdditionalEnvs := dict .Chart.Name (dict "additionalEnvironmentVariables" (include "bamboo.sanitizedAdditionalEnvVars" .)) }}
{{- $sanitizedAdditionalJvmArgs := dict .Chart.Name (dict "additionalJvmArgs" (include "bamboo.sanitizedAdditionalJvmArgs" .)) }}
{{- $mergedValues := merge $sanitizedAdditionalEnvs $sanitizedAdditionalJvmArgs .Values }}
{{- toYaml $mergedValues | replace " |2-" "" | replace " |-" "" |  replace "|2" "" | nindent 4 }}
{{- end }}

{{- define "bamboo.analyticsJson" }}
{
  "imageTag": {{ if or (eq .Values.image.tag "") (eq .Values.image.tag nil) }}{{ .Chart.AppVersion | quote }}{{ else }}{{ regexSplit "-" .Values.image.tag -1 | first |  quote }}{{ end }},
  "replicas": {{ .Values.replicaCount }},
  "isJmxEnabled": {{ .Values.monitoring.exposeJmxMetrics }},
  "ingressType": {{ if not .Values.ingress.create }}"NONE"{{ else }}{{ if .Values.ingress.nginx }}"NGINX"{{ else }}"OTHER"{{ end }}{{ end }},
{{- $sanitizedMinorVersion := regexReplaceAll "[^0-9]" .Capabilities.KubeVersion.Minor "" }}
  "k8sVersion": "{{ .Capabilities.KubeVersion.Major }}.{{ $sanitizedMinorVersion }}",
  "serviceType": {{ if regexMatch "^(ClusterIP|NodePort|LoadBalancer|ExternalName)$" .Values.bamboo.service.type }}{{ snakecase .Values.bamboo.service.type | upper | quote }}{{ else }}"UNKNOWN"{{ end }},
{{- if eq .Values.database.type nil }}
  "dbType": "UNKNOWN",
{{- else }}
{{- $databaseTypeMap := dict "postgres" "POSTGRES" "mssql" "MSSQL" "sqlserver" "SQLSERVER" "oracle" "ORACLE" "mysql" "MYSQL" }}
{{- $dbTypeInValues := .Values.database.type }}
{{- $dbType := "UNKNOWN" | quote }}
{{- range $key, $value := $databaseTypeMap }}
{{- if regexMatch (printf "(?i)%s" $key) $dbTypeInValues }}
  {{- $dbType = $value | quote }}
{{- end }}
{{- end }}
  "dbType": {{ $dbType }},
{{- end }}
  "isSharedHomePVCCreated": {{ .Values.volumes.sharedHome.persistentVolumeClaim.create }},
  "isServiceMonitorCreated": {{ .Values.monitoring.serviceMonitor.create }},
  "isGrafanaDashboardsCreated": {{ .Values.monitoring.grafana.createDashboards }},
  "isRunOnOpenshift": {{ .Capabilities.APIVersions.Has "route.openshift.io/v1/Route" }},
  "isRunWithRestrictedSCC": {{ .Values.openshift.runWithRestrictedSCC }},
  "isOpenshiftRouteCreated": {{ .Values.ingress.openShiftRoute}}
}
{{- end }}

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
{{- define "bamboo.sharedHome.permissionFix.command" -}}
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
{{- if .Values.bamboo.additionalCertificates.secretName }}
- name: keystore
  mountPath: /var/ssl
{{- end }}
{{- if or .Values.atlassianAnalyticsAndSupport.analytics.enabled .Values.atlassianAnalyticsAndSupport.helmValues.enabled }}
- name: helm-values
  mountPath: /opt/atlassian/helm
{{- end }}
{{- if or .Values.bamboo.tomcatConfig.generateByHelm .Values.openshift.runWithRestrictedSCC }}
- name: server-xml
  mountPath: /opt/atlassian/bamboo/conf/server.xml
  subPath: server.xml
- name: init-properties
  mountPath: /opt/atlassian/bamboo/atlassian-bamboo/WEB-INF/classes/bamboo-init.properties
  subPath: bamboo-init.properties
{{- end }}
{{- if or .Values.bamboo.seraphConfig.generateByHelm .Values.openshift.runWithRestrictedSCC }}
- name: seraph-config-xml
  mountPath: /opt/atlassian/bamboo/atlassian-bamboo/WEB-INF/classes/seraph-config.xml
  subPath: seraph-config.xml
{{- end }}
{{- end }}

{{/*
Define pod annotations here to allow template overrides when used as a sub chart
*/}}
{{- define "bamboo.podAnnotations" -}}
{{- range $key, $value := .Values.podAnnotations }}
{{ $key }}: {{ tpl $value $ | quote }}
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
{{- if or .Values.bamboo.additionalCertificates.secretName .Values.bamboo.additionalCertificates.secretList }}
- name: keystore
  emptyDir: {}
{{- if .Values.bamboo.additionalCertificates.secretName }}
- name: certs
  secret:
    secretName: {{ .Values.bamboo.additionalCertificates.secretName }}
{{- else }}
{{- range .Values.bamboo.additionalCertificates.secretList }}
- name: {{ .name }}
  secret:
    secretName: {{ .name }}
{{- end }}
{{- end }}
{{- end }}
{{- if or .Values.atlassianAnalyticsAndSupport.analytics.enabled .Values.atlassianAnalyticsAndSupport.helmValues.enabled }}
- name: helm-values
  configMap:
    name: {{ include "common.names.fullname" . }}-helm-values
{{- end }}
{{- if or .Values.bamboo.tomcatConfig.generateByHelm .Values.openshift.runWithRestrictedSCC }}
- name: server-xml
  configMap:
    name: {{ include "common.names.fullname" . }}-server-config
    items:
      - key: server.xml
        path: server.xml
- name: init-properties
  configMap:
    name: {{ include "common.names.fullname" . }}-init-properties
    items:
      - key: bamboo-init.properties
        path: bamboo-init.properties
{{- end }}
{{- if or .Values.bamboo.seraphConfig.generateByHelm .Values.openshift.runWithRestrictedSCC }}
- name: seraph-config-xml
  configMap:
    name: {{ include "common.names.fullname" . }}-server-config
    items:
      - key: seraph-config.xml
        path: seraph-config.xml
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
{{- if and .Values.volumes.localHome.persistentVolumeClaimRetentionPolicy.whenDeleted .Values.volumes.localHome.persistentVolumeClaimRetentionPolicy.whenScaled }}
persistentVolumeClaimRetentionPolicy:
    whenDeleted: {{.Values.volumes.localHome.persistentVolumeClaimRetentionPolicy.whenDeleted}}
    whenScaled: {{.Values.volumes.localHome.persistentVolumeClaimRetentionPolicy.whenScaled}}
{{- end}}
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
{{- if .Values.bamboo.forceConfigUpdate }}
- name: ATL_FORCE_CFG_UPDATE
  value: "true"
{{- end }}
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

{{/*
Define additional hosts here to allow template overrides when used as a sub chart
*/}}
{{- define "bamboo.additionalHosts" -}}
{{- with .Values.additionalHosts }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{- define "bamboo.addCrtToKeystoreCmd" }}
{{- if .Values.bamboo.additionalCertificates.customCmd}}
{{ .Values.bamboo.additionalCertificates.customCmd}}
{{- else }}
set -e; cp $JAVA_HOME/lib/security/cacerts /var/ssl/cacerts; chmod 664 /var/ssl/cacerts; for crt in /tmp/crt/*.*; do echo "Adding $crt to keystore"; keytool -import -keystore /var/ssl/cacerts -storepass changeit -noprompt -alias $(echo $(basename $crt)) -file $crt; done;
{{- end }}
{{- end }}
