/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.accept.net.link.safeonline.user;

import java.util.UUID;

import junit.framework.TestCase;
import test.accept.net.link.safeonline.AcceptanceTestManager;

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumLogLevels;

/**
 * Acceptance test for user web application.
 * 
 * @author fcorneli
 * 
 */
public class UserTest extends TestCase {

	private Selenium selenium;

	private AcceptanceTestManager acceptanceTestManager;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		this.acceptanceTestManager = new AcceptanceTestManager();
		this.acceptanceTestManager.setUp();
		this.selenium = this.acceptanceTestManager.getSelenium();
	}

	@Override
	protected void tearDown() throws Exception {
		this.acceptanceTestManager.tearDown();
		super.tearDown();
	}

	public void testUserRegistrationLoginEditNameSubscribeToDemoLogoutAndDoDemoLoginLogout()
			throws Exception {
		this.selenium.setContext("Testing the user registration.",
				SeleniumLogLevels.DEBUG);

		String login = UUID.randomUUID().toString();
		String password = "secret";
		register(login, password);

		this.acceptanceTestManager.userLogon(login, password);

		String name = "name-" + login;
		editName(name);

		// navigate + check history
		this.acceptanceTestManager.openUserWebApp("/applications.seam");
		this.acceptanceTestManager.openUserWebApp("/devices.seam");
		this.acceptanceTestManager.openUserWebApp("/history.seam");
		assertTrue(this.selenium.isTextPresent("safe-online-user"));

		String applicationName = "demo-application";
		subscribe(applicationName);

		this.acceptanceTestManager.logout();

		demoLogin(login, password);

		demoLogout();
	}

	public void testUserPasswordChange() throws Exception {
		this.selenium.setContext("Testing password change.",
				SeleniumLogLevels.DEBUG);

		String login = UUID.randomUUID().toString();
		String password = "secret";
		register(login, password);

		this.acceptanceTestManager.userLogon(login, password);

		String applicationName = "demo-application";
		subscribe(applicationName);

		this.acceptanceTestManager.logout();

		demoLogin(login, password);

		demoLogout();

		this.acceptanceTestManager.userLogon(login, password);

		String newPassword = "secret2";
		changePassword(password, newPassword);

		this.acceptanceTestManager.logout();
		this.acceptanceTestManager.userLogon(login, newPassword);

		demoLogin(login, newPassword);

		demoLogout();

	}

	private void changePassword(String oldPassword, String newPassword) {
		this.acceptanceTestManager.openUserWebApp("/devices.seam");
		this.acceptanceTestManager.waitForPageToLoad();
		this.acceptanceTestManager.fillInputField("oldpassword", oldPassword);
		this.acceptanceTestManager.fillInputField("password1", newPassword);
		this.acceptanceTestManager.fillInputField("password2", newPassword);

		this.acceptanceTestManager.clickButtonAndWait("change");
	}

	private void register(String login, String password) {
		this.acceptanceTestManager.openUserWebApp("/");
		this.acceptanceTestManager.waitForPageToLoad();
		this.acceptanceTestManager.clickLink("register");
		this.acceptanceTestManager.waitForPageToLoad();

		this.acceptanceTestManager.fillInputField("login", login);
		this.acceptanceTestManager.fillInputField("password1", password);
		this.acceptanceTestManager.fillInputField("password2", password);
		this.acceptanceTestManager.clickButtonAndWait("register");

		assertTrue(this.selenium.isTextPresent("successfully"));
	}

	private void demoLogout() {
		this.selenium.click("//input[@value='Logout']");
		this.acceptanceTestManager.waitForPageToLoad();
	}

	private void demoLogin(String login, String password) {
		this.acceptanceTestManager.openDemoWebApp("/secure/");
		assertTrue(this.selenium.isTextPresent("Logon"));
		assertTrue(this.selenium.isTextPresent("Username"));
		assertTrue(this.selenium.isTextPresent("Password"));

		this.selenium.type("j_username", login);
		this.selenium.type("j_password", password);
		this.selenium.click("//input[@value='Logon']");
		this.acceptanceTestManager.waitForPageToLoad();

		assertTrue(this.selenium.isTextPresent("Welcome"));
		assertTrue(this.selenium.isTextPresent(login));
		assertFalse(this.selenium.isTextPresent("Invalid"));
	}

	private void subscribe(String applicationName) {
		this.acceptanceTestManager.openUserWebApp("/applications.seam");
		this.selenium
				.click("xpath=//table[contains(@Id, 'app-data')]//tr[./td/a[contains(text(), '"
						+ applicationName + "')]]/td/a[text() = 'Subscribe']");
		this.acceptanceTestManager.waitForPageToLoad();
		String subResult = this.selenium
				.getText("xpath=//table[contains(@Id, 'sub-data')]//tr/td/a[contains(text(), '"
						+ applicationName + "')]");
		assertEquals(applicationName, subResult);
	}

	private void editName(String name) {
		this.acceptanceTestManager.openUserWebApp("/profile.seam");
		this.acceptanceTestManager.fillInputField("name", name);
		this.acceptanceTestManager.clickButtonAndWait("save");

		assertEquals(name, this.selenium
				.getValue("xpath=//input[contains(@id, 'name')]"));
	}

}
