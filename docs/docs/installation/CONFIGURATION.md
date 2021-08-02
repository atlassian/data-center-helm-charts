# Configuration 

## Ingress
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


## Volumes
The Data Center products make use of filesystem storage. Each DC node has its own "local-home" volume, and all nodes in the DC cluster share a single "shared-home" volume.

By default, the Helm charts will configure all of these volumes as ephemeral "emptyDir" volumes. This makes it possible to install the charts without configuring any volume management, but comes with two big caveats:

Any data stored in the local-home or shared-home will be lost every time a pod starts. Whilst the data that is stored in local-home can generally be regenerated (e.g. from the database), this can be very a very expensive process that sometimes required manual intervention.

The shared-home volume will not actually be shared between multiple nodes in the DC cluster. Whilst this may not immediately prevent scaling the DC cluster up to multiple nodes, certain critical functionality of the products relies on the shared filesystem working as expected.

For these reasons, the default volume configuration of the Helm charts is suitable only for running a single DC node for evaluation purposes. Proper volume management needs to be configured in order for the data to survive restarts, and for multi-node DC clusters to operate correctly.

While you are free to configure your Kubernetes volume management in any way you wish, within the constraints imposed by the products, the recommended setup is to use Kubernetes `PersistentVolumes` and `PersistentVolumeClaims`. The `local-home` volume requires a `PersistentVolume` with `ReadWriteOnce (RWO)` capability, and `shared-home` requires a `PersistentVolume` with `ReadWriteMany (RWX)` capability. Typically, this will be an NFS volume provided as part of your infrastructure, but some public-cloud Kubernetes engines provide their own RWX volumes (e.g. AzureFile, ElasticFileStore). While this entails a higher upfront setup effort, it gives the best flexibility.

### Volumes configuration
By default, the charts will configure the `local-home` and `shared-home` values as follows:

```yaml
volumes:
  - name: local-home
    emptyDir: {}
  - name: shared-home
    emptyDir: {}
```

As explained above, this default configuration is suitable only for testing purposes. Proper volume management needs to be configured.

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

## Additional volumes

In additional to the `local-home` and `shared-home` volumes that are always
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

## Database connectivity

The products need to be supplied with the information they need to connect to the 
database service. Configuration for each product is mostly the same, with some small differences.

### `database.jdbcUrl`

All products require the JDBC URL of the database. The format if this URL depends
on the JDBC driver being used, but some examples are:

| Vendor | JDBC driver class  | Example JDBC URL  |   
|---|---|---|
| Postgres | `org.postgresql.Driver` | `jdbc:postgresql://<dbhost>:5432/<dbname>` |   
| MySQL | `com.mysql.jdbc.Driver` | `jdbc:mysql://<dbhost>/<dbname>` |   
| SQL Server | `com.microsoft.sqlserver.jdbc.SQLServerDriver` | `jdbc:sqlserver://<dbhost>:1433;databaseName=<dbname>` |   
| Oracle | `oracle.jdbc.OracleDriver` | `jdbc:oracle:thin:@<dbhost>:1521:<SID>` |

> **NOTE:** With regards, `<dbname>` in the `JDBC URL`, this database is not automatically created by the Atlassian product itself, as such a user and database must be manually created for the DB instance used. Details on how to create product specific DB's can be found below:
>  * [Jira](https://confluence.atlassian.com/adminjiraserver/connecting-jira-applications-to-a-database-938846850.html){.external}
>  * [Confluence](https://confluence.atlassian.com/doc/database-configuration-159764.html#DatabaseConfiguration-Databasesetupsetup){.external}
>  * [Bitbucket](https://confluence.atlassian.com/bitbucketserver/connect-bitbucket-to-an-external-database-776640378.html){.external}
>  * [Crowd](https://confluence.atlassian.com/crowd/connecting-crowd-to-a-database-4030904.html){.external}

### `database.driver`

Jira and Bitbucket require the JDBC driver class to be specified (Confluence will 
autoselect this based on the `database.type` value, see below). The JDBC driver must 
correspond to the JDBC URL used; see the table above for example driver classes.

Note that the products only ship with certain JDBC drivers installed, depending
on the license conditions of those drivers.

In order to use JDBC drivers that are not shipped with the product (e.g. MySQL 
and Oracle), you need to follow the steps to introduce additional libraries into the
installation (see below).

### `database.type`

Jira and Confluence both require this value to be specified, this declares the
database engine to be used. The acceptable values for this include:

| Vendor | Jira | Confluence  |   
|---|---|---|
| Postgres | `postgres72` | `postgresql` |   
| MySQL | `mysql57` / `mysql8` | `mysql` |   
| SQL Server | `mssql` | `mssql` |   
| Oracle | `oracle10g` | `oracle` |   

### Database credentials

All products can have their database connectivity and credentials specified either
interactively during first-time setup, or automatically by specifying certain configuration
via Kubernetes.

Depending on the product, the `database.type`, `database.url` and `database.driver` chart values
can be provided. In addition, the database username and password can be provided via a Kubernetes secret,
with the secret name specified with the `database.credentials.secretName` chart value. 
When all the required information is provided in this way, the database connectivity configuration screen
will be bypassed during product setup. 

## Namespace

The Helm charts are not opinionated as to whether they have a Kubernetes namespace to themselves. 
If you wish, you can run multiple Helm releases of the same product in the same namespace.

## Clustering
By default, the Helm charts are will not configure the products for Data Center clustering. 

In order to enable clustering, the appropriate chart value must be set to `true`.

=== "Jira"

    `jira.clustering.enabled`

=== "Confluence"

    `confluence.clustering.enabled`

=== "Bitbucket"

    `bitbucket.clustering.enabled`

In addition, the `shared-home` volume must be correctly configured as a read-write shared filesystem (e.g. NFS,
AWS EFS, Azure Files)

## Additional libraries & plugins

The products' Docker images contain the default set of bundled libraries and plugins. 
Additional libraries and plugins can be mounted into the product containers during
the Helm install. This can be useful for bundling extra JDBC drivers, for example,
or additional bundled plugins that you need for your installation.

In order for this to work, the additional JAR files need to be available as part
of a Kubernetes volume. Options here include putting them into the shared-home volume 
that you already need to have as part of the installation. Alternatively, you can 
create a new PV for them, as long as it has `ReadOnlyMany` capability. You
could even store the files as a `ConfigMap` that gets mounted as a volume, but
you're likely to run into file size limitations there.

Assuming that the existing `shared-home` volume is used for this, then the only 
configuration required is to specify the `additionalLibraries` and/or 
`additionalBundledPlugins` structures in your `values.yaml` file, e.g.

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

This will mount the `lib1.jar` and `lib2.jar` in the appropriate place in
the container.

Similarly, use `additionalBundledPlugins` to load product plugins into the
container. Note: Plugins installed via this method will appear as system plugins
rather than user plugins. An alternative to this method is to install the
plugins via "Manage Apps" in the product system administration UI.

If you're not using the `shared-home` volume, then you can declare your own custom 
volume in the "Additional Volumes" section below, then declare the libraries as above
(but with your custom volume name).

## Request body size
By default the maximum allowed size for the request body is set to 250MB. If the size in a request exceeds the maximum 
size of the client request body, an 413 error will be returned to the client. If the maximum request body can be 
configured by changing the value of `maxBodySize` in 'values.yaml'.

## Resources

The Helm charts allow you to specify container-level CPU and memory resources,
using standard Kubernetes `limit` and `request` structures, e.g.

```yaml
jira:
  resources:
    container:
      requests:
        cpu: "4"
        memory: "8G"
``` 

By default, no container-level resource limits or requests are set.

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

You can read more about [resource scaling](../operations/resource_management/RESOURCE_SCALING.md#vertical-scaling-adding-resources) and [resource requests and limits](../operations/resource_management/REQUESTS_AND_LIMITS.md).


## Additional containers

The Helm charts allow you to add your own `container` and `initContainer` 
entries to the product pods.  Use the values `additionalContainers` and
`additionalInitContainers` for this.

One use-case for an additional container would be to attach a sidecar container 
to the product pods.

## Additional labels, toleration's, node selectors, affinity

The Helm charts also allow you to specify `additionalLabls`, `tolerations`, 
`nodeSelectors` and `affinities`. These are standard Kubernetes structures 
that will be included in the pods. 
