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
import java.util.List;

/**
 * Storage location of the multipart detector data (in bytes)
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 * */

public class MultiPartUnitDataOffsetDetector {
	private static final String MULTIPART_BOUNDARY_EX_STR = "--";
	private static final char LF = '\n';
	private static final char CR = '\r';

	/**
	 * * Gets the position of the multi-part data storage
	 * 
	 * @param boundaryStr
	 * @param reqContentData
	 * @return A list of the storage location of the multipart data offset
	 */
	// To get the data list multi-part multi-part data storage offset location
	protected List<MultiPartUnitDataOffset> getMultiPartUnitDataOffset(String boundaryStr, byte[] reqContentData) {
		final int reqContentDataLen = reqContentData.length;

		// Pointer was way currently scan string starts from any location, or
		int crrScanStringStartPointer = 0;
		final List<MultiPartUnitDataOffset> mpUnitDataOffsetList = new ArrayList<MultiPartUnitDataOffset>();
		MultiPartUnitDataOffset previousMpUnitDataOffset = null;
		MultiPartUnitDataOffset currentMpUnitDataOffset = null;

		// Starting position of the first to discover the boundaries, sandwiched
		// between the present boundary and the boundary of the following data
		// (pointer) and ending at (pointer) to store list
		for (int cursor = 0; cursor < reqContentDataLen; cursor++) {
			if (cursor + 1 < reqContentDataLen) {
				int iData0 = new Integer(reqContentData[cursor]);
				int iData1 = new Integer(reqContentData[cursor + 1]);
				if (iData0 == CR && iData1 == LF) {

					// Now the string being scanned the last pointer (position
					// of)
					final int currentScanStringEndPointer = cursor + 1;
					byte[] strData = Arrays.copyOfRange(reqContentData, crrScanStringStartPointer, currentScanStringEndPointer);
					String line = new String(strData);
					line = line.replaceAll("\\r", "").replaceAll("\\n", "");
					if (line.equals(MULTIPART_BOUNDARY_EX_STR + boundaryStr)) {

						// -- the first boundary of the way it was, or if
						final int mpBoundarySymbolStartPos = crrScanStringStartPointer;
						final int mpBoundarySymbolEndPos = currentScanStringEndPointer;

						// (Because when we found the boundary of the following
						// end pointer is to hold the previousPointer) can be
						// set only the starting point of the data portion
						currentMpUnitDataOffset = new MultiPartUnitDataOffset(mpBoundarySymbolEndPos + 1, 0);
						mpUnitDataOffsetList.add(currentMpUnitDataOffset);
						if (previousMpUnitDataOffset != null) {
							previousMpUnitDataOffset.dataEnd = mpBoundarySymbolStartPos - 1;
						}
					} else if (line.equals(MULTIPART_BOUNDARY_EX_STR + boundaryStr + MULTIPART_BOUNDARY_EX_STR)) {

						// -- do not add a pointer to data based on the
						// boundaries, because the data is no longer if the
						// boundary of the last
						final int mpBoundarySymbolStartPos = crrScanStringStartPointer;
						if (previousMpUnitDataOffset != null) {
							previousMpUnitDataOffset.dataEnd = mpBoundarySymbolStartPos - 1;
						}
					}
					previousMpUnitDataOffset = currentMpUnitDataOffset;
					crrScanStringStartPointer = currentScanStringEndPointer + 1;
				}
			}
		}
		return mpUnitDataOffsetList;
	}
}
