package test.model;

import test.helm.Helm;

public enum Product {
    jira {
        @Override
        public String getDockerImageName() {
            return "atlassian/jira-software";
        }
    },
    confluence {
        @Override
        public String getDockerImageName() {
            return "atlassian/confluence-server";
        }
    },
    bitbucket {
        @Override
        public String getDockerImageName() {
            return "atlassian/bitbucket-server";
        }
    };

    public abstract String getDockerImageName();

    public String getHelmReleaseName() {
        return Helm.getHelmReleaseName(this);
    }
}
