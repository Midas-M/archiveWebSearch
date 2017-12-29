# README #
Web archive search with SOLR and WAYBACK servers.

### What is this repository for? ###

This repo contains a web interface and a REST api intermediate service that allows a user to query a SOLR server for the web archive matching results.
The responce redirects the user to a wayback server instance.


In order to get this working one needs to configure the SOLR server with the schema found @/solr_conf
Then he needs to point the system to the SOLR server from the settings file. The solr should contain the indexed data that point to the wayback server.

The result is a seach engine that allows to search and navigate through archived data.

