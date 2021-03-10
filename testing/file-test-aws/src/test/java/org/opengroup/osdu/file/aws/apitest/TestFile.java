package org.opengroup.osdu.file.aws.apitest;

import java.io.IOException;

import com.amazonaws.services.s3.model.S3Location;
import com.sun.jersey.api.client.ClientResponse;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.opengroup.osdu.core.common.model.file.FileLocationResponse;
import org.opengroup.osdu.core.common.model.file.LocationResponse;
import org.opengroup.osdu.file.apitest.File;
import org.opengroup.osdu.file.aws.util.CloudStorageUtilAws;
import org.opengroup.osdu.file.aws.util.HttpClientAws;
import org.opengroup.osdu.file.aws.util.IntTestS3Location;
import org.opengroup.osdu.file.util.FileUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestFile extends File {

    // protected static final DummyRecordsHelper RECORDS_HELPER = new DummyRecordsHelper();
    private static String containerName = System.getProperty("STAGING_CONTAINER_NAME", System.getenv("STAGING_CONTAINER_NAME"));
    
    @BeforeAll
    public static void setUp() throws IOException {
        client = new HttpClientAws();
        cloudStorageUtil = new CloudStorageUtilAws();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        if (!locationResponses.isEmpty()) {
            for (LocationResponse response : locationResponses) {
                ClientResponse getFileLocationResponse = client.send(
                    getFileLocation,
                    "POST",
                    getCommonHeader(),
                    FileUtils.generateFileRequestBody(response.getFileID()));

                FileLocationResponse fileLocationResponse = mapper
                    .readValue(getFileLocationResponse.getEntity(String.class), FileLocationResponse.class);
                if(fileLocationResponse!=null && StringUtils.isNotBlank(fileLocationResponse.getLocation())) {                    
                    
                        IntTestS3Location s3Location = new IntTestS3Location(fileLocationResponse.getLocation());                                                
                        cloudStorageUtil.deleteCloudFile(s3Location.bucket, s3Location.key);
                    
                }
            }
        }
    }

//     @Test
//     @Override
//   public void shouldReturnBadRequest_whenGetNonExistingFileLocation() throws Exception {
//     ClientResponse getFileLocationResponse = client.send(
//         getFileLocation,
//         "POST",
//         getCommonHeader(),
//         FileUtils.generateFileRequestBody(FileUtils.generateUniqueFileID()));

//     assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, getFileLocationResponse.getStatus());
//   }

//   @Test
//   @Override
//   public void first_getLocation_then_GetFileLocationShouldReturnUnsignedUrl() throws Exception {
//     ClientResponse getLocationResponse = client.send(
//         getLocation,
//         "POST",
//         getCommonHeader(),
//         FileUtils.generateFileRequestBody(FileUtils.generateUniqueFileID())
//     );

//     assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, getLocationResponse.getStatus());

//   }


//   @Test
//   @Override
//   public void first_getLocation_then_shouldReturnFileList_sameFileId() throws Exception {

//     ClientResponse getLocationResponse = client.send(
//         getLocation,
//         "POST",
//         getCommonHeader(),
//         "{}");

//     assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, getLocationResponse.getStatus());

//   }

//   @Test
//   @Override
//   public void shouldReturnNotEmptyFileIdAndSignedURL_whenGetLocationWithoutBody()
//       throws Exception {
//     ClientResponse getLocationResponse = client.send(
//         getLocation,
//         "POST",
//         getCommonHeader(),
//         "{}");

//     assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, getLocationResponse.getStatus());
//   }

//   @Test
//   @Override
//   public void shouldReturnSignedUrlAndSameFileId_whenGetLocationWithBody() throws Exception {
//     String fileID = FileUtils.generateUniqueFileID();
//     ClientResponse getLocationResponse = client.send(
//         getLocation,
//         "POST",
//         getCommonHeader(),
//         FileUtils.generateFileRequestBody(fileID)
//     );

//     assertEquals(HttpStatus.SC_NOT_IMPLEMENTED, getLocationResponse.getStatus());
//   }

    
}
