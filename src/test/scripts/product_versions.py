import json
import re
import sys
import urllib.request

known_supported_version = {
	'bitbucket': '7.21.0',
	'jira-software': '8.13.8',
	'confluence': '7.12.2',
	'stash': '7.12.1',
	'mesh': '2.0.1',
	'bamboo': '8.1.1'  # Bamboo has no LTS versions.
}

# If tag suffix is desired - e.g. 7.8.0-jdk11 -> tag_suffix = "-jdk11"
tag_suffix = ""


def get_lts_version(argv):
	product = argv[0].lower()
	if product == 'jira':
		product = 'jira-software'
	elif product == 'bitbucket':
		product = 'stash'
	if product in known_supported_version:
		url_archived = f"https://my.atlassian.com/download/feeds/archived/{product}.json"
		url_current = f"https://my.atlassian.com/download/feeds/current/{product}.json"

		try:
			# load archived feeds
			archive_feeds = urllib.request.urlopen(url_archived).read()
			feeds = loadJSON(archive_feeds)

			# load current feeds and append to archive
			current_feeds = urllib.request.urlopen(url_current).read()
			feeds += loadJSON(current_feeds)

			# Get the latest non-lts version if the second arg is provided
			if len(argv) > 1:
			    fetch_latest = argv[1].lower()
			    sortedVersions = sorted(feeds, key=lambda k:cversion(k['version']), reverse=True)
			else:
				# Filter all LTS versions and sort based on version
				lts_versions = [x for x in feeds if x['edition'].lower() == 'enterprise']
				sortedVersions = sorted(lts_versions, key=lambda k:cversion(k['version']), reverse=True)

			if len(sortedVersions) > 0:
				# Pick the latest LTS product version
				lts_version = sortedVersions[0]['version']
			else:
				lts_version = known_supported_version[product]

			# Currently, latest lts version of Bitbucket and Confluence don't support K8s
			# We use non-lts version of those products in the test
			if cversion(lts_version) < cversion(known_supported_version[product]):
				lts_version = known_supported_version[product]
		except:
			lts_version = known_supported_version[product]

		lts_version = f"{lts_version}{tag_suffix}"
	else:
		lts_version = 'unknown'
	
	return lts_version


def loadJSON(fdata):
	result = re.search("\[.*\]", fdata.decode("utf-8"))
	if result is None:
		return []
	return json.loads(result.group(0))


def cversion(version):
	# This method converts the version to a unified string to be used to sort and compare versions correctly
	# E.g: '7.12.1' => '00007000120000100000'
	#      '7.3.19' => '00007000030001900000'
	vers = version.split(".")
	mapped_ver = ''
	for i in range(max(len(vers)-1, 4)):
		if len(vers) > i:
			# Add zero on left side of version part and make a fixed size of 5 for each part
			mapped_ver += vers[i].zfill(5)
		else:
			# Add '00000' if build/patch/minor part of version are missing
			mapped_ver += '00000'
	return mapped_ver


if __name__ == "__main__":
	if len(sys.argv) > 1:
		get_lts_version(sys.argv[1:])
