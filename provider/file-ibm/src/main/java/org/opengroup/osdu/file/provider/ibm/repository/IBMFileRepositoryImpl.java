/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.file.provider.ibm.repository;

import static com.cloudant.client.api.query.Expression.eq;
import static com.cloudant.client.api.query.Expression.gte;
import static com.cloudant.client.api.query.Expression.lte;
import static com.cloudant.client.api.query.Operation.and;
import static java.lang.String.format;

import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.model.file.FileListRequest;
import org.opengroup.osdu.core.common.model.file.FileListResponse;
import org.opengroup.osdu.core.common.model.file.FileLocation;
import org.opengroup.osdu.core.common.model.tenant.TenantInfo;
import org.opengroup.osdu.core.ibm.auth.ServiceCredentials;
import org.opengroup.osdu.core.ibm.cloudant.IBMCloudantClientFactory;
import org.opengroup.osdu.file.exception.FileLocationNotFoundException;
import org.opengroup.osdu.file.provider.ibm.model.file.FileLocationDoc;
import org.opengroup.osdu.file.provider.interfaces.IFileLocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.Response;
import com.cloudant.client.api.query.JsonIndex;
import com.cloudant.client.api.query.QueryBuilder;
import com.cloudant.client.api.query.QueryResult;
import com.cloudant.client.org.lightcouch.NoDocumentException;

import lombok.extern.java.Log;

@Repository
@Log
public class IBMFileRepositoryImpl implements IFileLocationRepository {

	@Value("${ibm.db.url}")
	private String dbUrl;
	@Value("${ibm.db.user:#{null}}")
	private String dbUser;
	@Value("${ibm.db.password:#{null}}")
	private String dbPassword;
	@Value("${ibm.env.prefix:local-dev}")
	private String dbNamePrefix;
	@Value("${ibm.schemaName:file-locations}")
	public String DB_NAME;
	
	private Database db;
	
	@Autowired
	private TenantInfo tenant;
	
	@PostConstruct
	public void init() throws MalformedURLException {
		IBMCloudantClientFactory cloudantFactory = new IBMCloudantClientFactory(
				new ServiceCredentials(dbUrl, dbUser, dbPassword));
		db = cloudantFactory.getDatabase(dbNamePrefix, DB_NAME);
		db.createIndex(JsonIndex.builder().name("find-json-index").asc("createdDate", "createdBy").definition());
	}

	@Override
	public FileLocation findByFileID(String fileID) {
		
		if(fileID==null) {
			return null;
			
		}
		
		tenant.getName();
		log.info("Requesting file location. File ID : " + fileID);
		try {
			FileLocationDoc doc = db.find(FileLocationDoc.class, fileID);
			return doc.getFileLocation();
		} catch (NoDocumentException e) {
			return null;
		}
	}
	
	@Override
	public FileLocation save(FileLocation fileLocation) {

		String fileName = fileLocation.getFileID();
		if (db.contains(fileName)) {
			log.severe("File " + fileName + " already exist. Can't create again.");
			throw new IllegalArgumentException("File " + fileName + " already exist. Can't create again.");
		}

		FileLocationDoc doc = new FileLocationDoc(fileLocation);
		Response newDoc = db.save(doc);

		if (newDoc.getStatusCode() == HttpStatus.SC_CREATED) {
			return FileLocation.builder().fileID(newDoc.getId()).build();
		} else {
			return null;
		}
	}

	@Override
	public FileListResponse findAll(FileListRequest request) {

		int pageSize = request.getItems();
		int pageNum = request.getPageNum();

		log.info("request.getTimeFrom()...." + request.getTimeFrom());
		log.info("request.getUserID()................" + request.getUserID());
		log.info("request.getTimeTo()........" + request.getTimeTo());

		QueryResult<FileLocationDoc> results = db.query(
				new QueryBuilder(
						and(gte("createdDate", toDate(request.getTimeFrom())),
							lte("createdDate", toDate(request.getTimeTo())), 
							eq("createdBy", request.getUserID())))
						.limit(pageSize).skip(pageSize * pageNum).build(),
				FileLocationDoc.class);

		if (results == null) {
			throw new FileLocationNotFoundException(
					format("Nothing found for such filter and page(num: %s, size: %s).", pageNum, pageSize));
		}

		List<FileLocation> content = results.getDocs().stream()
		        .map(FileLocationDoc::getFileLocation)
		        .collect(Collectors.toList());

		return FileListResponse.builder()
				.content(content)
				.size(pageSize)
				.number(pageNum)
				.numberOfElements(content.size()).build();

	}
	
	private Long toDate(LocalDateTime dateTime) {
		return Date.from(dateTime.toInstant(ZoneOffset.UTC)).getTime();

	}

}
