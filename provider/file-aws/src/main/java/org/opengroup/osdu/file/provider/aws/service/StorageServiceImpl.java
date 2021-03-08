package org.opengroup.osdu.file.provider.aws.service;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.NotImplementedException;
import org.opengroup.osdu.core.common.http.json.HttpResponseBodyMapper;
import org.opengroup.osdu.core.common.http.json.HttpResponseBodyParsingException;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.file.model.SignedUrl;
import org.opengroup.osdu.file.provider.aws.di.DatasetException;
import org.opengroup.osdu.file.provider.aws.di.IDatasetFactory;
import org.opengroup.osdu.file.provider.aws.di.IDatasetService;
import org.opengroup.osdu.file.provider.aws.di.model.DatasetExceptionResponse;
import org.opengroup.osdu.file.provider.aws.di.model.DatasetRetrievalDeliveryItem;
import org.opengroup.osdu.file.provider.aws.di.model.FileDeliveryItemAWSImpl;
import org.opengroup.osdu.file.provider.aws.di.model.GetDatasetRetrievalInstructionsResponse;
import org.opengroup.osdu.file.provider.interfaces.IStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import lombok.extern.slf4j.Slf4j;

@Service
@RequestScope
@Slf4j
public class StorageServiceImpl implements IStorageService {

    @Inject
    IDatasetFactory datasetFactory;

    @Inject
    DpsHeaders headers;

    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    private final HttpResponseBodyMapper bodyMapper = new HttpResponseBodyMapper(objectMapper); 


    @Override
    public SignedUrl createSignedUrl(String fileID, String authorizationToken, String partitionID) {
        
        IDatasetService datasetService = datasetFactory.create(headers);

        GetDatasetRetrievalInstructionsResponse response;

        try {
            response = datasetService.getRetrievalInstructions(fileID);
        } catch (DatasetException e) {
            try {
                DatasetExceptionResponse body = bodyMapper.parseBody(e.getHttpResponse(), DatasetExceptionResponse.class);
                throw new AppException(body.getCode(), "Dataset Service: " + body.getReason(), body.getMessage());
            } catch (HttpResponseBodyParsingException e1) {
                throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                        "Failed to parse error from Dataset Service");
            }
        }

        DatasetRetrievalDeliveryItem deliveryItem = response.getDelivery().get(0);        

        FileDeliveryItemAWSImpl fileRetrievalProperties = deliveryItem.getRetrievalProperties();

        try {
            return SignedUrl.builder()
                            .uri(fileRetrievalProperties.getSignedUrl())
                            .url(fileRetrievalProperties.getSignedUrl().toURL())
                            .fileSource(fileRetrievalProperties.getUnsignedUrl())
                            .connectionString(fileRetrievalProperties.getConnectionString())
                            .createdBy(headers.getUserEmail())
                            .createdAt(fileRetrievalProperties.getCreatedAt())
                            .build();
        } catch (MalformedURLException e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                        "Failed to parse URI into URL for File Signed Url Path");
        }
                        
        
        
    }

    @Override
    public SignedUrl createSignedUrlFileLocation(String unsignedUrl, String authorizationToken) {
        
        throw new NotImplementedException("Not implemented. Use createSignedUrl(fileId, ...) instead");
    }  
    
}
