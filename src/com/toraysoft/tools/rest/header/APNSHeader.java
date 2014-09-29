package com.toraysoft.tools.rest.header;

import com.toraysoft.tools.rest.RestHeader;
import com.toraysoft.utils.encrypt.SHA1;

public class APNSHeader extends RestHeader {

	/*
	 * API header
	 */
	private String apnsKey;
	private String apnsSecret;

	public APNSHeader(String apnsKey, String apnsSecret) {
		super();
		this.apnsKey = apnsKey;
		this.apnsSecret = apnsSecret;

		String timestamp = "" + System.currentTimeMillis();

		headers.put("X-APNSAGENT-API-KEY", apnsKey);
		headers.put("X-APNSAGENT-API-VERSION", "1.0");
		headers.put("X-APNSAGENT-API-SIGNATURE", generatePushToken(timestamp));
		headers.put("X-APNSAGENT-API-TIMESTAMP", timestamp);
	}

	// apns header token
	private String generatePushToken(String timestamp) {
		return new SHA1()
				.getDigestOfString((apnsKey + "&" + apnsSecret + "&" + timestamp)
						.getBytes());
	}
}
