### Running E2E Tests

You will need to have the following environment variables defined.

| name                | value                              | description                   | sensitive? | source |
|---------------------|------------------------------------|-------------------------------|------------|--------|
| `ACL_OWNERS`        | ex `data.default.owners`           | ACL Owner privilege           | no         | -      |
| `ACL_VIEWERS`       | ex `data.default.viewers`          | ACL Viewer privilege          | no         | -      |
| `FILE_SERVICE_HOST` | ex`http://localhost:8080/api/file` | Endpoint of File service host | no         | -      |
| `TENANT_NAME`       | ex `opendes`                       | OSDU tenant used for testing  | no         | --     |
| `SHARED_TENANT`     | ex `opendes`                       | Shared Tenant name            | no         | -      |
| `PRIVATE_TENANT1`   | ex `opendes`                       | Private Tenant                | no         | -      |
| `PRIVATE_TENANT2`   | ex `opendes`                       | Private Tenant 2              | no         | -      |
| `LEGAL_TAG`         | ex `opendes`                       | Legal Tag name                | no         | -      |
| `DOMAIN`            | ex `group`                         | Group ID                      | no         | -      |



Authentication can be provided as OIDC config:

| name                                            | value                                   | description                   | sensitive? | source |
|-------------------------------------------------|-----------------------------------------|-------------------------------|------------|--------|
| `TEST_OPENID_PROVIDER_CLIENT_ID`                | `********`                              | OpenId Provider Client Id     | yes        | -      |
| `TEST_OPENID_PROVIDER_CLIENT_SECRET`            | `********`                              | OpenId Provider Client secret | yes        | -      |
| `TEST_OPENID_PROVIDER_URL`                      | `https://keycloak.com/auth/realms/osdu` | OpenID provider url           | yes        | -      |

Or tokens can be used directly from env variables:

| name                    | value      | description           | sensitive? | source |
|-------------------------|------------|-----------------------|------------|--------|
| `PRIVILEGED_USER_TOKEN` | `********` | PRIVILEGED_USER Token | yes        | -      |


Execute following command to build code and run all the integration tests:

 ```bash
 # Note: this assumes that the environment variables for integration tests as outlined
 #       above are already exported in your environment.
 # build + install integration test core
 $ (cd file-acceptance-test && mvn clean test)
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
