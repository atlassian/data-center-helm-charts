import json
import subprocess
import tempfile
import os

script_dir = os.path.dirname(os.path.abspath(__file__))
script_path = os.path.join(script_dir, 'process_dashboard.py')
temp_dir = tempfile.TemporaryDirectory()
file_path = temp_dir.name + '/general.json'
args = ['--source', 'https://raw.githubusercontent.com/atlassian-labs/data-center-grafana-dashboards/main/jira'
                    '/general.json', '--dest', file_path, '--product', 'jira']

subprocess.call(['python', script_path] + args)

with open(file_path) as json_file:
    # Load the JSON data
    data = json.load(json_file)


# we expect 3 variables - prometheus, namespace and service

jira_query = 'com_atlassian_jira_issue_assigned_count_Value'
assert len(data['templating']['list']) == 3, f"Assertion failed. Expected 3, got {len(data['templating']['list'])}"
assert data['templating']['list'][0]['query'] == "prometheus", f"Assertion failed. Expected query prometheus, got {data['templating']['list'][0]}"
assert data['templating']['list'][1]['query']['query'] == jira_query, f"Assertion failed. Expected query {jira_query}, got {data['templating']['list'][0]}"

assert data['templating']['list'][1]['name'] == "namespace", f"Assertion failed. No 'name': 'namespace' in {data['templating']['list'][1]}"
assert data['templating']['list'][2]['name'] == "service", f"Assertion failed. No 'name': 'service' in {data['templating']['list'][2]}"

expected_datasource = json.loads('{"type":"prometheus","uid":"$datasource"}')

for panel in data['panels']:
    if panel['type'] != 'row':
        for target in panel.get('targets', []):
            assert target[
                       'datasource'] == expected_datasource, f"Assertion failed. Expected {expected_datasource}, got {target['datasource']}"
            assert "product=" not in target[
                'expr'], f"Assertion failed, product template variable in not expected. Got {target['expr']}"
            assert "namespace=\"$namespace\"" in target['expr'], f"Assertion failed, namespace template variable not " \
                                                                 f"found in {target['expr']}"
            assert "service=\"$service\"" in target['expr'], f"Assertion failed, service template variable not " \
                                                                 f"found in {target['expr']}"