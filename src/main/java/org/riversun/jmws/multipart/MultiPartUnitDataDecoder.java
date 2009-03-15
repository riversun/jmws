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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.riversun.jmws.HttpServerDef;
import org.riversun.jmws.common.HttpdLog;
import org.riversun.jmws.core.HttpServerException;

/**
 * Retrieve multipart data from binarydata in the multipart data.
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 * */
public class MultiPartUnitDataDecoder {
	private static String CAT = "SERVER";
	private static String LOGTAG = MultiPartUnitDataOffsetDetector.class.getSimpleName();
	private static final char LF = '\n';
	private static final char CR = '\r';

	/**
	 * * unit data of multi-part binary data to obtain individual data parsed
	 * multipart.
	 * 
	 * @param multipartUnitBinaryData
	 * @return
	 * @throws HttpServerException
	 */
	public MultipartUnitData decodeMultipartUnitBinaryData(byte[] multipartUnitBinaryData) throws HttpServerException {
		final RawMultipartData decodeRawMultipartData = decodeRawMultipartData(multipartUnitBinaryData);
		final MultipartUnitData parseRawMultipartData = parseRawMultipartData(decodeRawMultipartData);
		return parseRawMultipartData;
	}

	/**
	 * * POJO of decodeRawMultipartData
	 * 
	 * @author Tomonori.Misawa@jp.sony.com
	 * 
	 */
	private class RawMultipartData {

		// Content type comes in these data (String): application/octet-stream
		public List<String> mpHeaderLineList = new ArrayList<String>();

		// Data itself. To judge from the mpPropertyList string becomes the
		// String and binary data can be
		public byte[] mpDBody;
	}

	/**
	 * * Get the (body) header and data from the multipart data that has been
	 * converted to binary.
	 * 
	 * @param multipartUnitBinaryData
	 * @return
	 */
	// Is the binary multipart data from the header and data (body)
	private RawMultipartData decodeRawMultipartData(byte[] multipartUnitBinaryData) {
		RawMultipartData result = new RawMultipartData();

		// Pointer was way currently scan string starts from any location, or
		int crrScanStringStartPointer = 0;
		int dataLength = multipartUnitBinaryData.length;
		for (int cursor = 0; cursor < dataLength; cursor++) {
			if (cursor + 1 < dataLength) {
				int iData0 = new Integer(multipartUnitBinaryData[cursor + 0]);
				int iData1 = new Integer(multipartUnitBinaryData[cursor + 1]);
				if (iData0 == CR && iData1 == LF) {

					// Now the string being scanned the last pointer (position
					// of)
					final int currentScanStringEndPointer = cursor + 1;
					byte[] strData = Arrays.copyOfRange(multipartUnitBinaryData, crrScanStringStartPointer, currentScanStringEndPointer);

					// This data would be multipart header data (like this
					// guy-&gt; content-type: application/octet-stream)
					String multipartPropertyLine = new String(strData);
					multipartPropertyLine = multipartPropertyLine.replaceAll("\\r", "").replaceAll("\\n", "");
					result.mpHeaderLineList.add(multipartPropertyLine);
					if (cursor + 3 < dataLength) {
						int iData2 = new Integer(multipartUnitBinaryData[cursor + 2]);
						int iData3 = new Integer(multipartUnitBinaryData[cursor + 3]);
						if (iData2 == CR && iData3 == LF) {

							// -- consisting of data and point data part to
							// begin
							final int dataStartPointer = (cursor + 3) + 1;
							final int dataEndPointer = dataLength - 1;
							byte[] multipartContent = Arrays.copyOfRange(multipartUnitBinaryData, dataStartPointer, dataEndPointer);
							result.mpDBody = multipartContent;

							// Data is attached at the end, so don't scan for
							// more
							break;
						}
					}
					crrScanStringStartPointer = currentScanStringEndPointer + 1;
				}
			}
		}
		return result;
	}

	/**
	 * * Parses the header and the data to obtain individual data of multipart
	 * 
	 * @param rawMultiPartData
	 * @return
	 * @throws HttpServerException
	 */

	private MultipartUnitData parseRawMultipartData(RawMultipartData rawMultiPartData) throws HttpServerException {
		Map<String, MultipartHeaderData> multipartHeaderMap = new LinkedHashMap<String, MultipartHeaderData>();
		List<String> mpPropertyLineList = rawMultiPartData.mpHeaderLineList;
		for (String mpPropertyLine : mpPropertyLineList) {

			String[] firstBlocks = mpPropertyLine.split(": ");
			if (firstBlocks.length == 2) {

				final String headerName = firstBlocks[0].toLowerCase();
				HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#parseRawMultipartData() parsing headerName->" + headerName, 3);

				String valueBlockData = firstBlocks[1];
				String[] valueBlocks = valueBlockData.split("; ");
				if (valueBlocks.length > 0) {

					// -- form-data; name = "file"; to get the "form-data"
					// filename = "test.png"
					final String headerValue = valueBlocks[0];
					final Map<String, String> attrMap = new LinkedHashMap<String, String>();
					for (int i = 1; i < valueBlocks.length; i++) {

						// -- This arrow "name =" file".
						String attrPairData = valueBlocks[i];
						HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#parseRawMultipartData() parsing attrPairData->" + attrPairData, 3);

						// separate file with name
						String[] attrPairBlocks = attrPairData.split("=");
						if (attrPairBlocks.length == 2) {
							String attrName = attrPairBlocks[0];
							String attrValue = attrPairBlocks[1].replaceAll("\"", "");
							HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#parseRawMultipartData() parsing attrName=" + attrName + " attrValue=" + attrValue, 3);
							attrMap.put(attrName, attrValue);
						}
					}
					MultipartHeaderData mpHeaderData = new MultipartHeaderData(headerName, headerValue, attrMap);
					multipartHeaderMap.put(headerName, mpHeaderData);
				} else {

					// Throws an error
					throw new HttpServerException(HttpServerDef.HTTP_400_BAD_REQUEST, "Illegal multipart headers.");
				}
			} else {

				// Throws an error
				throw new HttpServerException(HttpServerDef.HTTP_400_BAD_REQUEST, "Illegal multipart headers.");
			}
		}
		final MultipartUnitData mpUnitData = new MultipartUnitData(multipartHeaderMap, rawMultiPartData.mpDBody);
		return mpUnitData;
	}
}
