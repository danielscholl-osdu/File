# Reference File Service

The Reference file service provides internal and external API endpoints to let the application or
user fetch any records from the system or request file location data. For example, users can request
generation of an individual signed URL per file. Using a signed URL, OSDU R2 users will be able to
upload their files to the system.

The current implementation of the File service allow the use of on-premises locations.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for
development and testing purposes. See deployment for notes on how to deploy the project on a live
system.

### Prerequisites

Pre-requisites

* JDK 8
* Lombok 1.16 or later
* Maven

### Environment Variables

In order to run the service locally, you will need to have the following environment variables defined.

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `server.servlet.contextPath` | ex `/api/file/` | File service context path | no | - |
| `LOG_PREFIX` | `ingest` | Logging prefix | no | - |
| `OSDU_ENTITLEMENTS_URL` | `/https://entitlements.com/entitlements/v1` | Entitlements API endpoint | no | output of infrastructure deployment |
| `GCP_STORAGE_STAGING_AREA` | ex `ex staging-area` | staging area bucket(will be concatenated with project id ex osdu-cicd-epam-staging-area) | no | output of infrastructure deployment |
| `GCP_STORAGE_PERSISTENT_AREA` | ex `ex persistent-area` | persistent area bucket(will be concatenated with project id ex osdu-cicd-epam-persistent-area | no | output of infrastructure deployment |
| `FILE_LOCATION_USER-ID` | ex `common-user` |  User id which used to define files location in bucket | no | output of infrastructure deployment |
| `RECORDS_ROOT_URL` | ex `https://os-storage-dot-nice-etching-277309.uc.r.appspot.com/api/storage/v2` / Storage API endpoint | no | output of infrastructure deployment |
| `PARTITION_API` | ex `http://localhost:8081/api/partition/v1` | Partition service endpoint | no | - |
| `STORAGE_API` | ex `http://localhost:8081/api/storage/v2/` | Storage service endpoint | no | - |
| `RABBITMQ_URI` | ex `amqp://guest:guest@127.0.0.1:5672/%2F` | Rabbit MQ URI | no | - |
| `RABBITMQ_ENABLED` | `true` or `false` | If set `true`then status messages will be published to specified topic, otherwise stub publisher will write messages to logs| no | - |
| `RABBITMQ_TOPIC_NAME` | ex `status-changed` | Pubsub topic for status publishing | no | output of infrastructure deployment |
| `MONGO_DB_HOST` | ex `127.0.0.1` | MongoDB host | no | - |
| `MONGO_DB_PORT` | ex `27017` | MongoDB port | no | - |
| `MONGO_DB_AUTHENTICATION_DATABASE` | ex `admin` | MongoDB auth database name | no | - |
| `MONGO_DB_USERNAME` | ex `admin` | MongoDB username | no | - |
| `MONGO_DB_PASSWORD` | ex `password` | MongoDB password | no | - |
| `MONGO_DB_DATABASE` | ex `database` | MongoDB database | no | - |
| `MINIO_ENDPOINT_URL` | ex `http://127.0.0.1:9000` | MinIO URL | no | - |
| `MINIO_ACCESS_KEY` | ex `root` | MinIO access key | no | - |
| `MINIO_SECRET_KEY` | ex `password` | MinIO secret key | no | - |
| `MINIO_BUCKET_RECORD_NAME` | ex `record-bucket` | MinIO bucket record name | no | - |
| `MINIO_SIGNED_URL_EXPIRATION_DAYS` | ex `1` | MinIO signed URL expiration days | no | - |
| `REFERENCE_STAGING_AREA` | ex `staging-area` | Staging area bucket name | no | - |
| `REFERENCE_PERSISTENT_AREA` | ex `persistent-area` | Persistent area bucket name | no | - |
| `REFERENCE_DATA_PARTITION_ID` | ex `anthos` | Data partition id | no | - |
| `SEARCH_QUERY_RECORD_HOST` | ex `localhost/api/search/v2/query` | Search query record host | no | - |
| `SEARCH_QUERY_LIMIT` | ex `1000` | Search query limit | no | - |
| `SEARCH_BATCH_SIZE` | ex `100` | Search batch size | no | - |

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

You may need to configure access to the remote maven repository that holds the OSDU dependencies. This file should live within `~/.mvn/community-maven.settings.xml`:
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

### Build and run the application

* Navigate to File Service root folder and run:

```bash
mvn clean install
```

* If you wish to see the coverage report then go to target\site\jacoco\index.html and open index.html

* If you wish to build the project without running tests

```bash
mvn clean install -DskipTests
```

After configuring your environment as specified above, you can follow these steps to build and run the application. These steps should be invoked from the *repository root.*
```bash
cd provider/file-reference/ && mvn spring-boot:run
```

## License

Copyright © Google LLC

Copyright © EPAM Systems

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
