# Atlassian Data Center Helm Charts

[![Atlassian license](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat-square)](LICENSE) 
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](CONTRIBUTING.md) 
[![Maven unit tests](https://github.com/atlassian-labs/data-center-helm-charts/actions/workflows/maven.yml/badge.svg)](https://github.com/atlassian-labs/data-center-helm-charts/actions/workflows/maven.yml)

This project contains [Helm charts](https://helm.sh/) for installing Atlassian's [Jira Data Center](https://www.atlassian.com/enterprise/data-center/jira), [Confluence Data Center](https://www.atlassian.com/enterprise/data-center/confluence), and [Bitbucket Data Center](https://www.atlassian.com/enterprise/data-center/bitbucket) on [Kubernetes](https://kubernetes.io/docs/concepts/overview/what-is-kubernetes/). 

Use the charts to install and operate Data Center products within a Kubernetes cluster of your choice. It can be a managed environment, such as [Amazon EKS](https://aws.amazon.com/eks/), [Azure Kubernetes Service](https://azure.microsoft.com/en-au/services/kubernetes-service/), [Google Kubernetes Engine](https://cloud.google.com/kubernetes-engine), or a custom on-premise system.

## Support disclaimer

These Helm charts are in **Beta phase and unsupported**, with the goal of introducing official support once they have been
stabilized.

The platforms documented on [PLATFORMS.md](docs/PLATFORMS.md) **are not officially supported** as part of the Atlassian Data Center products, and should be used only as examples.

All the functionality described in [examples](docs/examples) **is not officially supported**, use it as examples only.

## Feedback

If you find any issue, please [raise a ticket](https://github.com/atlassian-labs/data-center-helm-charts/issues/new) in this repository. If you have general feedback or question regarding the charts, please use [Atlassian Community Kubernetes space](https://community.atlassian.com/t5/Atlassian-Data-Center-on/gh-p/DC_Kubernetes).

## Installing the Helm charts

* [Prerequisites and setup](docs/PREREQUISITES.md) - everything you need to do before installing the Helm charts
* [Installation](docs/INSTALLATION.md) - the steps to install the Helm charts

## Additional content

* [Operation](docs/OPERATION.md) - how to upgrade applications, scale your cluster, and update resources
* [Configuration](docs/CONFIGURATION.md) - a deep dive into the configuration parameters
* [Platforms support](docs/PLATFORMS.md) - how to allow support for different platforms
* [Examples](docs/examples/)
  * [How to deploy an EFK stack to Kubernetes](docs/examples/logging/efk/EFK.md)
  * [Implementation of an NFS Server for Bitbucket](docs/examples/storage/nfs/NFS.md)
  * [Local storage - utilizing AWS EBS-backed volumes](docs/examples/storage/aws/LOCAL_STORAGE.md)
  * [Shared storage - utilizing AWS EFS-backed filesystem](docs/examples/storage/aws/SHARED_STORAGE.md)


  

## Contributions

Contributions are welcome. Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details. 


## License

Copyright (c) [2021] Atlassian and others.
Apache 2.0 licensed, see [LICENSE](LICENSE) file.
