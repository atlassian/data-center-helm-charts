package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Product;

import java.util.Map;

class JiraS3EnabledTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }


    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"jira"})
    void jira_s3_avatars_storage_env_vars(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".s3Storage.avatars.bucketName", "my-bucket",
                product + ".s3Storage.avatars.bucketRegion", "my-region"
        ));

        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer()
                .getEnv()
                .assertHasValue("ATL_S3AVATARS_BUCKET_NAME", "my-bucket")
                .assertHasValue("ATL_S3AVATARS_REGION", "my-region")
                .assertDoesNotHaveAnyOf("ATL_S3AVATARS_ENDPOINT_OVERRIDE");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"jira"})
    void jira_s3_avatars_storage_endpoint_override(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".s3Storage.avatars.bucketName", "my-bucket",
                product + ".s3Storage.avatars.bucketRegion", "my-region",
                product + ".s3Storage.avatars.endpointOverride", "http://minio.svc.cluster.local"
        ));

        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer()
                .getEnv()
                .assertHasValue("ATL_S3AVATARS_BUCKET_NAME", "my-bucket")
                .assertHasValue("ATL_S3AVATARS_REGION", "my-region")
                .assertHasValue("ATL_S3AVATARS_ENDPOINT_OVERRIDE", "http://minio.svc.cluster.local");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"jira"})
    void jira_s3_avatars_storage_missing_env_vars(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product + ".s3Storage.avatars.bucketName", "my-bucket"
        ));
        resources.getStatefulSet(product.getHelmReleaseName())
                .getContainer()
                .getEnv()
                .assertDoesNotHaveAnyOf("ATL_S3AVATARS_BUCKET_NAME")
                .assertDoesNotHaveAnyOf("ATL_S3AVATARS_REGION")
                .assertDoesNotHaveAnyOf("ATL_S3AVATARS_ENDPOINT_OVERRIDE");
    }

}
