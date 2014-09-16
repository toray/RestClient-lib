package com.toraysoft.tools.rest;

import java.io.File;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.toraysoft.tools.rest.RestCallback.OnRestCallback;
import com.toraysoft.tools.rest.RestParameter.REQUEST_METHOD;
import com.toraysoft.tools.rest.cache.CacheUtil;
import com.toraysoft.tools.rest.image.ImageUtil;

public class RestClient {

	private Context mContext;
	private CacheUtil mCacheUtil;
	private RestHeader mRestHeader;
	private ImageUtil mImageUtil;
	private RestRequest mRestRequest;
	private String restHost = "";

	private RestClient() {

	}

	public RestClient(Context ctx, String hostName) {
		mContext = ctx;
		restHost = hostName;
		mRestHeader = new RestHeader();
		mRestRequest = new RestRequest(ctx, this);
		mCacheUtil = new CacheUtil();
		mImageUtil = new ImageUtil(this);
	}

	public void initCacheDir(File cacheDir) {
		if (mCacheUtil != null)
			mCacheUtil.initCacheDir(cacheDir);
	}

	public void setRestHeaderKeys(String key, String secret) {
		if (mRestHeader != null)
			mRestHeader.setRestHeaderKeys(key, secret);
	}

	public void setRestHeaderPushKeys(String apnsKey, String apnsSecret) {
		if (mRestHeader != null)
			mRestHeader.setRestHeaderPushKeys(apnsKey, apnsSecret);
	}

	public void setRestHeaderSchema(String schema, String clientVersion) {
		if (mRestHeader != null)
			mRestHeader.setRestHeaderSchema(schema, clientVersion);
	}

	public void setRestHeaderSign(String rand, String token, String username) {
		if (mRestHeader != null)
			mRestHeader.setRestHeaderParams(rand, token, username);
	}

	public Context getContext() {
		return mContext;
	}

	public CacheUtil getCacheUtil() {
		return mCacheUtil;
	}

	public ImageUtil getImageUtil() {
		return mImageUtil;
	}

	public RestHeader getRestHeader() {
		return mRestHeader;
	}

	public String getRestHost() {
		return restHost;
	}

	// doGet with normal header, none param
	public void doGet(String url, OnRestCallback l) {
		doGet(null, null, url, l);
	}

	// doGet with other header, none param
	public void doGet(Map<String, String> headers, String url, OnRestCallback l) {
		doGet(headers, null, url, l);
	}
	
	// doPost with normal header,none param
	public void doPost(String url, OnRestCallback l) {
		doPost(null, null, url, l);
	}

	// doPost with other header,none param
	public void doPost(Map<String, String> headers, String url, OnRestCallback l) {
		doPost(headers, null, url, l);
	}

	// doPost with normal header,param
	public void doPost(String url, Map<String, String> params, OnRestCallback l) {
		doPost(null, params, url, l);
	}

	// doPost with other header,param
	public void doPost(Map<String, String> headers, String url,
			Map<String, String> params, OnRestCallback l) {
		doPost(headers, params, url, l);
	}

	// doPost with normal header,JSONObject param
	public void doPost(String url, JSONObject param, OnRestCallback l) {
		doPost(null, param, url, l);
	}

	// doPost with other header,JSONObject param
	public void doPost(Map<String, String> headers, String url,
			JSONObject param, OnRestCallback l) {
		doPost(headers, param, url, l);
	}

	// doPost with normal header, JSONArray param
	public void doPost(String url, JSONArray param, OnRestCallback l) {
		doPost(null, param, url, l);
	}

	// doPost with other header, JSONArray param
	public void doPost(Map<String, String> headers, String url,
			JSONArray param, OnRestCallback l) {
		doPost(headers, param, url, l);
	}

	private void doGet(Map<String, String> headers, Object param, String url,
			OnRestCallback l) {
		mRestRequest.doMethodHelper(REQUEST_METHOD.GET, headers, param, url, l);
	}

	private void doPost(Map<String, String> headers, Object param, String url,
			OnRestCallback l) {
		mRestRequest
				.doMethodHelper(REQUEST_METHOD.POST, headers, param, url, l);
	}

}
