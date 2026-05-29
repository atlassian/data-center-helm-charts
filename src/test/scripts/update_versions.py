import logging

import requests
import yaml

import product_versions

# USER_AGENT constant for marketplace API calls
USER_AGENT = "Mozilla_dc_core_eng"

# Marketplace API base host.
MARKETPLACE_BASE_URL = "https://marketplace.atlassian.com"

"""
This script is used to update the product versions in the helm charts descriptors (Chart.yaml).
It fetches the latest available version (LTS for products with LTS) from marketplace and updates the required
tag in the product chart. It also updates expected output for the product.

Script is currently executed manually and is in a fairly rough shape.
"""

logging.basicConfig(level=logging.INFO, format="%(levelname).1s %(message)s")

products = ["bitbucket", "jira", "bamboo", "confluence", "crowd"]
tag_suffix = ""
lts_products = ["bitbucket", "jira", "confluence"]


def update_versions(product_to_update, new_version):
    products_to_update = [product_to_update]
    if product_to_update == 'bamboo':
        products_to_update.append("bamboo-agent")

    chart_files = [f'../../main/charts/{p}/Chart.yaml' for p in products_to_update]

    for chart_file in chart_files:
        with open(chart_file, "r") as stream:
            content = stream.read()
            doc = yaml.safe_load(content)
            current_version = doc['appVersion']
            logging.info("Current version: %s", current_version)

        new_content = content.replace(current_version, new_version)

        with open(chart_file, "w") as stream:
            stream.write(new_content)
            logging.info("Updated product chart: %s", chart_file)

    update_expected_output(products_to_update, new_version)


def update_expected_output(products_to_update, new_version):
    output_files = [f'../resources/expected_helm_output/{p}/output.yaml' for p in products_to_update]
    for output_file in output_files:
        with open(output_file, "r") as stream:
            content = stream.read()

        old_version = list(yaml.safe_load_all(content))[0]['metadata']['labels']['app.kubernetes.io/version']
        new_content = content.replace(old_version, f"{new_version}")

        with open(output_file, "w") as stream:
            stream.write(new_content)
        logging.info('Updated expected output file: %s', output_file)


def latest_marketplace_version(product_key):
    """Return the latest version string for a host product via the v3
    `parent-software` API. Uses `?limit=1`; v3 returns the list in
    descending buildNumber order, so the first item is the latest."""
    url = f"{MARKETPLACE_BASE_URL}/rest/3/parent-software/{product_key}/versions"
    r = requests.get(url, params={'limit': 1}, headers={'User-Agent': USER_AGENT})
    return r.json()['versions'][0]['versionNumber']


def update_mesh_tag():
    logging.info("-------------------------------")
    logging.info('- Updating Bitbucket Mesh tag -')
    logging.info("-------------------------------")
    new_version = product_versions.get_lts_version(['mesh']).replace(tag_suffix, "")
    bitbucket_values_file = '../../main/charts/bitbucket/values.yaml'
    expected_bitbucket_output_file = '../resources/expected_helm_output/bitbucket/output.yaml'

    with open(bitbucket_values_file, "r") as stream:
        content = stream.read()
        doc = yaml.safe_load(content)
        current_version = doc['bitbucket']['mesh']['image']['tag']
        logging.info("Current version: %s", current_version)
        logging.info("New version: %s", new_version)

    new_content = content.replace(current_version, new_version)
    with open(bitbucket_values_file, "w") as stream:
        stream.write(new_content)

    logging.info('Updated values file: %s', bitbucket_values_file)
    with open(expected_bitbucket_output_file, "r") as file:
        file_contents = file.read()

    modified_contents = file_contents.replace(current_version, new_version)
    logging.info('Updated expected output file: %s', expected_bitbucket_output_file)
    with open(expected_bitbucket_output_file, 'w') as file:
        file.write(modified_contents)


logging.info("Updating product versions in helm charts")
for product in products:
    logging.info("-------------------------")
    logging.info("Product: %s", product)

    if product in lts_products:
        version = product_versions.get_lts_version([product]).replace(tag_suffix, "")
        logging.info("Latest LTS version: %s", version)
    else:
        logging.info("Non-LTS product")
        version = latest_marketplace_version(product)

    new_version_tag = f"{version}{tag_suffix}"
    logging.info(f"Latest version: %s, tagname: {version}{tag_suffix}", version)
    update_versions(product, new_version_tag)

update_mesh_tag()
logging.info(">>>> ATTENTION - Don't forget to update the product Changelogs.md - ATTENTION <<<<")
