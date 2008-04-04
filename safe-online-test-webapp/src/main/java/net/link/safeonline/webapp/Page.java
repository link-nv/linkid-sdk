/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp;

import junit.framework.Assert;
import net.link.safeonline.webapp.auth.AuthMain;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;

public abstract class Page {
	public static final Log LOG = LogFactory.getLog(Page.class);

	protected static final String SAFE_ONLINE_AUTH_WEBAPP_PREFIX = "/olas-auth";

	protected static final String SAFE_ONLINE_BEID_WEBAPP_PREFIX = "/olas-beid";

	protected static final String SAFE_ONLINE_ENCAP_WEBAPP_PREFIX = "/olas-encap";

	protected static final String SAFE_ONLINE_USER_WEBAPP_PREFIX = "/olas";

	protected static final String SAFE_ONLINE_OPER_WEBAPP_PREFIX = "/olas-oper";

	protected static final String SAFE_ONLINE_OWNER_WEBAPP_PREFIX = "/olas-owner";

	protected static final String SAFE_ONLINE_HELPDESK_WEBAPP_PREFIX = "/olas-helpdesk";

	protected static final String SAFE_ONLINE_DEMO_TICKET_WEBAPP_PREFIX = "/demo-ticket";

	protected static final String SAFE_ONLINE_DEMO_LAWYER_WEBAPP_PREFIX = "/demo-lawyer";

	protected static final String SAFE_ONLINE_DEMO_PAYMENT_WEBAPP_PREFIX = "/demo-payment";

	protected static final String SAFE_ONLINE_DEMO_MANDATE_WEBAPP_PREFIX = "/demo-mandate";

	protected static final String SAFE_ONLINE_DEMO_PRESCRIPTION_WEBAPP_PREFIX = "/demo-prescription";

	private static final String TIMEOUT = "5000";

	private static Selenium selenium;

	private static AcceptanceTestManager acceptanceTestManager;

	private String page;

	public Page(String page) {
		this.page = page;
	}

	public static void setSelenium(Selenium selenium) {
		Page.selenium = selenium;
	}

	public static Selenium getSelenium() {
		return selenium;
	}

	public static void setAcceptanceManager(
			AcceptanceTestManager acceptanceTestManager) {
		Page.acceptanceTestManager = acceptanceTestManager;
	}

	public static AcceptanceTestManager getAcceptanceTestManager() {
		return acceptanceTestManager;
	}

	public void open() {
		selenium.open(this.page);
		waitForPageToLoad();
	}

	public AuthMain login() {
		if (selenium.isElementPresent("xpath=//a[contains(@id, 'login')]")) {
			clickLinkAndWait("login");
		} else {
			clickButtonAndWait("login");
		}
		waitForRedirect(AuthMain.PAGE_NAME);
		return new AuthMain();
	}

	public void logout() {
		if (selenium.isElementPresent("xpath=//a[contains(@id, 'logout')]")) {
			clickLinkAndWait("logout");
		} else {
			clickButtonAndWait("logout");
		}
		Assert
				.assertTrue(selenium
						.isElementPresent("xpath=//a[contains(@id, 'login')]")
						|| selenium
								.isElementPresent("xpath=//input[contains(@id, 'login')]"));
	}

	protected void fillInputField(String id, String value) {
		selenium.type("xpath=//input[contains(@id, '" + id + "')]", value);
	}

	protected void fillInputFieldInRepeat(String label, String id, String value) {
		selenium.type("//label[./span[contains(text(), '" + label
				+ "')]]//following::input[contains(@id, '" + id + "')]", value);
	}

	protected void clickButton(String id) {
		selenium.click("xpath=//input[@type = 'submit' and contains(@id, '"
				+ id + "')]");
	}

	protected void clickButtonAndWait(String id) {
		clickButton(id);
		waitForPageToLoad();
	}

	protected void clickLink(String id) {
		selenium.click("xpath=//a[contains(@id, '" + id + "')]");
	}

	protected void clickLinkInRow(String table, String row, String id) {
		selenium.click("xpath=//table[contains(@Id, '" + table
				+ "')]//tr[./td[contains(text(), '" + row
				+ "')]]/td/a[contains(@Id, '" + id + "')]");
	}

	protected void clickLinkAndWait(String id) {
		clickLink(id);
		waitForPageToLoad();
	}

	protected void clickLinkInRowAndWait(String table, String row, String id) {
		clickLinkInRow(table, row, id);
		waitForPageToLoad();
	}

	protected boolean isCheckedRadioButton(String value) {
		return selenium
				.isChecked("xpath=//input[@type = 'radio' and contains(@value, '"
						+ value + "')]");
	}

	protected boolean isCheckedCheckBox(String id) {
		return selenium
				.isChecked("xpath=//input[@type = 'checkbox' and contains(@Id, '"
						+ id + "')]");
	}

	protected void clickRadioButton(String value) {
		selenium.click("xpath=//input[@type = 'radio' and contains(@value, '"
				+ value + "')]");
	}

	protected void clickCheckbox(String id) {
		selenium.click("xpath=//input[@type = 'checkbox' and contains(@Id, '"
				+ id + "')]");
	}

	protected void setCheckBox(String id, boolean check) {
		boolean isChecked = isCheckedCheckBox(id);
		if ((isChecked && !check) || (!isChecked && check))
			clickCheckbox(id);
	}

	protected boolean checkLinkInRow(String table, String row, String id) {
		return selenium.isVisible("xpath=//table[contains(@Id, '" + table
				+ "')]//tr[./td[contains(text(), '" + row
				+ "')]]/td/a[contains(@Id, '" + id + "')]");
	}

	protected String getSafeOnlineAttributeValue(String attribute) {
		return selenium
				.getText("//span[contains(@class, 'so-nameoutput') and contains(text(), '"
						+ attribute + "')]//following::span");
	}

	protected boolean checkTextPresent(String text) {
		return selenium.isTextPresent(text);
	}

	protected void waitForRedirect(String redirectPage) {
		PageUtils.waitForRedirect(Page.acceptanceTestManager, redirectPage);
	}

	// It is possible a page already loaded before executing this command so
	// catch the timeout exception.
	protected void waitForPageToLoad() {
		try {
			selenium.waitForPageToLoad(TIMEOUT);
		} catch (SeleniumException e) {
			LOG.debug("Selenium exception: " + e.getMessage());
			if (!e.getMessage().startsWith("Timed out after"))
				throw e;
		}
	}
}