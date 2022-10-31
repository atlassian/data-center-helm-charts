
from argparse import ArgumentParser
import logging as log
import os
import re
from ruamel.yaml import YAML
from tempfile import mkstemp

products = ["bamboo", "bamboo-agent", "bitbucket", "confluence", "crowd", "jira"]
prodbase = "src/main/charts"

def parse_args():
    parser = ArgumentParser()
    parser.add_argument("version", help="The version to release")
    parser.add_argument("ghkey", help="Your Github API key")
    args = parser.parse_args()

    return args

def update_charts_yaml(version):
    for prod in products:
        log.info(f"Updating {prod} to {version}")

        proddir = f"{prodbase}/{prod}"
        chartfile = f"{proddir}/Chart.yaml"

        with open(chartfile, 'r') as chart:
            yaml = YAML()
            yaml.preserve_quotes = True
            chartyaml = yaml.load(chart)

        chartyaml['version'] = version

        with open(chartfile, 'w') as chart:
            yaml.dump(chartyaml, chart)


def main():
    log.basicConfig(level=log.INFO)

    args = parse_args()
    log.info(f"Updating Helm charts to release {args.version}")

    update_charts_yaml(args.version)

if __name__ == '__main__':
    main()
