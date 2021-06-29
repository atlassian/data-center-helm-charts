import sys
import urllib.request
import json

known_supported_version = {
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

	if product in known_supported_version:
		url = f"https://my.atlassian.com/download/feeds/archived/{product}.json"

		try:
			fl = urllib.request.urlopen(url)
			fdata = fl.read()
			jsdata = json.loads(fdata[10:len(fdata)-1].decode("utf-8"))
			lts_versions = [x for x in jsdata if x['edition'].lower() == 'enterprise']
			sortedVersions = sorted(lts_versions, key=lambda k:cversion(k['version']), reverse=True)

			if len(sortedVersions) > 0:
				lts_version = sortedVersions[0]['version']
			else:
				lts_version = known_supported_version[product]

			# as currently latest lts version of bitbucket and confluence don't support k8s
			# we use non-lts version of those products in the test
			if cversion(lts_version) < cversion(known_supported_version[product]):
				lts_version = known_supported_version[product]
		except:
			lts_version = known_supported_version[product]

		lts_version = f"{lts_version}-jdk11"
	else:
		lts_version = 'unknown'

	return lts_version


def cversion(version):
	# this method converts the version to a unified string to be used for sort and compare versions correctly
	# e.g: '7.12.1' => '00007000120000100000'
	#      '7.3.19' => '00007000030001900000'
	vers = version.split(".")
	mapped_ver = ''
	for i in range(max(len(vers)-1, 4)):
		if len(vers) > i:
			# add zero in left side of version part and make a fixed size of 5 for each part
			mapped_ver += vers[i].zfill(5)
		else:
			# add '00000' if build/patch/minor part of version are missing
			mapped_ver += '00000'
	return mapped_ver


if __name__ == "__main__":
	if len(sys.argv) > 1:
		get_lts_version(sys.argv[1:])