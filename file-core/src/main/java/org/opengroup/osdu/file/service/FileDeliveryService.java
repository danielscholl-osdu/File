package org.opengroup.osdu.file.service;

import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.constant.FileMetadataConstant;
import org.opengroup.osdu.file.model.SignedUrlParameters;
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

  public DownloadUrlResponse getSignedUrlsByRecordId(String id,
      SignedUrlParameters signedUrlParameters) throws StorageException {

    DataLakeStorageService dataLakeStorage = this.storageFactory.create(headers);
    Record rec;

    try {
      rec = dataLakeStorage.getRecord(id);

    } catch (StorageException storageExc) {

      log.error("Failed to find record for the given file id.");
      throw storageExc;
    }

    if (null == rec)
      throw new AppException(HttpStatus.SC_NOT_FOUND, "Not Found.", "File id not found.");

    String fileSource = extractFileSource(rec);
    String absolutePath = storageUtilService.getPersistentLocation(fileSource,
                                                                   headers.getPartitionId());
    SignedUrl signedUrl = storageService
        .createSignedUrlForFileLocationBasedOnParams(absolutePath, headers.getAuthorization(),
            signedUrlParameters);
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

}
