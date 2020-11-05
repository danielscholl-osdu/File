package org.opengroup.osdu.file.mapper;

import org.opengroup.osdu.file.exception.ApplicationException;
import org.opengroup.osdu.file.model.filemetadata.FileMetadata;
import org.opengroup.osdu.file.model.filemetadata.RecordVersion;
import org.opengroup.osdu.file.model.filemetadata.filedetails.FileData;
import org.opengroup.osdu.file.model.storage.Record;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

@Component
public class FileMetadataRecordMapper {

  private final ObjectMapper mapper;

  public FileMetadataRecordMapper(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  JsonObject asJsonObject(FileData fileData) throws ApplicationException {
    try {
      String schemaString = mapper.writeValueAsString(fileData);
      Gson gson = new GsonBuilder().create();
      return gson.fromJson(schemaString, JsonObject.class);
    } catch (Exception e) {
      throw new ApplicationException("Error occurred in data payload parsing", e);
    }
  }

  FileData jsonObjectToFileData(JsonObject jsonObject) throws ApplicationException {
    try {
      Gson gson = new GsonBuilder().create();

      String schemaString = gson.toJson(jsonObject);

      return mapper.readValue(schemaString, FileData.class);
    } catch (Exception e) {
      throw new ApplicationException("Error occurred in data payload parsing", e);
    }
  }

  public Record fileMetadataToRecord(FileMetadata fileMetadata) throws ApplicationException {
    Record record = new Record();
    record.setId(fileMetadata.getId());
    record.setAcl(fileMetadata.getAcl());
    record.setLegal(fileMetadata.getLegal());
    record.setKind(fileMetadata.getKind());
    record.setAncestry(fileMetadata.getAncestry());
    record.setData(asJsonObject(fileMetadata.getData()));

    return record;

  }

  public RecordVersion recordToRecordVersion(Record record) throws ApplicationException {
    return RecordVersion
        .builder()
        .id(record.getId())
        .acl(record.getAcl())
        .legal(record.getLegal())
        .ancestry(record.getAncestry())
        .kind(record.getKind())
        .data(jsonObjectToFileData(record.getData()))
        .version(record.getVersion())
        .build();

  }
}
