package net.link.safeonline.user;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.faces.component.UIComponent;
import javax.xml.transform.TransformerException;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.link.safeonline.jsfunit.util.JSFUnitUtil;

import org.apache.cactus.ServletTestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.jsfunit.facade.JSFClientSession;
import org.jboss.jsfunit.facade.JSFServerSession;
import org.jboss.jsfunit.richfaces.RichFacesClient;
import org.xml.sax.SAXException;

public class UserTest extends ServletTestCase {

	private final static Log LOG = LogFactory.getLog(UserTest.class);

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
		return new TestSuite(UserTest.class);
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
	 * Test applications.xhtml
	 * 
	 * @throws SAXException
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws TransformerException
	 */
	public void testApplications() throws MalformedURLException, IOException,
			SAXException {
		// Login
		JSFUnitUtil.login(this.client.getWebConversation(), "overview.seam");

		// Check if login was successfull
		assertEquals("/overview.xhtml", this.server.getCurrentViewID());

		// Test applications.xhtml
		this.client = new JSFClientSession(this.client.getWebConversation(),
				"/applications.seam");
		this.server = new JSFServerSession(this.client);
		assertEquals("/applications.xhtml", this.server.getCurrentViewID());

		// Try to unsuscribe from olas-user and assert the error is there
		RichFacesClient richFacesClient = new RichFacesClient(this.client);
		richFacesClient.ajaxSubmit("subscriptionForm:sub-data:6:unsubscribe");
		assertEquals("/applications.xhtml", this.server.getCurrentViewID());
		LOG.debug("result: " + this.client.getWebResponse().getText());

		UIComponent errorMessage = this.server
				.findComponent("subscriptionForm:error");
		assertTrue(errorMessage.isRendered());
	}
}
