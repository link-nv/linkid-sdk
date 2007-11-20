/*
 * SafeOnline project. Copyright 2006-2007 Lin.k N.V. All rights reserved. Lin.k N.V.
 * proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.drivers;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.transform.TransformerException;

import net.link.safeonline.sdk.trust.SafeOnlineTrustManager;
import net.link.safeonline.util.jacc.ProfileData;
import net.link.safeonline.util.jacc.ProfileDataLockedException;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;
import org.w3c.tidy.DOMTextImpl;
import org.w3c.tidy.Tidy;

/**
 * @author mbillemo
 */
public class AuthDriver extends ProfileDriver {
	static final Log LOG = LogFactory.getLog(AuthDriver.class);

	private HttpClient client;
	private List<ProfileData> iterationDatas;
	private String location;
	private Tidy tidy;
	private XPathUtil xpath;

	public AuthDriver(String hostname) {

		super(hostname, "Authentication Driver");

		SafeOnlineTrustManager.configureSsl();

		Protocol.registerProtocol("https", new Protocol("https",
				new MySSLSocketFactory(), 443));

		this.tidy = new Tidy();
		this.xpath = new XPathUtil();
		this.client = new HttpClient();
		this.iterationDatas = new ArrayList<ProfileData>();

		this.tidy.setQuiet(true);
		this.tidy.setShowWarnings(false);
	}

	public static class MySSLSocketFactory implements ProtocolSocketFactory {

		public Socket createSocket(String host, int port) throws IOException,
				UnknownHostException {
			LOG.debug("createSocket: " + host + ":" + port);
			return null;
		}

		public Socket createSocket(String host, int port,
				InetAddress localAddress, int localPort) throws IOException,
				UnknownHostException {
			LOG.debug("createSocket: " + host + ":" + port + ", local: "
					+ localAddress + ":" + localPort);
			return null;
		}

		public Socket createSocket(String host, int port,
				InetAddress localAddress, int localPort,
				HttpConnectionParams params) throws IOException,
				UnknownHostException, ConnectTimeoutException {
			LOG.debug("createSocket: " + host + ":" + port + ", local: "
					+ localAddress + ":" + localPort + ", params: " + params);
			SSLSocketFactory sslSocketFactory = HttpsURLConnection
					.getDefaultSSLSocketFactory();
			Socket socket = sslSocketFactory.createSocket(host, port,
					localAddress, localPort);
			return socket;
		}
	}

	/**
	 * Authenticate with OLAS's auth-webapp.
	 * 
	 * @return The user's UUID.
	 */
	public String login(String application, String username, String password)
			throws DriverException {

		Node reply;
		String submit, action = "/olas-auth/entry", method = "get";
		String[] keys, values;
		Map<String, String> data;

		startNewIteration();
		try {
			LOG.debug("Requesting First Page.");
			keys = new String[] { "application", "target" };
			values = new String[] { application, "" };
			reply = request(action, method, keys, values);

			if (this.location.startsWith("first-time")) {
				LOG.debug("Received Login Modes (new/existing user)");
				data = getHiddenFormData(reply);
				action = this.xpath.getString(reply, "@action");
				method = this.xpath.getString(reply, "@method");
				submit = getFieldName("submit", "existing", reply);
				data.put(submit, null);

				LOG.debug("Selecting Existing User");
				keys = data.keySet().toArray(new String[0]);
				values = data.values().toArray(new String[0]);
				reply = request(action, method, keys, values);
			}

			LOG.debug("Received Authentication Devices");
			data = getHiddenFormData(reply);
			action = this.xpath.getString(reply, "@action");
			method = this.xpath.getString(reply, "@method");
			submit = getFieldName("submit", "next", reply);
			String deviceValue = "password";
			String deviceKey = this.xpath.getString(reply,
					".//input[@type='radio' and @value='%s']/@name",
					deviceValue);
			data.put(deviceKey, deviceValue);
			data.put(submit, "");

			LOG.debug("Selecting Password Device");
			keys = data.keySet().toArray(new String[0]);
			values = data.values().toArray(new String[0]);
			reply = request(action, method, keys, values);

			LOG.debug("Received Authentication Device Fields");
			data = getHiddenFormData(reply);
			action = this.xpath.getString(reply, "@action");
			method = this.xpath.getString(reply, "@method");
			submit = getFieldName("submit", "login", reply);
			String userKey = getFieldName("text", "username", reply);
			String passKey = getFieldName("password", "password", reply);
			data.put(userKey, username);
			data.put(passKey, password);
			data.put(submit, "");

			LOG.debug("Submitting Authentication Credentials for Fields");
			keys = data.keySet().toArray(new String[0]);
			values = data.values().toArray(new String[0]);
			reply = request(action, method, keys, values);
			if (reply instanceof ResultNode)
				return reply.getNodeValue(); // Possible exit.

			LOG.debug("Received Usage Agreement");
			data = getHiddenFormData(reply);
			action = this.xpath.getString(reply, "@action");
			method = this.xpath.getString(reply, "@method");
			submit = getFieldName("submit", "confirm", reply);
			data.put(submit, null);

			LOG.debug("Confirming Usage Agreement");
			keys = data.keySet().toArray(new String[0]);
			values = data.values().toArray(new String[0]);
			reply = request(action, method, keys, values);

			LOG.debug("Received Attributes");
			data = getHiddenFormData(reply);
			action = this.xpath.getString(reply, "@action");
			method = this.xpath.getString(reply, "@method");
			submit = getFieldName("submit", "agree", reply);
			data.put(submit, null);

			LOG.debug("Agreeing on Usage of Attributes");
			keys = data.keySet().toArray(new String[0]);
			values = data.values().toArray(new String[0]);
			reply = request(action, method, keys, values);
			if (reply instanceof ResultNode)
				return reply.getNodeValue(); // Possible exit.

			throw new DriverException(
					"Expected authentication cycle to have ended by now.");
		}

		catch (Exception e) {
			setIterationError(e);

			if (e instanceof DriverException)
				throw (DriverException) e;
			throw new DriverException(e);
		}

		finally {
			ProfileData iterationData = new ProfileData();
			for (ProfileData requestData : this.iterationDatas)
				for (Map.Entry<String, Long> measurement : requestData
						.getMeasurements().entrySet())
					try {
						String key = measurement.getKey();
						Long value = measurement.getValue();

						if (ProfileData.isRequestKey(key)
								&& iterationData.getMeasurements().containsKey(
										key))
							value += iterationData.getMeasurements().get(key);

						iterationData.addMeasurement(measurement.getKey(),
								measurement.getValue());
					} catch (ProfileDataLockedException e) {
						setIterationError(e);
					}

			setIterationData(iterationData);
		}
	}

	private String getFieldName(String type, String nameKey, Node form)
			throws TransformerException {

		return this.xpath.getString(form,
				".//input[@type='%s' and contains(@name,':%s')]/@name", type,
				nameKey);
	}

	private Map<String, String> getHiddenFormData(Node form)
			throws TransformerException {

		List<Node> hiddenNodes = this.xpath.getNodes(form,
				"input[@type='hidden']");

		Map<String, String> data = new HashMap<String, String>();
		for (Node hiddenNode : hiddenNodes) {
			String dataKey = this.xpath.getString(hiddenNode, "@name");
			String dataValue = this.xpath.getString(hiddenNode, "@value");

			data.put(dataKey, dataValue);
		}

		return data;
	}

	private Node request(String path, String method, String[] keys,
			String[] values) throws HttpException, IOException,
			TransformerException, DriverException {

		// Gather the parameters for the method.
		List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		if (null != keys && null != values)
			for (int pair = 0; pair < keys.length; ++pair)
				parameters.add(new NameValuePair(keys[pair], values[pair]));

		// Initialize the request method.
		HttpMethod request;
		String url;
		if (path.startsWith("https://"))
			url = path;
		else
			url = String.format("https://%s%s", this.host, path);
		if (null == method || "get".equalsIgnoreCase(method)) {
			request = new GetMethod(url);
			request.setQueryString(parameters.toArray(new NameValuePair[0]));
		} else if ("post".equalsIgnoreCase(method)) {
			PostMethod postRequest = new PostMethod(url);
			postRequest.addParameters(parameters.toArray(new NameValuePair[0]));
			request = postRequest;
		} else
			throw new IllegalArgumentException(
					"'method' argument must be either GET or POST (or NULL).");

		// Execute the request.
		request.setFollowRedirects(false);

		this.client.executeMethod(request);

		// Retrieve the performance headers.
		Map<String, List<String>> requestHeaders = new HashMap<String, List<String>>();
		for (Header header : request.getResponseHeaders()) {
			List<String> headerValues = new ArrayList<String>();
			headerValues.add(header.getValue());

			requestHeaders.put(header.getName(), headerValues);
		}
		this.iterationDatas.add(new ProfileData(requestHeaders));

		// Perform a manual redirect if required,
		// but first check if we're coming from the auth-webapp exit.
		Header redirect = request.getResponseHeader("Location");
		if (null != redirect && redirect.getValue().length() > 0) {
			if (request.getURI().toString().endsWith("/exit")) {
				String redirection = request.getResponseHeader("Location")
						.getValue();
				String uuid = redirection.replaceFirst(
						".*[?&]username=([^&]*).*", "$1");

				return new ResultNode(uuid); // Authentication Done.
			}

			return request(redirect.getValue(), null, null, null);
		}

		// Parse the result into an HTML DOM.
		Node root = this.tidy.parseDOM(request.getResponseBodyAsStream(), null);
		this.xpath.renew();

		// Check for errors; if any, throw them.
		String error = this.xpath.getString(root, "//*[@class='error']");
		if ((null == error || error.length() == 0)
				&& this.xpath.getString(root, "/html/head/title").endsWith(
						"Error report"))
			error = this.xpath.getString(root, "//h1");
		if (null != error && error.length() > 0)
			throw new DriverException(path + ": " + error);

		this.location = request.getURI().toString().replaceFirst(".*/", "");
		return this.xpath.getNode(root, "//form");
	}

	static class ResultNode extends DOMTextImpl {

		private static final long serialVersionUID = 1L;

		public ResultNode(String uuid) {

			super(new org.w3c.tidy.Node(org.w3c.tidy.Node.TEXT_NODE, uuid
					.getBytes(), 0, uuid.length()));
		}
	}
}
