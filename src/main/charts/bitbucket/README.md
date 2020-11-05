# bitbucket

![Version: 0.1.0](https://img.shields.io/badge/Version-0.1.0-informational?style=flat-square) ![Type: application](https://img.shields.io/badge/Type-application-informational?style=flat-square) ![AppVersion: 7.7.0-jdk11](https://img.shields.io/badge/AppVersion-7.7.0--jdk11-informational?style=flat-square)

A chart for installing Bitbucket DC on Kubernetes

## Values

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| additionalContainers | list | `[]` | Additional container definitions that will be added to all Bitbucket pods |
| additionalInitContainers | list | `[]` | Additional initContainer definitions that will be added to all Bitbucket pods |
| affinity | object | `{}` | Standard Kubernetes affinities that will be applied to all Bitbucket pods |
| bitbucket.additionalBundledPlugins | list | `[]` | Specifies a list of additional Bitbucket plugins that should be added to the Bitbucket container. These are specified in the same manner as the additionalLibraries field, but the files will be loaded as bundled plugins rather than as libraries. |
| bitbucket.additionalLibraries | list | `[]` | Specifies a list of additional Java libraries that should be added to the Bitbucket container. Each item in the list should specify the name of the volume which contain the library, as well as the name of the library file within that volume's root directory. Optionally, a subDirectory field can be included to specify which directory in the volume contains the library file. |
| bitbucket.additionalVolumeMounts | list | `[]` | Defines any additional volumes mounts for the Bitbucket container. These can refer to existing volumes, or new volumes can be defined in volumes.additional. |
| bitbucket.gid | string | `"2003"` | The GID used by the Bitbucket docker image |
| bitbucket.license.secretKey | string | `"license-key"` | The key in the Kubernetes Secret which contains the Bitbucket license key |
| bitbucket.license.secretName | string | `"bitbucket-license"` | The name of the Kubernetes Secret which contains the Bitbucket license key |
| bitbucket.ports.hazelcast | int | `5701` |  |
| bitbucket.ports.http | int | `7990` |  |
| bitbucket.ports.ssh | int | `7999` |  |
| bitbucket.proxy.fqdn | string | `nil` | The fully-qualified domain name of the ingress |
| bitbucket.proxy.port | int | `443` | The port number of the ingress |
| bitbucket.proxy.scheme | string | `"https"` | note that, if present, the value of x-forwarded-proto header will trump this setting |
| bitbucket.proxy.secure | bool | `true` |  |
| bitbucket.resources.container | object | `{}` | Specifies the standard Kubernetes resource requests and/or limits for the Bitbucket container. It is important that if the memory resources are specified here, they must allow for the size of the Bitbucket JVM. That means the maximum heap size, the reserved code cache size, plus other JVM overheads, must be accommodated. Allowing for (maxHeap+codeCache)*1.5 would be an example. |
| bitbucket.resources.jvm.maxHeap | string | `"1g"` | The maximum amount of heap memory that will be used by the Bitbucket JVM |
| bitbucket.resources.jvm.minHeap | string | `"1g"` | The minimum amount of heap memory that will be used by the Bitbucket JVM |
| bitbucket.service.port | int | `80` | The port on which the Jira Kubernetes service will listen |
| bitbucket.service.type | string | `"ClusterIP"` | The type of Kubernetes service to use for Jira |
| bitbucket.sysadminCredentials.displayNameSecretKey | string | `"displayName"` | The key in the Kubernetes Secret which contains the sysadmin display name |
| bitbucket.sysadminCredentials.emailAddressSecretKey | string | `"emailAddress"` | The key in the Kubernetes Secret which contains the sysadmin email address |
| bitbucket.sysadminCredentials.passwordSecretKey | string | `"password"` | The key in the Kubernetes Secret which contains the sysadmin password |
| bitbucket.sysadminCredentials.secretName | string | `"bitbucket-sysadmin-credentials"` | The name of the Kubernetes Secret which contains the Bitbucket sysadmin credentials |
| bitbucket.sysadminCredentials.usernameSecretKey | string | `"username"` | The key in the Kubernetes Secret which contains the sysadmin username |
| database.credentials.passwordSecretKey | string | `"password"` | The key in the Secret used to store the database login password |
| database.credentials.secretName | string | `"bitbucket-database-credentials"` | The name of the Kubernetes Secret that contains the database login credentials. |
| database.credentials.usernameSecretKey | string | `"username"` | The key in the Secret used to store the database login username |
| database.driver | string | `nil` | The Java class name of the JDBC driver to be used, e.g. org.postgresql.Driver |
| database.url | string | `nil` | The JDBC URL of the database to be used by Bitbucket, e.g. jdbc:postgresql://host:port/database |
| image.pullPolicy | string | `"IfNotPresent"` |  |
| image.repository | string | `"atlassian/bitbucket-server"` |  |
| image.tag | string | `""` | The docker image tag to be used. Defaults to the Chart appVersion. |
| nodeSelector | object | `{}` | Standard Kubernetes node-selectors that will be applied to all Bitbucket pods |
| podAnnotations | object | `{}` | Specify custom annotations to be added to all Bitbucket pods |
| replicaCount | int | `1` | The initial number of pods that should be started at deployment of Bitbucket. |
| serviceAccountName | string | `nil` | Specifies which serviceAccount to use for the pods. If not specified, the kubernetes default will be used. |
| tolerations | list | `[]` | Standard Kubernetes tolerations that will be applied to all Bitbucket pods |
| volumes.additional | list | `[]` | Defines additional volumes that should be applied to all Bitbucket pods. Note that this will not create any corresponding volume mounts; those needs to be defined in bitbucket.additionalVolumeMounts |
| volumes.localHome.mountPath | string | `"/var/atlassian/application-data/bitbucket"` |  |
| volumes.localHome.resources | object | `{"requests":{"storage":"1Gi"}}` | Specifies the standard Kubernetes resource requests and/or limits for the Bitbucket local-home volume. |
| volumes.localHome.storageClassName | string | `nil` | Specifies the name of the storage class that should be used for the Bitbucket local-home volume |
| volumes.sharedHome.mountPath | string | `"/var/atlassian/application-data/shared-home"` | Specifies the path in the Bitbucket container to which the shared-home volume will be mounted. |
| volumes.sharedHome.nfsPermissionFixer.command | string | `nil` | By default, the fixer will change the group ownership of the volume's root directory to match the Bitbucket container's GID (2003), and then ensures the directory is group-writeable. If this is not the desired behaviour, command used can be specified here. |
| volumes.sharedHome.nfsPermissionFixer.enabled | bool | `true` | If enabled, this will alter the shared-home volume's root directory so that Bitbucket can write to it. This is a workaround for a Kubernetes bug affecting NFS volumes: https://github.com/kubernetes/examples/issues/260 |
| volumes.sharedHome.nfsPermissionFixer.mountPath | string | `"/shared-home"` | The path in the initContainer where the shared-home volume will be mounted |
| volumes.sharedHome.subPath | string | `nil` | Specifies the sub-directory of the shared-home volume which will be mounted in to the Bitbucket container. |
| volumes.sharedHome.volumeClaimName | string | `"bitbucket-shared-home"` | The name of the PersistentVolumeClaim which will be used for the shared-home volume |

----------------------------------------------
Autogenerated from chart metadata using [helm-docs v1.4.0](https://github.com/norwoodj/helm-docs/releases/v1.4.0)
