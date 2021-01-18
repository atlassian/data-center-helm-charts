FROM fluent/fluentd:v1.12-1

# Dockerfile taken from customisation example at https://hub.docker.com/r/fluent/fluentd/

# Use root account to use apk
USER root

RUN apk add --no-cache --update --virtual .build-deps \
        sudo build-base ruby-dev \
 && sudo gem install fluent-plugin-elasticsearch \
 && sudo gem sources --clear-all \
 && apk del .build-deps \
 && rm -rf /tmp/* /var/tmp/* /usr/lib/ruby/gems/*/cache/*.gem

USER fluent