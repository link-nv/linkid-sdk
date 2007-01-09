/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.user.servlet;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import net.link.safeonline.authentication.service.CredentialService;
import net.link.safeonline.user.servlet.IdentityServlet;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletMapping;

public class IdentityServletTest extends TestCase {

	private static final Log LOG = LogFactory.getLog(IdentityServletTest.class);

	private Server server;

	private String location;

	private HttpClient httpClient;

	private CredentialService mockCredentialServiceBean;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY,
				"org.shiftone.ooc.InitialContextFactoryImpl");

		InitialContext initialContext = new InitialContext();
		NamingEnumeration<NameClassPair> list = initialContext.list("");
		javax.naming.Context safeOnlineContext;
		if (list.hasMore()) {
			safeOnlineContext = (javax.naming.Context) initialContext
					.lookup("SafeOnline");
		} else {
			safeOnlineContext = initialContext.createSubcontext("SafeOnline");
		}
		list = safeOnlineContext.list("");
		javax.naming.Context credentialServiceBeanContext;
		if (list.hasMore()) {
			credentialServiceBeanContext = (javax.naming.Context) safeOnlineContext
					.lookup("CredentialServiceBean");
		} else {
			credentialServiceBeanContext = safeOnlineContext
					.createSubcontext("CredentialServiceBean");
		}

		this.mockCredentialServiceBean = createMock(CredentialService.class);
		credentialServiceBeanContext.rebind("local",
				this.mockCredentialServiceBean);

		this.server = new Server();
		Connector connector = new SelectChannelConnector();
		connector.setPort(0);
		server.addConnector(connector);

		Context context = new Context();
		context.setContextPath("/");
		server.addHandler(context);

		ServletHandler handler = context.getServletHandler();

		ServletHolder servletHolder = new ServletHolder();
		servletHolder.setClassName(IdentityServlet.class.getName());
		String servletName = "IdentityServlet";
		servletHolder.setName(servletName);
		handler.addServlet(servletHolder);

		ServletMapping servletMapping = new ServletMapping();
		servletMapping.setServletName(servletName);
		servletMapping.setPathSpecs(new String[] { "/*" });
		handler.addServletMapping(servletMapping);

		this.server.start();

		int port = connector.getLocalPort();
		LOG.debug("port: " + port);

		this.location = "http://localhost:" + port + "/";

		this.httpClient = new HttpClient();
	}

	@Override
	protected void tearDown() throws Exception {
		this.server.stop();
		super.tearDown();
	}

	public void testWrongContentTypeGivesBadRequestResult() throws Exception {
		// setup
		PostMethod postMethod = new PostMethod(this.location);

		// operate
		int result = this.httpClient.executeMethod(postMethod);

		// verify
		LOG.debug("result: " + result);
		assertEquals(HttpServletResponse.SC_BAD_REQUEST, result);
	}

	public void testGetNotAllowed() throws Exception {
		// setup
		GetMethod getMethod = new GetMethod(this.location);

		// operate
		int result = this.httpClient.executeMethod(getMethod);

		// verify
		LOG.debug("result: " + result);
		assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, result);
	}

	public void testDoPost() throws Exception {
		// setup
		PostMethod postMethod = new PostMethod(this.location);
		RequestEntity requestEntity = new StringRequestEntity("test-message",
				"text/xml", null);
		postMethod.setRequestEntity(requestEntity);

		// expectations
		this.mockCredentialServiceBean.mergeIdentityStatement("test-message");

		// prepare
		replay(this.mockCredentialServiceBean);

		// operate
		int result = this.httpClient.executeMethod(postMethod);

		// verify
		assertEquals(HttpServletResponse.SC_OK, result);
		verify(this.mockCredentialServiceBean);
	}

	public void testJREOnlyClient() throws Exception {
		// setup
		URL url = new URL(this.location);
		HttpURLConnection httpURLConnection = (HttpURLConnection) url
				.openConnection();

		httpURLConnection.setRequestMethod("POST");
		httpURLConnection.setAllowUserInteraction(false);
		httpURLConnection.setRequestProperty("Content-type", "text/xml");
		httpURLConnection.setDoOutput(true);
		OutputStream outputStream = httpURLConnection.getOutputStream();
		IOUtils.write("test-message", outputStream, null);
		outputStream.close();
		httpURLConnection.connect();

		httpURLConnection.disconnect();

		// expectations
		this.mockCredentialServiceBean.mergeIdentityStatement("test-message");

		// prepare
		replay(this.mockCredentialServiceBean);

		// operate
		int responseCode = httpURLConnection.getResponseCode();

		// verify
		LOG.debug("response code: " + responseCode);
		assertEquals(HttpServletResponse.SC_OK, responseCode);
		verify(this.mockCredentialServiceBean);
	}
}
