/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.accept.net.link.safeonline;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.openqa.selenium.server.SeleniumServer;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;

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

	public static final String SAFE_ONLINE_AUTH_WEBAPP_PREFIX = "/olas-auth";

	public static final String SAFE_ONLINE_BEID_WEBAPP_PREFIX = "/olas-beid";

	public static final String SAFE_ONLINE_ENCAP_WEBAPP_PREFIX = "/olas-encap";

	public static final String SAFE_ONLINE_USER_WEBAPP_PREFIX = "/olas";

	public static final String SAFE_ONLINE_OPER_WEBAPP_PREFIX = "/olas-oper";

	public static final String SAFE_ONLINE_OWNER_WEBAPP_PREFIX = "/olas-owner";

	public static final String SAFE_ONLINE_HELPDESK_WEBAPP_PREFIX = "/olas-helpdesk";

	public static final String SAFE_ONLINE_DEMO_TICKET_WEBAPP_PREFIX = "/demo-ticket";

	public static final String SAFE_ONLINE_DEMO_LAWYER_WEBAPP_PREFIX = "/demo-lawyer";

	public static final String SAFE_ONLINE_DEMO_PAYMENT_WEBAPP_PREFIX = "/demo-payment";

	public static final String SAFE_ONLINE_DEMO_MANDATE_WEBAPP_PREFIX = "/demo-mandate";

	public static final String SAFE_ONLINE_DEMO_PRESCRIPTION_WEBAPP_PREFIX = "/demo-prescription";

	private static final Log LOG = LogFactory
			.getLog(AcceptanceTestManager.class);

	private Selenium selenium;

	SeleniumServer seleniumServer;

	public static final int SELENIUM_SERVER_PORT = 4455;

	private static final String TIMEOUT = "5000";

	private static final int PAUSE = 1500;

	private static final int PAUSE_TIMEOUT = 10;

	String safeOnlineLocation;

	String captchaString;

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
		waitForPageToLoad();

	}

	public void openOperWebApp(String page) {
		this.selenium.open(this.safeOnlineLocation
				+ SAFE_ONLINE_OPER_WEBAPP_PREFIX + page);
		waitForPageToLoad();
	}

	public void openOwnerWebApp(String page) {
		this.selenium.open(this.safeOnlineLocation
				+ SAFE_ONLINE_OWNER_WEBAPP_PREFIX + page);
		waitForPageToLoad();
	}

	public void openHelpdeskWebApp(String page) {
		this.selenium.open(this.safeOnlineLocation
				+ SAFE_ONLINE_HELPDESK_WEBAPP_PREFIX + page);
		waitForPageToLoad();
	}

	public void openDemoTicketWebApp(String page) {
		this.selenium.open(this.safeOnlineLocation
				+ SAFE_ONLINE_DEMO_TICKET_WEBAPP_PREFIX + page);
		waitForPageToLoad();
	}

	public void openDemoPaymentWebApp(String page) {
		this.selenium.open(this.safeOnlineLocation
				+ SAFE_ONLINE_DEMO_PAYMENT_WEBAPP_PREFIX + page);
		waitForPageToLoad();
	}

	public void openDemoLawyerWebApp(String page) {
		this.selenium.open(this.safeOnlineLocation
				+ SAFE_ONLINE_DEMO_LAWYER_WEBAPP_PREFIX + page);
		waitForPageToLoad();
	}

	public void openDemoPrescriptionWebApp(String page) {
		this.selenium.open(this.safeOnlineLocation
				+ SAFE_ONLINE_DEMO_PRESCRIPTION_WEBAPP_PREFIX + page);
		waitForPageToLoad();
	}

	public void openDemoMandateWebApp(String page) {
		this.selenium.open(this.safeOnlineLocation
				+ SAFE_ONLINE_DEMO_MANDATE_WEBAPP_PREFIX + page);
		waitForPageToLoad();
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

	public void clickLinkInRow(String table, String row, String id) {
		this.selenium.click("xpath=//table[contains(@Id, '" + table
				+ "')]//tr[./td[contains(text(), '" + row
				+ "')]]/td/a[contains(@Id, '" + id + "')]");
	}

	public void clickLinkAndWait(String id) {
		clickLink(id);
		waitForPageToLoad();
	}

	public void clickLinkInRowAndWait(String table, String row, String id) {
		clickLinkInRow(table, row, id);
		waitForPageToLoad();
	}

	public void clickRadioButton(String value) {
		this.selenium
				.click("xpath=//input[@type = 'radio' and contains(@value, '"
						+ value + "')]");
	}

	public boolean checkLinkInRow(String table, String row, String id) {
		return this.selenium.isVisible("xpath=//table[contains(@Id, '" + table
				+ "')]//tr[./td[contains(text(), '" + row
				+ "')]]/td/a[contains(@Id, '" + id + "')]");
	}

	public void waitForRedirect(String page) throws InterruptedException {
		int timeout = 0;
		while (!this.selenium.getLocation().endsWith(page)
				&& timeout != PAUSE_TIMEOUT) {
			LOG.debug("page: " + this.selenium.getLocation());
			Thread.sleep(PAUSE);
			timeout++;
		}
		Assert.assertTrue(this.selenium.getLocation().endsWith(page));
	}

	public void register(String login, String password)
			throws InterruptedException {
		openUserWebApp("/");

		clickLink("login");
		waitForRedirect(SAFE_ONLINE_AUTH_WEBAPP_PREFIX + "/first-time.seam");

		// first-time.xhtml
		clickButtonAndWait("new");

		// main.xhtml
		clickButtonAndWait("new-user");

		// new-user.xhtml
		fillInputField("login", login);
		fillInputField("captcha", getCaptcha());
		clickButtonAndWait("register");

		// new-user-device.xhtml
		clickRadioButton("password");
		clickButtonAndWait("next");

		// password/register-password.xhtml
		fillInputField("password1", password);
		fillInputField("password2", password);
		clickButton("change");

		waitForRedirect("overview.seam");

		logout();
	}

	public void logon(String login, String password)
			throws InterruptedException {
		clickLinkAndWait("login");
		waitForRedirect(SAFE_ONLINE_AUTH_WEBAPP_PREFIX + "/main.seam");

		// main.xhtml
		clickRadioButton("password");
		clickButtonAndWait("next");

		// password/username-password.xhtml
		fillInputField(":username", login);
		fillInputField(":password", password);
		clickButtonAndWait(":login");

		waitForRedirect("overview.seam");
	}

	public void logout() {
		clickLinkAndWait("logout");
		Assert.assertTrue(this.selenium.isTextPresent("Login"));
	}

	public void userLogon(String login, String password)
			throws InterruptedException {
		openUserWebApp("/");
		logon(login, password);
	}

	public void operLogon(String login, String password)
			throws InterruptedException {
		openOperWebApp("/");
		logon(login, password);
	}

	public void ownerLogon(String login, String password)
			throws InterruptedException {
		openOwnerWebApp("/");
		logon(login, password);
	}

	public String getCaptcha() throws InterruptedException {
		String jSessionId = getJSessionID();
		LOG.debug("session id: " + jSessionId);
		JFrame captchaFrame = new CaptchaFrame(jSessionId);
		while (captchaFrame.isShowing())
			Thread.sleep(1000);
		if (null == this.captchaString)
			return "";
		return this.captchaString;
	}

	public String getJSessionID() {
		String cookies = this.selenium.getCookie();
		if (cookies.indexOf("JSESSIONID") == -1)
			return null;
		StringTokenizer st = new StringTokenizer(cookies);
		while (st.hasMoreTokens()) {
			StringTokenizer st2 = new StringTokenizer(st.nextToken(), "=");
			while (st2.hasMoreTokens()) {
				String key = st2.nextToken();
				String val = st2.nextToken();
				if (key.equals("JSESSIONID"))
					return val;
			}
		}
		return null;
	}

	// It is possible a page already loaded before executing this command so
	// catch the timeout exception.
	public void waitForPageToLoad() {
		try {
			this.selenium.waitForPageToLoad(TIMEOUT);
		} catch (SeleniumException e) {
			LOG.debug("Selenium exception: " + e.getMessage());
			if (!e.getMessage().startsWith("Timed out after"))
				throw e;
		}
	}

	private class CaptchaFrame extends JFrame {

		private static final long serialVersionUID = 1L;

		private JLabel label = new JLabel();
		private JTextField captchaText = new JTextField(15);
		private JButton refresh = new JButton("Refresh");
		private JButton submit = new JButton("Submit");

		private String jSessionId;

		public CaptchaFrame(String jSessionId) {
			this.jSessionId = jSessionId;
			loadCaptcha();

			JPanel imagePanel = new JPanel(new FlowLayout());
			imagePanel.add(this.label);
			imagePanel.add(this.refresh);

			JPanel inputPanel = new JPanel(new FlowLayout());
			inputPanel.add(this.captchaText);
			inputPanel.add(this.submit);

			this.getContentPane().add(imagePanel, BorderLayout.CENTER);
			this.getContentPane().add(inputPanel, BorderLayout.SOUTH);
			this.setTitle("Captcha");
			this.pack();
			this.setVisible(true);

			handleEvents();
		}

		private void handleEvents() {
			this.submit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					AcceptanceTestManager.this.captchaString = getCaptchaText();
					close();
				}
			});

			this.refresh.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					loadCaptcha();
				}
			});
		}

		public void loadCaptcha() {
			try {
				HttpClient httpClient = new HttpClient();
				HttpMethod method = new GetMethod(
						AcceptanceTestManager.this.safeOnlineLocation
								+ SAFE_ONLINE_AUTH_WEBAPP_PREFIX + "/captcha");
				method.setRequestHeader("Cookie", "JSESSIONID="
						+ this.jSessionId);

				httpClient.executeMethod(method);
				Image captchaImage = ImageIO.read(method
						.getResponseBodyAsStream());
				this.label.setIcon(new ImageIcon(captchaImage));

			} catch (IOException e) {
				return;
			}
		}

		public String getCaptchaText() {
			return this.captchaText.getText();
		}

		public void close() {
			this.dispose();
		}

	}
}
