import sys
import urllib.request
import json

known_lts_version = {
	'jira-software': '8.13.8',
	'confluence': '7.12.2',
	'stash': '7.12.1',
}


def get_lts_version(argv):
	product = argv[0].lower()
	if product == 'jira':
		product = 'jira-software'
	elif product == 'bitbucket':
		product = 'stash'

	if product in known_lts_version:
		url = f"https://my.atlassian.com/download/feeds/archived/{product}.json"

		try:
			fl = urllib.request.urlopen(url)
			fdata = fl.read()
			jsdata = json.loads(fdata[10:len(fdata)-1].decode("utf-8"))
			enterprise_edition = [x for x in jsdata if x['edition'].lower() == 'enterprise']
			sortedData = sorted(enterprise_edition, key=lambda k:cversion(k['version']), reverse=True)

			if len(sortedData) > 0:
				lts_version = sortedData[0]['version']
			else:
				lts_version = known_lts_version[product]

			# as currently latest lts versoin of bitbucket and confluence don't support k8s
			# we use none-lts version of those products in the test
			if cversion(lts_version) < cversion(known_lts_version[product]):
				lts_version = known_lts_version[product]
		except:
			lts_version = known_lts_version[product]

		lts_version = f"{lts_version}-jdk11"
	else:
		lts_version = 'unknown'

	return lts_version


def cversion(version):
	vers = version.split(".")
	mapped_ver = ''
	for i in range(max(len(vers)-1, 4)):
		if len(vers) > i:
			mapped_ver = f'{mapped_ver}{vers[i].zfill(5)}'
		else:
			mapped_ver = f'{mapped_ver}00000'
	return mapped_ver


if __name__ == "__main__":
	if len(sys.argv) > 1:
		get_lts_version(sys.argv[1:])