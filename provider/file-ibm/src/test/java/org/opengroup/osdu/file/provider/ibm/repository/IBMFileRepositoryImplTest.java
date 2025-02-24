/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.file.provider.ibm.repository;

import static org.junit.Assert.assertNotNull;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.file.DriverType;
import org.opengroup.osdu.core.common.model.file.FileListRequest;
import org.opengroup.osdu.core.common.model.file.FileListResponse;
import org.opengroup.osdu.core.common.model.file.FileLocation;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.ibm.cloudant.IBMCloudantClientFactory;
import org.opengroup.osdu.file.ReplaceCamelCase;
import org.opengroup.osdu.file.provider.ibm.model.file.FileLocationDoc;
import org.slf4j.Logger;

import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.Response;
import com.cloudant.client.api.query.QueryResult;
@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceCamelCase.class)
public class IBMFileRepositoryImplTest {

  private static final String FIND_ALL_FILTER_EXPRESSION = "createdAt BETWEEN :startDate and :endDate AND createdBy = :user";
  
  
@Mock
private   Database db;

@Mock
private DpsHeaders headers;
  
 
@Mock
QueryResult<FileLocationDoc> queryResults;

@Mock
QueryResult queryResult;

  
  @Mock
  private IBMCloudantClientFactory cloudantFactory;
  
  @Mock
  private Response response;
 
  
  @Mock
  private  Logger logger;
  
  @Mock FileListResponse fileListResponse;
  
  @Mock
  TenantInfo tenant;
 

  @InjectMocks
  private  IBMFileRepositoryImpl dbRepo;
  
  private static final String dataPartitionId = "testPartitionId";
  @Before
  public void setUp() {
    initMocks(this);
  }

  @Test
  public void testFindByFileId() throws Exception {
    // Arrange
    String testFileId = "test-id";
    Mockito.when(tenant.getName()).thenReturn("shri");

  //  Whitebox.setInternalState(dbRepo, "s3SignedUrlExpirationTimeInDays", s3SignedUrlExpirationTimeInDays);
    FileLocationDoc fileLocationDoc = new FileLocationDoc();
    fileLocationDoc.set_id(testFileId);
    
    
  
    
    
    fileLocationDoc.setFileID(testFileId);
    fileLocationDoc.setDriver(DriverType.GCS);
    fileLocationDoc.setLocation("testing");
    fileLocationDoc.setCreatedBy("testing");
    fileLocationDoc.setCreatedDate(1593171481221L);
    
  //  Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
	//Mockito.when(cloudantFactory.getDatabase(anyString(),anyString())).thenReturn(db);

  //  Mockito.when(db.contains(testFileId)).thenReturn(true);
    Mockito.when(db.find(FileLocationDoc.class, testFileId)).thenReturn(fileLocationDoc);
    
  //Mockito.when(fileLocationDoc.getFileLocation()).thenReturn(fileLocationDoc.getFileLocation());
   

    // Act
    FileLocation location = dbRepo.findByFileID(testFileId);

    // Assert
    Assert.assertEquals(testFileId, location.getFileID());
  }

  @Test
  public void testSave() throws Exception {
    // Arrange
	  String testFileId = "test-id";
//	  Mockito.when(tenant.getName()).thenReturn("shri");
  
    FileLocationDoc expectedDoc = new FileLocationDoc();
    expectedDoc.set_id(testFileId);
    
    expectedDoc.setFileID(testFileId);
    
    expectedDoc.setLocation("test location");
    expectedDoc.setDriver(DriverType.GCS);
    expectedDoc.setCreatedBy("test created by");
    expectedDoc.setCreatedDate(1593171481221L);
	Mockito.when(db.contains(Mockito.any())).thenReturn(false);
//	Mockito.when(dbRepo.buildFileLocation(expectedDoc)).thenReturn(location);
	Mockito.when(db.save(expectedDoc)).thenReturn(response);
	Mockito.when(response.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);

    // Assert
  assertNotNull(dbRepo.save(expectedDoc.getFileLocation()));
  
  }

  @Test
  public void testfindAll() throws UnsupportedEncodingException {
    // Arrange
	  FileListRequest request = new FileListRequest();
	//    request.setCursor("test cursor");
	  String testFileId = "test-id";
	  FileLocationDoc expectedDoc = new FileLocationDoc();
	    expectedDoc.set_id(testFileId);
	
	    expectedDoc.setFileID(testFileId);
	    
	    expectedDoc.setLocation("test location");
	    expectedDoc.setDriver(DriverType.GCS);
	    expectedDoc.setCreatedBy("test created by");
	    expectedDoc.setCreatedDate(1593171481221L);
	    request.setPageNum(0);
	    request.setTimeFrom(LocalDateTime.parse("2020-02-20T17:19:04.933"));
	    request.setTimeTo(LocalDateTime.parse("2020-02-21T17:19:04.933"));
	    request.setUserID("test user id");
	    request.setItems((short)1);
	    List fileLocationDocList = new <FileLocationDoc>ArrayList();
		fileLocationDocList.add(expectedDoc);
	    QueryResult<FileLocationDoc>queryResultList = new QueryResult  (fileLocationDocList ,null, null, null);
	   // queryResultList.
	    Mockito.when(db.query(Mockito.any(),Mockito.any())).thenReturn(queryResult);
        Mockito.when(queryResult.getDocs()).thenReturn(fileLocationDocList);

	  //  QueryResult<>  results= new QueryResult  new ArrayList<FileLocationDoc>());
	
	   
        
//	    Mockito.when(db.query(new QueryBuilder(
//	     		and(gte("fileLocation.createdAt",toDate(request.getTimeFrom())) ,lte("fileLocation.createdAt",  toDate(request.getTimeTo())),
//		                 eq ("fileLocation.createdBy", request.getUserID()))).fields("fileLocation").limit( request.getPageNum()).build(), FileLocationDoc.class))
//	    .thenReturn( queryResultList);
	    
	    FileListResponse fileListResponse = dbRepo.findAll(request);
	    		
	    Assert.assertEquals(expectedDoc.getCreatedBy(), fileListResponse.getContent().get(0).getCreatedBy());
	    Assert.assertEquals(expectedDoc.getLocation(), fileListResponse.getContent().get(0).getLocation());
	    
	
  }
//  
//  private String toDate(LocalDateTime dateTime) {
//		//return	from(dateTime.toInstant(ZoneOffset.UTC)).toString();
//			//System.out.println("current date .............."+ new Date().toString());
//			//return  new Date().toString();
//			//Jun 10, 2020 1:17:32 AM
//			DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("MMM dd, yyyy h:mm:ss a");
//		//	DateTimeFormatter DateFor = new DateTimeFormatter("MMM dd, yyyy h:mm:ss a");
//		  //  Date dateValue= Date.from(dateTime.toInstant(ZoneOffset.UTC));
//		    logger.debug("current date .............."+dateTime.format(myFormatObj));
//		
//		    return dateTime.format(myFormatObj);
//		    
//		  }
//  
////  
////  private FileLocation buildFileLocation( FileLocationDoc fileLocationDoc) {
////		
////		//    Logger.info("Build file location. Document snapshot : {}",data);
////			return FileLocation.builder()
////			        .fileID(fileLocationDoc.getFileLocation().getFileID())
////			        .driver(fileLocationDoc.getFileLocation().getDriver())
////			        .location(fileLocationDoc.getFileLocation().getLocation())
////			        .createdAt(fileLocationDoc.getFileLocation().getCreatedAt())
////			        .createdBy(fileLocationDoc.getFileLocation().getCreatedBy())
////			        .build();
////		  }
		
}
