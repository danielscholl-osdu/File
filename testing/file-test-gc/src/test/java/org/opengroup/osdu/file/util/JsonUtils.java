package org.opengroup.osdu.file.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import io.restassured.path.json.JsonPath;

public class JsonUtils {
    public static String toJson(Object src) {
        return new Gson().toJson(src);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return new Gson().fromJson(json, classOfT);
    }

    public static JsonPath getAsJsonPath(String src) {
        return JsonPath.with(src);
    }

    public static <T> T getPojoFromJSONString(Class<T> type, String json) throws JsonMappingException, JsonProcessingException,
        IOException {
		ObjectMapper mapper = new ObjectMapper();
        T pojo = mapper.readerFor(type).readValue(json);

        return pojo;
	}
}
