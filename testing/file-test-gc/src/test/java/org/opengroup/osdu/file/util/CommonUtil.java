package org.opengroup.osdu.file.util;

import java.util.logging.Logger;

import org.opengroup.osdu.file.constants.TestConstants;

public class CommonUtil {

	public static String selectTenant(String tenant) {
		
		final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

		switch (tenant) {
		case "PRIVATE_TENANT1":
			tenant = TestConstants.PRIVATE_TENANT1;
			break;
		case "PRIVATE_TENANT2":
			tenant = TestConstants.PRIVATE_TENANT2;
			break;
		case "SHARED_TENANT":
			tenant = TestConstants.SHARED_TENANT;
			break;
		default:
			LOGGER.info("Invalid tenant");
		}
		return tenant;
	}
}
