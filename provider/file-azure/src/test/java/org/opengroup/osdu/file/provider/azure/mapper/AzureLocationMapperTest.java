package org.opengroup.osdu.file.provider.azure.mapper;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.file.FileLocation;
import org.opengroup.osdu.core.common.model.file.LocationResponse;
import org.opengroup.osdu.file.model.SignedUrl;
import org.opengroup.osdu.file.provider.azure.TestUtils;

import java.net.URL;

import static org.mockito.MockitoAnnotations.openMocks;
import static org.opengroup.osdu.file.provider.azure.mapper.AzureLocationMapper.FILE_SOURCE;
import static org.opengroup.osdu.file.provider.azure.mapper.AzureLocationMapper.SIGNED_URL_KEY;

@ExtendWith(MockitoExtension.class)
public class AzureLocationMapperTest {

  @InjectMocks
  AzureLocationMapper azureLocationMapper;

  @BeforeEach
  public void init() {
    openMocks(this);
  }

  @Test
  public void ShouldBuildLocationResponse() {
    FileLocation fileLocation = FileLocation.builder()
        .fileID(TestUtils.FILE_ID)
        .build();

    SignedUrl signedUrl = getSignedUrl();

    LocationResponse response =
        azureLocationMapper.buildLocationResponse(signedUrl, fileLocation);

    Assertions.assertEquals(response.getFileID(), fileLocation.getFileID());
    Assertions.assertEquals(response.getLocation().get(SIGNED_URL_KEY), signedUrl.getUrl().toString());
    Assertions.assertEquals(response.getLocation().get(FILE_SOURCE), signedUrl.getFileSource());

  }

  private SignedUrl getSignedUrl() {
    String containerName = RandomStringUtils.randomAlphanumeric(4);
    String folderName = TestUtils.USER_DES_ID + "/" + RandomStringUtils.randomAlphanumeric(9);
    String filename = TestUtils.getUuidString();

    URL url = TestUtils.getAzureObjectUrl(containerName, folderName, filename);

    return SignedUrl.builder()
        .url(url)
        .fileSource(filename)
        .build();
  }
}
