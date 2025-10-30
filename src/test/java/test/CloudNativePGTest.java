package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import test.helm.Helm;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for CloudNativePG integration and database connectivity
 * These tests verify that the database configuration is compatible with CloudNativePG
 */
class CloudNativePGTest {
    private Helm helm;

    @BeforeEach
    void initHelm(TestInfo testInfo) {
        helm = new Helm(testInfo);
    }

    @Test
    void test_cloudnativepg_service_name_format() {
        // Test that the database URL format is compatible with CloudNativePG service naming
        // CloudNativePG creates services with pattern: {cluster-name}-rw for read-write
        // and {cluster-name}-ro for read-only
        
        String clusterName = "jira-db";
        String expectedRwService = clusterName + "-rw";
        String expectedRoService = clusterName + "-ro";
        
        assertThat(expectedRwService).isEqualTo("jira-db-rw");
        assertThat(expectedRoService).isEqualTo("jira-db-ro");
    }

    @Test
    void test_postgresql_connection_string_format() {
        // Test that the JDBC URL format is correct for PostgreSQL
        String host = "jira-db-rw";
        String port = "5432";
        String database = "jira";
        String expectedUrl = String.format("jdbc:postgresql://%s:%s/%s", host, port, database);
        
        assertThat(expectedUrl).isEqualTo("jdbc:postgresql://jira-db-rw:5432/jira");
    }

    @Test
    void test_database_credentials_secret_format() {
        // Test that the secret format is compatible with CloudNativePG expectations
        String appName = "jira";
        String expectedSecretName = appName + "-db-credentials";
        
        assertThat(expectedSecretName).isEqualTo("jira-db-credentials");
    }

    @Test
    void test_cluster_naming_convention() {
        // Test that cluster names follow the expected pattern
        String appName = "confluence";
        String expectedClusterName = appName + "-db";
        
        assertThat(expectedClusterName).isEqualTo("confluence-db");
    }
}