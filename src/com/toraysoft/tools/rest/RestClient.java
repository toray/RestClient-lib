package com.toraysoft.tools.rest;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.android.volley.NoConnectionError;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.toraysoft.tools.rest.RestCallback.OnHostErrorCallback;
import com.toraysoft.tools.rest.RestCallback.OnResponseCallback;
import com.toraysoft.tools.rest.RestCallback.RequestListener;
import com.toraysoft.utils.cache.CacheUtil;

public class RestClient {

	private static String PRE_CACHE = "api.page=1";

	private Context mContext;
	private String host;// api host
	private OnHostErrorCallback mOnHostErrorCallback = null;
	private CacheUtil cacheUtil;
	private RequestQueue mQueue;
	private RestHeader defaultHeader;

	@SuppressWarnings("unused")
	private RestClient() {
	}

	public RestClient(Context ctx, OnHostErrorCallback l) {
		mContext = ctx;
		this.mOnHostErrorCallback = l;
		if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
			cacheUtil = new CacheUtil(ctx.getExternalCacheDir());
		} else {
			cacheUtil = new CacheUtil(ctx.getCacheDir());
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> void send(final RestRequest req, final RequestListener<T> l) {

		String requestBody = "";
		if (req.getMethod() == Method.POST && req.getParams() != null) {
			requestBody = req.getParams().toJSONObject().toString();
		}
		RestRequest.ExJSONRequest jsonRequest = new RestRequest.ExJSONRequest(
				req.getMethod(), req.getFullUrl(), requestBody,
				new Response.Listener<T>() {

					@Override
					public void onResponse(T response) {
						if (l != null) {
							l.onResponse(response);
						}
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						if (error instanceof NoConnectionError) {
							if (getHostErrorListener() != null) {
								getHostErrorListener()
										.onChangHost(req.getUrl());
							}
						}
						if (l != null) {
							l.onErrorResponse(error);
						}
					}

				});
		if (req.getParams() != null) {
			jsonRequest.setParams(req.getParams().toMap());
		}
		if (req.getHeaders() != null) {
			jsonRequest.setHeaders(req.getHeaders().toMap());
		}
		jsonRequest.setShouldCache(req.isCache());
		getQueue().add(jsonRequest);
	}

	@SuppressWarnings("unchecked")
	public <T> void send(final RestRequest req, final OnResponseCallback<T> l) {

		req.setHost(getRestClientHost());
		String url = req.getFullUrl();

		final String cacheKey = url.hashCode() + "";
		if (req.isCache()) {
			boolean isFirstPage = url.contains(PRE_CACHE);
			if (isFirstPage) {
				if (getCacheUtil() != null) {
					Object cache = getCacheUtil().getJSONCache(cacheKey);
					if (cache != null) {
						l.onCache((T) cache);
					}
				}
			}
		}
		send(req, new RequestListener<T>() {

			@Override
			public void onResponse(T response) {
				if (response != null) {
					if (l != null) {
						l.onSuccess(response);
						if (req.isCache() && getCacheUtil() != null) {
							getCacheUtil().putJSONCache(cacheKey, response);
						}
					}
					return;
				}
			}

			@Override
			public void onErrorResponse(VolleyError error) {
				if (l != null) {
					String errmsg = "";
					if (error.networkResponse != null) {
						errmsg = new String(error.networkResponse.data);
					} else {
						errmsg = error.getMessage();
					}
					Log.d("RESTRequest", errmsg + "");
					l.onError(errmsg);
					if (req.isCache() && getCacheUtil() != null) {
						Object cache = getCacheUtil().getJSONCache(cacheKey);
						if (cache != null) {
							l.onCache((T) cache);
						}
					}
				}
			}
		});
	}

	public <T> void doGet(String url, RestParameter params,
			final OnResponseCallback<T> l) {
		doGet(url, params, l, true);
	}

	public <T> void doGet(String url, RestParameter params,
			final OnResponseCallback<T> l, boolean isCache) {
		RestRequest req = new RestRequest(mContext, Method.GET, url);
		if (params != null)
			req.setParams(params);
		if (defaultHeader != null)
			req.setHeaders(defaultHeader);
		req.setCache(isCache);
		send(req, l);
	}

	public <T> void doPost(String url, RestParameter params,
			final OnResponseCallback<T> l) {
		RestRequest req = new RestRequest(mContext, Method.POST, url);
		if (params != null)
			req.setParams(params);
		if (defaultHeader != null)
			req.setHeaders(defaultHeader);
		req.setCache(false);
		send(req, l);
	}

	public void setRestHost(String host) {
		this.host = host;
	}

	public Context getContext() {
		return mContext;
	}

	public String getRestClientHost() {
		return host;
	}

	public OnHostErrorCallback getHostErrorListener() {
		return mOnHostErrorCallback;
	}

	private RequestQueue getQueue() {
		if (mQueue == null) {
			mQueue = Volley.newRequestQueue(mContext);
		}
		return mQueue;
	}

	public CacheUtil getCacheUtil() {
		return cacheUtil;
	}

	public RestHeader getDefaultHeader() {
		return defaultHeader;
	}

	public void setDefaultHeader(RestHeader defaultHeader) {
		this.defaultHeader = defaultHeader;
	}

}
