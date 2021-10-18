# SSH service in Bitbucket on Kubernetes

In addition to providing a service on HTTP(S), Bitbucket also allows remote Git operations over SSH connections. By default, Kubernetes Ingress controllers only work for HTTP connections, but some ingress controllers also support TCP connections.

Depending on the need of your deployment, SSH access can be provided through two mechanisms:

1. Opening the TCP port through the ingress controller - This option should be used if the SSH service is required to be available on the same DNS name as the HTTP service.
2. Creating a separate Kubernetes `LoadBalancer` service - This option is available if the ingress controller does not support TCP connections, or if you don’t need your deployment to have the SSH service available on the same DNS name as the HTTP service.

## NGINX Ingress controller config for SSH connections
We can follow the official documentation for the NGINX Ingress controller for this: [Exposing TCP and UDP services - NGINX Ingress Controller](https://kubernetes.github.io/ingress-nginx/user-guide/exposing-tcp-udp-services/){.external}.

!!!info "Namespace co-location"
    These instructions should be performed in the same namespace in which the Ingress controller resides.

### 1. Create ConfigMap
Create a new `ConfigMap`:
``` shell
kubectl create configmap tcp-services
```

In our example we deployed Bitbucket using the Helm release name `bitbucket` in the namespace `ssh-test`, update the `ConfigMap` `tcp-services` accordingly:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: tcp-services
  namespace: ingress-nginx
data:
  7999: "<bitbucket namespace>/<bitbucket helm release name>:ssh"
```

### 2. Update Ingress deployment
Next, we have to edit the `deployment` of the ingress controller and add the `--tcp-services-configmap` option:
```shell
kubectl edit deployment <name of ingress-nginx deployment>
```
Add this line in the `args` of the container `spec`:
```shell
- --tcp-services-configmap=$(POD_NAMESPACE)/tcp-services
```
so it looks something like this:
```yaml
 spec:
   containers:
   - args:
     - /nginx-ingress-controller
     - --publish-service=$(POD_NAMESPACE)/ingress-nginx-controller
     - --election-id=ingress-controller-leader
     - --ingress-class=nginx
     - --configmap=$(POD_NAMESPACE)/ingress-nginx-controller
     - --validating-webhook=:8443
     - --validating-webhook-certificate=/usr/local/certificates/cert
     - --validating-webhook-key=/usr/local/certificates/key
     - --tcp-services-configmap=$(POD_NAMESPACE)/tcp-services
```

### 3. Update the Ingress service
Update the Ingress service to include an additional `port` definition for `ssh`
```shell
kubectl edit service <name of ingress-nginx service>
```
Add this section in the `ports` of the container `spec`:
```yaml
- name: ssh
  port: 7999
  protocol: TCP
```
so it looks something like this:
```yaml
spec:
  clusterIP: 10.100.19.60
  externalTrafficPolicy: Cluster
  ports:
  - name: http
    nodePort: 31381
    port: 80
    protocol: TCP
    targetPort: http
  - name: https
    nodePort: 32612
    port: 443
    protocol: TCP
    targetPort: https
  - name: ssh
    port: 7999
    protocol: TCP
```
After the deployment has been upgraded, the `SSH` service should be available on port `7999`.

## LoadBalancer service for SSH connections on AWS
In the values file for the helm chart, the extra SSH service can be enabled like this:
```yaml
bitbucket:
  sshService:
    enabled: true
```
On a deployment using AWS, assuming you have [external-dns](https://github.com/kubernetes-sigs/external-dns){.external} configured, you can add these annotations to automatically set up the DNS name for the SSH service:
```yaml
bitbucket:
  sshService:
    enabled: true
    annotations:
      external-dns.alpha.kubernetes.io/hostname: bitbucket-ssh.example.com
  additionalEnvironmentVariables:
    - name: PLUGIN_SSH_BASEURL
      value: ssh://bitbucket-ssh.example.com/
```
