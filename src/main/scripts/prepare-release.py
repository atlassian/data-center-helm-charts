
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


# TODO: As a first pass this currently only filters anything tagged
# with 'CLIP-nnn'. However there are other options; see
# https://hello.atlassian.net/wiki/spaces/DCD/pages/2108707663/DACI+Automating+the+Helm+release+process
def changelog_filter(log_entry):
    return re.match(r'^\*\s+(DC)?CLIP-[0-9]+', log_entry) != None


def get_chart_versions():
    versions = {}
    for prod in products:
        proddir = f"{prodbase}/{prod}"
        chartfile = f"{proddir}/Chart.yaml"

        with open(chartfile, 'r') as chart:
            yaml = YAML()
            chartyaml = yaml.load(chart)

        versions[prod] = {}
        versions[prod]['kubeVersion'] = chartyaml['kubeVersion']
        versions[prod]['appVersion'] = chartyaml['appVersion']

        log.info(f"Product {prod} versions: {versions[prod]['kubeVersion']}, {versions[prod]['appVersion']}")

    return versions

def gen_changelog(repo_path):
    repo = git.Repo(repo_path)
    cli = git.Git(repo_path)

    lasttag = repo.tags[-1]
    tagver = re.sub(r'^[^-]+-', '', lasttag.name)
    log.info(f'Generating changelog since {tagver}')

    changelog = cli.log(f'{lasttag}..main', graph=True, pretty='format:%s', abbrev_commit=True, date='relative')
    changelog = changelog.split('\n')
    filtered_changelog = list(filter(changelog_filter, changelog))
    sanitized_changelog = map(lambda c: re.sub(r'(DC)?CLIP-[0-9]{2,4}(?![0-9]): ', '', c), filtered_changelog)
    return list(sanitized_changelog)


def format_changelog_yaml(changelog):
    # The artifacthub annotations are a single string, but formatted
    # like YAML. Replacing the leading '*' with '-' should be
    # sufficient.
    c2 = map(lambda c: re.sub(r'^\* ', '- "', c), changelog)
    return '\n'.join(c2) + "\""


def update_changelog_file(version, changelog, chartversions):
    for prod in products:
        log.info(f"Updating {prod} Changelog.md to {version}")

        proddir = f"{prodbase}/{prod}"
        clfile = f"{proddir}/Changelog.md"
        (tmpfd, tmpname) = mkstemp(dir=proddir, text=True)

        foundfirst = False
        with open(clfile, 'r') as clfd:
            for line in clfd:
                if not foundfirst and re.match(r'^## [0-9]\.[0-9]\.[0-9]', line) != None:
                    # First version line, inject ours before it
                    foundfirst = True

                    kver = chartversions[prod]['kubeVersion']
                    appver = chartversions[prod]['appVersion']
                    now = datetime.now()
                    os.write(tmpfd, ("## %s\n\n" % version).encode())
                    os.write(tmpfd, ("**Release date:** %s-%s-%s\n\n" % (now.year, now.month, now.day)).encode())
                    os.write(tmpfd, ('![AppVersion: %s](https://img.shields.io/static/v1?label=AppVersion&message=%s&color=success&logo=)\n' % (appver, appver)).encode())
                    os.write(tmpfd, ('![Kubernetes: %s](https://img.shields.io/static/v1?label=Kubernetes&message=%s&color=informational&logo=kubernetes)\n' % (kver, kver)).encode())
                    os.write(tmpfd, '![Helm: v3](https://img.shields.io/static/v1?label=Helm&message=v3&color=informational&logo=helm)\n\n'.encode())
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


def update_charts_yaml(version, changelog):
    for prod in products:
        log.info(f"Updating {prod} Chart.yaml to {version}")

        proddir = f"{prodbase}/{prod}"
        chartfile = f"{proddir}/Chart.yaml"

        with open(chartfile, 'r') as chart:
            yaml = YAML()
            yaml.preserve_quotes = True
            chartyaml = yaml.load(chart)

        chartyaml['version'] = version
        chartyaml['annotations']['artifacthub.io/changes'] = format_changelog_yaml(changelog)

        with open(chartfile, 'w') as chart:
            yaml.dump(chartyaml, chart)

        update_helm_dependencies(prod)


def update_output_tests():
    log.info("Running Maven to update the unit test output")

    update_o = subprocess.run(['mvn', 'test', '-B', '-Dtest=test.HelmOutputComparisonTest#record_helm_template_output_matches_expectations', '-DrecordOutput=true'],
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
    log.basicConfig(level=log.INFO)

    args = parse_args()
    log.info(f"Updating Helm charts to release {args.version}")

    chartversions = get_chart_versions()
    changelog = gen_changelog(".")
    log.info('Changelog:\n%s' % '\n'.join(changelog))

    update_changelog_file(args.version, changelog, chartversions)

    update_charts_yaml(args.version, changelog)

    update_output_tests()


if __name__ == '__main__':
    main()
