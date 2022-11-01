
import git
import logging as log
import os
import re
from argparse import ArgumentParser
from ruamel.yaml import YAML
from tempfile import mkstemp

products = ["bamboo", "bamboo-agent",
            "bitbucket", "confluence", "crowd", "jira"]
prodbase = "src/main/charts"


# TODO: As a first pass this currently only filters anything tagged
# with 'CLIP-nnn'. However there are other options; see
# https://hello.atlassian.net/wiki/spaces/DCD/pages/2108707663/DACI+Automating+the+Helm+release+process
def changelog_filter(log_entry):
    return re.match(r'^\*\s+CLIP-[0-9]+', log_entry) != None


def gen_changelog(repo_path):
    repo = git.Repo(repo_path)
    cli = git.Git(repo_path)

    lasttag = repo.tags[-1]
    tagver = re.sub(r'^[^-]+-', '', lasttag.name)
    log.info(f'Generating changelog since {tagver}')

    changelog = cli.log(f'{lasttag}..main', graph=True, pretty='format:%s', abbrev_commit=True, date='relative')
    changelog = changelog.split('\n')

    return list(filter(changelog_filter, changelog))


def format_changelog(changelog):
    # The artifacthub annotations are a single string, but formatted
    # like YAML. Replacing the leading '*' with '-' should be
    # sufficient.
    c2 = map(lambda c: re.sub(r'^\* ', '- ', c), changelog)
    return '\n'.join(c2)


def update_charts_yaml(version, changelog):
    for prod in products:
        log.info(f"Updating {prod} to {version}")

        proddir = f"{prodbase}/{prod}"
        chartfile = f"{proddir}/Chart.yaml"

        with open(chartfile, 'r') as chart:
            yaml = YAML()
            yaml.preserve_quotes = True
            chartyaml = yaml.load(chart)

        chartyaml['version'] = version
        chartyaml['annotations']['artifacthub.io/changes'] = format_changelog(changelog)

        with open(chartfile, 'w') as chart:
            yaml.dump(chartyaml, chart)


def parse_args():
    parser = ArgumentParser()
    parser.add_argument("version", help="The version to release")
    parser.add_argument("ghkey", help="Your Github API key")
    args = parser.parse_args()

    return args


def main():
    log.basicConfig(level=log.INFO)

    args = parse_args()
    log.info(f"Updating Helm charts to release {args.version}")

    changelog = gen_changelog(".")
    log.info('Changelog:\n%s' % '\n'.join(changelog))
    update_charts_yaml(args.version, changelog)


if __name__ == '__main__':
    main()
