# Operation
Once you have [installed your product](../userguide/INSTALLATION.md), use this document if you want to scale your product, update your product, or see what examples we have.

## Expose JMX Metrics

When `monitoring.exposeJmxMetrics` is enabled, [JMX exporter](https://github.com/prometheus/jmx_exporter){.external} runs as javaagent to expose http server and serve metrics of a local JVM. In other words, JMX MBeans (if enabled in the product) are exposed and available to be scraped by [Prometheus](https://prometheus.io/){.external}.

[JMX exporter jar](https://github.com/prometheus/jmx_exporter){.external} isn't available in products dependencies or container images, that is why when `monitoring.exposeJmxMetrics`
is enabled there are 2 ways to get it:

* copy from an init container ([bitnami/jmx-exporter DockerHub image](https://hub.docker.com/r/bitnami/jmx-exporter){.external}) - the **default** option in values.yaml which works out of the box
* manually download from [JMX exporter GitHub releases page](https://github.com/prometheus/jmx_exporter/tags){.external} and copy to shared-home or mount as a secret


=== "Init Container"
    ### Enable JMX Monitoring: The Default Method

    You can expose JMX metrics by either passing `--set monitoring.exposeJmxMetrics=true` to your `helm install/upgrade command` or change the default value
    in your `values.yaml` file:

    ```yaml
    monitoring:
      exposeJmxMetrics: true
    ```

=== "Manually Copy the Jar"
    ### Enable JMX Monitoring: Manually Copy the Jar

    If you don't want to have an additional container which will slightly increase pod startup time, you can manually download the exporter jar, override its location and disable an init container:

    First, download the jar:

    ```shell
    # check the latest version from https://github.com/prometheus/jmx_exporter/releases
    wget -P . https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/0.18.0/jmx_prometheus_javaagent-0.18.0.jar
    ```

    Then, copy it to shared-home. The example below assumes that Confluence is deployed to `atlassian` namespace, and `volumes.sharedHome.mountPath` uses the default value:

    ```
    kubectl cp \
      jmx_prometheus_javaagent-0.18.0.jar \
      atlassian/confluence-0:/var/atlassian/application-data/shared-home/jmx_prometheus_javaagent-0.18.0.jar
    ```

    Disable an init container and set a custom jar location in the `values.yaml` file:

    ```
    monitoring:
      exposeJmxMetrics: true
      fetchJmxExporterJar: false
      jmxExporterCustomJarLocation: /var/atlassian/application-data/shared-home/jmx_prometheus_javaagent-0.18.0.jar
    ```

Run `helm upgrade` to apply changes. Once done, you can verify metrics is available by running:

```
kubectl port-forward confluence-0 9999:9999 -n atlassian 
``` 

Go to `http://localhost:9999/metrics` in your local browser to verify metrics availability.

!!!warning "JMX service security"
    By default, JMX services are created as ClusterIP types, i.e. they are not available outside the Kubernetes cluster.
    Because the metrics endpoint isn't password protected, make sure you protected with SecurityGroup rules (if in AWS)
    when exposing it as a LoadBalancer if required:
    ```
    monitoring:
      jmxExporterPortType: LoadBalancer
    ``` 

### Scrape Metrics With Prometheus

See: [Prometheus Monitoring](../examples/monitoring/PROMETHEUS.md)


## Managing resources

You can scale your application by [adding additonal pods](resource_management/RESOURCE_SCALING.md) or by [managing available resources with requests and limits](resource_management/RESOURCE_SCALING.md).

## Upgrading application

### Kubernetes update strategies
Kubernetes provides two strategies to update applications managed by `statefulset` controllers:

#### Rolling update
The pods will be upgraded one by one until all pods run containers with the updated template. The upgrade is managed by 
Kubernetes and the user has limited control during the upgrade process, after having modified the template. This is the default 
upgrade strategy in Kubernetes. 

To perform a canary or multi-phase upgrade, a partition can be defined on the cluster and Kubernetes will upgrade just 
the nodes in that partition. 

The default implementation is based on *RollingUpdate* strategy with no *partition* defined. 

#### OnDelete strategy
In this strategy users select the pod to upgrade by deleting it, and Kubernetes will replace it by creating a new pod
 based on the updated template. To select this strategy the following should be replaced with the current 
 implementation of `updateStrategy` in the `statefulset` spec:

```yaml
  updateStrategy:
    type: OnDelete
```  

### Upgrade

* To learn about upgrading the Helm charts see [Helm chart upgrade](upgrades/HELM_CHART_UPGRADE.md).  
* To learn about upgrading the products without upgrading the Helm charts see [Products upgrade](upgrades/PRODUCTS_UPGRADE.md).


## Examples
### Logging
#### How to deploy an EFK stack to Kubernetes
There are different methods to deploy an EFK stack. We provide two deployment methods, the first is deploying EFK locally on Kubernetes, and the second is using managed Elasticsearch outside the Kubernetes cluster. Please refer to [Logging in Kubernetes](../examples/logging/efk/EFK.md).
