{{- define "fluentd.container" -}}
{{ if .Values.fluentd.enabled }}
- name: fluentd
  image: {{ .Values.fluentd.imageName }}
  command: ["fluentd", "-c", "/fluentd/etc/fluent.conf", "-v"]
  volumeMounts:
    - name: local-home
      mountPath: /application-data/logs
      subPath: log
      readOnly: true
    - name: fluentd-config
      mountPath: /fluentd/etc
      readOnly: true
  env:
    - name: POD_NAME
      valueFrom:
        fieldRef:
          fieldPath: metadata.name
    - name: POD_NAMESPACE
      valueFrom:
        fieldRef:
          fieldPath: metadata.namespace
    - name: POD_IP
      valueFrom:
        fieldRef:
          fieldPath: status.podIP
    - name: HELM_RELEASE_NAME
      value: {{ include "bitbucket.fullname" . }}
{{ end }}
{{ end }}

{{- define "fluentd.config.volume" }}
{{ if .Values.fluentd.enabled }}
- name: fluentd-config
  configMap:
    name: {{ include "bitbucket.fullname" . }}-fluentd-config
{{ end }}
{{ end }}