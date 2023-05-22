# Monitoring with Prometheus

The instructions outlined on this page provide details on how you can enable [Prometheus](https://prometheus.io/){.external} monitoring on your stack with [Grafana](https://grafana.com/){.external}

## 1. Install Prometheus stack

!!!abstract "Note"
    * This approach will also install Grafana
    * For the purposes of this guide the Prometheus stack will be installed with the release name `prometheus-stack`.

Install [kube-prometheus-stack](https://github.com/prometheus-community/helm-charts/tree/main/charts/kube-prometheus-stack){.external} Helm chart

Fetch the repo and perform an update
```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
```
Now install the Prometheus stack
```bash
helm install prometheus-stack prometheus-community/kube-prometheus-stack
```

!!!tip "Persist Prometheus & Grafana data"

    By default, the Prometheus stack configures Pods to store data using an `emptyDir` volume, meaning data is not persisted when the Pods are redeployed/restarted. To maintain state, persistent storage for Prometheus and Grafana can be enabled. This can be done by updating the `prometheus-stack` with the following `yaml`:
    
    ```yaml linenums="1" title="Maintain chart and metric state"
    grafana:
      persistence:
        enabled: true
        size: 10Gi
      sidecar:
        dashboards:
          enabled: true
          label: grafana_dashboard
          labelValue: 1
    prometheus:
      prometheusSpec:
        storageSpec:
          volumeClaimTemplate:
            spec:
              accessModes:
                - ReadWriteOnce
              resources:
                requests:
                  storage: 10Gi
    ```

    This `yaml` (added to a file called `prometheus-persistence.yaml`) can then be used to upgrade the `prometheus-stack`

    ```bash
    helm upgrade prometheus-stack prometheus-community/kube-prometheus-stack -f prometheus-persistence.yaml --wait --namespace <prometheus-stack-namespace> 
    ```
    

## 2. Expose JMX metrics

Follow [these instructions](../../../userguide/OPERATION/#expose-jmx-metrics) for details on how to enable and expose `JMX` for your product via a dedicated `Service`. 


## 3. Create a ServiceMonitor
    
Now that `JMX` metrics are exposed, we need a way of scraping them. This will be done using the [Prometheus custom resource definition; ServiceMonitor](https://github.com/prometheus-operator/prometheus-operator/tree/e1ed82c75b05d3579f2349369b6077b4c0b9b4f8#customresourcedefinitions){.external}. There are two ways this `ServiceMonitor` can be provisioned:

1. Automatically - using `helm upgrade`
2. Manually - deploy a new `serviceMonitor` CRD

=== "Automatically"

    Update the `serviceMonitor` stanza within the deployments `values.yaml` and perform a `helm upgrade`.
    
    ```yaml linenums="1" title="Automated deployment"
    serviceMonitor:
      create: true
      prometheusLabelSelector:
        release: prometheus-stack
      scrapeIntervalSeconds: 30
    ```

    !!!tip "`prometheusLabelSelector` identification"
        The `prometheusLabelSelector.release` value will be the release name used for provisioning Prometheus stack in [1. Install Prometheus stack](#1-install-prometheus-stackto). It can also be identified using the following:
        ```bash
        kubectl get prometheus/prometheus-stack-kube-prom-prometheus -n <prometheus-stack-namespace> -o jsonpath={'.spec.serviceMonitorSelector.matchLabels'}
        ```

    Now perform an upgrade using the updated `values.yaml`:
    ```bash
    helm upgrade confluence atlassian-data-center/confluence -f values.yaml --wait --namespace <dc-product-namespace>
    ```

=== "Manually"

    Alternatively you can manually provision the `serviceMontitor` by creating a new file called `serviceMonitor.yaml` with the following:
    
    ```yaml linenums="1" title="Manual deployment"
    apiVersion: monitoring.coreos.com/v1
    kind: ServiceMonitor
    metadata:
      name: confluence
      labels:
        release: prometheus-stack
    spec:
      endpoints:
      - interval: 15s
        path: /metrics
        port: jmx
        scheme: http
      namespaceSelector:
        any: true
      selector:
        matchLabels:
          app.kubernetes.io/name: confluence
    ```

    !!!tip "Particular values to be aware of"
        The above example assumes that:

        * the DC Helm release name is `confluence`
        * `labels.release` is `prometheus-stack` (release name used for provisioning Prometheus stack in [1. Install Prometheus stack](#1-install-prometheus-stack).
    
    Now provision the `serviceMonitor`:
    ```bash
    kubectl apply -f servicemonitor.yaml
    ```

## 4. Expose & confirm Prometheus service
Out of the box Prometheus services are not exposed, the simplest way to do this is to forward the service port (replace pod name with an actual Prometheus pod name):
```
kubectl port-forward prometheus-prometheus-stack-kube-prom-prometheus-0 9090:9090 -n <prometheus-stack-namespace>
```

Navigate to the URL `http://localhost:9090` in your browser and then select; **Status -> Targets**. You should be able to see your product pods as targets.


## 5. Access Grafana

To access Grafana, run (replace pod name with an actual Grafana pod name):

```
kubectl port-forward prometheus-stack-grafana-57dc5589b-2wh98 3000:3000 -n <prometheus-stack-namespace>
```

!!!info "Grafana details"

    * The name of the Grafana pod (`prometheus-stack-grafana-57dc5589b-2wh98`) may vary slightly between deploys
    * The default credentials are `admin:prom-operator` (these can be overridden when deploying kube-prometheus-stack). Alternatively, you may expose grafana service as a `LoadBalancer` `Service` type.

and go to `http://localhost:3000` in your browser.

You can then create a new [Dashboard](https://grafana.com/docs/grafana/latest/dashboards/) and use any of the exported metrics. To get the list of available metrics, run:

```
kubectl port-forward confluence-0 9999:9999 -n <dc-product-namespace> 
``` 

and go to `http://localhost:9999/metrics` in your local browser. You should see a list of available metrics that can be used.

## 6. Provision Pre-canned product dashboards

We provide a set of Grafana specific dashboards for each DC product. These can be provisioned by following the [Pre-canned product charts guide.](PRE_CANNED_CHARTS.md)

## Existing Standalone Prometheus Instance

!!!warning "JMX service security"
    By default, JMX services are created as ClusterIP types, i.e. they are not available outside the Kubernetes cluster.
    If your Prometheus instance, is deployed outside the Kubernetes cluster, you will need to expose the `JMX` service:
    ```
    monitoring:
      jmxExporterPortType: LoadBalancer
    ``` 
    Make sure you allow access only to Prometheus CIDR in the LB SecurityGroup (if you deploy to AWS) because
    JMX endpoints are not password protected. See: [jmx_exporter does not support authentication to the HTTP endpoint](https://github.com/prometheus/jmx_exporter/issues/687)

If you use a standalone Prometheus in Kubernetes, you need to manually create scrape configuration. See: [Monitor Jira with Prometheus and Grafana](https://confluence.atlassian.com/adminjiraserver/monitor-jira-with-prometheus-and-grafana-1155466715.html).

