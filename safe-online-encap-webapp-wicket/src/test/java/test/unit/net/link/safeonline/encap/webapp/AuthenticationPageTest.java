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
import net.link.safeonline.encap.webapp.AuthenticationPage;
import net.link.safeonline.helpdesk.HelpdeskManager;
import net.link.safeonline.model.encap.EncapDeviceService;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class AuthenticationPageTest {

    private EncapDeviceService   mockEncapDeviceService;

    private SamlAuthorityService mockSamlAuthorityService;

    private HelpdeskManager      mockHelpdeskManager;

    private WicketTester         wicket;

    private JndiTestUtils        jndiTestUtils;

    private static final String  TEST_USERID    = UUID.randomUUID().toString();

    private static final String  TEST_MOBILE    = "0523012295";

    private static final String  TEST_CHALLENGE = "0123456789";

    private static final String  TEST_OTP       = "000000";


    @Before
    public void setUp()
            throws Exception {

        this.jndiTestUtils = new JndiTestUtils();
        this.jndiTestUtils.setUp();

        this.mockEncapDeviceService = createMock(EncapDeviceService.class);
        this.mockSamlAuthorityService = createMock(SamlAuthorityService.class);
        this.mockHelpdeskManager = createMock(HelpdeskManager.class);

        WicketUtil.setUnitTesting(true);
        this.wicket = new WicketTester(new EncapTestApplication());
        this.wicket.processRequestCycle();
    }

    @After
    public void tearDown()
            throws Exception {

        this.jndiTestUtils.tearDown();
    }

    /**
     * Sets wicket up to begin authentication and injects the Encap device service, SAML authority service and HelpDesk service.
     * 
     * @return The {@link FormTester} for the authentication for on the authentication page.
     */
    private FormTester prepareAuthentication()
            throws Exception {

        // Load Authentication Page.
        this.wicket.startPage(AuthenticationPage.class);
        this.wicket.assertRenderedPage(AuthenticationPage.class);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID, Form.class);

        // Inject EJBs.
        EJBTestUtils.inject(this.wicket.getLastRenderedPage(), this.mockEncapDeviceService);
        EJBTestUtils.inject(this.wicket.getLastRenderedPage(), this.mockSamlAuthorityService);
        this.jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, this.mockHelpdeskManager);

        // Setup mocks.
        expect(this.mockSamlAuthorityService.getAuthnAssertionValidity()).andStubReturn(Integer.MAX_VALUE);
        expect(this.mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // Return Authentication Form.
        return this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID);
    }

    @Test
    public void testAuthenticate()
            throws Exception {

        // Setup.
        FormTester authenticationForm = prepareAuthentication();
        this.mockEncapDeviceService.checkMobile(TEST_MOBILE);
        expect(this.mockEncapDeviceService.requestOTP(TEST_MOBILE)).andStubReturn(TEST_CHALLENGE);
        expect(this.mockEncapDeviceService.authenticate(TEST_MOBILE, TEST_CHALLENGE, TEST_OTP)).andStubReturn(TEST_USERID);
        replay(this.mockEncapDeviceService, this.mockSamlAuthorityService, this.mockHelpdeskManager);

        // Request OTP for our mobile.
        authenticationForm.setValue(AuthenticationPage.MOBILE_FIELD_ID, TEST_MOBILE);
        authenticationForm.submit(AuthenticationPage.CHALLENGE_BUTTON_ID);

        // Check for errors.
        this.wicket.assertRenderedPage(AuthenticationPage.class);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID, Form.class);
        this.wicket.assertNoErrorMessage();

        // Specify our OTP and begin login.
        authenticationForm = this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID);
        authenticationForm.setValue(AuthenticationPage.OTP_FIELD_ID, TEST_OTP);
        authenticationForm.submit(AuthenticationPage.LOGIN_BUTTON_ID);

        // Check for errors.
        this.wicket.assertRenderedPage(AuthenticationPage.class);
        this.wicket.assertNoErrorMessage();
        verify(this.mockEncapDeviceService, this.mockSamlAuthorityService);
    }

    @Test
    public void testAuthenticateSubjectNotFound()
            throws Exception {

        // Setup.
        FormTester authenticationForm = prepareAuthentication();
        this.mockEncapDeviceService.checkMobile(TEST_MOBILE);
        expectLastCall().andThrow(new SubjectNotFoundException());
        replay(this.mockEncapDeviceService, this.mockSamlAuthorityService, this.mockHelpdeskManager);

        // Request OTP for our mobile.
        authenticationForm.setValue(AuthenticationPage.MOBILE_FIELD_ID, TEST_MOBILE);
        authenticationForm.submit(AuthenticationPage.CHALLENGE_BUTTON_ID);

        // Check for errors.
        this.wicket.assertRenderedPage(AuthenticationPage.class);
        this.wicket.assertErrorMessages(new String[] { "mobileNotRegistered" });
        verify(this.mockEncapDeviceService, this.mockHelpdeskManager);
    }

    @Test
    public void testAuthenticateDeviceDisabled()
            throws Exception {

        // Setup.
        FormTester authenticationForm = prepareAuthentication();
        this.mockEncapDeviceService.checkMobile(TEST_MOBILE);
        expectLastCall().andThrow(new DeviceDisabledException());
        replay(this.mockEncapDeviceService, this.mockSamlAuthorityService, this.mockHelpdeskManager);

        // Request OTP for our mobile.
        authenticationForm.setValue(AuthenticationPage.MOBILE_FIELD_ID, TEST_MOBILE);
        authenticationForm.submit(AuthenticationPage.CHALLENGE_BUTTON_ID);

        // Check for errors.
        this.wicket.assertRenderedPage(AuthenticationPage.class);
        this.wicket.assertErrorMessages(new String[] { "mobileDisabled" });
        verify(this.mockEncapDeviceService, this.mockHelpdeskManager);
    }

    @Test
    public void testAuthenticateFailed()
            throws Exception {

        // Setup.
        FormTester authenticationForm = prepareAuthentication();
        this.mockEncapDeviceService.checkMobile(TEST_MOBILE);
        expect(this.mockEncapDeviceService.requestOTP(TEST_MOBILE)).andStubReturn(TEST_CHALLENGE);
        expect(this.mockEncapDeviceService.authenticate(TEST_MOBILE, TEST_CHALLENGE, TEST_OTP)).andStubReturn(null);
        replay(this.mockEncapDeviceService, this.mockSamlAuthorityService, this.mockHelpdeskManager);

        // Request OTP for our mobile.
        authenticationForm.setValue(AuthenticationPage.MOBILE_FIELD_ID, TEST_MOBILE);
        authenticationForm.submit(AuthenticationPage.CHALLENGE_BUTTON_ID);

        // Check for errors.
        this.wicket.assertRenderedPage(AuthenticationPage.class);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID, Form.class);
        this.wicket.assertNoErrorMessage();

        // Specify our OTP and begin login.
        authenticationForm = this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID);
        authenticationForm.setValue(AuthenticationPage.OTP_FIELD_ID, TEST_OTP);
        authenticationForm.submit(AuthenticationPage.LOGIN_BUTTON_ID);

        // Check for errors.
        this.wicket.assertRenderedPage(AuthenticationPage.class);
        this.wicket.assertErrorMessages(new String[] { "authenticationFailedMsg" });
        verify(this.mockEncapDeviceService, this.mockHelpdeskManager);
    }
}
