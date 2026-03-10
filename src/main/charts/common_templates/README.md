# Common Templates

Shared Helm template helpers that are symlinked into each product chart's `templates/` directory.

## Why this pattern?

The existing `common/` library chart is published as a remote Helm dependency. Any change to it
requires a **two-step rollout**: publish the library first, then update each product chart's
`Chart.yaml` to reference the new version.

This `common_templates/` directory avoids that by using **symlinks**. Each product chart has a
symlink at `templates/common_templates → ../../common_templates`, so Helm picks up these templates
directly. Changes here take effect immediately across all products — no publishing, no version
bumps, single PR.

## When to use this vs `common/`

**All new shared templates should go in `common_templates/` (this directory).**

The `common/` library chart is a legacy pattern that requires a two-step release process for
every change. It should not be used for new shared templates. Existing templates in `common/`
(`_labels.tpl`, `_names.tpl`, `_jmx.tpl`) should be incrementally migrated here when they are
next modified — there is no technical reason to keep them in the library chart.

## How it works

Each product chart has a symlink:

```
src/main/charts/<product>/templates/common_templates → ../../common_templates
```

Helm follows symlinks during `helm template` and `helm package`, so:

- **Local development**: templates are resolved via the symlink
- **Published charts**: `helm package` embeds the actual file content in the `.tgz` — consumers see regular files, not
  symlinks

## Adding a new shared template

1. Create your `.tpl` file in this directory
2. That's it — all product charts pick it up automatically via the existing symlink

## Platform note

Git on Windows doesn't enable symlinks by default. Contributors on Windows need
`git config core.symlinks true` (or run Git as admin) for symlinks to work correctly.

