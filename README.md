# Overview
## JMWS(Java micro web server) is a simple httpd for Java.
- Web server
- Web app server - Host your <b>Micro Service</b> like <b>REST API</b>
- Has basic http operations POST/GET
- Supports <b>[RFC 2388](http://www.ietf.org/rfc/rfc2388.txt)</b> multipart message.<br/>(<b>File uploading</b> and something.)

## Purpose
- Debug your web site
- Debug your micro services logic
- Debug your front end apps - JMWS runs straightforward without unnecessary caching.
- Embedded server

It is licensed under [MIT](https://opensource.org/licenses/MIT).

# Quick start

## Example Code 1 : Publish your directory to WEB

- Run Example1.java and open <b>http://localhost</b> with your browser.

### Example1.java

```java
package com.example;

import org.riversun.jmws.WebServer;

public class Example1 {

	public static void main(String[] args) {

		final int port = 80;
		final WebServer server = new WebServer(port);

		// Publish "/usr/local/htdocs" for "/"
		server.addDirectory("/", "/usr/local/htdocs");

		server.startServer();
	}
}

```

## Example Code 2 : Publish your <b>REST API( GET Style)</b> to WEB

- Run Example2.java and open<br/> <b>http://localhost/example?str1=apple&str2=pen</b><br/>with your browser.<br><br>
- You will get
```
{"result":"apple-pen" }
```

### Example2.java
```java
package com.example;

import java.io.*;
import org.riversun.jmws.*;
import org.riversun.jmws.core.*;

public class Example2 {

	public static void main(String[] args) {

		final int port = 80;

		final WebServer server = new WebServer(port);

		server.addService("/example", new MicroService() {

			@Override
			public void service(HttpReq req, HttpRes res) throws Exception {

				res.setContentType("application/json; charset=UTF-8");

				String str1 = req.asString("str1");
				String str2 = req.asString("str2");

				String resultText = (str1 != null ? str1 : "") + "-" + (str2 != null ? str2 : "");

				String responseJsonText = "{\"result\":\"" + resultText + "\" }";

				OutputStream os = res.getOutputStream();
				PrintWriter out = new PrintWriter(os);
				out.println(responseJsonText);
				out.flush();
				out.close();

			}
		});

		server.startServer();
	}
}


```

## Example Code 3 : Publish your <b>REST API (JSON POST Style)</b> to WEB

### Example3.java
```java
package com.example;

import java.io.*;
import org.riversun.jmws.*;
import org.riversun.jmws.core.*;

public class Example3
{
	public static void main(String[] args)
	{

		final int port = 80;

		final WebServer server = new WebServer(port);

		server.addService("/json_api", new MicroService() {

			@Override
			public void service(HttpReq req, HttpRes res) throws Exception {

				// Do handle request
				final StringBuffer sb = new StringBuffer();

				String line = null;
				BufferedReader reader = req.getReader();
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
				final String requestJsonText = sb.toString();

				System.out.println(requestJsonText);

				// Do respond
				res.setContentType("application/json; charset=UTF-8");

				String responseJsonText = "{\"result\":\"" + "somthing" + "\" }";

				OutputStream os = res.getOutputStream();
				PrintWriter out = new PrintWriter(os);
				out.println(responseJsonText);
				out.flush();
				out.close();

			}
		});

	}
}

```

## Tips

- If you want to stop the server.
```Java
server.stopServer();
```

- Add listener for server status callback
```Java
server.setServerCallback(new ServerCallBack() {
			@Override
			public void onServerStatUpdated(EServerStatus serverStatus, String message) {
				System.out.println("serverStatus=" + serverStatus + " message=" + message);
			}
		});
```

# Downloads
## maven
- You can add dependencies to maven pom.xml file.

```xml
<dependency>
  <groupId>org.riversun</groupId>
  <artifactId>jmws</artifactId>
  <version>1.1.0</version>
</dependency>
```
