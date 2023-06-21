# AWS S3 Avatars Storage

Since 9.9.0 Jira supports storing avatars in AWS S3. To enable this feature, update the image `tag` to `9.9.0` and define bucket name and AWS region in `jira.s3Storage.avatars`, for example:

```yaml
tag: 9.9.0
jira:
  s3Storage:
    avatars:
      bucketName: jira-avatars-bucket
      bucketRegion: us-east-1
```

# AWS Authentication

You will find details on available authentication methods in [Credential Provider](https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/auth/credentials/DefaultCredentialsProvider.html).

Make sure `ATL_UNSET_SENSITIVE_ENV_VARS` is set to false if you choose to define `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` in `jira.additionalEnvironmentVariables`:

```yaml
additionalEnvironmentVariables:
  - name: AWS_ACCESS_KEY_ID
    valueFrom:
      secretKeyRef:
        name: aws-creds
        key: AWS_ACCESS_KEY_ID
  - name: AWS_SECRET_ACCESS_KEY
    valueFrom:
      secretKeyRef:
        name: aws-creds
        key: AWS_SECRET_ACCESS_KEY
  - name: ATL_UNSET_SENSITIVE_ENV_VARS
    value: "false"
```

## EKS IRSA

If Jira is deployed to AWS EKS, it is strongly recommended to use [IAM roles for service accounts (IRSA)](https://docs.aws.amazon.com/eks/latest/userguide/iam-roles-for-service-accounts.html).

The Jira service account will be automatically annotated with a role `ARN` if it is defined, for example:

```yaml
serviceAccount:
  eksIrsa:
    roleArn: arn:aws:iam::37583956:role/jira-s3-role
```

Below is an example policy, providing appropriate S3 access to Jira, that needs to be attached to the role:

```json
{
    "Statement": [
        {
            "Action": [
                "s3:PutObject",
                "s3:ListBucket",
                "s3:GetObject",
                "s3:DeleteObject"
            ],
            "Effect": "Allow",
            "Resource": [
                "arn:aws:s3:::jira-avatars-bucket/*",
                "arn:aws:s3:::jira-avatars-bucket"
            ],
            "Sid": ""
        }
    ],
    "Version": "2012-10-17"
}
```
