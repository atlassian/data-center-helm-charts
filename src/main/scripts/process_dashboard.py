"""
This script fetches JSON files, processes them and writes the resulting files to a local destination.
Its main function is to convert dashboard jsons https://github.com/atlassian-labs/data-center-grafana-dashboards
to a K8s friendly format. In particular:
* add namespace and service environment variables to templating
* add namespace and service environment variables to every expression
* remove non k8s specific labels if any
* replace instance with pod in panel legends
* remove extra commas
* in case of Bitbucket, set endpoint=jmx to all expressions in the dashboards
* in case of Bitbucket, set endpoint=jmx-mesh-sidecar to all expressions in the dashboards processed with --mesh sidecar
"""

import json
import re
import sys
import urllib.request
import argparse

parser = argparse.ArgumentParser(description="Read remote JSON file")
parser.add_argument("--source", help="URL of the target remote JSON file")
parser.add_argument("--dest", help="Local path of the destination processed json")
parser.add_argument("--product", help="Product")
parser.add_argument("--mesh", help="sidecar")
args = parser.parse_args()

if args.source is None:
    print("Error: --source argument is required. This must be URL of a raw JSON file.")
    sys.exit(1)

if args.dest is None:
    print("Error: --dest argument is required. This must be an absolute path to the destination json file.")
    sys.exit(1)

if args.product is None:
    print("Error: --product argument is required. This must be the product of the dashboard to be converted.")
    sys.exit(1)

print('Fetching a remote file ' + args.source)
file = urllib.request.urlopen(args.source)
data = json.load(file)
file.close()

if args.product == 'jira':
    product_unique_metric = 'com_atlassian_jira_issue_assigned_count_Value'
elif args.product == 'confluence':
    product_unique_metric = 'Confluence_MailTaskQueue_ErrorQueueSize'
elif args.product == 'bitbucket':
    product_unique_metric = 'com_atlassian_bitbucket_Repositories_Count'
elif args.product == 'bitbucket-mesh':
    product_unique_metric = 'metrics_grpc_Value'
elif args.product == 'bamboo':
    product_unique_metric = 'java_lang_Memory_HeapMemoryUsage_committed{product=\\"bamboo\\"}'
elif args.product == 'crowd':
    product_unique_metric = 'java_lang_Runtime_Uptime{product=\\"crowd\\"}'

# mind double escaping // in regex
templating_string = '{"list":[{"current":{"selected":true,"text":"default","value":"default"},"hide":0,' \
                    '"includeAll":false,"label":"Data Source","multi":false,"name":"datasource","options":[],' \
                    '"query":"prometheus","queryValue":"","refresh":1,"regex":"","skipUrlSync":false,' \
                    '"type":"datasource"},{"datasource":{"type":"prometheus","uid":"$datasource"},' \
                    '"definition":"' + product_unique_metric + '","hide":0,"includeAll":false,' \
                    '"multi":false,"name":"namespace","options":[],"query":{' \
                    '"query":"' + product_unique_metric + '","refId":"StandardVariableQuery"},"refresh":1,' \
                    '"regex":"/namespace=\\"([^\\"]*)\\"/","skipUrlSync":false,"sort":0,"type":"query"},' \
                    '{"datasource":{"type":"prometheus","uid":"$datasource"},' \
                    '"definition":"' + product_unique_metric + '","hide":0,"includeAll":false,' \
                    '"multi":false,"name":"service","options":[],"query":{' \
                    '"query":"' + product_unique_metric + '","refId":"StandardVariableQuery"},"refresh":1,' \
                    '"regex":"/service=\\"([^\\"]*)\\"/","skipUrlSync":false,"sort":0,"type":"query"}]} '

# add datasource, namespace and service variables
templating = json.loads(templating_string)
data['templating'] = templating


# Finding and replacing values
def process_panels(panels):
    for panel in panels:
        if panel['type'] != 'row':
            print('Processing panel ' + panel['title'])
            for target in panel.get('targets', []):
                print('Processing expression ' + target['expr'])
                # make sure datasource is templated and there is no hardcoded ID
                datasource = json.loads('{"type":"prometheus","uid":"$datasource"}')
                target['datasource'] = datasource
                # we don't need product labels in K8s except for Bamboo
                remove_product_label = re.sub(r'product="[^"]*"', '', target['expr'])
                target['expr'] = remove_product_label
                # we don't need instance labels in K8s because pod IPs are dynamic
                remove_instance_label = re.sub(r'instance="[^"]*"', '', target['expr'])
                target['expr'] = remove_instance_label
                # In the legend, a pod makes more sense than instance
                # which is IP that will change. Replace {{instance}} with {{ pod }}, regex is used
                # to match {{ instance }}, {{instance }} and {{ instance}}
                replace_instance_with_pod = re.sub(r'{{\s*instance\s*}}', '{{ pod }}', target['legendFormat'])
                target['legendFormat'] = replace_instance_with_pod
                expr = target['expr']
                expr = expr.replace('{{instance}}', '{{ pod }}')
                # bitbucket server has 2 endpoints - the server itself and Mesh sidecar, we need to add
                # endpoint="jmx" to all expressions in dashboards that are being processed without --mesh sidecar arg
                if args.product == "bitbucket":
                    jmx_endpoint = "jmx"
                    if args.mesh == "sidecar":
                        jmx_endpoint = "jmx-mesh-sidecar"
                    expr = expr.replace('{', '{endpoint="' + jmx_endpoint + '", ')
                # we need namespace and service filters in all expressions to make dashboards flexible
                # we assume that the expression has the following format: expression{},
                # i.e. it has an opening curly bracket. Make sure all expressions in the source json have those
                expr = expr.replace('{', '{namespace="$namespace", service="$service", ')
                expr = expr.replace(', }', '}')
                target['expr'] = expr

        nested_panels = panel.get('panels', [])
        if nested_panels:
            process_panels(nested_panels)


process_panels(data['panels'])

print('Saving the file to ' + args.dest)
with open(args.dest, 'w') as file:
    json.dump(data, file, indent=2)
