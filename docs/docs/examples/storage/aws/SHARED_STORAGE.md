# Shared storage
This file provides examples on how a Kubernetes cluster and helm deployment can be configured to utilize an AWS EFS backed filesystem.

## Static provisioning
An example detailing how an existing EFS filesystem can be created and consumed using static provisioning.

## Prerequisites
1. [EFS](https://github.com/kubernetes-sigs/aws-efs-csi-driver){.external} CSI driver is [installed](https://docs.aws.amazon.com/eks/latest/userguide/efs-csi.html){.external} within the k8s cluster.
2. A physical EFS filesystem has been [provisioned](https://docs.aws.amazon.com/eks/latest/userguide/efs-csi.html#efs-create-filesystem){.external}

Additional details on static EFS provisioning can be found [here](https://github.com/kubernetes-sigs/aws-efs-csi-driver/tree/master/examples/kubernetes/static_provisioning){.external}

You can confirm that the EFS CSI driver has been installed by running:

```shell
kubectl get csidriver
```
the output of the above command should include the named driver `efs.csi.aws.com` for example:
```shell
NAME              ATTACHREQUIRED   PODINFOONMOUNT   MODES        AGE
efs.csi.aws.com   false            false            Persistent   5d3h
```

### Provisioning
1. Create a Persistent Volume
2. Create a Persistent Volume Claim
3. Update `values.yaml` to utilise Persistent Volume Claim

#### 1. Create Persistent Volume
Create a persistent volume for the pre-provisioned EFS filesystem by providing the `<efs-id>`. The EFS id can be identified using the CLI command below with the appropriate region

```yaml
aws efs describe-file-systems --query "FileSystems[*].FileSystemId" --region ap-southeast-2
```

```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: my-shared-vol-pv
spec:
  capacity:
    storage: 1Gi
  volumeMode: Filesystem
  accessModes:
    - ReadWriteMany
  storageClassName: efs-pv
  persistentVolumeReclaimPolicy: Retain
  mountOptions:
    - rw
    - lookupcache=pos
    - noatime
    - intr
    - _netdev
  csi:
    driver: efs.csi.aws.com
    volumeHandle: <efs-id>
```

#### 2. Create Persistent Volume Claim
```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: my-shared-vol-pvc
spec:
  accessModes:
    - ReadWriteMany
  storageClassName: efs-pv
  volumeMode: Filesystem
  volumeName: my-shared-vol-pv
  resources:
    requests:
      storage: 1Gi
```

#### 3. Update values.yaml
Update the `sharedHome` `claimName` value within `values.yaml` to the name of the Persistent Volume Claim created in step 2 above

```yaml
volumes:
  sharedHome:
    customVolume:
      persistentVolumeClaim:
        claimName: "my-shared-vol-pvc"
```

# Resources
Some useful resources on provisioning shared storage with the AWS CSI Driver:

- [Amazon EFS CSI driver]( https://docs.aws.amazon.com/eks/latest/userguide/efs-csi.html){.external}
- [Introducing Amazon EFS CSI dynamic provisioning](https://aws.amazon.com/blogs/containers/introducing-efs-csi-dynamic-provisioning/){.external}

---
!!!tip "Next step - Local storage"

    Having created the shared home volume continue with provisioning the next piece of prerequisite infrastructure, [local storage](../../../userguide/PREREQUISITES.md#configure-local-home-volume).
