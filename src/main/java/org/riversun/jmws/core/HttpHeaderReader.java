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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.riversun.jmws.HttpServerDef;
import org.riversun.jmws.common.HttpdLog;
import org.riversun.jmws.util.CoString;

/**
 * processing HTTP headers<br>
 * ・if header-length will over MAX_HTTP_HEADER_BUFFER_SIZE, return
 * HTTP_400_BAD_REQUEST<br>
 * 
 * ・processing HTTP headers ,and set into protocolInfo and headerInfo.
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 * */
public class HttpHeaderReader {
	private static String CAT = "SERVER";
	private static String LOGTAG = HttpHeaderReader.class.getSimpleName();

	// The buffer size for the header gets. The same as Apache.
	private static final int MAX_HTTP_HEADER_BUFFER_SIZE = 8192;
	public byte[] overReadData;
	public int totalReadLength = 0;
	public int headerEndPosition = 0;
	private HttpProtocolInfo mProtocolInfo = null;
	private HttpRequestHeaderInfo mHttpHeaderInfo = null;

	public HttpProtocolInfo getProtocolInfo() {
		return mProtocolInfo;
	}

	public HttpRequestHeaderInfo getHttpHeaderInfo() {
		return mHttpHeaderInfo;
	}

	public HttpHeaderReader(InputStream is) throws HttpServerException {
		try {
			byte[] buffferForHeader = new byte[MAX_HTTP_HEADER_BUFFER_SIZE];
			int crrReadLength = is.read(buffferForHeader, 0, MAX_HTTP_HEADER_BUFFER_SIZE);
			while (crrReadLength > 0) {
				totalReadLength += crrReadLength;

				// detect end of http header "\r\n\r\n"
				int scanPos = 0;
				while (scanPos + 3 < totalReadLength) {
					if (buffferForHeader[scanPos + 0] == '\r') {
						if (buffferForHeader[scanPos + 1] == '\n') {
							if (buffferForHeader[scanPos + 2] == '\r') {
								if (buffferForHeader[scanPos + 3] == '\n') {
									headerEndPosition = scanPos + 4;
									break;
								}
							}
						}
					}
					scanPos++;
				}

				//
				HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#HttpHeaderReader() headerEndPosition=" + headerEndPosition, 3);
				if (headerEndPosition > 0) {
					break;
				}
				crrReadLength = is.read(buffferForHeader, totalReadLength, MAX_HTTP_HEADER_BUFFER_SIZE - totalReadLength);

				// Repels on exceptions, been granted exceeds the buffer size
				// for the header size of the HTTP headers
				if (totalReadLength >= MAX_HTTP_HEADER_BUFFER_SIZE) {
					throw new HttpServerException(HttpServerDef.HTTP_400_BAD_REQUEST, "Too match headers.");
				}
			}

			// Writes: I too read
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			if (headerEndPosition < totalReadLength) {
				baos.write(buffferForHeader, headerEndPosition, totalReadLength - headerEndPosition);
			}
			overReadData = baos.toByteArray();
			HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#HttpHeaderReader() totalReadLength=" + totalReadLength + " overRead size=" + overReadData.length, 3);
			ByteArrayInputStream headerDataStream = new ByteArrayInputStream(buffferForHeader, 0, totalReadLength);
			BufferedReader br = new BufferedReader(new InputStreamReader(headerDataStream));
			mProtocolInfo = new HttpProtocolInfo();
			mHttpHeaderInfo = new HttpRequestHeaderInfo();
			decodeHeader(br, mProtocolInfo, mHttpHeaderInfo);
		} catch (IOException e) {

			// e. printStackTrace ();
			HttpdLog.stackTrace(e);
			throw new HttpServerException(HttpServerDef.HTTP_500_INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	private void decodeHeader(BufferedReader br, HttpParamColl protocolInfo, HttpParamColl httpHeaderInfo) throws HttpServerException {
		try {

			// Read the request line
			String firstLine = br.readLine();
			if (CoString.isBlank(firstLine)) {
				return;
			}
			String[] protocolBlocks = firstLine.split(" ");
			if (protocolBlocks.length < 2) {

				throw new HttpServerException(HttpServerDef.HTTP_400_BAD_REQUEST, "Request is incorrect.");
			} else {
				String method = protocolBlocks[0];
				String uri = protocolBlocks[1];
				protocolInfo.addParam(new HttpParam(HttpProtocolInfo.PROTOCOL_INFO_METHOD, method));
				protocolInfo.addParam(new HttpParam(HttpProtocolInfo.PROTOCOL_INFO_URI, uri));
				if (protocolBlocks.length >= 3) {
					String httpVer = protocolBlocks[2];
					protocolInfo.addParam(new HttpParam(HttpProtocolInfo.PROTOCOL_INFO_HTTP_VER, httpVer));
				}
			}
			String line = br.readLine();

			while (CoString.isNotBlank(line)) {

				HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#HttpHeaderReader() header line=" + line, 3);

				String[] propBlocks = line.split(":");

				if (propBlocks.length == 2) {
					String propKey = propBlocks[0].trim();
					String lowerCasedPropKey = propKey.toLowerCase();
					String propValue = propBlocks[1].trim();
					httpHeaderInfo.addParam(new HttpParam(lowerCasedPropKey, propValue));
				}
				line = br.readLine();
			}
		} catch (IOException e) {

			HttpdLog.stackTrace(e);
			throw new HttpServerException(HttpServerDef.HTTP_500_INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}
}
