# HTTP Basic auth for ElasticSearch

This plugin provides an extension of ElasticSearchs HTTP Transport module to enable HTTP Basic authorization.

Requesting / does not request authentication to simplify health heck configuration.

## Installation

Download the current version from https://github.com/Asquera/elasticsearch-http-basic/downloads and copy it to `plugins/http-basic`.
    
## Configuration

The plugin is disabled by default. Enabling basic authorization will disable the default HTTP Transport module.

```
http.basic.enabled: true
http.basic.user: "my_username"
http.basic.password: "my_password"
```

Be aware that the password is stored in plain text.

## Testing

```
$ curl -v localhost:9200 # works
$ curl -v --user my_username:my_password localhost:9200/foo # works
$ curl -v --user my_username:password localhost:9200/foo # sends 401
```

## Problems: Elasticsearch version and build

This version of the plugin sends the 'WWW-Authorize' header.

However it requires a branch of elastic-search that supports setting custom http-headers to response:
https://github.com/elasticsearch/elasticsearch/pull/2936

Once this is merged, this version of the plugin will require version 0.90.0.RC3
Please vote on the [PR] (https://github.com/elasticsearch/elasticsearch/pull/2936) to make sure it happens.

## Issues

Please file your issue here: https://github.com/Asquera/elasticsearch-http-basic/issues
