package org.opengroup.osdu.file.mapper;

import java.util.HashMap;
import java.util.Map;

import org.opengroup.osdu.file.exception.ApplicationException;
import org.opengroup.osdu.file.model.filemetadata.FileMetadata;
import org.opengroup.osdu.file.model.filemetadata.RecordVersion;
import org.opengroup.osdu.file.model.filemetadata.filedetails.FileData;
import org.opengroup.osdu.file.model.storage.Record;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

@Component
public class FileMetadataRecordMapper {

	private final ObjectMapper mapper;

	public FileMetadataRecordMapper(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	public Record fileMetadataToRecord(FileMetadata fileMetadata) throws ApplicationException {
		Record record = new Record();
		record.setId(fileMetadata.getId());
		record.setAcl(fileMetadata.getAcl());
		record.setLegal(fileMetadata.getLegal());
		record.setKind(fileMetadata.getKind());
		record.setAncestry(fileMetadata.getAncestry());
		record.setData(asJsonMap(fileMetadata.getData()));
		if (fileMetadata.getMeta() != null) {
			record.setMeta(fileMetadata.getMeta());
		}
		if (fileMetadata.getTags() != null) {
			record.setTags(fileMetadata.getTags());
		}

		return record;

	}

	JsonObject convertToJsonObject(Map<String, String> map) throws ApplicationException {
		try {
			String tags = mapper.writeValueAsString(map);
			Gson gson = new GsonBuilder().create();
			return gson.fromJson(tags, JsonObject.class);
		} catch (JsonProcessingException | JsonSyntaxException e) {
			throw new ApplicationException("Error occurred in tags payload parsing", e);
		}

	}

	public RecordVersion recordToRecordVersion(Record record) throws ApplicationException {
		RecordVersion recordVersion = RecordVersion.builder().id(record.getId()).acl(record.getAcl())
				.legal(record.getLegal()).ancestry(record.getAncestry()).kind(record.getKind())
				.data(fileDataFromJsonMap(record.getData())).version(record.getVersion()).build();
		if (record.getMeta() != null) {
			recordVersion.setMeta(record.getMeta());
		}

		if (record.getTags() != null) {
			recordVersion.setTags(record.getTags());
		}
		return recordVersion;
	}

	private Map<String, Object> asJsonMap(FileData fileData) throws ApplicationException {
		String schemaString = null;
		try {
			schemaString = mapper.writeValueAsString(fileData);
			TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
			};
			return mapper.readValue(schemaString, typeRef);
		} catch (JsonProcessingException | JsonSyntaxException e) {
			throw new ApplicationException("Error occurred in data payload parsing", e);
		}
	}

	private FileData fileDataFromJsonMap(Map<String, Object> jsonMap) throws ApplicationException {
		try {
			String jsonStr = mapper.writeValueAsString(jsonMap);
			return mapper.readValue(jsonStr, FileData.class);
		} catch (JsonProcessingException e) {
			throw new ApplicationException("Error occurred in data payload parsing", e);
		}
	}
}
