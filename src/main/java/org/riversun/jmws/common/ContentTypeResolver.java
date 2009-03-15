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
package org.riversun.jmws.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Detect the MimeType from ext
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 * */
public class ContentTypeResolver {
	public static final String MIME_TYPE_UNKOWN = "mime_type_unkown";
	static {
		initializeMimeMap();
	}
	private static Map<String, MimeInfo> __mimeMap;

	public static MimeInfo getContentType(String extension) {
		return __mimeMap.get(extension);
	}

	private static void initializeMimeMap() {

		// Document
		addMimeMap(".txt", "text/plain", EMimeDataType.TEXT);
		addMimeMap(".csv", "text/csv", EMimeDataType.TEXT);
		addMimeMap(".doc", "application/msword", EMimeDataType.BINARY);
		addMimeMap(".xls", "application/vnd.ms-excel", EMimeDataType.BINARY);
		addMimeMap(".ppt", "application/vnd.ms-powerpoint", EMimeDataType.BINARY);
		addMimeMap(".pdf", "application/pdf", EMimeDataType.BINARY);
		addMimeMap(".html", "text/html", EMimeDataType.TEXT);
		addMimeMap(".htm", "text/html", EMimeDataType.TEXT);
		addMimeMap(".css", "text/css", EMimeDataType.TEXT);
		addMimeMap(".js", "application/javascript", EMimeDataType.TEXT);
		addMimeMap(".xml", "application/xml", EMimeDataType.TEXT);

		// Images
		addMimeMap(".jpg", "image/jpeg", EMimeDataType.BINARY);
		addMimeMap(".jpeg", "image/jpeg", EMimeDataType.BINARY);
		addMimeMap(".png", "image/png", EMimeDataType.BINARY);
		addMimeMap(".png", "image/png", EMimeDataType.BINARY);
		addMimeMap(".bmp", "image/bmp", EMimeDataType.BINARY);
		addMimeMap(".ico", "image/vnd.microsoft.icon", EMimeDataType.BINARY);
		addMimeMap(".gif", "image/gif", EMimeDataType.BINARY);

		// Music
		addMimeMap(".mp3", "audio/mpeg", EMimeDataType.BINARY);
		addMimeMap(".m4a", "audio/mp4", EMimeDataType.BINARY);
		addMimeMap(".wav", "audio/x-wav", EMimeDataType.BINARY);
		addMimeMap(".mid", "audio/midi", EMimeDataType.BINARY);
		addMimeMap(".midi", "audio/midi", EMimeDataType.BINARY);
		addMimeMap(".mmf", "application/x-smaf", EMimeDataType.BINARY);

		// Videos
		addMimeMap(".mpg", "video/mpeg", EMimeDataType.BINARY);
		addMimeMap(".mpeg", "video/mpeg", EMimeDataType.BINARY);
		addMimeMap(".wmv", "video/x-ms-wmv", EMimeDataType.BINARY);
		addMimeMap(".swf", "application/x-shockwave-flash", EMimeDataType.BINARY);
		addMimeMap(".3g2", "video/3gpp2", EMimeDataType.BINARY);

		// Archive
		addMimeMap(".zip", "application/zip", EMimeDataType.BINARY);
		addMimeMap(".lha", "application/x-lzh", EMimeDataType.BINARY);
		addMimeMap(".lzh", "application/x-lzh", EMimeDataType.BINARY);
		addMimeMap(".tar", "application/x-tar", EMimeDataType.BINARY);
		addMimeMap(".tgz", "application/x-tar", EMimeDataType.BINARY);

		// Other
		addMimeMap(".exe", "application/octet-stream", EMimeDataType.BINARY);
		addMimeMap(MIME_TYPE_UNKOWN, "application/octet-stream", EMimeDataType.BINARY);
	}

	private static void addMimeMap(String extension, String mimeType, EMimeDataType dataType) {
		if (__mimeMap == null) {
			__mimeMap = new HashMap<String, MimeInfo>();
		}
		__mimeMap.put(extension, new MimeInfo(extension, mimeType, dataType));
	}
}
