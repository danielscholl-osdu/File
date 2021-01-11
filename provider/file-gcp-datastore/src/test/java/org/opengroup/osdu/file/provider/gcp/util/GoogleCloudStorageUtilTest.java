package org.opengroup.osdu.file.provider.gcp.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opengroup.osdu.file.provider.gcp.config.PropertiesConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class GoogleCloudStorageUtilTest {

    @Mock
    PropertiesConfiguration propertiesConfiguration;

    @InjectMocks
    GoogleCloudStorageUtil googleCloudStorageUtil;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(propertiesConfiguration.getPersistentArea()).thenReturn("persistent-area");
        when(propertiesConfiguration.getStagingArea()).thenReturn("persistent-area");
        googleCloudStorageUtil = new GoogleCloudStorageUtil(propertiesConfiguration);
    }

  @Test
    public void getAcls() {
        assertEquals(1, googleCloudStorageUtil.getAcls("test-service-account").size());
    }

    @Test
    public void getCompleteFilePath() {
        String destinationBucket = "google-bucket";
        String filePath = "/some-area/some-folder/filename.txt";

        String requiredPath = "gs://google-bucket/some-area/some-folder/filename.txt";

        assertEquals(requiredPath, googleCloudStorageUtil.getCompleteFilePath(destinationBucket,
                filePath));

    }

    @Test
    public void getPersistentBucket() {
        String persistentArea = "tenant-project-persistent-area";
        assertEquals(persistentArea, googleCloudStorageUtil.getPersistentBucket("tenant-project"));
    }

    @Test
    public void isPathEmpty() {
        assertFalse(googleCloudStorageUtil.isPathsEmpty("fromBucket", "fromPath", "toBucket", "toPath"));
        assertTrue(googleCloudStorageUtil.isPathsEmpty("", "fromPath", "toBucket", "toPath"));
        assertTrue(googleCloudStorageUtil.isPathsEmpty(null, "fromPath", "toBucket", "toPath"));
    }

    @Test
    public void getBucketName() {
        String path = "gs://google-bucket/some-area/some-folder/filename.txt";
        assertEquals("google-bucket", googleCloudStorageUtil.getBucketName(path));
    }

    @Test
    public void getDirectoryPath() {
        String path = "gs://google-bucket/some-area/some-folder/filename.txt";
        assertEquals("some-area/some-folder/filename.txt", googleCloudStorageUtil.getDirectoryPath(path));
    }
}
