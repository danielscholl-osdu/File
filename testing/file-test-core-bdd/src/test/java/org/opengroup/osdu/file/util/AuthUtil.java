package org.opengroup.osdu.file.util;

import com.google.common.base.Strings;

public class AuthUtil {
	public synchronized String getToken() throws Exception {
		String token = null;
		String vendor = System.getProperty("VENDOR", System.getenv("VENDOR"));
		if (Strings.isNullOrEmpty(token) && vendor.equals("gcp")) {
			String serviceAccountFile = System.getProperty("INTEGRATION_TESTER", System.getenv("INTEGRATION_TESTER"));
			String audience = System.getProperty("INTEGRATION_TEST_AUDIENCE",
					System.getenv("INTEGRATION_TEST_AUDIENCE"));
		} else if ("aws".equals(vendor)) {
			System.out.println("Token generation code for aws comes here");
		} else if ("azure".equals(vendor)) {
			System.out.println("Token generation code for azure comes here");
		} else if ("ibm".equals(vendor)) {
			System.out.println("Token generation code for ibm comes here");
		}
		System.out.println("Bearer " + token);
		return "Bearer " + token;
	}
}
