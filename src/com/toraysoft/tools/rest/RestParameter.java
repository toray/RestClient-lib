package com.toraysoft.tools.rest;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class RestParameter {

	Map<String, String> params;

	public RestParameter() {
		params = new HashMap<String, String>();
	}

	public void add(String key, String value) {
		params.put(key, value);
	}

	public void remove(String key) {
		params.remove(key);
	}

	public void pullMap(Map<String, String> headers) {
		this.params.putAll(headers);
	}

	public Map<String, String> toMap() {
		return params;
	}

	public JSONObject toJSONObject() {
		JSONObject ret = new JSONObject();
		if (params != null) {
			for (String key : params.keySet()) {
				try {
					ret.put(key, get(key));
				} catch (JSONException e) {
				}
			}
		}
		return ret;
	}

	public String get(String key) {
		return params.get(key);
	}

	public int size(){
		return params.size();
	}
}
