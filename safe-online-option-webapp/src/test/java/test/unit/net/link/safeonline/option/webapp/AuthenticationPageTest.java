package test.unit.net.link.safeonline.option.webapp;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.UUID;

import javax.servlet.http.Cookie;

import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.AuthenticationContext;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.helpdesk.HelpdeskManager;
import net.link.safeonline.model.option.OptionDeviceService;
import net.link.safeonline.option.webapp.AuthenticationPage;
import net.link.safeonline.option.webapp.OptionApplication;
import net.link.safeonline.option.webapp.OptionDevice;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.test.UrlPageSource;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.protocol.http.MockHttpServletRequest;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class AuthenticationPageTest {

    private JndiTestUtils             jndiTestUtils;
    private OptionDeviceService       mockOptionDeviceService;
    private SamlAuthorityService      mockSamlAuthorityService;
    private HelpdeskManager           mockHelpdeskManager;
    private WicketTester              wicket;
    private NodeAuthenticationService mockNodeAuthenticationService;

    private static final String       TEST_APPLICATION = "test-application";
    private static final String       TEST_USERID      = UUID.randomUUID().toString();
    private static final String       TEST_IMEI        = "012345678912345";
    private static final String       TEST_PIN         = "0000";


    @Before
    public void setUp()
            throws Exception {

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();

        mockNodeAuthenticationService = createMock(NodeAuthenticationService.class);
        mockOptionDeviceService = createMock(OptionDeviceService.class);
        mockSamlAuthorityService = createMock(SamlAuthorityService.class);
        mockHelpdeskManager = createMock(HelpdeskManager.class);

        WicketUtil.setUnitTesting(true);
        wicket = new WicketTester(new OptionTestApplication());
        wicket.processRequestCycle();
    }

    @After
    public void tearDown()
            throws Exception {

        jndiTestUtils.tearDown();
    }

    /**
     * Sets wicket up to begin authentication and injects the Option device service, SAML authority service and HelpDesk service.
     * 
     * @return The {@link FormTester} for the authentication for on the authentication page.
     */
    private FormTester prepareAuthentication()
            throws Exception {

        // Initialize contexts.
        AuthenticationContext authenticationContext = AuthenticationContext.getAuthenticationContext(wicket.getServletSession());
        authenticationContext.setApplication(TEST_APPLICATION);
        authenticationContext.setApplicationFriendlyName(TEST_APPLICATION);
        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(wicket.getServletSession());
        protocolContext.setSubject(TEST_USERID);
        protocolContext.setAttributeId(TEST_IMEI);

        // Inject EJBs.
        EJBTestUtils.inject(wicket.getLastRenderedPage(), mockOptionDeviceService);
        EJBTestUtils.inject(wicket.getLastRenderedPage(), mockSamlAuthorityService);
        EJBTestUtils.inject(wicket.getLastRenderedPage(), mockNodeAuthenticationService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);

        // Setup mocks.
        expect(mockSamlAuthorityService.getAuthnAssertionValidity()).andStubReturn(Integer.MAX_VALUE);
        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);
        replay(mockSamlAuthorityService, mockHelpdeskManager);

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
        OptionDevice._update(TEST_PIN, TEST_IMEI);
        Cookie[] cookies = wicket.getWicketRequest().getCookies();

        wicket.startPage(new UrlPageSource(OptionApplication.AUTHENTICATION_MOUNTPOINT));
        FormTester form = prepareAuthentication();
        // TODO: See SOS-393
        ((MockHttpServletRequest) wicket.getWicketRequest().getHttpServletRequest()).setCookies(cookies);

        // Describe Expected Scenario.
        expect(mockOptionDeviceService.authenticate(TEST_IMEI)).andStubReturn(TEST_USERID);
        expect(mockNodeAuthenticationService.getLocalNode()).andReturn(new NodeEntity("Test", null, null, 0, 0, null));
        replay(mockOptionDeviceService, mockNodeAuthenticationService);

        // Pass the PIN for our device.
        form.setValue(AuthenticationPage.PIN_FIELD_ID, TEST_PIN);
        form.submit(AuthenticationPage.LOGIN_BUTTON_ID);

        // Check for errors.
        wicket.assertRenderedPage(AuthenticationPage.class);
        wicket.assertNoErrorMessage();
        verify(mockOptionDeviceService, mockSamlAuthorityService, mockHelpdeskManager);
        assertNotNull("No authenticated user on the session.", //
                AuthenticationContext.getAuthenticationContext(wicket.getServletSession()).getUserId());
    }

    @Test
    public void testAuthenticateSubjectNotFound()
            throws Exception {

        // Setup.
        OptionDevice._update(TEST_PIN, TEST_IMEI);
        Cookie[] cookies = wicket.getWicketRequest().getCookies();

        wicket.startPage(new UrlPageSource(OptionApplication.AUTHENTICATION_MOUNTPOINT));
        FormTester form = prepareAuthentication();
        // TODO: See SOS-393
        ((MockHttpServletRequest) wicket.getWicketRequest().getHttpServletRequest()).setCookies(cookies);

        // Describe Expected Scenario.
        mockOptionDeviceService.authenticate(TEST_IMEI);
        expectLastCall().andThrow(new SubjectNotFoundException());
        replay(mockOptionDeviceService);

        // Pass the PIN for our device.
        form.setValue(AuthenticationPage.PIN_FIELD_ID, TEST_PIN);
        form.submit(AuthenticationPage.LOGIN_BUTTON_ID);

        // Check for errors.
        wicket.assertRenderedPage(AuthenticationPage.class);
        wicket.assertErrorMessages(new String[] { "errorSubjectNotFound" });
        verify(mockOptionDeviceService, mockSamlAuthorityService, mockHelpdeskManager);
        assertNull("There was an authenticated user on the session.", //
                AuthenticationContext.getAuthenticationContext(wicket.getServletSession()).getUserId());
    }

    @Test
    public void testAuthenticateDeviceDisabled()
            throws Exception {

        // Setup.
        OptionDevice._update(TEST_PIN, TEST_IMEI);
        Cookie[] cookies = wicket.getWicketRequest().getCookies();

        wicket.startPage(new UrlPageSource(OptionApplication.AUTHENTICATION_MOUNTPOINT));
        FormTester form = prepareAuthentication();
        // TODO: See SOS-393
        ((MockHttpServletRequest) wicket.getWicketRequest().getHttpServletRequest()).setCookies(cookies);

        // Describe Expected Scenario.
        mockOptionDeviceService.authenticate(TEST_IMEI);
        expectLastCall().andThrow(new DeviceDisabledException());
        replay(mockOptionDeviceService);

        // Pass the PIN for our device.
        form.setValue(AuthenticationPage.PIN_FIELD_ID, TEST_PIN);
        form.submit(AuthenticationPage.LOGIN_BUTTON_ID);

        // Check for errors.
        wicket.assertRenderedPage(AuthenticationPage.class);
        wicket.assertErrorMessages(new String[] { "errorDeviceDisabled" });
        verify(mockOptionDeviceService, mockSamlAuthorityService, mockHelpdeskManager);
        assertNull("There was an authenticated user on the session.", //
                AuthenticationContext.getAuthenticationContext(wicket.getServletSession()).getUserId());
    }

    @Test
    public void testAuthenticateFailed()
            throws Exception {

        // Setup.
        OptionDevice._update(TEST_PIN, TEST_IMEI);
        Cookie[] cookies = wicket.getWicketRequest().getCookies();

        wicket.startPage(new UrlPageSource(OptionApplication.AUTHENTICATION_MOUNTPOINT));
        FormTester form = prepareAuthentication();
        // TODO: See SOS-393
        ((MockHttpServletRequest) wicket.getWicketRequest().getHttpServletRequest()).setCookies(cookies);

        // Describe Expected Scenario.
        expect(mockOptionDeviceService.authenticate(TEST_IMEI)).andStubReturn(null);
        replay(mockOptionDeviceService);

        // Pass the PIN for our device.
        form.setValue(AuthenticationPage.PIN_FIELD_ID, TEST_PIN);
        form.submit(AuthenticationPage.LOGIN_BUTTON_ID);

        // Check for errors.
        wicket.assertRenderedPage(AuthenticationPage.class);
        wicket.assertErrorMessages(new String[] { "authenticationFailedMsg" });
        verify(mockOptionDeviceService, mockSamlAuthorityService, mockHelpdeskManager);
        assertNull("There was an authenticated user on the session.", //
                AuthenticationContext.getAuthenticationContext(wicket.getServletSession()).getUserId());
    }
}
