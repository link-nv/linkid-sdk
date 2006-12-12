/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.accept.net.link.safeonline;

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.openqa.selenium.server.SeleniumServer;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

/**
 * Acceptance test manager based on the Selenium testing framework.
 * 
 * This component will launch the selenium server on port 4455.
 * 
 * Make sure that the SafeOnline web applications are up and running.
 * 
 * Note: JBoss AS already runs on the default selenium server port 4444.
 * 
 * @author fcorneli
 * 
 */
public class AcceptanceTestManager {

	private static final String SAFE_ONLINE_USER_WEBAPP_PREFIX = "/safe-online";

	private static final String SAFE_ONLINE_DEMO_WEBAPP_PREFIX = "/demo";

	private static final String SAFE_ONLINE_OPER_WEBAPP_PREFIX = "/safe-online-oper";

	private static final String SAFE_ONLINE_OWNER_WEBAPP_PREFIX = "/safe-online-owner";

	private static final Log LOG = LogFactory
			.getLog(AcceptanceTestManager.class);

	private Selenium selenium;

	private SeleniumServer seleniumServer;

	public static final int SELENIUM_SERVER_PORT = 4455;

	private static final String TIMEOUT = "5000";

	private String safeOnlineLocation;

	public void setUp() throws Exception {
		this.seleniumServer = new SeleniumServer(SELENIUM_SERVER_PORT);
		this.seleniumServer.start();
		Properties properties = new Properties();
		InputStream propConfigInputStream = AcceptanceTestManager.class
				.getResourceAsStream("/test-accept-config.properties");
		properties.load(propConfigInputStream);
		this.safeOnlineLocation = "http://"
				+ properties.getProperty("safeonline.location");
		LOG.debug("SafeOnline location: " + this.safeOnlineLocation);
		this.selenium = new DefaultSelenium("localhost", SELENIUM_SERVER_PORT,
				"*firefox", this.safeOnlineLocation);
		this.selenium.start();
	}

	public void tearDown() throws Exception {
		this.selenium.stop();
		this.seleniumServer.stop();
	}

	public Selenium getSelenium() {
		if (null == this.selenium) {
			throw new IllegalStateException("call setUp first");
		}
		return this.selenium;
	}

	public String getSafeOnlineLocation() {
		return this.safeOnlineLocation;
	}

	public void openUserWebApp(String page) {
		this.selenium.open(this.safeOnlineLocation
				+ SAFE_ONLINE_USER_WEBAPP_PREFIX + page);
	}

	public void openDemoWebApp(String page) {
		this.selenium.open(this.safeOnlineLocation
				+ SAFE_ONLINE_DEMO_WEBAPP_PREFIX + page);
	}

	public void openOperWebApp(String page) {
		this.selenium.open(this.safeOnlineLocation
				+ SAFE_ONLINE_OPER_WEBAPP_PREFIX + page);
	}

	public void openOwnerWebApp(String page) {
		this.selenium.open(this.safeOnlineLocation
				+ SAFE_ONLINE_OWNER_WEBAPP_PREFIX + page);
	}

	public void fillInputField(String id, String value) {
		this.selenium.type("xpath=//input[contains(@id, '" + id + "')]", value);
	}

	public void clickButton(String id) {
		this.selenium
				.click("xpath=//input[@type = 'submit' and contains(@id, '"
						+ id + "')]");
	}

	public void clickButtonAndWait(String id) {
		clickButton(id);
		waitForPageToLoad();
	}

	public void clickLink(String id) {
		this.selenium.click("xpath=//a[contains(@id, '" + id + "')]");
	}

	public void userLogon(String login, String password) {
		openUserWebApp("/");
		logon(login, password);

		Assert.assertTrue(this.selenium.isTextPresent("Welcome"));
		Assert.assertTrue(this.selenium.isTextPresent(login));
	}

	public void logon(String login, String password) {
		fillInputField("username", login);
		fillInputField("password", password);
		clickButton("login");
		this.selenium.waitForPageToLoad(TIMEOUT);
	}

	public void operLogon(String login, String password) {
		openOperWebApp("/");
		logon(login, password);

		Assert.assertTrue(this.selenium.isTextPresent("Welcome"));
		Assert.assertTrue(this.selenium.isTextPresent(login));
	}

	public void ownerLogon(String login, String password) {
		openOwnerWebApp("/");
		logon(login, password);

		Assert.assertTrue(this.selenium.isTextPresent("Welcome"));
		Assert.assertTrue(this.selenium.isTextPresent(login));
	}

	public void waitForPageToLoad() {
		this.selenium.waitForPageToLoad(TIMEOUT);
	}

	public void logout() {
		this.selenium
				.click("xpath=//input[@type = 'submit' and contains(@id, 'logout')]");
		waitForPageToLoad();

		Assert.assertTrue(this.selenium.isTextPresent("Login"));
	}
}
