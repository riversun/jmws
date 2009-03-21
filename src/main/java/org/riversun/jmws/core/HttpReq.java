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

import java.io.BufferedReader;

import org.riversun.jmws.util.CoString;

/**
 * HTTP request<br>
 * store the HTTP request info
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 */
public class HttpReq {
	private String _uri;
	private HttpProtocolInfo _protocolInfo;
	private HttpRequestHeaderInfo _headerInfo;
	private HttpQueryParamInfo _paramInfo;
	private BufferedReader _reader;

	public HttpReq(HttpProtocolInfo protocolInfo, HttpRequestHeaderInfo headerInfo, HttpQueryParamInfo paramInfo,
			BufferedReader reader) {
		super();
		this._protocolInfo = protocolInfo;
		this._headerInfo = headerInfo;
		this._paramInfo = paramInfo;
		this._reader = reader;
	}

	public HttpProtocolInfo getProtocolInfo() {
		return _protocolInfo;
	}

	public BufferedReader getReader() {
		return _reader;
	}

	public String getUri() {
		return _uri;
	}

	public Object getPramValue(String paramName) {
		return _paramInfo.getParam(paramName);
	}

	public void setUri(String uri) {
		this._uri = uri;
	}

	public HttpRequestHeaderInfo getHeaderInfo() {
		return _headerInfo;
	}

	public void setHeaderInfo(HttpRequestHeaderInfo headerInfo) {
		this._headerInfo = headerInfo;
	}

	public HttpQueryParamInfo getParamInfo() {
		return _paramInfo;
	}

	public void setParamInfo(HttpQueryParamInfo paramInfo) {
		this._paramInfo = paramInfo;
	}

	public String asString(String paramName) {
		return _paramInfo.getStringValue(paramName);
	}

	public String asStringBlank(String paramName) {
		String val = _paramInfo.getStringValue(paramName);
		if (val == null) {
			return "";
		} else {
			return val;
		}
	}

	public Integer asInteger(String paramName) {
		String str = asString(paramName);
		if (CoString.isBlank(str)) {

			// -- If you do not set the parameter value
			return null;
		} else {

			// -- If you have set the parameter value
			Integer inte = null;
			try {
				inte = Integer.parseInt(str);
			} catch (NumberFormatException e) {

				// If the string is not numeric
			}
			return inte;
		}
	}

	public int asIntegerZero(String paramName) {
		Integer inte = asInteger(paramName);
		if (inte == null) {
			return 0;
		} else {
			return inte;
		}
	}
}
