.PHONY: help doc-docker doc-build doc-serve

help:
	    @echo "Atlassian DC helm charts"
	    @echo ""
	    @echo "Commands:"
	    @echo "docs - starts live server with documentation"

.DEFAULT_GOAL := help

doc-docker:
	@docker build -t squidfunk/mkdocs-material docs/build/

doc-build:
	@docker run --rm -it -v ${PWD}/docs:/docs squidfunk/mkdocs-material build

doc-serve:
	@docker run --rm -it -p 8000:8000 -v ${PWD}/docs:/docs squidfunk/mkdocs-material

docs: doc-docker doc-serve