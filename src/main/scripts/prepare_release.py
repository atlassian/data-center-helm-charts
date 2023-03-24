# This script prepares Helm charts for a new release (release version is an argument to the script):
#
# * for each Helm chart collect commits, sanitize commit messages to remove Jira keys,
#   drop commits we don't need in release notes,
#   remove commits with identical commit messages
#
# * update Changelog.md for each Helm chart with new version's release notes
#
# * update Chart.yaml for each Helm chart with a new chart(release) version
#
# * convert changelog into a list of strings in the acceptable artifacthub.io annotations format
#
# * update Helm output for unit tests

import git
import logging as log
import os
import re
import subprocess
import sys
from argparse import ArgumentParser
from datetime import datetime
from ruamel.yaml import YAML
from tempfile import mkstemp

products = ["bamboo", "bamboo-agent",
            "bitbucket", "confluence", "crowd", "jira"]
prod_base = "src/main/charts"

jira_keys_pattern = r'(CLIP|DCCLIP)-[0-9]{1,5}(?![0-9]): '
drop_commits_pattern = r'^\* Prepare release [0-9].{1,4}'
update_app_versions_commit_msg = r'(\* Update appVersions for DC apps|.*#(\d+))'

# parse Chart.yaml to get K8s version, app version and Helm chart version
# to use those when generating changelog/release notes
def get_chart_versions(path=prod_base):
    versions = {}
    for prod in products:
        prod_dir = f"{path}/{prod}"
        chart_file = f"{prod_dir}/Chart.yaml"

        with open(chart_file, 'r') as chart:
            yaml = YAML()
            chart_yaml = yaml.load(chart)

        versions[prod] = {}
        versions[prod]['kubeVersion'] = chart_yaml['kubeVersion']
        versions[prod]['appVersion'] = chart_yaml['appVersion']
        versions[prod]['version'] = chart_yaml['version']

        log.info(f"Product {prod} versions: {versions[prod]['kubeVersion']}, {versions[prod]['appVersion']}")

    return versions


def gen_changelog(product, path, changelog=None, test=False):
    repo = git.Repo(path)
    cli = git.Git(path)
    tags = sorted(repo.tags, key=lambda t: t.commit.committed_datetime)

    last_tag = tags[-1]
    tag_ver = re.sub(r'^[^-]+-', '', last_tag.name)
    log.info(f'Generating {product} changelog since {tag_ver}')

    # git log will pick up commits in src/main/charts/$product directory only
    if not changelog and not test:
        changelog = cli.log(f'{last_tag}..main', "--", prod_base + '/' + product, graph=True, pretty='format:%s',
                            abbrev_commit=True, date='relative', )
    changelog = changelog.split('\n')
    pattern = re.compile(drop_commits_pattern)
    # we don't need pre-release commits that update chart.yamls and changelogs
    # this pattern is based on the commit message from a GitHub action that prepares Helm release
    filtered_changelog = list(filter(lambda x: not pattern.match(x), changelog))

    # if there are multiple commits with identical message but different PR number, e.g.
    # Update appVersions for DC apps (#123), Update appVersions for DC apps (#124)
    # we need to leave just one - the latest.
    matching_messages = []
    for message in filtered_changelog:
        match = re.search(update_app_versions_commit_msg, message)
        if match:
            matching_messages.append(message)

    if matching_messages:
        highest_message = sorted(matching_messages, key=lambda x: int(re.search(r'#(\d+)', x).group(1)), reverse=True)[
            0]
        filtered_changelog = [m for m in filtered_changelog if
                              m == highest_message or 'Update appVersions for DC apps' not in m]

    if len(filtered_changelog) == 0 or filtered_changelog.count(''):
        # It is possible that there are no commits to the Helm chart, but we still need to release
        # in this case we just write a generic git log
        log.info(f'No commits to {product} Helm chart found')
        default_git_log = '* Update Helm chart version'
        filtered_changelog = [default_git_log]

    # remove Jira keys from commit messages. This substitution assumes the commit message has the following format:
    # CLIP-1234: Here is my message
    # DCCLIP-1234: Here is my message
    sanitized_changelog = map(lambda c: re.sub(jira_keys_pattern, '', c), filtered_changelog)
    return list(dict.fromkeys(sanitized_changelog))


def format_changelog_yaml(changelog):
    # The ArtifactHub annotations are a single string, but formatted
    # like YAML. Replacing the leading '*' with '-'  and wrapping
    # strings in double quotes should be sufficient.
    sanitized_changelog = []
    for string in changelog:
        # append `"` to the end of the string
        string = string + '"'
        sanitized_changelog.append(string)
    # replace '* ' with '- "' to have changelog in the following format:
    # - "String1"
    # - "String2"
    c2 = map(lambda c: re.sub(r'^\* ', '- "', c), sanitized_changelog)
    return '\n'.join(c2)


# add new version changelog at the top of the Changelog.md file for each Helm chart
def update_changelog_file(product, version, changelog, chartversions):
    log.info(f"Updating {product} Changelog.md to {version}")
    prod_dir = f"{prod_base}/{product}"
    changelog_file = f"{prod_dir}/Changelog.md"
    (tmp_fd, tmp_name) = mkstemp(dir=prod_dir, text=True)

    found_first = False
    with open(changelog_file, 'r') as clfd:
        for line in clfd:
            # process the first line and look for a line that matches ## <version>
            if not found_first and re.match(r'^## [0-9]{1,2}\.[0-9]{1,2}\.[0-9]{1,2}', line) is not None:
                # First version line, inject ours before it
                found_first = True
                k8s_version = chartversions[product]['kubeVersion']
                app_version = chartversions[product]['appVersion']
                now = datetime.now()
                os.write(tmp_fd, ("## %s\n\n" % version).encode())
                os.write(tmp_fd, ("**Release date:** %s-%s-%s\n\n" % (now.year, now.month, now.day)).encode())
                os.write(tmp_fd, (
                        '![AppVersion: %s](https://img.shields.io/static/v1?label=AppVersion&message=%s&color=success'
                        '&logo=)\n' % (
                            app_version, app_version)).encode())
                os.write(tmp_fd, (
                        '![Kubernetes: %s](https://img.shields.io/static/v1?label=Kubernetes&message=%s&color'
                        '=informational&logo=kubernetes)\n' % (
                            k8s_version, k8s_version)).encode())
                os.write(tmp_fd,
                         '![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo'
                         '=helm)\n\n'.encode())
                for change in changelog:
                    os.write(tmp_fd, ('%s\n' % change).encode())
                os.write(tmp_fd, '\n'.encode())

            os.write(tmp_fd, line.encode())

    os.close(tmp_fd)
    os.rename(tmp_name, changelog_file)


# updating dependencies is required to run mvn command that updates test output
def update_helm_dependencies(product):
    log.info(f"Updating Helm dependencies for {product}")
    path = f"{prod_base}/{product}"

    update_o = subprocess.run(['helm', 'dependency', 'update', path], capture_output=True)
    if update_o.returncode != 0:
        log.error("Failed to update Helm dependencies: \n%s", re.sub(r'\\n', '\n', str(update_o.stderr)))
        sys.exit(-1)


def update_charts_yaml(product, version, changelog):
    log.info(f"Updating {product} Chart.yaml to {version}")
    prod_dir = f"{prod_base}/{product}"
    chart_file = f"{prod_dir}/Chart.yaml"

    with open(chart_file, 'r') as chart:
        yaml = YAML()
        yaml.preserve_quotes = True
        chart_yaml = yaml.load(chart)

    chart_yaml['version'] = version
    chart_yaml['annotations']['artifacthub.io/changes'] = format_changelog_yaml(changelog)

    with open(chart_file, 'w') as chart:
        yaml.dump(chart_yaml, chart)

    update_helm_dependencies(product)


def update_output_tests():
    log.info("Running Maven to update the unit test output")

    update_o = subprocess.run(
        ['mvn', 'test', '-B', '-Dtest=test.HelmOutputComparisonTest#record_helm_template_output_matches_expectations',
         '-DrecordOutput=true'],
        capture_output=True)
    if update_o.returncode != 0:
        log.error("Failed to update the unittest comparison data: \n%s", re.sub(r'\\n', '\n', str(update_o.stdout)))
        sys.exit(-1)


def parse_args():
    parser = ArgumentParser()
    parser.add_argument("version", help="The version to release")
    args = parser.parse_args()

    return args


def main():
    log.basicConfig(level=log.DEBUG)

    args = parse_args()
    log.info(f"Updating Helm charts to release {args.version}")

    chartversions = get_chart_versions()
    for product in products:
        changelog = gen_changelog(product, ".", None, False)
        log.info(product + ' changelog:\n%s' % '\n'.join(changelog))
        update_changelog_file(product, args.version, changelog, chartversions)
        update_charts_yaml(product, args.version, changelog)

    update_output_tests()


if __name__ == '__main__':
    main()
