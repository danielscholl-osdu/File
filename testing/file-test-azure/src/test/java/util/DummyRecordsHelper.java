// Copyright 2017-2019, Schlumberger
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
package util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.lang3.RandomStringUtils;

public class DummyRecordsHelper {
  public static String buildInvalidFileLocationPayload(String fileId){
    JsonObject item= new JsonObject();
    item.addProperty("File",fileId);

    return new Gson().toJson(item);
  }

    public BadRequestMock getRecordsMockFromBadRequestResponse(ClientResponse response) {
        String json = response.getEntity(String.class);
        Gson gson = new Gson();
        return gson.fromJson(json, BadRequestMock.class);
    }
    public class BadRequestMock {
        public String status;
        public String message;
        public String[] errors;

    }
  public static String generateUniqueFileID() {
    return  RandomStringUtils.randomAlphanumeric(1025).toLowerCase();
  }

}