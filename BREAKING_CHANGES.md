# Breaking Changes

## Release 2.0.0
### securityContext change
`.Values.<product>.securityContext` is now fully configurable.    
In order to make this happen, we removed `gid` and `enabled` fields from `securityContext` stanza in values.yaml file. 
Instead, `fsGroup` is now a required field for non-OpenShift deployment. 

*Action required*:

- If `enabled` was set to `true`, put the previous `gid` value to `fsGroup`.
- If `enabled` was set to `false`, comment out all fields under `securityContext` stanza. 



