package test.accept.net.link.safeonline.demo;

import junit.framework.TestCase;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumLogLevels;

/**
 * Demo Acceptance Test. This test requires the selenium server to be up and
 * running:
 * <code>java -jar selenium-server-0.9.0.jar -port 4455 -interactive</code>
 * <p>
 * and that the Safe Online demo web application is also up and running.
 * <p>
 * JBoss AS already runs on the default selenium server port 4444.
 * 
 * @author fcorneli
 * 
 */
public class DemoTest extends TestCase {

	private static final String TIMEOUT = "5000";

	private static final int SELENIUM_SERVER_PORT = 4455;

	private static final String DEMO_LOCATION = "http://localhost:8080/demo/";

	private Selenium selenium;

	@Override
	public void setUp() throws Exception {
		this.selenium = new DefaultSelenium("localhost", SELENIUM_SERVER_PORT,
				"*firefox", DEMO_LOCATION);
		this.selenium.start();
	}

	@Override
	protected void tearDown() throws Exception {
		this.selenium.stop();
	}

	public void testDemoLogonLogout() throws Exception {
		selenium.setContext("Testing the demo logon and logout",
				SeleniumLogLevels.DEBUG);

		this.selenium.open(DEMO_LOCATION);
		assertTrue(this.selenium.isTextPresent("Logon"));
		assertTrue(this.selenium.isTextPresent("Username"));
		assertTrue(this.selenium.isTextPresent("Password"));

		this.selenium.type("j_username", "fcorneli");
		this.selenium.type("j_password", "secret");
		this.selenium.click("//input[@value='Logon']");
		this.selenium.waitForPageToLoad(TIMEOUT);

		assertTrue(this.selenium.isTextPresent("Welcome"));
		assertTrue(this.selenium.isTextPresent("fcorneli"));
		assertFalse(this.selenium.isTextPresent("Invalid"));

		this.selenium.click("//input[@value='Logout']");
		this.selenium.waitForPageToLoad(TIMEOUT);
		assertTrue(this.selenium.isTextPresent("Logon"));

		this.selenium.type("j_username", "foobar");
		this.selenium.type("j_password", "foobar");
		this.selenium.click("//input[@value='Logon']");
		this.selenium.waitForPageToLoad(TIMEOUT);

		assertTrue(this.selenium.isTextPresent("Invalid"));
	}
}
