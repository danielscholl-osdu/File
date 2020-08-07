/*
 * Copyright 2020 Google LLC
 * Copyright 2020 EPAM Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.file;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.MediaType;
import org.opengroup.osdu.file.apitest.Config;

public abstract class HttpClient {

  protected static String accessToken;
  protected static String noDataAccessToken;

  public abstract String getAccessToken() throws IOException;

  public abstract String getNoDataAccessToken() throws IOException;

  public ClientResponse send(String path, String httpMethod, Map<String, String> headers,
      String requestBody)
      throws Exception {

    Client client = this.getClient();

    String mergedURL = new URL(Config.getFileServiceHost() + path).toString();
    System.out.println(String.format("calling %s API:%s", httpMethod, mergedURL));
    System.out.println(String.format("request body:%s", requestBody));

    if (requestBody != null) {
      headers.put("Content-Length", Long.toString(requestBody.length()));
    } else {
      headers.put("Content-Length", "0");
    }

    WebResource webResource = client.resource(mergedURL);

    WebResource.Builder builder = webResource.accept(MediaType.APPLICATION_JSON)
        .type(MediaType.APPLICATION_JSON);
    headers.forEach(builder::header);

    return builder.method(httpMethod, ClientResponse.class, requestBody);
  }


  private Client getClient() throws Exception {
    TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
      @Override
      public X509Certificate[] getAcceptedIssuers() {
        return null;
      }

      @Override
      public void checkClientTrusted(X509Certificate[] certs, String authType) {
      }

      @Override
      public void checkServerTrusted(X509Certificate[] certs, String authType) {
      }
    }};

    try {
      SSLContext sc = SSLContext.getInstance("TLS");
      sc.init(null, trustAllCerts, new SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    } catch (Exception e) {
      throw new Exception();
    }

    return Client.create();
  }

}
