/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.accept.net.link.safeonline.demo;

import java.util.UUID;

import junit.framework.TestCase;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.webapp.AcceptanceTestManager;
import net.link.safeonline.webapp.PageUtils;
import net.link.safeonline.webapp.WebappConstants;
import net.link.safeonline.webapp.auth.AuthIdentityConfirmation;
import net.link.safeonline.webapp.auth.AuthMain;
import net.link.safeonline.webapp.auth.AuthMissingAttributes;
import net.link.safeonline.webapp.auth.AuthSubscription;
import net.link.safeonline.webapp.auth.password.AuthUserNamePassword;
import net.link.safeonline.webapp.demo.payment.DemoPaymentMain;
import net.link.safeonline.webapp.demo.payment.DemoPaymentOverview;
import net.link.safeonline.webapp.oper.OperApplicationsMain;
import net.link.safeonline.webapp.oper.OperOverview;
import net.link.safeonline.webapp.oper.applications.OperApplicationEdit;
import net.link.safeonline.webapp.oper.applications.OperApplicationView;
import net.link.safeonline.webapp.oper.applications.OperApplications;
import net.link.safeonline.webapp.oper.attributes.OperAttributeAdd;
import net.link.safeonline.webapp.oper.attributes.OperAttributeAddAc;
import net.link.safeonline.webapp.oper.attributes.OperAttributeAddType;
import net.link.safeonline.webapp.oper.attributes.OperAttributeRemove;
import net.link.safeonline.webapp.oper.attributes.OperAttributes;
import net.link.safeonline.webapp.user.UserAccount;
import net.link.safeonline.webapp.user.UserOverview;
import net.link.safeonline.webapp.user.UserRemove;

/**
 * Payment demo tests.
 * 
 * @author wvdhaute
 * 
 */
public class DemoPaymentTest extends TestCase {

	private AcceptanceTestManager acceptanceTestManager;

	@Override
	protected void setUp() throws Exception {
		this.acceptanceTestManager = new AcceptanceTestManager();
		this.acceptanceTestManager.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		this.acceptanceTestManager.tearDown();
	}

	/**
	 * This test will :
	 * <ol>
	 * <li>Register new user</li>
	 * <li>Log new user in demo payment, confirm identity</li>
	 * <li>Add attribute to demo payment application identity</li>
	 * <li>Log new user in demo payment, confirm updated identity</li>
	 * <li>Remove attribute from demo payment</li>
	 * <li>Remove new user</li>
	 * </ol
	 * 
	 * @throws Exception
	 */
	public void testDemoPaymentTicket() throws Exception {
		// setup
		this.acceptanceTestManager.setContext("Testing the demo payment.");

		String login = UUID.randomUUID().toString();
		String password = "secret";
		String testAttribute = "test-attribute";

		// register test user
		PageUtils.registerUserWithPassword(this.acceptanceTestManager, login,
				password);

		// login test user to demo-ticket webapp
		DemoPaymentMain demoPaymentMain = new DemoPaymentMain();
		demoPaymentMain.open();

		AuthMain authMain = demoPaymentMain.login();
		authMain.selectDevice(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID);

		AuthUserNamePassword authUserNamePassword = (AuthUserNamePassword) authMain
				.next();
		authUserNamePassword.setLogin(login);
		authUserNamePassword.setPassword(password);
		authUserNamePassword.logon();

		AuthSubscription authSubscription = new AuthSubscription();
		AuthIdentityConfirmation authIdentityConfirmation = authSubscription
				.confirm();
		authIdentityConfirmation.agree();
		AuthMissingAttributes authMissingAttributes = new AuthMissingAttributes();
		authMissingAttributes.setAttributeValue(
				WebappConstants.DEMO_PAYMENT_VISA_LABEL, "0000111122223333");
		authMissingAttributes.save();

		PageUtils.waitForRedirect(this.acceptanceTestManager,
				DemoPaymentOverview.PAGE_NAME);
		DemoPaymentOverview demoPaymentOverview = new DemoPaymentOverview();
		demoPaymentOverview.checkLoggedIn(login);
		demoPaymentOverview.logout();

		// add new attribute type
		OperOverview operOverview = PageUtils
				.loginOperWithPassword(this.acceptanceTestManager,
						WebappConstants.OPER_ADMIN, "admin");
		OperAttributes operAttributes = operOverview.gotoAttributes();
		OperAttributeAdd operAttributeAdd = operAttributes.add();
		operAttributeAdd.setName(testAttribute);
		operAttributeAdd.setSingleValued();
		operAttributeAdd.next();

		OperAttributeAddType operAttributeAddType = new OperAttributeAddType();
		OperAttributeAddAc operAttributeAddAc = operAttributeAddType.next();
		operAttributeAddAc.setUserVisible(true);
		operAttributeAddAc.setUserEditable(true);
		operAttributes = operAttributeAddAc.add();
		assertTrue(operAttributes.isAttributePresent(testAttribute));

		// add new attribute type to demo payment identity
		OperApplicationsMain operApplicationsMain = operOverview
				.gotoApplicationsMain();
		OperApplications operApplications = operApplicationsMain
				.gotoApplications();
		OperApplicationView operApplicationView = operApplications
				.viewApplication("demo-payment");
		OperApplicationEdit operApplicationEdit = operApplicationView.edit();
		operApplicationEdit.setAttribute(testAttribute, true, true, true);
		operApplicationView = operApplicationEdit.save();
		operApplicationView.logout();

		// login test user in demo payment with new identity
		demoPaymentMain.open();

		authMain = demoPaymentMain.login();
		authMain.selectDevice(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID);

		authUserNamePassword = (AuthUserNamePassword) authMain.next();
		authUserNamePassword.setLogin(login);
		authUserNamePassword.setPassword(password);
		authUserNamePassword.logon();

		authIdentityConfirmation = new AuthIdentityConfirmation();
		authIdentityConfirmation.agree();
		authMissingAttributes = new AuthMissingAttributes();
		authMissingAttributes.setAttributeValue(testAttribute, "value");
		authMissingAttributes.save();

		PageUtils.waitForRedirect(this.acceptanceTestManager,
				DemoPaymentOverview.PAGE_NAME);
		demoPaymentOverview = new DemoPaymentOverview();
		demoPaymentOverview.checkLoggedIn(login);
		demoPaymentOverview.logout();

		// remove new attribute from demo payment identity
		operOverview = PageUtils
				.loginOperWithPassword(this.acceptanceTestManager,
						WebappConstants.OPER_ADMIN, "admin");
		operApplicationsMain = operOverview.gotoApplicationsMain();
		operApplications = operApplicationsMain.gotoApplications();
		operApplicationView = operApplications.viewApplication("demo-payment");
		operApplicationEdit = operApplicationView.edit();
		operApplicationEdit.setAttribute(testAttribute, false, false, false);
		operApplicationView = operApplicationEdit.save();

		// remove new attribute
		operAttributes = operApplicationView.gotoAttributes();
		OperAttributeRemove operAttributeRemove = operAttributes
				.removeAttribute(testAttribute);
		operAttributes = operAttributeRemove.remove();
		operAttributes.logout();

		// remove user
		UserOverview userOverview = PageUtils.loginUserWithPassword(
				this.acceptanceTestManager, login, password);

		// remove account
		UserAccount userAccount = userOverview.gotoAccount();
		UserRemove userRemove = userAccount.gotoRemove();
		userRemove.remove();

	}
}
