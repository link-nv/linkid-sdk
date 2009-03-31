package test.unit.net.link.safeonline.user.webapp.pages;

import java.util.UUID;

import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.test.util.DummyLoginModule;
import net.link.safeonline.test.util.JaasTestUtils;
import net.link.safeonline.user.webapp.pages.OverviewPage;
import net.link.safeonline.user.webapp.pages.account.AccountPage;
import net.link.safeonline.user.webapp.pages.applications.ApplicationsPage;
import net.link.safeonline.user.webapp.pages.devices.DevicesPage;
import net.link.safeonline.user.webapp.pages.profile.ProfilePage;
import net.link.safeonline.user.webapp.template.NavigationPanel;
import net.link.safeonline.user.webapp.template.UserTemplatePage;
import net.link.safeonline.webapp.template.TemplatePage;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.unit.net.link.safeonline.user.webapp.UserTestApplication;


public class OverviewPageTest {

    private WicketTester wicket;


    @Before
    public void setUp()
            throws Exception {

        wicket = new WicketTester(new UserTestApplication());
    }

    @After
    public void tearDown()
            throws Exception {

    }

    @Test
    public void testPage()
            throws Exception {

        // setup
        JaasTestUtils.initJaasLoginModule(DummyLoginModule.class);
        wicket.getServletSession().setAttribute(LoginManager.USERID_SESSION_ATTRIBUTE, UUID.randomUUID().toString());

        // operate
        wicket.startPage(OverviewPage.class);

        // verify
        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + OverviewPage.PROFILE_LINK_ID, ProfilePage.class);
        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + OverviewPage.APPLICATIONS_LINK_ID, ApplicationsPage.class);
        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + OverviewPage.DEVICES_LINK_ID, DevicesPage.class);
        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + OverviewPage.ACCOUNT_LINK_ID, AccountPage.class);

        wicket.assertPageLink(TemplatePage.HEADER_ID + ":" + UserTemplatePage.NAVIGATION_ID + ":" + NavigationPanel.HOME_LINK_ID,
                OverviewPage.class);
        wicket.assertPageLink(TemplatePage.HEADER_ID + ":" + UserTemplatePage.NAVIGATION_ID + ":" + NavigationPanel.PROFILE_LINK_ID,
                ProfilePage.class);
        wicket.assertPageLink(TemplatePage.HEADER_ID + ":" + UserTemplatePage.NAVIGATION_ID + ":" + NavigationPanel.APPLICATIONS_LINK_ID,
                ApplicationsPage.class);
        wicket.assertPageLink(TemplatePage.HEADER_ID + ":" + UserTemplatePage.NAVIGATION_ID + ":" + NavigationPanel.DEVICES_LINK_ID,
                DevicesPage.class);
        wicket.assertPageLink(TemplatePage.HEADER_ID + ":" + UserTemplatePage.NAVIGATION_ID + ":" + NavigationPanel.ACCOUNT_LINK_ID,
                AccountPage.class);

    }
}
