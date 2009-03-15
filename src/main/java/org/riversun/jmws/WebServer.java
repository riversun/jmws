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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.riversun.jmws.common.HttpdLog;
import org.riversun.jmws.common.MimeInfo;
import org.riversun.jmws.common.ContentTypeResolver;
import org.riversun.jmws.core.HttpHandler;
import org.riversun.jmws.core.HttpReq;
import org.riversun.jmws.core.HttpRes;

/**
 * Http Server
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 * */
public class WebServer {

	private static String LOGTAG = WebServer.class.getSimpleName();
	private static final int MAX_HTTP_CHILD_THREAD = 20;

	private JmwsServerCallBack _callback;
	private ServerSocket _serverSocket = null;
	private Thread _serverThread = null;
	private final int _port;
	private boolean isRunning = false;

	private final Map<String, MicroService> _serviceMap = new ConcurrentHashMap<String, MicroService>();

	private final Map<String, String> _dirMap = new ConcurrentHashMap<String, String>();

	public int getPortNumber() {
		return _port;
	}

	public WebServer(int port) {
		HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#WebServer() construct jmws http server as port=" + port + " MAX_HTTP_CHILD_THREAD="
				+ MAX_HTTP_CHILD_THREAD, 3);
		_port = port;
	}

	/**
	 * Service
	 * 
	 * @param uri
	 *            to add a service (application logic) request * param service
	 */
	public void addService(String uri, MicroService service) {
		HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#addService() added service class for uri=" + uri + " service=" + service, 3);
		if (_serviceMap.containsKey(uri)) {

			// To remove existing ones when trying to register with the same URI
			MicroService jmwsHttpAppService = _serviceMap.get(uri);
			_serviceMap.remove(jmwsHttpAppService);
		}
		_serviceMap.put(uri, service);
	}

	/**
	 * @return to retrieve the list of registered Web service
	 */
	public List<ServiceInfo> getServiceList() {
		List<ServiceInfo> result = new ArrayList<ServiceInfo>();
		Set<String> serviceUriSet = _serviceMap.keySet();
		for (String serviceUri : serviceUriSet) {
			MicroService jmwsHttpAppService = _serviceMap.get(serviceUri);
			result.add(new ServiceInfo(serviceUri, jmwsHttpAppService));
		}
		return result;
	}

	public static class ServiceInfo {
		private final String uri;
		private final MicroService service;

		public String getUri() {
			return uri;
		}

		public MicroService getService() {
			return service;
		}

		public ServiceInfo(String uri, MicroService service) {
			super();
			this.uri = uri;
			this.service = service;
		}
	}

	/**
	 * * Access * to add the directory @param uri * request * param srcPath
	 * directories to be able
	 */
	public void addDirectory(String uri, String srcPath) {
		if (_dirMap.containsKey(uri)) {
			String string = _dirMap.get(uri);
			_dirMap.remove(string);
		}
		_dirMap.put(uri, srcPath);
	}

	/**
	 * * @return to get a directory listing
	 */
	public List<DirectoryInfo> getDirectoryList() {
		List<DirectoryInfo> result = new ArrayList<DirectoryInfo>();
		Set<String> directoryUriSet = _dirMap.keySet();
		for (String directoryUri : directoryUriSet) {
			String srcPath = _dirMap.get(directoryUri);
			result.add(new DirectoryInfo(directoryUri, srcPath));
		}
		return result;
	}

	public static class DirectoryInfo {
		private final String uri;
		private final String srcPath;

		public String getUri() {
			return uri;
		}

		public String getSrcPath() {
			return srcPath;
		}

		public DirectoryInfo(String uri, String srcPath) {
			super();
			this.uri = uri;
			this.srcPath = srcPath;
		}
	}

	public static enum EServerStatus {
		SUCCESSFULLY_STARTED, FAILED_TO_START, SUCCESSFULLY_STOPPED, STOPPED_BY_ERROR;
	}

	public static interface JmwsServerCallBack {
		public void onServerStatUpdated(EServerStatus serverStatus, String message);
	}

	public void setServerCallback(JmwsServerCallBack callback) {
		_callback = callback;
	}

	public boolean isRunning() {
		return isRunning;
	}

	// What request is accepted?
	private int mTotalReceiveCount = 0;

	public synchronized void startServer() {

		if (mUserStopInProgress) {

			HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#start() server start error. now server is stop in progress.", 3);

			if (_callback != null) {
				_callback.onServerStatUpdated(EServerStatus.FAILED_TO_START, "Failed to start.");
			}

			return;

		} else {

		}

		HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#start()", 3);

		if (_dirMap.size() == 0 && _serviceMap.size() == 0) {

			HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#start() Neither directory nor service has been added.Add welcome service.", 3);

			final MicroService welcomeService = new MicroService() {
				@Override
				public void service(HttpReq req, HttpRes res) {

					final MimeInfo htmlMimeType = ContentTypeResolver.getContentType(".html");

					res.setContentType(htmlMimeType.mimeType + "; charset=UTF-8");

					OutputStream os = res.getOutputStream();
					PrintWriter out = new PrintWriter(os);

					final String welcomeHtml = "<html>"
							+ "<title>Welcome to JMWS!</title>"
							+ "<body>"
							+ "<h1>It works</h1>"
							+ "Welcome to java-micro-webserver (JMWS)!"
							+ "<br/>"
							+ "<br/>"
							+ "Neither directory nor service has been added."
							+ "<br/><br/>"
							+ "<b>Example 1 : Add directory</b>"
							+ "<br/>"
							+ "WebServer#addDirectory(\"/\",\"/usr/local/htdocs\");"
							+ "<br/><br/>"
							+ "<b>Example 2 : Add service</b>"
							+ "<br/>"
							+ "WebServer#addService(\"/myapi\",new MiroService(){...});"
							+ "</body>"
							+ "</html>";

					out.println(welcomeHtml);
					out.flush();
				}
			};

			addService("/", welcomeService);

		}

		Runnable r = new Runnable() {
			@Override
			public void run() {
				final ExecutorService executor = Executors.newFixedThreadPool(MAX_HTTP_CHILD_THREAD);
				boolean errorFlag = false;

				// Creates a server-side Socket instance
				try {
					try {
						if (!mUserStopInProgress) {

							// If you notice start() the stop() and how to
							// measure to the null check
							_serverSocket = new ServerSocket(_port);
							_serverSocket.setReuseAddress(true);
						}
					} catch (BindException e) {

						// java.net.BindException: Address already in use:
						// JVM_Bind
						HttpdLog.stackTrace(e);
						HttpdLog.err(HttpdLog.CATE_HTTPD, LOGTAG + "#start() server socket cannot open. address already in use. e=" + e.getMessage(), 3);
						throw new Exception(e);
					}
					isRunning = true;
					while (true) {
						HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#start() server socket ready for port=" + _port
								+ " now waiting for new client access...", 3);
						if (mTotalReceiveCount == 0) {
							if (_callback != null) {
								_callback.onServerStatUpdated(EServerStatus.SUCCESSFULLY_STARTED, "Successfully started listening on port:" + _port);
							}
						}

						// If you notice start() the stop() and how to measure
						// to the null check
						if (_serverSocket != null) {

							// Waiting for connection
							Socket clientSocket = _serverSocket.accept();
							mTotalReceiveCount++;

							// Clientsocaet.setreuseaddress(true);-&gt; no
							// relationship since before the bind call
							HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#run() server socket client connected for port=" + _port, 3);

							// TODO connection per process to allocate the new
							// thread, but here uses the thread pool
							HttpHandler httpHandler = new HttpHandler(clientSocket, _serviceMap, _dirMap);
							executor.execute(httpHandler);
						} else {
							break;
						}
					}
				} catch (IOException e) {
					if (mUserStopInProgress) {
						HttpdLog.log(HttpdLog.CATE_HTTPD,
								LOGTAG + "#run() server socket close for stop in progress. stopInProgress=" + mUserStopInProgress, 3);
						errorFlag = false;
					} else {
						HttpdLog.err(HttpdLog.CATE_HTTPD, LOGTAG + "#run() server error occured. e=" + e.getMessage(), 3);

						// e. printStackTrace ();
						HttpdLog.stackTrace(e);
						errorFlag = true;
					}
				} catch (Exception e) {

					HttpdLog.stackTrace(e);
					errorFlag = true;
				} finally {
					isRunning = false;
					executor.shutdown();
					HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#run() thread finished", 3);
					if (_callback != null) {
						if (errorFlag) {
							_callback.onServerStatUpdated(EServerStatus.STOPPED_BY_ERROR, "Server stopped by error.Please see log.");
						} else {
							_callback.onServerStatUpdated(EServerStatus.SUCCESSFULLY_STOPPED, "Server successfully stopped.");
						}
					}
				}
			}
		};
		_serverThread = new Thread(r);
		_serverThread.setName("JmwsHttpServerThread");

		// setting myThread.setDaemon(true);// main thread after decease
		_serverThread.start();
	}

	private volatile boolean mUserStopInProgress = false;

	/**
	 * * Stops the server.
	 */
	public synchronized boolean stop() {
		HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#stop() called mUserStopInProgress=" + mUserStopInProgress, 3);
		if (!mUserStopInProgress) {
			mUserStopInProgress = true;
			try {
				if (_serverSocket != null) {
					_serverSocket.close();
				}
				if (_serverThread != null) {
					_serverThread.join();
				}
				_serverSocket = null;
				_serverThread = null;
				isRunning = false;
				HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#stop() server successfully stopped.", 3);
			} catch (IOException e) {

				HttpdLog.stackTrace(e);
				isRunning = false;
			} catch (InterruptedException e) {

				HttpdLog.stackTrace(e);
				isRunning = false;
			} finally {
				mUserStopInProgress = false;
			}
			return true;
		} else {
			HttpdLog.log(HttpdLog.CATE_HTTPD, LOGTAG + "#stop() called but now stop in progress. stopInProgress=" + mUserStopInProgress, 3);
			return false;
		}
	}
}
