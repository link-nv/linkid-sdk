/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.accept.net.link.safeonline.demo;

import java.util.UUID;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.link.safeonline.model.beid.BeIdConstants;
import net.link.safeonline.webapp.AcceptanceTestManager;
import net.link.safeonline.webapp.PageUtils;
import net.link.safeonline.webapp.WebappConstants;
import net.link.safeonline.webapp.auth.AuthIdentityConfirmation;
import net.link.safeonline.webapp.auth.AuthMain;
import net.link.safeonline.webapp.auth.AuthMissingAttributes;
import net.link.safeonline.webapp.auth.AuthSubscription;
import net.link.safeonline.webapp.demo.payment.DemoPaymentCards;
import net.link.safeonline.webapp.demo.payment.DemoPaymentCompleted;
import net.link.safeonline.webapp.demo.payment.DemoPaymentEntry;
import net.link.safeonline.webapp.demo.payment.DemoPaymentSearch;
import net.link.safeonline.webapp.demo.payment.DemoPaymentSearchResult;
import net.link.safeonline.webapp.demo.ticket.DemoTicketAdd;
import net.link.safeonline.webapp.demo.ticket.DemoTicketCheckout;
import net.link.safeonline.webapp.demo.ticket.DemoTicketList;
import net.link.safeonline.webapp.demo.ticket.DemoTicketMain;
import net.link.safeonline.webapp.demo.ticket.DemoTicketOverview;
import net.link.safeonline.webapp.user.UserAccount;
import net.link.safeonline.webapp.user.UserHistory;
import net.link.safeonline.webapp.user.UserOverview;
import net.link.safeonline.webapp.user.UserRemove;
import net.link.safeonline.webapp.user.device.UserDevices;
import net.link.safeonline.webapp.user.profile.UserProfile;


/**
 * Demo apps Acceptance Test.
 * 
 * @author fcorneli
 * 
 */
public class DemoTicketPaymentTest extends TestCase {

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
     * This test needs some manual intervention as BeId is used. The following steps are executed :
     * 
     * 1. Register test user with password, login user webapp and register BeId (check profile, applications, devices, history )
     * 
     * 2. Login with 'payment-admin' into payment webapp, set test user as 'junior'.
     * 
     * 3. Login with test user into ticket webapp, buy ticket, pay with payment webapp.
     * 
     * 4. Remove user in ticket webapp ( free up BeId ), login user webapp, remove beid, remove user.
     * 
     * @throws Exception
     */
    public void testDemoPaymentTicket() throws Exception {

        // setup
        this.acceptanceTestManager.setContext("Testing the demo payment and ticket webapp.");

        String login = UUID.randomUUID().toString();
        String password = "secret";

        // register test user
        PageUtils.registerUserWithPassword(this.acceptanceTestManager, login, password);

        // register test user beid device
        UserOverview userOverview = PageUtils.loginUserWithPassword(this.acceptanceTestManager, login, password);

        UserAccount userAccount = userOverview.gotoAccount();

        UserHistory userHistory = userAccount.gotoHistory();
        userHistory.checkHistoryPasswordLogon();

        UserDevices userDevices = userHistory.gotoDevices();
        userDevices.registerBeId();
        userDevices.logout();

        // set junior attribute in payment webapp for test user
        DemoPaymentSearch demoPaymentSearch = PageUtils.loginPaymentAdmin(this.acceptanceTestManager);
        demoPaymentSearch.setName(login);
        DemoPaymentSearchResult demoPaymentSearchResult = demoPaymentSearch.search();
        demoPaymentSearchResult.setJunior(true);
        demoPaymentSearchResult.save();
        Assert.assertTrue(demoPaymentSearchResult.getJunior());
        demoPaymentSearchResult.logout();

        // login test user to user webapp, check junior attribute set
        userOverview = PageUtils.loginUserWithPassword(this.acceptanceTestManager, login, password);
        UserProfile userProfile = userOverview.gotoProfile();
        Assert.assertEquals("true", userProfile.getAttributeValue(WebappConstants.DEMO_PAYMENT_JUNIOR_LABEL));
        userProfile.logout();

        // login test user to demo-ticket webapp
        DemoTicketMain demoTicketMain = new DemoTicketMain();
        demoTicketMain.open();

        AuthMain authMain = demoTicketMain.login();
        authMain.selectDevice(BeIdConstants.BEID_DEVICE_ID);
        authMain.next();
        PageUtils.waitForRedirect(this.acceptanceTestManager, AuthSubscription.PAGE_NAME);

        AuthSubscription authSubscription = new AuthSubscription();
        AuthIdentityConfirmation authIdentityConfirmation = authSubscription.confirm();
        authIdentityConfirmation.agree();
        PageUtils.waitForRedirect(this.acceptanceTestManager, DemoTicketOverview.PAGE_NAME);
        DemoTicketOverview demoTicketOverview = new DemoTicketOverview();
        demoTicketOverview.checkLoggedIn(login);

        // buy ticket
        DemoTicketAdd demoTicketAdd = demoTicketOverview.add();

        DemoTicketCheckout demoTicketCheckout = demoTicketAdd.checkout();

        DemoPaymentEntry demoPaymentEntry = demoTicketCheckout.confirm();

        // subscribe demo-payment
        authMain = demoPaymentEntry.confirm();
        authMain.selectDevice(BeIdConstants.BEID_DEVICE_ID);
        authMain.next();
        PageUtils.waitForRedirect(this.acceptanceTestManager, AuthSubscription.PAGE_NAME);

        authSubscription = new AuthSubscription();
        authIdentityConfirmation = authSubscription.confirm();
        authIdentityConfirmation.agree();
        AuthMissingAttributes authMissingAttributes = new AuthMissingAttributes();
        authMissingAttributes.setAttributeValue(WebappConstants.DEMO_PAYMENT_VISA_LABEL, "0000111122223333");
        authMissingAttributes.save();
        PageUtils.waitForRedirect(this.acceptanceTestManager, DemoPaymentCards.PAGE_NAME);

        // pay ticket
        DemoPaymentCards demoPaymentCards = new DemoPaymentCards();
        DemoPaymentCompleted demoPaymentCompleted = demoPaymentCards.confirm();

        // check ticket
        DemoTicketList demoTicketList = demoPaymentCompleted.done();
        demoTicketList.checkTicketPresent("GENT");

        // remove demo ticket user to free up NRN
        demoTicketOverview.open();
        demoTicketOverview.remove();

        // remove beid device
        userOverview = PageUtils.loginUserWithPassword(this.acceptanceTestManager, login, password);
        userDevices = userOverview.gotoDevices();
        userDevices.removeBeId();

        // remove account
        userAccount = userDevices.gotoAccount();
        UserRemove userRemove = userAccount.gotoRemove();
        userRemove.remove();
    }
}
