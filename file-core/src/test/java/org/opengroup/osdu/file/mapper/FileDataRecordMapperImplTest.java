// Copyright © 2021 Amazon Web Services
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.file.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opengroup.osdu.file.constant.FileMetadataConstant;
import org.opengroup.osdu.file.exception.ApplicationException;
import org.opengroup.osdu.file.model.filemetadata.FileMetadata;
import org.opengroup.osdu.file.model.filemetadata.RecordVersion;
import org.opengroup.osdu.file.model.filemetadata.filedetails.DatasetProperties;
import org.opengroup.osdu.file.model.filemetadata.filedetails.FileData;
import org.opengroup.osdu.file.model.filemetadata.filedetails.FileSourceInfo;
import org.opengroup.osdu.file.model.storage.Record;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.path.json.JsonPath;

@ExtendWith(SpringExtension.class)
public class FileDataRecordMapperImplTest {

    private FileMetadataRecordMapper fileDataRecordMapper;
    public static final String RECORD_ID = "tenant1:file:1b9dd1a8-d317-11ea-87d0-0242ac130003";
    public static final String FILE_DATA = "{ \"ResourceHomeRegionID\": \"namespace:reference-data--OSDURegion:SomeUniqueOSDURegionID:\", \"ResourceHostRegionIDs\": [ \"namespace:reference-data--OSDURegion:SomeUniqueOSDURegionID:\" ], \"ResourceCurationStatus\": \"namespace:reference-data--ResourceCurationStatus:CREATED:\", \"ResourceLifecycleStatus\": \"namespace:reference-data--ResourceLifecycleStatus:LOADING:\", \"ResourceSecurityClassification\": \"namespace:reference-data--ResourceSecurityClassification:RESTRICTED:\", \"Source\": \"Example Data Source\", \"ExistenceKind\": \"namespace:reference-data--ExistenceKind:Prototype:\", \"Name\": \"Dataset X221/15\", \"Description\": \"As originally delivered by ACME.com.\", \"TotalSize\": \"13245217273\", \"EncodingFormatTypeID\": \"namespace:reference-data--EncodingFormatType:text%2Fcsv:\", \"SchemaFormatTypeID\": \"namespace:reference-data--SchemaFormatType:CWLS%20LAS3:\", \"Endian\": \"BIG\", \"DatasetProperties\": { \"FileSourceInfo\": { \"FileSource\": \"s3://default_bucket/r1/data/provided/documents/1000.witsml\", \"Name\": \"1000.witsml\" } }, \"Checksum\": \"d41d8cd98f00b204e9800998ecf8427e\", \"ExtensionProperties\": {} }";
   
    
    public static final String KIND = "wks:tenant1:file";

    @Test
    public void fileMetadataToRecord() throws ApplicationException, JsonProcessingException {
        fileDataRecordMapper = new FileMetadataRecordMapper(new ObjectMapper());

        FileSourceInfo fileSourceInfo = FileSourceInfo.builder().fileSource("stage/file.txt").build();        
        DatasetProperties datasetProperties = DatasetProperties.builder().fileSourceInfo(fileSourceInfo).build();
        FileData fileData = FileData.builder().datasetProperties(datasetProperties).build();

        FileMetadata fileMetadata = FileMetadata.builder()
                .id("tenant1:1234")
                .kind("wks:tenant1:file")
                .data(fileData).build();

        Record record = fileDataRecordMapper.fileMetadataToRecord(fileMetadata);
        assertEquals("tenant1:1234", record.getId());
		assertEquals("stage/file.txt", extractFileSource(record));
	}
    
    @Test
    public void fileMetadataToRecordwithMetaBlock() throws ApplicationException {
        fileDataRecordMapper = new FileMetadataRecordMapper(new ObjectMapper());
        List<Map<String, Object>> meta = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("kye", "value");
        meta.add(map);
        FileMetadata fileMetadata = FileMetadata.builder().id(RECORD_ID).kind(KIND)
                .data(FileData.builder().source("stage/file.txt").build()).build();
        fileMetadata.setMeta(meta);
        Record record = fileDataRecordMapper.fileMetadataToRecord(fileMetadata);
        assertNotNull(record.getMeta());
    }

    @Test
    public void recordToRecordVersionTest() throws ApplicationException, JsonMappingException, JsonProcessingException {
        fileDataRecordMapper = new FileMetadataRecordMapper(new ObjectMapper());
        Record record = getRecordObj();
        Map<String, Object> jsonMap = strToJsonMap(FILE_DATA);
        record.setData(jsonMap);
        RecordVersion recordVersion = fileDataRecordMapper.recordToRecordVersion(record);
        assertNotNull(recordVersion.getData());
    }

    @Test
    public void recordToRecordVersionWithMetaBlockTest() throws ApplicationException, JsonMappingException, JsonProcessingException {
        fileDataRecordMapper = new FileMetadataRecordMapper(new ObjectMapper());
        Record record = getRecordObj();
        Map<String, Object> jsonMap = strToJsonMap(FILE_DATA);
        record.setData(jsonMap);
        List<Map<String, Object>> meta = new ArrayList<>();
        meta.add(jsonMap);
        record.setMeta(meta);
        RecordVersion recordVersion = fileDataRecordMapper.recordToRecordVersion(record);
        assertNotNull(recordVersion.getMeta());
    }

	@Test
	public void fileMetadataToRecordJsonParseError() throws ApplicationException {
		fileDataRecordMapper = new FileMetadataRecordMapper(new ObjectMapper());
		FileMetadata fileMetadata = FileMetadata.builder().id(RECORD_ID).build();

		Record result = fileDataRecordMapper.fileMetadataToRecord(fileMetadata);
		assertNotNull(result);
	}

    @Test
    public void recordToRecordVersionParseError() throws ApplicationException, JsonMappingException, JsonProcessingException {
        fileDataRecordMapper = new FileMetadataRecordMapper(new ObjectMapper());
        Record record = getRecordObj();
        String jsonString = "{ \"SchemaFormateID\": \"string\", \"PreLoadFilePath\": \"string\", \"Source\": \"/osdu-user/1609348736017-2020-12-30-17-18-56-017/943ee9537b8c4254a2fdc1f9c8684f79\", \"FileSize\": 0, \"Name\": \"File\", \"EncodingFormatTypeID\": \"string\", \"Endian\": \"BIG\", \"LossyCompressionIndicator\": true, \"CompressionMethodTypeID\": \"string\", \"CompressionLevel\": 0, \"Checksum\": \"string\", \"VectorHeaderMapping\": [ { \"KeyName\": \"string\", \"WordFormat\": \"string\", \"WordWidth\": 0, \"Position\": 0, \"UoM\": \"string\", \"ScalarIndicator\": \"STANDARD\", \"ScalarOverride\": 0 } ], \"relationships\": { \"parentEntity\": { \"confidence\": 1, \"id\": \"data_partition:namespace:entity_845934c40e8d922bc57b678990d55722\", \"name\": \"Survey ST2016\", \"version\": 0 }, \"relatedItems\": { \"confidences\": [ 0 ], \"ids\": [ \"string\" ], \"names\": [ \"string\" ], \"versions\": [ 0 ] } }, \"ExtensionProperties\": { \"Classification\": \"Raw File\", \"Description\": \"An text further describing this file example.\", \"ExternalIds\": [ \"string\" ], \"FileDateCreated\": {}, \"FileDateModified\": {}, \"FileContentsDetails\": { \"TargetKind\": \"os:npd:wellbore:1:*.*\", \"FileType\": \"csv\", \"FrameOfReference\": [ { \"kind\": \"CRS\", \"name\": \"[NAD27 * OGP-Usa Conus / North Dakota South [3202115851]ft]\", \"persistableReference\": \"scaleOffset:scale:0.3048006096012192offset:0.0symbol:ftUSbaseMeasurement:ancestry:Lengthtype:UMtype:USO}\", \"propertyNames\": [ \"elevationFromMsl\", \"totalDepthMdDriller\", \"wellHeadProjected\" ], \"propertyValues\": [ \"F\", \"ftUS\", \"deg\" ], \"uncertainty\": 0 } ], \"ExtensionProperties\": { \"kind\": \"os:npd:csvFileExtDetails:1.0.0\" }, \"ParentReference\": \"CSBE0417\" } } }";
        Map<String, Object> jsonMap = strToJsonMap(jsonString);
        record.setData(jsonMap);
        List<Map<String, Object>> meta = new ArrayList<>();
        meta.add(jsonMap);
        record.setMeta(meta);
        assertThrows(ApplicationException.class, () -> {
            RecordVersion recordVersion = fileDataRecordMapper.recordToRecordVersion(record);
            assertNull(recordVersion.getMeta());
        });
    }
    @Test
    public void fileMetadataToRecordwithTagsBlock() throws ApplicationException {
        
        fileDataRecordMapper = new FileMetadataRecordMapper(new ObjectMapper());
       
        Map<String, String> tags = new HashMap<String, String>();
        tags.put("kye", "value");
        
        FileMetadata fileMetadata = FileMetadata.builder().id(RECORD_ID).kind(KIND)
                .data(FileData.builder().source("stage/file.txt").build()).build();
        fileMetadata.setTags(tags);
        Record record = fileDataRecordMapper.fileMetadataToRecord(fileMetadata);
        assertNotNull(record.getTags());
    }
    
    @Test
    public void recordToRecordVersionWithTagsTest() throws ApplicationException, JsonMappingException, JsonProcessingException {
        fileDataRecordMapper = new FileMetadataRecordMapper(new ObjectMapper());
        Record record = getRecordObj();
        record.setData(strToJsonMap(FILE_DATA));
        Map<String, String> tagsMap = new HashMap<String, String>();
        tagsMap.put("tag", "tag");
        record.setTags(tagsMap);
        RecordVersion recordVersion = fileDataRecordMapper.recordToRecordVersion(record);
        assertNotNull(recordVersion.getTags());
    }

    private Record getRecordObj() {
        Record record = new Record();
        record.setId(RECORD_ID);
        record.setKind(KIND);
        return record;
    }
    
    private Map<String, Object> strToJsonMap(String jsonStr) throws JsonProcessingException, JsonMappingException {
		TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
		};
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(jsonStr, typeRef);
	}
    
    private String extractFileSource(Object obj) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();

		String jsonStr = mapper.writeValueAsString(obj);
		JsonPath jsonPath = JsonPath.with(jsonStr);
		return jsonPath.get(FileMetadataConstant.FILE_SOURCE_PATH);
	}
}
