package com.toraysoft.tools.rest;

import java.util.HashMap;
import java.util.Map;

public class RestHeader {

	protected Map<String, String> headers;

	public RestHeader() {
		headers = new HashMap<String, String>();
	}

	public void add(String key, String value) {
		headers.put(key, value);
	}

	public void remove(String key) {
		headers.remove(key);
	}

	public void pullMap(Map<String, String> headers){
		this.headers.putAll(headers);
	}
	
	public Map<String, String> toMap() {
		return headers;
	}

	public int size(){
		return headers.size();
	}
}
