# ARLAS Persistence API Overview

## IMPORTANT
 
Three types of storage are available in this server:
  - File system storage
  - All SQL SGBD compliant with hibernate, we use a non configurable unique table [`user_data`](../docker/docker-files/pgCreateTable.sql) in a dedicated configurable database.
  ARLAS Persistence WILL NOT CREATE the table for you, think to create table before running the server. 
  **WARNING** if you want to use this storage, make sure you have the environment variable ``ARLAS_PERSISTENCE_ENGINE`` set to **"hibernate"** with the double quote.
  - Google Cloud Firestore : think to set GOOGLE_APPLICATION_CREDENTIALS as environement variable.
 

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

