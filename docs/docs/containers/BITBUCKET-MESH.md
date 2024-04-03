# ![Atlassian Bitbucket Mesh](https://wac-cdn.atlassian.com/dam/jcr:bf39fc40-3871-491f-98e3-fb2293f57a00/bitbucket-icon-gradient-blue.svg?cdnVersion=696){: style="height:35px;width:35px"} Bitbucket Mesh

## Overview

Bitbucket Data Center is an on-premises source code management solution for Git
that's secure, fast, and enterprise grade. Create and manage repositories, set
up fine-grained permissions, and collaborate on code - all with the flexibility
of your servers.

Bitbucket Mesh is an optional scalability extension for Bitbucket. For more
information see <https://confluence.atlassian.com/bitbucketserver/bitbucket-mesh-1128304351.html>.

This Docker image is published as `atlassian/bitbucket-mesh`.

This Docker container makes it easy to get Mesh nodes for a Bitbucket Data
Center up and running. It will only work in conjunction with a Bitbucket Data
Center server.

For full documentation on running Bitbucket Data Center with Mesh nodes, see [the
Bitbucket documentation](https://confluence.atlassian.com/bitbucketserver/bitbucket-mesh-1128304351.html).

_* If running this image in a production environment, we strongly recommend you
run this image using a specific version tag instead of latest. This is because
the image referenced by the latest tag changes often and we cannot guarantee
that it will be backwards compatible. *_

**Use docker version >= 20.10.10**

## Quick Start

For the `MESH_HOME` directory that is used to store the repository data (amongst
other things) we recommend mounting a host directory as a
[data volume](https://docs.docker.com/engine/tutorials/dockervolumes/#/data-volumes),
or via a named volume.

Volume permissions are managed by entry scripts. To get started you can use a
data volume, or named volumes. In this example we'll use named volumes.
```shell
docker volume create --name bitbucketMeshVolume
docker run -v bitbucketMeshVolume:/var/atlassian/application-data/mesh --name="bitbucket-mesh" -d -p 7777:7777 atlassian/bitbucket-mesh
```

Note that this command can substitute folder paths with named volumes.

Please ensure your container has the necessary resources allocated to it.
We recommend 2GiB of memory allocated to accommodate both the application server
and the git processes.
See [Supported Platforms](https://confluence.atlassian.com/display/BitbucketServer/Supported+platforms) for further information.


## Common settings

### Verbose container entrypoint logging

During the startup process of the container, various operations and checks are performed to ensure that the application
is configured correctly and ready to run. To help in troubleshooting and to provide transparency into this process, you
can enable verbose logging. The `VERBOSE_LOGS` environment variable enables detailed debug messages to the container's 
log, offering insights into the actions performed by the entrypoint script.

* `VERBOSE_LOGS` (default: false)

  Set to `true` to enable detailed debug messages during the container initialization.

### Mesh Node Configuration

* `MESH_HOME`

  The home directory used by the Mesh node. This should have full read/write
  permissions and be persistent – your Bitbucket Mesh data will be stored here.

* `GRPC_SERVER_PORT` (default: 7777)

  The port used by the Mesh node to communicate with the server.

### Mesh Node JVM Configuration

If you need to override the Mesh node's default memory configuration or pass
additional JVM arguments, use the environment variables below

* `JVM_MINIMUM_MEMORY` (default: 512m)

  The minimum heap size of the JVM

* `JVM_MAXIMUM_MEMORY` (default: 1024m)

  The maximum heap size of the JVM

* `JVM_SUPPORT_RECOMMENDED_ARGS` (default: NONE)

  Additional JVM arguments for the Mesh node , such as a custom Java Trust Store

### JMX Monitoring

JMX monitoring can be enabled with `JMX_ENABLED=true`. Information
on additional settings and available metrics is available in the
[Bitbucket JMX documentation](https://confluence.atlassian.com/bitbucketserver/enabling-jmx-counters-for-performance-monitoring-776640189.html).

## Other settings

As well as the above settings, all settings that are available in the
[mesh.properties file](https://confluence.atlassian.com/bitbucketserver/mesh-configuration-properties-1128304362.html)
can also be provided via Docker environment variables. For a full explanation of converting Bitbucket properties into environment
variables see
[the relevant Spring Boot documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html#boot-features-external-config-relaxed-binding).

## Container Configuration

* `SET_PERMISSIONS` (default: true)

  Define whether to set home directory permissions on startup. Set to `false` to disable
  this behaviour.

## Home directory and user IDs

By default the Bitbucket application runs as the user `bitbucket`, with a UID
and GID of 2003. If for some reason a different UID must be used, there are a
number of options available:

* The Docker image can be rebuilt with a different UID.
* Under Linux, the UID can be remapped using
  [user namespace remapping](https://docs.docker.com/engine/security/userns-remap/).

## Shutdown

The Mesh node allows a configurable grace period for active operations to finish
before termination; by default this is 30s. If sending a `docker stop` this
should be taken into account with the `--time` flag.

Alternatively, the script `/shutdown-wait.sh` is provided, which will initiate a
clean shutdown and wait for the process to complete. This is the recommended
method for shutdown in environments which provide for orderly shutdown,
e.g. Kubernetes via the `preStop` hook.

## Versioning

You should ensure you are running the appropriate Mesh version for your
Bitbucket Data Center server. A support matrix is available here:
[Bitbucket Mesh compatibility matrix](https://confluence.atlassian.com/bitbucketserver/bitbucket-mesh-compatibility-matrix-1127254859.html).

## Supported JDK versions and base images

All the Atlassian Docker images are now JDK11 and JDK17 (since version 2.4) only, and generated from the
[official Eclipse Temurin OpenJDK Docker images](https://hub.docker.com/_/eclipse-temurin).

Starting from 2.4 [UBI based](https://catalog.redhat.com/software/containers/ubi9/openjdk-17/61ee7c26ed74b2ffb22b07f6?architecture=amd64) tags are published as well.
UBI tags are available in 2 formats: `<version>-ubi9` and `<version>-ubi9-jdk17`.

The Docker images follow the [Atlassian Support end-of-life
policy](https://confluence.atlassian.com/support/atlassian-support-end-of-life-policy-201851003.html);
images for unsupported versions of the products remain available but will no longer
receive updates or fixes.

If for some reason you need a different version, see "Building your own image"

## Migration to UBI

If you have been mounting any files to `${JAVA_HOME}` directory in `eclipse-temurin` based container, `JAVA_HOME` in UBI JDK17 container is set to `/usr/lib/jvm/java-17`.

Also, if you have been mounting and running any custom scripts in the container, UBI-based images may lack some tools and utilities that are available out of the box in eclipse-temurin tags. If that's the case, see "Building your own image".

## Building your own image

* Clone the Atlassian repository at https://bitbucket.org/atlassian-docker/docker-atlassian-bitbucket-mesh/
* Modify or replace the [Jinja](https://jinja.palletsprojects.com/) templates
  under `config`; _NOTE_: The files must have the `.j2` extensions. However, you
  don't have to use template variables if you don't wish.
* Build the new image with e.g: `docker build --tag my-bitbucket-mesh-image --build-arg MESH_VERSION=1.x.x .`
* Optionally push to a registry, and deploy.

## Supported architectures

Currently, the Atlassian Docker images are built for the `linux/amd64` target
platform; we do not have other architectures on our roadmap at this
point. However the Dockerfiles and support tooling have now had all
architecture-specific components removed, so if necessary it is possible to
build images for any platform supported by Docker.

## Building on the target architecture

The simplest method of getting a platform image is to build it on a target
machine; see "Building your own image" above.

Note: This method is known to work on Mac M1 and AWS ARM64 machines, but has not
be extensively tested.

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

## Manual diagnostics

The `jcmd` utility is also included in these images and can be used by starting a `bash` shell
in the running container:
```shell
docker exec -it my_container /bin/bash
```

## Support

For product support, go to [support.atlassian.com](https://support.atlassian.com/)

You can also visit the [Atlassian Data Center](https://community.atlassian.com/t5/Atlassian-Data-Center-on/gh-p/DC_Kubernetes)
forum for discussion on running Atlassian Data Center products in containers.

# Changelog

For a detailed list of changes to the Docker image configuration see [the Git
commit history](https://bitbucket.org/atlassian-docker/docker-atlassian-bitbucket-mesh/commits/).

# License

Copyright © 2022 Atlassian Corporation Pty Ltd.
Licensed under the Apache License, Version 2.0.
