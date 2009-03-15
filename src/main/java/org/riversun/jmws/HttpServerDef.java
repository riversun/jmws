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
package org.riversun.jmws;

/**
 * Const of server messages
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 * */
public class HttpServerDef {

	public static final String HTTP_200_OK = "200 OK";
	public static final String HTTP_400_BAD_REQUEST = "400 Bad Request";
	public static final String HTTP_403_FORBIDDEN = "403 Forbidden";
	public static final String HTTP_404_NOTFOUND = "404 Not Found";
	public static final String HTTP_500_INTERNAL_SERVER_ERROR = "500 Internal Server Error";
	public static final String HTTP_501_NOT_IMPLEMENTED = "501 Not Implemented";
	
	public static final String HTTP_METHOD_VERSION = "HTTP/1.0";
	public static final String HTTP_CONTENT_TYPE = "Content-Type";
	
	public static final String HTTP_SERVER_NAME = "JMWS";
}
