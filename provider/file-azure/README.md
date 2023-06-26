# file-azure

[![coverage report](https://community.opengroup.org/osdu/platform/system/file/badges/master/coverage.svg)](https://community.opengroup.org/osdu/platform/system/file/-/commits/master)

file-azure is a [Spring Boot](https://spring.io/projects/spring-boot) service that  provides internal and external API endpoints to let the application or user fetch any records from the system or request file location data.
For example, users can request generation of an individual signed URL per file. Using a signed URL, OSDU R2 users will be able to upload their files to the system.

### DELETE /v2/files/revokeURL

The `/v2/files/revokeURL` API endpoint revokes the Signed URLs based on the request parameters.<br/>
For example:  for the given `storage account`.

- Required permissions: `service.file.admin` role required to perform revoke operation
- Required environment property: `AZURE_SUBSCRIPTION_ID` Azure Subscription ID of the given environment


> **Note**:
>- For the given storage account, all the existing Signed URLs generated via 'UserDelegationKey' method will be revoked.
>- It will not have any impact on the Signed URLs generated using 'Account Key' method. <br/>
>- 'msi_enabled' feature should be enabled and Pod Identity environment variables [AZURE_CLIENT_ID, AZURE_CLIENT_SECRET] should be removed.
>- `Managed Identity` will be used for Authentication. All the required access for resources [CosmosDB Storage account , Event Grid] should be granted to the `Managed Identity`

#### Request body

| Property | Type     | Description                        |
| ------- | -------- |------------------------------------|
| resourceGroup        | `String` | Resource Group name of the Storage account |
| storageAccount        | `String` | Storage acount name |

> **Note**:
>- The Request is a Map of Properties to manage the input parameters required to revoke Signed URLs.
>- Each CSP will have its own implementation and request properties based on the revoke Logic supported by the provider.
>- Example: For `Azure` [ Resource Group and Storage Account Name] is required to revoke URLs

**References**:
- https://learn.microsoft.com/en-us/rest/api/storagerp/storage-accounts/revoke-user-delegation-keys?tabs=HTTP

Request example:

```sh
curl --location --request DELETE 'https://{path}/v2/files/revokeURL' \
     --header 'Authorization: Bearer {token}' \
     --header 'Content-Type: application/json' \
     --data-raw '{
        "resourceGroup": "test-resource-group-name",
        "storageAccount" : "test-storage-account-name"
     }'
```

#### Response
The File service returns the following `HTTP 204 No Content` for successful response.

## Running Locally

### Requirements

In order to run this service locally, you will need the following:

- [Maven 3.6.0+](https://maven.apache.org/download.cgi)
- [AdoptOpenJDK8](https://adoptopenjdk.net/)
- Infrastructure dependencies, deployable through the relevant [infrastructure template](https://dev.azure.com/slb-des-ext-collaboration/open-data-ecosystem/_git/infrastructure-templates?path=%2Finfra&version=GBmaster&_a=contents)
- While not a strict dependency, example commands in this document use [bash](https://www.gnu.org/software/bash/)

### General Tips

**Environment Variable Management**
The following tools make environment variable configuration simpler
 - [direnv](https://direnv.net/) - for a shell/terminal environment
 - [EnvFile](https://plugins.jetbrains.com/plugin/7861-envfile) - for [Intellij IDEA](https://www.jetbrains.com/idea/)

**Lombok**
This project uses [Lombok](https://projectlombok.org/) for code generation. You may need to configure your IDE to take advantage of this tool.
 - [Intellij configuration](https://projectlombok.org/setup/intellij)
 - [VSCode configuration](https://projectlombok.org/setup/vscode)


### Understanding Environment Variables

In order to run the service locally, you will need to have the following environment variables defined.

**Note** The following command can be useful to pull secrets from keyvault:
```bash
az keyvault secret show --vault-name $KEY_VAULT_NAME --name $KEY_VAULT_SECRET_NAME --query value -otsv
```

**Required to run service**

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `LOG_PREFIX` | `file` | Logging prefix | no | - |
| `AZURE_CLIENT_ID` | `********` | Identity to run the service locally. This enables access to Azure resources. You only need this if running locally | yes | keyvault secret: `$KEYVAULT_URI/secrets/app-dev-sp-username` |
| `AZURE_TENANT_ID` | `********` | AD tenant to authenticate users from | yes | -- |
| `AZURE_CLIENT_SECRET` | `********` | Secret for `$AZURE_CLIENT_ID` | yes | keyvault secret: `$KEYVAULT_URI/secrets/app-dev-sp-password` |
| `KEYVAULT_URL` | ex `https://foo-keyvault.vault.azure.net/` | URI of KeyVault that holds application secrets | no | output of infrastructure deployment |
| `appinsights_key` | `********` | API Key for App Insights | yes | output of infrastructure deployment |
| `cosmosdb_database` | ex `foo-db` | The name of the CosmosDB database | no | output of infrastructure deployment |
| `AZURE_AD_APP_RESOURCE_ID` | `********` | AAD client application ID | yes | output of infrastructure deployment |
| `osdu_entitlements_url` | ex `https://foo-osdu.msft-osdu-test.org/entitlements/v1` | Entitlements API endpoint | no | output of infrastructure deployment |
| `osdu_entitlements_app_key` | `OBSOLETE` | This is deprecated | no | -- |
| `spring.application.name` | `file-azure` | Name of application. Needed by App Insights | no | -- |
| `osdu_storage_url` | `https://storage.azurewebsites.net/api/storage/v2` | Storage API endpoint | no | -- |
| `server_port` | ex `8082` | Port the service will run on | no | -- |
| `AZURE_STORAGE_ACCOUNT` | ex `foo-storage-account` | Storage account for storing documents | no | output of infrastructure deployment |
| `storage_account` | ex `foo-storage-account` | Storage account for storing documents | no | output of infrastructure deployment |
| `azure_istioauth_enabled` | `true` (depends on if service is running in Kubernetes environment with Istio installed) | Configuring use of Istio | no | Set to true when deploying the service into a Kubernetes cluster with Istio configured. Set to false and uncomment the three lines [here](https://community.opengroup.org/osdu/platform/system/file/-/blob/master/provider/file-azure/src/main/resources/application.properties) defining `azure.activedirectory.client-id`, `azure.activedirectory.AppIdUri`, and `azure.activedirectory.session-stateless=true` if running locally |

**Required to run integration tests**

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `FILE_SERVICE_HOST` | ex `http://localhost:8080/` | The host where the service is running | no | -- |
| `DATA_PARTITION_ID` | ex `opendes` | OSDU tenant used for testing | no | -- |
| `INTEGRATION_TESTER` | `********` | System identity to assume for API calls. Note: this user must have entitlements configured already | no | -- |
| `TESTER_SERVICEPRINCIPAL_SECRET` | `********` | Secret for `$INTEGRATION_TESTER` | yes | -- |
| `AZURE_AD_TENANT_ID` | `********` | AD tenant to authenticate users from | yes | -- |
| `AZURE_AD_APP_RESOURCE_ID` | `********` | AAD client application ID | yes | output of infrastructure deployment |
| `NO_DATA_ACCESS_TESTER` | `********` | Service principal ID of a service principal without entitlements | yes | `aad-no-data-access-tester-client-id` secret from keyvault |
| `NO_DATA_ACCESS_TESTER_SERVICEPRINCIPAL_SECRET` | `********` | Secret for `$NO_DATA_ACCESS_TESTER` | yes | `aad-no-data-access-tester-secret` secret from keyvault |
| `AZURE_STORAGE_ACCOUNT` | ex `foo-storage-account` | Storage account for storing documents | no | output of infrastructure deployment |
| `USER_ID` | osdu-user | User ID | no | - |
| `EXIST_FILE_ID` | ex '****' | Existing file Id should be added  | no | - |
| `TIME_ZONE` | `UTC+0` | Time zone required for tests to pass  | yes | - |
| `STAGING_CONTAINER_NAME` | `file_staging_area` | Name of staging container for file service | no | created by Terraform |



## Running Locally

### Configure Maven

Check that maven is installed:
```bash
$ mvn --version
Apache Maven 3.6.0
Maven home: /usr/share/maven
Java version: 1.8.0_212, vendor: AdoptOpenJDK, runtime: /usr/lib/jvm/jdk8u212-b04/jre
...
```

### Build and run the application

After configuring your environment as specified above, you can follow these steps to build and run the application. These steps should be invoked from the repository root.

```bash
# build + test + install core service code from repository root
$ (cd file-core && mvn clean install)

# build + test + package azure service code
$ (cd provider/file-azure/ && mvn clean package)

# run service from repository root
#
# Note: this assumes that the environment variables for running the service as outlined above are already exported in your environment.
$ java -jar $(find provider/file-azure/target/ -name '*-spring-boot.jar')

```

### Test the Application

_After the service has started it should be accessible via a web browser by visiting [http://localhost:8080/api/file/v2/swagger](http://localhost:8080/api/file/v2/swagger). If the request does not fail, you can then run the integration tests._

```bash
# build + install integration test core
$ (cd testing/file-test-core/ && mvn clean install)

# build + run Azure integration tests.
#
# Note: this assumes that the environment variables for integration tests as outlined above are already exported in your environment.
$ (cd testing/file-test-azure/ && mvn clean test)
```

## Open API 3.0 - Swagger
- Swagger UI:  http://localhost:8080/api/file/v2/swagger (will redirect to  http://localhost:8080/api/file/v2/swagger-ui/index.html)
- api-docs (JSON) :  http://localhost:8080/api/file/v2/api-docs
- api-docs (YAML) :  http://localhost:8080/api/file/v2/api-docs.yaml

All the Swagger and OpenAPI related common properties are managed here [swagger.properties](../../file-core/src/main/resources/swagger.properties)

## Debugging

Jet Brains - the authors of Intellij IDEA, have written an [excellent guide](https://www.jetbrains.com/help/idea/debugging-your-first-java-application.html) on how to debug java programs.


## License
Copyright © Microsoft Corporation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
