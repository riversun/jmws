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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.riversun.jmws.HttpServerDef;
import org.riversun.jmws.common.EMimeDataType;
import org.riversun.jmws.common.HttpdLog;
import org.riversun.jmws.common.MimeInfo;
import org.riversun.jmws.common.ContentTypeResolver;
import org.riversun.jmws.core.HttpHandler.HttpRawResponse;
import org.riversun.jmws.util.CoString;

/**
 * Directory based(file based) response handler
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 * */
public class HttpDirectoryResponseHandler {
	private static String LOGTAG = HttpHandler.class.getSimpleName();

	public HttpRawResponse handleDirectoryResponse(String uri, HttpReq req, HttpRes res, String destDirectoryPath, String srcUri) throws HttpServerException {
		final String defaultResponseStr = "";
		HttpRawResponse rawResponse = new HttpRawResponse();
		InputStream responseContentIs = null;
		HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#doService() destDirectoryPath=" + destDirectoryPath + " srcUri=" + srcUri + " uri=" + uri, 3);
		if (destDirectoryPath.endsWith("/")) {
			destDirectoryPath = subString(destDirectoryPath, "/");
		} else {
		}

		// subString operations like this-&gt; /summer/begin minus /summer
		String relativePath = subString(uri, srcUri);
		String destAbsoluteFilePath = "";
		if (relativePath.startsWith("/")) {

			//-- If relative URL starts / from
			destAbsoluteFilePath = destDirectoryPath + relativePath;
		} else {

			//-- If relative URL starts / from not on the processed String
			// relativePath = subString (uri, srcUri); The "/" (root
			// representation) was designated have been removed and that the
			// directory be
			destAbsoluteFilePath = destDirectoryPath + "/" + relativePath;
		}

		// The physical path of the directory on
		destAbsoluteFilePath = destAbsoluteFilePath.replaceAll("\\\\", "/").replaceAll("\\./", "/").replaceAll("\\.\\.", "");
		HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#doService() destAbsoluteFilePath=" + destAbsoluteFilePath, 3);
		File file = new File(destAbsoluteFilePath);
		if (file.exists()) {

			//-- If you have directory or file
			if (file.isDirectory()) {

				//-- If the directory was
				HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#doService() dir response(dir)=" + destAbsoluteFilePath, 3);
				try {

					// The directory is not shown
					responseContentIs = new ByteArrayInputStream(defaultResponseStr.getBytes("UTF-8"));
					rawResponse.responseData = responseContentIs;
				} catch (UnsupportedEncodingException e) {

					// e. printStackTrace ();
					HttpdLog.stackTrace(e);
				}
			}
			else if (file.isFile()) {

				//-- If the file was
				HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#doService() file response(file)=" + destAbsoluteFilePath, 3);
				String simpleFileName = file.getName();
				String fileBody = getFileBody(simpleFileName);
				String fileExtension = getFileExtensionLowerCase(simpleFileName);

				MimeInfo mimeInfo = ContentTypeResolver.getContentType(fileExtension);

				HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#doService() detect mime type fileExtension=" + fileExtension + " mimeInfo=" + mimeInfo, 3);
				if (mimeInfo == null) {
					HttpdLog.err(HttpdLog.CATE_HTTPD, LOGTAG + "#doService() cannot detect mime type of file. fileExtension=" + fileExtension, 3);

					// If you could not determine the MIME type as an unknown
					// treatment, return octed-stream
					mimeInfo = ContentTypeResolver.getContentType(ContentTypeResolver.MIME_TYPE_UNKOWN);
				}
				String mimeType = mimeInfo.mimeType;
				if (EMimeDataType.TEXT.equals(mimeInfo.dataType)) {

					//-- Configure the MimeType of text with the character set
					// the mimeType = mimeType + "; Charset = UTF-8 ";
				}
				res.setContentType(mimeType);

				// Too eat up memory and once bytes from the file, so commenting
				// out byte[] data = getFileByte (file); Sets the stream to file
				InputStream inputStreamFromFile = getInputStreamFromFile(file);
				rawResponse.responseData = inputStreamFromFile;
			}
		} else {

			//-- If the directory or file does not exist
			throw new HttpServerException(HttpServerDef.HTTP_404_NOTFOUND, "404 File not found. uri=" + uri);
		}
		return rawResponse;
	}

	private InputStream getInputStreamFromFile(File file) {
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		try {
			fis = new FileInputStream(file);
			bis = new BufferedInputStream(fis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return bis;
	}

	private String getFileExtensionLowerCase(String fileName) {
		int exPeriodPos = fileName.lastIndexOf(".");
		String extention = CoString.right(fileName, fileName.length() - exPeriodPos).toLowerCase();
		return extention;
	}

	private String getFileBody(String fileName) {
		int exPeriodPos = fileName.lastIndexOf(".");
		String extention = CoString.left(fileName, exPeriodPos);
		return extention;
	}

	/**
	 * * processing firstStr-secondStr
	 * 
	 * @param firstStr
	 * @param secondStr
	 * @return
	 */
	private String subString(String firstStr, String secondStr) {

		if (firstStr.startsWith(secondStr)) {
			String leave = CoString.right(firstStr, firstStr.length() - secondStr.length());
			return leave;
		} else {
			return firstStr;
		}
	}
}
