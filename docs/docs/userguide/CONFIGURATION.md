# Configuration 

## :material-directions-fork: Ingress
In order to make the Atlassian product available from outside of the Kubernetes cluster, a suitable HTTP/HTTPS ingress controller needs to be installed. The standard Kubernetes Ingress resource is not flexible enough for our needs, so a third-party ingress controller and resource definition must be provided. The exact details of the Ingress will be highly site-specific. These Helm charts were tested using the [NGINX Ingress Controller](https://kubernetes.github.io/ingress-nginx/){.external}. We also provide [example instructions](../examples/ingress/CONTROLLERS.md) on how this controller can be installed and configured.

The charts themselves provide a template for Ingress resource rules to be utilised by the provisioned controller. These include all required annotations and optional TLS configuration for the NGINX Ingress Controller. 

Some key considerations to note when configuring the controller are:

!!!Ingress requirements
    * At a minimum, the ingress needs the ability to support long request timeouts, as well as session affinity (aka "sticky sessions").
    * The Ingress Resource provided as part of the Helm charts is geared toward the [NGINX Ingress Controller](https://kubernetes.github.io/ingress-nginx/){.external} and can be configured via the `ingress` stanza in the appropriate `values.yaml`. Some key aspects that can be configured include:
      
         * Usage of the NGINX Ingress Controller 
         * Ingress Controller annotations
         * The request max body size
         * The hostname of the ingress resource
    
    * When installed, with the provided [configuration](https://kubernetes.github.io/ingress-nginx/deploy/){.external}, the NGINX Ingress Controller will provision an internet-facing (see diagram below) load balancer on your behalf. The load balancer should either support the [Proxy Protocol](https://www.haproxy.org/download/1.8/doc/proxy-protocol.txt){.external} or allow for the forwarding of `X-Forwarded-*` headers. This ensures any backend redirects are done so over the correct protocol.
    * If the `X-Forwarded-*` headers are being used, then enable the [use-forwarded-headers](https://kubernetes.github.io/ingress-nginx/user-guide/nginx-configuration/configmap/#use-forwarded-headers){.external} option on the controllers `ConfigMap`. This ensures that these headers are appropriately passed on. 
    * The diagram below provides a high-level overview of how external requests are routed via an internet-facing load balancer to the correct service via Ingress.

![ingress-architecture](../assets/images/ingress.png "Request routing via Ingress")
    
!!!note "Traffic flow (diagram)"
    0. Inbound client request
    1. DNS routes request to appropriate LB
    2. LB forwards request to internal Ingress 
    3. Ingress controller performs traffic routing lookup via Ingress object(s)
    4. Ingress forwards request to appropriate service based on Ingress object routing rule
    5. Service forwards request to appropriate pod
    6. Pod handles request 

!!!info "Request body size"
    By default the maximum allowed size for the request body is set to `250MB`. If the size in a request exceeds the maximum size of the client request body, an `413` error will be returned to the client. The maximum request body can be configured by changing the value of `maxBodySize` in `values.yaml`.

## :material-folder-home: Volumes
The Data Center products make use of filesystem storage. Each DC node has its own `local-home` volume, and all nodes in the DC cluster share a single `shared-home` volume.

By default, the Helm charts will configure all of these volumes as ephemeral [emptyDir](https://kubernetes.io/docs/concepts/storage/volumes/#emptydir){.external} volumes. This makes it possible to install the charts without configuring any volume management, but comes with two big caveats:

1. Any data stored in the `local-home` or `shared-home` will be lost every time a pod starts. 
1. Whilst the data that is stored in `local-home` can generally be regenerated (e.g. from the database), this can be a very expensive process that sometimes requires manual intervention.

For these reasons, the default volume configuration of the Helm charts is suitable only for running a single DC pod for evaluation purposes. Proper volume management needs to be configured in order for the data to survive restarts, and for multi-pod DC clusters to operate correctly.

While you are free to configure your Kubernetes volume management in any way you wish, within the constraints imposed by the products, the recommended setup is to use Kubernetes [PersistentVolumes](https://kubernetes.io/docs/concepts/storage/persistent-volumes/){.external} and `PersistentVolumeClaims`. 

The `local-home` volume requires a `PersistentVolume` with [ReadWriteOnce (RWO)](https://kubernetes.io/docs/concepts/storage/persistent-volumes/#access-modes){.external} capability, and `shared-home` requires a `PersistentVolume` with [ReadWriteMany (RWX)](https://kubernetes.io/docs/concepts/storage/persistent-volumes/#access-modes){.external} capability. Typically, this will be an NFS volume provided as part of your infrastructure, but some public-cloud Kubernetes engines provide their own `RWX` volumes (e.g. [AWS EFS](https://aws.amazon.com/efs/){.external} and [Azure Files](https://docs.microsoft.com/en-us/azure/storage/files/storage-files-introduction){.external}). While this entails a higher upfront setup effort, it gives the best flexibility.

### Volumes configuration
By default, the charts will configure the `local-home` and `shared-home` values as follows:

```yaml
volumes:
  - name: local-home
    emptyDir: {}
  - name: shared-home
    emptyDir: {}
```

As explained above, this default configuration is suitable only for evaluation or testing purposes. Proper volume management needs to be configured.

In order to enable the persistence of data stored in these volumes, it is necessary
to replace these volumes with something else.

The recommended way is to enable the use of `PersistentVolume` and `PersistentVolumeClaim`
for both volumes, using your install-specific `values.yaml` file, for example:

```yaml
volumes:
  localHome:
    persistentVolumeClaim:
      create: true
  shared-home:
    persistentVolumeClaim:
      create: true
```

This will result in each pod in the `StatefulSet` creating a `local-home` `PersistentVolumeClaim`
of type `ReadWriteOnce`, and a single `PersistentVolumeClaim` of type `ReadWriteMany` being created for the `shared-home`.

For each `PersistentVolumeClaim` created by the chart, a suitable `PersistentVolume` needs to be made available prior 
to installation. These can be provisioned either statically or dynamically, using an 
auto-provisioner.

An alternative to `PersistentVolumeClaims` is to use inline volume definitions,
either for `local-home` or `shared-home` (or both), for example:

```yaml
volumes:
  localHome:
    customVolume:
      hostPath:
        path: /path/to/my/data
  shared-home:
    customVolume:
      nfs:
        server: mynfsserver
        path: /export/path
```

Generally, any valid Kubernetes volume resource definition can be substituted
here. However, as mentioned previously, externalising the volume definitions
using `PersistentVolumes` is the strongly recommended approach.

### Volumes examples

1. Bitbucket needs a dedicated NFS server providing persistence for a shared home. Prior to installing the Helm chart, a suitable NFS shared storage solution must be provisioned. The exact details of this resource will be highly site-specific, but you can use this example as a guide: [Implementation of an NFS Server for Bitbucket](../examples/storage/nfs/NFS.md).
2. We have an example detailing how an existing EFS filesystem can be created and consumed using static provisioning: [Shared storage - utilizing AWS EFS-backed filesystem](../examples/storage/aws/SHARED_STORAGE.md).
3. You can also refer to an example on how a Kubernetes cluster and helm deployment can be configured to utilize AWS EBS backed volumes: [Local storage - utilizing AWS EBS-backed volumes](../examples/storage/aws/LOCAL_STORAGE.md).

### Additional volumes

In addition to the `local-home` and `shared-home` volumes that are always
attached to the product pods, you can attach your own volumes for your own
purposes, and mount them into the product container.  Use the `additional`
(under `volumes`) and `additionalVolumeMounts` values to both attach the volumes
and mount them in to the product container.

This might be useful if, for example, you have a custom plugin that requires its 
own filesystem storage.

Example:

```yaml
jira:
   additionalVolumeMounts:
      - volumeName: my-volume
        mountPath: /path/to/mount

volumes:
  additional:
    - name: my-volume
      persistentVolumeClaim:
         claimName: my-volume-claim
```

## :material-database: Database connectivity

The products need to be supplied with the information they need to connect to the 
database service. Configuration for each product is mostly the same, with some small differences.

### `database.url`

All products require the JDBC URL of the database. The format if this URL depends
on the JDBC driver being used, but some examples are:

| Vendor        | JDBC driver class                               | Example JDBC URL                                        |   
|---            |---                                              |---                                                      |
| PostgreSQL    | `org.postgresql.Driver`                         | `jdbc:postgresql://<dbhost>:5432/<dbname>`              |   
| MySQL         | `com.mysql.jdbc.Driver`                         | `jdbc:mysql://<dbhost>/<dbname>`                        |   
| SQL Server    | `com.microsoft.sqlserver.jdbc.SQLServerDriver`  | `jdbc:sqlserver://<dbhost>:1433;databaseName=<dbname>`  |   
| Oracle        | `oracle.jdbc.OracleDriver`                      | `jdbc:oracle:thin:@<dbhost>:1521:<SID>`                 |

!!!info "Database creation"

    The Atlassian product doesn't automatically create the database,`<dbname>`, in the `JDBC URL`, so you need to manually create a user and database for the used database instance. Details on how to create product-specific databases can be found below:
   
    === "Jira"

        [Connect Jira to an external database](https://confluence.atlassian.com/adminjiraserver/connecting-jira-applications-to-a-database-938846850.html){.external}

    === "Confluence"
    
        [Connect Confluence to an external database](https://confluence.atlassian.com/doc/database-configuration-159764.html#DatabaseConfiguration-Databasesetupsetup){.external}
    
    === "Bitbucket"
    
        [Connect Bitbucket to an external database](https://confluence.atlassian.com/bitbucketserver/connect-bitbucket-to-an-external-database-776640378.html){.external}

    === "Bamboo"

        [Connect Bamboo to an external database](https://confluence.atlassian.com/bamboo/connecting-bamboo-to-an-external-database-289276815.html){.external}

    === "Crowd"

        [Connect Crowd to an external database](https://confluence.atlassian.com/crowd/connecting-crowd-to-a-database-4030904.html){.external}


### `database.driver`

Jira and Bitbucket require the JDBC driver class to be specified (Confluence and Bamboo will 
autoselect this based on the `database.type` value, see below). The JDBC driver must 
correspond to the JDBC URL used; see the table above for example driver classes.

Note that the products only ship with certain JDBC drivers installed, depending
on the license conditions of those drivers.

!!!warning "Non-bundled DB drivers"
    MySQL and Oracle database drivers are not shipped with the products due to licensing restrictions.
    You will need to provide [`additionalLibraries` configuration](#additional-libraries-plugins).

### `database.type`

Jira, Confluence and Bamboo all require this value to be specified, this declares the
database engine to be used. The acceptable values for this include:

| Vendor        |  Jira                 | Confluence     | Bamboo        |
|---------------|-----------------------|---------------|---------------|
| PostgreSQL    | `postgres72`          | `postgresql`  | `postgresql`  |
| MySQL         | `mysql57` / `mysql8`  | `mysql`       | `mysql`       |
| SQL Server    | `mssql`               | `mssql`       | `mssql`       |
| Oracle        | `oracle10g`           | `oracle`      | `oracle12c`   |

### `database.credentials`

All products can have their database connectivity and credentials specified either
interactively during first-time setup, or automatically by specifying certain configuration
via Kubernetes.

Depending on the product, the `database.type`, `database.url` and `database.driver` chart values
can be provided. In addition, the database username and password can be provided via a [Kubernetes secret](https://kubernetes.io/docs/concepts/configuration/secret/){.external},
with the secret name specified with the `database.credentials.secretName` chart value. 
When all the required information is provided in this way, the database connectivity configuration screen
will be bypassed during product setup.

## :fontawesome-solid-user-tag: Namespace

The Helm charts are not opinionated whether they have a [Kubernetes namespace](https://kubernetes.io/docs/concepts/overview/working-with-objects/namespaces/){.external} to themselves. 
If you wish, you can run multiple Helm releases of the same product in the same namespace.

## :fontawesome-solid-network-wired: Clustering
By default, the Helm charts will not configure the products for Data Center clustering. In order to enable clustering, the `enabled` property for clustering must be set to `true`.

!!!note ""

    === "Jira"

        ```yaml
        jira:
          clustering:
            enabled: true
        ```

    === "Confluence"

        ```yaml
        confluence:
          clustering:
            enabled: true
        ```

    === "Bitbucket"

        ```yaml
        bitbucket:
          clustering:
            enabled: true
        ```

    === "Bamboo"

        Because of the limitations outlined under [Bamboo and clustering](../troubleshooting/LIMITATIONS.md#cluster-size) the `clustering` stanza is not available as a configurable property in the Bamboo `values.yaml`.
        

In addition, the `shared-home` volume must be correctly configured as a [ReadWriteMany (RWX)](https://kubernetes.io/docs/concepts/storage/persistent-volumes/#access-modes){.external} filesystem (e.g. NFS, [AWS EFS](https://aws.amazon.com/efs/){.external} and [Azure Files](https://docs.microsoft.com/en-us/azure/storage/files/storage-files-introduction){.external})

## :material-book-cog: Additional libraries & plugins

The products' Docker images contain the default set of bundled libraries and plugins. 
Additional libraries and plugins can be mounted into the product containers during
the Helm install. One such use case for this is mounting `JDBC` drivers that are not 
shipped with the products' by default.

To make use of this mechanism, the additional files need to be available as part
of a Kubernetes volume. Options here include putting them into the `shared-home` volume 
that's [required as part of the prerequisites](PREREQUISITES.md#configure-a-shared-home-volume). Alternatively, you can 
create a custom `PersistenVolume` for them, as long as it has `ReadOnlyMany` capability. 

!!!info "Custom volumes for loading libraries"

    If you're not using the `shared-home` volume, then you can declare your own custom 
    volume, by following the [Additional volumes](#additional-volumes) section above. 

You could even store the files as a `ConfigMap` that gets mounted as a volume, but
you're likely to run into file size limitations there.

Assuming that the existing `shared-home` volume is used for this, then the only 
configuration required is to specify the `additionalLibraries` in your `values.yaml` 
file, e.g.

```yaml
jira:
  additionalLibraries: 
    - volumeName: shared-home
      subDirectory: mylibs
      fileName: lib1.jar
    - volumeName: shared-home
      subDirectory: mylibs
      fileName: lib2.jar
```    

This will mount the `lib1.jar` and `lib2.jar` from the `mylibs` sub-directory from `shared-home` 
into the appropriate place in the container.

Similarly, you can use `additionalBundledPlugins` to load product plugins into the
container. 

!!!info "System plugin"

    Plugins installed via this method will appear as system plugins
    rather than user plugins. An alternative to this method is to install the
    plugins via "Manage Apps" in the product system administration UI.

For more details on the above, and how 3rd party libraries can be supplied to a Pod see the example [External libraries and plugins](../examples/external_libraries/EXTERNAL_LIBS.md)

## :octicons-cpu-16: CPU and memory requests

The Helm charts allow you to specify container-level CPU and memory [resource requests and limits](https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/#requests-and-limits){.external} e.g.

```yaml
jira:
  resources:
    container:
      requests:
        cpu: "4"
        memory: "8G"
``` 

!!!tip ""

    By default, the Helm Charts have no container-level resource limits, however there are default requests that are set.

Specifying these values is fine for CPU limits/requests, but for memory 
resources it is also necessary to configure the JVM's memory limits. 
By default, the JVM maximum heap size is set to 1 GB, so if you increase 
(or decrease) the container memory resources as above, you also need to change
the JVM's max heap size, otherwise the JVM won't take advantage of the extra 
available memory (or it'll crash if there isn't enough).

You specify the JVM memory limits like this:

```yaml
jira:
  resources:
    jvm:
      maxHeap: "8g"
```

Another difficulty for specifying memory resources is that the JVM requires
additional overheads over and above the max heap size, and the container resources 
need to take account of that.  A safe rule-of-thumb would be for the container
to request 2x the value of the max heap for the JVM.

This requirement to configure both the container memory and JVM heap will
hopefully be removed.

You can read more about [resource scaling](../userguide/resource_management/RESOURCE_SCALING.md#vertical-scaling-adding-resources) and [resource requests and limits](../userguide/resource_management/REQUESTS_AND_LIMITS.md).


## :octicons-container-16: Additional containers

The Helm charts allow you to add your own `container` and [initContainer](https://kubernetes.io/docs/concepts/workloads/pods/init-containers/){.external} entries to the product pods. Use the `additionalContainers` and `additionalInitContainers` stanzas within the `values.yaml` for this. One use-case for an additional container would be to attach a sidecar container to the product pods.

## :material-checkbox-multiple-marked-outline: Additional options

The Helm charts also allow you to specify: 

* [`additionalLabels`](https://kubernetes.io/docs/concepts/overview/working-with-objects/labels/){.external}
* [`tolerations`](https://kubernetes.io/docs/concepts/scheduling-eviction/taint-and-toleration/){.external}, 
* [`nodeSelectors`](https://kubernetes.io/docs/concepts/scheduling-eviction/assign-pod-node/#nodeselector){.external}  
* [`affinities`](https://kubernetes.io/docs/concepts/scheduling-eviction/assign-pod-node/#affinity-and-anti-affinity){.external}. 
  
These are standard Kubernetes structures that will be included in the pods.
