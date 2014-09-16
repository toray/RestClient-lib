package com.toraysoft.tools.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.NameValuePair;

public class RestParameter {

	// kind of request method
	public interface REQUEST_METHOD {
		int GET = 0;
		int POST = 1;
	}

	// build url with list params
	public static String getUrlWithParams(String url, List<NameValuePair> params) {
		StringBuilder sb = new StringBuilder(url);
		for (NameValuePair param : params) {
			String name = param.getName();
			try {
				String value = URLEncoder.encode(param.getValue(), "UTF-8");
				url = sb.toString();
				if (url.indexOf("?") == -1) {
					sb.append("?");
				} else {
					sb.append("&");
				}
				sb.append(name).append("=").append(value);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	// build url with stirng key
	public static String getUrlWithParams(String url, String key) {
		StringBuilder sb = new StringBuilder(url);
		try {
			key = URLEncoder.encode(key, "UTF-8");
			sb.append(key).append("/");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
}
