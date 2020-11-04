# Data Center Helm Charts

[![Atlassian license](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat-square)](LICENSE) [![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](CONTRIBUTING.md)

This project contains [Helm charts](https://helm.sh/) for Atlassian's [Data Center products](https://www.atlassian.com/enterprise/data-center).
These charts are experimental and unsupported, with the aim of introducing official support once they have been stabilized.

The charts will allow the Data Center products to be easily installed and operated within a Kubernetes cluster,
whether it be a managed environment such as Amazon EKS, or a custom, on-premise system.

## Installation

1. `helm repo add atlassian-data-center https://atlassian-labs.github.io/data-center-helm-charts`
1. Write a `values.yaml` file to provide your site-specific configuration
1. Create the required authentication Secrets in your Kubernetes cluster
1. `helm install <release name> --values <values.yaml> atlassian-data-center/jira` (or `/confluence`, `/bitbucket`)
1. `helm test <release name>`
1. Configure an HTTPS ingress for your deployment

## Documentation

[Here](docs/DOCUMENTATION.md)

## Contributions

Contributions are welcome. Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details. 

## License

Copyright (c) [2020] Atlassian and others.
Apache 2.0 licensed, see [LICENSE](LICENSE) file.
