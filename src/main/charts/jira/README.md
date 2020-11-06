# jira

![Version: 0.1.0](https://img.shields.io/badge/Version-0.1.0-informational?style=flat-square) ![Type: application](https://img.shields.io/badge/Type-application-informational?style=flat-square) ![AppVersion: 8.13.0-jdk11](https://img.shields.io/badge/AppVersion-8.13.0--jdk11-informational?style=flat-square)

A chart for installing Jira DC on Kubernetes

## Values

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| additionalContainers | list | `[]` | Additional container definitions that will be added to all Jira pods |
| additionalInitContainers | list | `[]` | Additional initContainer definitions that will be added to all Jira pods |
| affinity | object | `{}` | Standard Kubernetes affinities that will be applied to all Jira pods |
| database.credentials.passwordSecretKey | string | `"password"` | The key in the Secret used to store the database login password |
| database.credentials.secretName | string | `"jira-database-credentials"` | The name of the Kubernetes Secret that contains the database login credentials. |
| database.credentials.usernameSecretKey | string | `"username"` | The key in the Secret used to store the database login username |
| database.driver | string | `nil` | The Java class name of the JDBC driver to be used, e.g. org.postgresql.Driver |
| database.type | string | `nil` | The type of database being used. Valid values include 'postgres72', 'mysql57', 'mysql8', 'oracle10g', 'mssql', 'postgresaurora96' |
| database.url | string | `nil` | The JDBC URL of the database to be used by Jira, e.g. jdbc:postgresql://host:port/database |
| image.pullPolicy | string | `"IfNotPresent"` |  |
| image.repository | string | `"atlassian/jira-software"` |  |
| image.tag | string | `""` | The docker image tag to be used. Defaults to the Chart appVersion. |
| jira.additionalBundledPlugins | list | `[]` | Specifies a list of additional Jira plugins that should be added to the Jira container. These are specified in the same manner as the additionalLibraries field, but the files will be loaded as bundled plugins rather than as libraries. |
| jira.additionalEnvironmentVariables | list | `[]` | Defines any additional environment variables to be passed to the Jira container. See https://hub.docker.com/r/atlassian/jira-software for supported variables. |
| jira.additionalJvmArgs | string | `nil` | Specifies a list of additional arguments that can be passed to the Jira JVM, e.g. system properties |
| jira.additionalLibraries | list | `[]` | Specifies a list of additional Java libraries that should be added to the Jira container. Each item in the list should specify the name of the volume which contain the library, as well as the name of the library file within that volume's root directory. Optionally, a subDirectory field can be included to specify which directory in the volume contains the library file. |
| jira.additionalVolumeMounts | list | `[]` | Defines any additional volumes mounts for the Jira container. These can refer to existing volumes, or new volumes can be defined in volumes.additional. |
| jira.gid | string | `"2001"` | The GID used by the Jira docker image |
| jira.ports.http | int | `8080` | The port on which the Jira container listens for HTTP traffic |
| jira.readinessProbe.failureThreshold | int | `30` | The number of consecutive failures of the Jira container readiness probe before the pod fails readiness checks |
| jira.readinessProbe.initialDelaySeconds | int | `10` | The initial delay (in seconds) for the Jira container readiness probe, after which the probe will start running |
| jira.readinessProbe.periodSeconds | int | `5` | How often (in seconds) the Jira container readiness robe will run |
| jira.resources.container | object | `{}` | Specifies the standard Kubernetes resource requests and/or limits for the Jira container. It is important that if the memory resources are specified here, they must allow for the size of the Jira JVM. That means the maximum heap size, the reserved code cache size, plus other JVM overheads, must be accommodated. Allowing for (maxHeap+codeCache)*1.5 would be an example. |
| jira.resources.jvm.maxHeap | string | `"1g"` | The maximum amount of heap memory that will be used by the Jira JVM |
| jira.resources.jvm.minHeap | string | `"1g"` | The minimum amount of heap memory that will be used by the Jira JVM |
| jira.resources.jvm.reservedCodeCache | string | `"512m"` | The memory reserved for the Jira JVM code cache |
| jira.service.port | int | `80` | The port on which the Jira Kubernetes service will listen |
| jira.service.type | string | `"ClusterIP"` | The type of Kubernetes service to use for Jira |
| nodeSelector | object | `{}` | Standard Kubernetes node-selectors that will be applied to all Jira pods |
| podAnnotations | object | `{}` | Specify custom annotations to be added to all Jira pods |
| replicaCount | int | `1` | The initial number of pods that should be started at deployment of Jira. Note that because Jira requires initial manual configuration after the first pod is deployed, and before scaling up to additional pods, this should always be kept as 1. |
| serviceAccountName | string | `nil` | Specifies which serviceAccount to use for the pods. If not specified, the kubernetes default will be used. |
| tolerations | list | `[]` | Standard Kubernetes tolerations that will be applied to all Jira pods |
| volumes.additional | list | `[]` | Defines additional volumes that should be applied to all Jira pods. Note that this will not create any corresponding volume mounts; those needs to be defined in jira.additionalVolumeMounts |
| volumes.localHome.mountPath | string | `"/var/atlassian/application-data/jira"` | Specifies the path in the Jira container to which the local-home volume will be mounted. |
| volumes.localHome.resources | object | `{"requests":{"storage":"1Gi"}}` | Specifies the standard Kubernetes resource requests and/or limits for the Jira local-home volume. |
| volumes.localHome.storageClassName | string | `nil` | Specifies the name of the storage class that should be used for the Jira local-home volume |
| volumes.sharedHome.mountPath | string | `"/var/atlassian/application-data/shared-home"` | Specifies the path in the Jira container to which the shared-home volume will be mounted. |
| volumes.sharedHome.nfsPermissionFixer.command | string | `nil` | By default, the fixer will change the group ownership of the volume's root directory to match the Jira container's GID (2001), and then ensures the directory is group-writeable. If this is not the desired behaviour, command used can be specified here. |
| volumes.sharedHome.nfsPermissionFixer.enabled | bool | `true` | If enabled, this will alter the shared-home volume's root directory so that Jira can write to it. This is a workaround for a Kubernetes bug affecting NFS volumes: https://github.com/kubernetes/examples/issues/260 |
| volumes.sharedHome.nfsPermissionFixer.mountPath | string | `"/shared-home"` | The path in the initContainer where the shared-home volume will be mounted |
| volumes.sharedHome.subPath | string | `nil` | Specifies the sub-directory of the shared-home volume which will be mounted in to the Jira container. |
| volumes.sharedHome.volumeClaimName | string | `"jira-shared-home"` | The name of the PersistentVolumeClaim which will be used for the shared-home volume |

----------------------------------------------
Autogenerated from chart metadata using [helm-docs v1.4.0](https://github.com/norwoodj/helm-docs/releases/v1.4.0)
