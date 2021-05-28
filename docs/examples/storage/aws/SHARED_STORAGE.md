# Shared storage
This readme provides examples on how a Kubernetes cluster and helm deployment can be configured to utilize an AWS EFS backed filesystem.

## Static provisioning
An example detailing how an existing EFS filesystem can be created and consumed using static provisioning.

## Pre-requisites
1. [EFS](https://github.com/kubernetes-sigs/aws-efs-csi-driver) CSI driver is [installed](https://docs.aws.amazon.com/eks/latest/userguide/efs-csi.html) within the k8s cluster.
2. A physical EFS filesystem has been [provisioned](https://docs.aws.amazon.com/eks/latest/userguide/efs-csi.html#efs-create-filesystem)

Additional details on static EFS provisioning can be found [here](https://github.com/kubernetes-sigs/aws-efs-csi-driver/tree/master/examples/kubernetes/static_provisioning)

Confirm that the EFS CSI driver has been installed by running

```shell
kubectl get csidriver
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
  name: efs-pv
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
  name: efs-pvc
spec:
  accessModes:
    - ReadWriteMany
  storageClassName: efs-pv
  volumeMode: Filesystem
  volumeName: efs-pv
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
        claimName: "efs-pvc" 
```

# Resources
Some useful resources on provisioning shared storage with the AWS CSI Driver

- https://docs.aws.amazon.com/eks/latest/userguide/efs-csi.html
- https://aws.amazon.com/blogs/containers/introducing-efs-csi-dynamic-provisioning/
