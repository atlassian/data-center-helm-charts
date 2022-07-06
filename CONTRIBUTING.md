# Contributing to Data Center Helm Charts

Thank you for considering a contribution to Data Center Helm Charts. Pull requests, issues and comments are welcome. For pull requests, please:

* Add tests for new features and bug fixes
* Follow the existing style
* Separate unrelated changes into multiple pull requests

Follow the [development setup](DEVELOPMENT.md).

See the existing issues for things to start contributing.

For bigger changes, please make sure you start a discussion first by creating an issue and explaining the intended change.

Atlassian requires contributors to sign a Contributor License Agreement, known as a CLA. This serves as a record stating that the contributor is entitled to contribute the code/documentation/translation to the project and is willing to have it used in distributions and derivative works (or is willing to transfer ownership).

Prior to accepting your contributions we ask that you please follow the appropriate link below to digitally sign the CLA. The Corporate CLA is for those who are contributing as a member of an organization and the individual CLA is for those contributing as an individual.

* [CLA for corporate contributors](https://opensource.atlassian.com/corporate)
* [CLA for individuals](https://opensource.atlassian.com/individual)

## Code Owners' Responsibilities

For any pull requests, code owners should review the changes thoroughly and make sure all the requirements are met. Code owners should also run the internal Bamboo CI tests and E2E tests against the changes when are applicable.

For external contributions, any Github action workflow related changes are not acceptable.

### How to run E2E tests

The Data Center Helm Charts uses the latest release of [Deployment Automation for Atlassian DC on K8s](https://github.com/atlassian-labs/data-center-terraform#deployment-automation-for-atlassian-dc-on-k8s) for end-to-end testing. Internal reviewers can run the tests by adding `e2e` label on a pull request (for external contributions, be mindful of the changes as it will run on internal cloud environment).