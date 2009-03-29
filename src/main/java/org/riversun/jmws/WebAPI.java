package org.riversun.jmws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import org.riversun.jmws.MicroService;
import org.riversun.jmws.core.HttpParam;
import org.riversun.jmws.core.HttpProtocolInfo;
import org.riversun.jmws.core.HttpReq;
import org.riversun.jmws.core.HttpRes;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.xml.XmlMapper;

/**
 * Base class for Web API(REST) <br>
 * Easy to handle XML/JSON request and XML/JSON response
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 *
 */
public abstract class WebAPI extends MicroService {

	private static final Logger LOGGER = Logger.getLogger(WebAPI.class.getName());

	// implicit objects[begin]============
	protected HttpReq request;
	protected HttpRes response;

	// implicit objects[end]============

	private final ObjectMapper mJsonMapper = new ObjectMapper();
	private final XmlMapper mXmlMapper = new XmlMapper();

	@Override
	public void service(HttpReq req, HttpRes res) throws Exception {
		LOGGER.fine("");
		request = req;
		response = res;

		if (isPreFlightRequest()) {

			LOGGER.fine("preflight request");

			if (isCORSEnabled()) {
				returnNothing();
				return;
			}
		}

		doService();
	}

	/**
	 * Should override this method
	 * 
	 * @throws Exception
	 */
	protected abstract void doService() throws Exception;

	/**
	 * Returns if crossOriginRequest
	 * 
	 * @return
	 */
	protected boolean isPreFlightRequest() {
		final HttpProtocolInfo protocolInfo = request.getProtocolInfo();
		return "OPTIONS".equals(protocolInfo.getMethod());

	}

	protected String asString(String parameterName) {
		return request.asString(parameterName);
	}

	protected Long asLong(String parameterName) {
		Long retVal = null;

		try {
			retVal = Long.parseLong(asString(parameterName));
		} catch (Exception e) {

		}
		return retVal;

	}

	protected Integer asInteger(String parameterName) {
		Integer retVal = null;

		try {
			retVal = Integer.parseInt(asString(parameterName));
		} catch (Exception e) {
		}
		return retVal;

	}

	public void setContentType(String contentType) {
		response.setContentType(contentType);
	}

	public void setContentTypeTo_HTML_UTF8() {
		setContentType("text/html; charset=UTF-8");
	}

	public void setContentTypeTo_JSON_UTF8() {
		setContentType("application/json; charset=UTF-8");
	}

	public void setContentTypeTo_XML_UTF8() {
		setContentType("application/xml; charset=UTF-8");
	}

	/**
	 * Returns whether CORS is enabled or not.
	 * 
	 * @return
	 */
	protected boolean isCORSEnabled() {
		final HttpParam header = response.getHeader("Access-Control-Allow-Origin");
		if (header != null && header.getKey() != null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Set CORS policy
	 * 
	 * @param value
	 *            specify like "*","https://xxxxxx.com"
	 */
	protected void setAccessControlAllowOrigin(String value) {
		response.addHeader("Access-Control-Allow-Origin", value);
		response.addHeader("Access-Control-Allow-Headers", "Content-Type");
	}

	/**
	 * Enable Cookie while CORS connection
	 * 
	 * @param enabled
	 */
	protected void setAccessControlAllowCredentials(boolean enabled) {
		if (enabled) {
			response.addHeader("Access-Control-Allow-Credentials", String.valueOf(enabled));
		}
	}

	/**
	 * Returns object as JSON
	 * 
	 * @param modelObj
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void returnAsJSON(Object modelObj) throws IOException {
		setContentTypeTo_JSON_UTF8();

		final PrintWriter out = response.getWriter();
		final ObjectMapper mapper = new ObjectMapper();
		final String json = mapper.writeValueAsString(modelObj);
		out.println(json);
		out.close();
	}

	/**
	 * Returns object as JSONP
	 * 
	 * @param callback
	 * @param modelObj
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void returnAsJSONP(String callback, Object modelObj) throws IOException {
		setContentTypeTo_JSON_UTF8();

		final PrintWriter out = response.getWriter();

		mJsonMapper.setSerializationInclusion(Include.NON_NULL);
		final String jsonp = callback + "(" + mJsonMapper.writeValueAsString(modelObj) + ");";
		out.println(jsonp);
		out.close();
	}

	/**
	 * Returns object as XML
	 * 
	 * @param modelObj
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void returnAsXML(Object modelObj) throws IOException {
		setContentTypeTo_XML_UTF8();

		final PrintWriter out = response.getWriter();

		final String xml = mXmlMapper.writeValueAsString(modelObj);
		out.println(xml);
		out.close();
	}

	protected void returnNothing() throws IOException {
		setContentTypeTo_JSON_UTF8();

		final PrintWriter out = response.getWriter();
		out.close();
	}

	/**
	 * Read request content as JSON object
	 * 
	 * @param valueType
	 * @return
	 * @throws Exception
	 */
	public <T> T asJSON(Class<T> valueType) throws Exception
	{
		final StringBuffer sb = new StringBuffer();

		String line = null;

		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (Exception e) {
		}

		final String inJson = sb.toString();

		return mJsonMapper.readValue(inJson, valueType);
	}

}
