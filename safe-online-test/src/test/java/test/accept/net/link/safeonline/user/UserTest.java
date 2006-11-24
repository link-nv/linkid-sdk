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

	public void testUserRegistrationLoginLogout() throws Exception {
		selenium.setContext("Testing the user registration.",
				SeleniumLogLevels.DEBUG);

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

		this.selenium.isTextPresent("successfully");

		this.selenium.type("xpath=//input[contains(@id, 'username')]", login);
		this.selenium
				.type("xpath=//input[contains(@id, 'password')]", password);
		this.selenium
				.click("xpath=//input[@type = 'submit' and contains(@id, 'login')]");
		this.selenium.waitForPageToLoad(TIMEOUT);

		this.selenium.isTextPresent("Welcome");
		this.selenium
				.click("xpath=//input[@type = 'submit' and contains(@id, 'logout')]");
		this.selenium.waitForPageToLoad(TIMEOUT);

		this.selenium.isTextPresent("Login");
	}
}
