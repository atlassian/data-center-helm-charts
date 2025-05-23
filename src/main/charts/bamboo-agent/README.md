# bamboo-agent

![Version: 2.0.1](https://img.shields.io/badge/Version-2.0.1-informational?style=flat-square) ![Type: application](https://img.shields.io/badge/Type-application-informational?style=flat-square) ![AppVersion: 11.0.1](https://img.shields.io/badge/AppVersion-11.0.1-informational?style=flat-square)

A chart for installing Bamboo Data Center remote agents on Kubernetes

**Homepage:** <https://www.atlassian.com/software/bamboo>

## Source Code

* <https://github.com/atlassian/data-center-helm-charts>
* <https://bitbucket.org/atlassian-docker/docker-bamboo-agent-base>

## Requirements

Kubernetes: `>=1.21.x-0`

| Repository | Name | Version |
|------------|------|---------|
| https://atlassian.github.io/data-center-helm-charts | common | 1.2.7 |

## Values

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| additionalContainers | list | `[]` | Additional container definitions that will be added to all Bamboo agent pods  |
| additionalFiles | list | `[]` | Additional existing ConfigMaps and Secrets not managed by Helm that should be mounted into service container. Configuration details below (camelCase is important!): 'name'      - References existing ConfigMap or Secret name. 'type'      - 'configMap' or 'secret' 'key'       - The file name. 'mountPath' - The destination directory in a container. VolumeMount and Volumes are added with this name and index position, for example; custom-config-0, keystore-2  |
| additionalHosts | list | `[]` | Additional host aliases for each pod, equivalent to adding them to the /etc/hosts file. https://kubernetes.io/docs/concepts/services-networking/add-entries-to-pod-etc-hosts-with-host-aliases/ |
| additionalInitContainers | list | `[]` | Additional initContainer definitions that will be added to all Bamboo agent pods  |
| additionalLabels | object | `{}` | Additional labels that should be applied to all resources  |
| affinity | object | `{}` | Standard K8s affinities that will be applied to all Bamboo agent pods  |
| agent.additionalEnvironmentVariables | list | `[]` | Defines any additional environment variables to be passed to the Bamboo agent container. See https://bitbucket.org/atlassian-docker/docker-bamboo-agent-base for supported variables.  |
| agent.additionalPorts | list | `[]` | Defines any additional ports for the Bamboo agent container.  |
| agent.additionalVolumeMounts | object | `{}` | Defines any additional volume mounts for the Bamboo agent container. These can refer to existing volumes, or new volumes can be defined via 'volumes.additional'.  |
| agent.containerSecurityContext | object | `{}` | Standard K8s field that holds security configurations that will be applied to a container. https://kubernetes.io/docs/tasks/configure-pod-container/security-context/  |
| agent.readinessProbe.command | string | `"/probe-readiness.sh"` | Command to use to check the readiness status. This is provided by the agent image.  |
| agent.readinessProbe.failureThreshold | int | `30` | The number of consecutive failures of the Bamboo agent container readiness probe before the pod fails readiness checks.  |
| agent.readinessProbe.initialDelaySeconds | int | `1` | The initial delay (in seconds) for the Bamboo agent container readiness probe, after which the probe will start running. When used in conjunction with a startupProbe this can be short.  |
| agent.readinessProbe.periodSeconds | int | `5` | How often (in seconds) the Bamboo agent container readiness probe will run  |
| agent.resources.container.requests.cpu | string | `"1"` | Initial CPU request by Bamboo agent pod  |
| agent.resources.container.requests.memory | string | `"2G"` | Initial Memory request by Bamboo agent pod  |
| agent.resources.jvm.maxHeap | string | `"512m"` | The maximum amount of heap memory that will be used by the Bamboo agent JVM  |
| agent.resources.jvm.minHeap | string | `"256m"` | The minimum amount of heap memory that will be used by the Bamboo agent JVM  |
| agent.securityContext.fsGroup | int | `2005` | The GID used by the Bamboo docker image GID will default to 2005 if not supplied and securityContextEnabled is set to true. This is intended to ensure that the shared-home volume is group-writeable by the GID used by the Bamboo container. However, this doesn't appear to work for NFS volumes due to a K8s bug: https://github.com/kubernetes/examples/issues/260  |
| agent.securityContext.fsGroupChangePolicy | string | `"OnRootMismatch"` | fsGroupChangePolicy defines behavior for changing ownership and permission of the volume before being exposed inside a Pod. This field only applies to volume types that support fsGroup controlled ownership and permissions. https://kubernetes.io/docs/tasks/configure-pod-container/security-context/#configure-volume-permission-and-ownership-change-policy-for-pods  |
| agent.securityContextEnabled | bool | `true` | Whether to apply security context to pod.  |
| agent.securityToken.secretKey | string | `"security-token"` |  |
| agent.securityToken.secretName | string | `nil` | The name of the K8s Secret that contains the security token. When specified the token will be automatically utilised on agent boot. An Example of creating a K8s secret for the secret below: 'kubectl create secret generic <secret-name> --from-literal=security-token=<security token>' https://kubernetes.io/docs/concepts/configuration/secret/#opaque-secrets  |
| agent.server | string | `nil` |  |
| agent.serverProtocol | string | `"http"` | The network protocol used for accessing the Bamboo server. Valid values are "http" for unencrypted connections or "https" for encrypted connections using TLS/SSL.  |
| agent.shutdown.command | string | `nil` | Custom command for a [preStop hook](https://kubernetes.io/docs/concepts/containers/container-lifecycle-hooks/). Undefined by default which means no pre-stop hook is being executed when an agent container needs to be stopped and deleted  |
| agent.shutdown.terminationGracePeriodSeconds | int | `30` | The termination grace period for pods during shutdown. This should be set to the internal grace period, plus a small buffer to allow the JVM to fully terminate.  |
| agent.startupProbe.command | string | `"/probe-startup.sh"` | Command to use to check the startup status. This is provided by the agent image.  |
| agent.startupProbe.failureThreshold | int | `120` | The number of consecutive failures of the Bamboo agent container startup probe before the pod fails readiness checks.  |
| agent.startupProbe.initialDelaySeconds | int | `1` | The initial delay (in seconds) for the Bamboo agent container startup probe, after which the probe will start running.  |
| agent.startupProbe.periodSeconds | int | `1` | How often (in seconds) the Bamboo agent container startup probe will run  |
| agent.topologySpreadConstraints | list | `[]` | Defines topology spread constraints for Bamboo agent pods. See details: https://kubernetes.io/docs/concepts/workloads/pods/pod-topology-spread-constraints/  |
| hostNamespaces | object | `{}` | Share host namespaces which may include hostNetwork, hostIPC, and hostPID  |
| image.pullPolicy | string | `"IfNotPresent"` | Image pull policy  |
| image.repository | string | `"atlassian/bamboo-agent-base"` | The Bamboo agent Docker image to use https://hub.docker.com/r/atlassian/bamboo-agent-base  |
| image.tag | string | `""` | The docker image tag to be used - defaults to the Chart appVersion  |
| nodeSelector | object | `{}` | Standard K8s node-selectors that will be applied to all Bamboo agent pods  |
| openshift.runWithRestrictedSCC | bool | `false` | When set to true, the containers will run with a restricted Security Context Constraint (SCC). See: https://docs.openshift.com/container-platform/4.14/authentication/managing-security-context-constraints.html This configuration property unsets pod's SecurityContext, nfs-fixer init container (which runs as root), and mounts server configuration files as ConfigMaps.  |
| podAnnotations | object | `{}` | Custom annotations that will be applied to all Bamboo agent pods  |
| podLabels | object | `{}` | Custom labels that will be applied to all Bamboo agent pods  |
| priorityClassName | string | `nil` | Priority class for the application pods. The PriorityClass with this name needs to be available in the cluster. For details see https://kubernetes.io/docs/concepts/scheduling-eviction/pod-priority-preemption/#priorityclass  |
| replicaCount | int | `1` | The initial number of Bamboo agent pods that should be started at deployment time.  |
| schedulerName | string | `nil` | Standard K8s schedulerName that will be applied to all Bamboo agent pods. Check Kubernetes documentation on how to configure multiple schedulers: https://kubernetes.io/docs/tasks/extend-kubernetes/configure-multiple-schedulers/#specify-schedulers-for-pods  |
| serviceAccount.annotations | object | `{}` | Annotations to add to the ServiceAccount (if created)  |
| serviceAccount.create | bool | `true` | Set to 'true' if a ServiceAccount should be created, or 'false' if it already exists.  |
| serviceAccount.imagePullSecrets | list | `[]` | For Docker images hosted in private registries, define the list of image pull secrets that should be utilized by the created ServiceAccount https://kubernetes.io/docs/concepts/containers/images/#specifying-imagepullsecrets-on-a-pod  |
| serviceAccount.name | string | `nil` | The name of the ServiceAccount to be used by the pods. If not specified, but the "serviceAccount.create" flag is set to 'true', then the ServiceAccount name will be auto-generated, otherwise the 'default' ServiceAccount will be used. https://kubernetes.io/docs/tasks/configure-pod-container/configure-service-account/#use-the-default-service-account-to-access-the-api-server  |
| tolerations | list | `[]` | Standard K8s tolerations that will be applied to all Bamboo agent pods  |
| volumes | object | `{"additional":null}` | Defines additional volumes that should be applied to all Bamboo agent pods. Note that this will not create any corresponding volume mounts which need to be defined in bamboo.additionalVolumeMounts  |

----------------------------------------------
Autogenerated from chart metadata using [helm-docs v1.12.0](https://github.com/norwoodj/helm-docs/releases/v1.12.0)
