# Logging in Kubernetes environment - EFK Stack

## Disclaimer

**This functionality is not officially supported.** This document explains how to enable aggregated logging in your Kubernetes cluster. There are many ways to do this and this document showcases only a few of the options.

## EFK Stack

A common Kubernetes logging pattern is the combination of ***Elasticsearch***, ***Fluentd***, and ***Kibana***, known as *EFK Stack*. 

There are different methods to deploy an EFK stack. We provide two deployment methods, the first is deploying EFK locally on Kubernetes, and the second is using managed Elasticsearch outside the Kubernetes cluster. 

The two examples are detailed in [LOGGING.md](../../../docs/examples/LOGGING.md).
