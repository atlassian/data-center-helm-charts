# Development

## Environment

###kubectl
This is the basic CLI tool you use to communicate with a k8s cluster. You can use this to query the cluster to see what’s running on it, or make changes to it. 

Whilst you can use kubectl to deploy and manage your application, using YAML descriptor files, it’s a little too low-level, so we’ll use helm to do that for us (see below), but you’ll use kubectl a lot for diagnostics.

To find more information about kubectl and installation see [Kubernetes Documentation](https://kubernetes.io/docs/tasks/tools/).

### Helm

This is another CLI tool which talks to the k8s cluster. It uses YAML template files that describe an entire deployable application, and provides versioning and deployment/undeployment semantics. It also has a whole set of terminology all of its own:

* A chart is package of YAML files that describe a deployable application. You can get charts from a central repository, or you can build your own (which we will be doing). These YAML files describe k8s statefulsets, services and ingresses, and all get deloyed together. Charts are explicitly versioned - the chart itself has a version, and the application that it deploys also has a version.
* A release is a deployment of a Helm chart to a k8s cluster. You can deploy the same chart multiple times to the same cluster (e.g. for rolling upgrades)

To find more information about Helm installation see [Installing Helm](https://helm.sh/docs/intro/install/).

### Maven

Data 
Center Helm Chart project build is managed by Apache Maven, therefore maven should be installed as part of developement setup. 

## Prerequisites 
Before deploy and setup Atlassian's Data Center products the following resources are required:
* A Kubernetes cluster
* An Ingress Controller
* A database
* Have a `shared-home` volume
* Setup `local-home` volume

To see how to create and setup those resources follow [Prerequisites Documentation](https://atlassian-labs.github.io/data-center-helm-charts/userguide/PREREQUISITES/).

## Pre-commit hooks

Repository contains repository hook definition to auto-generate chart documentation in case there is a change to `values.yaml` file.

### Installation

1. Install `pre-commit` following [official instructions](https://pre-commit.com/#install).
2. In the repository root run `pre-commit install`, which will install the hooks.

### Usage

When you have the pre-commit hook installed and `commit` a file, it will run the pre-commit hook. If there is a change generated in the `README.md`, the hook will fail and notify you about the new change. You can review the changes and run the `commit` again, now it should pass.


## Build and test the project

To build and test the project locally use maven. Build process uses helm charts and values yaml files in `src/main/charts` and generates `/target` folder.  
All items in `Chart.yaml` and `values.yaml` could be overridden by `test/charts/config/[product]/` files. 

```shell script
mvn clean install -Dshared.pvc.name=shared-home-claim -Deks.ingress.domain=kube.jira-dev.com -Dkubernetes.target.namespace=jira
```

## Install helm charts 

To install the helm charts for test purposes `helm_install.sh` can be used. This script accepts the parameters to customise the 
installation and does the following:

* Check the bash version to ensure is 5 or higher
* Ensure jq is available
* Ensure the `shared-home` is available and bootstrap nfs
* Create the postgres database
* Create elasticsearch
* Package the selected product's Helm chart
* Package the functest Helm chart
* Install the product's Helm chart
* Install the functest Helm chart
* Wait until the Ingress we just created starts serving up non-error responses
* Run the chart's tests

Helm parameters are produced in the build process and stored in `test/config/[product]/helm_parameters`. 
To install and test the product on the existing Kubernetes cluster run the following script:

```shell script
./src/test/scripts/helm_install.sh target/config/[product]/helm_parameters
```

To uninstall the test product and cleanup the resources run `helm_uninstall.sh`:

```shell script
./src/test/scripts/helm_uninstall.sh target/config/[product]/helm_parameters
```

# Technical documentation

## Setup

Technical documentation is produced by `mkdocs` python library. The documentation and necessary files are located in `/docs/` folder.

## How to work with documentation

To make working with the documentation easier, there is a `Makefile` in the project root folder. To start the server just run:

    make docs

This will create a docker image locally, process the files and start the live server providing documentation. You should be able to open http://127.0.0.1:8000/ in your browser. If you make any change to the documentation in the `/docs/` folder, it will be picked up by the server and browser will automatically reload.

## Deployment

Documentation is automatically deployed to [official documentation](https://atlassian-labs.github.io/data-center-helm-charts/) with Github Pages. Any change that is merged into the default branch will trigger Github Actions that builds the static documentation site and pushes it to `gh-pages` branch. This branch is then deployed by Github Pages.