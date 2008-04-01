/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.accept.net.link.safeonline.user;

import java.util.UUID;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

	private static final Log LOG = LogFactory.getLog(UserTest.class);

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

	public void testUserRegistrationLoginViewHistoryUseDemoTicketRemoveAccount()
			throws Exception {
		this.selenium.setContext("Testing the user registration.");
		this.selenium.setBrowserLogLevel(SeleniumLogLevels.INFO);

		// register our test user
		String login = UUID.randomUUID().toString();
		String password = "secret";
		this.acceptanceTestManager.register(login, password);

		// register beid device in user webapp, also check history
		this.acceptanceTestManager.userLogon(login, password);
		this.acceptanceTestManager.clickLinkAndWait("page_account_link");
		this.acceptanceTestManager.clickLinkAndWait("history");
		assertTrue(this.selenium
				.isTextPresent("Logged in successfully into application 'olas-user' using device 'password'."));
		this.acceptanceTestManager.clickLinkAndWait("page_devices_link");
		this.acceptanceTestManager.clickLinkInRow("devicesTable",
				"Belgian eID", "register");

		this.acceptanceTestManager
				.waitForRedirect(AcceptanceTestManager.SAFE_ONLINE_BEID_WEBAPP_PREFIX
						+ "/register-beid.seam");
		this.acceptanceTestManager.waitForRedirect("/device/devices.seam");

		this.acceptanceTestManager.logout();

		// login to demo-ticket webapp, needs to register beid device
		this.acceptanceTestManager.openDemoTicketWebApp("/");
		this.acceptanceTestManager.clickButtonAndWait("login");
		this.acceptanceTestManager
				.waitForRedirect(AcceptanceTestManager.SAFE_ONLINE_AUTH_WEBAPP_PREFIX
						+ "/main.seam");
		this.acceptanceTestManager.clickRadioButton("beid");
		this.acceptanceTestManager.clickButtonAndWait("next");
		this.acceptanceTestManager
				.waitForRedirect(AcceptanceTestManager.SAFE_ONLINE_BEID_WEBAPP_PREFIX
						+ "/beid-applet.seam");

		// buy ticket

		// remove account

	}

	public void bestUserPasswordChange() throws Exception {
		this.selenium.setContext("Testing password change.");
		this.selenium.setBrowserLogLevel(SeleniumLogLevels.INFO);

		String login = UUID.randomUUID().toString();
		String password = "secret";
		this.acceptanceTestManager.register(login, password);

		this.acceptanceTestManager.userLogon(login, password);

		this.acceptanceTestManager.logout();

		this.acceptanceTestManager.userLogon(login, password);

		String newPassword = "secret2";
		changePassword(password, newPassword);

		this.acceptanceTestManager.logout();
		this.acceptanceTestManager.userLogon(login, newPassword);
	}

	private void changePassword(String oldPassword, String newPassword) {
		this.acceptanceTestManager.openUserWebApp("/devices.seam");
		this.acceptanceTestManager.waitForPageToLoad();
		this.acceptanceTestManager.fillInputField("oldpassword", oldPassword);
		this.acceptanceTestManager.fillInputField("password1", newPassword);
		this.acceptanceTestManager.fillInputField("password2", newPassword);

		this.acceptanceTestManager.clickButtonAndWait("change");
	}
}
