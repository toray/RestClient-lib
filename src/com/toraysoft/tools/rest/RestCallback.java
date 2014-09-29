package com.toraysoft.tools.rest;

import com.android.volley.VolleyError;

public class RestCallback {

	// restclient callback
	public interface OnResponseCallback<T> {
		public void onSuccess(T response);

		public void onCache(T cache);

		public void onError(String errmsg);
	}

	// request callback
	public interface RequestListener<T> {
		public void onResponse(T response);

		public void onErrorResponse(VolleyError error);
	}

	public interface OnHostErrorCallback {
		public void onChangHost(String url);
	}

}
