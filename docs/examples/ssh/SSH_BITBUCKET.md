# SSH service in Bitbucket on Kubernetes

In addition to providing a service on HTTP(S), Bitbucket also allows remote Git operations over SSH connections. By default, Kubernetes Ingress controllers only work for HTTP connections, but some ingress controllers also support TCP connections.

You can provide SSH access by creating a separate Kubernetes `LoadBalancer` service, but you can do this only if you don’t need your deployment to have the SSH service available on the same DNS name as the HTTP service.

## Creating a separate Kubernetes LoadBalancer service for SSH connections on AWS
In the values file for the helm chart, the extra SSH service can be enabled like this:
```
bitbucket:
  sshService:
    enabled: true
```
On a deployment using AWS, assuming you have [external-dns](https://github.com/kubernetes-sigs/external-dns) configured, you can add these annotations to automatically set up the DNS name for the SSH service:
```
bitbucket:
  sshService:
    enabled: true
    annotations:
      external-dns.alpha.kubernetes.io/hostname: bitbucket-ssh.example.com
  additionalEnvironmentVariables:
    - name: PLUGIN_SSH_BASEURL
      value: ssh://bitbucket-ssh.example.com/
```
