package test.unit.net.link.safeonline.user.webapp.pages.applications;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.UsageAgreementService;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.test.util.DummyLoginModule;
import net.link.safeonline.test.util.JaasTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.user.webapp.pages.applications.ViewSubscriptionPage;
import net.link.safeonline.util.ee.FieldNamingStrategy;
import net.link.safeonline.webapp.components.toggle.ToggleHeader;
import net.link.safeonline.webapp.template.TemplatePage;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.unit.net.link.safeonline.user.webapp.UserTestApplication;


public class ViewSubscriptionPageTest {

    private WicketTester          wicket;

    private UsageAgreementService mockUsageAgreementService;
    private IdentityService       mockIdentityService;

    private JndiTestUtils         jndiTestUtils;


    @Before
    public void setUp()
            throws Exception {

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.setNamingStrategy(new FieldNamingStrategy());

        mockUsageAgreementService = createMock(UsageAgreementService.class);
        mockIdentityService = createMock(IdentityService.class);
        jndiTestUtils.bindComponent(UsageAgreementService.class, mockUsageAgreementService);
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
        String userId = UUID.randomUUID().toString();
        JaasTestUtils.initJaasLoginModule(DummyLoginModule.class);
        wicket.getServletSession().setAttribute(LoginManager.USERID_SESSION_ATTRIBUTE, userId);

        String applicationName = "application-name";
        String applicationFriendlyName = "application-friendly-name";
        String applicationDescription = "Application description";
        String applicationOwnerName = "application-owner";
        String usageAgreementText = "Application Usage Agreement";

        ApplicationOwnerEntity applicationOwner = new ApplicationOwnerEntity();
        applicationOwner.setName(applicationOwnerName);
        ApplicationEntity application = new ApplicationEntity();
        application.setId(1L);
        application.setName(applicationName);
        application.setFriendlyName(applicationFriendlyName);
        application.setDescription(applicationDescription);
        application.setApplicationOwner(applicationOwner);

        SubscriptionEntity subscription = new SubscriptionEntity();
        subscription.setApplication(application);
        subscription.setConfirmedIdentityVersion(1L);

        String attribute1Name = "attribute-1-name";
        String attribute1Value = "attribute-1-value";
        String attribute2Name = "attribute-2-name";
        Date attribute2Value = new Date();
        AttributeDO attribute1 = new AttributeDO(attribute1Name, DatatypeType.STRING);
        attribute1.setStringValue(attribute1Value);
        AttributeDO attribute2 = new AttributeDO(attribute2Name, DatatypeType.DATE);
        attribute2.setDateValue(attribute2Value);
        List<AttributeDO> identityAttributes = new LinkedList<AttributeDO>();
        identityAttributes.add(attribute1);
        identityAttributes.add(attribute2);

        // stubs
        expect(
                mockUsageAgreementService.getUsageAgreementText(application.getId(), wicket.getWicketSession().getLocale().getLanguage(),
                        subscription.getConfirmedUsageAgreementVersion())).andReturn(usageAgreementText);
        expect(mockIdentityService.listConfirmedIdentity(applicationName, wicket.getWicketSession().getLocale())).andReturn(
                identityAttributes);

        // prepare
        replay(mockUsageAgreementService, mockIdentityService);

        // operate
        ViewSubscriptionPage viewSubscriptionPage = new ViewSubscriptionPage(subscription);
        wicket.startPage(viewSubscriptionPage);

        // verify
        verify(mockUsageAgreementService, mockIdentityService);

        /*
         * Information panel
         */
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + ViewSubscriptionPage.INFORMATION_HEADER_ID, ToggleHeader.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + ViewSubscriptionPage.INFORMATION_HEADER_ID + ":"
                + ViewSubscriptionPage.INFORMATION_BODY_ID, WebMarkupContainer.class);
        wicket.assertLabel(TemplatePage.CONTENT_ID + ":" + ViewSubscriptionPage.INFORMATION_HEADER_ID + ":"
                + ViewSubscriptionPage.INFORMATION_BODY_ID + ":" + ViewSubscriptionPage.NAME_ID, applicationFriendlyName);
        wicket.assertLabel(TemplatePage.CONTENT_ID + ":" + ViewSubscriptionPage.INFORMATION_HEADER_ID + ":"
                + ViewSubscriptionPage.INFORMATION_BODY_ID + ":" + ViewSubscriptionPage.OWNER_NAME_ID, applicationOwnerName);
        wicket.assertLabel(TemplatePage.CONTENT_ID + ":" + ViewSubscriptionPage.INFORMATION_HEADER_ID + ":"
                + ViewSubscriptionPage.INFORMATION_BODY_ID + ":" + ViewSubscriptionPage.DESCRIPTION_ID, applicationDescription);

        /*
         * Usage agreement panel
         */
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + ViewSubscriptionPage.USAGE_AGREEMENT_HEADER_ID, ToggleHeader.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + ViewSubscriptionPage.USAGE_AGREEMENT_HEADER_ID + ":"
                + ViewSubscriptionPage.USAGE_AGREEMENT_BODY_ID, WebMarkupContainer.class);
        wicket.assertLabel(TemplatePage.CONTENT_ID + ":" + ViewSubscriptionPage.USAGE_AGREEMENT_HEADER_ID + ":"
                + ViewSubscriptionPage.USAGE_AGREEMENT_BODY_ID + ":" + ViewSubscriptionPage.USAGE_AGREEMENT_ID, usageAgreementText);

        /*
         * Identity panel
         */
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + ViewSubscriptionPage.IDENTITY_HEADER_ID, ToggleHeader.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + ViewSubscriptionPage.IDENTITY_HEADER_ID + ":"
                + ViewSubscriptionPage.IDENTITY_BODY_ID, WebMarkupContainer.class);
        wicket.assertListView(TemplatePage.CONTENT_ID + ":" + ViewSubscriptionPage.IDENTITY_HEADER_ID + ":"
                + ViewSubscriptionPage.IDENTITY_BODY_ID + ":" + ViewSubscriptionPage.IDENTITY_ATTRIBUTES, identityAttributes);
        wicket.assertLabel(TemplatePage.CONTENT_ID + ":" + ViewSubscriptionPage.IDENTITY_HEADER_ID + ":"
                + ViewSubscriptionPage.IDENTITY_BODY_ID + ":" + ViewSubscriptionPage.IDENTITY_ATTRIBUTES + ":0:"
                + ViewSubscriptionPage.IDENTITY_ATTRIBUTE_NAME, attribute1Name);
        wicket.assertLabel(TemplatePage.CONTENT_ID + ":" + ViewSubscriptionPage.IDENTITY_HEADER_ID + ":"
                + ViewSubscriptionPage.IDENTITY_BODY_ID + ":" + ViewSubscriptionPage.IDENTITY_ATTRIBUTES + ":1:"
                + ViewSubscriptionPage.IDENTITY_ATTRIBUTE_NAME, attribute2Name);

        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + ViewSubscriptionPage.BACK_FORM_ID + ":"
                + ViewSubscriptionPage.BACK_BUTTON_ID, Button.class);

    }
}
