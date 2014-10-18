package com.toraysoft.tools.rest;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class RestParameter {

	Map<String, Object> params;

	public RestParameter() {
		params = new HashMap<String, Object>();
	}

	public void add(String key, Object value) {
		params.put(key, value);
	}

	public void remove(String key) {
		params.remove(key);
	}

	public void pullMap(Map<String, Object> headers) {
		this.params.putAll(headers);
	}

	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<String, String>();
		for (String key : params.keySet()) {
			if(params.get(key)!=null){
				map.put(key, params.get(key).toString());
			}else{
				throw new NullPointerException("RestClient parameter can not be NULL !!!");
			}
		}
		return map;
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

	public Object get(String key) {
		return params.get(key);
	}

	public int size() {
		return params.size();
	}
}
