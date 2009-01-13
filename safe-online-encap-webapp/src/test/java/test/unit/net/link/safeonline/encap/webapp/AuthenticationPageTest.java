package test.unit.net.link.safeonline.encap.webapp;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.UUID;

import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.AuthenticationContext;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.encap.webapp.AuthenticationPage;
import net.link.safeonline.encap.webapp.EncapApplication;
import net.link.safeonline.encap.webapp.AuthenticationPage.Goal;
import net.link.safeonline.helpdesk.HelpdeskManager;
import net.link.safeonline.model.encap.EncapDeviceService;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.test.UrlPageSource;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class AuthenticationPageTest {

    private JndiTestUtils        jndiTestUtils;
    private EncapDeviceService   mockEncapDeviceService;
    private SamlAuthorityService mockSamlAuthorityService;
    private HelpdeskManager      mockHelpdeskManager;
    private WicketTester         wicket;

    private static final String  TEST_APPLICATION = "test-application";
    private static final String  TEST_USERID      = UUID.randomUUID().toString();
    private static final String  TEST_MOBILE      = "0523012295";
    private static final String  TEST_CHALLENGE   = "0123456789";
    private static final String  TEST_OTP         = "000000";


    @Before
    public void setUp()
            throws Exception {

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();

        mockEncapDeviceService = createMock(EncapDeviceService.class);
        mockSamlAuthorityService = createMock(SamlAuthorityService.class);
        mockHelpdeskManager = createMock(HelpdeskManager.class);

        WicketUtil.setUnitTesting(true);
        wicket = new WicketTester(new EncapTestApplication());
        wicket.processRequestCycle();
    }

    @After
    public void tearDown()
            throws Exception {

        jndiTestUtils.tearDown();
    }

    /**
     * Sets wicket up to begin authentication and injects the Encap device service, SAML authority service and HelpDesk service.
     * 
     * @return The {@link FormTester} for the authentication for on the authentication page.
     */
    private FormTester prepareAuthentication(Goal goal)
            throws Exception {

        // Initialize contexts.
        AuthenticationContext authenticationContext = AuthenticationContext.getAuthenticationContext(wicket.getServletSession());
        authenticationContext.setApplication(TEST_APPLICATION);
        authenticationContext.setApplicationFriendlyName(TEST_APPLICATION);
        authenticationContext.setUserId(TEST_USERID);
        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(wicket.getServletSession());
        protocolContext.setSubject(TEST_USERID);
        protocolContext.setAttribute(TEST_MOBILE);

        // Check whether our mount point sends us to the authentication page.
        switch (goal) {
            case AUTHENTICATE:
                wicket.startPage(new UrlPageSource(EncapApplication.AUTHENTICATION_MOUNTPOINT));
            break;

            case ENABLE_DEVICE:
                wicket.startPage(new UrlPageSource(EncapApplication.ENABLE_MOUNTPOINT));
            break;

            case REGISTER_DEVICE:
                throw new UnsupportedOperationException();
        }
        wicket.assertRenderedPage(AuthenticationPage.class);
        wicket.dumpPage();

        // Inject EJBs.
        EJBTestUtils.inject(wicket.getLastRenderedPage(), mockEncapDeviceService);
        EJBTestUtils.inject(wicket.getLastRenderedPage(), mockSamlAuthorityService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);

        // Setup mocks.
        expect(mockSamlAuthorityService.getAuthnAssertionValidity()).andStubReturn(Integer.MAX_VALUE);
        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // Return Authentication Form.
        return getAuthenticationForm(wicket);
    }

    public static FormTester getAuthenticationForm(WicketTester wicket) {

        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID, Form.class);
        return wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID);
    }

    @Test
    public void testAuthenticate()
            throws Exception {

        // Setup.
        FormTester form = prepareAuthentication(Goal.AUTHENTICATE);

        // Describe Expected Scenario.
        mockEncapDeviceService.checkMobile(TEST_MOBILE);
        expect(mockEncapDeviceService.requestOTP(TEST_MOBILE)).andStubReturn(TEST_CHALLENGE);
        expect(mockEncapDeviceService.authenticate(TEST_MOBILE, TEST_CHALLENGE, TEST_OTP)).andStubReturn(TEST_USERID);
        replay(mockEncapDeviceService, mockSamlAuthorityService, mockHelpdeskManager);

        // Request OTP for our mobile.
        form.setValue(AuthenticationPage.MOBILE_FIELD_ID, TEST_MOBILE);
        form.submit(AuthenticationPage.CHALLENGE_BUTTON_ID);

        // Check for errors.
        wicket.assertRenderedPage(AuthenticationPage.class);
        wicket.assertNoErrorMessage();

        // Specify our OTP and begin login.
        form = getAuthenticationForm(wicket);
        form.setValue(AuthenticationPage.OTP_FIELD_ID, TEST_OTP);
        form.submit(AuthenticationPage.LOGIN_BUTTON_ID);

        // Check for errors.
        wicket.assertRenderedPage(AuthenticationPage.class);
        wicket.assertNoErrorMessage();
        verify(mockEncapDeviceService, mockSamlAuthorityService, mockHelpdeskManager);
    }

    @Test
    public void testAuthenticateSubjectNotFound()
            throws Exception {

        // Setup.
        FormTester form = prepareAuthentication(Goal.AUTHENTICATE);

        // Describe Expected Scenario.
        mockEncapDeviceService.checkMobile(TEST_MOBILE);
        expectLastCall().andThrow(new SubjectNotFoundException());
        replay(mockEncapDeviceService, mockSamlAuthorityService, mockHelpdeskManager);

        // Request OTP for our mobile.
        form.setValue(AuthenticationPage.MOBILE_FIELD_ID, TEST_MOBILE);
        form.submit(AuthenticationPage.CHALLENGE_BUTTON_ID);

        // Check for errors.
        wicket.assertRenderedPage(AuthenticationPage.class);
        wicket.assertErrorMessages(new String[] { "mobileNotRegistered" });
        verify(mockEncapDeviceService, mockSamlAuthorityService, mockHelpdeskManager);
    }

    @Test
    public void testAuthenticateDeviceDisabled()
            throws Exception {

        // Setup.
        FormTester form = prepareAuthentication(Goal.AUTHENTICATE);

        // Describe Expected Scenario.
        mockEncapDeviceService.checkMobile(TEST_MOBILE);
        expectLastCall().andThrow(new DeviceDisabledException());
        replay(mockEncapDeviceService, mockSamlAuthorityService, mockHelpdeskManager);

        // Request OTP for our mobile.
        form.setValue(AuthenticationPage.MOBILE_FIELD_ID, TEST_MOBILE);
        form.submit(AuthenticationPage.CHALLENGE_BUTTON_ID);

        // Check for errors.
        wicket.assertRenderedPage(AuthenticationPage.class);
        wicket.assertErrorMessages(new String[] { "mobileDisabled" });
        verify(mockEncapDeviceService, mockSamlAuthorityService, mockHelpdeskManager);
    }

    @Test
    public void testAuthenticateFailed()
            throws Exception {

        // Setup.
        FormTester form = prepareAuthentication(Goal.AUTHENTICATE);

        // Describe Expected Scenario.
        mockEncapDeviceService.checkMobile(TEST_MOBILE);
        expect(mockEncapDeviceService.requestOTP(TEST_MOBILE)).andStubReturn(TEST_CHALLENGE);
        expect(mockEncapDeviceService.authenticate(TEST_MOBILE, TEST_CHALLENGE, TEST_OTP)).andStubReturn(null);
        replay(mockEncapDeviceService, mockSamlAuthorityService, mockHelpdeskManager);

        // Request OTP for our mobile.
        form.setValue(AuthenticationPage.MOBILE_FIELD_ID, TEST_MOBILE);
        form.submit(AuthenticationPage.CHALLENGE_BUTTON_ID);

        // Check for errors.
        wicket.assertRenderedPage(AuthenticationPage.class);
        wicket.assertNoErrorMessage();

        // Specify our OTP and begin login.
        form = getAuthenticationForm(wicket);
        form.setValue(AuthenticationPage.OTP_FIELD_ID, TEST_OTP);
        form.submit(AuthenticationPage.LOGIN_BUTTON_ID);

        // Check for errors.
        wicket.assertRenderedPage(AuthenticationPage.class);
        wicket.assertErrorMessages(new String[] { "authenticationFailedMsg" });
        verify(mockEncapDeviceService, mockSamlAuthorityService, mockHelpdeskManager);
    }

    @Test
    public void testEnable()
            throws Exception {

        // Setup.
        FormTester form = prepareAuthentication(Goal.ENABLE_DEVICE);

        // Describe Expected Scenario.
        expect(mockEncapDeviceService.requestOTP(TEST_MOBILE)).andStubReturn(TEST_CHALLENGE);
        expect(mockEncapDeviceService.authenticateEncap(TEST_CHALLENGE, TEST_OTP)).andReturn(true);
        mockEncapDeviceService.enable(TEST_USERID, TEST_MOBILE);
        replay(mockEncapDeviceService, mockSamlAuthorityService, mockHelpdeskManager);

        // Request OTP for our mobile.
        form.setValue(AuthenticationPage.MOBILE_FIELD_ID, TEST_MOBILE);
        form.submit(AuthenticationPage.CHALLENGE_BUTTON_ID);

        // Check for errors.
        wicket.assertRenderedPage(AuthenticationPage.class);
        wicket.assertNoErrorMessage();

        // Specify our OTP and begin login.
        form = getAuthenticationForm(wicket);
        form.setValue(AuthenticationPage.OTP_FIELD_ID, TEST_OTP);
        form.submit(AuthenticationPage.LOGIN_BUTTON_ID);

        // Check for errors.
        wicket.assertRenderedPage(AuthenticationPage.class);
        wicket.assertNoErrorMessage();
        verify(mockEncapDeviceService, mockSamlAuthorityService, mockHelpdeskManager);
    }

    @Test
    public void testEnableSubjectNotFound()
            throws Exception {

        // Setup.
        FormTester form = prepareAuthentication(Goal.ENABLE_DEVICE);

        // Describe Expected Scenario.
        expect(mockEncapDeviceService.requestOTP(TEST_MOBILE)).andStubReturn(TEST_CHALLENGE);
        expect(mockEncapDeviceService.authenticateEncap(TEST_CHALLENGE, TEST_OTP)).andReturn(true);
        mockEncapDeviceService.enable(TEST_USERID, TEST_MOBILE);
        expectLastCall().andThrow(new SubjectNotFoundException());
        replay(mockEncapDeviceService, mockSamlAuthorityService, mockHelpdeskManager);

        // Request OTP for our mobile.
        form.setValue(AuthenticationPage.MOBILE_FIELD_ID, TEST_MOBILE);
        form.submit(AuthenticationPage.CHALLENGE_BUTTON_ID);

        // Check for errors.
        wicket.assertRenderedPage(AuthenticationPage.class);
        wicket.assertNoErrorMessage();

        // Specify our OTP and begin login.
        form = getAuthenticationForm(wicket);
        form.setValue(AuthenticationPage.OTP_FIELD_ID, TEST_OTP);
        form.submit(AuthenticationPage.LOGIN_BUTTON_ID);

        // Check for errors.
        wicket.assertRenderedPage(AuthenticationPage.class);
        wicket.assertErrorMessages(new String[] { "errorSubjectNotFound" });
        verify(mockEncapDeviceService, mockSamlAuthorityService, mockHelpdeskManager);
    }

    @Test
    public void testEnableAuthenticateFailed()
            throws Exception {

        // Setup.
        FormTester form = prepareAuthentication(Goal.ENABLE_DEVICE);

        // Describe Expected Scenario.
        expect(mockEncapDeviceService.requestOTP(TEST_MOBILE)).andStubReturn(TEST_CHALLENGE);
        expect(mockEncapDeviceService.authenticateEncap(TEST_CHALLENGE, TEST_OTP)).andReturn(false);
        replay(mockEncapDeviceService, mockSamlAuthorityService, mockHelpdeskManager);

        // Request OTP for our mobile.
        form.setValue(AuthenticationPage.MOBILE_FIELD_ID, TEST_MOBILE);
        form.submit(AuthenticationPage.CHALLENGE_BUTTON_ID);

        // Check for errors.
        wicket.assertRenderedPage(AuthenticationPage.class);
        wicket.assertNoErrorMessage();

        // Specify our OTP and begin login.
        form = getAuthenticationForm(wicket);
        form.setValue(AuthenticationPage.OTP_FIELD_ID, TEST_OTP);
        form.submit(AuthenticationPage.LOGIN_BUTTON_ID);

        // Check for errors.
        wicket.assertRenderedPage(AuthenticationPage.class);
        wicket.assertErrorMessages(new String[] { "authenticationFailedMsg" });
        verify(mockEncapDeviceService, mockSamlAuthorityService, mockHelpdeskManager);
    }
}
