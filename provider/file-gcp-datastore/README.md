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

### Anthos Service Configuration:

[Anthos service configuration ](docs/anthos/README.md)
### GCP Service Configuration:
[Gcp service configuration ](docs/gcp/README.md)

## Testing

#### Anthos:
[Anthos Testing](docs/anthos/README.md)
#### GCP:
[Gcp Testing](docs/gcp/README.md)


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
* To deploy into GKE, please, use this documentation:
  https://cloud.google.com/cloud-build/docs/deploying-builds/deploy-gke

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
