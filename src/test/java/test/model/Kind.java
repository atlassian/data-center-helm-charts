package test.model;

/**
 * The different types of Kubernetes resource we use.
 */
public enum Kind {
    StatefulSet, Deployment, ServiceAccount, ConfigMap, Secret, Service, Pod, Job, ClusterRole, ClusterRoleBinding, PersistentVolume, PersistentVolumeClaim, Ingress
}
