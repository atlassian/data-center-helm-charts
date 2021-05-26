# Local storage
This readme provides examples on how a Kubernetes cluster and helm deployment can be configured to utilize AWS EBS backed volumes.

## Static provisioning
An example detailing how an existing EBS volume can be created and consumed using static provisioning.

### Pre-requisites
1. [EBS](https://github.com/kubernetes-sigs/aws-ebs-csi-driver) CSI driver is [installed](https://docs.aws.amazon.com/eks/latest/userguide/ebs-csi.html) within the k8s cluster.
2. A physical EBS volume has been [pre-provisioned](https://docs.aws.amazon.com/cli/latest/reference/ec2/create-volume.html)

Additional details on static EBS provisioning can be found [here](https://github.com/kubernetes-sigs/aws-ebs-csi-driver/tree/master/examples/kubernetes/static-provisioning)

### Provisioning
1. Create a Persistent Volume
2. Create a Persistent Volume Claim
3. Update `values.yaml` to utilise Persistent Volume Claim

#### 1. Create Persistent Volume
```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: ebs-pv
spec:
  capacity:
    storage: 1Gi
  volumeMode: Filesystem
  accessModes:
    - ReadWriteOnce
  storageClassName: ebs-pv
  csi:
    driver: ebs.csi.aws.com
    volumeHandle: vol-01ced17b3b13299bc
    fsType: xfs
  nodeAffinity:
    required:
      nodeSelectorTerms:
        - matchExpressions:
            - key: topology.ebs.csi.aws.com/zone
              operator: In
              values:
                - ap-southeast-2a
```

Note the use of node affinity. Because the EBS volume is created in `ap-southeast-2a` only a node in the same `AZ` can consume this persistent volume.

#### 2. Create Persistent Volume Claim
```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: ebs-pvc
spec:
  accessModes:
    - ReadWriteOnce
  storageClassName: ebs-pv
  volumeName: ebs-pv
  resources:
    requests:
      storage: 1Gi
```

#### 3. Update values.yaml
Update the `localHome` `claimName` value within `values.yaml` to the name of the Persistent Volume Claim created in step 2 above

```yaml
volumes:
  localHome:
    customVolume:
      persistentVolumeClaim:
        claimName: "ebs-pvc" 
```

:information_source:  Attaching multiple pods to a single EBS volume is not possible (a `Multi-Attach error for volume` will be raised) nor is it advised. As such you will need to create a separate EBS/PV/PVC for each additional pod that you want to add to the cluster. This is approach may suit single pod clusters but is obviously very cumbersome for multi pod clustered deployments. For such situations [dynamic provisioning](#Dynamic-provisioning) should be used.

---

## Dynamic provisioning
An example detailing how an EBS volume(s) can be created and consumed using dynamic provisioning.

### Pre-requisites
1. [EBS](https://github.com/kubernetes-sigs/aws-ebs-csi-driver) CSI driver is [installed](https://docs.aws.amazon.com/eks/latest/userguide/ebs-csi.html) within the k8s cluster. Ensure that `enableVolumeScheduling=true` is set when installing, see [here](https://github.com/kubernetes-sigs/aws-ebs-csi-driver/tree/master/examples/kubernetes/dynamic-provisioning) for additional details.

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
