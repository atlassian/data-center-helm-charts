# Preparing an EKS cluster
This example provides instructions for creating a Kubernetes cluster using [Amazon EKS](https://aws.amazon.com/eks/){.external}.

## Prerequisites
We recommend installing and configuring [eksctl](https://docs.aws.amazon.com/eks/latest/userguide/eksctl.html){.external}, allowing for CLI interaction with the EKS cluster.

## Manual creation
Follow the [Getting started with Amazon EKS](https://docs.aws.amazon.com/eks/latest/userguide/getting-started.html){.external} for details on creating an EKS cluster. Or, using the `ClusterConfig` below as an example, deploy a K8s cluster with `eksctl` in ~20 minutes:

```yaml
apiVersion: eksctl.io/v1alpha5
kind: ClusterConfig

metadata:
  name: atlassian-cluster
  region: ap-southeast-2

managedNodeGroups:
  - name: appNodes
    instanceType: m5.large
    desiredCapacity: 2
    ssh: # enable SSH using SSM
      enableSsm: true
```

???question "Cluster considerations"
    It's always a good idea to consider the following points before creating the cluster:

    1. [Geographical region](https://aws.amazon.com/about-aws/global-infrastructure/regions_az/){.external} - where will the cluster reside.
    2. [EC2 instance type](https://aws.amazon.com/ec2/instance-types/){.external} - the instance type to be used for the nodes that make up the cluster.
    3. Number of nodes - guidance on the resource dimensions that should be used for these nodes can be found in [Requests and limits](../../userguide/resource_management/REQUESTS_AND_LIMITS.md).

Adding the config above to a file named `config.yaml` provision the cluster: 

```shell
eksctl create cluster -f config.yaml
```

---
!!!tip "Next step - Ingress controller"
    
    Having established a cluster, continue with provisioning the next piece of prerequisite infrastructure, the [Ingress controller](../../userguide/PREREQUISITES.md#provision-an-ingress-controller).
