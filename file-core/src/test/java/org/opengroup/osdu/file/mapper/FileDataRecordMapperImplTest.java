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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opengroup.osdu.file.exception.ApplicationException;
import org.opengroup.osdu.file.model.filemetadata.FileMetadata;
import org.opengroup.osdu.file.model.filemetadata.filedetails.DatasetProperties;
import org.opengroup.osdu.file.model.filemetadata.filedetails.FileData;
import org.opengroup.osdu.file.model.filemetadata.filedetails.FileSourceInfo;
import org.opengroup.osdu.file.model.storage.Record;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
public class FileDataRecordMapperImplTest {

    private FileMetadataRecordMapper fileDataRecordMapper;

    @Test
    public void fileMetadataToRecord() throws ApplicationException {
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
        assertEquals("stage/file.txt", record.getData()
                .get("DatasetProperties").getAsJsonObject()
                .get("FileSourceInfo").getAsJsonObject()
                .get("FileSource").getAsString());

    }

    @Test
    public void fileMetadataToRecordJsonParseError() throws ApplicationException {
        fileDataRecordMapper = new FileMetadataRecordMapper(new ObjectMapper());
        FileMetadata fileMetadata = FileMetadata.builder()
                .id("tenant1:1234")
                .kind("wks:tenant1:file")
                .build();

        assertThrows(ApplicationException.class,()->{
          Record record = fileDataRecordMapper.fileMetadataToRecord(fileMetadata);
          assertNull(record.getData());
        });
    }
}
