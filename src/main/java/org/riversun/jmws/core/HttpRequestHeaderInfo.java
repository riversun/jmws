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
 * HttpRequestHeaderInfo
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 * */
public class HttpRequestHeaderInfo extends HttpParamColl {

	protected static final String PROTOCOL_INFO_CONTENT_TYPE = "content-type";
	protected static final String PROTOCOL_INFO_CONTENT_LENGTH = "content-length";
	protected static final String PROTOCOL_INFO_USER_AGENT = "user-agent";
	protected static final String PROTOCOL_INFO_HOST = "host";

	public String getHost() {
		return getStringValue(PROTOCOL_INFO_HOST);
	}

	public String getContentLength() {
		return getStringValue(PROTOCOL_INFO_CONTENT_LENGTH);
	}

	public String getContentType() {

		String rawContentType = getStringValue(PROTOCOL_INFO_CONTENT_TYPE);
		String[] multipartContentTypeBlocks = rawContentType.split("; ");
		if (multipartContentTypeBlocks.length > 0) {
			String contentType = multipartContentTypeBlocks[0];
			return contentType;
		} else {
			return rawContentType;
		}
	}

	public String getMutipartBoundary() {

		String rawContentType = getStringValue(PROTOCOL_INFO_CONTENT_TYPE);
		String[] multipartContentTypeBlocks = rawContentType.split("; ");

		if (multipartContentTypeBlocks.length == 2) {

			String boundaryPart = multipartContentTypeBlocks[1];

			String[] boundaryBlocks = boundaryPart.split("=");

			if (boundaryBlocks.length == 2) {

				String boundaryKey = boundaryBlocks[0];
				String boundaryValue = boundaryBlocks[1];

				if ("boundary".equals(boundaryKey)) {
					return boundaryValue;
				} else {
					;
				}
			} else {
				;
			}
		} else {

			;
		}
		return null;
	}

	public String getUserAgent() {
		return getStringValue(PROTOCOL_INFO_USER_AGENT);
	}
}
