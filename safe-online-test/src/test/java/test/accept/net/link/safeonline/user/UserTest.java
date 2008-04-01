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
		this.acceptanceTestManager.clickLinkInRowAndWait("devicesTable",
				"Belgian eID", "register");

		// BeId registration done manually, we wait till its done
		this.acceptanceTestManager.waitForRedirect("/device/devices.seam");
		assertTrue(this.acceptanceTestManager.checkLinkInRow(
				"deviceRegistrationsTable", "Belgian eID", "remove"));
		assertTrue(this.acceptanceTestManager.checkLinkInRow(
				"deviceRegistrationsTable", "Belgian eID", "update"));

		this.acceptanceTestManager.logout();

		// login to demo-ticket webapp
		this.acceptanceTestManager.openDemoTicketWebApp("/");
		this.acceptanceTestManager.clickButtonAndWait("login");
		this.acceptanceTestManager
				.waitForRedirect(AcceptanceTestManager.SAFE_ONLINE_AUTH_WEBAPP_PREFIX
						+ "/main.seam");
		this.acceptanceTestManager.clickRadioButton("beid");
		this.acceptanceTestManager.clickButtonAndWait("next");
		this.acceptanceTestManager
				.waitForRedirect(AcceptanceTestManager.SAFE_ONLINE_AUTH_WEBAPP_PREFIX
						+ "/subscription.seam");
		this.acceptanceTestManager.clickButtonAndWait("confirm");
		this.acceptanceTestManager.clickButtonAndWait("agree");
		this.acceptanceTestManager
				.waitForRedirect(AcceptanceTestManager.SAFE_ONLINE_DEMO_TICKET_WEBAPP_PREFIX
						+ "/overview.seam");
		this.selenium.isTextPresent("Welcome " + login);

		// buy ticket
		this.acceptanceTestManager.clickLinkAndWait("add");
		// demo-ticket/add.seam
		this.acceptanceTestManager.clickLinkAndWait("checkout");
		// demo-ticket/checkout.seam
		this.acceptanceTestManager.clickLinkAndWait("confirm");
		// demo-payment/entry.seam
		this.acceptanceTestManager.clickLinkAndWait("confirm");

		this.acceptanceTestManager
				.waitForRedirect(AcceptanceTestManager.SAFE_ONLINE_AUTH_WEBAPP_PREFIX
						+ "/main.seam");
		this.acceptanceTestManager.clickRadioButton("beid");
		this.acceptanceTestManager
				.waitForRedirect(AcceptanceTestManager.SAFE_ONLINE_AUTH_WEBAPP_PREFIX
						+ "/subscription.seam");
		this.acceptanceTestManager.clickButtonAndWait("confirm");
		this.acceptanceTestManager.clickButtonAndWait("agree");
		// visa number missing
		this.selenium.isTextPresent("VISA number");
		this.acceptanceTestManager.fillInputField("value", "0000111122223333");
		this.acceptanceTestManager.clickLink("save");
		this.acceptanceTestManager
				.waitForRedirect(AcceptanceTestManager.SAFE_ONLINE_DEMO_PAYMENT_WEBAPP_PREFIX
						+ "/cards.seam");

		// demo-payment/cards.seam
		this.acceptanceTestManager.clickLinkAndWait("confirm");
		// demo-payment/completed.seam
		this.acceptanceTestManager.clickLinkAndWait("continue");
		this.acceptanceTestManager
				.waitForRedirect(AcceptanceTestManager.SAFE_ONLINE_DEMO_TICKET_WEBAPP_PREFIX
						+ "/list.seam");
		this.selenium.isTextPresent("GENT");
		this.acceptanceTestManager.logout();

		// remove beid device
		this.acceptanceTestManager.userLogon(login, password);
		this.acceptanceTestManager.clickLinkAndWait("page_devices_link");
		this.acceptanceTestManager.clickLinkInRowAndWait(
				"deviceRegistrationsTable", "Belgian eID", "remove");
		this.acceptanceTestManager.waitForRedirect("/device/devices.seam");
		assertFalse(this.acceptanceTestManager.checkLinkInRow(
				"deviceRegistrationsTable", "Belgian eID", "remove"));
		assertFalse(this.acceptanceTestManager.checkLinkInRow(
				"deviceRegistrationsTable", "Belgian eID", "update"));

		// remove account
		this.acceptanceTestManager.clickLinkAndWait("page_account_link");
		this.acceptanceTestManager.clickLinkAndWait("remove");
		this.acceptanceTestManager.clickLinkAndWait("remove");
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
