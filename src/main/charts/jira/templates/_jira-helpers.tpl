{{/* Define Jira StatefulSet that can be used to create additional StatefulSets with overrides */}}
{{- define "jira.base.statefulset" -}}
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: {{ include "common.names.fullname" . }}
  labels:
    {{- include "common.labels.commonLabels" . | nindent 4 }}
  annotations:
    {{- with .Values.jira.additionalAnnotations }}
    {{- toYaml . | nindent 4 }}
    {{- end }}
spec:
  {{- if .Values.updateStrategy }}
  {{- with .Values.updateStrategy }}
  updateStrategy:
  {{- toYaml . | nindent 4 }}
  {{- end }}
  {{- end }}
  {{ if .Values.ordinals.enabled }}
  ordinals:
    start: {{ .Values.ordinals.start }}
  {{ end }}
  replicas: {{ .Values.replicaCount }}
  serviceName: {{ include "common.names.fullname" . }}
  selector:
    matchLabels:
      {{- include "common.labels.selectorLabels" . | nindent 6 }}
  template:
    metadata:
      annotations:
        checksum/config-jvm: {{ include (print $.Template.BasePath "/config-jvm.yaml") . | sha256sum }}
        {{- if .Values.additionalConfigMaps }}
        checksum/config-additional: {{ include (print $.Template.BasePath "/configmap-additional-config.yaml") . | sha256sum }}
        {{- end }}
        {{- if .Values.fluentd.enabled }}
        checksum/config-fluentd: {{ include (print $.Template.BasePath "/configmap-fluentd.yaml") . | sha256sum }}
        {{- end }}
        {{- include "jira.podAnnotations" . | nindent 8 }}
      labels:
        {{- include "common.labels.selectorLabels" . | nindent 8 }}
        {{- include "jira.podLabels" . | nindent 8 }}
    spec:
      serviceAccountName: {{ include "jira.serviceAccountName" . }}
      terminationGracePeriodSeconds: {{ .Values.jira.shutdown.terminationGracePeriodSeconds }}
      hostAliases:
      {{- include "jira.additionalHosts" . | nindent 8 }}
      {{- if .Values.openshift.runWithRestrictedSCC }}
      {{- else }}
      {{- if .Values.jira.securityContextEnabled }}
      {{- with .Values.jira.securityContext }}
      securityContext:
        {{/* this condition is to be removed in v2.0.0 */}}
        {{- if and .enabled .gid }}
        fsGroup: {{ .gid }}
        {{- else }}
        {{ toYaml . | nindent 8 }}
        {{- if not .fsGroup }}
        fsGroup: 2001
        {{- end }}
        {{- end }}
      {{- end }}
      {{- end }}
      {{- end }}
      initContainers:
        {{- include "jira.additionalInitContainers" . | nindent 8 }}
        {{- if and .Values.volumes.sharedHome.nfsPermissionFixer.enabled (not .Values.openshift.runWithRestrictedSCC) }}
        - name: nfs-permission-fixer
          image: {{ .Values.volumes.sharedHome.nfsPermissionFixer.imageRepo }}:{{ .Values.volumes.sharedHome.nfsPermissionFixer.imageTag }}
          imagePullPolicy: IfNotPresent
          {{- if .Values.volumes.sharedHome.nfsPermissionFixer.resources }}
          resources:
          {{- with .Values.volumes.sharedHome.nfsPermissionFixer.resources }}
          {{- toYaml . | nindent 12 }}
          {{- end }}
          {{- end }}
          securityContext:
            runAsUser: 0 # make sure we run as root so we get the ability to change the volume permissions
          volumeMounts:
            - name: shared-home
              mountPath: {{ .Values.volumes.sharedHome.nfsPermissionFixer.mountPath | quote }}
              {{- if .Values.volumes.sharedHome.subPath }}
              subPath: {{ .Values.volumes.sharedHome.subPath | quote }}
              {{- end }}
          command: ["sh", "-c", {{ include "jira.sharedHome.permissionFix.command" . | quote }}]
        {{- end }}
        {{- include "common.jmx.initContainer" . | nindent 8 }}
        {{- if or .Values.jira.additionalCertificates.secretName .Values.jira.additionalCertificates.secretList }}
        - name: import-certs
          image: {{ include "jira.image" . | quote }}
          imagePullPolicy: {{ .Values.image.pullPolicy }}
          volumeMounts:
            - name: keystore
              mountPath: /var/ssl
          {{- if.Values.jira.additionalCertificates.secretName }}
            - name: certs
              mountPath: /tmp/crt
          {{- else }}
          {{- range .Values.jira.additionalCertificates.secretList }}
            {{- $secretName := .name }}
            {{- range .keys }}
            - name: {{ $secretName }}
              mountPath: /tmp/crt/{{$secretName}}-{{ . }}
              subPath: {{ . }}
            {{- end }}
          {{- end }}
          {{- end }}
          command: ["/bin/bash"]
          args: ["-c", {{ include "jira.addCrtToKeystoreCmd" . }}]
          resources:
          {{- with .Values.jira.additionalCertificates.initContainer.resources }}
          {{- toYaml . | nindent 12 }}
          {{- end }}
        {{- end }}
      containers:
        - name: {{ if .Values.jira.useHelmReleaseNameAsContainerName}}{{ include "common.names.fullname" . }}{{ else }}{{ .Chart.Name }}{{ end }}
          image: {{ include "jira.image" . | quote }}
          imagePullPolicy: {{ .Values.image.pullPolicy }}
          env:
            {{ if .Values.ingress.https }}
            - name: ATL_TOMCAT_SCHEME
              value: "https"
            - name: ATL_TOMCAT_SECURE
              value: "true"
            {{ end }}
            {{ if .Values.jira.service.contextPath }}
            - name: ATL_TOMCAT_CONTEXTPATH
              value: {{ .Values.jira.service.contextPath | quote }}
            {{ end }}
            - name: ATL_TOMCAT_PORT
              value: {{ .Values.jira.ports.http | quote }}
            {{ if .Values.ingress.host }}
            - name: ATL_PROXY_NAME
              value: {{ .Values.ingress.host | quote }}
            - name: ATL_PROXY_PORT
              value: {{ include "jira.ingressPort" . | quote }}
            {{ end }}
            {{- include "jira.s3StorageEnvVars" . | nindent 12 }}
            {{- include "jira.databaseEnvVars" . | nindent 12 }}
            {{- include "jira.clusteringEnvVars" . | nindent 12 }}
            - name: SET_PERMISSIONS
              value: {{ .Values.jira.setPermissions | quote }}
            - name: JIRA_SHARED_HOME
              value: {{ .Values.volumes.sharedHome.mountPath | quote }}
            - name: JVM_SUPPORT_RECOMMENDED_ARGS
              valueFrom:
                configMapKeyRef:
                  key: additional_jvm_args
                  name: {{ include "common.names.fullname" . }}-jvm-config
            - name: JVM_MINIMUM_MEMORY
              valueFrom:
                configMapKeyRef:
                  key: min_heap
                  name: {{ include "common.names.fullname" . }}-jvm-config
            - name: JVM_MAXIMUM_MEMORY
              valueFrom:
                configMapKeyRef:
                  key: max_heap
                  name: {{ include "common.names.fullname" . }}-jvm-config
            - name: JVM_RESERVED_CODE_CACHE_SIZE
              valueFrom:
                configMapKeyRef:
                  key: reserved_code_cache
                  name: {{ include "common.names.fullname" . }}-jvm-config
            {{- include "jira.additionalEnvironmentVariables" . | nindent 12 }}
          ports:
            - name: http
              containerPort: {{ .Values.jira.ports.http }}
              protocol: TCP
            - name: ehcache
              containerPort: {{ .Values.jira.ports.ehcache }}
              protocol: TCP
            - name: ehcacheobject
              containerPort: {{ .Values.jira.ports.ehcacheobject }}
              protocol: TCP
            {{- include "jira.additionalPorts" . | nindent 12 }}
            {{- include "common.jmx.port" . | nindent 12 }}
          {{- if .Values.jira.readinessProbe.enabled }}
          readinessProbe:
            {{- if .Values.jira.readinessProbe.customProbe}}
            {{- with .Values.jira.readinessProbe.customProbe }}
            {{- toYaml . | nindent 12 }}
            {{- end }}
            {{- else }}
            httpGet:
              port: {{ .Values.jira.ports.http }}
              path: {{ .Values.jira.service.contextPath }}/status
            initialDelaySeconds: {{ .Values.jira.readinessProbe.initialDelaySeconds }}
            periodSeconds: {{ .Values.jira.readinessProbe.periodSeconds }}
            timeoutSeconds: {{ .Values.jira.readinessProbe.timeoutSeconds }}
            failureThreshold: {{ .Values.jira.readinessProbe.failureThreshold }}
            {{- end }}
          {{- end }}
          {{- if .Values.jira.startupProbe.enabled }}
          startupProbe:
            tcpSocket:
              port: {{ .Values.jira.ports.http }}
            initialDelaySeconds: {{ .Values.jira.startupProbe.initialDelaySeconds }}
            periodSeconds: {{ .Values.jira.startupProbe.periodSeconds }}
            failureThreshold: {{ .Values.jira.startupProbe.failureThreshold }}
          {{- end }}
          {{- if .Values.jira.livenessProbe.enabled }}
          livenessProbe:
            {{- if .Values.jira.livenessProbe.customProbe}}
            {{- with .Values.jira.livenessProbe.customProbe }}
            {{- toYaml . | nindent 12 }}
            {{- end }}
            {{- else }}
            tcpSocket:
              port: {{ .Values.jira.ports.http }}
            initialDelaySeconds: {{ .Values.jira.livenessProbe.initialDelaySeconds }}
            periodSeconds: {{ .Values.jira.livenessProbe.periodSeconds }}
            timeoutSeconds: {{ .Values.jira.livenessProbe.timeoutSeconds }}
            failureThreshold: {{ .Values.jira.livenessProbe.failureThreshold }}
          {{- end }}
          {{- end }}
          {{- with .Values.jira.containerSecurityContext}}
          securityContext:
          {{- toYaml . | nindent 12}}
          {{- end}}
          {{- with .Values.jira.resources.container }}
          resources:
            {{- toYaml . | nindent 12 }}
          {{- end }}
          volumeMounts:
            {{- include "jira.volumeMounts" . | nindent 12 }}
            {{- include "common.jmx.config.volumeMounts" . | nindent 12 }}
            {{- include "jira.additionalVolumeMounts" . | nindent 12 }}
            {{- include "jira.additionalLibraries" . | nindent 12 }}
            {{- include "jira.additionalBundledPlugins" . | nindent 12 }}
            {{- range $i, $n := .Values.additionalFiles }}
            - name: {{ .name }}-{{$i}}
              mountPath: {{ .mountPath }}/{{ .key }}
              subPath: {{ .key }}
            {{ end }}
            {{- range $i, $n := .Values.additionalConfigMaps }}
            {{- range .keys }}
            - name: {{ .fileName | replace "_" "-" | replace "." "-" }}
              mountPath: {{ .mountPath }}/{{ .fileName }}
              subPath: {{ .fileName }}
            {{ end }}
            {{- end }}
          lifecycle:
          {{- if .Values.jira.postStart.command }}
            postStart:
              exec:
                command: ["/bin/sh", "-c", {{- .Values.jira.postStart.command | quote }}]
          {{- end }}
            preStop:
              exec:
                command: ["sh", "-c", {{ .Values.jira.shutdown.command | quote }}]
        {{- include "fluentd.container" . | nindent 8 }}
        {{- include "jira.additionalContainers" . | nindent 8 }}
      {{- with .Values.nodeSelector }}
      nodeSelector:
      {{- toYaml . | nindent 8 }}
      {{- end }}
      {{- with .Values.affinity }}
      affinity:
      {{- toYaml . | nindent 8 }}
      {{- end }}
      {{- with .Values.tolerations }}
      tolerations:
      {{- toYaml . | nindent 8 }}
      {{- end }}
      {{- with .Values.jira.topologySpreadConstraints }}
      topologySpreadConstraints:
      {{- toYaml . | nindent 8 }}
      {{- end }}
      {{- if .Values.priorityClassName }}
      priorityClassName: {{ .Values.priorityClassName }}
      {{- end }}
      {{- if .Values.schedulerName }}
      schedulerName: {{ .Values.schedulerName  | quote }}
      {{- end }}
      volumes:
        {{- range $i, $n := .Values.additionalFiles }}
        - name: {{ .name }}-{{$i}}
          {{ .type }}:
            {{ if hasPrefix .type "secret" }}{{ .type}}Name{{ else }}name{{ end }}: {{ .name }}
            items:
              - key: {{ .key }}
                path: {{ .key }}
        {{ end }}
        {{- range $i, $key := .Values.additionalConfigMaps }}
        {{- with $ }}
        {{- range $key.keys }}
        - name: {{ .fileName | replace "_" "-" | replace "." "-" }}
          configMap:
            name: {{ include "common.names.fullname" $ }}-{{ $key.name }}
            {{- if .defaultMode }}
            defaultMode: {{ .defaultMode }}
            {{- end }}
            items:
              - key: {{ .fileName }}
                path: {{ .fileName }}
            {{- end }}
        {{ end }}
        {{- end }}
        {{ include "jira.volumes" . | nindent 8 }}
        {{ include "fluentd.config.volume" . | nindent 8 }}
        {{ include "common.jmx.config.volume" . | nindent 8 }}
  {{ include "jira.volumeClaimTemplates" . | nindent 2 }}
{{- end }}

{{/*
Define additional StatefulSets with unique labels, annotation, resources requests,
nodeSelector, tolerations and topologySpreadConstraints
*/}}
{{- define "jira.additional.statefulsets" -}}
{{- $stsName := include "common.names.fullname" . -}}
{{- range .Values.additionalStatefulSets }}
  {{- $context := merge . (dict "Parent" $) -}}

  {{/* We need to get parent context to have access to common chart within the additionalStatefulSets loop */}}
  {{- $mainSts := fromYaml (tpl (include "jira.base.statefulset" $context.Parent) $context.Parent) -}}

  {{- /* Replace metadata: name, labels and annotations for both StateFulset and Pod */ -}}
  {{- $_ := set $mainSts.metadata "name" (printf "%s-%s" $stsName .name) -}}

  {{- /* Override replicas */ -}}
  {{- $_ := set $mainSts.spec "replicas" .replicas -}}

  {{- /* Merge labels for StatefulSet */ -}}
  {{- $baseLabels := $mainSts.metadata.labels | default dict -}}
  {{- $additionalLabels := .additionalLabels.statefulSet | default dict -}}
  {{- $mergedLabels := merge $baseLabels $additionalLabels -}}
  {{- $_ := set $mainSts.metadata "labels" $mergedLabels -}}

  {{- /* Build Pod template labels, excluding app.kubernetes.io/instance key to make sure the default label selector does not discover pods */ -}}
  {{- $podLabels := include "common.labels.selectorLabels" $context.Parent | fromYaml -}}
  {{- if hasKey $podLabels "app.kubernetes.io/instance" }}
    {{- $_ := unset $podLabels "app.kubernetes.io/instance" -}}
  {{- end }}

  {{- /* Add the app.kubernetes.io/role label with the additional labels if any */ -}}
  {{- $roleLabel := dict "app.kubernetes.io/role" (printf "%s-%s" $stsName .name) -}}
  {{- $podLabels = merge $podLabels $roleLabel .additionalLabels.pod -}}

  {{- /* Set resulting labels to spec.template.metadata.labels */ -}}
  {{- $_ := set $mainSts.spec.template.metadata "labels" $podLabels -}}
  {{- $_ := set $mainSts.spec.selector "matchLabels" $podLabels -}}

  {{- /* Merge StatefulSet annotations */ -}}
  {{- $baseAnnotations := $mainSts.metadata.annotations | default dict -}}
  {{- $additionalAnnotations := .additionalAnnotations.statefulSet | default dict -}}
  {{- $mergedAnnotations := merge $baseAnnotations $additionalAnnotations -}}
  {{- $_ := set $mainSts.metadata "annotations" $mergedAnnotations -}}

  {{- /* Merge pod annotation */ -}}
  {{- $podAnnotations := $mainSts.spec.template.metadata.annotations | default dict -}}
  {{- $additionalPodAnnotations := .additionalAnnotations.pod | default dict -}}
  {{- $mergedPodAnnotations := merge $podAnnotations $additionalPodAnnotations -}}
  {{- $_ := set $mainSts.spec.template.metadata "annotations" $mergedPodAnnotations -}}

  {{- /* Patch resources requests/limits if defined */ -}}
  {{- if .resources }}
  {{- $container := index $mainSts.spec.template.spec.containers 0 -}}
  {{- $_ := set $container "resources" .resources -}}
  {{- end }}

  {{- /* Patch NodeSelector, Tolerations, Affinity and TopologySpreadConstraints if defined */ -}}
  {{- if .nodeSelector }}
  {{- $_ := set $mainSts.spec.template.spec "nodeSelector" .nodeSelector -}}
  {{- end }}
  {{- if .tolerations }}
  {{- $_ := set $mainSts.spec.template.spec "tolerations" .tolerations -}}
  {{- end }}
  {{- if .affinity }}
  {{- $_ := set $mainSts.spec.template.spec "affinity" .affinity -}}
  {{- end }}
  {{- if .topologySpreadConstraints }}
  {{- $_ := set $mainSts.spec.template.spec "topologySpreadConstraints" .topologySpreadConstraints -}}
  {{- end }}
  {{- toYaml $mainSts | nindent 0 }}
---
{{- end }}
{{- end }}


{{/* Define Jira service */}}
{{- define "jira.base.service" -}}
apiVersion: v1
kind: Service
metadata:
  name: {{ include "common.names.fullname" . }}
  labels:
    {{- include "common.labels.commonLabels" . | nindent 4 }}
  annotations:
    {{- with .Values.jira.service.annotations }}
    {{- toYaml . | nindent 4 }}
    {{- end }}
spec:
  type: {{ .Values.jira.service.type }}
  sessionAffinity: {{ .Values.jira.service.sessionAffinity }}
  {{- if .Values.jira.service.sessionAffinityConfig.clientIP.timeoutSeconds }}
  sessionAffinityConfig:
    clientIP:
      timeoutSeconds: {{ .Values.jira.service.sessionAffinityConfig.clientIP.timeoutSeconds }}
  {{- end }}
  {{- if and (eq .Values.jira.service.type "LoadBalancer") (not (empty .Values.jira.service.loadBalancerIP)) }}
  loadBalancerIP: {{ .Values.jira.service.loadBalancerIP }}
  {{- end }}
  ports:
    - port: {{ .Values.jira.service.port }}
      targetPort: http
      protocol: TCP
      name: http
  selector:
    {{- include "common.labels.selectorLabels" . | nindent 4 }}
{{- end }}

{{/* Define Jira service */}}
{{- define "jira.additional.services" -}}
{{- $svcName := include "common.names.fullname" . -}}
{{- range .Values.additionalStatefulSets }}
  {{- $context := merge . (dict "Parent" $) -}}
  {{/* We need to get parent context to have access to common chart within the additionalStatefulSets loop */}}
  {{- $mainSvc := fromYaml (tpl (include "jira.base.service" $context.Parent) $context.Parent) -}}
  {{- /* Replace metadata: name, labels and annotations for the service */ -}}
  {{- $_ := set $mainSvc.metadata "name" (printf "%s-%s" $svcName .name) -}}

  {{- $labels := include "common.labels.commonLabels" $context.Parent | fromYaml -}}
  {{- $mergedServiceLabels := merge $labels .service.additionalLabels -}}
  {{- $_ := set $mainSvc.metadata "labels" $mergedServiceLabels -}}


  {{- /* Merge Service annotations */ -}}
  {{- $baseAnnotations := $mainSvc.metadata.annotations | default dict -}}
  {{- $additionalAnnotations := .service.additionalAnnotations | default dict -}}
  {{- $mergedAnnotations := merge $baseAnnotations $additionalAnnotations -}}
  {{- $_ := set $mainSvc.metadata "annotations" $mergedAnnotations -}}


  {{- $selectorLabels := include "common.labels.selectorLabels" $context.Parent | fromYaml -}}
  {{- if hasKey $selectorLabels "app.kubernetes.io/instance" }}
    {{- $_ := unset $selectorLabels "app.kubernetes.io/instance" -}}
  {{- end }}

  {{- /* Add the app.kubernetes.io/role label with the additional labels if any */ -}}
  {{- $roleLabel := dict "app.kubernetes.io/role" (printf "%s-%s" $svcName .name) -}}
  {{- $selectorLabels = merge $selectorLabels $roleLabel .additionalLabels.pod -}}

  {{- /* Set resulting labels to spec.template.metadata.labels */ -}}
  {{- $_ := set $mainSvc.spec "selector" $selectorLabels -}}

  {{- if .service.type }}
    {{- $_ := set $mainSvc.spec "type" .service.type -}}
  {{- end }}


  {{- toYaml $mainSvc | nindent 0 }}
{{- end }}
{{- end }}
