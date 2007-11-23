/*
 * SafeOnline project. Copyright 2006-2007 Lin.k N.V. All rights reserved. Lin.k N.V.
 * proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.drivers;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.transform.TransformerException;

import net.link.safeonline.sdk.DomUtils;
import net.link.safeonline.sdk.auth.saml2.AuthnRequestFactory;
import net.link.safeonline.util.jacc.ProfileData;
import net.link.safeonline.util.jacc.ProfileDataLockedException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Constants;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.w3c.tidy.Tidy;

/**
 * @author mbillemo
 */
public class AuthDriver extends ProfileDriver {
	static final Log LOG = LogFactory.getLog(AuthDriver.class);

	private HttpClient client;
	private List<ProfileData> iterationDatas;
	private Tidy tidy;

	public AuthDriver(String hostname) {
		super(hostname, "Authentication Driver");

		Protocol.registerProtocol("https", new Protocol("https",
				new MySSLSocketFactory(), 443));

		this.client = new HttpClient();
		this.iterationDatas = new ArrayList<ProfileData>();

		this.tidy = new Tidy();
		this.tidy.setQuiet(true);
		this.tidy.setShowWarnings(false);
	}

	public static class MySSLSocketFactory implements ProtocolSocketFactory {

		private final SSLSocketFactory sslSocketFactory;

		public MySSLSocketFactory() {
			try {
				SSLContext sslContext = SSLContext.getInstance("TLS");
				SecureRandom secureRandom = new SecureRandom();
				TrustManager trustManager = new MyTrustManager();
				TrustManager[] trustManagers = { trustManager };
				sslContext.init(null, trustManagers, secureRandom);
				this.sslSocketFactory = sslContext.getSocketFactory();
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException("no such algo");
			} catch (KeyManagementException e) {
				throw new RuntimeException("key error");
			}
		}

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

			Socket socket = this.sslSocketFactory.createSocket(host, port,
					localAddress, localPort);
			return socket;
		}
	}

	static class MyTrustManager implements X509TrustManager {

		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			throw new CertificateException("cannot verify client certificates");
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			if (null == chain) {
				throw new CertificateException("null certificate chain");
			}
			if (0 == chain.length) {
				throw new CertificateException("empty certificate chain");
			}
			if (null == authType) {
				throw new CertificateException("null authentication type");
			}
			if (0 == authType.length()) {
				throw new CertificateException("empty authentication type");
			}
			LOG.debug("server certificate: " + chain[0].getSubjectDN());
		}

		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	}

	/**
	 * Authenticate with OLAS's auth-webapp.
	 * 
	 * @return The user's UUID.
	 */
	public String login(PrivateKeyEntry application, String applicationName,
			String username, String password) throws DriverException {

		startNewIteration();
		try {
			/*
			 * Prepare authentication request token
			 */
			PublicKey publicKey = application.getCertificate().getPublicKey();
			PrivateKey privateKey = application.getPrivateKey();
			KeyPair keyPair = new KeyPair(publicKey, privateKey);
			String authnRequest = AuthnRequestFactory.createAuthnRequest(
					"performance-application", keyPair,
					"http://www.lin-k.net/performance-application", null, null);
			String encodedAuthnRequest = new String(Base64
					.encodeBase64(authnRequest.getBytes()));

			String uri = "https://" + this.host + "/olas-auth/entry";
			LOG.debug("URI: " + uri);
			PostMethod postMethod = new PostMethod(uri);
			postMethod.setRequestHeader("Cookie", "deflowered=true");
			postMethod.addParameter(new NameValuePair("SAMLRequest",
					encodedAuthnRequest));

			LOG.debug("initial request");
			int statusCode = executeRequest(postMethod, null);

			String jsessionId = getJSessionId();

			if (HttpStatus.SC_MOVED_TEMPORARILY != statusCode) {
				throw new DriverException("moved");
			}
			Header locationHeader = postMethod.getResponseHeader("Location");
			String locationValue = locationHeader.getValue();
			LOG.debug("location: " + locationValue);
			postMethod = new PostMethod(locationValue);

			postMethod.addRequestHeader("Cookie", "JSESSIONID=" + jsessionId);
			statusCode = executeRequest(postMethod, jsessionId);
			Document resultDocument = this.tidy.parseDOM(postMethod
					.getResponseBodyAsStream(), null);
			LOG.debug("result document: "
					+ DomUtils.domToString(resultDocument));

			Node formNode = XPathAPI.selectSingleNode(resultDocument, "//form");

			Node passwordInputNode = XPathAPI.selectSingleNode(formNode,
					"//input[@type='radio' and @value='password']");
			String passwordFieldName = passwordInputNode.getAttributes()
					.getNamedItem("name").getNodeValue();
			String passwordFieldValue = passwordInputNode.getAttributes()
					.getNamedItem("value").getNodeValue();
			LOG.debug("radio attribute: " + passwordFieldName + "="
					+ passwordFieldValue);

			postMethod = createFormPostMethod(formNode);
			postMethod.addParameter(new NameValuePair(passwordFieldName,
					passwordFieldValue));

			/*
			 * Select password device
			 */
			LOG.debug("select password device");
			statusCode = executeRequest(postMethod, jsessionId);

			resultDocument = this.tidy.parseDOM(postMethod
					.getResponseBodyAsStream(), null);
			LOG.debug("result document: "
					+ DomUtils.domToString(resultDocument));

			formNode = XPathAPI.selectSingleNode(resultDocument, "//form");
			postMethod = createFormPostMethod(formNode);
			Node usernameInputNode = XPathAPI.selectSingleNode(formNode,
					"//input[@type='text']");
			String usernameFieldName = usernameInputNode.getAttributes()
					.getNamedItem("name").getNodeValue();
			passwordInputNode = XPathAPI.selectSingleNode(formNode,
					"//input[@type='password']");
			passwordFieldName = passwordInputNode.getAttributes().getNamedItem(
					"name").getNodeValue();
			postMethod.addParameter(usernameFieldName, username);
			postMethod.addParameter(passwordFieldName, password);

			/*
			 * Enter password
			 */
			LOG.debug("enter password");
			statusCode = executeRequest(postMethod, jsessionId);

			locationHeader = postMethod.getResponseHeader("Location");
			locationValue = locationHeader.getValue();
			LOG.debug("location: " + locationValue);
			GetMethod getMethod = new GetMethod(locationValue);

			statusCode = executeRequest(getMethod, jsessionId);
			resultDocument = this.tidy.parseDOM(getMethod
					.getResponseBodyAsStream(), null);
			LOG.debug("result document: "
					+ DomUtils.domToString(resultDocument));

			formNode = XPathAPI.selectSingleNode(resultDocument, "//form");
			if (null == XPathAPI.selectSingleNode(formNode,
					"//input[@type='hidden' and @name='SAMLResponse']")) {
				/*
				 * Subscribe
				 */
				LOG.debug("Subscribe step");
				postMethod = createFormPostMethod(formNode);
				statusCode = executeRequest(postMethod, jsessionId);
				resultDocument = this.tidy.parseDOM(postMethod
						.getResponseBodyAsStream(), null);

				/*
				 * Exit
				 */
				locationHeader = postMethod.getResponseHeader("Location");
				locationValue = locationHeader.getValue();
				LOG.debug("location: " + locationValue);
				getMethod = new GetMethod(locationValue);
				statusCode = executeRequest(getMethod, jsessionId);
				resultDocument = this.tidy.parseDOM(getMethod
						.getResponseBodyAsStream(), null);

				LOG.debug("result document: "
						+ DomUtils.domToString(resultDocument));
				formNode = XPathAPI.selectSingleNode(resultDocument, "//form");
			}
			Node samlResponseInputNode = XPathAPI.selectSingleNode(formNode,
					"//input[@name='SAMLResponse']");
			String encodedSamlResponseValue = samlResponseInputNode
					.getAttributes().getNamedItem("value").getNodeValue();
			LOG.debug("encoded SAML Response: " + encodedSamlResponseValue);

			String samlResponseValue = new String(Base64
					.decodeBase64(encodedSamlResponseValue.getBytes()));
			LOG.debug("SAML Response: " + samlResponseValue);
			Document samlResponse = DomUtils.parseDocument(samlResponseValue);
			Element nsElement = samlResponse.createElement("nsElement");
			nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:samlp",
					"urn:oasis:names:tc:SAML:2.0:protocol");
			nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:saml",
					"urn:oasis:names:tc:SAML:2.0:assertion");
			Node subjectNameNode = XPathAPI.selectSingleNode(samlResponse,
					"/samlp:Response/saml:Assertion/saml:Subject/saml:NameID",
					nsElement);
			String subjectName = subjectNameNode.getTextContent();
			LOG.debug("subject name: " + subjectName);

			return subjectName;
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

	private String getJSessionId() throws DriverException {
		for (Cookie cookie : this.client.getState().getCookies()) {
			if ("JSESSIONID".equals(cookie.getName())) {
				String jsessionId = cookie.getValue();
				return jsessionId;
			}
		}
		throw new DriverException("no jsessionid cookie");
	}

	private PostMethod createFormPostMethod(Node formNode)
			throws TransformerException {
		NodeIterator hiddenInputNodeIterator = XPathAPI.selectNodeIterator(
				formNode, "//input[@type='hidden']");
		Node hiddenInputNode;
		List<NameValuePair> submitFields = new LinkedList<NameValuePair>();
		while (null != (hiddenInputNode = hiddenInputNodeIterator.nextNode())) {
			NamedNodeMap attributes = hiddenInputNode.getAttributes();
			String name = attributes.getNamedItem("name").getNodeValue();
			String value = attributes.getNamedItem("value").getNodeValue();
			LOG.debug("attribute: " + name + "=" + value);
			submitFields.add(new NameValuePair(name, value));
		}

		Node submitInputNode = XPathAPI.selectSingleNode(formNode,
				"//input[@type='submit']");
		Node submitNameNode = submitInputNode.getAttributes().getNamedItem(
				"name");
		if (null != submitNameNode) {
			String submitName = submitNameNode.getNodeValue();
			submitFields.add(new NameValuePair(submitName, ""));
		}

		String actionValue = formNode.getAttributes().getNamedItem("action")
				.getNodeValue();
		LOG.debug("action value: " + actionValue);
		if (false == actionValue.startsWith("http")) {
			actionValue = "https://" + this.host + actionValue;
		}
		PostMethod postMethod = new PostMethod(actionValue);
		postMethod.addParameters(submitFields.toArray(new NameValuePair[] {}));

		return postMethod;
	}

	private int executeRequest(HttpMethodBase method, String jsessionId)
			throws HttpException, IOException {
		if (null != jsessionId) {
			method.addRequestHeader("Cookie", "JSESSIONID=" + jsessionId);
		}
		int statusCode = this.client.executeMethod(method);
		Map<String, List<String>> requestHeaders = new HashMap<String, List<String>>();
		for (Header header : method.getResponseHeaders()) {
			List<String> headerValues = new ArrayList<String>();
			headerValues.add(header.getValue());

			requestHeaders.put(header.getName(), headerValues);
		}
		this.iterationDatas.add(new ProfileData(requestHeaders));
		return statusCode;
	}
}
