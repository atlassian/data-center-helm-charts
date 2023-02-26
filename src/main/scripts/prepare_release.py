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
prodbase = "src/main/charts"

def get_chart_versions(path=prodbase):
    versions = {}
    for prod in products:
        proddir = f"{path}/{prod}"
        chartfile = f"{proddir}/Chart.yaml"

        with open(chartfile, 'r') as chart:
            yaml = YAML()
            chartyaml = yaml.load(chart)

        versions[prod] = {}
        versions[prod]['kubeVersion'] = chartyaml['kubeVersion']
        versions[prod]['appVersion'] = chartyaml['appVersion']
        versions[prod]['version'] = chartyaml['version']

        log.info(f"Product {prod} versions: {versions[prod]['kubeVersion']}, {versions[prod]['appVersion']}")

    return versions


def gen_changelog(product, path):
    repo = git.Repo(path)
    cli = git.Git(path)
    tags = sorted(repo.tags, key=lambda t: t.commit.committed_datetime)

    lasttag = tags[-1]
    tagver = re.sub(r'^[^-]+-', '', lasttag.name)
    log.info(f'Generating {product} changelog since {tagver}')

    # git log will pick up commits in src/main/charts/$product directory only
    changelog = cli.log(f'{lasttag}..main', "--", prodbase + '/' + product, graph=True, pretty='format:%s',
                        abbrev_commit=True, date='relative', )

    changelog = changelog.split('\n')
    # we don't need pre-release commits that update chart.yamls and changelogs
    # this pattern is based on the commit message from a GitHub action that prepares Helm release
    pattern = re.compile(r'^\* Prepare release [0-9].{1,4}')
    filtered_changelog = list(filter(lambda x: not pattern.match(x), changelog))

    if len(filtered_changelog) == 0 or filtered_changelog.count(''):
        # It is possible that there are no commits to the Helm chart,
        # in this case we just write a generic git log
        log.info(f'No commits to {product} Helm chart found')
        default_git_log = '* Update Helm chart version'
        filtered_changelog = [default_git_log]

    # remove Jira keys from commit messages. This substitution assumes the commit message may have the following format:
    # CLIP-1234: Here is my message
    # DCCLIP-1234: Here is my message
    sanitized_changelog = map(lambda c: re.sub(r'(CLIP|DCCLIP)-[0-9]{1,5}(?![0-9]): ', '', c), filtered_changelog)
    return list(dict.fromkeys(sanitized_changelog))


def format_changelog_yaml(changelog):
    # The artifacthub annotations are a single string, but formatted
    # like YAML. Replacing the leading '*' with '-'  and wrapping
    # strings in double quotes should be sufficient.
    sanitized_changelog = []
    for string in changelog:
        string = string + '"'
        sanitized_changelog.append(string)
    c2 = map(lambda c: re.sub(r'^\* ', '- "', c), sanitized_changelog)
    return '\n'.join(c2)


def update_changelog_file(product, version, changelog, chartversions):
    log.info(f"Updating {product} Changelog.md to {version}")
    proddir = f"{prodbase}/{product}"
    clfile = f"{proddir}/Changelog.md"
    (tmpfd, tmpname) = mkstemp(dir=proddir, text=True)

    foundfirst = False
    with open(clfile, 'r') as clfd:
        for line in clfd:
            if not foundfirst and re.match(r'^## [0-9]\.[0-9]{1,2}\.[0-9]{1,2}', line) != None:
                # First version line, inject ours before it
                foundfirst = True

                kver = chartversions[product]['kubeVersion']
                appver = chartversions[product]['appVersion']
                now = datetime.now()
                os.write(tmpfd, ("## %s\n\n" % version).encode())
                os.write(tmpfd, ("**Release date:** %s-%s-%s\n\n" % (now.year, now.month, now.day)).encode())
                os.write(tmpfd, (
                        '![AppVersion: %s](https://img.shields.io/static/v1?label=AppVersion&message=%s&color=success&logo=)\n' % (
                    appver, appver)).encode())
                os.write(tmpfd, (
                        '![Kubernetes: %s](https://img.shields.io/static/v1?label=Kubernetes&message=%s&color=informational&logo=kubernetes)\n' % (
                    kver, kver)).encode())
                os.write(tmpfd,
                         '![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)\n\n'.encode())
                for change in changelog:
                    os.write(tmpfd, ('%s\n' % change).encode())
                os.write(tmpfd, '\n'.encode())

            os.write(tmpfd, line.encode())

    os.close(tmpfd)
    os.rename(tmpname, clfile)


def update_helm_dependencies(product):
    log.info(f"Updating Helm dependencies for {product}")
    path = f"{prodbase}/{product}"

    update_o = subprocess.run(['helm', 'dependency', 'update', path], capture_output=True)
    if update_o.returncode != 0:
        log.error("Failed to update Helm dependencies: \n%s", re.sub(r'\\n', '\n', str(update_o.stderr)))
        sys.exit(-1)


def update_charts_yaml(product, version, changelog):
    log.info(f"Updating {product} Chart.yaml to {version}")
    proddir = f"{prodbase}/{product}"
    chartfile = f"{proddir}/Chart.yaml"

    with open(chartfile, 'r') as chart:
        yaml = YAML()
        yaml.preserve_quotes = True
        chartyaml = yaml.load(chart)

    chartyaml['version'] = version
    chartyaml['annotations']['artifacthub.io/changes'] = format_changelog_yaml(changelog)

    with open(chartfile, 'w') as chart:
        yaml.dump(chartyaml, chart)

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
        changelog = gen_changelog(product, ".")
        log.info(product + ' changelog:\n%s' % '\n'.join(changelog))
        update_changelog_file(product, args.version, changelog, chartversions)
        update_charts_yaml(product, args.version, changelog)

    update_output_tests()


if __name__ == '__main__':
    main()
