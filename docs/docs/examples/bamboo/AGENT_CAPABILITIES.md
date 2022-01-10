# Agent capabilities

A capability is a feature of an agent. A capability can be defined on an agent for:

* an executable (e.g. Maven)
* a JDK
* a Version Control System client application (e.g. Git)

You can learn more about remote agents capabilities on the [official documentation page](https://confluence.atlassian.com/bamboo/configuring-capabilities-289277148.html){.external}.

## Custom capabilities

!!!info "Default capabilities"

    By default the Bamboo agent Helm chart will deploy the [bamboo-agent-base](https://hub.docker.com/r/atlassian/bamboo-agent-base){.external} Docker image. This image provides the following capabilities out of the box:
        
    * JDK 11
    * Git & Git LFS
    * Maven 3
    * Python 3

If additional capabilities are required, the [Bamboo agent base Docker image](https://bitbucket.org/atlassian-docker/docker-bamboo-agent-base/src/master/){.external} can be extended with those capabilities. 

This custom image can be used, by first updating the Bamboo agent `values.yaml` with the image `tag` of the custom Docker image i.e.

```yaml
image:
  repository: hoolicorp/bamboo-agent-base
  pullPolicy: IfNotPresent
  tag: "ruby-agent"
```

The custom agent can then be deployed via Helm:

```shell
helm install ruby-agent atlassian-data-center/bamboo-agent -f ruby-agent.yaml
```