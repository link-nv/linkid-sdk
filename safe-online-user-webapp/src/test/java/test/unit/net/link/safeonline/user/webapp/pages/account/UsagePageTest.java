package test.unit.net.link.safeonline.user.webapp.pages.account;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.UUID;

import net.link.safeonline.authentication.service.UsageAgreementService;
import net.link.safeonline.common.OlasNamingStrategy;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.SubjectManager;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.test.util.DummyLoginModule;
import net.link.safeonline.test.util.JaasTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
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


public class UsagePageTest {

    private WicketTester          wicket;

    private UsageAgreementService mockUsageAgreementService;
    private SubjectManager        mockSubjectManager;

    private JndiTestUtils         jndiTestUtils;


    @Before
    public void setUp()
            throws Exception {

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.setNamingStrategy(new OlasNamingStrategy());

        mockUsageAgreementService = createMock(UsageAgreementService.class);
        jndiTestUtils.bindComponent(UsageAgreementService.class, mockUsageAgreementService);

        mockSubjectManager = createMock(SubjectManager.class);
        jndiTestUtils.bindComponent(SubjectManager.class, mockSubjectManager);

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
        String usageText = "Global Usage Agreement Text";
        JaasTestUtils.initJaasLoginModule(DummyLoginModule.class);
        wicket.getServletSession().setAttribute(LoginManager.USERID_SESSION_ATTRIBUTE, userId);

        SubjectEntity subject = new SubjectEntity(userId);
        subject.setConfirmedUsageAgreementVersion(1L);

        // stubs
        expect(mockSubjectManager.getCallerSubject()).andReturn(subject);
        expect(
                mockUsageAgreementService.getGlobalUsageAgreementText(wicket.getWicketSession().getLocale().getLanguage(),
                        subject.getConfirmedUsageAgreementVersion())).andReturn(usageText);

        // prepare
        replay(mockSubjectManager, mockUsageAgreementService);

        // operate
        wicket.startPage(UsagePage.class);

        // verify
        verify(mockSubjectManager, mockUsageAgreementService);

        wicket.assertLabel(TemplatePage.CONTENT_ID + ":" + TemplatePage.SIDEBAR_ID + ":" + SidebarBorder.HELP_MESSAGE_ID,
                "helpUsageAgreement");
        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + TemplatePage.SIDEBAR_ID + ":" + SidebarBorder.SIDE_LINKS_ID + ":"
                + SidebarBorder.LINKS_ID + ":0:" + SidebarBorder.LINK_ID, AccountPage.class);
        wicket.assertLabel(TemplatePage.CONTENT_ID + ":" + TemplatePage.SIDEBAR_ID + ":" + SidebarBorder.SIDE_LINKS_ID + ":"
                + SidebarBorder.LINKS_ID + ":0:" + SidebarBorder.LINK_ID + ":" + SidebarBorder.LINK_MESSAGE_ID, "accountManagement");

        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + TemplatePage.SIDEBAR_ID + ":" + SidebarBorder.SIDE_LINKS_ID + ":"
                + SidebarBorder.LINKS_ID + ":1:" + SidebarBorder.LINK_ID, HistoryPage.class);
        wicket.assertLabel(TemplatePage.CONTENT_ID + ":" + TemplatePage.SIDEBAR_ID + ":" + SidebarBorder.SIDE_LINKS_ID + ":"
                + SidebarBorder.LINKS_ID + ":1:" + SidebarBorder.LINK_ID + ":" + SidebarBorder.LINK_MESSAGE_ID, "history");

        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + TemplatePage.SIDEBAR_ID + ":" + SidebarBorder.SIDE_LINKS_ID + ":"
                + SidebarBorder.LINKS_ID + ":2:" + SidebarBorder.LINK_ID, RemovePage.class);
        wicket.assertLabel(TemplatePage.CONTENT_ID + ":" + TemplatePage.SIDEBAR_ID + ":" + SidebarBorder.SIDE_LINKS_ID + ":"
                + SidebarBorder.LINKS_ID + ":2:" + SidebarBorder.LINK_ID + ":" + SidebarBorder.LINK_MESSAGE_ID, "removeAccount");

        wicket.assertLabel(TemplatePage.CONTENT_ID + ":" + UsagePage.USAGE_TEXT, usageText);

    }
}
