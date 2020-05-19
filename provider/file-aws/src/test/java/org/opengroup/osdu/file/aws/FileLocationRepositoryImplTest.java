package org.opengroup.osdu.file.aws;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opengroup.osdu.core.aws.dynamodb.DynamoDBQueryHelper;
import org.opengroup.osdu.core.aws.dynamodb.QueryPageResult;
import org.opengroup.osdu.core.common.model.file.DriverType;
import org.opengroup.osdu.core.common.model.file.FileListRequest;
import org.opengroup.osdu.core.common.model.file.FileListResponse;
import org.opengroup.osdu.core.common.model.file.FileLocation;
import org.opengroup.osdu.file.aws.repository.FileLocationDoc;
import org.opengroup.osdu.file.aws.repository.FileLocationRepositoryImpl;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;

import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest(classes={FileAwsApplication.class})
public class FileLocationRepositoryImplTest {

  private static final String FIND_ALL_FILTER_EXPRESSION = "createdAt BETWEEN :startDate and :endDate AND createdBy = :user";

  @InjectMocks
  private FileLocationRepositoryImpl repo;

  @Mock
  private DynamoDBQueryHelper queryHelper;

  @Before
  public void setUp() {
    initMocks(this);
  }

  @Test
  public void testFindByFileId(){
    // Arrange
    String testFileId = "test-id";

    FileLocationDoc doc = new FileLocationDoc();
    doc.setFileId("test-id");
    doc.setDriver("GCS");
    doc.setLocation("testing");
    doc.setCreatedBy("testing");

    Mockito.when(queryHelper.loadByPrimaryKey(Mockito.eq(FileLocationDoc.class), Mockito.eq(testFileId)))
        .thenReturn(doc);

    // Act
    FileLocation location = repo.findByFileID(testFileId);

    // Assert
    Assert.assertEquals(testFileId, location.getFileID());
  }

  @Test
  public void testSave(){
    // Arrange
    FileLocation testLocation = new FileLocation();
    testLocation.setFileID("test file id");
    testLocation.setLocation("test location");
    testLocation.setDriver(DriverType.valueOf("GCS"));
    testLocation.setCreatedBy("test created by");

    FileLocationDoc expectedDoc = new FileLocationDoc();
    expectedDoc.setFileId("test file id");
    expectedDoc.setLocation("test location");
    expectedDoc.setDriver("GCS");
    expectedDoc.setCreatedBy("test created by");

    Mockito.doNothing().when(queryHelper).save(Mockito.eq(expectedDoc));

    // Act
    FileLocation location = repo.save(testLocation);

    // Assert
    Mockito.verify(queryHelper, Mockito.times(1)).save(expectedDoc);
  }

  @Test
  public void testfindAll() throws UnsupportedEncodingException {
    // Arrange
    FileListRequest request = new FileListRequest();
    request.setCursor("test cursor");
    request.setPageNum(0);
    request.setTimeFrom(LocalDateTime.parse("2020-02-20T17:19:04.933"));
    request.setTimeTo(LocalDateTime.parse("2020-02-21T17:19:04.933"));
    request.setUserID("test user id");
    request.setItems((short)1);

    QueryPageResult<FileLocationDoc> docs = new QueryPageResult<>("next cursor", new ArrayList<>());
    Mockito.when(queryHelper.scanPage(Mockito.eq(FileLocationDoc.class), Mockito.eq(1), Mockito.eq("test cursor"),
        Mockito.eq(FIND_ALL_FILTER_EXPRESSION), Mockito.anyObject()))
        .thenReturn(docs);

    // Act
    FileListResponse response = repo.findAll(request);

    // Assert
    Mockito.verify(queryHelper, Mockito.times(1))
        .scanPage(Mockito.eq(FileLocationDoc.class), Mockito.eq(1), Mockito.eq("test cursor"),
        Mockito.eq(FIND_ALL_FILTER_EXPRESSION), Mockito.anyObject());
  }
}
