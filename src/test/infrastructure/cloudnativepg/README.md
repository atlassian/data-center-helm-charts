# CloudNativePG Test Infrastructure

Configuration files for deploying PostgreSQL databases using CloudNativePG operator in test environments.

## Overview

CloudNativePG operator is used to manage PostgreSQL clusters for Data Center application testing.

## Files

### `operator-values.yaml`
Configuration for the CloudNativePG operator Helm chart. This includes:
- Resource limits suitable for test environments
- Security context configuration
- Monitoring settings (disabled for tests)
- RBAC and service account settings

### `cluster-template.yaml`
Template for creating PostgreSQL clusters. Features:
- Single instance configuration for test environments
- PostgreSQL 16.2 (matching previous Bitnami version)
- Resource limits: 256Mi-1Gi memory, 100m-1000m CPU
- Ephemeral storage (1Gi) suitable for tests
- Performance optimizations for test environments (fsync disabled)
- Database and user creation via bootstrap configuration

### `init-scripts/init-db.sql`
Database initialization script that:
- Creates necessary PostgreSQL extensions
- Sets up proper permissions for application users
- Creates health check functions
- Provides initialization logging

## Usage

The CloudNativePG deployment is handled automatically by the test scripts:

### Local Testing (KinD)
```bash
# Deploy PostgreSQL cluster for Jira
DC_APP=jira source src/test/scripts/kind/deploy_app.sh
deploy_postgres
```

### CI/CD Testing
The GitHub Actions workflows automatically use the updated deployment scripts.

## Database Connection

Applications connect to the database using:
- **Service Name**: `{app-name}-db-rw` (read-write) or `{app-name}-db-ro` (read-only)
- **Port**: 5432
- **Database**: Same as application name
- **Credentials**: Stored in `{app-name}-db-credentials` secret

Example connection string for Jira:
```
jdbc:postgresql://jira-db-rw:5432/jira
```

## Monitoring

Monitoring is disabled in test environments to reduce resource usage. For production deployments, you can enable:
- Pod monitors for Prometheus integration
- Grafana dashboards
- Custom monitoring queries

## Troubleshooting

### Check Operator Status
```bash
kubectl get deployment cnpg-operator-controller-manager -n cnpg-system
```

### Check Cluster Status
```bash
kubectl get cluster {app-name}-db -n atlassian
kubectl describe cluster {app-name}-db -n atlassian
```

### Check Pod Status
```bash
kubectl get pods -l cnpg.io/cluster={app-name}-db -n atlassian
```

### Access Database
```bash
# Get primary pod name
PRIMARY_POD=$(kubectl get pods -n atlassian -l cnpg.io/cluster={app-name}-db,role=primary -o jsonpath='{.items[0].metadata.name}')

# Connect to database
kubectl exec -it ${PRIMARY_POD} -n atlassian -- psql -U {app-name} -d {app-name}
```

## Performance Considerations

The test configuration includes several performance optimizations:
- `fsync=off` - Disables synchronous writes (safe for ephemeral test data)
- `synchronous_commit=off` - Allows asynchronous commits
- `full_page_writes=off` - Reduces I/O for test workloads

**Warning**: These settings are only suitable for test environments with ephemeral data.

## Migration Notes

Key differences from Bitnami PostgreSQL:
1. **Service Names**: Changed from `postgres` to `{app-name}-db-rw`
2. **Pod Names**: Changed from `postgres-0` to `{app-name}-db-1`
3. **Operator Management**: Clusters are managed by the CloudNativePG operator
4. **Configuration**: Uses Kubernetes CRDs instead of Helm values
5. **Monitoring**: Native Prometheus integration available

## Resources

- [CloudNativePG Documentation](https://cloudnative-pg.io/documentation/)
- [CloudNativePG Helm Chart](https://github.com/cloudnative-pg/charts)
- [PostgreSQL Configuration Reference](https://www.postgresql.org/docs/current/runtime-config.html)