# Development

## Chart dependencies

Product charts are using a common library chart (`common`). To download the dependency, you are required to run:

```bash
for d in ./src/main/charts/*/ ; do (cd $d && helm dependency update); done
```

## Pre-commit hooks

Repository contains repository hook definition to auto-generate chart documentation in case there is a change to `values.yaml` file.

### Installation

1. Install `pre-commit` following [official instructions](https://pre-commit.com/#install).
2. In the repository root run `pre-commit install`, which will install the hooks.

### Usage

When you have the pre-commit hook installed and `commit` a file, it will run the pre-commit hook. If there is a change generated in the `README.md`, the hook will fail and notify you about the new change. You can review the changes and run the `commit` again, now it should pass.


# Technical documentation

## Setup

Technical documentation is produced by `mkdocs` python library. The documentation and necessary files are located in `/docs/` folder.

## How to work with documentation

To make working with the documentation easier, there is a `Makefile` in the project root folder. To start the server just run:

    make docs

This will create a docker image locally, process the files and start the live server providing documentation. You should be able to open http://127.0.0.1:8000/ in your browser. If you make any change to the documentation in the `/docs/` folder, it will be picked up by the server and browser will automatically reload.

## Deployment

Documentation is automatically deployed to [official documentation](https://atlassian.github.io/data-center-helm-charts/) with Github Pages. Any change that is merged into the default branch will trigger Github Actions that builds the static documentation site and pushes it to `gh-pages` branch. This branch is then deployed by Github Pages.