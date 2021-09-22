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
            return "atlassian/confluence";
        }

        @Override
        public String getContainerGid() {
            return "2002";
        }
    },
    crowd {
        @Override
        public String getDockerImageName() {
            return "atlassian/crowd";
        }

        @Override
        public String getContainerGid() {
            return "2004";
        }
    },
    bitbucket {
        @Override
        public String getDockerImageName() {
            return "atlassian/bitbucket";
        }

        @Override
        public String getContainerGid() {
            return "2003";
        }
    },
    bamboo {
        @Override
        public String getDockerImageName() { return "dchevell/bamboo"; }

        @Override
        public String getContainerGid() { return "2005"; }
    };

    public abstract String getDockerImageName();

    public abstract String getContainerGid();

    public String getHelmReleaseName() {
        return Helm.getHelmReleaseName(this);
    }
}
