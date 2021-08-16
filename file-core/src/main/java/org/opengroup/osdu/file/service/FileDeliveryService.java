package org.opengroup.osdu.file.service;



import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.constant.FileExtension;
import org.opengroup.osdu.file.constant.FileMetadataConstant;
import org.opengroup.osdu.file.model.DownloadUrlResponse;
import org.opengroup.osdu.file.model.SignedUrl;
import org.opengroup.osdu.file.model.storage.Record;
import org.opengroup.osdu.file.provider.interfaces.IStorageService;
import org.opengroup.osdu.file.provider.interfaces.IStorageUtilService;
import org.opengroup.osdu.file.service.storage.DataLakeStorageFactory;
import org.opengroup.osdu.file.service.storage.DataLakeStorageService;
import org.opengroup.osdu.file.service.storage.StorageException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.path.json.JsonPath;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileDeliveryService {

  final DpsHeaders headers;
  final JaxRsDpsLog log;
  final IStorageService storageService;
  final DataLakeStorageFactory storageFactory;
  final IStorageUtilService storageUtilService;

  public DownloadUrlResponse getSignedUrlsByRecordId(String id) throws StorageException {

    DataLakeStorageService dataLakeStorage = this.storageFactory.create(headers);
    Record rec;
    String contentType = null;

    try {
      rec = dataLakeStorage.getRecord(id);

    } catch (StorageException storageExc) {

      log.error("Failed to find record for the given file id.");
      throw storageExc;
    }

    if (null == rec)
      throw new AppException(HttpStatus.SC_NOT_FOUND, "Not Found.", "File id not found.");

    String fileSource = extractFileSource(rec);
    String fileName = extractFileName(rec);
    if (StringUtils.isNoneEmpty(fileName)) {
        contentType = getContentTypeFromFileName(fileName);
    }
    String absolutePath = storageUtilService.getPersistentLocation(fileSource, headers.getPartitionId());
    SignedUrl signedUrl = storageService.createSignedUrlFileLocation(absolutePath, headers.getAuthorization(), fileName, contentType);
    
    return DownloadUrlResponse.builder().signedUrl(signedUrl.getUrl().toString()).build();
  }

	private String extractFileSource(Object obj) {
		ObjectMapper mapper = new ObjectMapper();

		String jsonStr;
		try {
			jsonStr = mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new AppException(HttpStatus.SC_NOT_FOUND, "Not Found.", "Unable to parse fileSource in data.DatasetProperties.FileSourceInfo.FileSource");
		}
		JsonPath jsonPath = JsonPath.with(jsonStr);
		return jsonPath.get(FileMetadataConstant.FILE_SOURCE_PATH);
	}

	
	
    private String extractFileName(Object obj) {
        ObjectMapper mapper = new ObjectMapper();
        String fileName = null;
        String jsonStr;
        try {
            jsonStr = mapper.writeValueAsString(obj);
            JsonPath jsonPath = JsonPath.with(jsonStr);
            fileName = jsonPath.get(FileMetadataConstant.FILE_NAME_PATH);
        } catch (JsonProcessingException e) {
            log.warning("Unable to parse fileName in data.DatasetProperties.FileSourceInfo.Name");
        }
        return fileName;
    }
	
    private String getContentTypeFromFileName(String fileName) {
        FileExtension fileExtension = null;
        String contentType = null;
        try {
            int index = fileName.lastIndexOf('.');
            if (index > 0) {
                String result = fileName.substring(index + 1);
                fileExtension = Enum.valueOf(FileExtension.class, result.toUpperCase());
                return fileExtension.getMimeType();
            }
        } catch (IllegalArgumentException e) {
            contentType = FileMetadataConstant.FILE_DEFAULT_EXTENSION;
        }
        return contentType;
    }
}
