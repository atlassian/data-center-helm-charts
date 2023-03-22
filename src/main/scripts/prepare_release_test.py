import unittest
import logging
from prepare_release import gen_changelog, format_changelog_yaml

update_versions_message = "* Update appVersions for DC apps"
jira_key1 = "CLIP-1234"
jira_key2 = "DCCLIP-1234"

test_git_log = "* Something got updated\n" \
               + update_versions_message + " (#589)\n" \
               + update_versions_message + " (#589)\n" \
                                           "* Prepare release 1.10.12\n" \
                                           "* " + jira_key1 + ": Fixing a bug\n" \
                                           "* " + jira_key2 + ": Developing a feature\n" \
               + update_versions_message + " (#535)\n" \
               + update_versions_message + " (#543)"

no_git_log_messages = ""

print('Test git log:\n' + test_git_log)
git_log = gen_changelog("bamboo", ".", test_git_log, True)
empty_git_log = gen_changelog("bamboo", ".", no_git_log_messages, True)

print('\nProcessed git log:')
for message in git_log:
    print(message)
print('\n')


class TestGitLog(unittest.TestCase):

    def setUp(self):
        self.logger = logging.getLogger(__name__)
        self.logger.setLevel(logging.INFO)
        self.logger.info("Running test: {}".format(self._testMethodName))

    def test_remove_messages_with_smaller_pr_numbers(self):
        # asset that only commit message that starts with '* Update appVersions for DC apps'
        # and has the most recent commit in brackets is left in git log
        self.assertNotIn(update_versions_message + " (#533)", git_log)
        self.assertNotIn(update_versions_message + " (#543)", git_log)
        self.assertTrue(any(update_versions_message + " (#589)" in element for element in git_log))

    def test_remove_messages_matching_pattern(self):
        # assert commit messages that match the pattern are dropped
        self.assertNotIn('* Prepare release', git_log)

    def test_remove_jira_keys_from_messages(self):
        # assert Jira keys are removed from git log
        self.assertNotIn(jira_key1, git_log)
        self.assertNotIn(jira_key2, git_log)
        # asset the rest of the commit message makes it in the git log
        self.assertTrue(any('Developing a feature' in element for element in git_log))
        self.assertTrue(any('Fixing a bug' in element for element in git_log))

    def test_duplicate_messages(self):
        # set(git_log) will automatically remove duplicates from a list which will fail the assertion if that's the case
        self.assertCountEqual(git_log, set(git_log))

    def test_empty_git_log(self):
        # assert that if there are no commits, the default git log is returned
        self.assertEqual(empty_git_log[0], '* Update Helm chart version')

    def test_formatting(self):
        formatted_changelog = format_changelog_yaml(git_log)
        # assert '*' chars are removed and replaced with '-',
        # as well as string are wrapped in double quotes
        self.assertNotIn('*', formatted_changelog)
        self.assertIn('- "Fixing a bug"', formatted_changelog)
        self.assertIn('- "Developing a feature"', formatted_changelog)
        self.assertIn('- "Update appVersions for DC apps (#589)"', formatted_changelog)


if __name__ == '__main__':
    unittest.main(verbosity=2)
