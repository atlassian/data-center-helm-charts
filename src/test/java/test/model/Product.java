package test.model;

import test.helm.Helm;

public enum Product {
    jira {
        @Override
        public String getDockerImageName() {
            return "atlassian/jira-software";
        }

        @Override
        public String getContainerGid() {
            return "2001";
        }
    },
    confluence {
        @Override
        public String getDockerImageName() {
            return "atlassian/confluence-server";
        }

        @Override
        public String getContainerGid() {
            return "2002";
        }
    },
    bitbucket {
        @Override
        public String getDockerImageName() {
            return "atlassian/bitbucket-server";
        }

        @Override
        public String getContainerGid() {
            return "2003";
        }
    };

    public abstract String getDockerImageName();

    public abstract String getContainerGid();

    public String getHelmReleaseName() {
        return Helm.getHelmReleaseName(this);
    }
}
