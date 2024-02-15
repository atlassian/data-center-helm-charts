package test.model;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AnalyticsData {
    @JsonProperty("imageTag")
    private String imageTag;

    @JsonProperty("replicas")
    private int replicas;

    @JsonProperty("isJmxEnabled")
    private boolean isJmxEnabled;

    @JsonProperty("k8sVersion")
    private String k8sVersion;

    @JsonProperty("serviceType")
    private String serviceType;

    @JsonProperty("dbType")
    private String dbType;

    @JsonProperty("ingressType")
    private String ingressType;

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

    @JsonProperty("isRunOnOpenshift")
    private boolean isRunOnOpenshift;

    @JsonProperty("isRunWithRestrictedSCC")
    private boolean isRunWithRestrictedSCC;

    @JsonProperty("isOpenshiftRouteCreated")
    private boolean isOpenshiftRouteCreated;

    public String getImageTag() {
        return imageTag;
    }

    public void setImageTag(String imageTag) {
        this.imageTag = imageTag;
    }

    public String getIngressType() {
        return ingressType;
    }

    public void setIngressType(String ingressType) {
        this.ingressType = ingressType;
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

    public String getK8sVersion() {
        return k8sVersion;
    }

    public void setK8sVersion(String k8sVersion) {
        this.k8sVersion = k8sVersion;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
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

    public boolean isRunOnOpenshift() {
        return isRunOnOpenshift;
    }

    public void setRunOnOpenshift(boolean runOnOpenshift) {
        isRunOnOpenshift = runOnOpenshift;
    }

    public boolean isRunWithRestrictedSCC() {
        return isRunWithRestrictedSCC;
    }

    public void setRunWithRestrictedSCC(boolean runWithRestrictedSCC) {
        isRunWithRestrictedSCC = runWithRestrictedSCC;
    }

    public boolean isOpenshiftRouteCreated() {
        return isOpenshiftRouteCreated;
    }

    public void setOpenshiftRouteCreated(boolean openshiftRouteCreated) {
        isOpenshiftRouteCreated = openshiftRouteCreated;
    }

    public AnalyticsData() {
    }

    public AnalyticsData(String imageTag, int replicas, boolean isJmxEnabled, String ingressType, String k8sVersion, String serviceType, String dbType, boolean isS3AttachmentsStorageEnabled, boolean isS3AvatarsEnabled, boolean isClusteringEnabled, boolean isSharedHomePVCCreated, boolean isBitbucketMeshEnabled, boolean isServiceMonitorCreated, boolean isGrafanaDashboardsCreated, boolean isRunOnOpenshift, boolean isRunWithRestrictedSCC, boolean isOpenshiftRouteCreated) {
        this.imageTag = imageTag;
        this.replicas = replicas;
        this.isJmxEnabled = isJmxEnabled;
        this.ingressType = ingressType;
        this.k8sVersion = k8sVersion;
        this.serviceType = serviceType;
        this.dbType = dbType;
        this.isS3AttachmentsStorageEnabled = isS3AttachmentsStorageEnabled;
        this.isS3AvatarsEnabled = isS3AvatarsEnabled;
        this.isClusteringEnabled = isClusteringEnabled;
        this.isSharedHomePVCCreated = isSharedHomePVCCreated;
        this.isBitbucketMeshEnabled = isBitbucketMeshEnabled;
        this.isServiceMonitorCreated = isServiceMonitorCreated;
        this.isGrafanaDashboardsCreated = isGrafanaDashboardsCreated;
        this.isRunOnOpenshift = isRunOnOpenshift;
        this.isRunWithRestrictedSCC = isRunWithRestrictedSCC;
        this.isOpenshiftRouteCreated = isOpenshiftRouteCreated;
    }
}
