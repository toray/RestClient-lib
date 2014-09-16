package com.toraysoft.tools.rest;

import com.android.volley.VolleyError;

public class RestCallback {

	// restclient callback
	public interface OnRestCallback {
		public void onSuccess(String response);

		public void onCache(String cache);

		public void onError(String errmsg);

		public void onFail();
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
