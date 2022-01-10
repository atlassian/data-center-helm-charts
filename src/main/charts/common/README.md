# Atlassian Common Library Chart

A [Helm Library Chart](https://helm.sh/docs/topics/library_charts/#helm) for grouping common logic between Atlassian charts.

## TL;DR

### Chart.yaml

```yaml
dependencies:
  - name: common
    version: 1.x.x # picks up any non-breaking version within 1.x.y line
    repository: https://atlassian.github.io/data-center-helm-charts
```

### Local development

```bash
helm dependency update
```

### Product usage

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: {{ include "common.names.fullname" . }}
data:
  myvalue: "Hello World"
```

## Introduction

This chart provides a common template helpers which can be used to develop new charts using [Helm](https://helm.sh) package manager.

Atlassian charts are used for installing Atlassian's [Jira Data Center](https://www.atlassian.com/enterprise/data-center/jira), [Confluence Data Center](https://www.atlassian.com/enterprise/data-center/confluence), and [Bitbucket Data Center](https://www.atlassian.com/enterprise/data-center/bitbucket) on Kubernetes.

## Prerequisites

- Kubernetes `>=1.19.x-0`
- Helm 3.3+

## Parameters

The following table lists the helpers available in the library which are scoped in different sections.

### Labels

| Helper identifier           | Description                                          | Expected Input    |
|-----------------------------|------------------------------------------------------|-------------------|
| `common.labels.commonLabels`    | Return Kubernetes standard labels                    | `.` Chart context |
| `common.labels.selectorLabels` | Return the selector labels | `.` Chart context |

### Names

| Helper identifier       | Description                                                | Expected Inpput   |
|-------------------------|------------------------------------------------------------|-------------------|
| `common.names.chart`    | Chart name plus version                                    | `.` Chart context |
| `common.names.name`     | Expand the name of the chart or use `.Values.nameOverride` | `.` Chart context |
| `common.names.fullname` | Create a default fully qualified app name                 | `.` Chart context |
