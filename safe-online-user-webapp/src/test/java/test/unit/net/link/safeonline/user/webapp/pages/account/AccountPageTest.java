package test.unit.net.link.safeonline.user.webapp.pages.account;

import java.util.UUID;

import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.test.util.DummyLoginModule;
import net.link.safeonline.test.util.JaasTestUtils;
import net.link.safeonline.user.webapp.pages.account.AccountPage;
import net.link.safeonline.user.webapp.pages.account.HistoryPage;
import net.link.safeonline.user.webapp.pages.account.RemovePage;
import net.link.safeonline.user.webapp.pages.account.UsagePage;
import net.link.safeonline.webapp.template.SidebarBorder;
import net.link.safeonline.webapp.template.TemplatePage;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.unit.net.link.safeonline.user.webapp.UserTestApplication;


public class AccountPageTest {

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
        wicket.startPage(AccountPage.class);

        // verify
        wicket.assertLabel(TemplatePage.CONTENT_ID + ":" + TemplatePage.SIDEBAR_ID + ":" + SidebarBorder.HELP_MESSAGE_ID,
                "helpAccountManagement");
        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + TemplatePage.SIDEBAR_ID + ":" + SidebarBorder.SIDE_LINKS_ID + ":"
                + SidebarBorder.LINKS_ID + ":0:" + SidebarBorder.LINK_ID, HistoryPage.class);
        wicket.assertLabel(TemplatePage.CONTENT_ID + ":" + TemplatePage.SIDEBAR_ID + ":" + SidebarBorder.SIDE_LINKS_ID + ":"
                + SidebarBorder.LINKS_ID + ":0:" + SidebarBorder.LINK_ID + ":" + SidebarBorder.LINK_MESSAGE_ID, "history");

        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + TemplatePage.SIDEBAR_ID + ":" + SidebarBorder.SIDE_LINKS_ID + ":"
                + SidebarBorder.LINKS_ID + ":1:" + SidebarBorder.LINK_ID, UsagePage.class);
        wicket.assertLabel(TemplatePage.CONTENT_ID + ":" + TemplatePage.SIDEBAR_ID + ":" + SidebarBorder.SIDE_LINKS_ID + ":"
                + SidebarBorder.LINKS_ID + ":1:" + SidebarBorder.LINK_ID + ":" + SidebarBorder.LINK_MESSAGE_ID, "usageAgreement");

        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + TemplatePage.SIDEBAR_ID + ":" + SidebarBorder.SIDE_LINKS_ID + ":"
                + SidebarBorder.LINKS_ID + ":2:" + SidebarBorder.LINK_ID, RemovePage.class);
        wicket.assertLabel(TemplatePage.CONTENT_ID + ":" + TemplatePage.SIDEBAR_ID + ":" + SidebarBorder.SIDE_LINKS_ID + ":"
                + SidebarBorder.LINKS_ID + ":2:" + SidebarBorder.LINK_ID + ":" + SidebarBorder.LINK_MESSAGE_ID, "removeAccount");

        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + AccountPage.HISTORY_LINK_ID, HistoryPage.class);
        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + AccountPage.USAGE_LINK_ID, UsagePage.class);
        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + AccountPage.REMOVE_LINK_ID, RemovePage.class);

    }
}
