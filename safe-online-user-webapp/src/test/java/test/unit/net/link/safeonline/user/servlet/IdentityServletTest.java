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
import static org.junit.Assert.assertEquals;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.authentication.service.CredentialService;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.ServletTestManager;
import net.link.safeonline.user.servlet.IdentityServlet;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IdentityServletTest {

	private static final Log LOG = LogFactory.getLog(IdentityServletTest.class);

	private String location;

	private HttpClient httpClient;

	private CredentialService mockCredentialServiceBean;

	private ServletTestManager servletTestManager;

	private JndiTestUtils jndiTestUtils;

	@Before
	public void setUp() throws Exception {
		this.jndiTestUtils = new JndiTestUtils();
		this.jndiTestUtils.setUp();

		this.mockCredentialServiceBean = createMock(CredentialService.class);
		this.jndiTestUtils.bindComponent(
				"SafeOnline/CredentialServiceBean/local",
				this.mockCredentialServiceBean);

		this.servletTestManager = new ServletTestManager();
		this.servletTestManager.setUp(IdentityServlet.class);
		this.location = this.servletTestManager.getServletLocation();

		this.httpClient = new HttpClient();
	}

	@After
	public void tearDown() throws Exception {
		this.servletTestManager.tearDown();
		this.jndiTestUtils.tearDown();
	}

	@Test
	public void testWrongContentTypeGivesBadRequestResult() throws Exception {
		// setup
		PostMethod postMethod = new PostMethod(this.location);

		// operate
		int result = this.httpClient.executeMethod(postMethod);

		// verify
		LOG.debug("result: " + result);
		assertEquals(HttpServletResponse.SC_BAD_REQUEST, result);
	}

	@Test
	public void testGetNotAllowed() throws Exception {
		// setup
		GetMethod getMethod = new GetMethod(this.location);

		// operate
		int result = this.httpClient.executeMethod(getMethod);

		// verify
		LOG.debug("result: " + result);
		assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, result);
	}

	@Test
	public void testDoPost() throws Exception {
		// setup
		PostMethod postMethod = new PostMethod(this.location);
		RequestEntity requestEntity = new StringRequestEntity("test-message",
				"application/octet-stream", null);
		postMethod.setRequestEntity(requestEntity);

		// expectations
		this.mockCredentialServiceBean.mergeIdentityStatement(EasyMock
				.aryEq("test-message".getBytes()));

		// prepare
		replay(this.mockCredentialServiceBean);

		// operate
		int result = this.httpClient.executeMethod(postMethod);

		// verify
		assertEquals(HttpServletResponse.SC_OK, result);
		verify(this.mockCredentialServiceBean);
	}

	@Test
	public void testJREOnlyClient() throws Exception {
		// setup
		URL url = new URL(this.location);
		HttpURLConnection httpURLConnection = (HttpURLConnection) url
				.openConnection();

		httpURLConnection.setRequestMethod("POST");
		httpURLConnection.setAllowUserInteraction(false);
		httpURLConnection.setRequestProperty("Content-type",
				"application/octet-stream");
		httpURLConnection.setDoOutput(true);
		OutputStream outputStream = httpURLConnection.getOutputStream();
		IOUtils.write("test-message", outputStream, null);
		outputStream.close();
		httpURLConnection.connect();

		httpURLConnection.disconnect();

		// expectations
		this.mockCredentialServiceBean.mergeIdentityStatement(EasyMock
				.aryEq("test-message".getBytes()));

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
