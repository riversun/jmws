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

/**
 * HTTP protocol info
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 * */
public class HttpProtocolInfo extends HttpParamColl {
	protected static final String PROTOCOL_INFO_METHOD = "method";
	protected static final String PROTOCOL_INFO_URI = "uri";
	protected static final String PROTOCOL_INFO_HTTP_VER = "http_version";

	// Each may engage in a key method of giving, only.
	public String getMethod() {
		return getStringValue(PROTOCOL_INFO_METHOD);
	}

	public String getUri() {
		return getStringValue(PROTOCOL_INFO_URI);
	}

	public String getProtocol() {
		return getStringValue(PROTOCOL_INFO_HTTP_VER);
	}

	@Override
	public String toString() {
		return "HttpProtocolInfo [method()=" + getMethod() + ", uri=" + getUri() + ", protocol=" + getProtocol() + "]";
	}
	
}
