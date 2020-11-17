package org.opengroup.osdu.file.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opengroup.osdu.file.exception.ApplicationException;
import org.opengroup.osdu.file.model.filemetadata.FileMetadata;
import org.opengroup.osdu.file.model.filemetadata.filedetails.FileData;
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
        FileMetadata fileMetadata = FileMetadata.builder()
                .id("tenant1:1234")
                .kind("wks:tenant1:file")
                .data(FileData.builder().fileSource("stage/file.txt").build()).build();

        Record record = fileDataRecordMapper.fileMetadataToRecord(fileMetadata);
        assertEquals("tenant1:1234", record.getId());
        assertEquals("stage/file.txt", record.getData().get("FileSource").getAsString());

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
