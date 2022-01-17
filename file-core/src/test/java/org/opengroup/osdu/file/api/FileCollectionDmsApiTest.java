package org.opengroup.osdu.file.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengroup.osdu.core.common.dms.IDmsService;
import org.opengroup.osdu.core.common.dms.model.CopyDmsRequest;
import org.opengroup.osdu.core.common.dms.model.CopyDmsResponse;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsRequest;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class FileCollectionDmsApiTest {

  @Mock
  @Qualifier("FileCollectionDmsService")
  IDmsService fileCollectionDmsService;

  @InjectMocks
  FileCollectionDmsApi fileCollectionDmsApi;

  @Mock
  private StorageInstructionsResponse mockStorageInstructionsResp;

  @Mock
  private RetrievalInstructionsResponse mockRetrievalInstructionsResp;

  @Mock
  private RetrievalInstructionsRequest mockRetrievalInstructionsRequest;

  @Test
  public void testGetStorageInstructions() {
    when(fileCollectionDmsService.getStorageInstructions())
        .thenReturn(mockStorageInstructionsResp);

    ResponseEntity<StorageInstructionsResponse> storageInstructionsResponse
        = fileCollectionDmsApi.getStorageInstructions();

    assertEquals(storageInstructionsResponse.getStatusCode(), HttpStatus.OK);
  }

  @Test
  public void testGetRetrievalInstructions() {
    when(fileCollectionDmsService.getRetrievalInstructions(mockRetrievalInstructionsRequest))
        .thenReturn(mockRetrievalInstructionsResp);

    ResponseEntity<RetrievalInstructionsResponse> retrievalInstructionsResponse
        = fileCollectionDmsApi.getRetrievalInstructions(mockRetrievalInstructionsRequest);

    assertEquals(retrievalInstructionsResponse.getStatusCode(), HttpStatus.OK);
  }

  @Test
  public void testCopyDms() {
    CopyDmsRequest mockCopyDmsRequest = mock(CopyDmsRequest.class);
    CopyDmsResponse mockCopyDmsResponse = mock(CopyDmsResponse.class);

    List<CopyDmsResponse> copyDmsResponses = new ArrayList<>();
    copyDmsResponses.add(mockCopyDmsResponse);

    when(fileCollectionDmsService.copyDatasetsToPersistentLocation(any())).thenReturn(copyDmsResponses);
    ResponseEntity<List<CopyDmsResponse>>responses = fileCollectionDmsApi.copyDms(mockCopyDmsRequest);

    assertEquals(responses.getStatusCode(), HttpStatus.OK);

  }

}
