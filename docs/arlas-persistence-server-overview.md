# ARLAS Persistence API Overview

## IMPORTANT
- 
- Two types of storage are available in this server:
  - All SQL SGBD compliant with hibernate, we use a non configurable unique table [`user_data`](../docker/docker-files/pgCreateTable.sql) in a dedicated configurable database.
  ARLAS Persistence WILL NOT CREATE the table for you, think to create table before running the server.
  - Google Cloud Firestore : think to set GOOGLE_APPLICATION_CREDENTIALS as environement variable.
- The key used to retrieve data in the database is passed by a header. This custom header is configurable with the key `key_header`. If this header is not present on the request the server will always return Error 500.
Think to check if the `key_header` is present in `arlas_cors.allowed_headers`configuration key.
 

The ARLAS Persistence offers 3 APIs:

- a `management` API for [Data](arlas-api-persistence.md), meaning adding,deleting,updating an entry in storage.
- an API for monitoring the server health and performances
- endpoints for testing the write API and the status API with swagger

## Monitoring

The monitoring API provides some information about the health and the performances of the ARLAS Persistence that can be of interest:

| URL | Description |
| --- | --- |
| http://.../admin/metrics?pretty=true  |  Metrics about the performances of the ARLAS Persistence.|
| http://.../admin/ping | Returns pong  |
| http://.../admin/threads | List of running threads |
| http://.../admin/healthcheck?pretty=true  |  Whether the service is healthy or not |


## Swagger

| URL | Description |
| --- | --- |
| http://.../arlas_persistence/swagger  | The web application for testing the API  |
| http://.../arlas_persistence/swagger.yaml  | The swagger definition of the collections/exploration API with YAML format |
| http://.../arlas_persistence/swagger.json  | The swagger definition of the collections/exploration API with JSON format |

