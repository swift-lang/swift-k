package org.globus.cog.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

public class HTTPPost {
	private final static Logger logger = Logger.getLogger(HTTPPost.class);
	private final static String EOL = System.getProperty("line.separator");

	public String post(String url, Map args) throws MalformedURLException, IOException {
		return post(new URL(url), args);
	}

	/**
	 * Posts data args={parameterName=>parameterValue} to a url and then returns
	 * the result as a string
	 * 
	 * @param url
	 * @param args
	 * @return
	 * @throws IOException
	 */
	public String post(URL url, Map args) throws IOException {
		logger.debug("post");
		// URL connection channel.
		URLConnection urlConn = url.openConnection();
		DataOutputStream urlRequest;
		DataInputStream urlResponse;
		StringBuffer content = new StringBuffer();
		StringBuffer result = new StringBuffer();

		// Let the run-time system (RTS) know that we want input.
		urlConn.setDoInput(true);
		// Let the RTS know that we want to do output.
		urlConn.setDoOutput(true);
		// No caching, we want the real thing.
		urlConn.setUseCaches(false);
		// Specify the content type.
		urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

		// used to send POST output
		urlRequest = new DataOutputStream(urlConn.getOutputStream());

		// Iterate through each variableName (key)
		boolean isFirst = true;
		Iterator iVarNames = args.keySet().iterator();
		while (iVarNames.hasNext()) {
			String key = (String) iVarNames.next();
			Object valueObj = args.get(key);

			String[] values = null;
			if (valueObj instanceof String[]) {
				logger.debug("instanceof String[]");
				values = (String[]) valueObj;
			}
			else if (valueObj instanceof String) {
				logger.debug("instanceof String");
				values = new String[] { (String) valueObj };
			}
			else {
				logger.warn("valueObj should be a String or String[] using String.valueOf(valueObj)");
				values = new String[] { String.valueOf(valueObj) };
			}
			// for each value for this variable name
			for (int i = 0; values != null && i < values.length; i++) {
				String valueStr = values[i];
				if (logger.isDebugEnabled()) {
					logger.debug("valueStr=" + valueStr);
				}
				// put an & after each value, does not occur with the first
				// parameter name
				if (!isFirst) {
					content.append("&");
				}
				else {
					isFirst = false;
				}
				// put the variable name
				content.append(key);
				// = separates the variable name and the value
				content.append("=");
				// add an encoded value
				content.append(URLEncoder.encode(valueStr));
			}
		}
		// post our data
		urlRequest.writeBytes(content.toString());
		urlRequest.flush();
		urlRequest.close();

		// Get response data.
		urlResponse = new DataInputStream(urlConn.getInputStream());

		// now write the response to result
		String str;
		while (null != ((str = urlResponse.readLine()))) {
			result.append(str);
			result.append(EOL);
		}
		urlResponse.close();

		return result.toString();
	}

	/**
	 * A quick test of HTTPPost
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length != 1) {
			throw new IllegalArgumentException("You must specify a valid url to post the data to");
		}

		HTTPPost request = new HTTPPost();
		URL url;

		try {
			url = new URL(args[0]);
			Map data = new HashMap();
			data.put("var1", new String[] { "value_1.1", "value_1.2", "value_1.3" });
			data.put("var2", new String[] { "value_2.1", "value_2.2" });
			data.put("var3", new String[] { "value_3.1" });
			data.put("var4", "value_4.1");
			data.put("var=5", new Integer(5));
			String webPage = request.post(url, data);
			System.out.println(webPage);
		}
		catch (MalformedURLException urlException) {
			logger.fatal("Invalid URL", urlException);
			System.exit(1);
		}
		catch (IOException ioException) {
			logger.error("Error while posting data", ioException);
		}

		System.exit(0);
	}
}
