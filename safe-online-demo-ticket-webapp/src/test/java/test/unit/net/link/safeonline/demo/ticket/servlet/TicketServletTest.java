/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.demo.ticket.servlet;

import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.link.safeonline.demo.ticket.entity.Ticket.Site;
import net.link.safeonline.demo.ticket.service.TicketService;
import net.link.safeonline.demo.ticket.servlet.TicketServlet;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.ServletTestManager;
import junit.framework.TestCase;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.easymock.EasyMock.expect;

public class TicketServletTest extends TestCase {

	private static final Log LOG = LogFactory.getLog(TicketServletTest.class);

	private ServletTestManager servletTestManager;

	private String servletLocation;

	private JndiTestUtils jndiTestUtils;

	private TicketService mockTicketService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.jndiTestUtils = new JndiTestUtils();
		this.jndiTestUtils.setUp();
		this.mockTicketService = createMock(TicketService.class);
		this.jndiTestUtils.bindComponent(TicketService.LOCAL_BINDING,
				this.mockTicketService);

		this.servletTestManager = new ServletTestManager();
		this.servletTestManager.setUp(TicketServlet.class);

		this.servletLocation = this.servletTestManager.getServletLocation();
	}

	@Override
	protected void tearDown() throws Exception {
		this.servletTestManager.tearDown();
		this.jndiTestUtils.tearDown();
		super.tearDown();
	}

	public void testDoGetWithValidPass() throws Exception {
		// setup
		String testNrn = UUID.randomUUID().toString();
		String testFrom = Site.GENT.name();
		String testTo = Site.BRUSSEL.name();
		HttpClient httpClient = new HttpClient();
		GetMethod getMethod = new GetMethod(this.servletLocation);
		getMethod.setQueryString(new NameValuePair[] {
				new NameValuePair("NRN", testNrn),
				new NameValuePair("FROM", testFrom),
				new NameValuePair("TO", testTo) });

		// setup
		expect(this.mockTicketService.hasValidPass(testNrn, testFrom, testTo))
				.andReturn(true);

		// prepare
		replay(this.mockTicketService);

		// operate
		int result = httpClient.executeMethod(getMethod);

		// verify
		LOG.debug("result: " + result);
		verify(this.mockTicketService);
		assertEquals(HttpServletResponse.SC_OK, result);
	}

	public void testDoGetWithInvalidPass() throws Exception {
		// setup
		String testNrn = UUID.randomUUID().toString();
		String testFrom = Site.GENT.name();
		String testTo = Site.BRUSSEL.name();
		HttpClient httpClient = new HttpClient();
		GetMethod getMethod = new GetMethod(this.servletLocation);
		getMethod.setQueryString(new NameValuePair[] {
				new NameValuePair("NRN", testNrn),
				new NameValuePair("FROM", testFrom),
				new NameValuePair("TO", testTo) });

		// setup
		expect(this.mockTicketService.hasValidPass(testNrn, testFrom, testTo))
				.andReturn(false);

		// prepare
		replay(this.mockTicketService);

		// operate
		int result = httpClient.executeMethod(getMethod);

		// verify
		LOG.debug("result: " + result);
		verify(this.mockTicketService);
		assertEquals(HttpServletResponse.SC_UNAUTHORIZED, result);
	}

	public void testDoGetWithoutValidParams() throws Exception {
		// setup
		HttpClient httpClient = new HttpClient();
		GetMethod getMethod = new GetMethod(this.servletLocation);

		// prepare
		replay(this.mockTicketService);

		// operate
		int result = httpClient.executeMethod(getMethod);

		// verify
		LOG.debug("result: " + result);
		verify(this.mockTicketService);
		assertEquals(HttpServletResponse.SC_BAD_REQUEST, result);
	}
}
