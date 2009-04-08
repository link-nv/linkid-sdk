package test.unit.net.link.safeonline.user.webapp.pages.account;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertNull;

import java.util.UUID;

import net.link.safeonline.authentication.service.AccountService;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.test.util.DummyLoginModule;
import net.link.safeonline.test.util.JaasTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.user.webapp.pages.MainPage;
import net.link.safeonline.user.webapp.pages.account.AccountPage;
import net.link.safeonline.user.webapp.pages.account.HistoryPage;
import net.link.safeonline.user.webapp.pages.account.RemovePage;
import net.link.safeonline.user.webapp.pages.account.UsagePage;
import net.link.safeonline.util.ee.FieldNamingStrategy;
import net.link.safeonline.webapp.template.SidebarBorder;
import net.link.safeonline.webapp.template.TemplatePage;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.unit.net.link.safeonline.user.webapp.UserTestApplication;


public class RemovePageTest {

    private WicketTester   wicket;

    private AccountService mockAccountService;

    private JndiTestUtils  jndiTestUtils;


    @Before
    public void setUp()
            throws Exception {

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.setNamingStrategy(new FieldNamingStrategy());

        mockAccountService = createMock(AccountService.class);
        jndiTestUtils.bindComponent(AccountService.class, mockAccountService);

        wicket = new WicketTester(new UserTestApplication());
    }

    @After
    public void tearDown()
            throws Exception {

        jndiTestUtils.tearDown();

    }

    @Test
    public void testPage()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        JaasTestUtils.initJaasLoginModule(DummyLoginModule.class);
        wicket.getServletSession().setAttribute(LoginManager.USERID_SESSION_ATTRIBUTE, userId);

        // operate
        wicket.startPage(RemovePage.class);

        // verify
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + RemovePage.REMOVE_FORM_ID, Form.class);

        wicket.assertLabel(TemplatePage.CONTENT_ID + ":" + TemplatePage.SIDEBAR_ID + ":" + SidebarBorder.HELP_MESSAGE_ID,
                "helpRemoveAccount");
        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + TemplatePage.SIDEBAR_ID + ":" + SidebarBorder.SIDE_LINKS_ID + ":"
                + SidebarBorder.LINKS_ID + ":0:" + SidebarBorder.LINK_ID, AccountPage.class);
        wicket.assertLabel(TemplatePage.CONTENT_ID + ":" + TemplatePage.SIDEBAR_ID + ":" + SidebarBorder.SIDE_LINKS_ID + ":"
                + SidebarBorder.LINKS_ID + ":0:" + SidebarBorder.LINK_ID + ":" + SidebarBorder.LINK_MESSAGE_ID, "accountManagement");

        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + TemplatePage.SIDEBAR_ID + ":" + SidebarBorder.SIDE_LINKS_ID + ":"
                + SidebarBorder.LINKS_ID + ":1:" + SidebarBorder.LINK_ID, HistoryPage.class);
        wicket.assertLabel(TemplatePage.CONTENT_ID + ":" + TemplatePage.SIDEBAR_ID + ":" + SidebarBorder.SIDE_LINKS_ID + ":"
                + SidebarBorder.LINKS_ID + ":1:" + SidebarBorder.LINK_ID + ":" + SidebarBorder.LINK_MESSAGE_ID, "history");

        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + TemplatePage.SIDEBAR_ID + ":" + SidebarBorder.SIDE_LINKS_ID + ":"
                + SidebarBorder.LINKS_ID + ":2:" + SidebarBorder.LINK_ID, UsagePage.class);
        wicket.assertLabel(TemplatePage.CONTENT_ID + ":" + TemplatePage.SIDEBAR_ID + ":" + SidebarBorder.SIDE_LINKS_ID + ":"
                + SidebarBorder.LINKS_ID + ":2:" + SidebarBorder.LINK_ID + ":" + SidebarBorder.LINK_MESSAGE_ID, "usageAgreement");

        // stubs
        mockAccountService.removeAccount();

        // prepare
        replay(mockAccountService);

        // operate
        FormTester removeForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + RemovePage.REMOVE_FORM_ID);
        removeForm.submit(RemovePage.REMOVE_BUTTON_ID);

        // verify
        verify(mockAccountService);

        wicket.assertRenderedPage(MainPage.class);
        assertNull(wicket.getServletSession().getAttribute(LoginManager.USERID_SESSION_ATTRIBUTE));

    }
}
