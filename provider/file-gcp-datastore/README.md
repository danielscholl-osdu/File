# File Service

The OSDU R2 File service provides internal and external API endpoints to let the application or user
fetch any records from the system or request file location data. For example, users can request
generation of an individual signed URL per file. Using a signed URL, OSDU R2 users will be able to
upload their files to the system.

The current implementation of the File service supports only cloud platform-specific locations. The
future implementations might allow the use of on-premises locations.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for
development and testing purposes. See deployment for notes on how to deploy the project on a live
system.

### Prerequisites

Pre-requisites

* GCloud SDK with java (latest version)
* JDK 8
* Lombok 1.16 or later
* Maven

### Environment Variables

In order to run the service locally, you will need to have the following environment variables
defined.

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `LOG_PREFIX` | `ingest` | Logging prefix | no | - |
| `OSDU_ENTITLEMENTS_URL` | `/https://entitlements.com/entitlements/v1` | Entitlements API endpoint | no | output of infrastructure deployment |
| `GCLOUD_PROJECT` | ex `osdu-cicd-epam` | Google cloud project id | no | -- |
| `GOOGLE_AUDIENCES` | ex `*****.apps.googleusercontent.com` | Client ID for getting access to cloud resources | yes | https://console.cloud.google.com/apis/credentials |
| `GOOGLE_APPLICATION_CREDENTIALS` | ex `/path/to/directory/service-key.json` | Service account credentials, you only need this if running locally | yes | https://console.cloud.google.com/iam-admin/serviceaccounts |
| `RECORDS_ROOT_URL` | ex `https://os-storage-dot-nice-etching-277309.uc.r.appspot.com/api/storage/v2` / Storage API endpoint | no | output of infrastructure deployment |
| `GCP_STORAGE_STAGING_AREA` | ex `staging-area` | staging area bucket(will be concatenated with project id ex `osdu-cicd-epam-staging-area`) |no | output of infrastructure deployment |
| `GCP_STORAGE_PERSISTENT_AREA` | ex `persistent-area` | persistent area bucket(will be concatenated with project id ex `osdu-cicd-epam-persistent-area` | no | output of infrastructure deployment |
| `PARTITION_API` | ex `http://localhost:8081/api/partition/v1` | Partition service endpoint | no | - |
| `GCP_STATUS_CHANGED_MESSAGING_ENABLED` | `true` or `false` | If set `true`then status messages will be published to specified topic, otherwise stub publisher will write messages to logs| no | - |
| `GCP_STATUS_CHANGED_TOPIC` | ex `status-changed` | Pubsub topic for status publishing | no | output of infrastructure deployment |
| `GCP_FILE_LOCATION_KIND` | by default `file-locations-osm` | Kind for Datastore or Table for postgres  | no | - |
| `SPRING_PROFILES_ACTIVE` | `gcp` or `anthos` | Spring profile to simplify mappers configuration  | no | - |

#### For Mappers, to activate drivers

| name      | value     | description                                             |
|-----------|-----------|---------------------------------------------------------|
| OSMDRIVER | datastore | to activate **OSM** driver for **Google Datastore**     |
| OSMDRIVER | postgres  | to activate **OSM** driver for **PostgreSQL**           |
| OBMDRIVER | gcs       | to activate **OBM** driver for **Google Cloud Storage** |
| OBMDRIVER | minio     | to activate **OBM** driver for **MinIO**                |
| OQMDRIVER | pubsub    | to activate **OQM** driver for **Google PubSub**        |
| OQMDRIVER | rabbitmq  | to activate **OQM** driver for **Rabbit MQ**            |

## Configuring mappers' Datasources

When using non-Google-Cloud-native technologies, property sets must be defined on the Partition
service as part of PartitionInfo for each Tenant.

They are specific to each storage technology:

#### for OSM - Postgres:

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

And this is how **Postges** OSM driver does. Notice, the above hierarchy is kept, but Postgres uses
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

So this is a responsibility of DevOps / CICD to provision all required SQL tables (for all required
data kinds) when on new environment or tenant provisioning when using Postgres. Detailed
instructions (with examples) for creating new tables is in the **OSM module Postgres driver
README.md** `org/opengroup/osdu/core/gcp/osm/translate/postgresql/README.md`

As a quick shortcut, this example snippet can be used by DevOps DBA:

```postgres-psql
--CREATE SCHEMA "exampleschema";
CREATE TABLE exampleschema."ExampleKind"(
    id text COLLATE pg_catalog."default" NOT NULL,
    pk bigint NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    data jsonb NOT NULL,
    CONSTRAINT ExampleKind_id UNIQUE (id)
);
CREATE INDEX ExampleKind_datagin ON exampleschema."ExampleKind" USING GIN (data);
```

**prefix:** `osm.postgres`
It can be overridden by:

- through the Spring Boot property `osm.postgres.partitionPropertiesPrefix`
- environment variable `OSM_POSTGRES_PARTITIONPROPERTIESPREFIX`

**Propertyset:**

| Property | Description |
| --- | --- |
| osm.postgres.datasource.url | server URL |
| osm.postgres.datasource.username | username |
| osm.postgres.datasource.password | password |

<details><summary>Example of a definition for a single tenant</summary>

```

curl -L -X PATCH 'https://dev.osdu.club/api/partition/v1/partitions/opendes' -H 'data-partition-id: opendes' -H 'Authorization: Bearer ...' -H 'Content-Type: application/json' --data-raw '{
  "properties": {
    "osm.postgres.datasource.url": {
      "sensitive": false,
      "value": "jdbc:postgresql://35.239.205.90:5432/postgres"
    },
    "osm.postgres.datasource.username": {
      "sensitive": false,
      "value": "osm_poc"
    },
    "osm.postgres.datasource.password": {
      "sensitive": true,
      "value": "osm_poc"
    }
  }
}'

```

</details>

#### for OBM - MinIO:

**prefix:** `obm.minio`
It can be overridden by:

- through the Spring Boot property `osm.postgres.partitionPropertiesPrefix`
- environment variable `OBM_MINIO_PARTITIONPROPERTIESPREFIX`

**Propertyset:**

| Property            | Description            |
|---------------------|------------------------|
| obm.minio.endpoint  | server URL             |
| obm.minio.accessKey | credentials access key |
| obm.minio.secretKey | credentials secret key |

<details><summary>Example of a definition for a single tenant</summary>

```

curl -L -X PATCH 'https://dev.osdu.club/api/partition/v1/partitions/opendes' -H 'data-partition-id: opendes' -H 'Authorization: Bearer ...' -H 'Content-Type: application/json' --data-raw '{
  "properties": {
    "obm.minio.endpoint": {
      "sensitive": false,
      "value": "http://localhost:9000"
    },
    "obm.minio.accessKey": {
      "sensitive": false,
      "value": "QU2D8DWD3RT7XUPSCCXH"
    },
    "obm.minio.secretKey": {
      "sensitive": true,
      "value": "9sJd5v23Ywr6lEflQjxtmaKoITsVBOdKYMQ2XSoK"
    }
  }
}'

```

</details>

#### for OQM - RabbitMQ:

**prefix:** `oqm.rabbitmq`
It can be overridden by:

- through the Spring Boot property `oqm.rabbitmq.partitionPropertiesPrefix`
- environment variable `OQM_RABBITMQ_PARTITIONPROPERTIESPREFIX`

**Propertyset** (for two types of connection: messaging and admin operations):

| Property | Description |
| --- | --- |
| oqm.rabbitmq.amqp.host | messaging hostnameorIP |
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
      "sensitive": false,
      "value": "guest"
    },
    "oqm.rabbitmq.amqp.password": {
      "sensitive": true,
      "value": "guest"
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
      "value": "guest"
    }
  }
}'

```

</details>

**Cloud roles configuration for accounts**

The GCP Identity and Access Management service account for the File service must have the
`iam.serviceAccounts.signBlob` permission.

The predefined **Cloud Functions Service Agent**, **Cloud Run Service Agent**, and **Service Account
Token Creator** roles include the required permission.

For development purposes, it's recommended to create a separate service account. It's enough to
grant the **Service Account Token Creator** role to the development service account.

Obtaining user credentials for Application Default Credentials isn't suitable in this case because
signing a blob is only available with the service account credentials. Remember to set the
`GOOGLE_APPLICATION_CREDENTIALS` environment variable.

**Required to run integration tests**

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `FILE_SERVICE_HOST` | `http://localhost:8080` | File service url | no | - |
| `ACL_OWNERS` | `data.default.owners` | Acl owners group prefix | no | - |
| `ACL_VIEWERS` | `data.default.viewers` | Acl viewers group prefix | no | - |
| `DOMAIN` | `osdu-gcp.go3-nrg.projects.epam.com` | - | no | - |
| `TENANT_NAME` | `opendes` | Tenant name | no | - |
| `SHARED_TENANT` | `opendes` | Shared tenant id | no | - |
| `PRIVATE_TENANT1` | `opendes` | Private tenant id | no | - |
| `PRIVATE_TENANT2` | `opendes` | Private tenant id | no | - |
| `INTEGRATION_TEST_AUDIENCE` | `*.apps.googleusercontent.com` | Client Id for `$INTEGRATION_TESTER` | no | - |
| `INTEGRATION_TESTER` | `ewogICJ***` | Service account for API calls as Base64 encoded string| yes | - |
| `LEGAL_TAG` | `opendes-storage-tag` | Valid legal tag name| - | - |

**Entitlements configuration for integration accounts**

| INTEGRATION_TESTER |
| ---  |
| users<br/>service.file.editors<br/>service.file.viewers |

### Configure Maven

Check that maven is installed:

```bash
$ mvn --version
Apache Maven 3.6.0
Maven home: /usr/share/maven
Java version: 1.8.0_212, vendor: AdoptOpenJDK, runtime: /usr/lib/jvm/jdk8u212-b04/jre
...
```

You may need to configure access to the remote maven repository that holds the OSDU dependencies.
This file should live within `~/.mvn/community-maven.settings.xml`:

```bash
$ cat ~/.m2/settings.xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>community-maven-via-private-token</id>
            <!-- Treat this auth token like a password. Do not share it with anyone, including Microsoft support. -->
            <!-- The generated token expires on or before 11/14/2019 -->
             <configuration>
              <httpHeaders>
                  <property>
                      <name>Private-Token</name>
                      <value>${env.COMMUNITY_MAVEN_TOKEN}</value>
                  </property>
              </httpHeaders>
             </configuration>
        </server>
    </servers>
</settings>
```

#### Datastore

The service account for File service must have the `datastore.indexes.*` permissions. The
predefined **roles/datastore.indexAdmin** and **roles/datastore.owner** roles include the required
permission.

### Build and run the application

* Update the Google cloud SDK to the latest version:

```bash
gcloud components update
```

* Set Google Project Id:

```bash
gcloud config set project <YOUR-PROJECT-ID>
```

* Perform a basic authentication in the selected project:

```bash
gcloud auth application-default login
```

* Navigate to File Service root folder and run:

```bash
mvn clean install
```

* If you wish to see the coverage report then go to target\site\jacoco\index.html and open
  index.html

* If you wish to build the project without running tests

```bash
mvn clean install -DskipTests
```

After configuring your environment as specified above, you can follow these steps to build and run
the application. These steps should be invoked from the *repository root.*

```bash
cd provider/file-gcp-datastore/ && mvn spring-boot:run
```

### Test the application

After the service has started it should be accessible via a web browser by
visiting [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html). If the
request does not fail, you can then run the integration tests.

```bash
# build + install integration test core
$ (cd testing/file-test-core/ && mvn clean install)

# build + run GCP integration tests.
#
# Note: this assumes that the environment variables for integration tests as outlined
#       above are already exported in your environment.
$ (cd testing/file-test-gcp/ && mvn clean test)
```

## Deployment

Storage Service is compatible with Cloud Run.

* To deploy into Cloud run, please, use this documentation:
  https://cloud.google.com/run/docs/quickstarts/build-and-deploy

## License

Copyright © Google LLC

Copyright © EPAM Systems

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
compliance with the License. You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software distributed under the License is
distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing permissions and limitations under the
License.
