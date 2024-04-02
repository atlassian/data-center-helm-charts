# ![Atlassian Crowd](https://wac-cdn.atlassian.com/dam/jcr:d2a1da52-ae52-4b06-9ab1-da8647a89653/crowd-icon-gradient-blue.svg?cdnVersion=696){: style="height:35px;width:35px"} Crowd

## Overview

Crowd provides single sign-on and user identity that's easy to use.

Learn more about Crowd: [https://www.atlassian.com/software/crowd][1]

This Docker container makes it easy to get an instance of Crowd up and running.

**Use docker version >= 20.10.10**

## Quick Start

For the `CROWD_HOME` directory that is used to store application data (amongst other things) we recommend mounting a host directory as a [data volume](https://docs.docker.com/engine/tutorials/dockervolumes/#/data-volumes), or via a named volume.

To get started you can use a data volume, or named volumes. In this example we'll use named volumes.
```shell
docker volume create --name crowdVolume
docker run -v crowdVolume:/var/atlassian/application-data/crowd --name="crowd" -d -p 8095:8095 atlassian/crowd
```
!!! success "Crowd is now available on [http://localhost:8095](http://localhost:8095)."

Please ensure your container has the necessary resources allocated to it. See [Supported Platforms][2] for further information.

???+ tip "If you are using `docker-machine` on Mac OS X, please use `open http://$(docker-machine ip default):8095` instead."

## Common settings

### Verbose container entrypoint logging

During the startup process of the container, various operations and checks are performed to ensure that the application
is configured correctly and ready to run. To help in troubleshooting and to provide transparency into this process, you
can enable verbose logging. The `VERBOSE_LOGS` environment variable enables detailed debug messages to the container's
log, offering insights into the actions performed by the entrypoint script.

* `VERBOSE_LOGS` (default: false)

  Set to `true` to enable detailed debug messages during the container initialization.

### Memory / Heap Size

If you need to override Crowd's default memory allocation, you can control the minimum heap (Xms) and maximum heap (Xmx) via the below environment variables.

* `JVM_MINIMUM_MEMORY` (default: 384m)

   The minimum heap size of the JVM

* `JVM_MAXIMUM_MEMORY` (default: 768m)

   The maximum heap size of the JVM

### Reverse Proxy Settings

If Crowd is run behind a reverse proxy server as [described here][3], then you need to specify extra options to make Crowd aware of the setup. They can be controlled via the below environment variables.

* `ATL_PROXY_NAME` (default: NONE)

   The reverse proxy's fully qualified hostname. `CATALINA_CONNECTOR_PROXYNAME`
   is also supported for backwards compatability.

* `ATL_PROXY_PORT` (default: NONE)

   The reverse proxy's port number via which Crowd is
   accessed. `CATALINA_CONNECTOR_PROXYPORT` is also supported for backwards
   compatability.

* `ATL_TOMCAT_PORT` (default: 8095)

   The port for Tomcat/Crowd to listen on. Depending on your container
   deployment method this port may need to be
   [exposed and published][docker-expose].

* `ATL_TOMCAT_SCHEME` (default: http)

   The protocol via which Crowd is accessed. `CATALINA_CONNECTOR_SCHEME` is also
   supported for backwards compatability.

* `ATL_TOMCAT_SECURE` (default: false)

   Set 'true' if `ATL_TOMCAT_SCHEME` is 'https'. `CATALINA_CONNECTOR_SECURE` is
   also supported for backwards compatability.

The following Tomcat/Catalina options are also supported. For more information,
see https://tomcat.apache.org/tomcat-8.5-doc/config/index.html.

* `ATL_TOMCAT_MGMT_PORT` (default: 8000)
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

### JVM Configuration

If you need to pass additional JVM arguments to Crowd, such as specifying a custom trust store, you can add them via the below environment variable

* `JVM_SUPPORT_RECOMMENDED_ARGS`

   Additional JVM arguments for Crowd

??? example 
    `docker run -e JVM_SUPPORT_RECOMMENDED_ARGS=-Djavax.net.ssl.trustStore=/var/atlassian/application-data/crowd/cacerts -v crowdVolume:/var/atlassian/application-data/crowd --name="crowd" -d -p 8095:8095 atlassian/crowd` 

### Data Center configuration

This docker image can be run as part of a [Data Center][4] cluster. You can
specify the following properties to start Crowd as a Data Center node,
instead of manually configuring a cluster. See [Installing Crowd Data
Center][5] for more information.

### Container Configuration

* `SET_PERMISSIONS` (default: true)

   Define whether to set home directory permissions on startup. Set to `false` to disable
   this behaviour.

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

* Clone the Atlassian repository at https://bitbucket.org/atlassian-docker/docker-atlassian-crowd/
* Modify or replace the [Jinja](https://jinja.palletsprojects.com/) templates
  under `config`; _NOTE_: The files must have the `.j2` extensions. However you
  don't have to use template variables if you don't wish.
* Build the new image with e.g: `docker build --tag my-crowd-image --build-arg CROWD_VERSION=3.x.x .`
* Optionally push to a registry, and deploy.

##### Build a new image from the existing one

* Create a new `Dockerfile`, which starts with the Atlassian Crowd base image e.g: `FROM atlassian/crowd:latest`.
* Use a `COPY` line to overwrite the provided templates.
* Build, push and deploy the new image as above.

##### Overwrite the templates at runtime

There are two main ways of doing this:

* If your container is going to be long-lived, you can create it, modify the
  installed templates under `/opt/atlassian/etc/`, and then run it.
* Alternatively, you can create a volume containing your alternative templates,
  and mount it over the provided templates at runtime
  with `--volume my-config:/opt/atlassian/etc/`.

## Shared directory and user IDs

By default the Crowd application runs as the user `crowd`, with a UID
and GID of 2004. Consequently this UID must have write access to the shared
filesystem. If for some reason a different UID must be used, there are a number
of options available:

* The Docker image can be rebuilt with a different UID.
* Under Linux, the UID can be remapped using
  [user namespace remapping][7].

To preserve strict permissions for certain configuration files, this container starts as
`root` to perform bootstrapping before running Crowd under a non-privileged user
account. If you wish to start the container as a non-root user, please note that Tomcat
configuration will be skipped and a warning will be logged. You may still apply custom
configuration in this situation by mounting configuration files directly, e.g.
by mounting your own server.xml file directly to
`/opt/atlassian/crowd/apache-tomcat/conf/server.xml`

## Upgrade

To upgrade to a more recent version of Crowd you can simply stop the `crowd` container and start a new one based on a more recent image:

```shell
docker stop crowd
docker rm crowd
docker run ... (See above)
```

As your data is stored in the data volume directory on the host it will still  be available after the upgrade.

!!! note "Please make sure that you **don't** accidentally remove the `crowd` container and its volumes using the `-v` option."

## Backup

For evaluations you can use the built-in database that will store its files in the Crowd home directory. In that case it is sufficient to create a backup archive of the docker volume.

If you're using an external database, you can configure Crowd to make a backup automatically each night. This will back up the current state, including the database to the `crowdVolume` docker volume, which can then be archived. Alternatively you can backup the database separately, and continue to create a backup archive of the docker volume to back up the Crowd Home directory.

Read more about data recovery and backups: [Backing Up and Restoring Data][6]

## Versioning

The `latest` tag matches the most recent release of Atlassian Crowd. Thus `atlassian/crowd:latest` will use the newest version of Crowd available.

Alternatively you can use a specific major, major.minor, or major.minor.patch version of Crowd by using a version number tag:

* `atlassian/crowd:3`
* `atlassian/crowd:3.2`
* `atlassian/crowd:3.2.3`

All versions from 3.0+ are available

## Supported JDK versions and base images

All Atlassian Crowd Docker images are now JDK11 only, and generated from the
[official Eclipse Temurin OpenJDK Docker images](https://hub.docker.com/_/eclipse-temurin).

Starting from 5.2.3 [UBI based](https://catalog.redhat.com/software/containers/ubi9/openjdk-11-runtime/61ee7d1c33f211c45407a91c) tags are published as well.
UBI tags are available in 2 formats: `<version>-ubi9` and `<version>-ubi9-jdk11`.

The Docker images follow the [Atlassian Support end-of-life
policy](https://confluence.atlassian.com/support/atlassian-support-end-of-life-policy-201851003.html);
images for unsupported versions of the products remain available but will no longer
receive updates or fixes.

Historically, we have also generated other versions of the images, including
JDK8, Alpine, and 'slim' versions of the JDK. These legacy images still exist in
Docker Hub, however they should be considered deprecated, and do not receive
updates or fixes.

If for some reason you need a different version, see "Building your own image"
above.

## Migration to UBI

If you have been mounting any files to `${JAVA_HOME}` directory in `eclipse-temurin` based container, `JAVA_HOME` in UBI JDK11 container is set to `/usr/lib/jvm/java-11`.

Also, if you have been mounting and running any custom scripts in the container, UBI-based images may lack some tools and utilities that are available out of the box in `eclipse-temurin` tags. If that's the case, see [Building your own image](#building-your-own-image).

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
need to know which version of Crowd you want to build; substitute
`CROWD_VERSION=x.x.x` with your required version:

```shell
git clone --recurse-submodule https://bitbucket.org/atlassian-docker/docker-atlassian-crowd.git
cd docker-atlassian-crowd
docker build --tag my-image --build-arg CROWD_VERSION=x.x.x .
```
This image can be pushed up to your own Docker Hub or private repository.

## Troubleshooting

These images include built-in scripts to assist in performing common JVM diagnostic tasks.

### Thread dumps

`/opt/atlassian/support/thread-dumps.sh` can be run via `docker exec` to easily trigger the collection of thread
dumps from the containerized application. For example:

```shell
docker exec my_crowd /opt/atlassian/support/thread-dumps.sh
```

By default this script will collect 10 thread dumps at 5 second intervals. This can
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

* https://support.atlassian.com/crowd/

You can also visit the [Atlassian Data Center](https://community.atlassian.com/t5/Atlassian-Data-Center-on/gh-p/DC_Kubernetes)
forum for discussion on running Atlassian Data Center products in containers.

## Changelog

For a detailed list of changes to the Docker image configuration see [the Git
commit history](https://bitbucket.org/atlassian-docker/docker-atlassian-crowd/commits/).

## License

Copyright © 2019 Atlassian Corporation Pty Ltd.
Licensed under the Apache License, Version 2.0.

[1]: https://www.atlassian.com/software/crowd
[2]: https://confluence.atlassian.com/crowd/supported-platforms-191851.html
[3]: https://confluence.atlassian.com/crowd031/integrating-crowd-with-apache-949753124.html
[4]: https://confluence.atlassian.com/crowd/crowd-data-center-935372453.html
[5]: https://confluence.atlassian.com/crowd/installing-crowd-data-center-935369773.html
[6]: https://confluence.atlassian.com/crowd/backing-up-and-restoring-data-36470797.html
[7]: https://docs.docker.com/engine/security/userns-remap/
