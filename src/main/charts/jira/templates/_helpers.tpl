{{/* vim: set filetype=mustache: */}}

{{/* Define a sanitized list of additionalEnvironmentVariables */}}
{{- define "jira.sanitizedAdditionalEnvVars" -}}
{{- range .Values.jira.additionalEnvironmentVariables }}
- name: {{ .name }}
  value: {{ if regexMatch "(?i)(secret|token|password)" .name }}"Sanitized by Support Utility"{{ else}}{{ .value }}{{ end }}
{{- end }}
{{- end }}

{{/* Define a sanitized list of additionalJvmArgs */}}
{{- define "jira.sanitizedAdditionalJvmArgs" -}}
{{- range .Values.jira.additionalJvmArgs }}
 {{- $jvmArgs := regexSplit "=" . -1 }}
   {{- if regexMatch "(?i)(secret|token|password).*$" (first $jvmArgs) }}
-  {{ first $jvmArgs }}=Sanitized by Support Utility{{ else}}
-  {{ . }}
{{ end }}
{{- end }}
{{- end }}

{{/* Define sanitized Helm values */}}
{{- define "jira.sanitizedValues" -}}
{{- $sanitizedAdditionalEnvs := dict .Chart.Name (dict "additionalEnvironmentVariables" (include "jira.sanitizedAdditionalEnvVars" .)) }}
{{- $sanitizedAdditionalJvmArgs := dict .Chart.Name (dict "additionalJvmArgs" (include "jira.sanitizedAdditionalJvmArgs" .)) }}
{{- $mergedValues := merge $sanitizedAdditionalEnvs $sanitizedAdditionalJvmArgs .Values }}
{{- toYaml $mergedValues | replace " |2-" "" | replace " |-" "" |  replace "|2" "" | nindent 4 }}
{{- end }}

{{- define "jira.analyticsJson" -}}
{
  "imageTag": {{ if or (eq .Values.image.tag "") (eq .Values.image.tag nil) }}{{ .Chart.AppVersion | quote }}{{ else }}{{ regexSplit "-" .Values.image.tag -1 | first |  quote }}{{ end }},
  "replicas": {{ .Values.replicaCount }},
  "isJmxEnabled": {{ .Values.monitoring.exposeJmxMetrics }},
  "ingressType": {{ if not .Values.ingress.create }}"NONE"{{ else }}{{ if .Values.ingress.nginx }}"NGINX"{{ else }}"OTHER"{{ end }}{{ end }},
{{- $sanitizedMinorVersion := regexReplaceAll "[^0-9]" .Capabilities.KubeVersion.Minor "" }}
  "k8sVersion": "{{ .Capabilities.KubeVersion.Major }}.{{ $sanitizedMinorVersion }}",
  "isS3AvatarsEnabled": {{ if and .Values.jira.s3Storage.avatars.bucketName .Values.jira.s3Storage.avatars.bucketRegion }}true{{ else }}false{{ end }},
  "serviceType": {{ if regexMatch "^(ClusterIP|NodePort|LoadBalancer|ExternalName)$" .Values.jira.service.type }}{{ snakecase .Values.jira.service.type | upper | quote }}{{ else }}"UNKNOWN"{{ end }},
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
  "isClusteringEnabled": {{ .Values.jira.clustering.enabled }},
  "isSharedHomePVCCreated": {{ .Values.volumes.sharedHome.persistentVolumeClaim.create }},
  "isServiceMonitorCreated": {{ .Values.monitoring.serviceMonitor.create }},
  "isGrafanaDashboardsCreated": {{ .Values.monitoring.grafana.createDashboards }},
  "isRunOnOpenshift": {{ .Capabilities.APIVersions.Has "route.openshift.io/v1/Route" }},
  "isRunWithRestrictedSCC": {{ .Values.openshift.runWithRestrictedSCC }},
  "isOpenshiftRouteCreated": {{ .Values.ingress.openShiftRoute}}
}
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
{{- include "common.names.fullname" . -}}
{{- else -}}
default
{{- end -}}
{{- end -}}
{{- end }}

{{/*
Pod labels
*/}}
{{- define "jira.podLabels" -}}
{{ with .Values.podLabels }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
The command that should be run by the nfs-fixer init container to correct the permissions of the shared-home root directory.
*/}}
{{- define "jira.sharedHome.permissionFix.command" -}}
{{- $securityContext := .Values.jira.securityContext }}
{{- with .Values.volumes.sharedHome.nfsPermissionFixer }}
    {{- if .command }}
        {{ .command }}
    {{- else }}
        {{- if and $securityContext.gid $securityContext.enabled }}
            {{- printf "(chgrp %v %s; chmod g+w %s)" $securityContext.gid .mountPath .mountPath }}
        {{- else if $securityContext.fsGroup }}
            {{- printf "(chgrp %v %s; chmod g+w %s)" $securityContext.fsGroup .mountPath .mountPath }}
        {{- else }}
            {{- printf "(chgrp 2001 %s; chmod g+w %s)" .mountPath .mountPath }}
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
{{- if or .Values.jira.tomcatConfig.generateByHelm .Values.openshift.runWithRestrictedSCC }}
- name: server-xml
  mountPath: /opt/atlassian/jira/conf/server.xml
  subPath: server.xml
- name: temp
  mountPath: /opt/atlassian/jira/temp
- name: work
  mountPath: /opt/atlassian/jira/work
{{- end }}
{{- if or .Values.jira.seraphConfig.generateByHelm .Values.openshift.runWithRestrictedSCC }}
- name: seraph-config-xml
  mountPath: /opt/atlassian/jira/atlassian-jira/WEB-INF/classes/seraph-config.xml
  subPath: seraph-config.xml
{{- end }}
{{- if .Values.jira.additionalCertificates.secretName }}
- name: keystore
  mountPath: /var/ssl
{{- end }}
{{- if or .Values.atlassianAnalyticsAndSupport.analytics.enabled .Values.atlassianAnalyticsAndSupport.helmValues.enabled }}
- name: helm-values
  mountPath: /opt/atlassian/helm
{{- end }}
{{- end }}

{{/*
Define pod annotations here to allow template overrides when used as a sub chart
*/}}
{{- define "jira.podAnnotations" -}}
{{- range $key, $value := .Values.podAnnotations }}
{{ $key }}: {{ tpl $value $ | quote }}
{{- end }}
{{- end }}

{{/*
Define additional init containers here to allow template overrides when used as a sub chart
*/}}
{{- define "jira.additionalInitContainers" -}}
{{- with .Values.additionalInitContainers }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Define additional containers here to allow template overrides when used as a sub chart
*/}}
{{- define "jira.additionalContainers" -}}
{{- with .Values.additionalContainers }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Define additional ports here instead of in values.yaml to allow template overrides
*/}}
{{- define "jira.additionalPorts" -}}
{{- with .Values.jira.additionalPorts }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Define additional volume mounts here to allow template overrides when used as a sub chart
*/}}
{{- define "jira.additionalVolumeMounts" -}}
{{- with .Values.jira.additionalVolumeMounts }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Define additional environment variables here to allow template overrides when used as a sub chart
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
{{- if or .Values.jira.tomcatConfig.generateByHelm .Values.openshift.runWithRestrictedSCC }}
- name: server-xml
  configMap:
    name: {{ include "common.names.fullname" . }}-server-config
    items:
      - key: server.xml
        path: server.xml
- name: temp
  emptyDir: {}
- name: work
  emptyDir: {}
{{- end }}
{{- if or .Values.jira.seraphConfig.generateByHelm .Values.openshift.runWithRestrictedSCC }}
- name: seraph-config-xml
  configMap:
    name: {{ include "common.names.fullname" . }}-server-config
    items:
      - key: seraph-config.xml
        path: seraph-config.xml
{{- end }}
{{- if .Values.jira.additionalCertificates.secretName }}
- name: keystore
  emptyDir: {}
- name: certs
  secret:
    secretName: {{ .Values.jira.additionalCertificates.secretName }}
{{- end }}
{{- if or .Values.atlassianAnalyticsAndSupport.analytics.enabled .Values.atlassianAnalyticsAndSupport.helmValues.enabled }}
- name: helm-values
  configMap:
    name: {{ include "common.names.fullname" . }}-helm-values
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
    claimName: {{ include "common.names.fullname" . }}-shared-home
{{ else }}
{{ if .Values.volumes.sharedHome.customVolume }}
{{- toYaml .Values.volumes.sharedHome.customVolume | nindent 2 }}
{{ else }}
  emptyDir: {}
{{- end }}
{{- end }}
{{- end }}


{{/*
Define additional hosts here to allow template overrides when used as a sub chart
*/}}
{{- define "jira.additionalHosts" -}}
{{- with .Values.additionalHosts }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{- define "jira.volumeClaimTemplates" -}}
{{- if or .Values.volumes.localHome.persistentVolumeClaim.create .Values.jira.additionalVolumeClaimTemplates }}
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
{{- range .Values.jira.additionalVolumeClaimTemplates }}
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

{{- define "jira.s3StorageEnvVars" -}}
{{- if and .Values.jira.s3Storage.avatars.bucketName .Values.jira.s3Storage.avatars.bucketRegion }}
- name: ATL_S3AVATARS_BUCKET_NAME
  value: {{ .Values.jira.s3Storage.avatars.bucketName | quote }}
- name: ATL_S3AVATARS_REGION
  value: {{ .Values.jira.s3Storage.avatars.bucketRegion | quote }}
{{- if .Values.jira.s3Storage.avatars.endpointOverride }}
- name: ATL_S3AVATARS_ENDPOINT_OVERRIDE
  value: {{ .Values.jira.s3Storage.avatars.endpointOverride | quote }}
{{- end }}
{{- end }}
{{- end }}

{{- define "jira.databaseEnvVars" -}}
{{- if .Values.jira.forceConfigUpdate }}
- name: ATL_FORCE_CFG_UPDATE
  value: "true"
{{- end }}
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

{{- define "flooredCPU" -}}
    {{- if hasSuffix "m" (. | toString) }}
    {{- div (trimSuffix "m" .) 1000 | default 1 }}
    {{- else }}
    {{- . }}
    {{- end }}
{{- end}}

{{- define "jira.addCrtToKeystoreCmd" }}
{{- if .Values.jira.additionalCertificates.customCmd}}
{{ .Values.jira.additionalCertificates.customCmd}}
{{- else }}
set -e; cp $JAVA_HOME/lib/security/cacerts /var/ssl/cacerts; chmod 664 /var/ssl/cacerts; for crt in /tmp/crt/*.*; do echo "Adding $crt to keystore"; keytool -import -keystore /var/ssl/cacerts -storepass changeit -noprompt -alias $(echo $(basename $crt)) -file $crt; done;
{{- end }}
{{- end }}
