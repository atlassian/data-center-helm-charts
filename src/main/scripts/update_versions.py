import logging

import requests
import yaml

logging.basicConfig(level=logging.INFO, format="%(levelname).1s %(message)s")

products = ["bitbucket", "jira", "bamboo", "confluence", "crowd"]
suffix = "-jdk11"
current_lts = {"bitbucket": "7.17", "jira": "8.20", "confluence": "7.13"}


def update_chart(product, new_version):
    chart_file = f'../charts/{product}/Chart.yaml'
    with open(chart_file, "r") as stream:
        content = stream.read()
        doc = yaml.safe_load(content)
        current_version = doc['appVersion']
        logging.info("current version: %s", current_version)

    new_content = content.replace(current_version, f"{new_version}{suffix}")

    with open(chart_file, "w") as stream:
        stream.write(new_content)


def mac_versions(product_key):
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
for p in products:
    logging.info("-------------------------")
    logging.info("Product: %s", p)

    if p in current_lts.keys():
        logging.info('LTS product (%s)', current_lts[p])
        version = latest_minor(current_lts[p], mac_versions(p))
    else:
        logging.info("Non-LTS product")
        r = requests.get(f'https://marketplace.atlassian.com/rest/2/products/key/{p}/versions/latest')
        version = r.json()['name']

    logging.info(f"version: %s, tagname: {version}{suffix}", version)
    update_chart(p, version)
    logging.info("updated the product chart")

