package test.accept.net.link.safeonline.user;

import java.util.UUID;

import junit.framework.TestCase;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumLogLevels;

public class UserTest extends TestCase {

	private static final String TIMEOUT = "5000";

	private static final int SELENIUM_SERVER_PORT = 4455;

	private static final String USER_LOCATION = "http://localhost:8080/safe-online/";

	private Selenium selenium;

	@Override
	public void setUp() throws Exception {
		this.selenium = new DefaultSelenium("localhost", SELENIUM_SERVER_PORT,
				"*firefox", USER_LOCATION);
		this.selenium.start();
	}

	@Override
	protected void tearDown() throws Exception {
		this.selenium.stop();
	}

	public void testUserRegistrationLoginEditNameLogout() throws Exception {
		selenium.setContext("Testing the user registration.",
				SeleniumLogLevels.DEBUG);

		// REGISTER
		this.selenium.open(USER_LOCATION);
		this.selenium.waitForPageToLoad(TIMEOUT);
		this.selenium.click("xpath=//a[contains(@id, 'register')]");
		this.selenium.waitForPageToLoad(TIMEOUT);

		String login = UUID.randomUUID().toString();
		String password = "secret";
		this.selenium.type("xpath=//input[contains(@id, 'login')]", login);
		this.selenium.type("xpath=//input[contains(@id, 'password1')]",
				password);
		this.selenium.type("xpath=//input[contains(@id, 'password2')]",
				password);
		this.selenium
				.click("xpath=//input[@type = 'submit' and contains(@id, 'register')]");
		this.selenium.waitForPageToLoad(TIMEOUT);

		assertTrue(this.selenium.isTextPresent("successfully"));

		// LOGIN
		this.selenium.type("xpath=//input[contains(@id, 'username')]", login);
		this.selenium
				.type("xpath=//input[contains(@id, 'password')]", password);
		this.selenium
				.click("xpath=//input[@type = 'submit' and contains(@id, 'login')]");
		this.selenium.waitForPageToLoad(TIMEOUT);

		assertTrue(this.selenium.isTextPresent("Welcome"));
		assertTrue(this.selenium.isTextPresent(login));

		// EDIT NAME
		this.selenium.open(USER_LOCATION + "/profile.seam");
		String name = "name-" + login;
		this.selenium.type("xpath=//input[contains(@id, 'name')]", name);
		this.selenium
				.click("xpath=//input[@type = 'submit' and contains(@id, 'save')]");
		this.selenium.waitForPageToLoad(TIMEOUT);

		assertEquals(name, this.selenium
				.getValue("xpath=//input[contains(@id, 'name')]"));

		// NAVIGATE
		this.selenium.open(USER_LOCATION + "/applications.seam");
		this.selenium.open(USER_LOCATION + "/devices.seam");
		this.selenium.open(USER_LOCATION + "/history.seam");
		assertTrue(this.selenium.isTextPresent("safe-online-user"));

		// LOGOUT
		this.selenium
				.click("xpath=//input[@type = 'submit' and contains(@id, 'logout')]");
		this.selenium.waitForPageToLoad(TIMEOUT);

		assertTrue(this.selenium.isTextPresent("Login"));
	}
}
