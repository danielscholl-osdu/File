# Service Configuration for Baremetal

## Table of Contents <a name="TOC"></a>

* [Configuration structure](#configuration-structure)
* [Service level config](#service-level-config)
  * [ENV variables to override service level defaults](#env-variables-to-override-service-level-defaults)
  * [ENV variables to override environment level defaults](#env-variables-to-override-environment-level-defaults)
  * [Service level secret ENV variables](#service-level-secret-env-variables)
* [Partition level config](#partition-level-config)
  * [Prefixes](#prefixes)
  * [Non-sensitive partition properties](#non-sensitive-partition-properties)
  * [Sensitive partition properties](#sensitive-partition-properties)
  * [Partition level secret ENV variables](#partition-level-secret-env-variables)
* [Infrastructure config](#infrastructure-config)
  * [Postgres config](#postgres-config)
  * [Minio config](#minio-config)
  * [RabbitMq config](#rabbitmq-config)
* [Testing config](#testing-config)
* [Local config](#local-config)
* [Config examples](#config-examples)

## Configuration structure
1. Service level config
   - Spring config `application.properties` - base layer, service level defaults
   - Spring profiles `application-{environment}.properties` - environment level defaults, overrides `application.properties`
   - ENV variables - overrides service and environment level defaults
   - Secret ENV variables - stores secret values in runtime only
2. Partition level config
   - Non-sensitive partition properties
   - Sensitive partition properties referencing to Secret ENV variables
   - Secret ENV variables - stores secret values in runtime only
3. Infrastructure config
   - Service account entitlements groups
   - Service account RBAC permissions
   - Third party services config
4. Testing config
   - Testing accounts
   - ENV variables
5. Local config
   - ENV variables

***

## Service level config

### ENV variables to override service level defaults
| name                                            | value                                      | description                                                                                                                  | sensitive? | source                              |
|-------------------------------------------------|--------------------------------------------|------------------------------------------------------------------------------------------------------------------------------|------------|-------------------------------------|
| `LOG_LEVEL`                                     | `INFO`                                     | Logging level                                                                                                                | no         | -                                   |
| `LOG_PREFIX`                                    | `file`                                     | Logging prefix                                                                                                               | no         | -                                   |
| `GCP_STORAGE_STAGING_AREA`                      | ex `staging-area`                          | staging area bucket(will be concatenated with project id ex `osdu-cicd-epam-staging-area`)                                   | no         | output of infrastructure deployment |
| `GCP_STORAGE_PERSISTENT_AREA`                   | ex `persistent-area`                       | persistent area bucket(will be concatenated with project id ex `osdu-cicd-epam-persistent-area`                              | no         | output of infrastructure deployment |
| `PARTITION_PROPERTIES_STAGING_LOCATION_NAME`    | ex `file.staging.location`                 | name of partition property for staging location value                                                                        | yes        | -                                   |
| `PARTITION_PROPERTIES_PERSISTENT_LOCATION_NAME` | ex `file.persistent.location`              | name of partition property for persistent location value                                                                     | yes        | -                                   |
| `GCP_STATUS_CHANGED_MESSAGING_ENABLED`          | `true` or `false`                          | If set `true`then status messages will be published to specified topic, otherwise stub publisher will write messages to logs | no         | -                                   |
| `GCP_STATUS_CHANGED_TOPIC`                      | ex `status-changed`                        | PubSub topic for status publishing                                                                                           | no         | output of infrastructure deployment |
| `GCP_FILE_LOCATION_KIND`                        | by default `file-locations-osm`            | Kind for Datastore or Table for postgres                                                                                     | no         | -                                   |
| `MANAGEMENT_ENDPOINTS_WEB_BASE`                 | ex `/`                                     | Web base for Actuator                                                                                                        | no         | -                                   |
| `MANAGEMENT_SERVER_PORT`                        | ex `8081`                                  | Port for Actuator                                                                                                            | no         | -                                   |

### ENV variables to override environment level defaults
These variables define service behavior, and are used to switch between `reference` or `Google Cloud` environments,
their overriding and usage in mixed mode was not tested. Usage of spring profiles is preferred.

| name                     | value                 | required | description                                                                                                               | sensitive? | source                              |
|--------------------------|-----------------------|----------|---------------------------------------------------------------------------------------------------------------------------|------------|-------------------------------------|
| `SPRING_PROFILES_ACTIVE` | ex `anthos`           | YES      | Spring profile that activate default configuration for Google Cloud environment                                           | no         | -                                   |
| `OBMDRIVER`              | `minio`               |          | Obm driver mode that defines which object storage will be used                                                            | no         | -                                   |
| `OQMDRIVER`              | `rabbitmq`            |          | Oqm driver mode that defines which message broker will be used                                                            | no         | -                                   |
| `OSMDRIVER`              | `postgres`            |          | Osm driver mode that defines which KV storage will be used                                                                | no         | -                                   |
| `PARTITION_AUTH_ENABLED` | `true` or `false`     |          | Disable or enable auth token provisioning for requests to Partition service                                               | no         | -                                   |
| `SERVICE_TOKEN_PROVIDER` | `GCP` or `OPENID`     |          | Service account token provider, `GCP` means use Google service account `OPEIND` means use OpenId provider like `Keycloak` | no         | -                                   |
| `ENTITLEMENTS_HOST`      | `http://entitlements` |          | Entitlements service host address                                                                                         | no         | output of infrastructure deployment |
| `STORAGE_HOST`           | `http://storage`      |          | Storage service host address                                                                                              | no         | output of infrastructure deployment |
| `PARTITION_HOST`         | `http://partition`    |          | Partition service host address                                                                                            | no         | output of infrastructure deployment |

### Service level secret ENV variables
| name                            | value                                      | description                                                                                                                                        | sensitive? | source |
|---------------------------------|--------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|------------|--------|
| `OPENID_PROVIDER_CLIENT_ID`     | `*****`                                    | Client id that represents this service and serves to request tokens, example `workload-identity-legal`                                             | yes        | -      |
| `OPENID_PROVIDER_CLIENT_SECRET` | `*****`                                    | This client secret that serves to request tokens                                                                                                   | yes        | -      |
| `OPENID_PROVIDER_URL`           | `https://keycloack.com/auth/realms/master` | URL of OpenID Connect provider, it will be used as `<OpenID URL> + /.well-known/openid-configuration` to auto configure endpoint for token request | no         | -      |

## Partition level config

### Prefixes
**prefix:** `osm.postgres`

It can be overridden by:

- through the Spring Boot property `osm.postgres.partition-properties-prefix`
- environment variable `OSM_POSTGRES_PARTITION_PROPERTIES_PREFIX`

**prefix:** `obm.minio`
It can be overridden by:

- through the Spring Boot property `osm.postgres.partition-properties-prefix`
- environment variable `OBM_MINIO_PARTITION_PROPERTIES_PREFIX`

**prefix:** `oqm.rabbitmq`
It can be overridden by:

- through the Spring Boot property `oqm.rabbitmq.partition-properties-prefix`
- environment variable `OQM_RABBITMQ_PARTITION_PROPERTIES_PREFIX`

### Non-sensitive partition properties
| name                                  | value                         | description                                | sensitive? | source                                          |
|---------------------------------------|-------------------------------|--------------------------------------------|------------|-------------------------------------------------|
| `<STAGING_LOCATION_PROPERTY_NAME>`    | ex `project.partition.bucket` | staging location address in OBM storage    | no         | `PARTITION_PROPERTIES_STAGING_LOCATION_NAME`    |
| `<PERSISTENT_LOCATION_PROPERTY_NAME>` | ex `project.partition.bucket` | persistent location address in OBM storage | no         | `PARTITION_PROPERTIES_PERSISTENT_LOCATION_NAME` |

### Sensitive partition properties
Note that properties can be set in Partition as `sensitive` in that case in property `value` should be present **not value itself**, but **ENV variable name**.
This variable should be present in environment of service that need that variable.

Example:

```
    "elasticsearch.port": {
      "sensitive": false, <- value not sensitive
      "value": "9243"  <- will be used as is.
    },
      "elasticsearch.password": {
      "sensitive": true, <- value is sensitive
      "value": "ELASTIC_SEARCH_PASSWORD_OSDU" <- service consumer should have env variable ELASTIC_SEARCH_PASSWORD_OSDU with elastic search password
    }
```

| name                               | description                                                                                                                           |
|------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| `osm.postgres.datasource.url`      | Postgres url                                                                                                                          |
| `osm.postgres.datasource.username` | Postgres username                                                                                                                     |
| `osm.postgres.datasource.password` | Postgres password                                                                                                                     |
| `oqm.rabbitmq.amqp.password`       | Amqp username                                                                                                                         |
| `oqm.rabbitmq.amqp.password`       | Amqp password                                                                                                                         |
| `oqm.rabbitmq.admin.username`      | Amqp admin username                                                                                                                   |
| `oqm.rabbitmq.admin.password`      | Amqp admin password                                                                                                                   |
| `obm.minio.endpoint`               | server URL                                                                                                                            |
| `obm.minio.accessKey`              | credentials access key                                                                                                                |
| `obm.minio.secretKey`              | credentials secret key                                                                                                                |
| `obm.minio.ignoreCertCheck`        | optional, default value is 'false'. When set to 'true' disables certificate check for MinIO client                                    |
| `obm.minio.external.endpoint`      | optional, used when service should use internal endpoint(in cluster) but must provide credentials for end users for external endpoint |
| `oqm.rabbitmq.amqp.host`           | messaging hostname or IP                                                                                                              |
| `oqm.rabbitmq.amqp.port`           | - port                                                                                                                                |
| `oqm.rabbitmq.amqp.path`           | - path                                                                                                                                |
| `oqm.rabbitmq.amqp.username`       | - username                                                                                                                            |
| `oqm.rabbitmq.amqp.password`       | - password                                                                                                                            |
| `oqm.rabbitmq.admin.schema`        | admin host schema                                                                                                                     |
| `oqm.rabbitmq.admin.host`          | - host name                                                                                                                           |
| `oqm.rabbitmq.admin.port`          | - port                                                                                                                                |
| `oqm.rabbitmq.admin.path`          | - path                                                                                                                                |
| `oqm.rabbitmq.admin.username`      | - username                                                                                                                            |
| `oqm.rabbitmq.admin.password`      | - password                                                                                                                            |

### Partition level secret ENV variables
| name                                          | value                        | description                         |
|-----------------------------------------------|------------------------------|-------------------------------------|
| `<POSTGRES_URL_ENV_VARIABLE_NAME>`            | ex `POSTGRES_URL`            | Postgres url sensitive value        |
| `<POSTGRES_USERNAME_ENV_VARIABLE_NAME>`       | ex `POSTGRES_USERNAME`       | Postgres username sensitive value   |
| `<POSTGRES_PASSWORD_ENV_VARIABLE_NAME>`       | ex `POSTGRES_PASSWORD`       | Postgres password sensitive value   |
| `<MINIO_ACCESSKEY_ENV_VARIABLE_NAME>`         | ex `MINIO_ACCESS_KEY`        | Minio access key sensitive value    |
| `<MINIO_SECRETKEY_ENV_VARIABLE_NAME>`         | ex `MINIO_SECRET_KEY`        | Minio secret sensitive value        |
| `<RABBITMQ_USERNAME_ENV_VARIABLE_NAME>`       | ex `RABBITMQ_USERNAME`       | Amqp username sensitive value       |
| `<RABBITMQ_PASSWORD_ENV_VARIABLE_NAME>`       | ex `RABBITMQ_PASSWORD`       | Amqp password sensitive value       |
| `<RABBITMQ_ADMIN_USERNAME_ENV_VARIABLE_NAME>` | ex `RABBITMQ_ADMIN_USERNAME` | Amqp admin username sensitive value |
| `<RABBITMQ_ADMIN_PASSWORD_ENV_VARIABLE_NAME>` | ex `RABBITMQ_ADMIN_PASSWORD` | Amqp admin password sensitive value |

## Infrastructure config

### Postgres config
**database structure**
OSM works with data logically organized as "partition"->"namespace"->"kind"->"record"->"columns".
The above sequence describes how it is named in Google Datastore, where "partition" maps to "Google Cloud"
project".

For example, this is how **Datastore** OSM driver contains records for "RecordsChanged" data
register:

| hierarchy level     | value                            |
|---------------------|----------------------------------|
| partition (opendes) | osdu-cicd-epam                   |
| namespace           | opendes                          |
| kind                | StorageRecord                    |
| record              | `<multiple kind records>`        |
| columns             | acl; bucket; kind; legal; etc... |

And this is how **Postgres** OSM driver does. Notice, the above hierarchy is kept, but Postgres uses
alternative entities for it.

| Datastore hierarchy level |     | Postgres alternative used  |
|---------------------------|-----|----------------------------|
| partition (Google Cloud project)   | ==  | Postgres server URL        |
| namespace                 | ==  | Schema                     |
| kind                      | ==  | Table                      |
| record                    | ==  | '<multiple table records>' |
| columns                   | ==  | id, data (jsonb)           |

As we can see in the above table, Postgres uses different approach in storing business data in
records. Not like Datastore, which segments data into multiple physical columns, Postgres organises
them into the single JSONB "data"
column. It allows provisioning new data registers easily not taking care about specifics of certain
registers structure. In the current OSM version (as on December'21) the Postgres OSM driver is not
able to create new tables in runtime.

So this is a responsibility of DevOps / CI/CD to provision all required SQL tables (for all required
data kinds) when on new environment or tenant provisioning when using Postgres. Detailed
instructions (with examples) for creating new tables is in the **OSM module Postgres driver
README.md** `org/opengroup/osdu/core/gcp/osm/translate/postgresql/README.md`

As a quick shortcut, this example snippet can be used by DevOps DBA:

```postgres-psql
--CREATE SCHEMA "osdu";
CREATE TABLE osdu."file_locations_osm"(
    id text COLLATE pg_catalog."default" NOT NULL,
    pk bigint NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    data jsonb NOT NULL,
    CONSTRAINT file_locations_osm_id UNIQUE (id)
);
CREATE INDEX file_locations_osm_datagin ON osdu."file_locations_osm" USING GIN (data);
```

### Minio config

These buckets must be defined in tenants’ dedicated object store servers. OBM connection properties of these servers (url, etc.) are defined as specific properties in tenants’ PartitionInfo registration objects at the Partition service as described in accordant sections of this document.

<table>
  <tr>
   <td>Bucket Naming template
   </td>
   <td>Permissions required
   </td>
  </tr>
  <tr>
   <td>&lt;PartitionInfo.projectId-PartitionInfo.name>-$GCP_STORAGE_STAGING_AREA:<strong>staging-area</strong>
   </td>
   <td>ListObjects, CRUDObject, SignedURLs
   </td>
  </tr>
  <tr>
   <td>&lt;PartitionInfo.projectId-PartitionInfo.name>-$GCP_STORAGE_PERSISTENT_AREA:<strong>persistent-area</strong>
   </td>
   <td>ListObjects, CRUDObject, SignedURLs
   </td>
  </tr>
</table>

### RabbitMq config

At RabbitMq should be created exchange with name:

**name:** `status-changed`

It can be overridden by:

- through the Spring Boot property `gcp.status-changed.topicName`
- environment variable `STATUS_CHANGED_TOPIC_NAME`

ex.
![Screenshot](./pics/rabbit.PNG)

## Testing config

### Testing accounts Entitlements groups
| INTEGRATION_TESTER |
| ---  |
| users<br/>service.file.editors<br/>service.file.viewers |

### Testing ENV variables
| name                                 | value                                      | description                         | sensitive?                              | source |
|--------------------------------------|--------------------------------------------|-------------------------------------|-----------------------------------------|--------|
| `FILE_SERVICE_HOST`                  | ex `http://localhost:8080`                 | File service url                    | no                                      | -      |
| `ACL_OWNERS`                         | `data.default.owners`                      | Acl owners group prefix             | no                                      | -      |
| `ACL_VIEWERS`                        | `data.default.viewers`                     | Acl viewers group prefix            | no                                      | -      |
| `DOMAIN`                             | ex `osdu-gc.go3-nrg.projects.epam.com`     | -                                   | no                                      | -      |
| `TENANT_NAME`                        | `opendes`                                  | Tenant name                         | no                                      | -      |
| `SHARED_TENANT`                      | `opendes`                                  | Shared tenant id                    | no                                      | -      |
| `PRIVATE_TENANT1`                    | `opendes`                                  | Private tenant id                   | no                                      | -      |
| `PRIVATE_TENANT2`                    | `opendes`                                  | Private tenant id                   | no                                      | -      |
| `LEGAL_TAG`                          | ex `opendes-storage-tag`                   | Valid legal tag name                | -                                       | -      |
| `TEST_OPENID_PROVIDER_CLIENT_ID`     | `********`                                 | Client Id for `$INTEGRATION_TESTER` | yes                                     | --     |
| `TEST_OPENID_PROVIDER_CLIENT_SECRET` | `********`                                 |                                     | Client secret for `$INTEGRATION_TESTER` | --     |
| `TEST_OPENID_PROVIDER_URL`           | ex `https://keycloak.com/auth/realms/osdu` | OpenID provider url                 | yes                                     | --     |

## Local config

### Local ENV variables
| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |

***

### Config examples

<details><summary>Example of a OSM configuration for a single tenant</summary>

```

curl -L -X PATCH 'http://partition.com/api/partition/v1/partitions/opendes' -H 'data-partition-id: opendes' -H 'Authorization: Bearer ...' -H 'Content-Type: application/json' --data-raw '{
  "properties": {
    "osm.postgres.datasource.url": {
      "sensitive": false,
      "value": "jdbc:postgresql://127.0.0.1:5432/postgres"
    },
    "osm.postgres.datasource.username": {
      "sensitive": false,
      "value": "postgres"
    },
    "osm.postgres.datasource.password": {
      "sensitive": true,
     "value": "<POSTGRES_PASSWORD_ENV_VARIABLE_NAME>" <- (Not actual value, just name of env variable)
    }
  }
}'

```

</details>

<details><summary>Example of a OBM config for a single tenant</summary>

```
curl -L -X PATCH 'https:///api/partition/v1/partitions/opendes' -H 'data-partition-id: opendes' -H 'Authorization: Bearer ...' -H 'Content-Type: application/json' --data-raw '{
  "properties": {
    "obm.minio.endpoint": {
      "sensitive": false,
      "value": "http://localhost:9000"
    },
    "obm.minio.accessKey": {
      "sensitive": false,
      "value": "minioadmin"
    },
    "obm.minio.secretKey": {
      "sensitive": true,
      "value": "NAME_ENV_VARIABLE_WHICH_WILL_BE_IN_SERVICE_ENV"
    }
  }
}'
```

</details>

<details><summary>Example of a OQM config for single tenant</summary>

```
curl -L -X PATCH 'https://dev.osdu.club/api/partition/v1/partitions/opendes' -H 'data-partition-id: opendes' -H 'Authorization: Bearer ...' -H 'Content-Type: application/json' --data-raw '{
  "properties": {
    "oqm.rabbitmq.amqp.host": {
      "sensitive": false,
      "value": "localhost"
    },
    "oqm.rabbitmq.amqp.port": {
      "sensitive": false,
      "value": "5672"
    },
    "oqm.rabbitmq.amqp.path": {
      "sensitive": false,
      "value": ""
    },
    "oqm.rabbitmq.amqp.username": {
      "sensitive": true,
      "value": "NAME_ENV_VARIABLE_WHICH_WILL_BE_IN_SERVICE_ENV"
    },
    "oqm.rabbitmq.amqp.password": {
      "sensitive": true,
      "value": "NAME_ENV_VARIABLE_WHICH_WILL_BE_IN_SERVICE_ENV"
    },

     "oqm.rabbitmq.admin.schema": {
      "sensitive": false,
      "value": "http"
    },
     "oqm.rabbitmq.admin.host": {
      "sensitive": false,
      "value": "localhost"
    },
    "oqm.rabbitmq.admin.port": {
      "sensitive": false,
      "value": "9002"
    },
    "oqm.rabbitmq.admin.path": {
      "sensitive": false,
      "value": "/api"
    },
    "oqm.rabbitmq.admin.username": {
      "sensitive": false,
      "value": "guest"
    },
    "oqm.rabbitmq.admin.password": {
      "sensitive": true,
      "value": "NAME_ENV_VARIABLE_WHICH_WILL_BE_IN_SERVICE_ENV"
    }
  }
}'
```

</details>

***
