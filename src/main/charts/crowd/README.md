# crowd

![Version: 0.1.0](https://img.shields.io/badge/Version-0.1.0-informational?style=flat-square) ![Type: application](https://img.shields.io/badge/Type-application-informational?style=flat-square) ![AppVersion: 7.9.0-jdk11](https://img.shields.io/badge/AppVersion-7.9.0--jdk11-informational?style=flat-square)

A chart for installing Crowd DC on Kubernetes

**Homepage:** <https://github.com/atlassian-labs/data-center-helm-charts>

## Source Code

* <https://github.com/atlassian-labs/data-center-helm-charts>

## Requirements

Kubernetes: `>=1.17.x-0`

## Values

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| additionalContainers | list | `[]` | Additional container definitions that will be added to all Crowd pods |
| additionalInitContainers | list | `[]` | Additional initContainer definitions that will be added to all Crowd pods |
| additionalLabels | object | `{}` | Additional labels that should be applied to all resources |
| affinity | object | `{}` | Standard Kubernetes affinities that will be applied to all Crowd pods |
| crowd.additionalBundledPlugins | list | `[]` | Specifies a list of additional Crowd plugins that should be added to the Crowd container. These are specified in the same manner as the additionalLibraries field, but the files will be loaded as bundled plugins rather than as libraries. |
| crowd.additionalEnvironmentVariables | list | `[]` | Defines any additional environment variables to be passed to the Crowd container. See https://hub.docker.com/r/atlassian/crowd-server for supported variables. |
| crowd.additionalJvmArgs | list | `[]` | Specifies a list of additional arguments that can be passed to the Crowd JVM, e.g. system properties |
| crowd.additionalLibraries | list | `[]` | Specifies a list of additional Java libraries that should be added to the Crowd container. Each item in the list should specify the name of the volume which contain the library, as well as the name of the library file within that volume's root directory. Optionally, a subDirectory field can be included to specify which directory in the volume contains the library file. |
| crowd.additionalVolumeMounts | list | `[]` | Defines any additional volumes mounts for the Crowd container. These can refer to existing volumes, or new volumes can be defined in volumes.additional. |
| crowd.clustering.enabled | bool | `false` | Set to true if Data Center clustering should be enabled This will automatically configure cluster peer discovery between cluster nodes. |
| crowd.clustering.usePodNameAsClusterNodeName | bool | `true` | Set to true if the Kubernetes pod name should be used as the end-user-visible name of the Data Center cluster node. |
| crowd.ports.hazelcast | int | `5701` | The port on which the Crowd container listens for Hazelcast traffic |
| crowd.ports.http | int | `8090` | The port on which the Crowd container listens for HTTP traffic |
| crowd.readinessProbe.failureThreshold | int | `30` | The number of consecutive failures of the Crowd container readiness probe before the pod fails readiness checks |
| crowd.readinessProbe.initialDelaySeconds | int | `10` | The initial delay (in seconds) for the Crowd container readiness probe, after which the probe will start running |
| crowd.readinessProbe.periodSeconds | int | `5` | How often (in seconds) the Crowd container readiness robe will run |
| crowd.resources.container | object | `{}` | Specifies the standard Kubernetes resource requests and/or limits for the Crowd container. It is important that if the memory resources are specified here, they must allow for the size of the Crowd JVM. That means the maximum heap size, the reserved code cache size, plus other JVM overheads, must be accommodated. Allowing for (maxHeap+codeCache)*1.5 would be an example. |
| crowd.resources.jvm.maxHeap | string | `"1g"` | The maximum amount of heap memory that will be used by the Crowd JVM |
| crowd.resources.jvm.minHeap | string | `"1g"` | The minimum amount of heap memory that will be used by the Crowd JVM |
| crowd.resources.jvm.reservedCodeCache | string | `"512m"` | The memory reserved for the Crowd JVM code cache |
| crowd.securityContext | object | `{"enabled":true,"gid":"2002"}` | Enable or disable security context in StatefulSet template spec. Enabled by default with UID 2002. -- Disable when deploying to OpenShift, unless anyuid policy is attached to a service account |
| crowd.securityContext.gid | string | `"2002"` | The GID used by the Crowd docker image |
| crowd.service.port | int | `80` | The port on which the Crowd Kubernetes service will listen |
| crowd.service.type | string | `"ClusterIP"` | The type of Kubernetes service to use for Crowd |
| image.pullPolicy | string | `"IfNotPresent"` |  |
| image.repository | string | `"atlassian/crowd-server"` |  |
| image.tag | string | `nil` | The docker image tag to be used. Defaults to the Chart appVersion. |
| ingress.annotations | object | `{}` | The custom annotations that should be applied to the Ingress. |
| ingress.create | bool | `false` | True if an Ingress should be created. |
| ingress.host | string | `nil` | The fully-qualified hostname of the Ingress. |
| ingress.https | bool | `true` | True if the browser communicates with the application over HTTPS. |
| ingress.nginx | bool | `true` | True if the created Ingress is to use the Kubernetes ingress-nginx controller. This will populate the Ingress with annotations for that controller. Set to false if a different controller is to be used, in which case the annotations need to be specified. |
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
| volumes.localHome.mountPath | string | `"/var/atlassian/application-data/crowd"` |  |
| volumes.localHome.persistentVolumeClaim.create | bool | `false` | If true, then a PersistentVolumeClaim will be created for each local-home volume. |
| volumes.localHome.persistentVolumeClaim.resources | object | `{"requests":{"storage":"1Gi"}}` | Specifies the standard Kubernetes resource requests and/or limits for the local-home volume claims. |
| volumes.localHome.persistentVolumeClaim.storageClassName | string | `nil` | Specifies the name of the storage class that should be used for the local-home volume claim. |
| volumes.sharedHome.customVolume | object | `{}` | When persistentVolumeClaim.create is false, then this value can be used to define a standard Kubernetes volume which will be used for the shared-home volume. If not defined, then defaults to an emptyDir (i.e. unshared) volume. |
| volumes.sharedHome.mountPath | string | `"/var/atlassian/application-data/shared-home"` | Specifies the path in the Crowd container to which the shared-home volume will be mounted. |
| volumes.sharedHome.nfsPermissionFixer.command | string | `nil` | By default, the fixer will change the group ownership of the volume's root directory to match the Crowd container's GID (2002), and then ensures the directory is group-writeable. If this is not the desired behaviour, command used can be specified here. |
| volumes.sharedHome.nfsPermissionFixer.enabled | bool | `false` | If enabled, this will alter the shared-home volume's root directory so that Crowd can write to it. This is a workaround for a Kubernetes bug affecting NFS volumes: https://github.com/kubernetes/examples/issues/260 |
| volumes.sharedHome.nfsPermissionFixer.mountPath | string | `"/shared-home"` | The path in the initContainer where the shared-home volume will be mounted |
| volumes.sharedHome.persistentVolumeClaim.create | bool | `false` | If true, then a PersistentVolumeClaim will be created for the shared-home volume. |
| volumes.sharedHome.persistentVolumeClaim.resources | object | `{"requests":{"storage":"1Gi"}}` | Specifies the standard Kubernetes resource requests and/or limits for the shared-home volume claims. |
| volumes.sharedHome.persistentVolumeClaim.storageClassName | string | `nil` | Specifies the name of the storage class that should be used for the shared-home volume claim. |
| volumes.sharedHome.subPath | string | `nil` | Specifies the sub-directory of the shared-home volume which will be mounted in to the Crowd container. |

----------------------------------------------
Autogenerated from chart metadata using [helm-docs v1.5.0](https://github.com/norwoodj/helm-docs/releases/v1.5.0)
