# Troubleshooting and investigation tips

This guide contains general tips how to investigate deployment that doesn't work correctly.

## General tips

First it is important to gather information that will help you better understand where to focus your investigation efforts.
The next section assumes you've followed the [installation](../installation/INSTALLATION.md) and [configuration](../installation/CONFIGURATION.md) guide, and you cannot access the installed product service. 

For installation troubleshooting you will need to access the kubernetes cluster and have enough permissions to follow the commands below.

It is highly recommended reading through the Kubernetes official documentation describing [monitoring, logging and debugging](https://kubernetes.io/docs/tasks/debug-application-cluster/). 
Especially the [Application Introspection and Debugging section](https://kubernetes.io/docs/tasks/debug-application-cluster/debug-application-introspection/) has great starting tips.

Note: some commands include `RELEASE_NAME` and `NAMESPACE`. Replace it with the helm release name and namespace specified when running `helm install`.

## My service is not accessible

After `helm install` finishes, it prints a product service URL. It usually takes a few minutes for the service to start. If
you visit the URL too soon, it might return `5XX` error HTTP code (the actual code might be dependent on your network implementation).

If you waited long enough (more than 10 minutes), and the service is still not accessible, it is time to investigate the reason why this is the case.

### Helm release verification 

1. Run `helm list --all-namespaces` to get list of all installed chart releases.
    * You should be able to see your installation in the list.
    * The status for the release should be `deployed`.
2. Run `helm test RELEASE_NAME -n NAMESPACE`
    * This should return application tests in `succeeded` phase.
    * In case there are any test failures you will need to investigate the particular domain further.
   
### DNS verification

Verify the DNS record from your machine, run basic `dig` test:

```shell
dig SERVICE_DOMAIN_NAME
```

Or use a [web version of the tool](https://toolbox.googleapps.com/apps/dig/).

### Investigate application logs

You can get application logs from the pods with standard `kubectl` command:

```shell
kubectl logs APPLICATION_POD_NAME
```

You can read the output and make sure it doesn't contain an error or exception.

### Get application pod details

Follow the [official guide](https://kubernetes.io/docs/tasks/debug-application-cluster/debug-application-introspection/) for more details.

Get list of pods and their state:

```shell
kubectl get RELEASE_NAME -n NAMESPACE -o wide
```

Get details about specific pod:

```shell
kubectl describe POD_NAME -n NAMESPACE
```