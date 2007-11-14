/*
 * SafeOnline project. Copyright 2006-2007 Lin.k N.V. All rights reserved. Lin.k N.V.
 * proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.drivers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.w3c.dom.Node;
import org.w3c.tidy.DOMTextImpl;
import org.w3c.tidy.Tidy;

/**
 * @author mbillemo
 */
public class AuthDriver extends ProfileDriver {

	private HttpClient client;
	private Tidy tidy;

	private XPathUtil xpath;

	public AuthDriver(String hostname) {

		super(hostname, "Authentication Driver");

		this.tidy = new Tidy();
		this.xpath = new XPathUtil();
		this.client = new HttpClient();

		this.tidy.setQuiet(true);
		this.tidy.setShowWarnings(false);
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

		try {
			// Request Modes (new/existing user).
			keys = new String[] { "application", "target" };
			values = new String[] { application, "" };
			reply = request(action, method, keys, values);

			// Select 'Existing User' Mode.
			data = getHiddenFormData(reply);
			action = this.xpath.getString(reply, "@action");
			method = this.xpath.getString(reply, "@method");
			submit = getFieldName("submit", "existing", reply);
			data.put(submit, null);

			// Request Devices.
			keys = data.keySet().toArray(new String[0]);
			values = data.values().toArray(new String[0]);
			reply = request(action, method, keys, values);

			// Select 'Password' Device.
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

			// Request Authentication Fields.
			keys = data.keySet().toArray(new String[0]);
			values = data.values().toArray(new String[0]);
			reply = request(action, method, keys, values);

			// Fill in fields.
			data = getHiddenFormData(reply);
			action = this.xpath.getString(reply, "@action");
			method = this.xpath.getString(reply, "@method");
			submit = getFieldName("submit", "login", reply);
			String userKey = getFieldName("text", "username", reply);
			String passKey = getFieldName("password", "password", reply);
			data.put(userKey, username);
			data.put(passKey, password);
			data.put(submit, "");

			// Request Usage Agreement.
			keys = data.keySet().toArray(new String[0]);
			values = data.values().toArray(new String[0]);
			reply = request(action, method, keys, values);
			if (reply instanceof ResultNode)
				return reply.getNodeValue();

			// Confirm Usage Agreement.
			data = getHiddenFormData(reply);
			action = this.xpath.getString(reply, "@action");
			method = this.xpath.getString(reply, "@method");
			submit = getFieldName("submit", "confirm", reply);
			data.put(submit, null);

			// Request Attributes Confirmation.
			keys = data.keySet().toArray(new String[0]);
			values = data.values().toArray(new String[0]);
			reply = request(action, method, keys, values);

			// Agree on Usage of Attributes.
			data = getHiddenFormData(reply);
			action = this.xpath.getString(reply, "@action");
			method = this.xpath.getString(reply, "@method");
			submit = getFieldName("submit", "agree", reply);
			data.put(submit, null);

			// Request Usage Agreement.
			keys = data.keySet().toArray(new String[0]);
			values = data.values().toArray(new String[0]);
			reply = request(action, method, keys, values);
			if (reply instanceof ResultNode)
				return reply.getNodeValue();

			throw new DriverException(
					"Expected authentication cycle to have ended by now.");
		}

		catch (Exception e) {
			if (e instanceof DriverException)
				throw (DriverException) e;
			throw new DriverException(e);
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
		if (null != error && error.length() > 0)
			throw new DriverException(error);

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
