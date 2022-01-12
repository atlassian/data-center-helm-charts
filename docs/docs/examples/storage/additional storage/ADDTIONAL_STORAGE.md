# Additional Storage
You can use [volumeClaimTemplates](https://kubernetes.io/docs/concepts/workloads/controllers/statefulset/#volume-claim-templates) to have additional storage. This is useful when your enviroment uses several types of storage. 

E.g. If you want to deploy Confluence on NFS, but you want to use BlockStorage (or everything else instead of NFS) for the lucene-index, you can create extra volumn for BlockStorage by defining `volumeClaimTemplates` in `values.yaml` then mount the volume in `additionalVolumeMounts`.
```yaml
confluence:
  additionalVolumeClaimTemplates:
    - name: myadditionalvolumeclaim
      storageClassName: gp2
      resources:
        requests:
          storage: 1Gi
  additionalVolumeMounts:
    - mountPath: /var/atlassian/application-data/confluence/index
      name: myadditionalvolumeclaim
```