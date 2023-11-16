package test.model;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AnalyticsData {
    @JsonProperty("imageTag")
    private String imageTag;

    @JsonProperty("replicas")
    private int replicas;

    @JsonProperty("isJmxEnabled")
    private boolean isJmxEnabled;

    @JsonProperty("isIngressEnabled")
    private boolean isIngressEnabled;

    @JsonProperty("isIngressNginx")
    private boolean isIngressNginx;

    @JsonProperty("k8sVersion")
    private String k8sVersion;

    @JsonProperty("svcType")
    private String svcType;

    @JsonProperty("dbType")
    private String dbType;

    @JsonProperty("isS3AttachmentsStorageEnabled")
    private boolean isS3AttachmentsStorageEnabled;

    @JsonProperty("isS3AvatarsEnabled")
    private boolean isS3AvatarsEnabled;

    @JsonProperty("isClusteringEnabled")
    private boolean isClusteringEnabled;

    @JsonProperty("isSharedHomePVCCreated")
    private boolean isSharedHomePVCCreated;

    @JsonProperty("isServiceMonitorCreated")
    private boolean isServiceMonitorCreated;

    @JsonProperty("isGrafanaDashboardsCreated")
    private boolean isGrafanaDashboardsCreated;

    @JsonProperty("isBitbucketMeshEnabled")
    private boolean isBitbucketMeshEnabled;

    public String getImageTag() {
        return imageTag;
    }

    public void setImageTag(String imageTag) {
        this.imageTag = imageTag;
    }

    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }

    public boolean isJmxEnabled() {
        return isJmxEnabled;
    }

    public void setJmxEnabled(boolean jmxEnabled) {
        isJmxEnabled = jmxEnabled;
    }

    public boolean isIngressEnabled() {
        return isIngressEnabled;
    }

    public void setIngressEnabled(boolean ingressEnabled) {
        isIngressEnabled = ingressEnabled;
    }

    public boolean isIngressNginx() {
        return isIngressNginx;
    }

    public void setIngressNginx(boolean ingressNginx) {
        isIngressNginx = ingressNginx;
    }

    public String getK8sVersion() {
        return k8sVersion;
    }

    public void setK8sVersion(String k8sVersion) {
        this.k8sVersion = k8sVersion;
    }

    public String getSvcType() {
        return svcType;
    }

    public void setSvcType(String svcType) {
        this.svcType = svcType;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public boolean isS3AttachmentsStorageEnabled() {
        return isS3AttachmentsStorageEnabled;
    }

    public void setS3AttachmentsStorageEnabled(boolean s3AttachmentsStorageEnabled) {
        isS3AttachmentsStorageEnabled = s3AttachmentsStorageEnabled;
    }
    public boolean isS3AvatarsEnabled() {
        return isS3AvatarsEnabled;
    }

    public void setS3AvatarsEnabled(boolean s3AvatarsEnabled) {
        isS3AvatarsEnabled = s3AvatarsEnabled;
    }

    public boolean isBitbucketMeshEnabled() {
        return isBitbucketMeshEnabled;
    }

    public void setBitbucketMeshEnabled(boolean bitbucketMeshEnabled) {
        isBitbucketMeshEnabled = bitbucketMeshEnabled;
    }

    public boolean isClusteringEnabled() {
        return isClusteringEnabled;
    }

    public void setClusteringEnabled(boolean clusteringEnabled) {
        isClusteringEnabled = clusteringEnabled;
    }

    public boolean isSharedHomePVCCreated() {
        return isSharedHomePVCCreated;
    }

    public void setSharedHomePVCCreated(boolean sharedHomePVCCreated) {
        isSharedHomePVCCreated = sharedHomePVCCreated;
    }

    public boolean isServiceMonitorCreated() {
        return isServiceMonitorCreated;
    }

    public void setServiceMonitorCreated(boolean serviceMonitorCreated) {
        isServiceMonitorCreated = serviceMonitorCreated;
    }

    public boolean isGrafanaDashboardsCreated() {
        return isGrafanaDashboardsCreated;
    }

    public void setGrafanaDashboardsCreated(boolean grafanaDashboardsCreated) {
        isGrafanaDashboardsCreated = grafanaDashboardsCreated;
    }

    public AnalyticsData() {
    }

    public AnalyticsData(String imageTag, int replicas, boolean isJmxEnabled, boolean isIngressEnabled, boolean isIngressNginx, String k8sVersion, String svcType, String dbType, boolean isS3AttachmentsStorageEnabled, boolean isS3AvatarsEnabled, boolean isClusteringEnabled, boolean isSharedHomePVCCreated, boolean isBitbucketMeshEnabled, boolean isServiceMonitorCreated, boolean isGrafanaDashboardsCreated) {
        this.imageTag = imageTag;
        this.replicas = replicas;
        this.isJmxEnabled = isJmxEnabled;
        this.isIngressEnabled = isIngressEnabled;
        this.isIngressNginx = isIngressNginx;
        this.k8sVersion = k8sVersion;
        this.svcType = svcType;
        this.dbType = dbType;
        this.isS3AttachmentsStorageEnabled = isS3AttachmentsStorageEnabled;
        this.isS3AvatarsEnabled = isS3AvatarsEnabled;
        this.isClusteringEnabled = isClusteringEnabled;
        this.isSharedHomePVCCreated = isSharedHomePVCCreated;
        this.isBitbucketMeshEnabled = isBitbucketMeshEnabled;
        this.isServiceMonitorCreated = isServiceMonitorCreated;
        this.isGrafanaDashboardsCreated = isGrafanaDashboardsCreated;
    }
}
