# Service Configuration for Anthos

## Environment variables

### Must have

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `SPRING_PROFILES_ACTIVE` | ex `anthos` | Spring profile that activate default configuration for GCP environment | false | - |
| `OPENID_PROVIDER_CLIENT_ID` | `*****` |  Client id that represents this service and serves to request tokens, example `workload-identity-legal` |yes| - |
| `OPENID_PROVIDER_CLIENT_SECRET` | `*****` | This client secret that serves to request tokens| yes | - |
| `OPENID_PROVIDER_URL` | `https://keycloack.com/auth/realms/master` | URL of OpenID Connect provider, it will be used as `<OpenID URL> + /.well-known/openid-configuration` to auto configure endpoint for token request  | no | - |
| `<POSTGRES_PASSWORD_ENV_VARIABLE_NAME>` | ex `POSTGRES_PASS_OSDU` | Postgres password env name, name of that variable not defined at the service level, the name will be received through partition service. Each tenant can have it's own ENV name value, and it must be present in ENV of File service | yes | - |
| `<MINIO_SECRETKEY_ENV_VARIABLE_NAME>` | ex `MINIO_SECRET_OSDU` | Minio secret env name, name of that variable not defined at the service level, the name will be received through partition service. Each tenant can have it's own ENV name value, and it must be present in ENV of File service| yes | - |
| `<AMQP_PASSWORD_ENV_VARIABLE_NAME>` | ex `AMQP_PASS_OSDU` | Amqp password env name, name of that variable not defined at the service level, the name will be received through partition service. Each tenant can have it's own ENV name value, and it must be present in ENV of File service | yes | - |
| `<AMQP_ADMIN_PASSWORD_ENV_VARIABLE_NAME>` | ex `AMQP_ADMIN_PASS_OSDU` | Amqp admin password env name, name of that variable not defined at the service level, the name will be received through partition service. Each tenant can have it's own ENV name value, and it must be present in ENV of File service | yes | - |

### Common-properties-for-all-environments

Define the following environment variables.
Most of them are common to all hosting environments, but there are properties that are only necessary when running in Google Cloud.

In order to run the service locally, you will need to have the following environment variables defined.

**Required to run application**

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `LOG_PREFIX` | `file` | Logging prefix | no | - |
| `OSDU_ENTITLEMENTS_URL` | `/https://entitlements.com/entitlements/v1` | Entitlements API endpoint | no | output of infrastructure deployment |
| `RECORDS_ROOT_URL` | ex `https://os-storage-dot-nice-etching-277309.uc.r.appspot.com/api/storage/v2` / Storage API endpoint | no | output of infrastructure deployment |
| `GCP_STORAGE_STAGING_AREA` | ex `staging-area` | staging area bucket(will be concatenated with project id ex `osdu-cicd-epam-staging-area`) |no | output of infrastructure deployment |
| `GCP_STORAGE_PERSISTENT_AREA` | ex `persistent-area` | persistent area bucket(will be concatenated with project id ex `osdu-cicd-epam-persistent-area` | no | output of infrastructure deployment |
| `PARTITION_API` | ex `http://localhost:8081/api/partition/v1` | Partition service endpoint | no | - |
| `GCP_STATUS_CHANGED_MESSAGING_ENABLED` | `true` or `false` | If set `true`then status messages will be published to specified topic, otherwise stub publisher will write messages to logs| no | - |
| `GCP_STATUS_CHANGED_TOPIC` | ex `status-changed` | PubSub topic for status publishing | no | output of infrastructure deployment |
| `GCP_FILE_LOCATION_KIND` | by default `file-locations-osm` | Kind for Datastore or Table for postgres  | no | - |

These variables define service behavior, and are used to switch between `anthos` or `gcp` environments, their overriding and usage in mixed mode was not tested.
Usage of spring profiles is preferred.

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `OBMDRIVER` | `minio` | Obm driver mode that defines which object storage will be used | no | - |
| `OQMDRIVER` | `rabbitmq` | Oqm driver mode that defines which message broker will be used | no | - |
| `OSMDRIVER` | `postgres` | Osm driver mode that defines which KV storage will be used | no | - |
| `PARTITION_AUTH_ENABLED` | ex `true` or `false` | Disable or enable auth token provisioning for requests to Partition service | no | - |
| `SERVICE_TOKEN_PROVIDER` | `GCP` or `OPENID` |Service account token provider, `GCP` means use Google service account `OPEIND` means use OpenId provider like `Keycloak` | no | - |

### Properties set in Partition service:

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

### For OSM Postgres

**prefix:** `osm.postgres`

It can be overridden by:

- through the Spring Boot property `osm.postgres.partition-properties-prefix`
- environment variable `OSM_POSTGRES_PARTITION_PROPERTIES_PREFIX`

**PropertySet:**

| Property | Description |
| --- | --- |
| osm.postgres.datasource.url | server URL |
| osm.postgres.datasource.username | username |
| osm.postgres.datasource.password | password |

<details><summary>Example of a definition for a single tenant</summary>

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


**database structure**
OSM works with data logically organized as "partition"->"namespace"->"kind"->"record"->"columns".
The above sequence describes how it is named in Google Datastore, where "partition" maps to "GCP
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
| partition (GCP project)   | ==  | Postgres server URL        |
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
CREATE TABLE osdu."file-locations-osm"(
    id text COLLATE pg_catalog."default" NOT NULL,
    pk bigint NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    data jsonb NOT NULL,
    CONSTRAINT ExampleKind_id UNIQUE (id)
);
CREATE INDEX FileLocationsOsm_datagin ON osdu."file-locations-osm" USING GIN (data);
```


### For OBM MinIO

**prefix:** `obm.minio`
It can be overridden by:

- through the Spring Boot property `osm.postgres.partition-properties-prefix`
- environment variable `OBM_MINIO_PARTITION_PROPERTIES_PREFIX`

**PropertySet:**

| Property            | Description            |
|---------------------|------------------------|
| obm.minio.endpoint  | server URL             |
| obm.minio.accessKey | credentials access key |
| obm.minio.secretKey | credentials secret key |

<details><summary>Example of a definition for a single tenant</summary>

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

### Object store configuration <a name="ObjectStoreConfig"></a>

#### Used Technology
MinIO (or any other supported by OBM)

#### Per-tenant buckets configuration
These buckets must be defined in tenants’ dedicated object store servers. OBM connection properties of these servers (url, etc.) are defined as specific properties in tenants’ PartitionInfo registration objects at the Partition service as described in accordant sections of this document.

<table>
  <tr>
   <td>Bucket Naming template
   </td>
   <td>Permissions required
   </td>
  </tr>
  <tr>
   <td>&lt;PartitionInfo.projectId>-$GCP_STORAGE_STAGING_AREA:<strong>staging-area</strong>
   </td>
   <td>ListObjects, CRUDObject
   </td>
  </tr>
  <tr>
   <td>&lt;PartitionInfo.projectId>-$GCP_STORAGE_PERSISTENT_AREA:<strong>persistent-area</strong>
   </td>
   <td>ListObjects, CRUDObject
   </td>
  </tr>
</table>

### For OQM RabbitMQ

**prefix:** `oqm.rabbitmq`
It can be overridden by:

- through the Spring Boot property `oqm.rabbitmq.partition-properties-prefix`
- environment variable `OQM_RABBITMQ_PARTITION_PROPERTIES_PREFIX``

**PropertySet** (for two types of connection: messaging and admin operations):

| Property | Description |
| --- | --- |
| oqm.rabbitmq.amqp.host | messaging hostname or IP |
| oqm.rabbitmq.amqp.port | - port |
| oqm.rabbitmq.amqp.path | - path |
| oqm.rabbitmq.amqp.username | - username |
| oqm.rabbitmq.amqp.password | - password |
| oqm.rabbitmq.admin.schema | admin host schema |
| oqm.rabbitmq.admin.host | - host name |
| oqm.rabbitmq.admin.port | - port |
| oqm.rabbitmq.admin.path | - path |
| oqm.rabbitmq.admin.username | - username |
| oqm.rabbitmq.admin.password | - password |

<details><summary>Example of a single tenant definition</summary>

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


#### Exchanges and queues configuration

At RabbitMq should be created exchange with name:

**name:** `status-changed`

It can be overridden by:

- through the Spring Boot property `gcp.status-changed.topicName`
- environment variable `STATUS_CHANGED_TOPIC_NAME`

ex.
![Screenshot](./pics/rabbit.PNG)
