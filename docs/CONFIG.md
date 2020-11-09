## Namespace

The Helm charts are not opinionated as to whether they have a Kubernetes namespace to themselves. 
If you wish, you can run multiple Helm releases of the same product in the same namespace.

## Service account

Like any other Kubernetes application, the pods in the Data Center product deployments
use a Kubernetes service account. Unless otherwise configured, this will use the
default service account for the namespace.

In order to use a different service account, override the chart value `serviceAccountName`.

Note that for Jira, there are no special permissions required for this service account, but both Confluence and
Bitbucket use Hazelcast for peer discovery, which entails querying the Kubernetes
API. The account will need `get`/`list` permission for `endpoints`, `nodes` and `pods` for the current namespace.

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

The products require that there is a Kubernetes Secret present in the namespace,
that contains the username and password which the product should use to connect
to the database.

The default name for this secret, as well as the keys in the secret that contain
the credentials, are defined in the charts' README, but they can all be changed 
by overriding those settings.

## Additional libraries & plugins

The products' Docker images contain the default set of bundled libraries and plugins. 
Additional libraries and plugins can be mounted into the product containers during
the Helm install. This can be useful for bundling extra JDBC drivers, for example,
or additional bundled plugins that you need for your installation.

In order for this to work, the additional JAR files need to be available as part
of a Kubernetes volume. Options here include putting them into the shared-home volume 
that you already need to have as part of the insallation. Alternatively, you can 
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

## Additional labels, tolerations, node selectors, affinity

The Helm charts also allow you to specify `additionalLabls`, `tolerations`, 
`nodeSelectors` and `affinities`. These are standard Kubernetes structures 
that will be included in the pods. 