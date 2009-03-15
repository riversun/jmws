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

/**
 * Logger
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 * */
public class HttpdLog {

	public static final String CATE_HTTPD = "JMWS";

	public static boolean __logEnabled = false;

	public static void setLogEnabled(boolean val) {
		__logEnabled = val;
	}

	public static void log(String tag, String msg, int level) {
		if (__logEnabled) {
			Thread currentThread = Thread.currentThread();
			String currentThreadName = currentThread.getName();
			System.out.println(tag + "::" + currentThreadName + "::" + msg);
		}
	}

	public static void err(String tag, String msg, int level) {
		if (__logEnabled) {
			Thread currentThread = Thread.currentThread();
			String currentThreadName = currentThread.getName();
			System.err.println(tag + "::" + currentThreadName + "::" + msg);
		}
	}

	public static void stackTrace(Throwable e) {
		e.printStackTrace();
	}
}
