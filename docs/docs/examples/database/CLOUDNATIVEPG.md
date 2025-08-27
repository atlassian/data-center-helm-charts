# CloudNativePG Database Setup

This document describes how to set up PostgreSQL databases using CloudNativePG for Atlassian Data Center applications.

## Overview

CloudNativePG is a Kubernetes operator that manages PostgreSQL clusters natively in Kubernetes. It has replaced the previous Bitnami PostgreSQL Helm chart setup in our test environments.

## Installation

### Prerequisites

- Kubernetes cluster (version 1.21 or higher)
- Helm 3.x
- kubectl configured to access your cluster

### Install CloudNativePG Operator

```bash
# Add the CloudNativePG Helm repository
helm repo add cnpg https://cloudnative-pg.github.io/charts
helm repo update

# Install the operator
helm install cnpg-operator cnpg/cloudnative-pg \
  --namespace cnpg-system \
  --create-namespace \
  --wait
```

### Create a PostgreSQL Cluster

Create a PostgreSQL cluster for your Atlassian application:

```yaml
apiVersion: postgresql.cnpg.io/v1
kind: Cluster
metadata:
  name: atlassian-postgres
  namespace: atlassian
spec:
  instances: 1
  
  postgresql:
    parameters:
      max_connections: "200"
      shared_buffers: "256MB"
      effective_cache_size: "1GB"
      
  bootstrap:
    initdb:
      database: jira
      owner: jira
      secret:
        name: postgres-credentials
        
  storage:
    size: 10Gi
    storageClass: gp2
    
  resources:
    requests:
      memory: "1Gi"
      cpu: "500m"
    limits:
      memory: "2Gi"
      cpu: "1000m"
      
  monitoring:
    enabled: true
```

### Create Database Credentials

```bash
kubectl create secret generic postgres-credentials \
  --from-literal=username=jira \
  --from-literal=password=your-secure-password \
  --namespace atlassian
```

## Service Names and Connection Strings

CloudNativePG creates multiple services for each cluster:

- `<cluster-name>-rw`: Read-write service (primary)
- `<cluster-name>-ro`: Read-only service (replicas)
- `<cluster-name>-r`: Read service (any instance)

### Connection String Examples

For Atlassian applications, use the read-write service:

```yaml
database:
  type: postgresql  # or postgres72 for Jira
  url: jdbc:postgresql://atlassian-postgres-rw:5432/jira
  driver: org.postgresql.Driver
  credentials:
    secretName: jira-database-credentials
```

## Migration from Bitnami PostgreSQL

If you're migrating from Bitnami PostgreSQL:

1. **Update service names**: Change from `postgres` to `postgres-rw`
2. **Update connection strings**: Use CloudNativePG service naming
3. **Update deployment scripts**: Replace Bitnami Helm chart with CloudNativePG operator
4. **Update credentials**: CloudNativePG uses different secret formats

### Example Migration

**Before (Bitnami):**
```yaml
database:
  url: jdbc:postgresql://postgres:5432/jira
```

**After (CloudNativePG):**
```yaml
database:
  url: jdbc:postgresql://postgres-rw:5432/jira
```

## Monitoring and Observability

CloudNativePG provides built-in Prometheus metrics:

```yaml
spec:
  monitoring:
    enabled: true
    customQueriesConfigMap:
      - name: custom-queries
        key: queries.yaml
```

## Backup and Recovery

CloudNativePG supports automated backups:

```yaml
spec:
  backup:
    retentionPolicy: "30d"
    barmanObjectStore:
      destinationPath: "s3://my-backup-bucket/postgres"
      s3Credentials:
        accessKeyId:
          name: backup-credentials
          key: ACCESS_KEY_ID
        secretAccessKey:
          name: backup-credentials
          key: SECRET_ACCESS_KEY
```

## Troubleshooting

### Common Issues

1. **Pod not starting**: Check resource limits and storage availability
2. **Connection refused**: Verify service names and port configurations
3. **Authentication failed**: Check credentials secret format

### Useful Commands

```bash
# Check cluster status
kubectl get clusters -n atlassian

# View cluster details
kubectl describe cluster atlassian-postgres -n atlassian

# Check pod logs
kubectl logs -l cnpg.io/cluster=atlassian-postgres -n atlassian

# Connect to database
kubectl exec -it atlassian-postgres-1 -n atlassian -- psql -U jira -d jira
```

## References

- [CloudNativePG Documentation](https://cloudnative-pg.io/)
- [CloudNativePG GitHub Repository](https://github.com/cloudnative-pg/cloudnative-pg)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)