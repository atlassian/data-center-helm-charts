import json
import logging
import urllib.request

import requests
import yaml

import product_versions

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
    if product == 'bamboo':
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


def product_versions_marketplace(product_key):
    mac_url = 'https://marketplace.atlassian.com'
    request_url = f'/rest/2/products/key/{product_key}/versions'
    params = {'offset': 0, 'limit': 50}
    versions = set()
    page = 1
    while True:
        logging.debug(f'Retrieving Marketplace product versions for {product_key}: page {page}')
        r = requests.get(mac_url + request_url, params=params)
        version_data = r.json()
        for version in version_data['_embedded']['versions']:
            if all(d.isdigit() for d in version['name'].split('.')):
                logging.debug(f"Adding version {version['name']}")
                versions.add(version['name'])
        if 'next' not in version_data['_links']:
            break
        request_url = version_data['_links']['next']['href']
        page += 1
        params = {}
    logging.debug(f'Found {len(versions)} versions')
    return sorted(list(versions), reverse=True)


def latest_minor(version, mac_versions):
    major_minor_version = '.'.join(version.split('.')[:2])
    minor_versions = [v for v in mac_versions
                      if v.startswith(f'{major_minor_version}.')]
    minor_versions.sort(key=lambda s: [int(u) for u in s.split('.')])
    return minor_versions[-1]


def update_mesh_tag():
    logging.info("-------------------------------")
    logging.info('- Updating Bitbucket Mesh tag -')
    logging.info("-------------------------------")
    mesh_repo = 'atlassian/bitbucket-mesh'
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

    modified_contents = file_contents.replace(mesh_repo + ':' + current_version, mesh_repo + ':' + new_version)
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
        r = requests.get(f'https://marketplace.atlassian.com/rest/2/products/key/{product}/versions/latest')
        version = r.json()['name']

    new_version_tag = f"{version}{tag_suffix}"
    logging.info(f"Latest version: %s, tagname: {version}{tag_suffix}", version)
    update_versions(product, new_version_tag)

update_mesh_tag()
logging.info(">>>> ATTENTION - Don't forget to update the product Changelogs.md - ATTENTION <<<<")
