# ![Atlassian Jira Software](https://wac-cdn.atlassian.com/dam/jcr:826c97dc-1f5c-4955-bfcc-ea17d6b0c095/jira%20software-icon-gradient-blue.svg?cdnVersion=492){: style="height:35px;width:35px"}![Atlassian Jira Service Management](https://wac-cdn.atlassian.com/dam/jcr:8e0905be-0ee7-4652-ba3a-4e3db1143969/jira%20service%20desk-icon-gradient-blue.svg?cdnVersion=492){: style="height:35px;width:35px"}![Atlassian Jira Core](https://wac-cdn.atlassian.com/dam/jcr:f89f1ce5-60f1-47c2-b9f5-657de4940d31/jira%20core-icon-gradient-blue.svg?cdnVersion=492){: style="height:35px;width:35px"} Jira

## Overview

Jira Software Data Center helps the world’s best agile teams plan, track, and release great software at scale.

* Check out [atlassian/jira-software](http://hub.docker.com/r/atlassian/jira-software/) on Docker Hub
* Learn more about Jira Software: [https://www.atlassian.com/software/jira](https://www.atlassian.com/software/jira)

Jira Service Management Data Center is an enterprise ITSM solution that offers high availability, meeting your security and compliance needs so no request goes unresolved.

* Check out [atlassian/jira-servicemanagement](http://hub.docker.com/r/atlassian/jira-servicemanagement/) on Docker Hub
* Learn more about Jira Service Management: [https://www.atlassian.com/software/jira/service-management](https://www.atlassian.com/software/jira/service-management)

Jira Core is a project and task management solution built for business teams.

* Check out [atlassian/jira-core](http://hub.docker.com/r/atlassian/jira-core/) on Docker Hub
* Learn more about Jira Core: [https://www.atlassian.com/software/jira/core](https://www.atlassian.com/software/jira/core)

This Docker container makes it easy to get an instance of Jira Software, Service Management or Core up and running.

Note: Jira Software will be referenced in the examples provided.

**Use docker version >= 20.10.10**

## Quick Start

For the `JIRA_HOME` directory that is used to store application data (amongst
other things) we recommend mounting a host directory as a [data
volume](https://docs.docker.com/engine/tutorials/dockervolumes/#/data-volumes),
or via a named volume.

Additionally, if running Jira in Data Center mode it is required that a shared
filesystem is mounted. The mount point (inside the container) can be configured
with `JIRA_SHARED_HOME`.

To get started you can use a data volume, or named volumes. In this example
we'll use named volumes.

```shell
docker volume create --name jiraVolume
docker run -v jiraVolume:/var/atlassian/application-data/jira --name="jira" -d -p 8080:8080 atlassian/jira-software
```

!!! success "Jira is now available on [http://localhost:8080](http://localhost:8080)."

Please ensure your container has the necessary resources allocated to it. We
recommend 2GiB of memory allocated to accommodate the application server. See
[System Requirements](https://confluence.atlassian.com/adminjiraserver071/jira-applications-installation-requirements-802592164.html)
for further information.

???+ tip "If you are using `docker-machine` on Mac OS X, please use `open http://$(docker-machine ip default):8080` instead."

## Configuring Jira

This Docker image is intended to be configured from its environment; the
provided information is used to generate the application configuration files
from templates. This allows containers to be repeatably created and destroyed
on-the-fly, as required in advanced cluster configurations. Most aspects of the
deployment can be configured in this manner; the necessary environment variables
are documented below. However, if your particular deployment scenario is not
covered by these settings, it is possible to override the provided templates
with your own; see the section [Advanced Configuration](#advanced-configuration) below.

### Verbose container entrypoint logging

During the startup process of the container, various operations and checks are performed to ensure that the application
is configured correctly and ready to run. To help in troubleshooting and to provide transparency into this process, you
can enable verbose logging. The `VERBOSE_LOGS` environment variable enables detailed debug messages to the container's
log, offering insights into the actions performed by the entrypoint script.

* `VERBOSE_LOGS` (default: false)

  Set to `true` to enable detailed debug messages during the container initialization.

### Memory / Heap Size

If you need to override Jira's default memory allocation, you can control the minimum heap (Xms) and maximum heap (Xmx) via the below environment variables.

* `JVM_MINIMUM_MEMORY` (default: 384m)

   The minimum heap size of the JVM

* `JVM_MAXIMUM_MEMORY` (default: 768m)

   The maximum heap size of the JVM

* `JVM_RESERVED_CODE_CACHE_SIZE` (default: 512m)

    The reserved code cache size of the JVM

### Reverse Proxy Settings

If Jira is run behind a reverse proxy server (e.g. a load-balancer or nginx server) as
[described here](https://confluence.atlassian.com/adminjiraserver072/integrating-jira-with-apache-using-ssl-828788158.html),
then you need to specify extra options to make Jira aware of the setup. They can
be controlled via the below environment variables.

* `ATL_PROXY_NAME` (default: NONE)

   The reverse proxy's fully qualified hostname. `CATALINA_CONNECTOR_PROXYNAME`
   is also supported for backwards compatability.

* `ATL_PROXY_PORT` (default: NONE)

   The reverse proxy's port number via which Jira is
   accessed. `CATALINA_CONNECTOR_PROXYPORT` is also supported for backwards
   compatability.

* `ATL_TOMCAT_PORT` (default: 8080)

   The port for Tomcat/Jira to listen on. Depending on your container
   deployment method this port may need to be
   [exposed and published][docker-expose].

* `ATL_TOMCAT_SCHEME` (default: http)

   The protocol via which Jira is accessed. `CATALINA_CONNECTOR_SCHEME` is also
   supported for backwards compatability.

* `ATL_TOMCAT_SECURE` (default: false)

   Set 'true' if `ATL_TOMCAT_SCHEME` is 'https'. `CATALINA_CONNECTOR_SECURE` is
   also supported for backwards compatability.

* `ATL_TOMCAT_CONTEXTPATH` (default: NONE)

   The context path the application is served over. `CATALINA_CONTEXT_PATH` is
   also supported for backwards compatability.

* `ATL_TOMCAT_REQUESTATTRIBUTESENABLED`

  Checks for the existence of request attributes (typically set by the RemoteIpValve and similar)
  that should be used to override the values returned by the request for remote address,
  remote host, server port and protocol. This property is usually combined with `ATL_TOMCAT_TRUSTEDPROXIES`
  and `ATL_TOMCAT_INTERNALPROXIES` to show IP address of the remote host instead of the load balancer's.
  If not declared, the default value of `false` will be used.

* `ATL_TOMCAT_TRUSTEDPROXIES`

  A list of IP addresses separated by a pipe character e.g. `10.0.9.6|10.0.9.32`.  
  Trusted proxies that appear in the `remoteIpHeader` will be trusted and *will appear*
  in the `proxiesHeader` value. By adding a list of Trusted Proxies, Bamboo will remove the
  load balancers' IP addresses from Bamboo's view of the incoming connection. This could be desired
  in a clustered load balancer architecture where the load balancer address changes depending on
  which node proxies the connection, requiring re-approval of Agents.
  If not specified, no trusted proxies will be trusted.

* `ATL_TOMCAT_INTERNALPROXIES`

  A list of IP addresses separated by a pipe character e.g. `10.0.9.6|10.0.9.32`.  
  Trusted proxies that appear in the `remoteIpHeader` will be trusted and *will not appear*
  in the `proxiesHeader` value. By adding a list of Internal Proxies, Bamboo will remove the
  load balancers' IP addresses from Bamboo's view of the incoming connection. This could be desired
  in a clustered load balancer architecture where the load balancer address changes depending on
  which node proxies the connection, requiring re-approval of Agents.
  If not specified, no internal proxies will be trusted.

The following Tomcat/Catalina options are also supported. For more information,
see https://tomcat.apache.org/tomcat-7.0-doc/config/index.html.

* `ATL_TOMCAT_MGMT_PORT` (default: 8005)
* `ATL_TOMCAT_MAXTHREADS` (default: 100)
* `ATL_TOMCAT_MINSPARETHREADS` (default: 10)
* `ATL_TOMCAT_CONNECTIONTIMEOUT` (default: 20000)
* `ATL_TOMCAT_ENABLELOOKUPS` (default: false)
* `ATL_TOMCAT_PROTOCOL` (default: HTTP/1.1)
* `ATL_TOMCAT_ACCEPTCOUNT` (default: 10)
* `ATL_TOMCAT_MAXHTTPHEADERSIZE` (default: 8192)

### Access Log Settings

You can set the maximum number of days for access logs to be retained before being deleted. The default value of -1 means never delete old files.

* `ATL_TOMCAT_ACCESS_LOGS_MAXDAYS` (default: -1)

### JVM configuration

If you need to pass additional JVM arguments to Jira, such as specifying a custom trust store, you can add them via the below environment variable

* `JVM_SUPPORT_RECOMMENDED_ARGS`

   Additional JVM arguments for Jira

??? example 
    `docker run -e JVM_SUPPORT_RECOMMENDED_ARGS=-Djavax.net.ssl.trustStore=/var/atlassian/application-data/jira/cacerts -v jiraVolume:/var/atlassian/application-data/jira --name="jira" -d -p 8080:8080 atlassian/jira-software`

### Jira-specific settings

* `ATL_AUTOLOGIN_COOKIE_AGE` (default: 1209600; two weeks, in seconds)

   The maximum time a user can remain logged-in with 'Remember Me'.

### S3 Avatars storage configuration
Starting with Jira 9.9, you can configure Jira to store avatar files in Amazon S3. For requirements and additional 
information, please refer to 
[Configuring Amazon S3 Object Storage](https://confluence.atlassian.com/pages/viewpage.action?spaceKey=JSERVERM&title=.Configuring+Amazon+S3+object+storage+vJira_admin_9.9).

* `ATL_S3AVATARS_BUCKET_NAME`

  Bucket name to store avatars.

* `ATL_S3AVATARS_REGION`

  AWS region where the S3 bucket is located.

* `ATL_S3AVATARS_ENDPOINT_OVERRIDE`

  Override the default AWS API endpoint with a custom one (optional).


### Database configuration

It is optionally possible to configure the database from the environment,
avoiding the need to do so through the web startup screen.

The following variables are all must all be supplied if using this feature:

* `ATL_JDBC_URL`

   The database URL; this is database-specific.

* `ATL_JDBC_USER`

   The database user to connect as.

* `ATL_JDBC_PASSWORD`

   The password for the database user.

* `ATL_DB_DRIVER`

   The JDBC driver class; supported drivers are:

   * `com.microsoft.sqlserver.jdbc.SQLServerDriver`
   * `com.mysql.jdbc.Driver`
   * `oracle.jdbc.OracleDriver`
   * `org.postgresql.Driver`

   The driver must match the DB type (see next entry).

* `ATL_DB_TYPE`

   The type of database; valid supported values are:

   * `mssql`
   * `mysql`
   * `mysql57`
   * `mysql8`
   * `oracle10g`
   * `postgres72`

???+ note "MySQL supportability"
    `mysql` is only supported for versions prior to 8.13, and `mysql57` and `mysql8` are only supported after. 
    See [the 8.13.x upgrade instructions](https://confluence.atlassian.com/jirasoftware/jira-software-8-13-x-upgrade-notes-1018783378.html)
    for details.

The following variables may be optionally supplied when configuring the
database from the environment:

* `ATL_DB_SCHEMA_NAME`

   The schema name of the database. Depending on the value of `ATL_DB_TYPE`,
   the following default values are used if no schema name is specified:

   * `mssql`: `dbo`
   * `mysql`: NONE
   * `mysql57`: NONE
   * `mysql8`: NONE
   * `oracle10g`: NONE
   * `postgres72`: `public`

???+ note "MySQL or Oracle JDBC drivers"
    Due to licensing restrictions Jira does not ship with MySQL or Oracle JDBC drivers. 
    To use these databases you will need to copy a suitable driver into the container and restart it. 
    For example, to copy the MySQL driver into a container named "jira", you would do the following:

    `docker cp mysql-connector-java.x.y.z.jar jira:/opt/atlassian/jira/lib`

    `docker restart jira`

For more information see the page
[Startup check: JIRA database driver missing](https://confluence.atlassian.com/jirakb/startup-check-jira-database-driver-missing-873872169.html).

#### Optional database settings

* `ATL_JDBC_SECRET_CLASS`

  [Encryption class](https://confluence.atlassian.com/adminjiraserver/encrypting-database-password-974378811.html) for the database password. Depending on the secret class, the value of `ATL_JDBC_PASSWORD` will differ. Defaults to plaintext.

  Starting from 9.11 [AWS SecretsManager](https://confluence.atlassian.com/adminjiraserver/configuring-aws-secrets-manager-1282250155.html) is supported.

**IMPORTANT:** to start using password encryption for Jira instances that have already been set up, make sure `ATL_FORCE_CFG_UPDATE` is set to true
which will force the image entrypoint to regenerate `dbconfig.xml` with the new properties. Other database environment variables must be also set in the container:

```shell
docker run -v jiraVolume:/var/atlassian/application-data/jira --name='jira' -d -p 8080:8080 \
  -e ATL_JDBC_URL=jdbc:postgresql://172.17.0.1:5432/jira \
  -e ATL_JDBC_USER='jira' -e ATL_DB_DRIVER='org.postgresql.Driver' \
  -e ATL_DB_TYPE='postgres72' \
  -e ATL_JDBC_SECRET_CLASS='com.atlassian.secrets.store.aws.AwsSecretsManagerStore' \
  -e ATL_JDBC_PASSWORD='{"region": "us-east-1", "secretId": "mysecret", "secretPointer": "password"}' \
  -e ATL_FORCE_CFG_UPDATE='true' atlassian/jira-software
```

The following variables are for the Tomcat JDBC connection pool, and are
optional. For more information on these see: https://tomcat.apache.org/tomcat-7.0-doc/jdbc-pool.html

* `ATL_DB_MAXIDLE` (default: 20)
* `ATL_DB_MAXWAITMILLIS` (default: 30000)
* `ATL_DB_MINEVICTABLEIDLETIMEMILLIS` (default: 5000)
* `ATL_DB_MINIDLE` (default: 10)
* `ATL_DB_POOLMAXSIZE` (default: 100)
* `ATL_DB_POOLMINSIZE` (default: 20)
* `ATL_DB_REMOVEABANDONED` (default: true)
* `ATL_DB_REMOVEABANDONEDTIMEOUT` (default: 300)
* `ATL_DB_TESTONBORROW` (default: false)
* `ATL_DB_TESTWHILEIDLE` (default: true)
* `ATL_DB_TIMEBETWEENEVICTIONRUNSMILLIS` (default: 30000)
* `ATL_DB_VALIDATIONQUERY` (default: select 1)

The following settings only apply when using the Postgres driver:

* `ATL_DB_KEEPALIVE` (default: true)
* `ATL_DB_SOCKETTIMEOUT` (default: 240)

The following settings only apply when using the MySQL driver:

* `ATL_DB_VALIDATIONQUERYTIMEOUT` (default: 3)

### Data Center configuration

This docker image can be run as part of a
[Data Center](https://confluence.atlassian.com/enterprise/jira-data-center-472219731.html)
cluster. You can specify the following properties to start Jira as a Data Center
node, instead of manually configuring a cluster.properties file, See
[Installing Jira Data Center](https://confluence.atlassian.com/adminjiraserver071/installing-jira-data-center-802592197.html)
for more information on each property and its possible configuration.

#### Cluster configuration

*Jira Software and Jira Service Management only*

* `CLUSTERED` (default: false)

   Set 'true' to enable clustering configuration to be used. This will create a
   `cluster.properties` file inside the container's `$JIRA_HOME` directory.

* `JIRA_NODE_ID` (default: jira_node_<container-id>)

   The unique ID for the node. By default, this includes a randomly generated ID
   unique to each container, but can be overridden with a custom value.

* `JIRA_SHARED_HOME` (default: $JIRA_HOME/shared)

   The location of the shared home directory for all Jira nodes. **Note**: This
   must be real shared filesystem that is mounted inside the
   container. Additionally, see the note about UIDs.

* `EHCACHE_PEER_DISCOVERY` (default: default)

   Describes how nodes find each other.

* `EHCACHE_LISTENER_HOSTNAME` (default: NONE)

   The hostname of the current node for cache communication. Jira Data Center
   will resolve this this internally if the parameter isn't set.

* `EHCACHE_LISTENER_PORT` (default: 40001)

   The port the node is going to be listening to. Depending on your container
   deployment method this port may need to be [exposed and published][docker-expose].

* `EHCACHE_OBJECT_PORT` (default: dynamic)

   The port number on which the remote objects bound in the registry receive
   calls. This defaults to a free port if not specified. This port may need to
   be [exposed and published][docker-expose].

* `EHCACHE_LISTENER_SOCKETTIMEOUTMILLIS` (default: 2000)

   The default timeout for the Ehcache listener.

* `EHCACHE_MULTICAST_ADDRESS` (default: NONE)

   A valid multicast group address. Required when EHCACHE_PEER_DISCOVERY is set
   to 'automatic' instead of 'default'.

* `EHCACHE_MULTICAST_PORT` (default: NONE)

   The dedicated port for the multicast heartbeat traffic. Required when
   EHCACHE_PEER_DISCOVERY is set to 'automatic' instead of 'default'.  Depending
   on your container deployment method this port may need to be
   [exposed and published][docker-expose].

* `EHCACHE_MULTICAST_TIMETOLIVE` (default: NONE)

   A value between 0 and 255 which determines how far the packets will
   propagate. Required when EHCACHE_PEER_DISCOVERY is set to 'automatic' instead
   of 'default'.

* `EHCACHE_MULTICAST_HOSTNAME` (default: NONE)

   The hostname or IP of the interface to be used for sending and receiving
   multicast packets. Required when EHCACHE_PEER_DISCOVERY is set to 'automatic'
   instead of 'default'.

#### Shared directory and user IDs

By default, the Jira application runs as the user `jira`, with a UID and GID
of 2001. Consequently, this UID must have write access to the shared
filesystem. If for some reason a different UID must be used, there are a number
of options available:

* The Docker image can be rebuilt with a different UID.
* Under Linux, the UID can be remapped using
  [user namespace remapping](https://docs.docker.com/engine/security/userns-remap/).

To preserve strict permissions for certain configuration files, this container starts as
`root` to perform bootstrapping before running Jira under a non-privileged user account.
If you wish to start the container as a non-root user, please note that Tomcat
configuration will be skipped and a warning will be logged. You may still apply custom
configuration in this situation by mounting a custom server.xml file directly to
`/opt/atlassian/jira/conf/server.xml`

Database and Clustering bootstrapping will work as expected when starting this container
as a non-root user.

### Container configuration

* `ATL_FORCE_CFG_UPDATE` (default: false)

   The Docker [entrypoint][entrypoint.py] generates application configuration on
   first start; not all of these files are regenerated on subsequent
   starts. This is deliberate, to avoid race conditions or overwriting manual
   changes during restarts and upgrades. However in deployments where
   configuration is purely specified through the environment (e.g. Kubernetes)
   this behaviour may be undesirable; this flag forces an update of all
   generated files.

   In Jira the affected files are: `dbconfig.xml`

   See [the entrypoint code][entrypoint.py] for the details of how configuration
   files are generated.

*  `ATL_ALLOWLIST_SENSITIVE_ENV_VARS`

   Define a comma separated list of environment variables containing keywords 'PASS', 'SECRET' or 'TOKEN' to be ignored by the unset function which is executed in the entrypoint. The function uses `^` regex. For example, if you set `ATL_ALLOWLIST_SENSITIVE_ENV_VARS="PATH_TO_SECRET_FILE"`, all variables starting with `PATH_TO_SECRET_FILE` will not be unset.

???+ warning "Value exposure on host OS"
    When using this property, the values to sensitive environment variables will be available in clear text on the 
    host OS. As such, this data may be exposed to users or processes running on the host OS.

* `SET_PERMISSIONS` (default: true)

   Define whether to set home directory permissions on startup. Set to `false` to disable
   this behaviour.

* `ATL_UNSET_SENSITIVE_ENV_VARS` (default: true)

  Define whether to unset environment variables containing keywords 'PASS', 'SECRET' or 'TOKEN'.
  The unset function is executed in the entrypoint. Set to `false` if you want to allow passing
  sensitive environment variables to Jira container.

???+ warning "Value exposure on host OS"
    When using this property, the values to sensitive environment variables will be available in clear text on the 
    host OS. As such, this data may be exposed to users or processes running on the host OS.

### Advanced Configuration

As mentioned at the top of this section, the settings from the environment are
used to populate the application configuration on the container startup. However,
in some cases you may wish to customise the settings in ways that are not
supported by the environment variables above. In this case, it is possible to
modify the base templates to add your own configuration. There are three main
ways of doing this; modify our repository to your own image, build a new image
from the existing one, or provide new templates at startup. We will briefly
outline these methods here, but in practice how you do this will depend on your
needs.

##### Building your own image

* Clone the Atlassian repository at https://bitbucket.org/atlassian-docker/docker-atlassian-jira/
* Modify or replace the [Jinja](https://jinja.palletsprojects.com/) templates
  under `config`; _NOTE_: The files must have the `.j2` extensions. However you
  don't have to use template variables if you don't wish.
* Build the new image with e.g: `docker build --tag my-jira-8-image --build-arg JIRA_VERSION=8.x.x .`
* Optionally push to a registry, and deploy.

##### Build a new image from the existing one

* Create a new `Dockerfile`, which starts with the line e.g: `FROM
  atlassian/jira-software:latest`.
* Use a `COPY` line to overwrite the provided templates.
* Build, push and deploy the new image as above.

##### Overwrite the templates at runtime

There are two main ways of doing this:

* If your container is going to be long-lived, you can create it, modify the
  installed templates under `/opt/atlassian/etc/`, and then run it.
* Alternatively, you can create a volume containing your alternative templates,
  and mount it over the provided templates at runtime
  with `--volume my-config:/opt/atlassian/etc/`.

## Logging

By default the Jira logs are written inside the container, under
`${JIRA_HOME}/logs/`. If you wish to expose this outside the container (e.g. to
be aggregated by logging system) this directory can be a data volume or bind
mount. Additionally, Tomcat-specific logs are written to
`/opt/atlassian/jira/logs/`.

## Upgrades

To upgrade to a more recent version of Jira you can simply stop the `jira` container and start a new one based on a more recent image:

```shell
docker stop jira
docker rm jira
docker run ... (See above)
```

As your data is stored in the data volume directory on the host it will still  be available after the upgrade.

!!! note "Please make sure that you **don't** accidentally remove the `jira` container and its volumes using the `-v` option."

## Backup

For evaluations you can use the built-in database that will store its files in the Jira home directory. In that case it is sufficient to create a backup archive of the docker volume.

If you're using an external database, you can configure Jira to make a backup automatically each night. This will back up the current state, including the database to the `jiraVolume` docker volume, which can then be archived. Alternatively you can backup the database separately, and continue to create a backup archive of the docker volume to back up the Jira Home directory.

Read more about data recovery and backups: [https://confluence.atlassian.com/adminjiraserver071/backing-up-data-802592964.html](https://confluence.atlassian.com/adminjiraserver071/backing-up-data-802592964.html)

## Shutdown

Depending on your configuration Jira may take a short period to shutdown any
active operations to finish before termination. If sending a `docker stop` this
should be taken into account with the `--time` flag.

Alternatively, the script `/shutdown-wait.sh` is provided, which will initiate a
clean shutdown and wait for the process to complete. This is the recommended
method for shutdown in environments which provide for orderly shutdown,
e.g. Kubernetes via the `preStop` hook.

## Versioning

The `latest` tag matches the most recent release of Atlassian Jira Software, Jira Core or Jira Service Management. Thus `atlassian/jira-software:latest` will use the newest version of Jira available.

Alternatively you can use a specific major, major.minor, or major.minor.patch version of Jira by using a version number tag:

* `atlassian/jira-software:8`
* `atlassian/jira-software:8.14`
* `atlassian/jira-software:8.14.0`

* `atlassian/jira-servicemanagement:4`
* `atlassian/jira-servicemanagement:4.14`
* `atlassian/jira-servicemanagement:4.14.0`

* `atlassian/jira-core:8`
* `atlassian/jira-core:8.14`
* `atlassian/jira-core:8.14.0`

All Jira versions from 7.13+ (Software/Core) / 3.16+ (Service Management) are available.

???+ warning "`atlassian/jira-servicedesk` deprecation"
    All Jira Service Management 4.x versions are also available as `atlassian/jira-servicedesk`. 
    This namespace has been deprecated and versions from 5+ onwards will only be available as `atlassian/jira-servicemanagement`.

## Supported JDK versions and base images

Atlassian Docker images are generated from either
[official Eclipse Temurin OpenJDK Docker images](https://hub.docker.com/_/eclipse-temurin) or [Red Hat Universal Base Images](https://catalog.redhat.com/software/containers/ubi9/openjdk-17/61ee7c26ed74b2ffb22b07f6?architecture=amd64). 

UBI based images are only published from Jira 9.5 onwards and JDK17 only. Tags are available in 2 formats: `<version>-ubi9` and `<version>-ubi9-jdk17`.

The Docker images follow the [Atlassian Support end-of-life
policy](https://confluence.atlassian.com/support/atlassian-support-end-of-life-policy-201851003.html);
images for unsupported versions of the products remain available but will no longer
receive updates or fixes.

Historically, we have also generated other versions of the images, including
JDK8, Alpine, and 'slim' versions of the JDK. These legacy images still exist in
Docker Hub, however they should be considered deprecated, and do not receive
updates or fixes.

If for some reason you need a different version, see [Building your own image](#building-your-own-image) above.

## Migration to UBI

If you have been mounting any files to `${JAVA_HOME}` directory in `eclipse-temurin` based container, `JAVA_HOME` in UBI JDK17 container is set to `/usr/lib/jvm/java-17`.

Also, if you have been mounting and running any custom scripts in the container, UBI-based images may lack some tools and utilities that are available out of the box in eclipse-temurin tags. If that's the case, see [Building your own image](#building-your-own-image).

## Supported architectures

Currently, the Atlassian Docker images are built for the `linux/amd64` target
platform; we do not have other architectures on our roadmap at this
point. However, the Dockerfiles and support tooling have now had all
architecture-specific components removed, so if necessary it is possible to
build images for any platform supported by Docker.

### Building on the target architecture

Note: This method is known to work on Mac M1 and AWS ARM64 machines, but has not
been extensively tested.

The simplest method of getting a platform image is to build it on a target
machine. The following assumes you have git and Docker installed. You will also
need to know which version of Jira you want to build; substitute
`JIRA_VERSION=x.x.x` with your required version:

```shell
git clone --recurse-submodule https://bitbucket.org/atlassian-docker/docker-atlassian-jira.git
cd docker-atlassian-jira
docker build --tag my-image --build-arg JIRA_VERSION=x.x.x .
```
This image can be pushed up to your own Docker Hub or private repository.

## Troubleshooting

These images include built-in scripts to assist in performing common JVM diagnostic tasks.

### Thread dumps

`/opt/atlassian/support/thread-dumps.sh` can be run via `docker exec` to easily trigger the collection of thread
dumps from the containerized application. For example:

```shell
docker exec my_container /opt/atlassian/support/thread-dumps.sh
```

By default, this script will collect 10 thread dumps at 5 second intervals. This can
be overridden by passing a custom value for the count and interval, by using `-c` / `--count`
and `-i` / `--interval` respectively. For example, to collect 20 thread dumps at 3 second intervals:

```shell
docker exec my_container /opt/atlassian/support/thread-dumps.sh --count 20 --interval 3
```

Thread dumps will be written to `$APP_HOME/thread_dumps/<date>`.

???+ note "Disable capturing output from top run"
    By default this script will also capture output from top run in 'Thread-mode'. This can
    be disabled by passing `-n` / `--no-top`

### Heap dump

`/opt/atlassian/support/heap-dump.sh` can be run via `docker exec` to easily trigger the collection of a heap
dump from the containerized application. For example:

```shell
docker exec my_container /opt/atlassian/support/heap-dump.sh
```

A heap dump will be written to `$APP_HOME/heap.bin`. If a file already exists at this
location, use `-f` / `--force` to overwrite the existing heap dump file.

### Manual diagnostics

The `jcmd` utility is also included in these images and can be used by starting a `bash` shell
in the running container:

```shell
docker exec -it my_container /bin/bash
```

## Support

For product support, go to:

* https://support.atlassian.com/jira-software-server/
* https://support.atlassian.com/jira-service-management-server/
* https://support.atlassian.com/jira-core-server/

You can also visit the [Atlassian Data Center](https://community.atlassian.com/t5/Atlassian-Data-Center-on/gh-p/DC_Kubernetes)
forum for discussion on running Atlassian Data Center products in containers.

## Development and testing

See [Development][DEVELOPMENT.md] for details on setting up a development
environment and running tests.

## Changelog

For a detailed list of changes to the Docker image configuration see [the Git
commit history](https://bitbucket.org/atlassian-docker/docker-atlassian-jira/commits/).

## License

Copyright © 2020 Atlassian Corporation Pty Ltd.
Licensed under the Apache License, Version 2.0.

[docker-expose]: https://docs.docker.com/v17.09/engine/userguide/networking/default_network/binding/
[entrypoint.py]: https://bitbucket.org/atlassian-docker/docker-atlassian-jira/src/master/entrypoint.py
[DEVELOPMENT.md]: https://bitbucket.org/atlassian-docker/docker-atlassian-jira/src/master/DEVELOPMENT.md
