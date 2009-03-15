/**
 * java-micro-webserver
 * 
 * Copyright 2004-2010 Tom Misawa, riversun.org@gmail.com
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of 
 * this software and associated documentation files (the "Software"), to deal in the 
 * Software without restriction, including without limitation the rights to use, 
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the 
 * Software, and to permit persons to whom the Software is furnished to do so, 
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all 
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A 
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR 
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR 
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */
package org.riversun.jmws.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP parameter store wrapper
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 * */
public class HttpParamColl {
	private Map<String, HttpParam> _paramMap = new LinkedHashMap<String, HttpParam>();

	public HttpParamColl() {
		_paramMap.clear();
	}

	public void addParam(HttpParam param) {
		final String key = param.getKey();
		if (!_paramMap.containsKey(key)) {
			_paramMap.put(key, param);
		}
	}

	public HttpParam getParam(String key) {
		if (_paramMap.containsKey(key)) {
			return _paramMap.get(key);
		}
		return null;
	}

	public Object getValue(String key) {
		HttpParam param = getParam(key);
		if (param != null) {
			return param.getValue();
		} else {
			return null;
		}
	}

	public String getStringValue(String key) {
		HttpParam param = getParam(key);
		if (param != null) {
			return param.getStringValue();
		} else {
			return null;
		}
	}

	public List<HttpParam> getHttpParamList() {
		Collection<HttpParam> values = _paramMap.values();
		List<HttpParam> paramList = new ArrayList<HttpParam>(values);
		return paramList;
	}
}
