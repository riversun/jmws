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
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.riversun.jmws.HttpServerDef;
import org.riversun.jmws.MicroService;
import org.riversun.jmws.common.HttpdLog;
import org.riversun.jmws.common.ContentTypeResolver;
import org.riversun.jmws.multipart.HttpMultipartDecoder;
import org.riversun.jmws.util.CoString;

/**
 * HTTP Input/Output processing thread
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 * */
public class HttpHandler implements Runnable {

	private static final String RESPONSE_HEADER_KEY_SERVER_NAME = "Server";
	private static final int MAX_REQUEST_DATA = 3000000;

	private static String LOGTAG = HttpHandler.class.getSimpleName();

	private static final String POST_CONTENT_TYPE_APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
	private static final String POST_CONTENT_TYPE_MULTIPART_FORMADATA = "multipart/form-data";
	private static final String UTF_8 = "UTF-8";
	private static final String LINE_FEED = System.lineSeparator();
	private Socket _socket = null;
	private Map<String, MicroService> _serviceMap = null;
	private Map<String, String> _dirMap = null;

	public HttpHandler(Socket socket, Map<String, MicroService> serviceMap, Map<String, String> dirMap) {
		_socket = socket;
		_serviceMap = serviceMap;
		_dirMap = dirMap;
	}

	// Connecting threads
	public void run() {

		HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#run()", 3);

		try {

			InputStream is = _socket.getInputStream();

			if (is == null) {
				return;
			}

			HttpHeaderReader hr = new HttpHeaderReader(is);
			HttpProtocolInfo protocolInfo = hr.getProtocolInfo();
			final String method = protocolInfo.getMethod();
			final String uri = protocolInfo.getUri();
			HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#run() method=" + method, 3);
			HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#run() raw uri=" + uri, 3);

			// Click start //////// to cache the content portion sizes
			long contentSize = 0;
			HttpRequestHeaderInfo httpHeaderInfo = hr.getHttpHeaderInfo();
			final HttpQueryParamInfo queryInfo = new HttpQueryParamInfo();

			// Object containing information about the request
			final HttpReq req = new HttpReq(protocolInfo, httpHeaderInfo, queryInfo);
			String contentLength = httpHeaderInfo.getContentLength();

			HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#run() contentLength=" + contentLength, 3);

			if (contentLength != null) {
				try {
					contentSize = Integer.parseInt(contentLength);
				} catch (NumberFormatException ex) {
				}
			}

			if (contentSize > MAX_REQUEST_DATA) {
				throw new HttpServerException(HttpServerDef.HTTP_400_BAD_REQUEST, "Request data is too big. size=" + contentLength + " +maxSize=" + MAX_REQUEST_DATA);
			}

			// To cache the content area size [end] //////// should read from
			// the stream after the remaining content size
			long remainToReadSize = contentSize;

			// Write to this ByteArrayOutput part I read content part header
			// processing for
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			baos.write(hr.overReadData);
			if (hr.headerEndPosition < hr.totalReadLength) {

				// If you had read up further than the position of the end of
				// the HTTP header, appended to the HTTP request:
				remainToReadSize -= hr.totalReadLength - hr.headerEndPosition + 1;
			} else if (hr.headerEndPosition == 0) {
				remainToReadSize = 0;
			}
			final int BUFFER_SIZE_FOR_READ_REMAIN_CONTENT = 1024;
			byte[] buffer = new byte[BUFFER_SIZE_FOR_READ_REMAIN_CONTENT];
			int readLength = 0;
			// hr.totalReadLength;
			while (readLength >= 0 && remainToReadSize > 0) {
				readLength = is.read(buffer, 0, BUFFER_SIZE_FOR_READ_REMAIN_CONTENT);
				remainToReadSize -= readLength;
				if (readLength > 0) {
					baos.write(buffer, 0, readLength);
				}
			}
			byte[] reqContentData = baos.toByteArray();
			ByteArrayInputStream requestContentIs = new ByteArrayInputStream(reqContentData);

			// Required data from [start] ///////// request: content request
			// when content is loaded by 1 character reader
			BufferedReader requestContentReader = new BufferedReader(new InputStreamReader(requestContentIs));
			StringBuilder sb = new StringBuilder();
			int iOneChar;
			while ((iOneChar = requestContentReader.read()) != -1) {
				sb.append((char) iOneChar);
			}
			final String requestContentStr = sb.toString();
			requestContentReader.close();

			// You want to declare that no corresponding request when content is
			// loaded by 1 character [end]
			// /////// TODO-alive in the
			req.setUri(uri);
			if ("GET".equalsIgnoreCase(method)) {

				// queryInfo into
				// the query process
				String[] uriBlocks = uri.split("\\?");
				if (uriBlocks.length == 2) {
					String uriPart = uriBlocks[0];
					String queryPart = uriBlocks[1];
					parseQuery(queryPart, queryInfo);

					// Overwrite without query uri
					req.setUri(uriPart);
				}
			}
			if ("PUT".equalsIgnoreCase(method)) {

				// Does not support (that do not use dosend, throws an
				// exception, always from the process through)
				throw new HttpServerException(HttpServerDef.HTTP_501_NOT_IMPLEMENTED, "PUT method is't supported.");
			}
			if ("POST".equalsIgnoreCase(method)) {
				if (CoString.isBlank(contentLength)) {
					throw new HttpServerException(HttpServerDef.HTTP_400_BAD_REQUEST, "ContentLength is mssing.");
				}
				String contentType = "";
				String contentTypeHeader = httpHeaderInfo.getContentType();
				String[] cTypeBlocks = contentTypeHeader.split("; ");
				if (cTypeBlocks.length > 1) {
					contentType = cTypeBlocks[0];
				} else if (cTypeBlocks.length == 1) {
					contentType = cTypeBlocks[0];
				}
				HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#run() contentType=" + contentType, 3);
				if (POST_CONTENT_TYPE_MULTIPART_FORMADATA.equalsIgnoreCase(contentType)) {
					String mutipartBoundary = httpHeaderInfo.getMutipartBoundary();
					if (CoString.isNotBlank(mutipartBoundary)) {

						// Multipart parser
						final HttpMultipartDecoder httpMpDecoder = new HttpMultipartDecoder();

						// Parses a multipart the queryInfo Pack information
						httpMpDecoder.decodeMultipartData(mutipartBoundary, reqContentData, queryInfo);
					} else {

						// No multipart boundary case (that do not use dosend,
						// throws an exception, always from the process through)
						throw new HttpServerException(HttpServerDef.HTTP_400_BAD_REQUEST, "Invalid multipart message.");
					}
				}

				else if (POST_CONTENT_TYPE_APPLICATION_X_WWW_FORM_URLENCODED.equalsIgnoreCase(contentType)) {
					final String queryPart = requestContentStr;
					parseQuery(queryPart, queryInfo);
				} else {
					throw new HttpServerException(HttpServerDef.HTTP_400_BAD_REQUEST, "Unknown content type. contentType=" + contentType);
				}
			}
			HttpRes res = new HttpRes(new HttpResponseHeaderInfo());

			// To run the client
			HttpRawResponse doService = doService(req.getUri(), req, res);
			InputStream responseData = doService.responseData;

			sendHttpResponse(HttpServerDef.HTTP_200_OK, res.getContentype(), res.getHeaderInfo(), responseData);
			requestContentReader.close();
			is.close();
		} catch (IOException e) {
			doSendErrorResponse(HttpServerDef.HTTP_500_INTERNAL_SERVER_ERROR, e.getMessage());
		} catch (HttpServerException e) {
			HttpdLog.stackTrace(e);

			// e. printStackTrace ();
			doSendErrorResponse(e.getHttpErrorStatus(), e.getHttpErrorMessage());
		} finally {
			if (_socket != null && !_socket.isClosed()) {
				try {
					_socket.close();
				} catch (IOException e) {
					HttpdLog.stackTrace(e);
				}
			}
		}
		return;
	}

	protected static class HttpRawResponse {
		public InputStream responseData;
		public String mimeType;

	}

	private HttpRawResponse doService(String uri, HttpReq req, HttpRes res) throws HttpServerException {
		HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#doService() uri=" + uri, 3);
		HttpRawResponse rawResponse = new HttpRawResponse();
		if (uri == null) {
			HttpdLog.err(HttpdLog.CATE_HTTPD, LOGTAG + "#doService() uri=" + uri, 3);
			uri = "@NULL_URI_IS_REQUESTED";
		}
		MicroService service = _serviceMap.get(uri);
		InputStream responseContentIs = null;
		if (service != null) {
			try {
				service.service(req, res);
				ByteArrayOutputStream baos = (ByteArrayOutputStream) res.getOutputStream();
				byte[] responseContentData = baos.toByteArray();
				responseContentIs = new ByteArrayInputStream(responseContentData);
				rawResponse.responseData = responseContentIs;
			} catch (Exception e) {

				// -- in the service logic error has occurred
				HttpdLog.err(HttpdLog.CATE_HTTPD, LOGTAG + "#doService() error occured at user's service(req,res) method. e=" + e.getMessage(), 3);
				e.printStackTrace();
				throw new HttpServerException(HttpServerDef.HTTP_500_INTERNAL_SERVER_ERROR, e.getMessage());
			}
		} else {

			// -- the URI service is not registered
			final String defaultResponseStr = "";
			HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#doService() service uri=" + uri + " is not service.uri try to check dir...", 3);

			// To search the directory of
			String destDirectoryPath = null;
			String srcUri = null;
			Set<String> dirKeySet = _dirMap.keySet();
			for (String dirKey : dirKeySet) {
				if (uri.startsWith(dirKey)) {
					srcUri = dirKey;
					destDirectoryPath = _dirMap.get(dirKey);
					break;
				}
			}
			if (CoString.isBlank(destDirectoryPath)) {

				// -- If you do not find directory
				try {
					responseContentIs = new ByteArrayInputStream(defaultResponseStr.getBytes("UTF-8"));
					rawResponse.responseData = responseContentIs;
				} catch (UnsupportedEncodingException e) {

					// e. printStackTrace ();
					HttpdLog.stackTrace(e);
				}
			} else {

				// -- If the found directory
				HttpDirectoryResponseHandler dirResHandler = new HttpDirectoryResponseHandler();
				rawResponse = dirResHandler.handleDirectoryResponse(uri, req, res, destDirectoryPath, srcUri);
			}
		}
		return rawResponse;
	}

	/**
	 * * @throws HttpServerException to parse the query string and pack a POJO
	 */
	private void parseQuery(String queryStr, HttpQueryParamInfo queryInfo) throws HttpServerException {

		HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#parseQuery() queryStr=" + queryStr, 3);

		String[] paramBlocks = queryStr.split("&");

		int length = paramBlocks.length;

		for (int i = 0; i < length; i++) {
			String paramPair = paramBlocks[i];
			String[] paramAndVal = paramPair.split("=");
			if (paramAndVal.length == 2) {
				String paramKey = paramAndVal[0];
				String paramValue = paramAndVal[1];
				String decodedParamValue = "";
				try {
					decodedParamValue = URLDecoder.decode(paramValue, UTF_8);
				} catch (UnsupportedEncodingException e) {

					// e. printStackTrace ();
					HttpdLog.stackTrace(e);
				}
				HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#parseQuery() paramKey=" + paramKey + " encodedParamValue=" + decodedParamValue + " original paramValue=" + paramValue, 3);
				queryInfo.addParam(new HttpParam(paramKey, decodedParamValue));
			} else {
				throw new HttpServerException(HttpServerDef.HTTP_400_BAD_REQUEST, "Invalid query parameters.");
			}
		}
	}

	private void doSendErrorResponse(String status, String msg) {
		if (msg == null) {
			msg = "";
		}
		sendHttpResponse(status, ContentTypeResolver.getContentType(".txt").mimeType, new ByteArrayInputStream(msg.getBytes()));
	}

	private void sendHttpResponse(String status, String mimeType, InputStream responseIs) {
		sendHttpResponse(status, mimeType, null, responseIs);
	}

	private void sendHttpResponse(String status, String mimeType, HttpParamColl headerList, InputStream responseIs) {
		if (status == null) {
			throw new RuntimeException("status is null");
		}
		OutputStream os = null;
		PrintWriter pw = null;
		try {
			os = _socket.getOutputStream();

			// Start / write in the printwriter's content-type
			pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(os, "UTF-8")));
			pw.print(HttpServerDef.HTTP_METHOD_VERSION + " " + status + " " + LINE_FEED);

			// Send the server information
			pw.print(RESPONSE_HEADER_KEY_SERVER_NAME + ": " + HttpServerDef.HTTP_SERVER_NAME + LINE_FEED);

			mimeType="application/json";
			if (mimeType != null) {
				pw.print(HttpServerDef.HTTP_CONTENT_TYPE + ": " + mimeType + LINE_FEED);
			}
			if (headerList != null) {
				List<HttpParam> httpParamList = headerList.getHttpParamList();
				for (HttpParam param : httpParamList) {
					String key = param.getKey();
					if (RESPONSE_HEADER_KEY_SERVER_NAME.equals(key)) {

						// Also (user set) Skip Server information
						continue;
					}
					String stringValue = param.getStringValue();
					if (stringValue != null) {

						// When it isn't a string type parameter can be null to
						// null check.
						pw.print(key + ": " + stringValue + LINE_FEED);
					}
				}
			}

			// To insert the header for CRLF last header, so far
			pw.print(LINE_FEED);
			pw.flush();

			// Content types [end] / / / write in the printwriter's
			if (responseIs != null) {
				byte[] responseBuffer = new byte[1024];
				while (true) {
					int len = responseIs.read(responseBuffer);
					if (len < 0) {
						break;
					}
					os.write(responseBuffer, 0, len);
				}
			}
			os.flush();
		} catch (IOException e) {

			// e. printStackTrace ();
			HttpdLog.stackTrace(e);
		} finally {
			if (pw != null) {
				pw.close();
			}
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {

					// e. printStackTrace ();
					HttpdLog.stackTrace(e);
				}
			}
			if (responseIs != null) {
				try {
					responseIs.close();
				} catch (IOException e) {

					// e. printStackTrace ();
					HttpdLog.stackTrace(e);
				}
			}
		}
	}
}
