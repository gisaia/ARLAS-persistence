# Configuring ARLAS Persistence running environment

## ARLAS Persistence configuration file

ARLAS Persistence is configured with a yaml configuration file.

External module configurations are available online:

| Module     | Link                                                          |
|------------|---------------------------------------------------------------|
| Swagger    | https://github.com/federecio/dropwizard-swagger               |
| Dropwizard | http://www.dropwizard.io/1.0.4/docs/manual/configuration.html |
| Zipkin     | https://github.com/smoketurner/dropwizard-zipkin              |

## Configure ARLAS Persistence as a docker container

#### With environment variables

ARLAS Persistence can run as a docker container. A rich set of properties of the configuration file can be overriden by passing environment variables to the container:

```shell
docker run -ti -d \
   --name arlas-persistence \
   -e "ARLAS_PERSISTENCE_HIBERNATE_DRIVER=org.postgresql.Driver" \
   gisaia/arlas-persistence:latest
```
All supported environment variables are listed below.

### With file/URL based configuration

Instead of overriding some properties of the configuration file, it is possible to start the ARLAS Persistence container with a given configuration file.

#### File

The ARLAS Persistence container can start with a mounted configuration file thanks to docker volume mapping. For instance, if the current directory of the host contains a `configuration.yaml` file, the container can be started as follow:

```shell
docker run -ti -d \
   --name arlas-persistence \
   -v `pwd`/configuration.yaml:/opt/app/configuration.yaml \
   gisaia/arlas-persistence:latest
```

#### URL

The ARLAS Persistence container can start with a configuration file that is downloaded before starting up. The configuration file must be available through an URL accessible from within the container. The URL is specified with an environment variable:

| Environment variable                | Description                                                                                       |
|-------------------------------------|---------------------------------------------------------------------------------------------------|
| ARLAS_PERSISTENCE_CONFIGURATION_URL | URL of the ARLAS Persistence configuration file to be downloaded by the container before starting |

For instance, if the current directory of the host contains a `configuration.yaml` file, the container can be started as follow:

```shell
docker run -ti -d \
   --name arlas-persistence \
   -e ARLAS_PERSISTENCE_CONFIGURATION_URL="http://somemachine/conf.yaml" \
   gisaia/arlas-persistence:latest
```

## ARLAS Persistence configuration properties

### Server

| Environment variable                  | ARLAS Server configuration variable                    | Default                            | Description                                                                         |
|---------------------------------------|--------------------------------------------------------|------------------------------------|-------------------------------------------------------------------------------------|
| ARLAS_PERSISTENCE_ACCESS_LOG_FILE     | server.requestLog.appenders.currentLogFilename         | arlas-persistence-access.log       |                                                                                     |
| ARLAS_PERSISTENCE_LOG_FILE_ARCHIVE    | server.requestLog.appenders.archivedLogFilenamePattern | arlas-persistence-access-%d.log.gz |                                                                                     |
| ARLAS_PERSISTENCE_APP_PATH            | server.applicationContextPath                          | /                                  | Base URL path                                                                       |
| ARLAS_PERSISTENCE_PREFIX              | server.rootPath                                        | /arlas_persistence_server          | Base sub-path for **general API**, gets appended to `server.applicationContextPath` |
| ARLAS_PERSISTENCE_ADMIN_PATH          | server.adminContextPath                                | /admin                             | Base sub-path for **admin API**, gets appended to `server.applicationContextPath`   |
| ARLAS_PERSISTENCE_PORT                | server.connector.port                                  | 9997                               |                                                                                     |
| ARLAS_PERSISTENCE_MAX_THREADS         | server.maxThreads                                      | 1024                               |                                                                                     |
| ARLAS_PERSISTENCE_MIN_THREADS         | server.minThreads                                      | 8                                  |                                                                                     |
| ARLAS_PERSISTENCE_MAX_QUEUED_REQUESTS | server.maxQueuedRequests                               | 1024                               |                                                                                     |

### URL Masking

| Environment variable       | ARLAS Server configuration variable | Default | Description                                                             |
|----------------------------|-------------------------------------|---------|-------------------------------------------------------------------------|
| ARLAS_PERSISTENCE_BASE_URI | arlas-base-uri                      | `None`  | Base URI to ARLAS Persistence. If not set, the real base URI is exposed |
    
### Logging

| Environment variable                                | ARLAS Server configuration variable                      | Default                  |
|-----------------------------------------------------|----------------------------------------------------------|--------------------------|
| ARLAS_PERSISTENCE_LOGGING_LEVEL                     | logging.level                                            | INFO                     |
| ARLAS_PERSISTENCE_LOGGING_CONSOLE_LEVEL             | logging.appenders[type: console].threshold               | INFO                     |
| ARLAS_PERSISTENCE_LOGGING_FILE                      | logging.appenders[type: file].currentLogFilename         | arlas-persistence.log    |
| ARLAS_PERSISTENCE_LOGGING_FILE_LEVEL                | logging.appenders[type: file].threshold                  | INFO                     |
| ARLAS_PERSISTENCE_LOGGING_FILE_ARCHIVE              | logging.appenders[type: file].archive                    | true                     |
| ARLAS_PERSISTENCE_LOGGING_FILE_ARCHIVE_FILE_PATTERN | logging.appenders[type: file].archivedLogFilenamePattern | arlas-persistence-%d.log |
| ARLAS_PERSISTENCE_LOGGING_FILE_ARCHIVE_FILE_COUNT   | logging.appenders[type: file].archivedFileCount          | 5                        |

### Zipkin

| Environment variable                  | ARLAS Server configuration variable | Default               |
|---------------------------------------|-------------------------------------|-----------------------|
| ARLAS_PERSISTENCE_ZIPKIN_ENABLED      | zipkin.enabled                      | false                 |
| ARLAS_PERSISTENCE_ZIPKIN_SERVICE_HOST | zipkin.serviceHost                  | 127.0.0.1             |
| ARLAS_PERSISTENCE_PORT                | zipkin.servicePort                  | 9997                  |
| ARLAS_PERSISTENCE_ZIPKIN_COLLECTOR    | zipkin.collector                    | http                  |
| ARLAS_PERSISTENCE_ZIPKIN_BASEURL      | zipkin.baseUrl                      | http://localhost:9411 |


### CORS, HEADERS for API response

| Environment variable           | ARLAS Server configuration variable | Default                                                                                                                 | Description                                                      |
|--------------------------------|-------------------------------------|-------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------|
| ARLAS_CORS_ENABLED             | arlas_cors.enabled                  | false                                                                                                                   | Whether to configure cors or not                                 |
| ARLAS_CORS_ALLOWED_ORIGINS     | arlas_cors.allowed_origins          | "*"                                                                                                                     | Comma-separated list of allowed origins                          |
| ARLAS_CORS_ALLOWED_HEADERS     | arlas_cors.allowed_headers          | "arlas-user,arlas-groups,arlas-organization,X-Requested-With,Content-Type,Accept,Origin,Authorization,X-Forwarded-User" | Comma-separated list of allowed headers                          |
| ARLAS_CORS_ALLOWED_METHODS     | arlas_cors.allowed_methods          | "OPTIONS,GET,PUT,POST,DELETE,HEAD"                                                                                      | Comma-separated list of allowed methods                          |
| ARLAS_CORS_ALLOWED_CREDENTIALS | arlas_cors.allowed_credentials      | true                                                                                                                    | Whether to allow credentials or not                              |
| ARLAS_CORS_EXPOSED_HEADERS     | arlas_cors.exposed_headers          | "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin,Location"                                     | Comma-separated list of exposed headers, readable on client side |

### AUTH

| Environment variable    | ARLAS Server configuration variable | Default                                     | Description                                                                          |
|-------------------------|-------------------------------------|---------------------------------------------|--------------------------------------------------------------------------------------|
| ARLAS_AUTH_POLICY_CLASS | arlas_auth_policy_class             | io.arlas.commons.rest.auth.NoPolicyEnforcer | Specify a PolicyEnforcer class to load in order to activate Authentication if needed |

### API 
| Environment variable                   | ARLAS Server configuration variable | Default              | Description                                                                                                 |
|----------------------------------------|-------------------------------------|----------------------|-------------------------------------------------------------------------------------------------------------|
| ARLAS_PERSISTENCE_ENGINE               | persistence_engine                  | 'file'               | Determine in which mode we run the api : sql or firestore, possible values : hibernate or firestore or file |
| ARLAS_PERSISTENCE_FIRESTORE_COLLECTION | firestore_collection                | 'user_pref'          | Name of the Firestore collection if relevant (required when engine is 'firestore')                          |
| ARLAS_PERSISTENCE_LOCAL_FOLDER         | local_folder                        | '/tmp/persist/'      | Folder name where data is persisted (required when engine is 'file')                                        |
| ARLAS_ORGANIZATION_HEADER              | arlas_organization_header           | 'arlas-organization' | HTTP header name holding the user's organisation                                                            |
| ARLAS_ANONYMOUS_VALUE                  | anonymous_value                     | 'anonymous'          | Default value for identity headers when authentication is not enabled                                       |


### Database Mandatory if persistence_engine is hibernate

| Environment variable                 | ARLAS Server configuration variable   | Default                                     | Description                                                  |
|--------------------------------------|---------------------------------------|---------------------------------------------|--------------------------------------------------------------|
| ARLAS_PERSISTENCE_HIBERNATE_DRIVER   | database.driverClass                  | org.postgresql.Driver                       | Driver to use for SGBD                                       |
| ARLAS_PERSISTENCE_HIBERNATE_USER     | database.user                         | pg-user                                     | User of SGBD                                                 |
| ARLAS_PERSISTENCE_HIBERNATE_PASSWORD | database.password                     | iAMs00perSecrEET                            | Password of SGBD                                             |
| ARLAS_PERSISTENCE_HIBERNATE_URL      | database.password                     | jdbc:postgresql://db:5432/arlas_persistence | Hibernate url to database, you can decide the name of the db |
| ARLAS_PERSISTENCE_HIBERNATE_DIALECT  | database.properties.hibernate.dialect | org.hibernate.dialect.PostgreSQLDialect     | Class for hibernate dialect                                  |

### CACHE

| Environment variable                  | ARLAS Server configuration variable | Default                               | Description                            |
|---------------------------------------|-------------------------------------|---------------------------------------|----------------------------------------|
| ARLAS_PERSISTENCE_CACHE_FACTORY_CLASS | arlas_cache_factory_class           | io.arlas.commons.cache.NoCacheFactory | Factory class to get the cache manager |
| ARLAS_CACHE_TIMEOUT                   | arlas-cache-timeout                 | 60                                    | TTL in seconds of items in the cache   |

### GOOGLE FIRESTORE Mandatory if persistence_engine is firestore
    
| Environment variable           | Description                                                               |
|--------------------------------|---------------------------------------------------------------------------|
 | GOOGLE_APPLICATION_CREDENTIALS | Path to file with google credential key json for Gcloud firestore service |
  
### JAVA
  
| Environment variable | Description            |
|----------------------|------------------------|
 | ARLAS_XMX            | Java Maximum Heap Size |
