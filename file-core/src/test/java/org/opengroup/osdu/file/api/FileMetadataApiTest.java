package org.opengroup.osdu.file.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengroup.osdu.file.model.DownloadUrlResponse;
import org.opengroup.osdu.file.model.filemetadata.FileMetadata;
import org.opengroup.osdu.file.model.filemetadata.FileMetadataResponse;
import org.opengroup.osdu.file.model.filemetadata.RecordVersion;
import org.opengroup.osdu.file.service.FileMetadataService;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class FileMetadataApiTest {

    @Mock
    private FileMetadataService metadataService;

    @InjectMocks
    private FileMetadataApi fileMetadataApi;

    FileMetadata fileMetadata;

    @Test
    public void saveNewMetaData() throws Exception {
        fileMetadata = new FileMetadata();
        FileMetadataResponse fileMetadataResponse = new FileMetadataResponse();
        fileMetadataResponse.setId("1234");
        when(metadataService.saveMetadata(fileMetadata)).thenReturn(fileMetadataResponse);
        assertEquals(HttpStatus.CREATED, fileMetadataApi.postFilesMetadata(fileMetadata).getStatusCode());
    }

    @Test
    public void getMetadata() throws Exception {
        fileMetadata = new FileMetadata();
        RecordVersion recordVersion = new RecordVersion();
        when(metadataService.getMetadataById("1234")).thenReturn(recordVersion);
        assertEquals(HttpStatus.OK, fileMetadataApi.getFileMetadataById("1234").getStatusCode());
    }

    @Test
    public void deleteMetadata() throws Exception {
        doNothing().when(metadataService).deleteMetadataRecord("123");
        assertEquals(HttpStatus.NO_CONTENT, fileMetadataApi.deleteFileMetadataById("1234").getStatusCode());
    }

    private DownloadUrlResponse getMockSignedDownloadUrlResponse() {
        return DownloadUrlResponse.builder().signedUrl("dummy-signed-url").build();
    }

}
