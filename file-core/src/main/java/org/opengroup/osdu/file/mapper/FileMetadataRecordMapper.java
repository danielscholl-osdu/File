package org.opengroup.osdu.file.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opengroup.osdu.file.exception.ApplicationException;
import org.opengroup.osdu.file.model.filemetadata.FileMetadata;
import org.opengroup.osdu.file.model.filemetadata.RecordVersion;
import org.opengroup.osdu.file.model.filemetadata.filedetails.FileData;
import org.opengroup.osdu.file.model.storage.Record;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.JsonProcessingException;
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

	JsonObject asJsonObject(FileData fileData) throws ApplicationException {

		String schemaString = null;
		try {
			schemaString = mapper.writeValueAsString(fileData);
			Gson gson = new GsonBuilder().create();
			return gson.fromJson(schemaString, JsonObject.class);
		} catch (JsonProcessingException | JsonSyntaxException e) {
			throw new ApplicationException("Error occurred in data payload parsing", e);
		}

	}

	FileData jsonObjectToFileData(JsonObject jsonObject) throws ApplicationException {

		Gson gson = new GsonBuilder().create();

		String schemaString = gson.toJson(jsonObject);

		try {
			return mapper.readValue(schemaString, FileData.class);
		} catch (JsonProcessingException e) {
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
		if (fileMetadata.getMeta() != null) {
			record.setMeta(convertToJsonList(fileMetadata.getMeta()));
		}
		if (fileMetadata.getTags() != null) {
			record.setTags(convertToJsonObject(fileMetadata.getTags()));
		}

		return record;

	}

	private List<JsonObject> convertToJsonList(List<Map<String, Object>> meta) throws ApplicationException {
		List<JsonObject> metaList = new ArrayList<>();

		for (Map<String, Object> map : meta) {
			try {
				String metaString = mapper.writeValueAsString(map);
				Gson gson = new GsonBuilder().create();
				JsonObject jsonObj = gson.fromJson(metaString, JsonObject.class);
				metaList.add(jsonObj);
			} catch (JsonProcessingException | JsonSyntaxException e) {
				throw new ApplicationException("Error occurred in meta payload parsing", e);
			}
		}
		return metaList;

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
				.data(jsonObjectToFileData(record.getData())).version(record.getVersion()).build();
		if (record.getMeta() != null) {
			recordVersion.setMeta(convertJsonListToMap(record.getMeta()));
		}

		if (record.getTags() != null) {
			recordVersion.setTags(convertJsonObjectToMap(record.getTags()));
		}
		return recordVersion;
	}

	private List<Map<String, Object>> convertJsonListToMap(List<JsonObject> metaJsonList) throws ApplicationException {

		List<Map<String, Object>> listOfMaps = new ArrayList<Map<String, Object>>();
		for (JsonObject metaJson : metaJsonList) {
			try {
				Gson gson = new GsonBuilder().create();
				String metaString = gson.toJson(metaJson);
				Map<String, Object> map = gson.fromJson(metaString, Map.class);
				listOfMaps.add(map);

			} catch (JsonSyntaxException e) {
				throw new ApplicationException("Error occurred in meta payload parsing", e);
			}
		}
		return listOfMaps;

	}

	private Map<String, String> convertJsonObjectToMap(JsonObject jsonObject) throws ApplicationException {
		try {
			Gson gson = new GsonBuilder().create();
			String metaString = gson.toJson(jsonObject);
			return gson.fromJson(metaString, Map.class);

		} catch (JsonSyntaxException e) {
			throw new ApplicationException("Error occurred in tags payload parsing", e);
		}

	}
}
