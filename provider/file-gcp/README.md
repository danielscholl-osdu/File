# File Service

The OSDU R2 File service provides internal and external API endpoints to let the application or
user fetch any records from the system or request file location data.  For example, users can
request generation of an individual signed URL per file. Using a signed URL, OSDU R2 users will be
able to upload their files to the system.

The current implementation of the File service supports only cloud platform-specific locations.
The future implementations might allow the use of on-premises locations.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

Pre-requisites

* GCloud SDK with java (latest version)
* JDK 8
* Lombok 1.16 or later
* Maven

### Environment Variables

In order to run the service locally, you will need to have the following environment variables defined.

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `LOG_PREFIX` | `ingest` | Logging prefix | no | - |
| `OSDU_ENTITLEMENTS_URL` | `/https://entitlements.com/entitlements/v1` | Entitlements API endpoint | no | output of infrastructure deployment |
| `FILE_LOCATION_BUCKET-NAME` | ex `osdu-cicd-epam-file` | Bucket name for files | no | - |
| `FILE_LOCATION_USER-ID` | ex `common-user` |  User id which used to define files location in bucket | no | output of infrastructure deployment |
| `GCLOUD_PROJECT` | ex `osdu-cicd-epam` | Google cloud project id | no | -- |
| `GOOGLE_AUDIENCES` | ex `*****.apps.googleusercontent.com` | Client ID for getting access to cloud resources | yes | https://console.cloud.google.com/apis/credentials |
| `GOOGLE_APPLICATION_CREDENTIALS` | ex `/path/to/directory/service-key.json` | Service account credentials, you only need this if running locally | yes | https://console.cloud.google.com/iam-admin/serviceaccounts |
| `RECORDS_ROOT_URL` | ex `https://os-storage-dot-nice-etching-277309.uc.r.appspot.com/api/storage/v2` / Storage API endpoint | no | output of infrastructure deployment |
| `PARTITION_API` | ex `http://localhost:8081/api/partition/v1` | Partition service endpoint | no | - |

**Cloud roles configuration for accounts**

The GCP Identity and Access Management service account for the File service must have the
`iam.serviceAccounts.signBlob` permission.

The predefined **Cloud Functions Service Agent**, **Cloud Run Service Agent**, and **Service Account
Token Creator** roles include the required permission.

For development purposes, it's recommended to create a separate service account.
It's enough to grant the **Service Account Token Creator** role to the development service account.

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

#### Firestore collections

The GCP implementation of the File service uses Cloud Firestore with the following collections
and indexes.

##### `file-locations` collection

| Field     | Type     | Description                                                               |
| --------- | -------- | ------------------------------------------------------------------------- |
| FileID    | `Object` | Unique file ID that references a file data object with Driver, Location, CreatedAt, and CreatedBy |
| Driver    | `String` | Description of the storage where files were loaded                        |
| Location  | `String` | Direct URI to the file in storage                                         |
| CreatedAt | `String` | Time when the record was created                                          |
| CreatedBy | `String` | ID of the user that requested file location                               |

> **Note**: The `Location` value might be different from the signed URL returned to the user.
> **Note**: The `CreatedBy` property isn't supported in the OSDU R2 Prototype.

#### Indexes

##### Single Field

| Collection ID  | Field path | Collection scope | Collection group scope |
| -------------- | ---------- | ---------------- | ---------------------- |
| file-locations | FileID     | _no changes_     | _no changes_           |

##### Composite

| Collection ID  | Fields                             | Query scope |
| -------------- | ---------------------------------- | ----------- |
| file-locations | `CreatedBy: ASC`, `CreatedAt: ASC` | Collection  |

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

* If you wish to see the coverage report then go to target\site\jacoco\index.html and open index.html

* If you wish to build the project without running tests

```bash
mvn clean install -DskipTests
```

After configuring your environment as specified above, you can follow these steps to build and run the application. These steps should be invoked from the *repository root.*
```bash
cd provider/file-gcp/ && mvn spring-boot:run
```

### Test the application

After the service has started it should be accessible via a web browser by visiting [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html). If the request does not fail, you can then run the integration tests.

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

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
