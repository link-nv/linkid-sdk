package net.link.safeonline.helpdesk;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.faces.component.UIComponent;
import javax.faces.component.UIMessage;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.link.safeonline.jsfunit.util.JSFUnitUtil;

import org.apache.cactus.ServletTestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.jsfunit.facade.JSFClientSession;
import org.jboss.jsfunit.facade.JSFServerSession;
import org.xml.sax.SAXException;

public class HelpdeskTest extends ServletTestCase {

	private final static Log LOG = LogFactory.getLog(HelpdeskTest.class);

	private JSFClientSession client;

	private JSFServerSession server;

	@Override
	public void setUp() throws IOException, SAXException {
		this.client = new JSFClientSession("/main.seam");
		this.server = new JSFServerSession(this.client);
		assertEquals("/main.xhtml", this.server.getCurrentViewID());
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(HelpdeskTest.class);
	}

	/**
	 * Test main.xhtml
	 * 
	 * @throws SAXException
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public void testMain() {
		// Assert that the login component is in the component tree and
		// rendered
		UIComponent login = this.server.findComponent("login");
		assertTrue(login.isRendered());

		LOG
				.debug("jsessionid: "
						+ this.client.getWebConversation().getCookieValue(
								"JSESSIONID"));
	}

	/**
	 * Test log.xhtml
	 * 
	 * @throws SAXException
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public void testLog() throws MalformedURLException, IOException,
			SAXException {
		// Login
		JSFUnitUtil.login(this.client.getWebConversation(), "overview.seam");

		// Check if login was successfull
		assertEquals("/overview.xhtml", this.server.getCurrentViewID());

		// Test log.xhtml
		this.client = new JSFClientSession(this.client.getWebConversation(),
				"/log.seam");
		this.server = new JSFServerSession(this.client);
		assertEquals("/log.xhtml", this.server.getCurrentViewID());

		UIComponent searchInput = this.server.findComponent("searchId");
		assertTrue(searchInput.isRendered());
		this.client.setParameter("searchId", "1");
		this.client.submit("search");

		UIMessage searchMessage = (UIMessage) this.server
				.findComponent("searchMessage");
		assertTrue(searchMessage.isRendered());
	}

	/**
	 * Test user.xhtml
	 * 
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws SAXException
	 */
	public void testUser() throws MalformedURLException, IOException,
			SAXException {
		// Login
		JSFUnitUtil.login(this.client.getWebConversation(), "overview.seam");

		// Check if login was successfull
		assertEquals("/overview.xhtml", this.server.getCurrentViewID());

		// Test user.xhtml
		this.client = new JSFClientSession(this.client.getWebConversation(),
				"/user.seam");
		this.server = new JSFServerSession(this.client);
		assertEquals("/user.xhtml", this.server.getCurrentViewID());

		UIComponent searchInput = this.server.findComponent("searchUserName");
		assertTrue(searchInput.isRendered());
		this.client.setParameter("searchUserName", "test-user");
		this.client.submit("searchUser");

		UIMessage searchMessage = (UIMessage) this.server
				.findComponent("searchMessage");
		assertTrue(searchMessage.isRendered());
	}
}
