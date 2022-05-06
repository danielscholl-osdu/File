# Service Configuration for GCP

## Environment variables

### Must have

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `SPRING_PROFILES_ACTIVE` | ex `gcp` | Spring profile that activate default configuration for GCP environment | false | - |
| `GOOGLE_APPLICATION_CREDENTIALS` | ex `/path/to/directory/service-key.json` | Service account credentials, you only need this if running locally | yes | https://console.cloud.google.com/iam-admin/serviceaccounts |
| `GOOGLE_AUDIENCES` | ex `*****.apps.googleusercontent.com` | Client ID for getting access to cloud resources | yes | https://console.cloud.google.com/apis/credentials |

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

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `PARTITION_AUTH_ENABLED` | ex `true` or `false` | Disable or enable auth token provisioning for requests to Partition service | no | - |
| `OQMDRIVER` | `pubsub` | Oqm driver mode that defines which message broker will be used | no | - |
| `OBMDRIVER` | `gcs` | Obm driver mode that defines which object storage will be used | no | - |
| `OSMDRIVER` | `datastore` | Osm driver mode that defines which KV storage will be used | no | - |

## PubSub configuration

At PubSub should be created topic with name:

**name:** `status-changed`

It can be overridden by:

- through the Spring Boot property `gcp.status-changed.topicName`
- environment variable `STATUS_CHANGED_TOPIC_NAME`

## GCS configuration <a name="ObjectStoreConfig"></a>

### Per-tenant buckets configuration
These buckets must be defined in tenants’ “data” GCP projects that names are pointed in tenants’ PartitionInfo registration objects’ “projectId” property at the Partition service.

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
The GCP Identity and Access Management service account for the File service must have the
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


