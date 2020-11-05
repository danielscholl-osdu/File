package org.opengroup.osdu.file.errors.model;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.opengroup.osdu.file.errors.ErrorDetails;

public class StorageError extends ErrorDetails {

    public void setErrorProperties(String errorJsonObject) {
        Gson gson = new GsonBuilder().create();
        JsonObject jsonObject =  gson.fromJson(errorJsonObject, JsonObject.class);
        this.setMessage(jsonObject.get("message").getAsString());
        this.setDomain("global");
        this.setReason(jsonObject.get("reason").getAsString());
    }
}
