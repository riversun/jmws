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

import java.util.Arrays;
import java.util.List;

import org.riversun.jmws.common.HttpdLog;
import org.riversun.jmws.core.HttpParam;
import org.riversun.jmws.core.HttpQueryParamInfo;
import org.riversun.jmws.core.HttpServerException;

/**
 * HTTP Multipart decoder<br>
 * 
 * see [RFC 2388] {@see http://www.ietf.org/rfc/rfc2388.txt}
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 * */
public class HttpMultipartDecoder {
	private static String CAT = "SERVER";
	private static String LOGTAG = HttpMultipartDecoder.class.getSimpleName();

	/**
	 * * Decode multi part deta
	 * 
	 * @param boundaryStr
	 * @param reqContentData
	 * @param queryInfo
	 * @throws HttpServerException
	 */
	public void decodeMultipartData(String boundaryStr, byte[] reqContentData, HttpQueryParamInfo queryInfo) throws HttpServerException {
		MultiPartUnitDataOffsetDetector offsetDetector = new MultiPartUnitDataOffsetDetector();
		final List<MultiPartUnitDataOffset> mpUnitDataOffsetList = offsetDetector.getMultiPartUnitDataOffset(boundaryStr, reqContentData);

		// From data pointer (the body of a multipart data) actually parses
		// multipart data
		for (MultiPartUnitDataOffset mpUnitDataOffset : mpUnitDataOffsetList) {

			// From multipart individual by one multi-part individual data were
			// gone and to get
			byte[] multiPartUnitBinaryData = Arrays.copyOfRange(reqContentData, mpUnitDataOffset.dataStart, mpUnitDataOffset.dataEnd);

			// That parses multipart data (binary)
			MultiPartUnitDataDecoder mpuDecoder = new MultiPartUnitDataDecoder();
			MultipartUnitData mpUnitData = mpuDecoder.decodeMultipartUnitBinaryData(multiPartUnitBinaryData);

			// TODO Punitdata to properly handle the survey content is such as
			// the ContentType and Encoding. Filename attribute is not in the
			// status quo must be able to identify with these all be regarded as
			// text
			if (mpUnitData.isFile()) {

				// If the file attachment
				String key = mpUnitData.getName();
				String fileName = mpUnitData.getFileName();
				byte[] binaryData = mpUnitData.getBody();
				MultipartFileData fileData = new MultipartFileData(fileName, binaryData);
				HttpParam param = new HttpParam(key, fileData);
				queryInfo.addParam(param);

				// For more information which can be taken for a multipart, as
				// auxiliary data adding to the POJO
				queryInfo.addMultipartAttr(key, mpUnitData);
				HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#decodeMultipartData() handle query param as file data: key=" + key + " fileName=" + fileName + " value=" + fileData, 3);

				// In this class the saveFile method can test whether or not the
				// file is sent to the appropriate
			} else {

				// The file attachment if at present, other than file
				// attachments to interpret as a String
				String key = mpUnitData.getName();
				String strValue = new String(mpUnitData.getBody());
				HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#decodeMultipartData() handle query param as string: key=" + key + " value=" + strValue, 3);
				HttpParam param = new HttpParam(key, strValue);
				queryInfo.addParam(param);

				// For more information which can be taken for a multipart, as
				// auxiliary data adding to the POJO
				queryInfo.addMultipartAttr(key, mpUnitData);
			}
		}
	}

}
