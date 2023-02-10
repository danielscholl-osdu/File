# Service Configuration for Google Cloud

## Environment variables

### Must have

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `SPRING_PROFILES_ACTIVE` | ex `gcp` | Spring profile that activate default configuration for Google Cloud environment | false | - |
| `GOOGLE_APPLICATION_CREDENTIALS` | ex `/path/to/directory/service-key.json` | Service account credentials, you only need this if running locally | yes | https://console.cloud.google.com/iam-admin/serviceaccounts |

### Common-properties-for-all-environments

Define the following environment variables.
Most of them are common to all hosting environments, but there are properties that are only necessary when running in Google Cloud.

In order to run the service locally, you will need to have the following environment variables defined.

**Required to run application**

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `LOG_PREFIX` | `file` | Logging prefix | no | - |
| `ENTITLEMENTS_HOST` | `http://entitlements` | Entitlements service host address | no | output of infrastructure deployment |
| `STORAGE_HOST` | ex `http://storage` / Storage service host address | no | output of infrastructure deployment |
| `GCP_STORAGE_STAGING_AREA` | ex `staging-area` | staging area bucket(will be concatenated with project id ex `osdu-cicd-epam-staging-area`) |no | output of infrastructure deployment |
| `GCP_STORAGE_PERSISTENT_AREA` | ex `persistent-area` | persistent area bucket(will be concatenated with project id ex `osdu-cicd-epam-persistent-area` | no | output of infrastructure deployment |
| `PARTITION_HOST` | ex `http://partition` | Partition service host address | no | - |
| `GCP_STATUS_CHANGED_MESSAGING_ENABLED` | `true` or `false` | If set `true`then status messages will be published to specified topic, otherwise stub publisher will write messages to logs| no | - |
| `GCP_STATUS_CHANGED_TOPIC` | ex `status-changed` | PubSub topic for status publishing | no | output of infrastructure deployment |
| `GCP_FILE_LOCATION_KIND` | by default `file-locations-osm` | Kind for Datastore or Table for postgres  | no | - |

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `PARTITION_AUTH_ENABLED` | ex `true` or `false` | Disable or enable auth token provisioning for requests to Partition service | no | - |
| `OQMDRIVER` | `pubsub` | Oqm driver mode that defines which message broker will be used | no | - |
| `OBMDRIVER` | `gcs` | Obm driver mode that defines which object storage will be used | no | - |
| `OSMDRIVER` | `datastore` | Osm driver mode that defines which KV storage will be used | no | - |

## Testing

### Running E2E Tests

**Required to run integration tests**

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `FILE_SERVICE_HOST` | ex `http://localhost:8080` | File service url | no | - |
| `ACL_OWNERS` | `data.default.owners` | Acl owners group prefix | no | - |
| `ACL_VIEWERS` | `data.default.viewers` | Acl viewers group prefix | no | - |
| `DOMAIN` | ex `osdu-gc.go3-nrg.projects.epam.com` | - | no | - |
| `TENANT_NAME` | `opendes` | Tenant name | no | - |
| `SHARED_TENANT` | `opendes` | Shared tenant id | no | - |
| `PRIVATE_TENANT1` | `opendes` | Private tenant id | no | - |
| `PRIVATE_TENANT2` | `opendes` | Private tenant id | no | - |
| `INTEGRATION_TESTER` | ex `ewogICJ***` | Service account for API calls as Base64 encoded string| yes | - |
| `LEGAL_TAG` | ex `opendes-storage-tag` | Valid legal tag name| - | - |

**Entitlements configuration for integration accounts**

| INTEGRATION_TESTER |
| ---  |
| users<br/>service.file.editors<br/>service.file.viewers |

## PubSub configuration

At PubSub should be created topic with name:

**name:** `status-changed`

It can be overridden by:

- through the Spring Boot property `gcp.status-changed.topicName`
- environment variable `STATUS_CHANGED_TOPIC_NAME`

## GCS configuration <a name="ObjectStoreConfig"></a>

### Per-tenant buckets configuration
These buckets must be defined in tenants’ “data” Google Cloud projects that names are pointed in tenants’ PartitionInfo registration objects’ “projectId” property at the Partition service.

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
   <td>ListObjects, CRUDObject, SignedURLs, DownscopedCreds
   </td>
  </tr>
  <tr>
   <td>&lt;PartitionInfo.projectId-PartitionInfo.name>-$GCP_STORAGE_PERSISTENT_AREA:<strong>persistent-area</strong>
   </td>
   <td>ListObjects, CRUDObject, SignedURLs, DownscopedCreds
   </td>
  </tr>
</table>

## Google cloud service account configuration
The Google Cloud Identity and Access Management service account for the File service must have the
`iam.serviceAccounts.signBlob` permission.

| Required roles |
| ---    |
| Cloud Functions Service Agent |
| Cloud Run Service Agent |
| Service Account Token Creator |

For development purposes, it's recommended to create a separate service account. It's enough to
grant the **Service Account Token Creator** role to the development service account.

Obtaining user credentials for Application Default Credentials isn't suitable in this case because
signing a blob is only available with the service account credentials. Remember to set the
`GOOGLE_APPLICATION_CREDENTIALS` environment variable.
