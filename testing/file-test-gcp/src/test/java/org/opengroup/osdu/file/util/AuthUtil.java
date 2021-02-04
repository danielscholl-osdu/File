package org.opengroup.osdu.file.util;

import com.google.common.base.Strings;

public class AuthUtil {

  public synchronized String getToken() throws Exception {
    String token = null;
    if (Strings.isNullOrEmpty(token)) {
      String serviceAccountFile = System
          .getProperty("INTEGRATION_TESTER", System.getenv("INTEGRATION_TESTER"));
      String audience = System.getProperty("INTEGRATION_TEST_AUDIENCE",
          System.getenv("INTEGRATION_TEST_AUDIENCE"));
      token = new GoogleServiceAccount(serviceAccountFile).getAuthToken(audience);
    }
    System.out.println("Bearer " + token);
    return "Bearer " + token;
  }
}
