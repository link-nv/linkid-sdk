package test.unit.net.link.safeonline.user.webapp.pages;

import java.util.UUID;

import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.test.util.DummyLoginModule;
import net.link.safeonline.test.util.JaasTestUtils;
import net.link.safeonline.user.webapp.pages.MainPage;
import net.link.safeonline.webapp.template.SidebarBorder;
import net.link.safeonline.webapp.template.TemplatePage;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.unit.net.link.safeonline.user.webapp.UserTestApplication;


public class MainPageTest {

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
    public void testLoginLinkPresent()
            throws Exception {

        // verify.
        wicket.startPage(MainPage.class);

        String loginLinkPath = TemplatePage.CONTENT_ID + ":" + TemplatePage.SIDEBAR_ID + ":" + SidebarBorder.SIDE_LINKS_ID + ":"
                + SidebarBorder.LINKS_ID + ":0:" + SidebarBorder.LINK_ID;
        String loginLinkMessagePath = loginLinkPath + ":" + SidebarBorder.LINK_MESSAGE_ID;
        wicket.assertComponent(loginLinkPath, Link.class);
        wicket.assertLabel(loginLinkMessagePath, "loginaction");
    }

    @Test
    public void testLogoutLinkPresent()
            throws Exception {

        // setup
        JaasTestUtils.initJaasLoginModule(DummyLoginModule.class);
        wicket.getServletSession().setAttribute(LoginManager.USERID_SESSION_ATTRIBUTE, UUID.randomUUID().toString());

        // verify.
        wicket.startPage(MainPage.class);

        String logoutLinkPath = TemplatePage.CONTENT_ID + ":" + TemplatePage.SIDEBAR_ID + ":" + SidebarBorder.SIDE_LINKS_ID + ":"
                + SidebarBorder.LINKS_ID + ":0:" + SidebarBorder.LINK_ID;
        String logoutLinkMessagePath = logoutLinkPath + ":" + SidebarBorder.LINK_MESSAGE_ID;
        wicket.assertComponent(logoutLinkPath, Link.class);
        wicket.assertLabel(logoutLinkMessagePath, "logout");
    }
}
