package test.unit.net.link.safeonline.user.webapp.pages.applications;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.common.OlasNamingStrategy;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.test.util.DummyLoginModule;
import net.link.safeonline.test.util.JaasTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.user.webapp.pages.applications.ApplicationsPage;
import net.link.safeonline.webapp.components.CustomPagingNavigator;
import net.link.safeonline.webapp.components.ErrorFeedbackPanel;
import net.link.safeonline.webapp.template.TemplatePage;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.unit.net.link.safeonline.user.webapp.UserTestApplication;


public class ApplicationsPageTest {

    private WicketTester        wicket;

    private SubscriptionService mockSubscriptionService;

    private JndiTestUtils       jndiTestUtils;


    @Before
    public void setUp()
            throws Exception {

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.setNamingStrategy(new OlasNamingStrategy());

        mockSubscriptionService = createMock(SubscriptionService.class);
        jndiTestUtils.bindComponent(SubscriptionService.class, mockSubscriptionService);

        wicket = new WicketTester(new UserTestApplication());
    }

    @After
    public void tearDown()
            throws Exception {

        jndiTestUtils.tearDown();

    }

    @Test
    public void testPageCannotUnsubscribe()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        JaasTestUtils.initJaasLoginModule(DummyLoginModule.class);
        wicket.getServletSession().setAttribute(LoginManager.USERID_SESSION_ATTRIBUTE, userId);

        String application1FriendlyName = "application-1-friendly-name";
        String application2FriendlyName = "application-2-friendly-name";

        SubscriptionEntity subscription1 = new SubscriptionEntity();
        ApplicationEntity application1 = new ApplicationEntity();
        application1.setId(1L);
        application1.setFriendlyName(application1FriendlyName);
        subscription1.setApplication(application1);

        SubscriptionEntity subscription2 = new SubscriptionEntity();
        ApplicationEntity application2 = new ApplicationEntity();
        application2.setId(2L);
        application2.setFriendlyName(application2FriendlyName);
        subscription2.setApplication(application2);

        List<SubscriptionEntity> subscriptionList = new LinkedList<SubscriptionEntity>();
        subscriptionList.add(subscription1);
        subscriptionList.add(subscription2);

        // stubs
        expect(mockSubscriptionService.listSubscriptions()).andReturn(subscriptionList);

        // prepare
        replay(mockSubscriptionService);

        // operate
        wicket.startPage(ApplicationsPage.class);

        // verify
        verify(mockSubscriptionService);

        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + ApplicationsPage.SUBSCRIPTIONS_ID, DataView.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + ApplicationsPage.SUBSCRIPTIONS_ID + ":1:" + ApplicationsPage.VIEW_LINK_ID,
                Link.class);
        wicket.assertLabel(TemplatePage.CONTENT_ID + ":" + ApplicationsPage.SUBSCRIPTIONS_ID + ":1:" + ApplicationsPage.VIEW_LINK_ID + ":"
                + ApplicationsPage.NAME_LABEL_ID, application1FriendlyName);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + ApplicationsPage.SUBSCRIPTIONS_ID + ":1:"
                + ApplicationsPage.UNSUBSCRIBE_LINK_ID, Link.class);

        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + ApplicationsPage.SUBSCRIPTIONS_ID, DataView.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + ApplicationsPage.SUBSCRIPTIONS_ID + ":2:" + ApplicationsPage.VIEW_LINK_ID,
                Link.class);
        wicket.assertLabel(TemplatePage.CONTENT_ID + ":" + ApplicationsPage.SUBSCRIPTIONS_ID + ":2:" + ApplicationsPage.VIEW_LINK_ID + ":"
                + ApplicationsPage.NAME_LABEL_ID, application2FriendlyName);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + ApplicationsPage.SUBSCRIPTIONS_ID + ":2:"
                + ApplicationsPage.UNSUBSCRIBE_LINK_ID, Link.class);

        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + ApplicationsPage.NAVIGATOR_ID, CustomPagingNavigator.class);

        // reset stubs
        reset(mockSubscriptionService);

        // stubs
        mockSubscriptionService.unsubscribe(application1.getId());
        expectLastCall().andThrow(new PermissionDeniedException(""));

        // prepare
        replay(mockSubscriptionService);

        // operate
        wicket.clickLink(TemplatePage.CONTENT_ID + ":" + ApplicationsPage.SUBSCRIPTIONS_ID + ":1:" + ApplicationsPage.UNSUBSCRIBE_LINK_ID);

        // verify
        verify(mockSubscriptionService);
        wicket.assertRenderedPage(ApplicationsPage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + ApplicationsPage.FEEDBACK_ID, ErrorFeedbackPanel.class);
        wicket.assertErrorMessages(new String[] { "errorUserMayNotUnsubscribeFrom" });
    }
}
