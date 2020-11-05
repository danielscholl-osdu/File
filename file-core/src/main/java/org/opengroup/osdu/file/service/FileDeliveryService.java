package org.opengroup.osdu.file.service;

import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.http.HttpResponse;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.constant.FileMetadataConstant;
import org.opengroup.osdu.file.model.DownloadUrlResponse;
import org.opengroup.osdu.file.model.SignedUrl;
import org.opengroup.osdu.file.model.storage.Record;
import org.opengroup.osdu.file.provider.interfaces.IStorageService;
import org.opengroup.osdu.file.provider.interfaces.IStorageUtilService;
import org.opengroup.osdu.file.service.storage.DataLakeStorageFactory;
import org.opengroup.osdu.file.service.storage.StorageException;
import org.opengroup.osdu.file.service.storage.DataLakeStorageService;
import org.springframework.stereotype.Service;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileDeliveryService {

  final DpsHeaders headers;
  final IStorageService storageService;
  final DataLakeStorageFactory storageFactory;
  final IStorageUtilService storageUtilService;

  public DownloadUrlResponse getSignedUrlsByRecordId(String id) {

    DataLakeStorageService dataLakeStorage = this.storageFactory.create(headers);
    Record rec;

    try {
      rec = dataLakeStorage.getRecord(id);

    }catch(StorageException storageExc) {

      HttpResponse response = storageExc.getHttpResponse();
      throw new AppException(response.getResponseCode(), "Failed to find record for the given file id.",storageExc.getMessage());
    }

    if(null == rec)
      throw new AppException(HttpStatus.SC_NOT_FOUND, "Not Found.", "File id not found.");

    JsonObject data = rec.getData();
    JsonElement filePathJE = data.get(FileMetadataConstant.FILE_SOURCE);
    String absolutePath = storageUtilService.getPersistentLocation(filePathJE.getAsString(), headers.getPartitionId());
    SignedUrl signedUrl = storageService.createSignedUrlFileLocation(absolutePath, headers.getAuthorization());
    return  DownloadUrlResponse.builder().signedUrl(signedUrl.getUrl().toString()).build();
  }

}
