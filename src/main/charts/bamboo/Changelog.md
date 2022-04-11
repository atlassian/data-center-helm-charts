# Change Log

## 1.4.0

**Release date:** TBD

![AppVersion: 7.13.5](https://img.shields.io/static/v1?label=AppVersion&message=7.13.5&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Support for configuring ingress proxy settings via values.yaml (#402)

## 1.3.0

**Release date:** 2022-23-03

![AppVersion: 8.1.3](https://img.shields.io/static/v1?label=AppVersion&message=8.1.3&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* TFKUBE-384: ATL_BASE_URL should be appropriately set when ingress.path is supplied (#391)
* Update Bamboo version to 8.1.3 (#396)

## 1.2.0

**Release date:** 2022-02-14

![AppVersion: 8.1.2-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=8.1.1-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* DCD-1452: Updated appVersion to the latest product LTS version. (#378)
* Added end-to-end test for Bamboo Helm chart using Terraform (#375)
* Improvements on [documentation](https://github.com/atlassian/data-center-helm-charts/) (#370)
* Updated Atlassian charts to use common definitions (#303)
* Added service account annotation (#363)
* Added new feature additionalVolumeClaimTemplates and provided example in documentation (#334, #368)


## 1.0.0

**Release date:** 2022-01-11

This is the first officially supported version of the Helm chart.

![AppVersion: 8.1.1-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=8.1.1-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)

* Support for defining podLabels (#366)
* Support for defining loadBalancerIP (#365)
* Support for defining podAnnotations (#341)

## 0.0.2

**Release date:** 2021-11-03

![AppVersion: 8.1.1-jdk11](https://img.shields.io/static/v1?label=AppVersion&message=8.1.1-jdk11&color=success&logo=)
![Kubernetes: >=1.19.x-0](https://img.shields.io/static/v1?label=Kubernetes&message=>=1.19.x-0&color=informational&logo=kubernetes)
![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)


* BAMBK8S-194: Support for unattended setups (#359)
* DCKUBE-738: Add topologySpreadConstraints to products (#351)
* BAMBK8S-129: Bamboo pre-seeding configuration (#349)
* BAMBK8S-86: Initial agent support (#335)
* Add additionalPorts for the Bamboo StatefulSet (#353)

## 0.0.1

Initial release