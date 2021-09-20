# Troubleshooting tips

This guide contains general tips on how to investigate an application deployment that doesn't work correctly.

## General tips

First, it is important to gather the information that will help you better understand where to focus your investigation efforts.
The next section assumes you've followed the [installation](../userguide/INSTALLATION.md) and [configuration](../userguide/CONFIGURATION.md) guides, and you can't access the installed product service. 

For installation troubleshooting, you will need to access the Kubernetes cluster and have enough permissions to follow the commands below.

We highly recommend that you read through the Kubernetes official documentation describing [monitoring, logging and debugging](https://kubernetes.io/docs/tasks/debug-application-cluster/){.external}. Additionally, for great starting tips read the [Application Introspection and Debugging section](https://kubernetes.io/docs/tasks/debug-application-cluster/debug-application-introspection/){.external}.

???+ info "Value placeholders"
    Some commands include `<release_name>` and `<namespace>`. Replace them with the Helm release name and namespace specified when running `helm install`.

## My service is not accessible

After `helm install` finishes, it prints a product service URL. It usually takes a few minutes for the service to start. If
you visit the URL too soon, it might return a `5XX` error HTTP code (the actual code might be dependent on your network implementation).

If you have waited long enough (more than 10 minutes), and the service is still not accessible, it is time to investigate the reason why this is the case.

### Helm release verification 

1. Run `helm list --all-namespaces` to get the list of all installed chart releases.
    * You should be able to see your installation in the list
    * The status for the release should be `deployed`
2. Run `helm test <release_name> -n <namespace>`
    * This should return application tests in `succeeded` phase
    * In case there are any test failures you will need to further investigate the particular domain
   
### DNS verification

To verify the DNS record from your machine, run a basic `dig` test:

```shell
dig SERVICE_DOMAIN_NAME
```

Or use a [web version of the tool](https://toolbox.googleapps.com/apps/dig/){.external}.

### Investigate application logs

You can get application logs from the pods with a standard `kubectl` command:

```shell
kubectl logs APPLICATION_POD_NAME
```

You can read the output and make sure it doesn't contain an error or an exception.

### Get application pod details

For more details follow the [official guide for debugging](https://kubernetes.io/docs/tasks/debug-application-cluster/debug-application-introspection/){.external}.

Get the list of pods and their states:

```shell
kubectl get <release_name> -n <namespace> -o wide
```

Get details about a specific pod:

```shell
kubectl describe POD_NAME -n <namespace>
```

### Get storage details

Each application pod needs to have successfully mounted local and shared home. You can find out the details for the persistent volume claims with this command: 

???+ info "Prerequisities"
      The example needs to have [`jq`](https://stedolan.github.io/jq/){.external} tool installed.

```shell
kubectl get pods --all-namespaces -o=json | jq -c \
'.items[] | {name: .metadata.name, namespace: .metadata.namespace, claimName:.spec.volumes[] | select( has ("persistentVolumeClaim") ).persistentVolumeClaim.claimName }'
```
Find all the application pods in the output and verify they have the correct claims (shared home and local home). For more details follow the [documentation for persistent volumes](https://kubernetes.io/docs/concepts/storage/persistent-volumes/){.external}.
