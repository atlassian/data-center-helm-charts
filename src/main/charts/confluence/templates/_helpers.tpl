{{/* vim: set filetype=mustache: */}}

{{/* Define a sanitized list of additionalEnvironmentVariables */}}
{{- define "confluence.sanitizedAdditionalEnvVars" -}}
{{- range .Values.confluence.additionalEnvironmentVariables }}
- name: {{ .name }}
  value: {{ if regexMatch "(?i)(secret|token|password)" .name }}"Sanitized by Support Utility"{{ else}}{{ .value }}{{ end }}
{{- end }}
{{- end }}

{{/* Define a sanitized list of additionalJvmArgs */}}
{{- define "confluence.sanitizedAdditionalJvmArgs" -}}
{{- range .Values.confluence.additionalJvmArgs }}
 {{- $jvmArgs := regexSplit "=" . -1 }}
   {{- if regexMatch "(?i)(secret|token|password).*$" (first $jvmArgs) }}
-  {{ first $jvmArgs }}=Sanitized by Support Utility{{ else}}
-  {{ . }}
{{ end }}
{{- end }}
{{- end }}

{{/* Define sanitized Helm values */}}
{{- define "confluence.sanitizedValues" -}}
{{- $sanitizedAdditionalEnvs := dict .Chart.Name (dict "additionalEnvironmentVariables" (include "confluence.sanitizedAdditionalEnvVars" .)) }}
{{- $sanitizedAdditionalJvmArgs := dict .Chart.Name (dict "additionalJvmArgs" (include "confluence.sanitizedAdditionalJvmArgs" .)) }}
{{- $mergedValues := merge $sanitizedAdditionalEnvs $sanitizedAdditionalJvmArgs .Values }}
{{- toYaml $mergedValues | replace " |2-" "" | replace " |-" "" |  replace "|2" "" | nindent 4 }}
{{- end }}

{{- define "confluence.analyticsJson" }}
{
  "imageTag": {{ if or (eq .Values.image.tag "") (eq .Values.image.tag nil) }}{{ .Chart.AppVersion | quote }}{{ else }}{{ regexSplit "-" .Values.image.tag -1 | first |  quote }}{{ end }},
  "replicas": {{ .Values.replicaCount }},
  "isJmxEnabled": {{ .Values.monitoring.exposeJmxMetrics }},
  "ingressType": {{ if not .Values.ingress.create }}"NONE"{{ else }}{{ if .Values.ingress.nginx }}"NGINX"{{ else }}"OTHER"{{ end }}{{ end }},
{{- $sanitizedMinorVersion := regexReplaceAll "[^0-9]" .Capabilities.KubeVersion.Minor "" }}
  "k8sVersion": "{{ .Capabilities.KubeVersion.Major }}.{{ $sanitizedMinorVersion }}",
  "serviceType": {{ if regexMatch "^(ClusterIP|NodePort|LoadBalancer|ExternalName)$" .Values.confluence.service.type }}{{ snakecase .Values.confluence.service.type | upper | quote }}{{ else }}"UNKNOWN"{{ end }},
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
  "isS3AttachmentsStorageEnabled": {{- if and .Values.confluence.s3AttachmentsStorage.bucketName .Values.confluence.s3AttachmentsStorage.bucketRegion }}true{{ else }}false{{ end }},
  "isClusteringEnabled": {{ .Values.confluence.clustering.enabled }},
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
{{- define "confluence.ingressPort" -}}
{{ default (ternary "443" "80" .Values.ingress.https) .Values.ingress.port -}}
{{- end }}

{{/*
The name the synchrony app within the chart.
TODO: This will break if the common.names.name exceeds 63 characters, need to find a more rebust way to do this
*/}}
{{- define "synchrony.name" -}}
{{ include "common.names.name" . }}-synchrony
{{- end }}

{{/*
The full-qualfied name of the synchrony app within the chart.
TODO: This will break if the confluence.fullname exceeds 63 characters, need to find a more rebust way to do this
*/}}
{{- define "synchrony.fullname" -}}
{{ include "common.names.fullname" . }}-synchrony
{{- end }}

{{/*
The name of the service account to be used.
If the name is defined in the chart values, then use that,
else if we're creating a new service account then use the name of the Helm release,
else just use the "default" service account.
*/}}
{{- define "confluence.serviceAccountName" -}}
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
The name of the ClusterRole that will be created.
If the name is defined in the chart values, then use that,
else use the name of the Helm release.
*/}}
{{- define "confluence.clusterRoleName" -}}
{{- if and .Values.serviceAccount.clusterRole.name .Values.serviceAccount.clusterRole.create }}
{{- .Values.serviceAccount.clusterRole.name }}
{{- else }}
{{- include "common.names.fullname" . -}}
{{- end }}
{{- end }}

{{/*
The name of the ClusterRoleBinding that will be created.
If the name is defined in the chart values, then use that,
else use the name of the ClusterRole.
*/}}
{{- define "confluence.clusterRoleBindingName" -}}
{{- if and .Values.serviceAccount.clusterRoleBinding.name .Values.serviceAccount.clusterRoleBinding.create }}
{{- .Values.serviceAccount.clusterRoleBinding.name }}
{{- else }}
{{- include "confluence.clusterRoleName" . -}}
{{- end }}
{{- end }}

{{/*
These labels will be applied to all Synchrony resources in the chart
*/}}
{{- define "synchrony.labels" -}}
helm.sh/chart: {{ include "common.names.chart" . }}
{{ include "synchrony.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{ with .Values.additionalLabels }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Selector labels for finding Confluence (non-Synchrony) resources
*/}}
{{- define "confluence.selectorLabels" -}}
app.kubernetes.io/name: {{ include "common.names.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Selector labels for finding Synchrony resources
*/}}
{{- define "synchrony.selectorLabels" -}}
app.kubernetes.io/name: {{ include "synchrony.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Pod labels
*/}}
{{- define "confluence.podLabels" -}}
{{ with .Values.podLabels }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{- define "confluence.sysprop.hazelcastListenPort" -}}
-Dconfluence.cluster.hazelcast.listenPort={{ .Values.confluence.ports.hazelcast }}
{{- end }}

{{- define "confluence.sysprop.s3Config" -}}
{{- if and .Values.confluence.s3AttachmentsStorage.bucketName .Values.confluence.s3AttachmentsStorage.bucketRegion }}
-Dconfluence.filestore.attachments.s3.bucket.name={{ .Values.confluence.s3AttachmentsStorage.bucketName }}
-Dconfluence.filestore.attachments.s3.bucket.region={{ .Values.confluence.s3AttachmentsStorage.bucketRegion }}
{{- if .Values.confluence.s3AttachmentsStorage.endpointOverride }}
-Dconfluence.filestore.attachments.s3.endpoint.override={{ .Values.confluence.s3AttachmentsStorage.endpointOverride }}
{{- end }}
{{- end }}
{{- end }}

{{- define "confluence.sysprop.clusterNodeName" -}}
-Dconfluence.clusterNodeName.useHostname={{ .Values.confluence.clustering.usePodNameAsClusterNodeName }}
{{- end }}

{{- define "confluence.sysprop.fluentdAppender" -}}
-Datlassian.logging.cloud.enabled={{ if and .Values.fluentd.enabled (not .Values.fluentd.customConfigFile ) }}{{.Values.fluentd.enabled }}{{ else }}false{{ end }}
{{- end }}

{{- define "confluence.sysprop.debug" -}}
{{- if .Values.confluence.jvmDebug.enabled }} -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 {{ end -}}
{{- end }}

{{- define "confluence.sysprop.enable.synchrony.by.default" -}}
-Dsynchrony.by.default.enable.collab.editing.if.manually.managed=true
{{- end -}}

{{- define "confluence.sysprop.synchronyServiceUrl" -}}
{{- $synchronyIngressPath := "synchrony" }}
{{- if .Values.synchrony.ingress.path }}
{{- $sanitizePathRegex := "^/|\\(.*" }}
{{- $synchronyIngressPath = regexReplaceAll $sanitizePathRegex .Values.synchrony.ingress.path "" }}
{{- end }}
{{- if .Values.synchrony.enabled -}}
    {{- if .Values.ingress.https -}}-Dsynchrony.service.url=https://{{ .Values.ingress.host }}/{{ $synchronyIngressPath }}/v1
    {{- else }}-Dsynchrony.service.url=http://{{ .Values.ingress.host }}/{{ $synchronyIngressPath }}/v1
    {{- end }}
{{- else -}}
-Dsynchrony.btf.disabled=true
{{- end -}}
{{- end -}}

{{/*
Create default value for ingress path
*/}}
{{- define "confluence.ingressPath" -}}
{{- if .Values.ingress.path -}}
{{- .Values.ingress.path -}}
{{- else -}}
{{ default ( "/" ) .Values.confluence.service.contextPath -}}
{{- end }}
{{- end }}

{{/*
The command that should be run by the nfs-fixer init container to correct the permissions of the shared-home root directory.
*/}}
{{- define "confluence.sharedHome.permissionFix.command" -}}
{{- $securityContext := .Values.confluence.securityContext }}
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

{{- define "confluence.image" -}}
{{- if .Values.image.registry -}}
{{ .Values.image.registry}}/{{ .Values.image.repository }}:{{ .Values.image.tag | default .Chart.AppVersion }}
{{- else -}}
{{ .Values.image.repository }}:{{ .Values.image.tag | default .Chart.AppVersion }}
{{- end }}
{{- end }}

{{/*
Defines the volume mounts used by the Confluence container.
Note that the local-home volume is mounted twice, once for the local-home directory itself, and again
on Tomcat's logs directory. THis ensures that Tomcat+Confluence logs get captured in the same volume.
*/}}
{{ define "confluence.volumeMounts" }}
- name: local-home
  mountPath: {{ .Values.volumes.localHome.mountPath | quote }}
  {{- if .Values.volumes.localHome.subPath }}
  subPath: {{ .Values.volumes.localHome.subPath | quote }}
  {{- end }}
- name: local-home
  mountPath: {{ .Values.confluence.accessLog.mountPath | quote }}
  subPath: {{ .Values.confluence.accessLog.localHomeSubPath | quote }}
- name: shared-home
  mountPath: {{ .Values.volumes.sharedHome.mountPath | quote }}
  {{- if .Values.volumes.sharedHome.subPath }}
  subPath: {{ .Values.volumes.sharedHome.subPath | quote }}
  {{- end }}
{{- if or .Values.confluence.tomcatConfig.generateByHelm .Values.openshift.runWithRestrictedSCC }}
- name: server-xml
  mountPath: /opt/atlassian/confluence/conf/server.xml
  subPath: server.xml
{{- end }}
{{- if or .Values.confluence.seraphConfig.generateByHelm .Values.openshift.runWithRestrictedSCC }}
- name: seraph-config-xml
  mountPath: /opt/atlassian/confluence/confluence/WEB-INF/classes/seraph-config.xml
  subPath: seraph-config.xml
{{- end }}
{{- if or .Values.confluence.additionalCertificates.secretName .Values.confluence.additionalCertificates.secretList }}
- name: keystore
  mountPath: /var/ssl
{{- end }}
{{- if or .Values.atlassianAnalyticsAndSupport.analytics.enabled .Values.atlassianAnalyticsAndSupport.helmValues.enabled }}
- name: helm-values
  mountPath: /opt/atlassian/helm
{{- end }}
{{ end }}

{{/*
Defines the volume mounts used by the Synchrony container.
*/}}
{{ define "synchrony.volumeMounts" }}
- name: synchrony-home
  mountPath: {{ .Values.volumes.synchronyHome.mountPath | quote }}
{{- if or .Values.synchrony.additionalCertificates.secretName .Values.synchrony.additionalCertificates.secretList }}
- name: keystore
  mountPath: /var/ssl
{{- end }}
{{ end }}

{{/*
For each additional library declared, generate a volume mount that injects that library into the Confluence lib directory
*/}}
{{- define "confluence.additionalLibraries" -}}
{{- range .Values.confluence.additionalLibraries }}
- name: {{ .volumeName }}
  mountPath: "/opt/atlassian/confluence/confluence/WEB-INF/lib/{{ .fileName }}"
  {{- if .subDirectory }}
  subPath: {{ printf "%s/%s" .subDirectory .fileName | quote }}
  {{- else }}
  subPath: {{ .fileName | quote }}
  {{- end }}
{{- end }}
{{- end }}

{{/*
For each additional Synchrony library declared, generate a volume mount that injects that library into the Confluence lib directory
*/}}
{{- define "synchrony.additionalLibraries" -}}
{{- range .Values.synchrony.additionalLibraries }}
- name: {{ .volumeName }}
  mountPath: "/opt/atlassian/confluence/confluence/WEB-INF/lib/{{ .fileName }}"
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
{{- define "confluence.podAnnotations" -}}
{{- range $key, $value := .Values.podAnnotations }}
{{ $key }}: {{ tpl $value $ | quote }}
{{- end }}
{{- end }}

{{/*
Define pod annotations here to allow template overrides when used as a sub chart
*/}}
{{- define "synchrony.podAnnotations" -}}
{{- with .Values.synchrony.podAnnotations }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Define additional init containers here to allow template overrides when used as a sub chart
*/}}
{{- define "confluence.additionalInitContainers" -}}
{{- with .Values.additionalInitContainers }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Define additional hosts here to allow template overrides when used as a sub chart
*/}}
{{- define "confluence.additionalHosts" -}}
{{- with .Values.additionalHosts }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Define additional containers here to allow template overrides when used as a sub chart
*/}}
{{- define "confluence.additionalContainers" -}}
{{- with .Values.additionalContainers }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Define additional ports here instead of in values.yaml to allow template overrides
*/}}
{{- define "confluence.additionalPorts" -}}
{{- with .Values.confluence.additionalPorts }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Define additional ports here instead of in values.yaml to allow template overrides
*/}}
{{- define "synchrony.additionalPorts" -}}
{{- with .Values.synchrony.additionalPorts }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Define additional Confluence volume mounts here to allow template overrides when used as a sub chart
*/}}
{{- define "confluence.additionalVolumeMounts" -}}
{{- with .Values.confluence.additionalVolumeMounts }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Define additional Synchrony volume mounts here to allow template overrides when used as a sub chart
*/}}
{{- define "synchrony.additionalVolumeMounts" -}}
{{- with .Values.synchrony.additionalVolumeMounts }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
Define additional environment variables here to allow template overrides when used as a sub chart
*/}}
{{- define "confluence.additionalEnvironmentVariables" -}}
{{- with .Values.confluence.additionalEnvironmentVariables }}
{{- toYaml . }}
{{- end }}
{{- end }}

{{/*
For each additional plugin declared, generate a volume mount that injects that library into the Confluence plugins directory
*/}}
{{- define "confluence.additionalBundledPlugins" -}}
{{- range .Values.confluence.additionalBundledPlugins }}
- name: {{ .volumeName }}
  mountPath: "/opt/atlassian/confluence/confluence/WEB-INF/atlassian-bundled-plugins/{{ .fileName }}"
  {{- if .subDirectory }}
  subPath: {{ printf "%s/%s" .subDirectory .fileName | quote }}
  {{- else }}
  subPath: {{ .fileName | quote }}
  {{- end }}
{{- end }}
{{- end }}

{{- define "confluence.volumes" -}}
{{ if not .Values.volumes.localHome.persistentVolumeClaim.create }}
{{ include "confluence.volumes.localHome" . }}
{{- end }}
{{ include "confluence.volumes.sharedHome" . }}
{{- with .Values.volumes.additional }}
{{- toYaml . | nindent 0 }}
{{- end }}
{{- if or .Values.confluence.tomcatConfig.generateByHelm .Values.openshift.runWithRestrictedSCC }}
- name: server-xml
  configMap:
    name: {{ include "common.names.fullname" . }}-server-config
    items:
      - key: server.xml
        path: server.xml
{{- end }}
{{- if or .Values.confluence.seraphConfig.generateByHelm .Values.openshift.runWithRestrictedSCC }}
- name: seraph-config-xml
  configMap:
    name: {{ include "common.names.fullname" . }}-server-config
    items:
      - key: seraph-config.xml
        path: seraph-config.xml
{{- end }}
{{- if or .Values.confluence.additionalCertificates.secretName .Values.confluence.additionalCertificates.secretList }}
- name: keystore
  emptyDir: {}
{{- if .Values.confluence.additionalCertificates.secretName }}
- name: certs
  secret:
    secretName: {{ .Values.confluence.additionalCertificates.secretName }}
{{- else }}
{{- range .Values.confluence.additionalCertificates.secretList }}
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
{{- end }}

{{- define "synchrony.volumes" -}}
{{ if not .Values.volumes.synchronyHome.persistentVolumeClaim.create }}
{{ include "synchrony.volumes.synchronyHome" . }}
{{- end }}
{{- if .Values.synchrony.additionalLibraries }}
{{ include "confluence.volumes.sharedHome" . }}
{{- end }}
{{- with .Values.volumes.additionalSynchrony }}
{{- toYaml . | nindent 0 }}
{{- end }}
{{- if or .Values.synchrony.additionalCertificates.secretName .Values.synchrony.additionalCertificates.secretList }}
- name: keystore
  emptyDir: {}
{{- if .Values.synchrony.additionalCertificates.secretName }}
- name: certs
  secret:
    secretName: {{ .Values.synchrony.additionalCertificates.secretName }}
{{- else }}
{{- range .Values.synchrony.additionalCertificates.secretList }}
- name: {{ .name }}
  secret:
    secretName: {{ .name }}
{{- end }}
{{- end }}
{{- end }}
{{- end }}

{{- define "confluence.volumes.localHome" -}}
{{- if not .Values.volumes.localHome.persistentVolumeClaim.create }}
- name: local-home
{{ if .Values.volumes.localHome.customVolume }}
{{- toYaml .Values.volumes.localHome.customVolume | nindent 2 }}
{{ else }}
  emptyDir: {}
{{- end }}
{{- end }}
{{- end }}

{{- define "synchrony.volumes.synchronyHome" -}}
{{- if not .Values.volumes.synchronyHome.persistentVolumeClaim.create }}
- name: synchrony-home
{{ if .Values.volumes.synchronyHome.customVolume }}
{{- toYaml .Values.volumes.synchronyHome.customVolume | nindent 2 }}
{{ else }}
  emptyDir: {}
{{- end }}
{{- end }}
{{- end }}

{{- define "confluence.volumes.sharedHome" -}}
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

{{- define "confluence.volumeClaimTemplates" -}}
{{- if or .Values.volumes.localHome.persistentVolumeClaim.create .Values.confluence.additionalVolumeClaimTemplates }}
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
{{- range .Values.confluence.additionalVolumeClaimTemplates }}
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

{{- define "synchrony.volumeClaimTemplates" -}}
{{ if .Values.volumes.synchronyHome.persistentVolumeClaim.create }}
{{- if and .Values.volumes.synchronyHome.persistentVolumeClaimRetentionPolicy.whenDeleted .Values.volumes.synchronyHome.persistentVolumeClaimRetentionPolicy.whenScaled }}
persistentVolumeClaimRetentionPolicy:
    whenDeleted: {{.Values.volumes.synchronyHome.persistentVolumeClaimRetentionPolicy.whenDeleted}}
    whenScaled: {{.Values.volumes.synchronyHome.persistentVolumeClaimRetentionPolicy.whenScaled}}
{{- end}}
volumeClaimTemplates:
- metadata:
    name: synchrony-home
  spec:
    accessModes: [ "ReadWriteOnce" ]
    {{- if .Values.volumes.synchronyHome.persistentVolumeClaim.storageClassName }}
    storageClassName: {{ .Values.volumes.synchronyHome.persistentVolumeClaim.storageClassName | quote }}
    {{- end }}
    {{- with .Values.volumes.synchronyHome.persistentVolumeClaim.resources }}
    resources:
      {{- toYaml . | nindent 6 }}
    {{- end }}
{{- end }}
{{- end }}

{{- define "confluence.tunnelVars"}}
{{- if .Values.confluence.tunnel.additionalConnector.port }}
{{- with .Values.confluence.tunnel.additionalConnector.port }}
- name: ATL_TOMCAT_ADDITIONAL_CONNECTOR_PORT
  value: {{ . | quote }}
{{- end }}
{{- with .Values.confluence.tunnel.additionalConnector.connectionTimeout }}
- name: ATL_TOMCAT_ADDITIONAL_CONNECTOR_CONNECTION_TIMEOUT
  value: {{ . | quote }}
{{- end }}
{{- with .Values.confluence.tunnel.additionalConnector.maxThreads }}
- name: ATL_TOMCAT_ADDITIONAL_CONNECTOR_MAX_THREADS
  value: {{ . | quote }}
{{- end }}
{{- with .Values.confluence.tunnel.additionalConnector.minSpareThreads }}
- name: ATL_TOMCAT_ADDITIONAL_CONNECTOR_MIN_SPARE_THREADS
  value: {{ . | quote }}
{{- end }}
{{- with .Values.confluence.tunnel.additionalConnector.enableLookups }}
- name: ATL_TOMCAT_ADDITIONAL_CONNECTOR_ENABLE_LOOKUPS
  value: {{ . | quote }}
{{- end }}
{{- with .Values.confluence.tunnel.additionalConnector.acceptCount }}
- name: ATL_TOMCAT_ADDITIONAL_CONNECTOR_ACCEPT_COUNT
  value: {{ . | quote }}
{{- end }}
{{- with .Values.confluence.tunnel.additionalConnector.secure }}
- name: ATL_TOMCAT_ADDITIONAL_CONNECTOR_SECURE
  value: {{ . | quote }}
{{- end }}
{{- with .Values.confluence.tunnel.additionalConnector.URIEncoding }}
- name: ATL_TOMCAT_ADDITIONAL_CONNECTOR_URI_ENCODING
  value: {{ . | quote }}
{{- end }}
{{- end }}
{{- end }}

{{- define "confluence.databaseEnvVars" -}}
{{- if .Values.confluence.forceConfigUpdate }}
- name: ATL_FORCE_CFG_UPDATE
  value: "true"
{{- end }}
{{ with .Values.database.type }}
- name: ATL_DB_TYPE
  value: {{ . | quote }}
{{ end }}
{{ with .Values.database.url }}
- name: ATL_JDBC_URL
  value: {{ if contains "&amp;" . }}{{ . | quote }}{{ else }}{{ . | replace "&" "&amp;" | quote }}{{ end }}
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

{{- define "synchrony.databaseEnvVars" -}}
{{ with .Values.database.url }}
- name: SYNCHRONY_DATABASE_URL
  value: {{ . | replace "&amp;" "&" | quote }}
{{ end }}
{{ with .Values.database.credentials.secretName }}
- name: SYNCHRONY_DATABASE_USERNAME
  valueFrom:
    secretKeyRef:
      name: {{ . }}
      key: {{ $.Values.database.credentials.usernameSecretKey }}
- name: SYNCHRONY_DATABASE_PASSWORD
  valueFrom:
    secretKeyRef:
      name: {{ . }}
      key: {{ $.Values.database.credentials.passwordSecretKey }}
{{ end }}
{{ end }}

{{- define "confluence.clusteringEnvVars" -}}
{{ if .Values.confluence.clustering.enabled }}
- name: KUBERNETES_NAMESPACE
  valueFrom:
    fieldRef:
      fieldPath: metadata.namespace
- name: HAZELCAST_KUBERNETES_SERVICE_NAME
  value: {{ include "common.names.fullname" . | quote }}
- name: HAZELCAST_KUBERNETES_SERVICE_PORT
  value: {{ .Values.confluence.ports.hazelcast | quote }}
- name: ATL_CLUSTER_TYPE
  value: "kubernetes"
- name: ATL_CLUSTER_NAME
  value: {{ include "common.names.fullname" . | quote }}
{{ end }}
{{ end }}

{{- define "synchrony.clusteringEnvVars" -}}
{{ if .Values.confluence.clustering.enabled }}
- name: KUBERNETES_NAMESPACE
  valueFrom:
    fieldRef:
      fieldPath: metadata.namespace
- name: HAZELCAST_KUBERNETES_SERVICE_NAME
  value: {{ include "synchrony.fullname" . | quote }}
- name: HAZELCAST_KUBERNETES_SERVICE_PORT
  value: {{ .Values.synchrony.ports.hazelcast | quote }}
- name: CLUSTER_JOIN_TYPE
  value: "kubernetes"
{{ end }}
{{ end }}

{{- define "flooredCPU" -}}
    {{- if hasSuffix "m" (. | toString) }}
    {{- div (trimSuffix "m" .) 1000 | default 1 }}
    {{- else }}
    {{- . }}
    {{- end }}
{{- end}}

{{- define "confluence.addCrtToKeystoreCmd" }}
{{- if .Values.confluence.additionalCertificates.customCmd}}
{{ .Values.confluence.additionalCertificates.customCmd}}
{{- else }}
set -e; cp $JAVA_HOME/lib/security/cacerts /var/ssl/cacerts; chmod 664 /var/ssl/cacerts; for crt in /tmp/crt/*.*; do echo "Adding $crt to keystore"; keytool -import -keystore /var/ssl/cacerts -storepass changeit -noprompt -alias $(echo $(basename $crt)) -file $crt; done;
{{- end }}
{{- end }}

{{- define "synchrony.addCrtToKeystoreCmd" }}
{{- if .Values.synchrony.additionalCertificates.customCmd}}
{{ .Values.synchrony.additionalCertificates.customCmd}}
{{- else }}
set -e; cp $JAVA_HOME/lib/security/cacerts /var/ssl/cacerts; chmod 664 /var/ssl/cacerts; for crt in /tmp/crt/*.*; do echo "Adding $crt to keystore"; keytool -import -keystore /var/ssl/cacerts -storepass changeit -noprompt -alias $(echo $(basename $crt)) -file $crt; done;
{{- end }}
{{- end }}


{{- define "generate_static_password_b64enc" -}}
{{- if not (index .Release "temp_vars") -}}
{{-   $_ := set .Release "temp_vars" dict -}}
{{- end -}}
{{- $key := printf "%s_%s" .Release.Name "password" -}}
{{- if not (index .Release.temp_vars $key) -}}
{{-   $_ := set .Release.temp_vars $key (randAlphaNum 40 | b64enc ) -}}
{{- end -}}
{{- index .Release.temp_vars $key -}}
{{- end -}}

{{- define "opensearch.initial.admin.password" }}
{{- $defaultSecretName := "opensearch-initial-password" }}
{{- $secretName := default $defaultSecretName .Values.opensearch.credentials.existingSecretRef.name }}
{{- $secretData := (lookup "v1" "Secret" .Release.Namespace $secretName) }}
{{- if $secretData.data }}
{{- index $secretData.data "OPENSEARCH_INITIAL_ADMIN_PASSWORD" }}
{{- else }}
{{ include "generate_static_password_b64enc" . }}
{{- end }}
{{- end }}
