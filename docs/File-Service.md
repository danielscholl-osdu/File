## Table of Contents

* [Introduction](#introduction)
* [System interactions](#system-interactions)
	- [File Upload](#file-upload)
  - [File Metadata](#file-metadata)
  - [File Download](#file-download)
* [Validations](#validations)
* [References](#references)


## Introduction <a name="introduction"></a>

The File Service allows users to manage files on the data platform. File Management includes uploads, downloads, creation and retrieval of metadata record for files.

As part of metadata input, the APIs have the provision to describe the information associated with the file as well as describe the content of the file. The content description could be of use for any workflows that would like to extract or process the contents of the file.

## System interactions <a name="system-interactions"></a>

The File service defines the following workflows:

* File Upload
* File Metadata
* File Download

### File Upload <a name="file-upload"></a>

**Required roles:**  `service.file.editors` or `service.file.viewers`.

These endpoints are used to generate signed URL, and are used by the users or an applications to upload a file for ingestion:

- `/v2/getLocation`
This is a **POST** endpoint, that creates a new location in the landing zone to upload a file. If a FileID isn't provided in the request, the File Service generates a
Universally Unique Identifier (UUID) to be stored in FileID. If a FileID is provided and is already registered in the system, an error is returned.
	
	The generated signed URL has a maximum duration of 7 days.
	
	The response body constains `FileID` along with a Location object which contains the Signed URL within it. Using this signed URL user can upload the file.

  **Note:**
  - The FileID must correspond to the regular expression: ^[\w,\s-]+(\.\w+)?$.
  - This endpoint will be deprecated and the recommendation is to not use this. Please refer to `/v2/files/uploadURL` for generating signed URL to upload a file.

- `/v2/files/uploadURL`
It is a **GET** endpoint to upload a file. This generates a temporary signed URL to upload a file.

  **Note:**
  -	The signed URL expires after a set time that varies as per environment. For example Azure implementation has the expiry limit set to 7 days.
  - When generated URL expires it cannot be used anymore to upload a file. The user should request a new signed URL.
  - When the generated URL expires in the middle of file upload the upload will continue, and the file will be uploaded.

  The user receives a `FileSource` in the response. This is the relative path where the uploaded file will persist. Once the file is uploaded successfully, `FileSource` can then be used to post metadata of the file. The uploaded file goes in landing zone and gets automatically deleted, if the metadata is not posted within 24 hours of uploading the file.

### File Metadata <a name="file-metadata"></a>

The metadata schema not only allows users to define the attributes/properties of the file, like name, size etc but also allow users to define/describe the contents of the file. This can be done using the `ExtensionProperties`. `FileContentDetails` part of the ExtensionProperties.

The File Service includes the content details in the file Metadata records. The main consumers of this information are workflows that get triggered after a file is uploaded and is discoverable.

The schema for providing the metadata information for a file can be found here: [Generic File Metadata Schema](#file-metadata-schema)

This is the sample metadata for CSV file: [Sample Generic File Metadata](#generic-metadata)


These endpoints are used to perform create and read operations on file metadata:

- `/v2/files/metadata`

	This is a **POST** endpoint that creates a metadata record for a file that is already uploaded. The Metadata is linked to the file via FileSource provided in the request body.
	
	If FileSource attribute is missing in the request body, "FileSource can not be empty" error is returned.
  If there is no file present, then the request fails with an error "Invalid source file path to copy from /osdu-user/1614784413120-2021-03-03-15-13-33-120/da92f52401dc4d1cb93515f159c110d4"
	
	When metadata is successfully updated in the system, the file is copied to persistent zone and then deleted from landing zone. Success response returns the Id of the file metadata record.

- `/v2/files/{Id}/metadata`

	This a **GET** endpoint which return the latest version of File metadata record identified by the given `Id`.

### File Download <a name="file-download"></a>

The below endpoints are used to generate the signed URL used to download and access the already uploaded file content.

- `/v2/files/{Id}/downloadURL`
This is a **GET** endpoint to generate a download signed URL for the files which were already uploaded and whose metadata were also created. For all such files, users should provide unique file `Id`. This download signed URL allows the user to download and access the content of the file.

	**Note:** 
	-	The signed URL expires after a set time that varies as per environment. For example Azure implementation has the expiry limit set to 7 days.
	-	The duration that signed URL is valid may vary from vendor to vendor.
    -   When generated URL expires it cannot be used anymore to download the file. The user should request a new signed URL.
    -   When the generated URL expires in the middle of file download the download will continue, and the file will be downloaded successfully.

- `/v2/getFileLocation`

This is a **POST** endpoint, which returns the `Location` (signed URL) and `Driver` (vendor name) for a given `FileId` shared within request body.

  **Note:**
  - This endpoint will be deprecated and the recommendation is to not use this. Please refer to `/v2/files/{Id}/downloadURL` for generating signed URL to download a file.

## Validations <a name="validations"></a>

The File Service implementation performs a general check of the validity of the
authorization token and partition ID before the service starts generation of a location. For accessing the file metadata, legal tags and ACL associated with the file are validated.

However, File Service won’t do any validation on attribute value passed in payload, it is the user's responsibility to pass right value by looking at description & pattern of that attribute in schema (schema reference can be found below in reference section) and File Service doesn't perform any verification whether a file upload happened or whether the user started ingestion after uploading a file. 

The File service doesn't look inside the file to validate the content within.


## References <a name="references"></a>

### Generic File Metadata Schema <a name="file-metadata-schema"></a>

- [Generic File Schema](https://community.opengroup.org/osdu/data/data-definitions/-/blob/master/Generated/dataset/File.Generic.1.0.0.json)

### Sample Generic File Metadata <a name="generic-metadata"></a>

- [Sample](https://community.opengroup.org/osdu/data/data-definitions/-/blob/master/Examples/dataset/File.Generic.1.0.0.json)
