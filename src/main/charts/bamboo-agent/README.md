# bamboo-agent

![Version: 1.3.0](https://img.shields.io/badge/Version-1.3.0-informational?style=flat-square) ![Type: application](https://img.shields.io/badge/Type-application-informational?style=flat-square) ![AppVersion: 8.1.3](https://img.shields.io/badge/AppVersion-8.1.3-informational?style=flat-square)

A chart for installing Bamboo Data Center remote agents on Kubernetes

For installation please follow [the documentation](https://atlassian.github.io/data-center-helm-charts/).

**Homepage:** <https://www.atlassian.com/software/bamboo>

## Source Code

* <https://github.com/atlassian/data-center-helm-charts>
* <https://bitbucket.org/atlassian-docker/docker-bamboo-agent-base>

## Requirements

Kubernetes: `>=1.19.x-0`

| Repository | Name | Version |
|------------|------|---------|
| https://atlassian.github.io/data-center-helm-charts | common | 1.0.0 |

## Values

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| additionalContainers | list | `[]` | Additional container definitions that will be added to all Bamboo agent pods |
| additionalInitContainers | list | `[]` | Additional initContainer definitions that will be added to all Bamboo agent pods |
| additionalLabels | object | `{}` | Additional labels that should be applied to all resources |
| affinity | object | `{}` | Standard K8s affinities that will be applied to all Bamboo agent pods |
| agent.additionalEnvironmentVariables | list | `[]` | Defines any additional environment variables to be passed to the Bamboo agent container. See https://bitbucket.org/atlassian-docker/docker-bamboo-agent-base for  supported variables. |
| agent.additionalPorts | list | `[]` | Defines any additional ports for the Bamboo agent container. |
| agent.containerSecurityContext | object | `{}` | Standard K8s field that holds security configurations that will be applied to a container. https://kubernetes.io/docs/tasks/configure-pod-container/security-context/ |
| agent.readinessProbe.command | string | `"/probe-readiness.sh"` | Command to use to check the readiness status. This is provided by the agent image. |
| agent.readinessProbe.failureThreshold | int | `30` | The number of consecutive failures of the Bamboo agent container readiness probe  before the pod fails readiness checks. |
| agent.readinessProbe.initialDelaySeconds | int | `1` | The initial delay (in seconds) for the Bamboo agent container readiness probe, after which the probe will start running. When used in conjunction with a startupProbe this can be short. |
| agent.readinessProbe.periodSeconds | int | `5` | How often (in seconds) the Bamboo agent container readiness probe will run |
| agent.resources.container.requests.cpu | string | `"1"` | Initial CPU request by Bamboo agent pod |
| agent.resources.container.requests.memory | string | `"2G"` | Initial Memory request by Bamboo agent pod |
| agent.resources.jvm.maxHeap | string | `"512m"` | The maximum amount of heap memory that will be used by the Bamboo agent JVM |
| agent.resources.jvm.minHeap | string | `"256m"` | The minimum amount of heap memory that will be used by the Bamboo agent JVM |
| agent.securityContext.fsGroup | int | `2005` | The GID used by the Bamboo docker image If not supplied, will default to 2005. This is intended to ensure that the shared-home volume is group-writeable by the GID used by the Bamboo container. However, this doesn't appear to work for NFS volumes due to a K8s bug: https://github.com/kubernetes/examples/issues/260 |
| agent.securityToken.secretKey | string | `"security-token"` |  |
| agent.securityToken.secretName | string | `nil` | The name of the K8s Secret that contains the security token. When specified the token  will be automatically utilised on agent boot. An Example of creating a K8s secret for the  secret below: 'kubectl create secret generic <secret-name> --from-literal=security-token=<security token>' https://kubernetes.io/docs/concepts/configuration/secret/#opaque-secrets |
| agent.server | string | `nil` |  |
| agent.shutdown.command | string | `"/shutdown-wait.sh"` | By default pods will be stopped via a [preStop hook](https://kubernetes.io/docs/concepts/containers/container-lifecycle-hooks/), using a script supplied by the Docker image. If any other shutdown behaviour is needed it can be achieved by overriding this value. Note that the shutdown command needs to wait for the application shutdown completely before exiting; see [the default command](https://bitbucket.org/atlassian-docker/docker-bamboo-agent-base/src/master/shutdown-wait.sh) for details. |
| agent.shutdown.terminationGracePeriodSeconds | int | `30` | The termination grace period for pods during shutdown. This should be set to the internal grace period, plus a small buffer to allow the JVM to fully terminate. |
| agent.startupProbe.command | string | `"/probe-startup.sh"` | Command to use to check the startup status. This is provided by the agent image. |
| agent.startupProbe.failureThreshold | int | `120` | The number of consecutive failures of the Bamboo agent container startup probe before the pod fails readiness checks. |
| agent.startupProbe.initialDelaySeconds | int | `1` | The initial delay (in seconds) for the Bamboo agent container startup probe, after which the probe will start running. |
| agent.startupProbe.periodSeconds | int | `1` | How often (in seconds) the Bamboo agent container startup probe will run |
| agent.topologySpreadConstraints | list | `[]` | Defines topology spread constraints for Bamboo agent pods. See details: https://kubernetes.io/docs/concepts/workloads/pods/pod-topology-spread-constraints/ |
| image.pullPolicy | string | `"IfNotPresent"` | Image pull policy |
| image.repository | string | `"atlassian/bamboo-agent-base"` | The Bamboo agent Docker image to use https://hub.docker.com/r/atlassian/bamboo-agent-base |
| image.tag | string | `""` | The docker image tag to be used - defaults to the Chart appVersion |
| nodeSelector | object | `{}` | Standard K8s node-selectors that will be applied to all Bamboo agent pods |
| podAnnotations | object | `{}` | Custom annotations that will be applied to all Bamboo agent pods |
| podLabels | object | `{}` | Custom labels that will be applied to all Bamboo agent pods |
| replicaCount | int | `1` | The initial number of Bamboo agent pods that should be started at deployment time.  |
| schedulerName | string | `nil` | Standard K8s schedulerName that will be applied to all Bamboo agent pods. Check Kubernetes documentation on how to configure multiple schedulers: https://kubernetes.io/docs/tasks/extend-kubernetes/configure-multiple-schedulers/#specify-schedulers-for-pods |
| serviceAccount.annotations | object | `{}` | Annotations to add to the ServiceAccount (if created) |
| serviceAccount.create | bool | `true` | Set to 'true' if a ServiceAccount should be created, or 'false' if it  already exists. |
| serviceAccount.imagePullSecrets | list | `[]` | For Docker images hosted in private registries, define the list of image pull  secrets that should be utilized by the created ServiceAccount https://kubernetes.io/docs/concepts/containers/images/#specifying-imagepullsecrets-on-a-pod |
| serviceAccount.name | string | `nil` | The name of the ServiceAccount to be used by the pods. If not specified, but  the "serviceAccount.create" flag is set to 'true', then the ServiceAccount name  will be auto-generated, otherwise the 'default' ServiceAccount will be used. https://kubernetes.io/docs/tasks/configure-pod-container/configure-service-account/#use-the-default-service-account-to-access-the-api-server |
| tolerations | list | `[]` | Standard K8s tolerations that will be applied to all Bamboo agent pods |