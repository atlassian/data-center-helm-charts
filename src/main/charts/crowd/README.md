# crowd

![Version: 0.9.0](https://img.shields.io/badge/Version-0.9.0-informational?style=flat-square) ![Type: application](https://img.shields.io/badge/Type-application-informational?style=flat-square) ![AppVersion: 4.3.0-jdk11](https://img.shields.io/badge/AppVersion-4.3.0--jdk11-informational?style=flat-square)

A chart for installing Crowd Data Center on Kubernetes

**Homepage:** <https://www.atlassian.com/software/crowd>

## Source Code

* <https://github.com/atlassian-labs/data-center-helm-charts>
* <https://bitbucket.org/atlassian-docker/docker-atlassian-crowd/>

## Requirements

Kubernetes: `>=1.17.x-0`

## Values

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| additionalContainers | list | `[]` | Additional container definitions that will be added to all Crowd pods |
| additionalFiles | list | `[]` | Additional existing ConfigMaps and Secrets not managed by Helm that should be mounted into server container configMap and secret are two available types (camelCase is important!) mountPath is a destination directory in a container and key is file name name references existing ConfigMap or secret name. VolumeMount and Volumes are added with this name + index position, for example custom-config-0, keystore-2 |
| additionalInitContainers | list | `[]` | Additional initContainer definitions that will be added to all Crowd pods |
| additionalLabels | object | `{}` | Additional labels that should be applied to all resources |
| affinity | object | `{}` | Standard Kubernetes affinities that will be applied to all Crowd pods |
| crowd.accessLog.enabled | bool | `true` | True if access logging should be enabled. |
| crowd.accessLog.localHomeSubPath | string | `"logs"` | The subdirectory within the local-home volume where access logs should be stored. |
| crowd.accessLog.mountPath | string | `"/opt/atlassian/crowd/logs"` | The path within the Crowd container where the local-home volume should be mounted in order to capture access logs. |
| crowd.additionalBundledPlugins | list | `[]` | Specifies a list of additional Crowd plugins that should be added to the Crowd container. These are specified in the same manner as the additionalLibraries field, but the files will be loaded as bundled plugins rather than as libraries. |
| crowd.additionalEnvironmentVariables | list | `[]` | Defines any additional environment variables to be passed to the Crowd container. See https://hub.docker.com/r/atlassian/crowd for supported variables. |
| crowd.additionalJvmArgs | list | `["-XX:ActiveProcessorCount=2"]` | Specifies a list of additional arguments that can be passed to the Crowd JVM, e.g. system properties |
| crowd.additionalJvmArgs[0] | string | `"-XX:ActiveProcessorCount=2"` | The value defined for ActiveProcessorCount should correspond to that provided for 'container.requests.cpu' see: https://docs.oracle.com/en/java/javase/11/tools/java.html#GUID-3B1CE181-CD30-4178-9602-230B800D4FAE |
| crowd.additionalLibraries | list | `[]` | Specifies a list of additional Java libraries that should be added to the Crowd container. Each item in the list should specify the name of the volume which contain the library, as well as the name of the library file within that volume's root directory. Optionally, a subDirectory field can be included to specify which directory in the volume contains the library file. |
| crowd.additionalVolumeMounts | list | `[]` | Defines any additional volumes mounts for the Crowd container. These can refer to existing volumes, or new volumes can be defined in volumes.additional. |
| crowd.clustering.enabled | bool | `false` | Set to true if Data Center clustering should be enabled This will automatically configure cluster peer discovery between cluster nodes. |
| crowd.clustering.usePodNameAsClusterNodeName | bool | `true` | Set to true if the Kubernetes pod name should be used as the end-user-visible name of the Data Center cluster node. |
| crowd.ports.hazelcast | int | `5701` | The port on which the Crowd container listens for Hazelcast traffic |
| crowd.ports.http | int | `8095` | The port on which the Crowd container listens for HTTP traffic |
| crowd.readinessProbe.failureThreshold | int | `30` | The number of consecutive failures of the Crowd container readiness probe before the pod fails readiness checks |
| crowd.readinessProbe.initialDelaySeconds | int | `10` | The initial delay (in seconds) for the Crowd container readiness probe, after which the probe will start running |
| crowd.readinessProbe.periodSeconds | int | `5` | How often (in seconds) the Crowd container readiness robe will run |
| crowd.resources.container | object | `{"requests":{"cpu":"2","memory":"1G"}}` | Specifies the standard Kubernetes resource requests and/or limits for the Crowd container. It is important that if the memory resources are specified here, they must allow for the size of the Crowd JVM. That means the maximum heap size, the reserved code cache size, plus other JVM overheads, must be accommodated. Allowing for maxHeap * 1.5 would be an example. |
| crowd.resources.jvm.maxHeap | string | `"768m"` | JVM memory arguments below are based on the defaults defined for the Crowd docker container, see: https://bitbucket.org/atlassian-docker/docker-atlassian-crowd/src/master/ -- The maximum amount of heap memory that will be used by the Crowd JVM |
| crowd.resources.jvm.minHeap | string | `"384m"` | The minimum amount of heap memory that will be used by the Crowd JVM |
| crowd.securityContext | object | `{"enabled":true,"gid":"2004"}` | Enable or disable security context in StatefulSet template spec. Enabled by default with UID 2002. -- Disable when deploying to OpenShift, unless anyuid policy is attached to a service account |
| crowd.securityContext.gid | string | `"2004"` | The GID used by the Crowd docker image |
| crowd.service.port | int | `80` | The port on which the Crowd Kubernetes service will listen |
| crowd.service.type | string | `"ClusterIP"` | The type of Kubernetes service to use for Crowd |
| crowd.umask | string | `"0022"` | The umask used by the Crowd process when it creates new files. Default is 0022, which makes the new files read/writeable by the Crowd user, and readable by everyone else. |
| fluentd.elasticsearch.enabled | bool | `true` | True if fluentd should send all log events to an elasticsearch service. |
| fluentd.elasticsearch.hostname | string | `"elasticsearch"` | The hostname of the Elasticsearch service that fluentd should send logs to. |
| fluentd.elasticsearch.indexNamePrefix | string | `"crowd"` | The prefix of the elasticsearch index name that will be used |
| fluentd.enabled | bool | `false` | True if the fluentd sidecar should be added to each pod |
| fluentd.httpPort | int | `9880` | The port on which the fluentd sidecar will listen |
| fluentd.imageName | string | `"fluent/fluentd-kubernetes-daemonset:v1.11.5-debian-elasticsearch7-1.2"` | The name of the image containing the fluentd sidecar |
| image.pullPolicy | string | `"IfNotPresent"` |  |
| image.repository | string | `"atlassian/crowd"` |  |
| image.tag | string | `"4.2.2"` | The docker image tag to be used. Defaults to the Chart appVersion. |
| ingress.annotations | object | `{}` | The custom annotations that should be applied to the Ingress Resource when not using the Kubernetes ingress-nginx controller. |
| ingress.create | bool | `false` | True if an Ingress Resource should be created. |
| ingress.host | string | `nil` | The fully-qualified hostname of the Ingress Resource. |
| ingress.https | bool | `true` | True if the browser communicates with the application over HTTPS. |
| ingress.maxBodySize | string | `"250m"` | The max body size to allow. Requests exceeding this size will result in an 413 error being returned to the client. https://kubernetes.github.io/ingress-nginx/user-guide/nginx-configuration/annotations/#custom-max-body-size |
| ingress.nginx | bool | `true` | True if the created Ingress Resource is to use the Kubernetes ingress-nginx controller: https://kubernetes.github.io/ingress-nginx/ This will populate the Ingress Resource with annotations for the Kubernetes ingress-nginx controller. Set to false if a different controller is to be used, in which case the appropriate annotations for that controller need to be specified. |
| ingress.path | string | `"/"` | The base path for the ingress rule. |
| ingress.tlsSecretName | string | `nil` | Secret that contains a TLS private key and certificate. Optional if Ingress Controller is configured to use one secret for all ingresses |
| nodeSelector | object | `{}` | Standard Kubernetes node-selectors that will be applied to all Crowd pods |
| podAnnotations | object | `{}` | Specify additional annotations to be added to all Crowd pods |
| replicaCount | int | `1` | The initial number of pods that should be started at deployment of each of Crowd. Note that because Crowd requires initial manual configuration after the first pod is deployed, and before scaling up to additional pods, this should always be kept as 1. |
| serviceAccount.clusterRole.create | bool | `true` | true if a ClusterRole should be created, or false if it already exists |
| serviceAccount.clusterRole.name | string | `nil` | Specifies the name of the ClusterRole that will be created if the "serviceAccount.clusterRole.create" flag is set. If not specified, a name will be auto-generated. |
| serviceAccount.clusterRoleBinding.create | bool | `true` | true if a ClusterRoleBinding should be created, or false if it already exists |
| serviceAccount.clusterRoleBinding.name | string | `nil` | Specifies the name of the ClusterRoleBinding that will be created if the "serviceAccount.clusterRoleBinding.create" flag is set If not specified, a name will be auto-generated. |
| serviceAccount.create | bool | `true` | true if a ServiceAccount should be created, or false if it already exists |
| serviceAccount.imagePullSecrets | list | `[]` | The list of image pull secrets that should be added to the created ServiceAccount |
| serviceAccount.name | string | `nil` | Specifies the name of the ServiceAccount to be used by the pods. If not specified, but the the "serviceAccount.create" flag is set, then the ServiceAccount name will be auto-generated, otherwise the 'default' ServiceAccount will be used. |
| tolerations | list | `[]` | Standard Kubernetes tolerations that will be applied to all Crowd pods |
| volumes.additional | list | `[]` | Defines additional volumes that should be applied to all Crowd pods. Note that this will not create any corresponding volume mounts; those needs to be defined in crowd.additionalVolumeMounts |
| volumes.localHome.customVolume | object | `{}` | When persistentVolumeClaim.create is false, then this value can be used to define a standard Kubernetes volume which will be used for the local-home volumes. If not defined, then defaults to an emptyDir volume. |
| volumes.localHome.mountPath | string | `"/var/atlassian/application-data/crowd"` | The path within the Crowd container which the local-home volume should be mounted. |
| volumes.localHome.persistentVolumeClaim.create | bool | `false` | If true, then a PersistentVolumeClaim will be created for each local-home volume. |
| volumes.localHome.persistentVolumeClaim.resources | object | `{"requests":{"storage":"1Gi"}}` | Specifies the standard Kubernetes resource requests and/or limits for the local-home volume claims. |
| volumes.localHome.persistentVolumeClaim.storageClassName | string | `nil` | Specifies the name of the storage class that should be used for the local-home volume claim. |
| volumes.sharedHome.customVolume | object | `{}` | When persistentVolumeClaim.create is false, then this value can be used to define a standard Kubernetes volume which will be used for the shared-home volume. If not defined, then defaults to an emptyDir (i.e. unshared) volume. |
| volumes.sharedHome.mountPath | string | `"/var/atlassian/application-data/crowd/shared"` | Specifies the path in the Crowd container to which the shared-home volume will be mounted. |
| volumes.sharedHome.nfsPermissionFixer.command | string | `nil` | By default, the fixer will change the group ownership of the volume's root directory to match the Crowd container's GID (2002), and then ensures the directory is group-writeable. If this is not the desired behaviour, command used can be specified here. |
| volumes.sharedHome.nfsPermissionFixer.enabled | bool | `false` | If enabled, this will alter the shared volume's root directory so that Crowd can write to it. This is a workaround for a Kubernetes bug affecting NFS volumes: https://github.com/kubernetes/examples/issues/260 |
| volumes.sharedHome.nfsPermissionFixer.mountPath | string | `"/shared-home"` | The path in the initContainer where the shared-home volume will be mounted |
| volumes.sharedHome.persistentVolumeClaim.create | bool | `false` | If true, then a PersistentVolumeClaim will be created for the shared-home volume. |
| volumes.sharedHome.persistentVolumeClaim.resources | object | `{"requests":{"storage":"1Gi"}}` | Specifies the standard Kubernetes resource requests and/or limits for the shared-home volume claims. |
| volumes.sharedHome.persistentVolumeClaim.storageClassName | string | `nil` | Specifies the name of the storage class that should be used for the shared-home volume claim. |
| volumes.sharedHome.subPath | string | `nil` | Specifies the sub-directory of the shared-home volume which will be mounted in to the Crowd container. |

----------------------------------------------
Autogenerated from chart metadata using [helm-docs v1.5.0](https://github.com/norwoodj/helm-docs/releases/v1.5.0)
