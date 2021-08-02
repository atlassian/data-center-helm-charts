# Local storage
This file provides examples on how a Kubernetes cluster and helm deployment can be configured to utilize AWS EBS backed volumes.

## Dynamic provisioning
Due to the ephemeral nature of Kubernetes pods we advise dynamic provisioning be used for creating and consuming EBS volume(s)

### Prerequisites
1. [EBS](https://github.com/kubernetes-sigs/aws-ebs-csi-driver){.external} CSI driver is [installed](https://docs.aws.amazon.com/eks/latest/userguide/ebs-csi.html) within the k8s cluster. Ensure that `enableVolumeScheduling=true` is set when installing, see [here](https://github.com/kubernetes-sigs/aws-ebs-csi-driver/tree/master/examples/kubernetes/dynamic-provisioning){.external} for additional details.

You can confirm that the EBS CSI driver has been installed by running

```shell
kubectl get csidriver
```

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

- https://github.com/kubernetes-sigs/aws-ebs-csi-driver
- https://docs.aws.amazon.com/eks/latest/userguide/ebs-csi.html
- https://aws.amazon.com/blogs/containers/introducing-efs-csi-dynamic-provisioning/
