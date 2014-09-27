package com.toraysoft.tools.rest;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.text.TextUtils;

import com.toraysoft.tools.rest.encrypt.Base64;
import com.toraysoft.tools.rest.encrypt.SHA1;

public class RestHeader {

	/*
	 * API header 配置
	 */
	private String key;
	private String secret;
	// 用户登录加密信息
	private String rand;
	private String token;
	private String username;
	private String schema;// header字段
	private String clientVersion;
	// 推送配置
	private String apnsKey;
	private String apnsSecret;
	private String apiVersion = "1.0";

	public RestHeader() {

	}

	// appkey,appsecret
	public void setRestHeaderKeys(String key, String secret) {
		this.key = key;
		this.secret = secret;
	}

	// apnskey,apnssecret
	public void setRestHeaderPushKeys(String apnsKey, String apnsSecret) {
		this.apnsKey = apnsKey;
		this.apnsSecret = apnsSecret;
	}

	// user's rand,token,username
	public void setRestHeaderParams(String rand, String token, String username) {
		this.rand = rand;
		this.token = token;
		this.username = username;
	}
	
	// header schema
	public void setRestHeaderSchema(String schema, String clientVersion) {
		this.schema = schema;
		this.clientVersion = clientVersion;
	}
	
	// api version
	public void setRestHeaderApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	// api basic header
	public List<BasicNameValuePair> getBasicHeader() {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setGroupingUsed(false);
		String time = nf.format(System.currentTimeMillis() / 1000.0);

		List<BasicNameValuePair> headers = new ArrayList<BasicNameValuePair>();
		headers.add(new BasicNameValuePair("X-" + schema + "-API-KEY", key));
		headers.add(new BasicNameValuePair("X-" + schema + "-API-TIMESTAMP",
				time));
		headers.add(new BasicNameValuePair("X-" + schema + "-API-VERSION",
				apiVersion));
		headers.add(new BasicNameValuePair("X-" + schema + "-CLIENT-OS",
				"android"));
		headers.add(new BasicNameValuePair("X-" + schema + "-CLIENT-VERSION",
				clientVersion));

		String sign = key + "&" + secret + "&" + apiVersion + "&" + time;

		headers.add(new BasicNameValuePair("X-" + schema + "-API-SIGNATURE",
				new SHA1().getDigestOfString(sign.getBytes())));
		if (!TextUtils.isEmpty(rand) && !TextUtils.isEmpty(token)
				&& !TextUtils.isEmpty(username)) {
			String auth = key + "&" + secret + "&" + rand.trim() + "&"
					+ token.trim() + "&" + time;

			auth = username.trim() + ":"
					+ new SHA1().getDigestOfString(auth.getBytes());

			auth = new String(Base64.encode(auth.getBytes()));
			auth = "signature " + auth;
			headers.add(new BasicNameValuePair("AUTHORIZATION", auth));
		}
		return headers;
	}

	// add other header filed to header
	public List<BasicNameValuePair> getExHeader(
			List<BasicNameValuePair> exHeaders) {
		List<BasicNameValuePair> headers = getBasicHeader();
		headers.addAll(exHeaders);
		return headers;
	}

	// apns header
	public List<BasicNameValuePair> getPushHeader() {
		String timestamp = "" + System.currentTimeMillis();

		List<BasicNameValuePair> headers = new ArrayList<BasicNameValuePair>();
		headers.add(new BasicNameValuePair("X-APNSAGENT-API-KEY", apnsKey));
		headers.add(new BasicNameValuePair("X-APNSAGENT-API-VERSION", "1.0"));
		headers.add(new BasicNameValuePair("X-APNSAGENT-API-SIGNATURE",
				generatePushToken(timestamp)));
		headers.add(new BasicNameValuePair("X-APNSAGENT-API-TIMESTAMP",
				timestamp));
		return headers;
	}

	// apns header token
	private String generatePushToken(String timestamp) {
		return new SHA1()
				.getDigestOfString((apnsKey + "&" + apnsSecret + "&" + timestamp)
						.getBytes());
	}
}
