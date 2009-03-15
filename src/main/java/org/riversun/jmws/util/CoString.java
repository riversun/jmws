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
package org.riversun.jmws.util;

public class CoString {

	final String LOGTAG = CoString.class.getName();

	public static boolean isBlank(String str) {
		if (str == null) {
			return true;
		}
		if (str.equals("")) {
			return true;
		}
		return false;
	}

	public static boolean isNotBlank(String str) {
		if (str != null && !str.equals("")) {
			return true;
		}
		return false;
	}

	public static String left(String src, int cnt) {
		String retValue = "";
		if (src != null) {
			int length = src.length();
			if (cnt < length) {
				retValue = src.substring(0, cnt);
			} else {
				retValue = src;
			}
		}
		return retValue;
	}

	public static String right(String src, int cnt) {
		String retValue = "";
		if (src != null) {
			int length = src.length();
			if (cnt < length) {
				retValue = src.substring(length - cnt, length);
			} else {
				retValue = src;
			}
		}
		return retValue;
	}
}
