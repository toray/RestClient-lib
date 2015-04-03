package com.toraysoft.tools.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
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

	private static String PAGE_CACHE = "api.page";
	private static String PRE_CACHE = PAGE_CACHE + "=1";

	private Context mContext;
	private String host;// api host
	private OnHostErrorCallback mOnHostErrorCallback = null;
	private CacheUtil cacheUtil;
	private RequestQueue mQueue;
	private RestHeader defaultHeader;
	private boolean isDebug = false;
	private boolean isRandom = false;
	private String user = "";
	private String proxyHost;
	private int proxyPort;

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

	private void d(String msg) {
		if (isDebug)
			Log.d("RestClient", msg);
	}
	
	private <T> void send(final RestRequest req, final RequestListener<T> l) {
		this.send(req, false, l);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> void send(final RestRequest req, boolean isForm, final RequestListener<T> l) {

		String requestBody = "";
		if (req.getMethod() == Method.POST && req.getParams() != null) {
			if(isForm){
				StringBuffer sb = new StringBuffer();
				Map<String, String> map = req.getParams().toMap();
				Set<Entry<String, String>>  entrys = map.entrySet();
				for (Entry<String, String> e : entrys) {
					try {
						sb.append(e.getKey());
						sb.append("=");
						sb.append(URLEncoder.encode(e.getValue(), "UTF-8"));
						sb.append("&");
					} catch (UnsupportedEncodingException e1) {
						e1.printStackTrace();
					}
				}
				sb.deleteCharAt(sb.length() - 1);
				requestBody = sb.toString();
			}else{
				requestBody = req.getParams().toJSONObject().toString();
			}
		}
		String fullUrl = req.getFullUrl();
		if (isRandom()) {
			long r = Math.round(Math.random() * 99999999);
			fullUrl = fullUrl + (fullUrl.contains("?") ? "&" : "?") + "random="
					+ r;
		}
		d("Request url: " + fullUrl);
		final String furl = fullUrl;
		RestRequest.ExJSONRequest jsonRequest = new RestRequest.ExJSONRequest(
				req.getMethod(), fullUrl, requestBody,
				new Response.Listener<T>() {

					@Override
					public void onResponse(T response) {
						d("Response from URL [" + furl + "] : \n"
								+ response.toString());
						if (l != null) {
							l.onResponse(response);
						}
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						if (error instanceof NoConnectionError) {
							if(error.getMessage() != null && error.getMessage().contains("UnknownHostException")) {
								if (getHostErrorListener() != null) {
									getHostErrorListener()
											.onChangHost(host);
								}
							}
						}
						if (l != null) {
							l.onErrorResponse(error);
						}
					}

				});
		if(isForm){
			jsonRequest.setBodyContentType("application/x-www-form-urlencoded");
		}
		if (req.getParams() != null) {
			jsonRequest.setParams(req.getParams().toMap());
		}
		if (req.getHeaders() != null) {
			jsonRequest.setHeaders(req.getHeaders().toMap());
		}
		jsonRequest.setShouldCache(req.isCache());
		getQueue().add(jsonRequest);
	}
	
	public <T> void send(final RestRequest req, final OnResponseCallback<T> l) {
		this.send(req, false, l);
	}

	@SuppressWarnings("unchecked")
	public <T> void send(final RestRequest req, boolean isForm, final OnResponseCallback<T> l) {

		req.setHost(getRestClientHost());
		String url = req.getFullUrl();

		final String cacheKey = user + url.hashCode();
		if (req.isCache()) {
			boolean isHasPage = url.contains(PAGE_CACHE);
			boolean isFirstPage = url.contains(PRE_CACHE);
			if (!isHasPage || isFirstPage) {
				if (getCacheUtil() != null) {
					Object cache = getCacheUtil().getJSONCache(cacheKey);
					if (cache != null) {
						l.onCache((T) cache);
					}
				}
			}
		}
		send(req, isForm, new RequestListener<T>() {

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
		this.doPost(url, params, false, l);
	}
	
	public <T> void doPost(String url, RestParameter params, boolean isForm,
			final OnResponseCallback<T> l) {
		RestRequest req = new RestRequest(mContext, Method.POST, url);
		if (params != null)
			req.setParams(params);
		if (defaultHeader != null)
			req.setHeaders(defaultHeader);
		req.setCache(false);
		send(req, isForm, l);
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
			if(!TextUtils.isEmpty(proxyHost) && proxyPort > 0) {
				mQueue = Volley.newRequestQueue(mContext, proxyHost, proxyPort);
				if (isDebug)
					System.out.println("*****************************>>>create by proxy!!!!!!!");
			} else {
				mQueue = Volley.newRequestQueue(mContext);
				if (isDebug)
					System.out.println("*****************************>>>create not by proxy!!!!!!!");
			}
		}
		if (isDebug) {
			if(proxyPort > 0) {
				System.out.println("==============>>>using proxy!!!!!!!!");
			}	
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

	public boolean isDebug() {
		return isDebug;
	}

	public void setDebug(boolean isDebug) {
		this.isDebug = isDebug;
	}

	public boolean isRandom() {
		return isRandom;
	}

	public void setRandom(boolean isRandom) {
		this.isRandom = isRandom;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
	
	public void setProxy(String proxyHost, int proxyPort) {
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
		this.mQueue = null;
	}
	
	public void setProxy(String proxyHost) {
		this.setProxy(proxyHost, 80);
	}
	
}
