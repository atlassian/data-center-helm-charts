# bamboo-agent

![Version: 0.0.1](https://img.shields.io/badge/Version-0.0.1-informational?style=flat-square) ![Type: application](https://img.shields.io/badge/Type-application-informational?style=flat-square) ![AppVersion: 8.0.1-jdk11](https://img.shields.io/badge/AppVersion-8.0.1--jdk11-informational?style=flat-square)

A chart for installing Bamboo Data Center remote agents on Kubernetes

For installation please follow [the documentation](https://atlassian.github.io/data-center-helm-charts/).

**Homepage:** <https://www.atlassian.com/software/bamboo>

## Source Code

* <https://github.com/atlassian-labs/data-center-helm-charts>
* <https://bitbucket.org/atlassian-docker/docker-bamboo-agent-base>

## Requirements

Kubernetes: `>=1.19.x-0`

## Values

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| additionalContainers | list | `[]` | Additional container definitions that will be added to all Bamboo agent pods |
| additionalInitContainers | list | `[]` | Additional initContainer definitions that will be added to all Bamboo agent pods |
| additionalLabels | object | `{}` | Additional labels that should be applied to all resources |
| affinity | object | `{}` | Standard K8s affinities that will be applied to all Bamboo agent pods |
| agent.additionalEnvironmentVariables | list | `[]` | Defines any additional environment variables to be passed to the Bamboo agent container. See https://bitbucket.org/atlassian-docker/docker-bamboo-agent-base for  supported variables. |
| agent.containerSecurityContext | object | `{}` | Standard K8s field that holds security configurations that will be applied to a container. https://kubernetes.io/docs/tasks/configure-pod-container/security-context/ |
| agent.ports.http | int | `80` | The port on which the Bamboo agent listens for HTTP traffic |
| agent.readinessProbe.failureThreshold | int | `30` | The number of consecutive failures of the Bamboo agent container readiness probe  before the pod fails readiness checks. |
| agent.readinessProbe.initialDelaySeconds | int | `10` | The initial delay (in seconds) for the Bamboo agent container readiness probe,  after which the probe will start running. |
| agent.readinessProbe.periodSeconds | int | `5` | How often (in seconds) the Bamboo agent container readiness probe will run |
| agent.resources.container.requests.cpu | string | `"1"` | Initial CPU request by Bamboo agent pod |
| agent.resources.container.requests.memory | string | `"2G"` | Initial Memory request by Bamboo agent pod |
| agent.resources.jvm.maxHeap | string | `"512m"` | The maximum amount of heap memory that will be used by the Bamboo agent JVM |
| agent.resources.jvm.minHeap | string | `"256m"` | The minimum amount of heap memory that will be used by the Bamboo agent JVM |
| agent.securityContext.fsGroup | int | `2005` | The GID used by the Bamboo docker image If not supplied, will default to 2005. This is intended to ensure that the shared-home volume is group-writeable by the GID used by the Bamboo container. However, this doesn't appear to work for NFS volumes due to a K8s bug: https://github.com/kubernetes/examples/issues/260 |
| agent.securityToken | string | `nil` |  |
| agent.server | string | `nil` |  |
| agent.shutdown.command | string | `"/shutdown-wait.sh"` | By default pods will be stopped via a [preStop hook](https://kubernetes.io/docs/concepts/containers/container-lifecycle-hooks/), using a script supplied by the Docker image. If any other shutdown behaviour is needed it can be achieved by overriding this value. Note that the shutdown command needs to wait for the application shutdown completely before exiting; see [the default TODO: This needs to be updated when Steve's changes are done command](https://bitbucket.org/atlassian-docker/docker-atlassian-jira/src/master/shutdown-wait.sh) for details. |
| agent.shutdown.terminationGracePeriodSeconds | int | `30` | The termination grace period for pods during shutdown. This should be set to the internal grace period, plus a small buffer to allow the JVM to fully terminate. |
| image.pullPolicy | string | `"IfNotPresent"` | Image pull policy |
| image.repository | string | `"atlassian/bamboo-agent-base"` | The Bamboo agent Docker image to use https://hub.docker.com/r/atlassian/bamboo-agent-base |
| image.tag | string | `""` | The docker image tag to be used - defaults to the Chart appVersion |
| nodeSelector | object | `{}` | Standard K8s node-selectors that will be applied to all Bamboo agent pods |
| podAnnotations | object | `{}` | Custom annotations that will be applied to all Bamboo agent pods |
| replicaCount | int | `1` | The initial number of Bamboo agent pods that should be started at deployment time.  |
| schedulerName | string | `nil` | Standard K8s schedulerName that will be applied to all Bamboo agent pods. Check Kubernetes documentation on how to configure multiple schedulers: https://kubernetes.io/docs/tasks/extend-kubernetes/configure-multiple-schedulers/#specify-schedulers-for-pods |
| serviceAccount.create | bool | `true` | Set to 'true' if a ServiceAccount should be created, or 'false' if it  already exists. |
| serviceAccount.imagePullSecrets | list | `[]` | For Docker images hosted in private registries, define the list of image pull  secrets that should be utilized by the created ServiceAccount https://kubernetes.io/docs/concepts/containers/images/#specifying-imagepullsecrets-on-a-pod |
| serviceAccount.name | string | `nil` | The name of the ServiceAccount to be used by the pods. If not specified, but  the "serviceAccount.create" flag is set to 'true', then the ServiceAccount name  will be auto-generated, otherwise the 'default' ServiceAccount will be used. https://kubernetes.io/docs/tasks/configure-pod-container/configure-service-account/#use-the-default-service-account-to-access-the-api-server |
| tolerations | list | `[]` | Standard K8s tolerations that will be applied to all Bamboo agent pods |