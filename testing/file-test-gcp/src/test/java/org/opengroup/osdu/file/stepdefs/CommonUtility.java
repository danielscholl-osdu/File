package org.opengroup.osdu.file.stepdefs;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.opengroup.osdu.file.constants.TestConstants;
import org.opengroup.osdu.file.util.AuthUtil;
import org.opengroup.osdu.file.util.CommonUtil;

public class CommonUtility {

	private static final String FILE_ID = "file-integration-test-";
	private static String AUTH_TOKEN = null;

	public static String generateUniqueFileID() {
		return FILE_ID + RandomStringUtils.randomAlphanumeric(10).toLowerCase();
	}

	public static Map<String, String> getHeaderWithoutPartiton() throws Exception {
		if (AUTH_TOKEN == null) {
			AUTH_TOKEN = new AuthUtil().getToken();
		}
		Map<String, String> authHeaders = new HashMap<String, String>();
		authHeaders.put(TestConstants.AUTHORIZATION, AUTH_TOKEN);
		authHeaders.put(TestConstants.CONTENT_TYPE, TestConstants.JSON_CONTENT);
		return authHeaders;
	}

	public static Map<String, String> getHeaderWithoutAuthToken() throws Exception {
		if (AUTH_TOKEN == null) {
			AUTH_TOKEN = new AuthUtil().getToken();
		}
		Map<String, String> authHeaders = new HashMap<String, String>();
		authHeaders.put(TestConstants.DATA_PARTITION_ID, TestConstants.PRIVATE_TENANT1);
		authHeaders.put(TestConstants.CONTENT_TYPE, TestConstants.JSON_CONTENT);
		return authHeaders;
	}

	public static Map<String, String> getValidHeader() throws Exception {
		if (AUTH_TOKEN == null) {
			AUTH_TOKEN = new AuthUtil().getToken();
		}
		Map<String, String> authHeaders = prepareHeaderMap(AUTH_TOKEN, TestConstants.PRIVATE_TENANT1);
		return authHeaders;
	}

	public static Map<String, String> getHeaderWithVaidAuthorizationForPartiton(String partition) throws Exception {
		if (AUTH_TOKEN == null) {
			AUTH_TOKEN = new AuthUtil().getToken();
		}
		Map<String, String> authHeaders = prepareHeaderMap(AUTH_TOKEN, partition);
		return authHeaders;
	}

	public static Map<String, String> getHeaderWithInvaidAuthorizationForPartiton(String partition) throws Exception {
		if (AUTH_TOKEN == null) {
			AUTH_TOKEN = new AuthUtil().getToken();
		}
		Map<String, String> authHeaders = prepareHeaderMap(AUTH_TOKEN + "invalid", partition);
		return authHeaders;
	}

	private static Map<String, String> prepareHeaderMap(String authToken, String partition) {
		Map<String, String> authHeaders = new HashMap<String, String>();
		authHeaders.put(TestConstants.AUTHORIZATION, authToken);
		authHeaders.put(TestConstants.DATA_PARTITION_ID, partition);
		authHeaders.put(TestConstants.CONTENT_TYPE, TestConstants.JSON_CONTENT);
		return authHeaders;
	}

	public static String generateFileIDExceedingLegthLimit() {
		return RandomStringUtils.randomAlphanumeric(1025).toLowerCase();
	}
}
