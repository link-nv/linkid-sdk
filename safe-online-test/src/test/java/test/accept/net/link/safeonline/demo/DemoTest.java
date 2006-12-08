package test.accept.net.link.safeonline.demo;

import junit.framework.TestCase;
import test.accept.net.link.safeonline.AcceptanceTestManager;

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

	private Selenium selenium;

	private AcceptanceTestManager acceptanceTestManager;

	@Override
	protected void setUp() throws Exception {
		this.acceptanceTestManager = new AcceptanceTestManager();
		this.acceptanceTestManager.setUp();
		this.selenium = this.acceptanceTestManager.getSelenium();
	}

	@Override
	protected void tearDown() throws Exception {
		this.acceptanceTestManager.tearDown();
	}

	public void testDemoLogonLogout() throws Exception {
		selenium.setContext("Testing the demo logon and logout",
				SeleniumLogLevels.DEBUG);

		this.acceptanceTestManager.openDemoWebApp("/");
		assertTrue(this.selenium.isTextPresent("Logon"));
		assertTrue(this.selenium.isTextPresent("Username"));
		assertTrue(this.selenium.isTextPresent("Password"));

		this.selenium.type("j_username", "fcorneli");
		this.selenium.type("j_password", "secret");
		this.selenium.click("//input[@value='Logon']");
		this.acceptanceTestManager.waitForPageToLoad();

		assertTrue(this.selenium.isTextPresent("Welcome"));
		assertTrue(this.selenium.isTextPresent("fcorneli"));
		assertFalse(this.selenium.isTextPresent("Invalid"));

		this.selenium.click("//input[@value='Logout']");
		this.acceptanceTestManager.waitForPageToLoad();
		assertTrue(this.selenium.isTextPresent("Logon"));

		this.selenium.type("j_username", "foobar");
		this.selenium.type("j_password", "foobar");
		this.selenium.click("//input[@value='Logon']");
		this.acceptanceTestManager.waitForPageToLoad();

		assertTrue(this.selenium.isTextPresent("Invalid"));
	}
}
