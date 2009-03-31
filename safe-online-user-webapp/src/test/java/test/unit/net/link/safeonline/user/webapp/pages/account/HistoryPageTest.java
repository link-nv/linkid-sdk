package test.unit.net.link.safeonline.user.webapp.pages.account;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.LinkedList;
import java.util.UUID;

import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.entity.HistoryEntity;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.test.util.DummyLoginModule;
import net.link.safeonline.test.util.JaasTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.user.webapp.pages.account.AccountPage;
import net.link.safeonline.user.webapp.pages.account.HistoryPage;
import net.link.safeonline.user.webapp.pages.account.RemovePage;
import net.link.safeonline.user.webapp.pages.account.UsagePage;
import net.link.safeonline.util.ee.FieldNamingStrategy;
import net.link.safeonline.webapp.components.CustomPagingNavigator;
import net.link.safeonline.webapp.template.SidebarBorder;
import net.link.safeonline.webapp.template.TemplatePage;

import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.unit.net.link.safeonline.user.webapp.UserTestApplication;


public class HistoryPageTest {

    private WicketTester    wicket;

    private IdentityService mockIdentityService;

    private JndiTestUtils   jndiTestUtils;


    @Before
    public void setUp()
            throws Exception {

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.setNamingStrategy(new FieldNamingStrategy());

        mockIdentityService = createMock(IdentityService.class);
        jndiTestUtils.bindComponent(IdentityService.class, mockIdentityService);

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
        JaasTestUtils.initJaasLoginModule(DummyLoginModule.class);
        wicket.getServletSession().setAttribute(LoginManager.USERID_SESSION_ATTRIBUTE, UUID.randomUUID().toString());

        // stubs
        expect(mockIdentityService.listHistory()).andReturn(new LinkedList<HistoryEntity>());

        // prepare
        replay(mockIdentityService);

        // operate
        wicket.startPage(HistoryPage.class);

        // verify
        verify(mockIdentityService);

        wicket.assertLabel(TemplatePage.CONTENT_ID + ":" + TemplatePage.SIDEBAR_ID + ":" + SidebarBorder.HELP_MESSAGE_ID, "helpHistory");
        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + TemplatePage.SIDEBAR_ID + ":" + SidebarBorder.SIDE_LINKS_ID + ":"
                + SidebarBorder.LINKS_ID + ":0:" + SidebarBorder.LINK_ID, AccountPage.class);
        wicket.assertLabel(TemplatePage.CONTENT_ID + ":" + TemplatePage.SIDEBAR_ID + ":" + SidebarBorder.SIDE_LINKS_ID + ":"
                + SidebarBorder.LINKS_ID + ":0:" + SidebarBorder.LINK_ID + ":" + SidebarBorder.LINK_MESSAGE_ID, "accountManagement");

        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + TemplatePage.SIDEBAR_ID + ":" + SidebarBorder.SIDE_LINKS_ID + ":"
                + SidebarBorder.LINKS_ID + ":1:" + SidebarBorder.LINK_ID, UsagePage.class);
        wicket.assertLabel(TemplatePage.CONTENT_ID + ":" + TemplatePage.SIDEBAR_ID + ":" + SidebarBorder.SIDE_LINKS_ID + ":"
                + SidebarBorder.LINKS_ID + ":1:" + SidebarBorder.LINK_ID + ":" + SidebarBorder.LINK_MESSAGE_ID, "usageAgreement");

        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + TemplatePage.SIDEBAR_ID + ":" + SidebarBorder.SIDE_LINKS_ID + ":"
                + SidebarBorder.LINKS_ID + ":2:" + SidebarBorder.LINK_ID, RemovePage.class);
        wicket.assertLabel(TemplatePage.CONTENT_ID + ":" + TemplatePage.SIDEBAR_ID + ":" + SidebarBorder.SIDE_LINKS_ID + ":"
                + SidebarBorder.LINKS_ID + ":2:" + SidebarBorder.LINK_ID + ":" + SidebarBorder.LINK_MESSAGE_ID, "removeAccount");

        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + HistoryPage.HISTORY_ID, DataView.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + HistoryPage.NAVIGATOR_ID, CustomPagingNavigator.class);

    }
}
