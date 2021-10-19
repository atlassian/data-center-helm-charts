# Local storage
This file provides examples on how a Kubernetes cluster and helm deployment can be configured to utilize AWS EBS backed volumes.

## Dynamic provisioning
Due to the ephemeral nature of Kubernetes pods we advise dynamic provisioning be used for creating and consuming EBS volume(s).

### Prerequisites
Ensure the [EBS](https://github.com/kubernetes-sigs/aws-ebs-csi-driver){.external} CSI driver is installed within the k8s cluster, you can confirm this by running:  

```shell
kubectl get csidriver
```
the output of the above command should include the named driver `ebs.csi.aws.com` for example:
```shell
NAME              ATTACHREQUIRED   PODINFOONMOUNT   MODES        AGE
ebs.csi.aws.com   true             false            Persistent   5d1h
```
If not present the EBS driver can be installed using the following instructions [here](https://docs.aws.amazon.com/eks/latest/userguide/ebs-csi.html){.external}.


### Provisioning

1. Create a Storage Class
2. Update `values.yaml` to utilise Storage Class

#### 1. Create Storage Class

```yaml
kind: StorageClass
apiVersion: storage.k8s.io/v1
metadata:
  name: ebs-sc
provisioner: ebs.csi.aws.com
volumeBindingMode: WaitForFirstConsumer
```

#### 2. Update values.yaml

Update the `localHome` `storageClassName` value within `values.yaml` to the name of the Storage Class created in step 1 above

```yaml
volumes:
  localHome:
    persistentVolumeClaim:
      create: true
      storageClassName: "ebs-sc"
```

---

# Resources
Some useful resources on provisioning local storage with the AWS CSI Driver

- [EBS CSI driver - GitHub Repo](https://github.com/kubernetes-sigs/aws-ebs-csi-driver)
- [Official Amazon EBS CSI driver documentation](https://docs.aws.amazon.com/eks/latest/userguide/ebs-csi.html)
---

!!!tip "Product installation" 

    Creating the local home volume is the final step in provisioning the [required infrastructure](../../../userguide/PREREQUISITES.md). You can now move onto the next step, [Installation](../../../userguide/INSTALLATION.md).

