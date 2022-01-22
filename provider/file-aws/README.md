# OSDU File Service

## Running Locally

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Pre-requisistes

* JDK 8 (https://docs.aws.amazon.com/corretto/latest/corretto-8-ug/downloads-list.html)
* Maven 3.8.3 or later
* Lombok 1.16 or later
* OSDU Instance deployed on AWS

### Service Configuration
**In order to run the service locally or remotely, you will need to have the following environment variables defined.**
The environment variables need to be tied to the `FileApplicationAWS.java` file within the `file-aws` directory tree.

| name | example value | required | description | sensitive? |
| ---  | ---   | ---         | ---        | ---    |
| `AWS_REGION` | `us-east-1` | yes | The region where resources needed by the service are deployed | no |
| `AWS_ACCESS_KEY_ID` | `ASIAXXXXXXXXXXXXXX` | yes | The AWS Access Key for a user with access to Backend Resources required by the service | yes |
| `AWS_SECRET_ACCESS_KEY` | `super-secret-key==` | yes | The AWS Secret Key for a user with access to Backend Resources required by the service | yes |
| `AWS_SESSION_TOKEN` | `session-token-xxxxxxxxxx` | no | AWS Session token needed if using an SSO user session to authenticate | yes |
| `ENVIRONMENT` | `osdu-prefix` | yes | The Resource Prefix defined during deployment | no |
| `APPLICATION_PORT`  | `8080`   | yes         | HTTP Port the application uses to listen for incoming requests        | no    |
| `JAVA_OPTS`  | `-Xms538M -Xmx900M`   | no         |         | no    |
| `LOG_LEVEL`  | `DEBUG`   | no         | Defines the level of logging information outputted to the log file        | no    |
| `SSM_ENABLED`  | `false`   | no         |         | no    |
| `SSL_ENABLED`  | `false`   | no         |        | no    |
| `CACHE_CLUSTER_ENDPOINT`  | `some.cache.cluster.endpoint.url`   | yes         |         | yes    |
| `CACHE_CLUSTER_PORT`  | `6469`   | yes         |         | no    |
| `ENTITLEMENTS_BASE_URL`  | `https://{prefix}.hosted.url` or `http://localhost:{port number}`   | yes         |         | no    |
| `PARTITION_BASE_URL`  | `https://{prefix}.hosted.url` or `http://localhost:{port number}`   | yes         |         | no    |
| `STORAGE_BASE_URL`  | `https://{prefix}.hosted.url` or `http://localhost:{port number}`   | yes         |         | no    |
| `SEARCH_HOST`  | `https://{prefix}.hosted.url` or `http://localhost:{port number}`   | yes         |         | no    |
| `DATASET_BASE_URL`  | `https://{prefix}.hosted.url` or `http://localhost:{port number}`   | yes         |         | no    |
| `CACHE_CLUSTER_KEY`  | `SecretClusterKeyValue`   | yes         |         | yes    |
| `AWS_ROLE_ARN`  | `IAM service role`   | yes         |         | yes    |
| `AWS_WEB_IDENTITY_TOKEN_FILE`  | `/path/to/secret/token/file`   | yes         |         | yes    |

### Run Locally
Check that maven is installed:

example:
```shell
> mvn --version
Apache Maven 3.8.4 (9b656c72d54e5bacbed989b64718c159fe39b537)
Maven home: C:\Users\estepamo\AppData\Roaming\maven\apache-maven-3.8.4
Java version: 1.8.0_302, vendor: Amazon.com Inc., runtime: C:\Program Files\Amazon Corretto\jdk1.8.0_302\jre
Default locale: en_US, platform encoding: Cp1252
OS name: "windows 10", version: "10.0", arch: "amd64", family: "windows"
...
```

You may need to configure access to the remote maven repository that holds the OSDU dependencies. Copy one of the below files' content to your .m2 folder
* For development against the OSDU GitLab environment, leverage: `<REPO_ROOT>~/.mvn/community-maven.settings.xml`
* For development in an AWS Environment, leverage: `<REPO_ROOT>/provider/file-aws/maven/settings.xml`

**Important**: Copying the xml files above to your .m2 folder may cause some weird behavior with your other projects if you have multiple instances of Intellij open. The odd behavior manifests as the IDE/compiler being unable to resolve common libraries and dependencies.

* Navigate to the service's root folder and run:

```bash
mvn clean package -pl file-core,provider/file-aws
```

* If you wish to build the project without running tests

```bash
mvn clean package -pl file-core,provider/file-aws -DskipTests
```

After configuring your environment as specified above, you can follow these steps to run the application. These steps should be invoked from the *repository root.*
<br/>
<br/>
NOTE: If not on osx/linux: Replace `*` with version numbers as defined in the provider/file-aws/pom.xml file

```bash
java -jar provider/file-aws/target/file-aws-*.*.*-SNAPSHOT.jar
```

## Testing

### Running Integration Tests
This section describes how to run OSDU Integration tests (testing/file-test-aws).

You will need to have the following environment variables defined.

| name | example value                                  | description | sensitive? 
 |------------------------------------------------| ---   |------------| ---        |
| `AWS_ACCESS_KEY_ID` | `ASIAXXXXXXXXXXXXXX`                           | The AWS Access Key for a user with access to Backend Resources required by the service | yes        |
| `AWS_SECRET_ACCESS_KEY` | `super-secret-key==`                           | The AWS Secret Key for a user with access to Backend Resources required by the service | yes        |
| `AWS_SESSION_TOKEN` | `session-token-xxxxxxxxx`                      | AWS Session token needed if using an SSO user session to authenticate | yes        |
| `ADMIN_USER`  | `user@name`                                    |          | yes        |
| `SERVICE_PRINCIPAL_USER`  | `serviceprincipal@user`                        |          | yes        |
| `AWS_COGNITO_AUTH_FLOW` | `USER_PASSWORD_AUTH`                           | Auth flow used by reference cognito deployment | no         |
| `AWS_COGNITO_CLIENT_ID` | `xxxxxxxxxxxx`                                 | Client ID for the Auth Flow integrated with the Cognito User Pool | no         |
| `AWS_COGNITO_USER_POOL_ID` | `us-east-1_xxxxxxxx`                           | User Pool Id for the reference cognito | no         |
| `AWS_COGNITO_AUTH_PARAMS_USER` | `int-test-user@testing.com`                    | Int Test Username | no         |
| `AWS_COGNITO_AUTH_PARAMS_PASSWORD` | `some-secure-password`                         | Int Test User/NoAccessUser Password | yes        |
| `AWS_BASE_URL`  | `https://some.hosted.url`                      |          | no         |
| `ELASTIC_HOST`  | `elastic.host.url`                             |          | no         |
| `ELASTIC_PORT`  | `9200`                                         |          | no         |
| `ELASTIC_USERNAME`  | `username`                                     |          | no         |
| `ELASTIC_PASSWORD`  | `super-secret-password`                        |          | yes        |
| `FILE_URL`  | `https://some.hosted.url/api/file/v2`          |          | no         |
| `INDEXER_URL`  | `https://some.hosted.url/api/indexer/v2/`      |          | no         |
| `INDEXER_HOST`  | `https://some.hosted.url/api/indexer/v2/`      |          | no         |
| `LEGAL_QUEUE`  | `https://sqs.legal.queue.url/123456/endpoint`  |          | no         |
| `LEGAL_S3_BUCKET`  | `s3-bucket-name`                               |          | no         |
| `LEGAL_URL`  | `https://some.hosted.url/api/legal/v1/`        |          | no         |
| `RESOURCE_PREFIX`  | `osdu-prefix`                                  |          | no         |
| `SEARCH_URL`  | `https://some.hosted.url/api/search/v2/`       |          | no         |
| `STORAGE_URL`  | `https://some.hosted.url/api/storage/v2/`      |          | no         |
| `ENTITLEMENTS_URL`  | `https://some.hosted.url/api/entitlements/v2/` | ---         | no         |
| `SCHEMA_URL`  | `https://some.hosted.url`                      | ---         | no         |
| `UNIT_HOST`  | `some.hosted.url`                              | ---         | no         |
| `DATA_WORKFLOW_URL`  | `https://some.hosted.url/api/data-workflow/v1` | ---         | no         |
| `WORKFLOW_URL`  | `https://some.hosted.url/api/workflow/`        | ---         | no         |
| `PARTITION_BASE_URL`  | `https://some.hosted.url/`                     | ---         | no         |
| `CRS_CATALOG_HOST`  | `some.hosted.url`                                          | ---         | no         |
| `CRS_CONVERTER_HOST`  | `some.hosted.url`                                          | ---         | no         |
| `REGISTER_BASE_URL`  | `https://some.hosted.url/`                                          | ---         | no         |
| `DATASET_BASE_URL`  | `https://some.hosted.url/api/dataset/v1/`                                          | ---         | no         |
| `FILEDMS_BASE_URL`  | `https://some.hosted.url/api/dms/file/v1/`  | ---         | no         |
| `NOTIFICATION_BASE_URL`  | `https://some.hosted.url/api/notification/v1/`  | ---         | no         |
| `NOTIFICATION_REGISTER_BASE_URL`  | `https://some.hosted.url`  | ---         | no         |
| `SEISMIC_DMS_URL`  | `https://some.hosted.url`  | ---         | no         |
| `WELLBORE_DDMS_URL`  | `https://some.hosted.url`  | ---         | no         |
| `BINARYDMS_URL`  | `https://some.hosted.url`  | ---         | no         |
| `EDSDMS_BASE_URL`  | `https://some.hosted.url/api/dms/eds/v1/`  | ---         | no         |
| `ENTITLEMENT_V2_URL`  | `https://some.hosted.url/api/entitlements/v2/`  | ---         | no         |
| `TIMESERIESDMS_BASE_URL`  | `https://some.hosted.url/api/dms/timeseries/v1/`  | ---         | no         |
| `WELL_DELIVERY_URL`  | `https://some.hosted.url`  | ---         | no         |

* Prior to running tests, scripts must be executed locally to generate pipeline env vars

```bash
testing/file-test-aws/build-aws/prepare-dist.sh

#Set Neccessary ENV Vars here as defined in run-tests.sh

dist/testing/integration/build-aws/run-tests.sh 
```

### Run Tests using mvn
Set required env vars and execute the following:
```
mvn clean package -f testing/pom.xml -pl file-test-core,file-test-aws -DskipTests
mvn test -f testing/file-test-aws/pom.xml
```



## License
Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
