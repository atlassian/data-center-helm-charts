import json
import re
import sys
import urllib.request
import argparse

parser = argparse.ArgumentParser(description="Read remote JSON file")
parser.add_argument("--source", help="URL of the target remote JSON file")
parser.add_argument("--dest", help="Local path of the destination processed json")
args = parser.parse_args()

if args.source is None:
    print("Error: --source argument is required. This must be URL of a raw JSON file.")
    sys.exit(1)

if args.dest is None:
    print("Error: --dest argument is required. This must an absolute path to the destination json file.")
    sys.exit(1)

print('Fetching a remote file ' + args.source)
file = urllib.request.urlopen(args.source)
data = json.load(file)
file.close()

# mind double escaping // in regex
templating_string = '{"list":[{"current":{"selected":true,"text":"default","value":"default"},"hide":0,' \
                    '"includeAll":false,"label":"Data Source","multi":false,"name":"datasource","options":[],' \
                    '"query":"prometheus","queryValue":"","refresh":1,"regex":"","skipUrlSync":false,' \
                    '"type":"datasource"},{"datasource":{"type":"prometheus","uid":"$datasource"},' \
                    '"definition":"Confluence_MailTaskQueue_ErrorQueueSize","hide":0,"includeAll":false,' \
                    '"multi":false,"name":"namespace","options":[],"query":{' \
                    '"query":"Confluence_MailTaskQueue_ErrorQueueSize","refId":"StandardVariableQuery"},"refresh":1,' \
                    '"regex":"/namespace=\\"([^\\"]*)\\"/","skipUrlSync":false,"sort":0,"type":"query"},' \
                    '{"datasource":{"type":"prometheus","uid":"$datasource"},' \
                    '"definition":"Confluence_MailTaskQueue_ErrorQueueSize","hide":0,"includeAll":false,' \
                    '"multi":false,"name":"service","options":[],"query":{' \
                    '"query":"Confluence_MailTaskQueue_ErrorQueueSize","refId":"StandardVariableQuery"},"refresh":1,' \
                    '"regex":"/service=\\"([^\\"]*)\\"/","skipUrlSync":false,"sort":0,"type":"query"}]} '

# add datasource, namespace and service variables
templating = json.loads(templating_string)
data['templating'] = templating


# Finding and replacing values
def process_panels(panels):
    for panel in panels:
        print('Processing panel ' + panel['title'])
        for target in panel.get('targets', []):
            print('Processing expression ' + target['expr'])
            # make sure datasource is templated and there is no hardcoded ID
            datasource = json.loads('{"type":"prometheus","uid":"$datasource"}')
            target['datasource'] = datasource
            # we don't need product labels in K8s
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
            # we need namespace and service filters in all expressions to make dashboards flexible
            # we assume that the expression has the following format: expression{},
            # i.e. it has an opening curly bracket. Make sure all expressions in the source json have those
            expr = expr.replace('{', '{namespace="$namespace", service="$service", ')
            target['expr'] = expr

        nested_panels = panel.get('panels', [])
        if nested_panels:
            process_panels(nested_panels)


process_panels(data['panels'])

print('Saving the file to ' + args.dest)
with open(args.dest, 'w') as file:
    json.dump(data, file, indent=2)
