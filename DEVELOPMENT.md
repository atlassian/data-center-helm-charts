# Development

## Pre-commit hooks

Repository contains repository hook definition to auto-generate chart documentation in case there is a change to `values.yaml` file.

### Installation

1. Install `pre-commit` following [official instructions](https://pre-commit.com/#install).
2. In the repository root run `pre-commit install` which will install the hooks.

### Usage

When you have the pre-commit hook installed and `commit` a file, it will run the pre-commit hook. If there is a change generated in the `README.md`, the hook will fail and notify you about the new change. You can review the changes and run the `commit` again, now it should pass.