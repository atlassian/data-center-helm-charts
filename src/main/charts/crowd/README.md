# crowd

![Version: 1.16.6](https://img.shields.io/badge/Version-1.16.6-informational?style=flat-square) ![Type: application](https://img.shields.io/badge/Type-application-informational?style=flat-square) ![AppVersion: 5.2.1](https://img.shields.io/badge/AppVersion-5.2.1-informational?style=flat-square)

A chart for installing Crowd Data Center on Kubernetes

**Homepage:** <https://atlassian.github.io/data-center-helm-charts/>

## Source Code

* <https://github.com/atlassian/data-center-helm-charts>
* <https://bitbucket.org/atlassian-docker/docker-atlassian-crowd/>

## Requirements

Kubernetes: `>=1.21.x-0`

| Repository | Name | Version |
|------------|------|---------|
| https://atlassian.github.io/data-center-helm-charts | common | 1.2.5 |

## Values

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| additionalConfigMaps | list | `[]` | Create additional ConfigMaps with given names, keys and content. Ther Helm release name will be used as a prefix for a ConfigMap name, fileName is used as subPath  |
| additionalContainers | list | `[]` | Additional container definitions that will be added to all Crowd pods  |
| additionalFiles | list | `[]` | Additional existing ConfigMaps and Secrets not managed by Helm that should be mounted into service container. Configuration details below (camelCase is important!): 'name'      - References existing ConfigMap or secret name. 'type'      - 'configMap' or 'secret' 'key'       - The file name. 'mountPath' - The destination directory in a container. VolumeMount and Volumes are added with this name and index position, for example; custom-config-0, keystore-2  |
| additionalHosts | list | `[]` | Additional host aliases for each pod, equivalent to adding them to the /etc/hosts file. https://kubernetes.io/docs/concepts/services-networking/add-entries-to-pod-etc-hosts-with-host-aliases/ |
| additionalInitContainers | list | `[]` | Additional initContainer definitions that will be added to all Crowd pods  |
| additionalLabels | object | `{}` | Additional labels that should be applied to all resources  |
| affinity | object | `{}` | Standard K8s affinities that will be applied to all Crowd pods  |
| crowd.accessLog.enabled | bool | `true` | Set to 'true' if access logging should be enabled.  |
| crowd.accessLog.localHomeSubPath | string | `"logs"` | The subdirectory within the local-home volume where access logs should be stored.  |
| crowd.accessLog.mountPath | string | `"/opt/atlassian/crowd/apache-tomcat/logs"` | The path within the Crowd container where the local-home volume should be mounted in order to capture access logs.  |
| crowd.additionalBundledPlugins | list | `[]` | Specifies a list of additional Crowd plugins that should be added to the Crowd container. Note plugins installed via this method will appear as bundled plugins rather than user plugins. These should be specified in the same manner as the 'additionalLibraries' property. Additional details: https://atlassian.github.io/data-center-helm-charts/examples/external_libraries/EXTERNAL_LIBS/  NOTE: only .jar files can be loaded using this approach. OBR's can be extracted (unzipped) to access the associated .jar  An alternative to this method is to install the plugins via "Manage Apps" in the product system administration UI.  |
| crowd.additionalCertificates | object | `{"customCmd":null,"secretName":null}` | Certificates to be added to Java truststore. Provide reference to a secret that contains the certificates  |
| crowd.additionalEnvironmentVariables | list | `[]` | Defines any additional environment variables to be passed to the Crowd container. See https://hub.docker.com/r/atlassian/crowd for supported variables. |
| crowd.additionalJvmArgs | list | `[]` | Specifies a list of additional arguments that can be passed to the Crowd JVM, e.g. system properties.  |
| crowd.additionalLibraries | list | `[]` | Specifies a list of additional Java libraries that should be added to the Crowd container. Each item in the list should specify the name of the volume that contains the library, as well as the name of the library file within that volume's root directory. Optionally, a subDirectory field can be included to specify which directory in the volume contains the library file. Additional details: https://atlassian.github.io/data-center-helm-charts/examples/external_libraries/EXTERNAL_LIBS/  |
| crowd.additionalPorts | list | `[]` | Defines any additional ports for the Crowd container.  |
| crowd.additionalVolumeClaimTemplates | list | `[]` | Defines additional volumeClaimTemplates that should be applied to the Crowd pod. Note that this will not create any corresponding volume mounts; those needs to be defined in crowd.additionalVolumeMounts  |
| crowd.additionalVolumeMounts | list | `[]` | Defines any additional volumes mounts for the Crowd container. These can refer to existing volumes, or new volumes can be defined in volumes.additional. |
| crowd.containerSecurityContext | object | `{}` | Standard K8s field that holds security configurations that will be applied to a container. https://kubernetes.io/docs/tasks/configure-pod-container/security-context/  |
| crowd.livenessProbe.enabled | bool | `false` | Whether to apply the livenessProbe check to pod.  |
| crowd.livenessProbe.failureThreshold | int | `12` | The number of consecutive failures of the Crowd container liveness probe before the pod fails liveness checks.  |
| crowd.livenessProbe.initialDelaySeconds | int | `60` | Time to wait before starting the first probe  |
| crowd.livenessProbe.periodSeconds | int | `5` | How often (in seconds) the Crowd container liveness probe will run  |
| crowd.livenessProbe.timeoutSeconds | int | `1` | Number of seconds after which the probe times out  |
| crowd.ports.http | int | `8095` | The port on which the Crowd container listens for HTTP traffic  |
| crowd.readinessProbe.customProbe | object | `{}` | Custom readinessProbe to override the default /status httpGet  |
| crowd.readinessProbe.enabled | bool | `true` | Whether to apply the readinessProbe check to pod.  |
| crowd.readinessProbe.failureThreshold | int | `10` | The number of consecutive failures of the Crowd container readiness probe before the pod fails readiness checks.  |
| crowd.readinessProbe.initialDelaySeconds | int | `10` | The initial delay (in seconds) for the Crowd container readiness probe, after which the probe will start running.  |
| crowd.readinessProbe.periodSeconds | int | `5` | How often (in seconds) the Crowd container readiness probe will run  |
| crowd.readinessProbe.timeoutSeconds | int | `1` | Number of seconds after which the probe times out  |
| crowd.resources.container.requests.cpu | string | `"2"` | Initial CPU request by Crowd pod  |
| crowd.resources.container.requests.memory | string | `"1G"` | Initial Memory request by Crowd pod  |
| crowd.resources.jvm.maxHeap | string | `"768m"` | The maximum amount of heap memory that will be used by the Crowd JVM  |
| crowd.resources.jvm.minHeap | string | `"384m"` | The minimum amount of heap memory that will be used by the Crowd JVM  |
| crowd.securityContext.fsGroup | int | `2004` | The GID used by the Crowd docker image GID will default to 2004 if not supplied and securityContextEnabled is set to true. This is intended to ensure that the shared-home volume is group-writeable by the GID used by the Crowd container. However, this doesn't appear to work for NFS volumes due to a K8s bug: https://github.com/kubernetes/examples/issues/260  |
| crowd.securityContextEnabled | bool | `true` | Whether to apply security context to pod.  |
| crowd.service.annotations | object | `{}` | Additional annotations to apply to the Service  |
| crowd.service.loadBalancerIP | string | `nil` | Use specific loadBalancerIP. Only applies to service type LoadBalancer.  |
| crowd.service.port | int | `80` | The port on which the Crowd K8s Service will listen  |
| crowd.service.sessionAffinity | string | `"None"` | Session affinity type. If you want to make sure that connections from a particular client are passed to the same pod each time, set sessionAffinity to ClientIP. See: https://kubernetes.io/docs/reference/networking/virtual-ips/#session-affinity  |
| crowd.service.sessionAffinityConfig | object | `{"clientIP":{"timeoutSeconds":null}}` | Session affinity configuration  |
| crowd.service.sessionAffinityConfig.clientIP.timeoutSeconds | string | `nil` | Specifies the seconds of ClientIP type session sticky time. The value must be > 0 && <= 86400(for 1 day) if ServiceAffinity == "ClientIP". Default value is 10800 (for 3 hours).  |
| crowd.service.type | string | `"ClusterIP"` | The type of K8s service to use for Crowd. For loadBalancer type, deselect the consistent client IP address in Crowd Session configuration. Read more: https://atlassian.github.io/data-center-helm-charts/troubleshooting/LIMITATIONS/#loadbalancer-service-type  |
| crowd.setPermissions | bool | `true` | Boolean to define whether to set local home directory permissions on startup of Crowd container. Set to 'false' to disable this behaviour.  |
| crowd.shutdown.command | string | `"/shutdown-wait.sh"` | By default pods will be stopped via a [preStop hook](https://kubernetes.io/docs/concepts/containers/container-lifecycle-hooks/), using a script supplied by the Docker image. If any other shutdown behaviour is needed it can be achieved by overriding this value. Note that the shutdown command needs to wait for the application shutdown completely before exiting; see [the default command](https://bitbucket.org/atlassian-docker/docker-atlassian-crowd/src/master/shutdown-wait.sh) for details.  |
| crowd.shutdown.terminationGracePeriodSeconds | int | `30` | The termination grace period for pods during shutdown. This should be set to the internal grace period, plus a small buffer to allow the JVM to fully terminate.  |
| crowd.startupProbe.enabled | bool | `false` | Whether to apply the startupProbe check to pod.  |
| crowd.startupProbe.failureThreshold | int | `120` | The number of consecutive failures of the Crowd container startup probe before the pod fails startup checks.  |
| crowd.startupProbe.initialDelaySeconds | int | `60` | Time to wait before starting the first probe  |
| crowd.startupProbe.periodSeconds | int | `5` | How often (in seconds) the Crowd container startup probe will run  |
| crowd.topologySpreadConstraints | list | `[]` | Defines topology spread constraints for Crowd pods. See details: https://kubernetes.io/docs/concepts/workloads/pods/pod-topology-spread-constraints/  |
| crowd.umask | string | `"0022"` | The umask used by the Crowd process when it creates new files. The default is 0022. This gives the new files:  - read/write permissions for the Crowd user  - read permissions for everyone else.  |
| crowd.useHelmReleaseNameAsContainerName | bool | `false` | Whether the main container should acquire helm release name. By default the container name is `crowd` which corresponds to the name of the Helm Chart.  |
| fluentd.command | string | `nil` | The command used to start Fluentd. If not supplied the default command will be used: "fluentd -c /fluentd/etc/fluent.conf -v"  Note: The custom command can be free-form, however pay particular attention to the process that should ultimately be left running in the container. This process should be invoked with 'exec' so that signals are appropriately propagated to it, for instance SIGTERM. An example of how such a command may look is: "<command 1> && <command 2> && exec <primary command>" |
| fluentd.customConfigFile | bool | `false` | Set to 'true' if a custom config (see 'configmap-fluentd.yaml' for default) should be used for Fluentd. If enabled this config must be supplied via the 'fluentdCustomConfig' property below.  |
| fluentd.elasticsearch.enabled | bool | `true` | Set to 'true' if Fluentd should send all log events to an Elasticsearch service.  |
| fluentd.elasticsearch.extraVolumes | list | `[]` | Specify custom volumes to be added to Fluentd container (e.g. more log sources)  |
| fluentd.elasticsearch.hostname | string | `"elasticsearch"` | The hostname of the Elasticsearch service that Fluentd should send logs to.  |
| fluentd.elasticsearch.indexNamePrefix | string | `"crowd"` | The prefix of the Elasticsearch index name that will be used  |
| fluentd.enabled | bool | `false` | Set to 'true' if the Fluentd sidecar (DaemonSet) should be added to each pod  |
| fluentd.fluentdCustomConfig | object | `{}` | Custom fluent.conf file  |
| fluentd.httpPort | int | `9880` | The port on which the Fluentd sidecar will listen  |
| fluentd.imageRepo | string | `"fluent/fluentd-kubernetes-daemonset"` | The Fluentd sidecar image repository  |
| fluentd.imageTag | string | `"v1.11.5-debian-elasticsearch7-1.2"` | The Fluentd sidecar image tag  |
| fluentd.resources | object | `{}` | Resources requests and limits for fluentd sidecar container See: https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/  |
| image | object | `{"pullPolicy":"IfNotPresent","repository":"atlassian/crowd","tag":""}` | Image configuration  |
| image.pullPolicy | string | `"IfNotPresent"` | Image pull policy  |
| image.repository | string | `"atlassian/crowd"` | The Docker Crowd Docker image to use https://hub.docker.com/r/atlassian/crowd  |
| image.tag | string | `""` | The docker image tag to be used. Defaults to appVersion in Chart.yaml  |
| ingress.annotations | object | `{}` | The custom annotations that should be applied to the Ingress Resource. If using an ingress-nginx controller be sure that the annotations you add here are compatible with those already defined in the 'ingess.yaml' template  |
| ingress.className | string | `"nginx"` | The class name used by the ingress controller if it's being used.  Please follow documentation of your ingress controller. If the cluster contains multiple ingress controllers, this setting allows you to control which of them is used for Atlassian application traffic.  |
| ingress.create | bool | `false` | Set to 'true' if an Ingress Resource should be created. This depends on a pre-provisioned Ingress Controller being available.  |
| ingress.host | string | `nil` | The fully-qualified hostname (FQDN) of the Ingress Resource. Traffic coming in on this hostname will be routed by the Ingress Resource to the appropriate backend Service.  |
| ingress.https | bool | `true` | Set to 'true' if browser communication with the application should be TLS (HTTPS) enforced.  |
| ingress.maxBodySize | string | `"250m"` | The max body size to allow. Requests exceeding this size will result in an HTTP 413 error being returned to the client.  |
| ingress.nginx | bool | `true` | Set to 'true' if the Ingress Resource is to use the K8s 'ingress-nginx' controller. https://kubernetes.github.io/ingress-nginx/  This will populate the Ingress Resource with annotations that are specific to the K8s ingress-nginx controller. Set to 'false' if a different controller is to be used, in which case the appropriate annotations for that controller must be specified below under 'ingress.annotations'.  |
| ingress.path | string | `"/"` | The base path for the Ingress Resource. For example '/crowd'. Based on a 'ingress.host' value of 'company.k8s.com' this would result in a URL of 'company.k8s.com/crowd'. Due to temporary limitations with changing Crowd context on the application level, only "/" and "/crowd" paths are supported.  |
| ingress.proxyConnectTimeout | int | `60` | Defines a timeout for establishing a connection with a proxied server. It should be noted that this timeout cannot usually exceed 75 seconds.  |
| ingress.proxyReadTimeout | int | `60` | Defines a timeout for reading a response from the proxied server. The timeout is set only between two successive read operations, not for the transmission of the whole response. If the proxied server does not transmit anything within this time, the connection is closed.  |
| ingress.proxySendTimeout | int | `60` | Sets a timeout for transmitting a request to the proxied server. The timeout is set only between two successive write operations, not for the transmission of the whole request. If the proxied server does not receive anything within this time, the connection is closed.  |
| ingress.tlsSecretName | string | `nil` | The name of the K8s Secret that contains the TLS private key and corresponding certificate. When utilised, TLS termination occurs at the ingress point where traffic to the Service, and it's Pods is in plaintext.  Usage is optional and depends on your use case. The Ingress Controller itself can also be configured with a TLS secret for all Ingress Resources. https://kubernetes.io/docs/concepts/configuration/secret/#tls-secrets https://kubernetes.io/docs/concepts/services-networking/ingress/#tls  |
| monitoring.exposeJmxMetrics | bool | `false` | Expose JMX metrics with jmx_exporter https://github.com/prometheus/jmx_exporter  |
| monitoring.fetchJmxExporterJar | bool | `true` | Fetch jmx_exporter jar from the image. If set to false make sure to manually copy the jar to shared home and provide an absolute path in jmxExporterCustomJarLocation  |
| monitoring.grafana.createDashboards | bool | `false` | Create ConfigMaps with Grafana dashboards  |
| monitoring.grafana.dashboardAnnotations | object | `{}` | Annotations added to Grafana dashboards ConfigMaps. See: https://github.com/kiwigrid/k8s-sidecar#usage  |
| monitoring.grafana.dashboardLabels | object | `{}` | Label selector for Grafana dashboard importer sidecar  |
| monitoring.jmxExporterCustomConfig | object | `{}` | Custom JMX config with the rules  |
| monitoring.jmxExporterCustomJarLocation | string | `nil` | Location of jmx_exporter jar file if mounted from a secret or manually copied to shared home  |
| monitoring.jmxExporterImageRepo | string | `"bitnami/jmx-exporter"` | Image repository with jmx_exporter jar  |
| monitoring.jmxExporterImageTag | string | `"0.18.0"` | Image tag to be used to pull jmxExporterImageRepo  |
| monitoring.jmxExporterInitContainer | object | `{"customSecurityContext":{},"resources":{},"runAsRoot":true}` | JMX exporter init container configuration  |
| monitoring.jmxExporterInitContainer.customSecurityContext | object | `{}` | Custom SecurityContext for the jmx exporter init container  |
| monitoring.jmxExporterInitContainer.resources | object | `{}` | Resources requests and limits for the JMX exporter init container See: https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/  |
| monitoring.jmxExporterInitContainer.runAsRoot | bool | `true` | Whether to run JMX exporter init container as root to copy JMX exporter binary to shared home volume. Set to false if running containers as root is not allowed in the cluster.  |
| monitoring.jmxExporterPort | int | `9999` | Port number on which metrics will be available  |
| monitoring.jmxExporterPortType | string | `"ClusterIP"` | JMX exporter port type  |
| monitoring.jmxServiceAnnotations | object | `{}` | Annotations added to the jmx service  |
| monitoring.serviceMonitor.create | bool | `false` | Create ServiceMonitor to start scraping metrics. ServiceMonitor CRD needs to be created in advance.  |
| monitoring.serviceMonitor.prometheusLabelSelector | object | `{}` | ServiceMonitorSelector of the prometheus instance.  |
| monitoring.serviceMonitor.scrapeIntervalSeconds | int | `30` | Scrape interval for the JMX service.  |
| nodeSelector | object | `{}` | Standard K8s node-selectors that will be applied to all Crowd pods  |
| podAnnotations | object | `{}` | Custom annotations that will be applied to all Crowd pods  |
| podDisruptionBudget | object | `{"annotations":{},"enabled":false,"labels":{},"maxUnavailable":null,"minAvailable":null}` | PodDisruptionBudget: https://kubernetes.io/docs/tasks/run-application/configure-pdb/ You can specify only one of maxUnavailable and minAvailable in a single PodDisruptionBudget. When both minAvailable and maxUnavailable are set, maxUnavailable takes precedence.  |
| podLabels | object | `{}` | Custom labels that will be applied to all Crowd pods  |
| priorityClassName | string | `nil` | Priority class for the application pods. The PriorityClass with this name needs to be available in the cluster. For details see https://kubernetes.io/docs/concepts/scheduling-eviction/pod-priority-preemption/#priorityclass  |
| replicaCount | int | `1` | The initial number of Crowd pods that should be started at deployment time. Note that Crowd requires manual configuration via the browser post deployment after the first pod is deployed. This configuration must be completed before scaling up additional pods. As such this value should always be kept as 1, but can be altered once manual configuration is complete.  |
| schedulerName | string | `nil` | Standard K8s schedulerName that will be applied to all Crowd pods. Check Kubernetes documentation on how to configure multiple schedulers: https://kubernetes.io/docs/tasks/extend-kubernetes/configure-multiple-schedulers/#specify-schedulers-for-pods  |
| serviceAccount.annotations | object | `{}` | Annotations to add to the ServiceAccount (if created)  |
| serviceAccount.create | bool | `true` | Set to 'true' if a ServiceAccount should be created, or 'false' if it already exists.  |
| serviceAccount.imagePullSecrets | list | `[]` | For Docker images hosted in private registries, define the list of image pull secrets that should be utilized by the created ServiceAccount https://kubernetes.io/docs/concepts/containers/images/#specifying-imagepullsecrets-on-a-pod  |
| serviceAccount.name | string | `nil` | The name of the ServiceAccount to be used by the pods. If not specified, but the "serviceAccount.create" flag is set to 'true', then the ServiceAccount name will be auto-generated, otherwise the 'default' ServiceAccount will be used. https://kubernetes.io/docs/tasks/configure-pod-container/configure-service-account/#use-the-default-service-account-to-access-the-api-server  |
| terminationGracePeriodSeconds | int | `30` | Kubernetes default, but can be overridden here. |
| tolerations | list | `[]` | Standard K8s tolerations that will be applied to all Crowd pods  |
| volumes.additional | list | `[]` | Defines additional volumes that should be applied to all Crowd pods. Note that this will not create any corresponding volume mounts; those needs to be defined in crowd.additionalVolumeMounts  |
| volumes.localHome.customVolume | object | `{}` | Static provisioning of local-home using K8s PVs and PVCs  NOTE: Due to the ephemeral nature of pods this approach to provisioning volumes for pods is not recommended. Dynamic provisioning described above is the prescribed approach.  When 'persistentVolumeClaim.create' is 'false', then this value can be used to define a standard K8s volume that will be used for the local-home volume(s). If not defined, then an 'emptyDir' volume is utilised. Having provisioned a 'PersistentVolume', specify the bound 'persistentVolumeClaim.claimName' for the 'customVolume' object. https://kubernetes.io/docs/concepts/storage/persistent-volumes/#static  |
| volumes.localHome.mountPath | string | `"/var/atlassian/application-data/crowd"` | Specifies the path in the Crowd container to which the local-home volume will be mounted.  |
| volumes.localHome.persistentVolumeClaim.create | bool | `false` | If 'true', then a 'PersistentVolume' and 'PersistentVolumeClaim' will be dynamically created for each pod based on the 'StorageClassName' supplied below.  |
| volumes.localHome.persistentVolumeClaim.resources | object | `{"requests":{"storage":"1Gi"}}` | Specifies the standard K8s resource requests and/or limits for the local-home volume claims.  |
| volumes.localHome.persistentVolumeClaim.storageClassName | string | `nil` | Specify the name of the 'StorageClass' that should be used for the local-home volume claim.  |
| volumes.sharedHome.customVolume | object | `{}` | Static provisioning of shared-home using K8s PVs and PVCs  When 'persistentVolumeClaim.create' is 'false', then this value can be used to define a standard K8s volume that will be used for the shared-home volume. If not defined, then an 'emptyDir' volume is utilised. Having provisioned a 'PersistentVolume', specify the bound 'persistentVolumeClaim.claimName' for the 'customVolume' object. https://kubernetes.io/docs/concepts/storage/persistent-volumes/#static https://atlassian.github.io/data-center-helm-charts/examples/storage/aws/SHARED_STORAGE/  |
| volumes.sharedHome.mountPath | string | `"/var/atlassian/application-data/crowd/shared"` | Specifies the path in the Crowd container to which the shared-home volume will be mounted.  |
| volumes.sharedHome.nfsPermissionFixer.command | string | `nil` | By default, the fixer will change the group ownership of the volume's root directory to match the Crowd container's GID (2002), and then ensures the directory is group-writeable. If this is not the desired behaviour, command used can be specified here.  |
| volumes.sharedHome.nfsPermissionFixer.enabled | bool | `true` | If 'true', this will alter the shared-home volume's root directory so that Crowd can write to it. This is a workaround for a K8s bug affecting NFS volumes: https://github.com/kubernetes/examples/issues/260  |
| volumes.sharedHome.nfsPermissionFixer.imageRepo | string | `"alpine"` | Image repository for the permission fixer init container. Defaults to alpine  |
| volumes.sharedHome.nfsPermissionFixer.imageTag | string | `"latest"` | Image tag for the permission fixer init container. Defaults to latest  |
| volumes.sharedHome.nfsPermissionFixer.mountPath | string | `"/shared-home"` | The path in the K8s initContainer where the shared-home volume will be mounted  |
| volumes.sharedHome.nfsPermissionFixer.resources | object | `{}` | Resources requests and limits for nfsPermissionFixer init container See: https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/  |
| volumes.sharedHome.persistentVolumeClaim.create | bool | `false` | If 'true', then a 'PersistentVolumeClaim' and 'PersistentVolume' will be dynamically created for shared-home based on the 'StorageClassName' supplied below.  |
| volumes.sharedHome.persistentVolumeClaim.resources | object | `{"requests":{"storage":"1Gi"}}` | Specifies the standard K8s resource requests and/or limits for the shared-home volume claims.  |
| volumes.sharedHome.persistentVolumeClaim.storageClassName | string | `nil` | Specify the name of the 'StorageClass' that should be used for the 'shared-home' volume claim.  |
| volumes.sharedHome.subPath | string | `nil` | Specifies the sub-directory of the shared-home volume that will be mounted in to the Crowd container.  |

----------------------------------------------
Autogenerated from chart metadata using [helm-docs v1.11.0](https://github.com/norwoodj/helm-docs/releases/v1.11.0)
