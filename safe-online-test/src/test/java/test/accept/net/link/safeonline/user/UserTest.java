package test.accept.net.link.safeonline.user;

import java.util.UUID;

import junit.framework.TestCase;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumLogLevels;

/**
 * Acceptance test for user web application. This test requires the selenium
 * server to be up and running:
 * <code>java -jar selenium-server-0.9.0.jar -port 4455 -interactive</code>
 * <p>
 * and that the Safe Online demo web application is also up and running.
 * <p>
 * JBoss AS already runs on the default selenium server port 4444.
 * 
 * @author fcorneli
 * 
 */
public class UserTest extends TestCase {

	private static final String TIMEOUT = "5000";

	private static final int SELENIUM_SERVER_PORT = 4455;

	private static final String USER_LOCATION = "http://localhost:8080/safe-online/";

	private static final String DEMO_LOCATION = "http://localhost:8080/demo/";

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

	public void testUserRegistrationLoginEditNameSubscribeToDemoLogoutAndDoDemoLoginLogout()
			throws Exception {
		selenium.setContext("Testing the user registration.",
				SeleniumLogLevels.DEBUG);

		String login = UUID.randomUUID().toString();
		String password = "secret";
		register(login, password);

		userLogin(login, password);

		String name = "name-" + login;
		editName(name);

		// navigate + check history
		this.selenium.open(USER_LOCATION + "/applications.seam");
		this.selenium.open(USER_LOCATION + "/devices.seam");
		this.selenium.open(USER_LOCATION + "/history.seam");
		assertTrue(this.selenium.isTextPresent("safe-online-user"));

		String applicationName = "demo-application";
		subscribe(applicationName);

		userLogout();

		demoLogin(login, password);

		demoLogout();
	}

	public void testUserPasswordChange() throws Exception {
		selenium
				.setContext("Testing password change.", SeleniumLogLevels.DEBUG);

		String login = UUID.randomUUID().toString();
		String password = "secret";
		register(login, password);

		userLogin(login, password);

		String applicationName = "demo-application";
		subscribe(applicationName);

		userLogout();

		demoLogin(login, password);

		demoLogout();

		userLogin(login, password);

		String newPassword = "secret2";
		changePassword(password, newPassword);

		userLogout();
		userLogin(login, newPassword);

		demoLogin(login, newPassword);

		demoLogout();

	}

	private void fillInputField(String id, String value) {
		this.selenium.type("xpath=//input[contains(@id, '" + id + "')]", value);
	}

	private void clickButton(String id) {
		this.selenium
				.click("xpath=//input[@type = 'submit' and contains(@id, '"
						+ id + "')]");
	}

	private void clickLink(String id) {
		this.selenium.click("xpath=//a[contains(@id, '" + id + "')]");
	}

	private void changePassword(String oldPassword, String newPassword) {
		this.selenium.open(USER_LOCATION + "/devices.seam");
		this.selenium.waitForPageToLoad(TIMEOUT);
		fillInputField("oldpassword", oldPassword);
		fillInputField("password1", newPassword);
		fillInputField("password2", newPassword);

		clickButton("change");
		this.selenium.waitForPageToLoad(TIMEOUT);
	}

	private void register(String login, String password) {
		this.selenium.open(USER_LOCATION);
		this.selenium.waitForPageToLoad(TIMEOUT);
		clickLink("register");
		this.selenium.waitForPageToLoad(TIMEOUT);

		fillInputField("login", login);
		fillInputField("password1", password);
		fillInputField("password2", password);
		clickButton("register");
		this.selenium.waitForPageToLoad(TIMEOUT);

		assertTrue(this.selenium.isTextPresent("successfully"));
	}

	private void demoLogout() {
		this.selenium.click("//input[@value='Logout']");
		this.selenium.waitForPageToLoad(TIMEOUT);
		assertTrue(this.selenium.isTextPresent("Logon"));
	}

	private void demoLogin(String login, String password) {
		this.selenium.open(DEMO_LOCATION);
		assertTrue(this.selenium.isTextPresent("Logon"));
		assertTrue(this.selenium.isTextPresent("Username"));
		assertTrue(this.selenium.isTextPresent("Password"));

		this.selenium.type("j_username", login);
		this.selenium.type("j_password", password);
		this.selenium.click("//input[@value='Logon']");
		this.selenium.waitForPageToLoad(TIMEOUT);

		assertTrue(this.selenium.isTextPresent("Welcome"));
		assertTrue(this.selenium.isTextPresent(login));
		assertFalse(this.selenium.isTextPresent("Invalid"));
	}

	private void userLogout() {
		this.selenium
				.click("xpath=//input[@type = 'submit' and contains(@id, 'logout')]");
		this.selenium.waitForPageToLoad(TIMEOUT);

		assertTrue(this.selenium.isTextPresent("Login"));
	}

	private void subscribe(String applicationName) {
		this.selenium.open(USER_LOCATION + "/applications.seam");
		this.selenium
				.click("xpath=//table[contains(@Id, 'app-data')]//tr[./td/a[contains(text(), '"
						+ applicationName + "')]]/td/a[text() = 'Subscribe']");
		this.selenium.waitForPageToLoad(TIMEOUT);
		String subResult = this.selenium
				.getText("xpath=//table[contains(@Id, 'sub-data')]//tr/td/a[contains(text(), '"
						+ applicationName + "')]");
		assertEquals(applicationName, subResult);
	}

	private void editName(String name) {
		this.selenium.open(USER_LOCATION + "/profile.seam");
		fillInputField("name", name);
		clickButton("save");
		this.selenium.waitForPageToLoad(TIMEOUT);

		assertEquals(name, this.selenium
				.getValue("xpath=//input[contains(@id, 'name')]"));
	}

	private void userLogin(String login, String password) {
		this.selenium.open(USER_LOCATION);
		fillInputField("username", login);
		fillInputField("password", password);
		clickButton("login");
		this.selenium.waitForPageToLoad(TIMEOUT);

		assertTrue(this.selenium.isTextPresent("Welcome"));
		assertTrue(this.selenium.isTextPresent(login));
	}
}
