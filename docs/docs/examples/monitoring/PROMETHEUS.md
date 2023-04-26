# Prometheus Monitoring

## Prometheus Operator

Install [kube-prometheus-stack](https://github.com/prometheus-community/helm-charts/tree/main/charts/kube-prometheus-stack) Helm chart (call your release `prometheus-stack`).

Once [JMX metrics are exposed](../../../userguide/OPERATION/#monitoring) in your product Helm chart, Helm will create a dedicated `JMX` service, and you can now create a `ServiceMonitor` object so that Prometheus starts scraping metrics:


```yaml
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

The above example assumes that:

* the DC Helm release name is `confluence`
* Prometheus custom resource is watching service monitors using the following labelSelector:
  ```
  serviceMonitorSelector:
    matchLabels:
      release: prometheus-stack
  ```
  which is the case if [kube-prometheus-stack](https://github.com/prometheus-community/helm-charts/tree/main/charts/kube-prometheus-stack) release name is `prometheus-stack`


Out of the box, Prometheus and Grafana services are not exposed, and the simplest way to access them is to forward the service port (replace pod name with an actual Prometheus pod name):


```
kubectl port-forward prometheus-prometheus-kube-prometheus-prometheus-0 9090:9090 -n <prometheus-stack-namespace>
```
After `ServiceMonitor` is created, verify it is in Prometheus targets. Go to `http://localhost:9090` in your local browser, navigate to **Status -> Targets**. You should be able to see your product pods as targets.


To access Grafana, run (replace pod name with an actual Grafana pod name):

```
kubectl port-forward prometheus-grafana-656c669c85-g5kb4 3000:3000 -n <prometheus-stack-namespace>
```

and go to `http://localhost:3000` in your browser. The default credentials are `admin:prom-operator` (can be overridden when deploying kube-prometheus-stack).
Alternatively, you may expose grafana service as a LoadBalancer.


You can then create a new [Dashboard](https://grafana.com/docs/grafana/latest/dashboards/) and use any of the exported metrics. To get the list of available metrics, run:

```
kubectl port-forward confluence-0 9999:9999 -n atlassian 
``` 

and go to `http://localhost:9999/metrics` in your local browser.

## Existing Standalone Prometheus Instance

!!!warning "JMX service security"
    By default, JMX services are created as ClusterIP types, i.e. they are not available outside the Kubernetes cluster.
    If your Prometheus instance, is deployed outside the Kubernetes cluster, you will need to expose the jmx service:
    ```
    monitoring:
      jmxExporterPortType: LoadBalancer
    ``` 
    Make sure you allow access only to Prometheus CIDR in the LB SecurityGroup (if you deploy to AWS) because
    JMX endpoints are not password protected. See: [jmx_exporter does not support authentication to the HTTP endpoint](https://github.com/prometheus/jmx_exporter/issues/687)

If you use a standalone Prometheus in Kubernetes, you need to manually create scrape configuration. See: [Monitor Jira with Prometheus and Grafana](https://confluence.atlassian.com/adminjiraserver/monitor-jira-with-prometheus-and-grafana-1155466715.html).

