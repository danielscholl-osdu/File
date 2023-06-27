<!--- Deploy -->

# GC File chart

## Introduction

This chart bootstraps a deployment on a [Kubernetes](https://kubernetes.io) cluster using [Helm](https://helm.sh) package manager.

## Prerequisites

The code was tested on **Kubernetes cluster** (v1.23.12) with **Istio** (1.15)
> It is possible to use other versions, but it hasn't been tested

### Operation system

The code works in Debian-based Linux (Debian 10 and Ubuntu 20.04) and Windows WSL 2. Also, it works but is not guaranteed in Google Cloud Shell. All other operating systems, including macOS, are not verified and supported.

### Packages

Packages are only needed for installation from a local computer.

* **HELM** (version: v3.7.1 or higher) [helm](https://helm.sh/docs/intro/install/)
* **Kubectl** (version: v1.23.12 or higher) [kubectl](https://kubernetes.io/docs/tasks/tools/#kubectl)

## Installation

First you need to set variables in **values.yaml** file using any code editor. Some of the values are prefilled, but you need to specify some values as well. You can find more information about them below.

### Global variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**global.domain** | your domain for the external endpoint, ex `example.com` | string | - | yes
**global.onPremEnabled** | whether on-prem is enabled | boolean | false | yes
**global.limitsEnabled** | whether CPU and memory limits are enabled | boolean | true | yes

### Configmap variables

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|---------|
**data.logLevel** | logging level | string | "ERROR" | yes
**data.entitlementsHost** | Entitlements service host address | string | "http://entitlements" | yes
**data.partitionHost** | Partition service host address | string | "http://partition" | yes
**data.storageHost** | Storage service host address | string | "http://storage" | yes
**data.springProfilesActive** | Spring profile that activate default configuration for Google Cloud environment | string | "gcp" | yes
**data.fileLocationUserId** | file service location user ID | string | - | yes

### Deployment variables

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|---------|
**data.requestsCpu** | amount of requested CPU | string | "10m" | yes
**data.requestsMemory** | amount of requested memory| string | "450Mi" | yes
**data.limitsCpu** | CPU limit | string | "1" |only if `global.limitsEnabled` is true
**data.limitsMemory** | memory limit | string | "1G" | only if `global.limitsEnabled` is true
**data.serviceAccountName** | name of your service account | string | - | yes
**data.imagePullPolicy** | when to pull image | string | "IfNotPresent" | yes
**data.image** | service image | string | - | yes

### Config variables

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|---------|
**conf.configmap** | configmap to be used | string | "file-config" | yes
**conf.appName** | name of the app | string | "file" | yes
**conf.rabbitmqSecretName** | secret for rabbitmq | string | "rabbitmq-secret" | yes
**conf.fileMinioSecretName** | secret for MinIO | string | "file-minio-secret" | yes
**conf.fileKeycloakSecretName** | secret for Keykloak | string | "file-keycloak-secret" | yes
**conf.filePostgresSecretName** | secret for Postgres | string | "file-postgres-secret" | yes

### ISTIO variables

| Name | Description | Type | Default |Required |
|------|-------------|------|---------|---------|
**istio.proxyCPU** | CPU request for Envoy sidecars | string | 10m | yes
**istio.proxyCPULimit** | CPU limit for Envoy sidecars | string | 500m | yes
**istio.proxyMemory** | memory request for Envoy sidecars | string | 100Mi | yes
**istio.proxyMemoryLimit** | memory limit for Envoy sidecars | string | 512Mi | yes

### Install the helm chart

Run this command from within this directory:

```console
helm install file-deploy .
```

## Uninstalling the Chart

To uninstall the helm deployment:

```console
helm uninstall file-deploy
```

[Move-to-Top](#introduction)
