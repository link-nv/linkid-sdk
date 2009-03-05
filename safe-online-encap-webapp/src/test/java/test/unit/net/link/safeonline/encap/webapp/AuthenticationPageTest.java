package test.unit.net.link.safeonline.encap.webapp;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.UUID;

import net.link.safeonline.authentication.exception.DeviceAuthenticationException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.AuthenticationContext;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.encap.webapp.AuthenticationPage;
import net.link.safeonline.encap.webapp.EncapApplication;
import net.link.safeonline.encap.webapp.AuthenticationPage.Goal;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.helpdesk.HelpdeskManager;
import net.link.safeonline.model.encap.EncapDeviceService;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.test.UrlPageSource;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class AuthenticationPageTest {

    private JndiTestUtils             jndiTestUtils;
    private SamlAuthorityService      mockSamlAuthorityService;
    private HelpdeskManager           mockHelpdeskManager;
    private WicketTester              wicket;
    private EncapDeviceService        mockEncapDeviceService;
    private NodeAuthenticationService mockNodeAuthenticationService;

    private static final String       TEST_APPLICATION = "test-application";
    private static final String       TEST_USERID      = UUID.randomUUID().toString();
    private static final String       TEST_MOBILE      = "0523012295";
    private static final String       TEST_OTP         = "000000";


    @Before
    public void setUp()
            throws Exception {

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();

        mockNodeAuthenticationService = createMock(NodeAuthenticationService.class);
        jndiTestUtils.bindComponent(NodeAuthenticationService.JNDI_BINDING, mockNodeAuthenticationService);

        mockEncapDeviceService = createMock(EncapDeviceService.class);
        jndiTestUtils.bindComponent(EncapDeviceService.JNDI_BINDING, mockEncapDeviceService);

        mockSamlAuthorityService = createMock(SamlAuthorityService.class);
        mockHelpdeskManager = createMock(HelpdeskManager.class);

        wicket = new WicketTester(new EncapTestApplication());
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
        EJBTestUtils.inject(wicket.getLastRenderedPage(), mockSamlAuthorityService);
        EJBTestUtils.inject(wicket.getLastRenderedPage(), mockNodeAuthenticationService);
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
        mockEncapDeviceService.requestOTP(TEST_MOBILE);
        expect(mockEncapDeviceService.isChallenged()).andReturn(true);
        expect(mockEncapDeviceService.authenticate(TEST_OTP)).andStubReturn(TEST_USERID);
        expect(mockNodeAuthenticationService.getLocalNode()).andReturn(new NodeEntity("Test", null, null, 0, 0, null));
        replay(mockEncapDeviceService, mockSamlAuthorityService, mockHelpdeskManager, mockNodeAuthenticationService);

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
        assertNotNull("No authenticated user on the session.", //
                AuthenticationContext.getAuthenticationContext(wicket.getServletSession()).getUserId());
    }

    @Test
    public void testAuthenticateSubjectNotFound()
            throws Exception {

        // Setup.
        FormTester form = prepareAuthentication(Goal.AUTHENTICATE);

        // Describe Expected Scenario.
        mockEncapDeviceService.requestOTP(TEST_MOBILE);
        expect(mockEncapDeviceService.isChallenged()).andReturn(true);
        expect(mockEncapDeviceService.isChallenged()).andReturn(true);
        mockEncapDeviceService.authenticate(TEST_OTP);
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
        assertNull("There was an authenticated user on the session.", //
                AuthenticationContext.getAuthenticationContext(wicket.getServletSession()).getUserId());
    }

    @Test
    public void testAuthenticateDeviceDisabled()
            throws Exception {

        // Setup.
        FormTester form = prepareAuthentication(Goal.AUTHENTICATE);

        // Describe Expected Scenario.
        mockEncapDeviceService.requestOTP(TEST_MOBILE);
        expect(mockEncapDeviceService.isChallenged()).andReturn(true);
        expect(mockEncapDeviceService.isChallenged()).andReturn(true);
        mockEncapDeviceService.authenticate(TEST_OTP);
        expectLastCall().andThrow(new DeviceDisabledException());
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
        wicket.assertErrorMessages(new String[] { "errorDeviceDisabled" });
        verify(mockEncapDeviceService, mockSamlAuthorityService, mockHelpdeskManager);
        assertNull("There was an authenticated user on the session.", //
                AuthenticationContext.getAuthenticationContext(wicket.getServletSession()).getUserId());
    }

    @Test
    public void testAuthenticateFailed()
            throws Exception {

        // Setup.
        FormTester form = prepareAuthentication(Goal.AUTHENTICATE);

        // Describe Expected Scenario.
        mockEncapDeviceService.requestOTP(TEST_MOBILE);
        expect(mockEncapDeviceService.isChallenged()).andReturn(true);
        expect(mockEncapDeviceService.isChallenged()).andReturn(true);
        expect(mockEncapDeviceService.authenticate(TEST_OTP)).andStubReturn(null);
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
        assertNull("There was an authenticated user on the session.", //
                AuthenticationContext.getAuthenticationContext(wicket.getServletSession()).getUserId());
    }

    @Test
    public void testEnable()
            throws Exception {

        // Setup.
        FormTester form = prepareAuthentication(Goal.ENABLE_DEVICE);

        // Describe Expected Scenario.
        mockEncapDeviceService.requestOTP(TEST_MOBILE);
        expect(mockEncapDeviceService.isChallenged()).andReturn(true);
        mockEncapDeviceService.enable(TEST_USERID, TEST_OTP);
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
        mockEncapDeviceService.requestOTP(TEST_MOBILE);
        expect(mockEncapDeviceService.isChallenged()).andReturn(true);
        expect(mockEncapDeviceService.isChallenged()).andReturn(true);
        mockEncapDeviceService.enable(TEST_USERID, TEST_OTP);
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
        assertNull("There was an authenticated user on the session.", //
                AuthenticationContext.getAuthenticationContext(wicket.getServletSession()).getUserId());
    }

    @Test
    public void testEnableAuthenticateFailed()
            throws Exception {

        // Setup.
        FormTester form = prepareAuthentication(Goal.ENABLE_DEVICE);

        // Describe Expected Scenario.
        mockEncapDeviceService.requestOTP(TEST_MOBILE);
        expect(mockEncapDeviceService.isChallenged()).andReturn(true);
        expect(mockEncapDeviceService.isChallenged()).andReturn(true);
        mockEncapDeviceService.enable(TEST_USERID, TEST_OTP);
        expectLastCall().andThrow(new DeviceAuthenticationException());
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
        assertNull("There was an authenticated user on the session.", //
                AuthenticationContext.getAuthenticationContext(wicket.getServletSession()).getUserId());
    }
}
