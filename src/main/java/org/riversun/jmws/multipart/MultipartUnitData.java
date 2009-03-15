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
package org.riversun.jmws.multipart;

import java.util.Map;

/**
 * Unit data of multipart data.
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 * */

public class MultipartUnitData {
	public static final String CONTENT_DISPOSITION = "content-disposition";
	public static final String CONTENT_TYPE = "content-type";
	public static final String CONTENT_TRANSFER_ENCODING = "content-transfer-encoding";
	public static final String HEADER_ATTR_KEY_FILENAME = "filename";
	public static final String HEADER_ATTR_KEY_NAME = "name";
	private Map<String, MultipartHeaderData> mMpHeaderMap;
	private byte[] mBody;

	public MultipartUnitData(Map<String, MultipartHeaderData> headerMap, byte[] body) {
		mMpHeaderMap = headerMap;
		mBody = body;
	}

	public byte[] getBody() {
		return mBody;
	}

	public MultipartHeaderData getContentDisposition() {
		return mMpHeaderMap.get(CONTENT_DISPOSITION);
	}

	public String getContentType() {
		return getHeaderValue(CONTENT_TYPE);
	}

	public String getContentTransferEncoding() {
		return getHeaderValue(CONTENT_TRANSFER_ENCODING);
	}

	public String getFileName() {
		MultipartHeaderData contentDisposition = getContentDisposition();
		if (contentDisposition != null) {
			return contentDisposition.getAttrValue("filename");
		} else {
			return null;
		}
	}

	public String getName() {
		MultipartHeaderData contentDisposition = getContentDisposition();
		if (contentDisposition != null) {
			return contentDisposition.getAttrValue("name");
		} else {
			return null;
		}
	}

	public boolean isFile() {
		return getFileName() != null;
	}

	public String getHeaderValue(String headerName) {
		MultipartHeaderData multipartHeaderData = mMpHeaderMap.get(headerName);
		if (multipartHeaderData != null) {
			return multipartHeaderData.getHeaderValue();
		} else {
			return null;
		}
	}

	public String getHeaderAttrValue(String headerName, String headerAttrName) {
		MultipartHeaderData multipartHeaderData = mMpHeaderMap.get(headerName);
		if (multipartHeaderData != null) {
			return multipartHeaderData.getAttrValue(headerAttrName);
		} else {
			return null;
		}
	}
}
