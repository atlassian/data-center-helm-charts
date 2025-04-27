# Breaking Changes

## Version 2.0.0 [not released]

### Required values

* `.Values.bitbucket.clustering.group` is required for Bitbucket
* `.Values.bitbucket.mirror` is required for Bitbucket
* `.Values.bitbucket.podManagementStrategy` is required for Bitbucket
* `.Values.ingress.className` is required for all products

### `securityContext` format change
`.Values.<product>.securityContext` is now fully configurable with standard [Kubernetes values](https://kubernetes.io/docs/tasks/configure-pod-container/security-context/).    
In order to make this happen, we removed `gid` and `enabled` fields from `securityContext` stanza in `values.yaml` file. 
Instead, the standard `fsGroup` value is now the default parameter to make ensure the correct filesystem permissions. This setting is not necessary in OpenShift (or some other specific use cases).

#### Action required

- If `.Values.<product>.securityContext.enabled` was set to `true` (the default value), set the existing `.Values.<product>.securityContext.gid` value to `.Values.<product>.securityContext.fsGroup`.
- If `.Values.<product>.securityContext.enabled` was set to `false`, comment out all keys under `.Values.<product>.securityContext.securityContext` stanza. This means the `.Values.<product>.securityContext` should be empty.

#### Troubleshooting

If you encounter this error:

```
Error: unable to build kubernetes objects from release manifest: error validating "": error validating data: [ValidationError(StatefulSet.spec.template.spec.securityContext): unknown field "enabled" in io.k8s.api.core.v1.PodSecurityContext, ValidationError(StatefulSet.spec.template.spec.securityContext): unknown field "gid" in io.k8s.api.core.v1.PodSecurityContext]
```

It means your `value.yaml` file contains the unsupported `enabled` or `gid` values for the `securityContext`. Please follow the steps from `securityContext` change above.

### OpenSearch Credentials in Confluence Helm Chart

OpenSearch credentials are now passed as environment variables instead of JVM arguments in the ConfigMap. This is a breaking change for existing Confluence Helm charts that use bundled OpenSearch (`opensearch.enabled` is set to true).

In **2.0.0** `-Dopensearch.password` has been removed from config-jvm ConfigMap. OpenSearch credentials and other relevant settings are now passed as environment variables and written to `confluence.cfg.xml` in the image entrypoint (See: [Add Opensearch properties](https://bitbucket.org/atlassian-docker/docker-atlassian-confluence-server/pull-requests/192/overview)).

When upgrading Confluence Helm chart to version 2.0.0, you **must** set `confluence.forceConfigUpdate` to true in Helm values file, which will force the image entrypoint to recreate the `confluence.cfg.xml` file with the new Opensearch properties.
This is a one-time operation. After the upgrade, you can set `confluence.forceConfigUpdate` to false.
