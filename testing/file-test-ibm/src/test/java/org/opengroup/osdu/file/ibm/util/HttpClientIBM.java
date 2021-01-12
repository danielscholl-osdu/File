package org.opengroup.osdu.file.ibm.util;
import java.io.IOException;

import org.opengroup.osdu.core.ibm.util.IdentityClient;
import org.opengroup.osdu.file.HttpClient;

import com.google.common.base.Strings;

public class HttpClientIBM extends HttpClient {
	private static String token;
	private static String noDataAccesstoken;

	@Override
	public synchronized String getAccessToken() throws IOException {
		if (Strings.isNullOrEmpty(token)) {
			token = IdentityClient.getTokenForUserWithAccess();
		}
		return "Bearer " + token;
	}

	@Override
	public synchronized String getNoDataAccessToken() throws IOException {
		if (Strings.isNullOrEmpty(noDataAccesstoken)) {
			noDataAccesstoken = IdentityClient.getTokenForUserWithNoAccess();
		}
		return "Bearer " + noDataAccesstoken;
	}

}