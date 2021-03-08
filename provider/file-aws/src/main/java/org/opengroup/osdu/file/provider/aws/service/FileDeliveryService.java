package org.opengroup.osdu.file.provider.aws.service;

import javax.inject.Inject;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.model.DownloadUrlResponse;
import org.opengroup.osdu.file.model.SignedUrl;
import org.opengroup.osdu.file.provider.interfaces.IStorageService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Primary
public class FileDeliveryService {

  @Inject
  final DpsHeaders headers;
  
  @Inject
  final IStorageService storageService;
  // final DataLakeStorageFactory storageFactory;
  // final IStorageUtilService storageUtilService;

  public DownloadUrlResponse getSignedUrlsByRecordId(String id) {

    SignedUrl signedUrl = storageService.createSignedUrl(id, null, headers.getPartitionIdWithFallbackToAccountId());
    
    return DownloadUrlResponse.builder().signedUrl(signedUrl.getUrl().toString()).build();
  }
  

}
