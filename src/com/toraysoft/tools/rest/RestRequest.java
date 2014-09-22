package com.toraysoft.tools.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.toraysoft.tools.rest.RestCallback.OnRestCallback;
import com.toraysoft.tools.rest.RestCallback.RequestListener;
import com.toraysoft.tools.rest.RestParameter.HEADER_TYPE;

public class RestRequest {

	private static int DEFAULT_TIMEOUT = 15000;// request timeout 30 seconds
	private static String PRE_CACHE = "api.page=1";

	private RequestQueue mQueue;

	private Context mContext;

	private RestClient mRestClient;

	private HEADER_TYPE header_type;

	public RestRequest(Context context, RestClient client) {
		mContext = context;
		mRestClient = client;
	}

	private RequestQueue getQueue() {
		if (mQueue == null) {
			mQueue = Volley.newRequestQueue(mContext);
		}
		return mQueue;
	}

	private static class ExStringRequest extends StringRequest {

		Map<String, String> headers;
		Map<String, String> params;
		byte[] body;
		String contentType;

		public ExStringRequest(int method, String url,
				Listener<String> listener, ErrorListener errorListener) {
			super(method, url, listener, errorListener);
			setRetryPolicy(new DefaultRetryPolicy(DEFAULT_TIMEOUT,
					DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
					DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		}

		public ExStringRequest(String url, Listener<String> listener,
				ErrorListener errorListener) {
			super(url, listener, errorListener);
			setRetryPolicy(new DefaultRetryPolicy(DEFAULT_TIMEOUT,
					DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
					DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		}

		public void setHeaders(Map<String, String> headers) {
			this.headers = headers;
		}

		public void setParams(Map<String, String> params) {
			this.params = params;
		}

		public void setBody(byte[] body) {
			this.body = body;
		}

		@Override
		protected Map<String, String> getParams() throws AuthFailureError {
			return params;
		}

		@Override
		public byte[] getBody() throws AuthFailureError {
			return body == null ? super.getBody() : body;
		}

		@Override
		public Map<String, String> getHeaders() throws AuthFailureError {
			return headers;
		}

		public void setBodyContentType(String contentType) {
			this.contentType = contentType;
		}

		@Override
		public String getBodyContentType() {
			return contentType == null ? super.getBodyContentType()
					: contentType;
		}

	}

	private void doMethod(int method, Map<String, String> headers,
			Object param, final String url, final RequestListener<String> l) {
		ExStringRequest stringRequest = new ExStringRequest(method, url,
				new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						if (l != null) {
							l.onResponse(response);
						}
					}
				}, new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						if (error instanceof NoConnectionError) {
							if (mRestClient.getHostErrorListener() != null) {
								mRestClient.getHostErrorListener().onChangHost(
										url);
							}
						}
						if (l != null) {
							l.onErrorResponse(error);
						}
					}

				});
		if (param != null) {
			if (param instanceof Map) {
				stringRequest.setParams((Map<String, String>) param);
			} else if (param instanceof JSONObject
					|| param instanceof JSONArray) {
				stringRequest.setBody(param.toString().getBytes());
				stringRequest
						.setBodyContentType("application/json; charset=UTF-8");
			}
		}
		if (header_type == HEADER_TYPE.APNS_PUSH) {
			List<BasicNameValuePair> headerList = mRestClient.getRestHeader()
					.getPushHeader();
			Map<String, String> pushheaders = new HashMap<String, String>();
			for (NameValuePair nameValuePair : headerList) {
				pushheaders.put(nameValuePair.getName(),
						nameValuePair.getValue());
			}
			doRequest(stringRequest, pushheaders);
		} else {
			if (headers != null) {
				doRequest(stringRequest, headers);
			} else {
				doRequest(stringRequest);
			}
		}
	}

	// doRequest with normal header
	private void doRequest(ExStringRequest stringRequest) {
		List<BasicNameValuePair> headerList = mRestClient.getRestHeader()
				.getBasicHeader();
		Map<String, String> headers = new HashMap<String, String>();
		for (NameValuePair nameValuePair : headerList) {
			headers.put(nameValuePair.getName(), nameValuePair.getValue());
		}
		stringRequest.setHeaders(headers);
		getQueue().add(stringRequest);
	}

	// doRequest with other header
	private void doRequest(ExStringRequest stringRequest,
			Map<String, String> headers) {
		stringRequest.setHeaders(headers);
		getQueue().add(stringRequest);
	}

	public void doMethodHelper(int method, Map<String, String> headers,
			Object param, String url, final OnRestCallback l) {
		url = mRestClient.getRestClientHost() + url;
		boolean isFirstPage = url.contains(PRE_CACHE);
		final String cacheKey = url.hashCode() + "";
		if (isFirstPage) {
			if (mRestClient.getCacheUtil() != null) {
				String cache = mRestClient.getCacheUtil().getStringCache(
						cacheKey);
				if (!TextUtils.isEmpty(cache)) {
					l.onCache(cache);
				}
			}
		}
		doMethod(method, headers, param, url, new RequestListener<String>() {

			@Override
			public void onResponse(String response) {
				if (!TextUtils.isEmpty(response)) {
					if (l != null) {
						l.onSuccess(response);
						if (mRestClient.getCacheUtil() != null) {
							mRestClient.getCacheUtil().putStringCache(cacheKey,
									response);
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
					if (mRestClient.getCacheUtil() != null) {
						String cache = mRestClient.getCacheUtil()
								.getStringCache(cacheKey);
						if (!TextUtils.isEmpty(cache)) {
							l.onCache(cache);
						}
					}
				}
			}
		});
	}

	public void setHeaderType(HEADER_TYPE type) {
		this.header_type = type;
	}
}
