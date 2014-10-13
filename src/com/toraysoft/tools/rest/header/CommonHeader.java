package com.toraysoft.tools.rest.header;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.message.BasicNameValuePair;

import android.text.TextUtils;

import com.toraysoft.tools.rest.RestHeader;
import com.toraysoft.utils.encrypt.Base64;
import com.toraysoft.utils.encrypt.SHA1;

public class CommonHeader extends RestHeader {

	/*
	 * API header
	 */
	private String key;
	private String secret;

	private String rand = "";
	private String token = "";
	private String username = "";
	private String schema = "";
	private String clientVersion = "";

	private String apiVersion = "2";

	public CommonHeader(String key, String secret) {
		super();
		this.key = key;
		this.secret = secret;
		this.schema = "MUSIC";
	}

	// user's rand,token,username
	public void setRestHeaderParams(String rand, String token, String username) {
		this.rand = rand;
		this.token = token;
		this.username = username;
	}

	// header schema
	public void setRestHeaderSchema(String clientVersion) {
		this.clientVersion = clientVersion;
	}

	// api version
	public void setRestHeaderApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	@Override
	public Map<String, String> toMap() {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setGroupingUsed(false);
		String time = nf.format(System.currentTimeMillis() / 1000.0);

		headers.put("X-" + schema + "-API-KEY", key);
		headers.put("X-" + schema + "-API-TIMESTAMP", time);
		headers.put("X-" + schema + "-API-VERSION", apiVersion);
		headers.put("X-" + schema + "-CLIENT-OS", "android");
		headers.put("X-" + schema + "-CLIENT-VERSION", clientVersion);

		String sign = key + "&" + secret + "&" + apiVersion + "&" + time;

		headers.put("X-" + schema + "-API-SIGNATURE",
				new SHA1().getDigestOfString(sign.getBytes()));
		if (!TextUtils.isEmpty(rand) && !TextUtils.isEmpty(token)
				&& !TextUtils.isEmpty(username)) {
			String auth = key + "&" + secret + "&" + rand.trim() + "&"
					+ token.trim() + "&" + time;

			auth = username.trim() + ":"
					+ new SHA1().getDigestOfString(auth.getBytes());

			auth = new String(Base64.encode(auth.getBytes()));
			auth = "signature " + auth;
			headers.put("AUTHORIZATION", auth);
		}
		return super.toMap();
	}

}
