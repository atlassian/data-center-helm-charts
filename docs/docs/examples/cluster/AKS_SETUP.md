# Preparing an AKS cluster
This example provides instructions for creating a Kubernetes cluster using [Azure AKS](https://azure.microsoft.com/en-au/services/kubernetes-service/){.external}.

## Prerequisites
We recommend installing and configuring the [Azure Cloud Shell](https://docs.microsoft.com/en-au/azure/cloud-shell/quickstart){.external}, allowing for CLI interaction with the AKS cluster.

## Manual creation
Follow the [Azure Kubernetes Service Quickstart](https://docs.microsoft.com/en-au/azure/aks/kubernetes-walkthrough){.external} for details on creating an AKS cluster.

---
!!!tip "Next step - Ingress controller"

    Having established a cluster, continue with provisioning the next piece of prerequisite infrastructure, the [Ingress controller](../../userguide/PREREQUISITES.md#provision-an-ingress-controller).