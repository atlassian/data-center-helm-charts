# Verification

From release 1.11.0, all the Helm charts are signed with GPG key, following the instruction by official [Helm documentation](https://helm.sh/docs/topics/provenance/). 

To verify the integrity of the charts, 
1. download chart `.tgz` file, `.prov` file and `helm_key.pub` from [release assets](https://github.com/atlassian/data-center-helm-charts/releases), 

2. and then import the public key into your GPG keyring. (Install GnuPG tool if you haven't done so already.) 
    
    ```shell
    gpg --import helm_key.pub 
    ```
   
3. because Helm currently only supports the legacy gpg format, export the keyring to the legacy format:
    ```
    gpg --export >~/.gnupg/pubring.gpg
    ```
   
4. verify the chart.
    ```
    helm verify /path/to/product.tgz 
    ```
   
If the verification is successful, the output would be something like: 
```shell
helm verify ~/Downloads/jira-1.11.0.tgz                                                                         
Signed by: Atlassian DC Deployments <dc-deployments@atlassian.com>
Using Key With Fingerprint: DD1A5B2F7A599129274FB10AD38C66448E19B403
Chart Hash Verified: sha256:ca102cbf416a5c87998d06ba4527b5afc99e1d7d1776317ddd07720251715fde
```