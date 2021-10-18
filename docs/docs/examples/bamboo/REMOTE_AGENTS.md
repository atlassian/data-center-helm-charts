# Remote agents

Remote agents can be provisioned to a Kubernetes cluster to run jobs delegated to them via a Bamboo server. An agent can run a job if its capabilities match the requirements of a job. Each job inherits the requirements from individual tasks that it contains.

You can learn more details about remote agents on the [official documentation page](https://confluence.atlassian.com/bamboo/agents-and-capabilities-289277114.html).

## Requirements

!!!warning "Bamboo server prerequisites"

    * Your Bamboo server instance must be a fully licensed Bamboo Data Center instance

    * The bamboo server instance must be fully configured

    * The Bamboo server instance must have `security token verification` **enabled** 

    * The Bamboo server instance must have `remote agent authentication` **disabled**

## Installation

Steps required for deploying a remote agent

### Overview

1. Configure Bamboo server for remote agent support
2. Deploy agent

### Steps

#### 1. Configure Bamboo server

* When logged into the Bamboo server instance, and from the `Agents` settings tab, **enable** `security token verification`, and **disable** `remote agent authentication`
   ![security_token_verification](../../assets/images/bamboo_agents/enable-disable.png){ width="900" }
   
* Navigate to the remote agent's installation page by selecting the `Install remote agent` button from the `Agents` settings tab
   ![install_remote_agent](../../assets/images/bamboo_agents/install-remote-agent.png){ width="900" }

* Create a K8s secret using the `security token` rendered on the `Installing a remote agent` page
   ![security_token](../../assets/images/bamboo_agents/security-token.png){ width="900" }
      
   create secret using token...
   
   ``` shell
   kubectl create secret generic security-token --from-literal=security-token=<security token>
   ```

#### 2. Deploy agent 

* Update the bamboo agent `values.yaml` to utilize the security token secret and point to the bamboo server instance

```yaml
replicaCount: 3
agent:
  securityToken:
    secretName: "security-token"
    secretKey: security-token
  server: "bamboo.bamboo.svc.cluster.local"
```

!!!info "Values"

      * As long as your cluster has the physical resources the `replicaCount` can be set to any value from `1` .. `1 + n` 
      * `agent.server` should be configured with the K8s DNS record for the Bamboo server service. The value should be of the form: `<service_name>.<namespace>.svc.cluster.local`

* Install the agent

```shell
helm install bamboo-agent atlassian-data-center/bamboo-agent -f values.yaml
```

!!!tip "Custom agents"

        By default the Bamboo agent Helm chart will deploy the [bamboo-agent-base](https://hub.docker.com/r/atlassian/bamboo-agent-base){.external} Docker image. This image provides the following capabilities out of the box:
        
        * JDK 11
        * Git & Git LFS
        * Maven 3
        * Python 3

        For details on defining and deploying agents with custom/additional capabilities view the [agent capabilities guide](AGENT_CAPABILITIES.md)

## Scaling the agent count

The number of active agents can be easily increased or decreased: 

``` shell
helm upgrade --set replicaCount=<desired number of agents> \
             --reuse-values \
             <name of the release>
             atlassian-data-center/bamoboo-agent
```

## Troubleshooting

You can find the most common errors relating to agent configuration in the [official Bamboo agent documentation](https://confluence.atlassian.com/bamboo/bamboo-remote-agent-installation-guide-289276832.html){.external}.