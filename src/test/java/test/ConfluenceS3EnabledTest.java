package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import test.helm.Helm;
import test.model.Product;

import java.util.Map;

import static test.jackson.JsonNodeAssert.assertThat;

class ConfluenceS3EnabledTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }


    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"})
    void confluence_s3_storage_env_vars(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product+".s3AttachmentsStorage.bucketName", "my-bucket",
                product+".s3AttachmentsStorage.bucketRegion", "my-region"
        ));

        final var configMap = resources.getConfigMap(product.getHelmReleaseName() + "-jvm-config").getDataByKey("additional_jvm_args");
        assertThat(configMap).hasTextContaining("-Dconfluence.filestore.attachments.s3.bucket.name=my-bucket");
        assertThat(configMap).hasTextContaining("-Dconfluence.filestore.attachments.s3.bucket.region=my-region");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"})
    void confluence_s3_storage_endpoint_override(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product+".s3AttachmentsStorage.bucketName", "my-bucket",
                product+".s3AttachmentsStorage.bucketRegion", "my-region",
                product+".s3AttachmentsStorage.endpointOverride", "http://minio.svc.cluster.local"
        ));

        final var configMap = resources.getConfigMap(product.getHelmReleaseName() + "-jvm-config").getDataByKey("additional_jvm_args");
        assertThat(configMap).hasTextContaining("-Dconfluence.filestore.attachments.s3.bucket.name=my-bucket");
        assertThat(configMap).hasTextContaining("-Dconfluence.filestore.attachments.s3.endpoint.override=http://minio.svc.cluster.local");
    }

    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"})
    void confluence_s3_storage_no_endpoint_override(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product+".s3AttachmentsStorage.bucketName", "my-bucket",
                product+".s3AttachmentsStorage.bucketRegion", "my-region"
        ));

        final var configMap = resources.getConfigMap(product.getHelmReleaseName() + "-jvm-config").getDataByKey("additional_jvm_args");
        assertThat(configMap).hasTextNotContaining("-Dconfluence.filestore.attachments.s3.endpoint.override");
    }
    @ParameterizedTest
    @EnumSource(value = Product.class, names = {"confluence"})
    void confluence_s3_storage_missing_env_vars(Product product) throws Exception {
        final var resources = helm.captureKubeResourcesFromHelmChart(product, Map.of(
                product+".s3AttachmentsStorage.bucketName", "my-bucket"
        ));

        final var configMap = resources.getConfigMap(product.getHelmReleaseName() + "-jvm-config").getDataByKey("additional_jvm_args");
        assertThat(configMap).hasTextNotContaining("-Dconfluence.filestore.attachments.s3.bucket.name=my-bucket");
    }

}
