# SSH service in Bitbucket on Kubernetes

## Goal
In addition to providing a service on HTTP(S), Bitbucket also allows remote Git operations over SSH connections. By default, Kubernetes Ingress controllers only work for HTTP connections, but some ingress controllers also support TCP connections.


If you don’t need your deployment to have the SSH service available on the same DNS name as the HTTP service, you can provide SSH access by creating a separate Kubernetes `LoadBalancer` service for SSH connections on AWS.

## Steps for creating the LoadBalancer service 
1. Enable the SSH service in the `values.yaml` file:
   ```
   bitbucket:
     sshService:
       enabled: true
   ```

2. Add DNS annotaion in the `values.yaml` file. On a deployment using AWS, assuming you have [external-dns](https://github.com/kubernetes-sigs/external-dns) configured, you can add these annotations to automatically set up the DNS name for the SSH service: 
   ```
   bitbucket:
      annotations:
         external-dns.alpha.kubernetes.io/hostname: bitbucket-ssh.example.com
      additionalEnvironmentVariables:
       - name: PLUGIN_SSH_BASEURL
         value: ssh://bitbucket-ssh.example.com/
   ```
   This step is optional, you can set up the DNS name manually.

3. Apply new values.
