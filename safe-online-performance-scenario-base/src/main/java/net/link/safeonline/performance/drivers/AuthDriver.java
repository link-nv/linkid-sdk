/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.drivers;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.transform.TransformerException;

import net.link.safeonline.performance.DriverException;
import net.link.safeonline.performance.entity.ExecutionEntity;
import net.link.safeonline.sdk.DomUtils;
import net.link.safeonline.sdk.auth.saml2.AuthnRequestFactory;
import net.link.safeonline.util.performance.ProfileData;
import net.link.safeonline.util.performance.ProfileDataLockedException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
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

	private String response;

	public AuthDriver(ExecutionEntity execution) {

		super("Authentication Driver", execution);

		Protocol.registerProtocol("https", new Protocol("https",
				new MySSLSocketFactory(), 443));

		this.tidy = new Tidy();
		this.tidy.setQuiet(true);
		this.tidy.setShowWarnings(false);

		this.client = new HttpClient();
	}

	public static class MySSLSocketFactory implements ProtocolSocketFactory {

		private final SSLSocketFactory sslSocketFactory;

		public MySSLSocketFactory() {

			try {
				TrustManager trustManager = new MyTrustManager();
				TrustManager[] trustManagers = { trustManager };

				SSLContext sslContext = SSLContext.getInstance("TLS");
				sslContext.init(null, trustManagers, new SecureRandom());
				this.sslSocketFactory = sslContext.getSocketFactory();
			}

			catch (GeneralSecurityException e) {
				throw new RuntimeException(e);
			}
		}

		public Socket createSocket(String host, int port) throws IOException,
				UnknownHostException {

			LOG.debug("createSocket: " + host + ":" + port);
			return this.sslSocketFactory.createSocket(host, port);
		}

		public Socket createSocket(String host, int port,
				InetAddress localAddress, int localPort) throws IOException,
				UnknownHostException {

			LOG.debug("createSocket: " + host + ":" + port + ", local: "
					+ localAddress + ":" + localPort);
			return this.sslSocketFactory.createSocket(host, port, localAddress,
					localPort);
		}

		public Socket createSocket(String host, int port,
				InetAddress localAddress, int localPort,
				HttpConnectionParams params) throws IOException,
				UnknownHostException, ConnectTimeoutException {

			LOG.debug("createSocket: " + host + ":" + port + ", local: "
					+ localAddress + ":" + localPort + ", params: " + params);

			if (null != params && params.getConnectionTimeout() != 0)
				throw new IllegalArgumentException("Timeout is not supported.");

			return this.sslSocketFactory.createSocket(host, port, localAddress,
					localPort);
		}
	}

	static class MyTrustManager implements X509TrustManager {

		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {

			throw new CertificateException("cannot verify client certificates");
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {

			if (null == chain)
				throw new CertificateException("null certificate chain");
			if (0 == chain.length)
				throw new CertificateException("empty certificate chain");
			if (null == authType)
				throw new CertificateException("null authentication type");
			if (0 == authType.length())
				throw new CertificateException("empty authentication type");

			LOG.debug("server certificate: " + chain[0].getSubjectDN());
		}

		public X509Certificate[] getAcceptedIssuers() {

			return new X509Certificate[0];
		}
	}

	/**
	 * Authenticate with OLAS's auth-webapp.
	 * 
	 * @return The user's UUID.
	 */
	public String login(PrivateKeyEntry application, String applicationName,
			String username, String password) {

		this.iterationDatas = new ArrayList<ProfileData>();

		try {
			// Prepare authentication request token.
			PublicKey publicKey = application.getCertificate().getPublicKey();
			PrivateKey privateKey = application.getPrivateKey();
			KeyPair keyPair = new KeyPair(publicKey, privateKey);
			String uri = String.format("https://%s/olas-auth/entry", getHost());
			String authnRequest = AuthnRequestFactory.createAuthnRequest(
					applicationName, keyPair, "http://www.lin-k.net/"
							+ applicationName, uri, null, null);
			String encodedAuthnRequest = new String(Base64
					.encodeBase64(authnRequest.getBytes()));

			// Request the JSessionID cookie.
			PostMethod postMethod = new PostMethod(uri);
			postMethod.setRequestHeader("Cookie", "deflowered=true");
			postMethod.addParameter(new NameValuePair("SAMLRequest",
					encodedAuthnRequest));

			LOG.debug("Making initial request for:");
			LOG.debug(" - Application: " + applicationName);
			LOG.debug(" - At URI: " + uri);
			executeRequest(postMethod, null);
			String jsessionId = getJSessionId();

			try {
				// Receive devices list.
				postMethod = redirectPostMethod(postMethod);
				Node formNode = executeRequest(postMethod, jsessionId);

				// Submit password device selection.
				LOG.debug("Select Password Device:");
				Node passwordInputNode = XPathAPI.selectSingleNode(formNode,
						"//input[@type='radio' and @value='password']");
				postMethod = submitFormMethod(formNode, passwordInputNode);
				formNode = executeRequest(postMethod, jsessionId);

				// Submit username & password.
				String usernameKey = XPathAPI.eval(formNode,
						"//input[@type='text']/@name").str();
				String passwordKey = XPathAPI.eval(formNode,
						"//input[@type='password']/@name").str();
				postMethod = submitFormMethod(formNode, new NameValuePair[] {
						new NameValuePair(usernameKey, username),
						new NameValuePair(passwordKey, password) });
				formNode = executeRequest(postMethod, jsessionId);

				// This redirect sends us either the SAML response
				// or confirmation request (EULA + Attributes Confirmation).
				GetMethod getMethod = redirectGetMethod(postMethod);
				formNode = executeRequest(getMethod, jsessionId);
				if (null == XPathAPI.selectSingleNode(formNode,
						"//input[@type='hidden' and @name='SAMLResponse']")) {
					LOG.debug("One-time confirmation request.");
					postMethod = submitFormMethod(formNode);
					formNode = executeRequest(postMethod, jsessionId);

					postMethod = submitFormMethod(formNode);
					formNode = executeRequest(postMethod, jsessionId);

					getMethod = redirectGetMethod(postMethod);
					formNode = executeRequest(getMethod, jsessionId);
				}

				if (null == XPathAPI.selectSingleNode(formNode,
						"//input[@type='hidden' and @name='SAMLResponse']"))
					throw new DriverException("Expected a SAMLResponse!");

				// Retrieve and decode the SAML response.
				String encodedSamlResponseValue = XPathAPI.eval(formNode,
						"//input[@name='SAMLResponse']/@value").str();
				String samlResponseValue = new String(Base64
						.decodeBase64(encodedSamlResponseValue.getBytes()));
				LOG.debug("SAML Response: " + samlResponseValue);

				// Parse the subject name out of the SAML response.
				Document samlResponse = DomUtils
						.parseDocument(samlResponseValue);
				Element nsElement = samlResponse.createElement("nsElement");
				nsElement.setAttributeNS(Constants.NamespaceSpecNS,
						"xmlns:samlp", "urn:oasis:names:tc:SAML:2.0:protocol");
				nsElement.setAttributeNS(Constants.NamespaceSpecNS,
						"xmlns:saml", "urn:oasis:names:tc:SAML:2.0:assertion");
				Node subjectNameNode = XPathAPI
						.selectSingleNode(
								samlResponse,
								"/samlp:Response/saml:Assertion/saml:Subject/saml:NameID",
								nsElement);

				String subjectName = subjectNameNode.getTextContent();
				LOG.debug("subject name: " + subjectName);
				return subjectName;
			}

			catch (NullPointerException e) {
				LOG.error("Unexpected reply:\n" + this.response, e);
				throw e;
			}
		}

		catch (Exception e) {
			report(e);
		}

		finally {
			ProfileData iterationData = new ProfileData();
			for (ProfileData requestData : this.iterationDatas)
				for (Map.Entry<String, Long> measurement : requestData
						.getMeasurements().entrySet())

					try {
						String key = measurement.getKey();
						Long value = measurement.getValue();

						// Sum non-request times and DELTA_TIME request time.
						if (!ProfileData.isRequestKey(key)
								|| ProfileData.REQUEST_DELTA_TIME.equals(key))
							value += iterationData.getMeasurement(key);

						iterationData.addMeasurement(key, value);
					} catch (ProfileDataLockedException e) {
					}

			report(iterationData);
		}

		return null;
	}

	private GetMethod redirectGetMethod(HttpMethod postMethod)
			throws DriverException {

		return new GetMethod(redirectMethod(postMethod));
	}

	private PostMethod redirectPostMethod(HttpMethod postMethod)
			throws DriverException {

		return new PostMethod(redirectMethod(postMethod));
	}

	private String redirectMethod(HttpMethod postMethod) throws DriverException {

		Header locationHeader = postMethod.getResponseHeader("Location");
		if (null == locationHeader)
			throw new DriverException("Expected a redirect.");

		return locationHeader.getValue();
	}

	private String getJSessionId() throws DriverException {

		for (Cookie cookie : this.client.getState().getCookies())
			if ("JSESSIONID".equals(cookie.getName()))
				return cookie.getValue();

		throw new DriverException("The JSessionID Cookie was not set!");
	}

	private PostMethod submitFormMethod(Node formNode,
			Node... additionalInputNodes) throws TransformerException {

		NameValuePair[] additionalInputValues = new NameValuePair[additionalInputNodes.length];
		for (int i = 0; i < additionalInputNodes.length; ++i) {
			NamedNodeMap attributes = additionalInputNodes[i].getAttributes();
			String name = attributes.getNamedItem("name").getNodeValue();
			String value = attributes.getNamedItem("value").getNodeValue();

			LOG.debug(" - " + name + " = " + value);
			additionalInputValues[i] = new NameValuePair(name, value);
		}

		return submitFormMethod(formNode, additionalInputValues);
	}

	private PostMethod submitFormMethod(Node formNode,
			NameValuePair[] additionalInputValues) throws TransformerException {

		// Create the post method off of the form's action value.
		String uri = formNode.getAttributes().getNamedItem("action")
				.getNodeValue();
		if (!uri.startsWith("http"))
			uri = String.format("https://%s%s", getHost(), uri);
		PostMethod postMethod = new PostMethod(uri);
		postMethod.addParameters(additionalInputValues);
		LOG.debug(" - URI: " + uri);

		// Enumerate hidden form fields.
		Node hiddenInputNode;
		NodeIterator hiddenInputNodeIterator = XPathAPI.selectNodeIterator(
				formNode, "//input[@type='hidden']");

		// Add all these fields as parameters to the post method.
		while (null != (hiddenInputNode = hiddenInputNodeIterator.nextNode())) {
			NamedNodeMap attributes = hiddenInputNode.getAttributes();
			String name = attributes.getNamedItem("name").getNodeValue();
			String value = attributes.getNamedItem("value").getNodeValue();

			LOG.debug(" - " + name + " = " + value);
			postMethod.addParameter(name, value);
		}

		// Add the submit parameter.
		String submitName = XPathAPI.eval(formNode,
				"//input[@type='submit']/@name").str();
		if (null != submitName && submitName.length() > 0)
			postMethod.addParameter(submitName, "");

		return postMethod;
	}

	private Node executeRequest(HttpMethodBase method, String jsessionId)
			throws HttpException, IOException, TransformerException,
			DriverException {

		// Optionally add JSessionID cookie and execute method.
		if (null != jsessionId)
			method.addRequestHeader("Cookie", "JSESSIONID=" + jsessionId);
		this.client.executeMethod(method);

		// Parse response headers for profile data.
		Map<String, List<String>> requestHeaders = new HashMap<String, List<String>>();
		for (Header header : method.getResponseHeaders()) {
			List<String> headerValues = new ArrayList<String>();
			headerValues.add(header.getValue());

			requestHeaders.put(header.getName(), headerValues);
		}
		this.iterationDatas.add(new ProfileData(requestHeaders));

		// Remember the last non-null response body for debugging purposes.
		String newResponse = method.getResponseBodyAsString();
		if (null != newResponse)
			this.response = newResponse;

		// Parse response body as DOM and extract form node.
		Document resultDocument = this.tidy.parseDOM(method
				.getResponseBodyAsStream(), null);
		if (null == resultDocument)
			return null;

		// Parse out HTTP/AS errors and throw them as exceptions.
		String error = XPathAPI.eval(resultDocument, "//*[@class='error']")
				.str();
		if (error.length() == 0) {
			error = XPathAPI.eval(resultDocument,
					"//title[contains(text(),'Error')]/text()").str();
			String errorReport = XPathAPI.eval(resultDocument, "//h1").str();
			if (error.endsWith("Error report") && errorReport.length() > 0)
				error = errorReport;
		}
		if (error.length() != 0)
			throw new DriverException(error);

		// Return the form node, if any.
		return XPathAPI.selectSingleNode(resultDocument, "//form");
	}
}
