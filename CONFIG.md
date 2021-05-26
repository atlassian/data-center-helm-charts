## Namespace

The Helm charts are not opinionated as to whether they have a Kubernetes namespace to themselves. 
If you wish, you can run multiple Helm releases of the same product in the same namespace.

## Volumes

By default, the charts will configure the `local-home` and `shared-home` volues as follows:

```yaml
volumes:
  - name: local-home
    emptyDir: {}
  - name: shared-home
    emptyDir: {}
```

In order to enable the persistence of data stored in these volumes, it is necessary
to replace these volumes with something else.

The recommended way is to enable the use of PersistentVolume and PersistentVolumeClaim
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

This will result in each pod in the StatefulSet creating a `local-home` `PersistentVolumeClaim`
of type `ReadWriteOnce`, and a single PVC of type `ReadWriteMany` being created for the shared-home.

For each PVC created by the chart, a suitable `PersistentVolume` needs to be made available prior 
to installation. These can be provisioned either statically or dynamically, using an 
auto-provisioner.

An alternative to PersistentVolumeClaims is to use inline volume definitions,
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
````

Generally, any valid Kubernetes volume resource definition can be substituted
here. However, as mentioned previously, externalising the volume definitions
using PersistentVolumes is the strongly recommended approach.

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

Jira and Confluence both require this value to be specified, which declares the
"flavour" of database system being used. The acceptable values for this include:

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

## Clustering
By default, the Helm charts are will not configure the products for Data Center clustering. 

In order to enable clustering, the appropriate chart value must be set to `true` (`jira.clustering.enabled`, 
`confluence.clustering.enabled` or `bitbucket.clustering.enabled`) 

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
container.

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
hopefully be removed by [SCALE-37](https://jira.atlassian.com/browse/SCALE-37).

## Additional volumes

In additional to the `local-home` and `shared-home` volumes that are
always attached to the product pods, you can attach your own volumes for
your own purposes, and mount them into the product container. 
Use the `additionalVolumes` and `additionalVolumeMounts` values to both attach 
the volumes and mount them in to the product container.

This might be useful if, for example, you have a custom plugin that requires its 
own filesystem storage.

Example:

```yaml
jira:
   additionalVolumeMounts:
      - volumeName: my-volume
        mountPath: /path/to/mount

additionalVolumes:
  - name: my-volume
    persistentVolumeClaim:
       claimName: my-volume-claim
```

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