# Smart Mirroring

Smart Mirroring can greatly improve Git clone speeds for distributed teams working with large repositories. Large repositories that take hours to clone from a Bitbucket instance over the Internet from the other side of the world can take minutes when cloned from a local mirror on a fast network.

You can learn more details about smart mirroring on the [official documentation page](https://confluence.atlassian.com/bitbucketserver/smart-mirroring-776640046.html).

## Requirements

!!!warning
    
    **Your primary Bitbucket instance must be a fully licensed Bitbucket Data Center instance** - You do not have to run your Bitbucket Data Center instance as a multi-node cluster to use smart mirroring, but you must have an up-to-date Data Center license.


    **The primary instance and all mirror(s) must have HTTPS with a valid (i.e., signed by a Certificate Authority anchored to the root and not expired) SSL certificate** - This is a strict requirement of smart mirroring on both the primary instance and all mirror(s), and cannot be bypassed. The mirror setup wizard will not proceed if either the mirror or the primary instance does not have a valid SSL certificate.


    **The primary Bitbucket instance must have SSH enabled** - Mirrors keep their repositories synchronized with the primary instance over SSH and cannot use HTTP or HTTPS for this. See Enable SSH access to Git repositories for instructions on enabling SSH access on your primary instance.


## Workflow

1. Install the upstream as usual with a Helm chart
2. Install the mirror farm with another Helm chart
      1. There is a set of new properties that need to be configured to make the mirror works

## Configuration

Examples are using nginx-ingress controller.

### Upstream values

### Mirror values

```
bitbucket:
  mode: mirror
  displayName: Bitbucket Mirror Testing Instance
  clustering:
    enabled: true
  applicationMode: "mirror"
  mirror:
    upstreamUrl: https://bitbucket-upstream.example.com

ingress:
  create: true
  nginx: true
  maxBodySize: 250m
  host: bitbucket-mirror.example.com
  path: "/"
  annotations:
    cert-manager.io/issuer: "letsencrypt-prod" # Using https://letsencrypt.org/
  https: true
  tlsSecretName: tls-certificate-mirror

volumes:
  localHome:
    create: true
  sharedHome:
    nfsPermissionFixer: false
```