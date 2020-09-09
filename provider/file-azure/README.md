# file-azure

file-azure is a [Spring Boot](https://spring.io/projects/spring-boot) service that  provides internal and external API endpoints to let the application or user fetch any records from the system or request file location data.  
For example, users can request generation of an individual signed URL per file. Using a signed URL, OSDU R2 users will be able to upload their files to the system.

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
| `server.servlet.contextPath` | `/api/file/v2/` | Servlet context path | no | - |
| `AZURE_CLIENT_ID` | `********` | Identity to run the service locally. This enables access to Azure resources. You only need this if running locally | yes | keyvault secret: `$KEYVAULT_URI/secrets/app-dev-sp-username` |
| `AZURE_TENANT_ID` | `********` | AD tenant to authenticate users from | yes | -- |
| `AZURE_CLIENT_SECRET` | `********` | Secret for `$AZURE_CLIENT_ID` | yes | keyvault secret: `$KEYVAULT_URI/secrets/app-dev-sp-password` |
| `KEYVAULT_URI` | ex `https://foo-keyvault.vault.azure.net/` | URI of KeyVault that holds application secrets | no | output of infrastructure deployment |
| `appinsights_key` | `********` | API Key for App Insights | yes | output of infrastructure deployment |
| `cosmosdb_database` | ex `foo-db` | The name of the CosmosDB database | no | output of infrastructure deployment |
| `cosmosdb_key` | `********` | Key for CosmosDB | yes | output of infrastructure deployments |
| `cosmosdb_account` | ex `devintosdur2cosmosacct` | Cosmos account name | no | output of infrastructure deployment |
| `SEARCH_HOST` | `https://search.azurewebsites.net/api/search/v2/query` | API endpoint for the search query endpoint | no | output of infrastructure deployment + path to the query endpoint |
| `AZURE_AD_APP_RESOURCE_ID` | `********` | AAD client application ID | yes | output of infrastructure deployment |
| `osdu_entitlements_url` | ex `https://foo-entitlements.azurewebsites.net` | Entitlements API endpoint | no | output of infrastructure deployment |
| `osdu_entitlements_app_key` | `********` | The API key clients will need to use when calling the entitlements | yes | -- |
| `spring.application.name` | `entitlements-azure` | Name of application. Needed by App Insights | no | -- |
| `osdu_storage_url` | `https://storage.azurewebsites.net/api/storage/v2` | Storage API endpoint | no | -- |

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
| `azure.storage.account-name` | ex `foo-storage-account` | Storage account for storing documents | no | output of infrastructure deployment |
| `USER_ID` | osdu-user | User ID | no | - |
| `EXIST_FILE_ID` | ex '****' | Existing file Id should be added  | no | - |



### Configure Maven

Check that maven is installed:
```bash
$ mvn --version
Apache Maven 3.6.0
Maven home: /usr/share/maven
Java version: 1.8.0_212, vendor: AdoptOpenJDK, runtime: /usr/lib/jvm/jdk8u212-b04/jre
...
```

You may need to configure access to the remote maven repository that holds the OSDU dependencies. A default file should live within `~/.m2/settings.xml`:
```bash
$ cat ~/.m2/settings.xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>file-core</id>
            <username>mvn-pat</username>
            <!-- Treat this auth token like a password. Do not share it with anyone, including Microsoft support. -->
            <!-- The generated token expires on or before 11/14/2019 -->
            <password>$PERSONAL_ACCESS_TOKEN_GOES_HERE</password>
        </server>
    </servers>
</settings>
```

_A settings file is also conveniently located in ./.mvn/community-maven.settings.xml which is also used for CI/CD processes._

### Build, Run and Test the application Locally

After configuring your environment as specified above, you can follow these steps to build and run the application

```bash
# execute build + unit tests
$ mvn clean package
...
[INFO] BUILD SUCCESS

# run service locally **REQUIRES SPECIFIC ENVIRONMENT VARIABLES SET**
$ java -jar $(find ./target/ -name '*.jar')

# Test the application  **REQUIRES SPECIFIC ENVIRONMENT VARIABLES SET**
$ mvn clean test -f integration-tests/pom.xml
```

_After the service has started it should be accessible via a web browser by visiting [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html). If the request does not fail, you can then run the integration tests._

## Debugging

Jet Brains - the authors of Intellij IDEA, have written an [excellent guide](https://www.jetbrains.com/help/idea/debugging-your-first-java-application.html) on how to debug java programs.


## Deploying the Service

Service deployments into Azure are standardized to make the process the same for all services if using ADO and are closely related to the infrastructure deployed. The steps to deploy into Azure can be [found here](https://github.com/azure/osdu-infrastructure)

The default ADO pipeline is /devops/azure-pipelines.yml

### Manual Deployment Steps

__Environment Settings__

The following environment variables are necessary to properly deploy a service to an Azure OSDU Environment.

```bash
# Group Level Variables
export AZURE_TENANT_ID=""
export AZURE_SUBSCRIPTION_ID=""
export AZURE_SUBSCRIPTION_NAME=""
export AZURE_PRINCIPAL_ID=""
export AZURE_PRINCIPAL_SECRET=""
export AZURE_APP_ID=""
export AZURE_NO_ACCESS_ID=""
export AZURE_NO_ACCESS_SECRET=""
export AZURE_OTHER_APP_ID=""
export AZURE_BASENAME_21=""
export AZURE_BASENAME=""
export AZURE_BASE=""
export AZURE_INVALID_JWT=""

# Pipeline Level Variable
export AZURE_SERVICE="file"
export AZURE_BUILD_SUBDIR="."
export AZURE_TEST_SUBDIR="integration-tests"
export AZURE_OSDU_TENANT="opendes"
export AZURE_COMPANY_DOMAIN="contoso.com"
export AZURE_VALID_GROUPNAME="integ.test.data.creator"
export AZURE_INVALID_GROUPNAME="InvalidTestAdmin"

# Required for Azure Deployment
export AZURE_CLIENT_ID="${AZURE_PRINCIPAL_ID}"
export AZURE_CLIENT_SECRET="${AZURE_PRINCIPAL_SECRET}"
export AZURE_RESOURCE_GROUP="${AZURE_BASENAME}-osdu-r2-app-rg"
export AZURE_APPSERVICE_PLAN="${AZURE_BASENAME}-osdu-r2-sp"
export AZURE_APPSERVICE_NAME="${AZURE_BASENAME_21}-au-${AZURE_SERVICE}"

# Required for Testing
export ENTITLEMENT_URL="https://${AZURE_BASENAME_21}-au-file.azurewebsites.net/"
export AZURE_AD_TENANT_ID="${AZURE_TENANT_ID}"
export INTEGRATION_TESTER="${AZURE_PRINCIPAL_ID}"
export ENTITLEMENT_MEMBER_NAME_VALID="${AZURE_PRINCIPAL_ID}"
export AZURE_TESTER_SERVICEPRINCIPAL_SECRET="${AZURE_PRINCIPAL_SECRET}"
export AZURE_AD_APP_RESOURCE_ID="${AZURE_APP_ID}"
export AZURE_AD_OTHER_APP_RESOURCE_ID="${AZURE_OTHER_APP_ID}"
export EXPIRED_TOKEN="${AZURE_INVALID_JWT}"
export DOMAIN="${AZURE_COMPANY_DOMAIN}"
export MY_TENANT="${AZURE_OSDU_TENANT}"
export ENTITLEMENT_GROUP_NAME_VALID="${AZURE_VALID_GROUPNAME}"
export ENTITLEMENT_MEMBER_NAME_INVALID="${AZURE_INVALID_GROUPNAME}"
export AZURE_AD_USER_EMAIL="${AZURE_AD_USER_EMAIL}"
export AZURE_AD_USER_OID="${AZURE_AD_USER_OID}"
export AZURE_AD_GUEST_EMAIL="${AZURE_AD_GUEST_EMAIL}"
export AZURE_AD_GUEST_OID="${AZURE_AD_GUEST_OID}"
export AZURE_AD_OTHER_APP_RESOURCE_ID="${AZURE_AD_OTHER_APP_RESOURCE_ID}"
export AZURE_AD_OTHER_APP_RESOURCE_OID="${AZURE_AD_OTHER_APP_RESOURCE_OID}"
export AZURE_INVALID_EMAIL="invalid.test@email.com"
export AZURE_INVALID_APP_ID="03015fad-093c-424a-a7c4-42ed9993f9e3"
export AZURE_INVALID_ID="03012fadBADX424a-a7c4-42ed9993f9e3"
```

__Azure Service Deployment__


1. Deploy the service using the Maven Plugin  _(azure_deploy)_

```bash
cd $AZURE_BUILD_SUBDIR
mvn azure-webapp:deploy \
  -DAZURE_DEPLOY_TENANT=$AZURE_TENANT_ID \
  -Dazure.appservice.subscription=$AZURE_SUBSCRIPTION_ID \
  -DAZURE_DEPLOY_CLIENT_ID=$AZURE_CLIENT_ID \
  -DAZURE_DEPLOY_CLIENT_SECRET=$AZURE_CLIENT_SECRET \
  -Dazure.appservice.resourcegroup=$AZURE_RESOURCE_GROUP \
  -Dazure.appservice.plan=$AZURE_APPSERVICE_PLAN \
  -Dazure.appservice.appname=$AZURE_APPSERVICE_NAME
```

2. Configure the Web App to start the SpringBoot Application _(azure_config)_


```bash
az login --service-principal -u $AZURE_CLIENT_ID -p $AZURE_CLIENT_SECRET --tenant $AZURE_TENANT_ID

# Set the JAR FILE as required
TARGET=$(find ./target/ -name '*.jar')
JAR_FILE=${TARGET##*/}

JAVA_COMMAND="java -jar /home/site/wwwroot/${JAR_FILE}"
JSON_TEMPLATE='{"appCommandLine":"%s"}'
JSON_FILE="config.json"
echo $(printf "$JSON_TEMPLATE" "$JAVA_COMMAND") > $JSON_FILE

az webapp config set --resource-group $AZURE_RESOURCE_GROUP --name $AZURE_APPSERVICE_NAME --generic-configurations @$JSON_FILE
```

3. Execute the Integration Tests against the Service Deployment _(azure_test)_


```bash
mvn clean test -f $AZURE_TEST_SUBDIR/pom.xml


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
