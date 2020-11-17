package org.opengroup.osdu.file.provider.gcp.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
public class GoogleCloudStorageUtilTest {

    @InjectMocks
    GoogleCloudStorageUtil googleCloudStorageUtil;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        googleCloudStorageUtil = new GoogleCloudStorageUtil();
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
