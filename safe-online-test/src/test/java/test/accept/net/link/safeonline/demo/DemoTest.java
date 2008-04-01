/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.accept.net.link.safeonline.demo;

import junit.framework.TestCase;
import test.accept.net.link.safeonline.AcceptanceTestManager;

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumLogLevels;

/**
 * Demo Acceptance Test.
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
		this.selenium.setContext("Testing the demo logon and logout");
		this.selenium.setBrowserLogLevel(SeleniumLogLevels.DEBUG);

		this.acceptanceTestManager.openDemoWebApp("/secure/");
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
		assertFalse(this.selenium.isTextPresent("fcorneli"));

		this.acceptanceTestManager.openDemoWebApp("/secure/");
		this.selenium.type("j_username", "foobar");
		this.selenium.type("j_password", "foobar");
		this.selenium.click("//input[@value='Logon']");
		this.acceptanceTestManager.waitForPageToLoad();

		assertTrue(this.selenium.isTextPresent("Invalid"));
	}
}
