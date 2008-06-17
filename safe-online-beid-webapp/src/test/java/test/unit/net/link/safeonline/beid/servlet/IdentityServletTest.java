/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.beid.servlet;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.beid.servlet.IdentityServlet;
import net.link.safeonline.model.beid.BeIdDeviceService;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.ServletTestManager;

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

	private String protocol = "http";

	private String location;

	private HttpClient httpClient;

	private BeIdDeviceService mockBeIdDeviceServiceBean;

	private SamlAuthorityService mockSamlAuthorityService;

	private ServletTestManager servletTestManager;

	private JndiTestUtils jndiTestUtils;

	@Before
	public void setUp() throws Exception {
		this.jndiTestUtils = new JndiTestUtils();
		this.jndiTestUtils.setUp();

		this.mockBeIdDeviceServiceBean = createMock(BeIdDeviceService.class);
		this.jndiTestUtils.bindComponent(
				"SafeOnline/BeIdDeviceServiceBean/local",
				this.mockBeIdDeviceServiceBean);

		this.mockSamlAuthorityService = createMock(SamlAuthorityService.class);
		this.jndiTestUtils.bindComponent(
				"SafeOnline/SamlAuthorityServiceBean/local",
				this.mockSamlAuthorityService);

		this.servletTestManager = new ServletTestManager();
		Map<String, String> servletInitParams = Collections.singletonMap(
				"Protocol", this.protocol);
		this.servletTestManager.setUp(IdentityServlet.class, servletInitParams);
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
		this.mockBeIdDeviceServiceBean.register((String) EasyMock.anyObject(),
				EasyMock.aryEq("test-message".getBytes()));
		EasyMock.expect(
				this.mockSamlAuthorityService.getAuthnAssertionValidity())
				.andStubReturn(Integer.MAX_VALUE);

		// prepare
		replay(this.mockBeIdDeviceServiceBean);

		// operate
		int result = this.httpClient.executeMethod(postMethod);

		// verify
		assertEquals(HttpServletResponse.SC_OK, result);
		verify(this.mockBeIdDeviceServiceBean);
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
		this.mockBeIdDeviceServiceBean.register((String) EasyMock.anyObject(),
				EasyMock.aryEq("test-message".getBytes()));
		EasyMock.expect(
				this.mockSamlAuthorityService.getAuthnAssertionValidity())
				.andStubReturn(Integer.MAX_VALUE);

		// prepare
		replay(this.mockBeIdDeviceServiceBean);

		// operate
		int responseCode = httpURLConnection.getResponseCode();

		// verify
		LOG.debug("response code: " + responseCode);
		assertEquals(HttpServletResponse.SC_OK, responseCode);
		verify(this.mockBeIdDeviceServiceBean);
	}
}
