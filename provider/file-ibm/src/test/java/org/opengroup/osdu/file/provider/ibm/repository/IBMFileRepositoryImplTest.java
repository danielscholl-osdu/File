package org.opengroup.osdu.file.provider.ibm.repository;

import static com.cloudant.client.api.query.Expression.lte;
import static com.cloudant.client.api.query.Expression.gte;
import static com.cloudant.client.api.query.Expression.in;
import static com.cloudant.client.api.query.Expression.regex;
import static com.cloudant.client.api.query.Operation.and;
import static com.cloudant.client.api.query.Expression.eq;
import com.cloudant.client.api.model.Response;
import org.junit.jupiter.api.BeforeAll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.junit.runner.*;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.runners.MockitoJUnitRunner;
import org.omg.CORBA.portable.ApplicationException;
import org.opengroup.osdu.core.common.model.file.DriverType;
import org.opengroup.osdu.core.common.model.file.FileListRequest;
import org.opengroup.osdu.core.common.model.file.FileListResponse;
import org.opengroup.osdu.core.common.model.file.FileLocation;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.ibm.cloudant.IBMCloudantClientFactory;
import org.opengroup.osdu.file.ReplaceCamelCase;
import org.opengroup.osdu.file.provider.ibm.FileIBMApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.Before;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.query.QueryBuilder;
import com.cloudant.client.api.query.QueryResult;
import com.ibm.cloud.objectstorage.services.kms.model.NotFoundException;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.*;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.MockitoAnnotations.initMocks;
import org.opengroup.osdu.file.provider.ibm.model.file.FileLocationDoc;

import org.slf4j.Logger;
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
  
    FileLocationDoc expectedDoc = new FileLocationDoc();
    expectedDoc.set_id(testFileId);
   // Response response=new Response();
    //response.
    
    expectedDoc.setFileID(testFileId);
    
    expectedDoc.setLocation("test location");
    expectedDoc.setDriver(DriverType.GCS);
    expectedDoc.setCreatedBy("test created by");
    expectedDoc.setCreatedDate(1593171481221L);
   // expectedDoc.setCreatedDate(Instant.now());
   // FileLocation location =expectedDoc.getFileLocation();
  
 //   Mockito.when(headers.getPartitionIdWithFallbackToAccountId()).thenReturn(dataPartitionId);
  //  Mockito.when(cloudantFactory.getDatabase(anyString(),anyString())).thenReturn(db);
//	Mockito.when(db.contains(anyString())).thenReturn(false);
//	Mockito.when(dbRepo.buildFileLocation(expectedDoc)).thenReturn(location);
	Mockito.when(db.save(expectedDoc)).thenReturn(response);
	Mockito.when(response.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);


    // Act
  FileLocation location = dbRepo.save(expectedDoc.getFileLocation());

    // Assert
   Assert.assertEquals(expectedDoc.getCreatedBy(), location.getCreatedBy());
   Assert.assertEquals(expectedDoc.getLocation(), location.getLocation());
  
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
