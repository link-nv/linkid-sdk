/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.webapp;

import junit.framework.Assert;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.webapp.auth.AuthFirstTime;
import net.link.safeonline.webapp.auth.AuthIdentityConfirmation;
import net.link.safeonline.webapp.auth.AuthMain;
import net.link.safeonline.webapp.auth.AuthMissingAttributes;
import net.link.safeonline.webapp.auth.AuthNewUser;
import net.link.safeonline.webapp.auth.AuthNewUserDevice;
import net.link.safeonline.webapp.auth.AuthSubscription;
import net.link.safeonline.webapp.auth.password.AuthRegisterPassword;
import net.link.safeonline.webapp.auth.password.AuthUserNamePassword;
import net.link.safeonline.webapp.demo.payment.DemoPaymentMain;
import net.link.safeonline.webapp.demo.payment.DemoPaymentSearch;
import net.link.safeonline.webapp.helpdesk.HelpdeskMain;
import net.link.safeonline.webapp.helpdesk.HelpdeskOverview;
import net.link.safeonline.webapp.oper.OperOverview;
import net.link.safeonline.webapp.oper.authorization.OperMain;
import net.link.safeonline.webapp.owner.OwnerMain;
import net.link.safeonline.webapp.owner.OwnerOverview;
import net.link.safeonline.webapp.user.UserMain;
import net.link.safeonline.webapp.user.UserOverview;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class providing some standard webapp operations like login, register,
 * ...
 * 
 * @author wvdhaute
 * 
 */
public class PageUtils {

	private static final Log LOG = LogFactory.getLog(PageUtils.class);

	private static final int PAUSE = 1000;

	private static final int PAUSE_TIMEOUT = 20;

	public static void registerUserWithPassword(
			AcceptanceTestManager acceptanceTestManager, String login,
			String password) {
		UserMain userMain = new UserMain();
		userMain.open();

		AuthFirstTime authFirstTime = userMain.loginFirstTime();

		AuthMain authMain = authFirstTime.newUser();

		AuthNewUser authNewUser = authMain.newUser();
		authNewUser.setLogin(login);
		authNewUser.setCaptcha(acceptanceTestManager);

		AuthNewUserDevice authNewUserDevice = authNewUser.register();
		authNewUserDevice
				.selectDevice(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID);

		AuthRegisterPassword authRegisterPassword = (AuthRegisterPassword) authNewUserDevice
				.next();
		authRegisterPassword.setPassword1(password);
		authRegisterPassword.setPassword2(password);
		authRegisterPassword.register();

		UserOverview userOverview = new UserOverview();

		// verify
		Assert.assertTrue(acceptanceTestManager.getLocation().endsWith(
				UserOverview.PAGE_NAME));

		userOverview.logout();
	}

	public static UserOverview loginUserWithPassword(
			AcceptanceTestManager acceptanceTestManager, String login,
			String password) {
		UserMain userMain = new UserMain();
		userMain.open();
		userMain.login();

		loginWithPassword(login, password);

		waitForRedirect(acceptanceTestManager, UserOverview.PAGE_NAME);
		return new UserOverview();
	}

	public static UserOverview loginFirstTimeUserWithPassword(
			AcceptanceTestManager acceptanceTestManager, String login,
			String password) {
		UserMain userMain = new UserMain();
		userMain.open();
		userMain.loginFirstTime();

		loginFirstTimeWithPassword(login, password);

		waitForRedirect(acceptanceTestManager, UserOverview.PAGE_NAME);
		return new UserOverview();
	}

	public static OperOverview loginOperWithPassword(
			AcceptanceTestManager acceptanceTestManager, String login,
			String password) {
		OperMain operMain = new OperMain();
		operMain.open();
		operMain.login();

		loginWithPassword(login, password);

		waitForRedirect(acceptanceTestManager, OperOverview.PAGE_NAME);
		return new OperOverview();
	}

	public static OperOverview loginFirstTimeOperWithPassword(
			AcceptanceTestManager acceptanceTestManager, String login,
			String password) {
		OperMain operMain = new OperMain();
		operMain.open();
		operMain.loginFirstTime();

		loginFirstTimeWithPassword(login, password);

		waitForRedirect(acceptanceTestManager, OperOverview.PAGE_NAME);
		return new OperOverview();
	}

	public static OwnerOverview loginOwnerWithPassword(
			AcceptanceTestManager acceptanceTestManager, String login,
			String password) {
		OwnerMain ownerMain = new OwnerMain();
		ownerMain.open();
		ownerMain.login();

		loginWithPassword(login, password);

		waitForRedirect(acceptanceTestManager, OwnerOverview.PAGE_NAME);
		return new OwnerOverview();
	}

	public static HelpdeskOverview loginHelpdeskWithPassword(
			AcceptanceTestManager acceptanceTestManager, String login,
			String password) {
		HelpdeskMain helpdeskMain = new HelpdeskMain();
		helpdeskMain.open();
		helpdeskMain.login();

		loginWithPassword(login, password);

		waitForRedirect(acceptanceTestManager, HelpdeskOverview.PAGE_NAME);
		return new HelpdeskOverview();
	}

	private static void loginFirstTimeWithPassword(String login, String password) {
		AuthFirstTime authFirstTime = new AuthFirstTime();
		authFirstTime.existingUser();

		loginWithPassword(login, password);
	}

	private static void loginWithPassword(String login, String password) {
		AuthMain authMain = new AuthMain();
		authMain.selectDevice(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID);

		AuthUserNamePassword authUserNamePassword = (AuthUserNamePassword) authMain
				.next();
		authUserNamePassword.setLogin(login);
		authUserNamePassword.setPassword(password);
		authUserNamePassword.logon();
	}

	public static DemoPaymentSearch loginPaymentAdmin(
			AcceptanceTestManager acceptanceTestManager) {
		DemoPaymentMain demoPaymentMain = new DemoPaymentMain();
		demoPaymentMain.open();
		AuthMain authMain = demoPaymentMain.login();
		authMain.selectDevice(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID);

		AuthUserNamePassword authUserNamePassword = (AuthUserNamePassword) authMain
				.next();
		authUserNamePassword.setLogin(WebappConstants.DEMO_PAYMENT_ADMIN);
		authUserNamePassword.setPassword("secret");
		authUserNamePassword.logon();

		// payment admin subscription
		if (StringUtils.contains(acceptanceTestManager.getLocation(),
				AuthSubscription.PAGE_NAME)) {
			AuthSubscription authSubscription = new AuthSubscription();
			AuthIdentityConfirmation authIdentityConfirmation = authSubscription
					.confirm();
			authIdentityConfirmation.agree();
			AuthMissingAttributes authMissingAttributes = new AuthMissingAttributes();
			authMissingAttributes
					.setAttributeValue(WebappConstants.DEMO_PAYMENT_VISA_LABEL,
							"9999888877776666");
			authMissingAttributes.save();
		}
		PageUtils.waitForRedirect(acceptanceTestManager,
				DemoPaymentSearch.PAGE_NAME);
		return new DemoPaymentSearch();
	}

	/**
	 * Causes the thread to sleep and every now and then check if we have landed
	 * on the specified page yet. Timeouts after a while.
	 * 
	 * @param redirectPage
	 */
	public static void waitForRedirect(
			AcceptanceTestManager acceptanceTestManager, String redirectPage) {
		int timeout = 0;
		while (!StringUtils.contains(acceptanceTestManager.getLocation(),
				redirectPage)
				&& timeout != PAUSE_TIMEOUT) {
			LOG.debug("page(" + timeout + ") : "
					+ acceptanceTestManager.getLocation());
			try {
				Thread.sleep(PAUSE);
			} catch (InterruptedException e) {
				Assert.fail("Thread interrupted");
			}
			timeout++;
		}
		Assert.assertTrue(StringUtils.contains(acceptanceTestManager
				.getLocation(), redirectPage));
	}
}
