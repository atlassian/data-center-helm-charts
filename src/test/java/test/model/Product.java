package test.model;

import test.helm.Helm;

/*
 * When adding additional products charts, products names comprising more than 
 * one word should be separated by an underscore(s) "_". Hyphens "-" are not valid
 * when declaring enum names. See "bamboo_agent" below as an example of this
 */
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
        // TODO: this image will need to be updated once Stevs changes are in
        public String getDockerImageName() { return "atlassian/bamboo"; }

        @Override
        public String getContainerGid() { return "2005"; }
    },
    bamboo_agent {
        @Override
        // TODO: this image will need to be updated once Stevs changes are in
        public String getDockerImageName() { return "atlassian/bamboo-agent-base"; }

        @Override
        public String getContainerGid() { return "2005"; }
    };

    public abstract String getDockerImageName();

    public abstract String getContainerGid();

    public String getHelmReleaseName() {
        return Helm.getHelmReleaseName(this);
    }
    
    /*
     * So that we can create Chart directories of the form: 
     * 
     * "src/main/charts/bamboo-agent"
     * 
     * but also ensure our tests still work, we override this method
     * to replace underscores with hyphens.
     * 
     */
    @Override
    public String toString() {
        return super.toString().replaceAll("_", "-");
    }
}
