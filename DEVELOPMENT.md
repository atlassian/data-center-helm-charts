# Development

## Pre-commit hooks

Repository contains repository hook definition to auto-generate chart documentation in case there is a change to `values.yaml` file.

### Installation

1. Install `pre-commit` following [official instructions](https://pre-commit.com/#install).
2. In the repository root run `pre-commit install` which will install the hooks.

Now the pre-commit hook will run for each commit.