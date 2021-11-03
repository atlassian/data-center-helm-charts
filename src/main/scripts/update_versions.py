import logging

import requests
import yaml

logging.basicConfig(level=logging.INFO, format="%(levelname).1s %(message)s")

products = ["bitbucket", "jira", "bamboo", "confluence", "crowd"]
suffix = "-jdk11"
current_lts = {"bitbucket": "7.17", "jira": "8.20", "confluence": "7.13"}


def update_versions(product_to_update, new_version):
    products_to_update = [product_to_update]
    if product == 'bamboo':
        products_to_update.append("bamboo-agent")

    chart_files = [f'../charts/{p}/Chart.yaml' for p in products_to_update]

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
    output_files = [f'../../test/resources/expected_helm_output/{p}/output.yaml' for p in products_to_update]
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


logging.info("Updating product versions in helm charts")
logging.warning("Always check the latest available LTS versions on https://confluence.atlassian.com/enterprise/long-term-support-releases-948227420.html")
for product in products:
    logging.info("-------------------------")
    logging.info("Product: %s", product)

    if product in current_lts.keys():
        logging.info('LTS product (%s)', current_lts[product])
        version = latest_minor(current_lts[product], product_versions_marketplace(product))
    else:
        logging.info("Non-LTS product")
        r = requests.get(f'https://marketplace.atlassian.com/rest/2/products/key/{product}/versions/latest')
        version = r.json()['name']

    new_version_tag = f"{version}{suffix}"
    logging.info(f"Latest version: %s, tagname: {version}{suffix}", version)
    update_versions(product, new_version_tag)
