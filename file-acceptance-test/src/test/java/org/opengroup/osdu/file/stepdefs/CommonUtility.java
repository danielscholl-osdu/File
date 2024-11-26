/*
 *  Copyright 2020-2022 Google LLC
 *  Copyright 2020-2022 EPAM Systems, Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opengroup.osdu.file.stepdefs;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import static org.awaitility.Awaitility.await;
import org.opengroup.osdu.file.constants.TestConstants;
import org.opengroup.osdu.file.util.AuthUtil;

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

  public static void customStaticWait_Timeout_Minutes(long timeout) {
    int expiryTime = Integer.parseInt(getSignedURLExpiryTime(String.valueOf(timeout)));
    // adding an extra minute to avoid race condition between the actual wait and configured wait in Awaitility, so that actual wait time is always less.
    Awaitility.setDefaultTimeout(Duration.ONE_MINUTE.multiply(expiryTime).plus(Duration.ONE_MINUTE));
    await().pollDelay(expiryTime, TimeUnit.MINUTES).until(() -> true);
  }

  public static String getSignedURLExpiryTime(String expiryTimeInMinutes) {
    return StringUtils.isNotEmpty(TestConstants.SIGNED_URL_EXPIRY_TIME_MINUTES) ? TestConstants.SIGNED_URL_EXPIRY_TIME_MINUTES : expiryTimeInMinutes;
  }
}
