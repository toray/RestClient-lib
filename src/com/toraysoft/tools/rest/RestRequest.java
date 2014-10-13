package com.toraysoft.tools.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

public class RestRequest {

	private static int DEFAULT_TIMEOUT = 15000;// request timeout 30 seconds

	private Context mContext;

	private RestClient mRestClient;

	private int method;
	String url;
	RestHeader headers;
	RestParameter params;
	String host;
	boolean isCache = false;

	public RestRequest(Context context, int method, String url) {
		this.mContext = context;
		this.method = method;
		this.url = url;
	}

	static class ExJSONRequest<T> extends JsonRequest<T> {

		public ExJSONRequest(int method, String url, String requestBody,
				Listener<T> listener, ErrorListener errorListener) {
			super(method, url, requestBody, listener, errorListener);
			setRetryPolicy(new DefaultRetryPolicy(DEFAULT_TIMEOUT,
					DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
					DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
			headers = new RestHeader();
			params = new RestParameter();
		}

		RestHeader headers;
		RestParameter params;
		byte[] body;
		String contentType = "application/json";

		public void setHeaders(Map<String, String> headers) {
			this.headers.pullMap(headers);
		}

		public void setParams(Map<String, String> params) {
			this.params.pullMap(params);
		}

		public void setBody(byte[] body) {
			this.body = body;
		}

		@Override
		protected Map<String, String> getParams() throws AuthFailureError {
			return params.toMap();
		}

		@Override
		public byte[] getBody() {
			return body == null ? super.getBody() : body;
		}

		@Override
		public Map<String, String> getHeaders() throws AuthFailureError {
			return headers.toMap();
		}

		public void setBodyContentType(String contentType) {
			this.contentType = contentType;
		}

		@Override
		public String getBodyContentType() {
			return contentType == null ? super.getBodyContentType()
					: contentType;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected Response<T> parseNetworkResponse(NetworkResponse response) {
			String resp_data = (new String(response.data)).trim();
			if (resp_data.startsWith("[") && resp_data.endsWith("]")) {
				try {
					String jsonString = new String(response.data,
							HttpHeaderParser.parseCharset(response.headers));
					return (Response<T>) Response.success(new JSONArray(
							jsonString), HttpHeaderParser
							.parseCacheHeaders(response));
				} catch (UnsupportedEncodingException e) {
					return Response.error(new ParseError(e));
				} catch (JSONException je) {
					return Response.error(new ParseError(je));
				}
			} else {
				try {
					String jsonString = new String(response.data,
							HttpHeaderParser.parseCharset(response.headers));
					return (Response<T>) Response.success(new JSONObject(
							jsonString), HttpHeaderParser
							.parseCacheHeaders(response));
				} catch (UnsupportedEncodingException e) {
					return Response.error(new ParseError(e));
				} catch (JSONException je) {
					return Response.error(new ParseError(je));
				}
			}
		}
	}

	public RestHeader getHeaders() {
		return headers;
	}

	public void setHeaders(RestHeader headers) {
		this.headers = headers;
	}

	public RestParameter getParams() {
		return params;
	}

	public void setParams(RestParameter params) {
		this.params = params;
	}

	public Context getmContext() {
		return mContext;
	}

	public void setmContext(Context mContext) {
		this.mContext = mContext;
	}

	public RestClient getmRestClient() {
		return mRestClient;
	}

	public void setmRestClient(RestClient mRestClient) {
		this.mRestClient = mRestClient;
	}

	public int getMethod() {
		return method;
	}

	public void setMethod(int method) {
		this.method = method;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getFullUrl() {
		if (host != null && !url.startsWith("http")) {
			String fullUrl = host + url;
			if (fullUrl.indexOf("?") == -1) {
				if (!fullUrl.endsWith("/")) {
					fullUrl = fullUrl + "/";
				}
			}

			if (this.method == Method.GET && this.params != null
					&& this.params.size() > 0) {
				StringBuilder sb = new StringBuilder(fullUrl);

				for (String key : this.params.toMap().keySet()) {
					try {
						String value = URLEncoder.encode(this.params.get(key),
								"UTF-8");
						if (sb.toString().indexOf("?") == -1) {
							sb.append("?");
						} else {
							sb.append("&");
						}
						sb.append(key).append("=").append(value);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
				return sb.toString();
			}
			return fullUrl;
		}
		return url;
	}

	public boolean isCache() {
		return isCache;
	}

	public void setCache(boolean isCache) {
		this.isCache = isCache;
	}
}
