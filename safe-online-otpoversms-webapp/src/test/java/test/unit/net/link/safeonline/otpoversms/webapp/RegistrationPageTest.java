package test.unit.net.link.safeonline.otpoversms.webapp;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.net.ConnectException;
import java.util.UUID;

import junit.framework.TestCase;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.wicket.tools.WicketUtil;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.device.sdk.saml2.DeviceOperationType;
import net.link.safeonline.helpdesk.HelpdeskManager;
import net.link.safeonline.model.otpoversms.OtpOverSmsDeviceService;
import net.link.safeonline.otpoversms.webapp.RegistrationPage;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.webapp.template.TemplatePage;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class RegistrationPageTest extends TestCase {

    private OtpOverSmsDeviceService mockOtpOverSmsDeviceService;

    private SamlAuthorityService    mockSamlAuthorityService;

    private HelpdeskManager         mockHelpdeskManager;

    private SecurityAuditLogger     mockSecurityAuditLogger;

    private WicketTester            wicket;

    private JndiTestUtils           jndiTestUtils;


    @Override
    @Before
    public void setUp()
            throws Exception {

        super.setUp();

        WicketUtil.setUnitTesting(true);

        this.jndiTestUtils = new JndiTestUtils();
        this.jndiTestUtils.setUp();

        this.mockOtpOverSmsDeviceService = createMock(OtpOverSmsDeviceService.class);
        this.mockSamlAuthorityService = createMock(SamlAuthorityService.class);
        this.mockHelpdeskManager = createMock(HelpdeskManager.class);
        this.mockSecurityAuditLogger = createMock(SecurityAuditLogger.class);

        this.wicket = new WicketTester(new OtpOverSmsTestApplication());

    }

    @Override
    @After
    public void tearDown()
            throws Exception {

        this.jndiTestUtils.tearDown();
    }

    @Test
    public void testRegister()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String mobile = "+32 494 575 697";
        String convertedMobile = net.link.safeonline.custom.converter.PhoneNumberConverter.convertNumber(mobile);
        String otp = UUID.randomUUID().toString();
        String pin = "0000";

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(this.wicket.getServletSession());
        protocolContext.setDeviceOperation(DeviceOperationType.NEW_ACCOUNT_REGISTER);
        protocolContext.setSubject(userId);

        // verify
        RegistrationPage registrationPage = (RegistrationPage) this.wicket.startPage(RegistrationPage.class);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + RegistrationPage.REQUEST_OTP_FORM_ID, Form.class);
        this.wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + RegistrationPage.VERIFY_OTP_FORM_ID);

        // setup
        EJBTestUtils.inject(registrationPage, this.mockOtpOverSmsDeviceService);
        EJBTestUtils.inject(registrationPage, this.mockSamlAuthorityService);

        // stubs
        this.mockOtpOverSmsDeviceService.requestOtp(this.wicket.getServletSession(), convertedMobile);

        // prepare
        replay(this.mockOtpOverSmsDeviceService);

        // operate
        FormTester requestOtpForm = this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + RegistrationPage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(RegistrationPage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(RegistrationPage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(this.mockOtpOverSmsDeviceService);

        // verify
        registrationPage = (RegistrationPage) this.wicket.getLastRenderedPage();
        this.wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + RegistrationPage.REQUEST_OTP_FORM_ID);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + RegistrationPage.VERIFY_OTP_FORM_ID, Form.class);

        // setup
        EasyMock.reset(this.mockOtpOverSmsDeviceService);

        // stubs
        expect(this.mockOtpOverSmsDeviceService.verifyOtp(this.wicket.getServletSession(), otp)).andStubReturn(true);
        this.mockOtpOverSmsDeviceService.register(userId, convertedMobile, pin);
        expect(this.mockSamlAuthorityService.getAuthnAssertionValidity()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(this.mockOtpOverSmsDeviceService, this.mockSamlAuthorityService);

        // operate
        FormTester verifyOtpForm = this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + RegistrationPage.VERIFY_OTP_FORM_ID);
        verifyOtpForm.setValue(RegistrationPage.OTP_FIELD_ID, otp);
        verifyOtpForm.setValue(RegistrationPage.PIN1_FIELD_ID, pin);
        verifyOtpForm.setValue(RegistrationPage.PIN2_FIELD_ID, pin);
        verifyOtpForm.submit(RegistrationPage.SAVE_BUTTON_ID);

        // verify
        verify(this.mockOtpOverSmsDeviceService, this.mockSamlAuthorityService);
    }

    @Test
    public void testRegisterConnectException()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String mobile = "+32 494 575 697";
        String convertedMobile = net.link.safeonline.custom.converter.PhoneNumberConverter.convertNumber(mobile);

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(this.wicket.getServletSession());
        protocolContext.setDeviceOperation(DeviceOperationType.NEW_ACCOUNT_REGISTER);
        protocolContext.setSubject(userId);

        // verify
        RegistrationPage registrationPage = (RegistrationPage) this.wicket.startPage(RegistrationPage.class);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + RegistrationPage.REQUEST_OTP_FORM_ID, Form.class);
        this.wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + RegistrationPage.VERIFY_OTP_FORM_ID);

        // setup
        EJBTestUtils.inject(registrationPage, this.mockOtpOverSmsDeviceService);
        this.jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, this.mockHelpdeskManager);
        this.jndiTestUtils.bindComponent(SecurityAuditLogger.JNDI_BINDING, this.mockSecurityAuditLogger);

        // stubs
        this.mockOtpOverSmsDeviceService.requestOtp(this.wicket.getServletSession(), convertedMobile);
        org.easymock.EasyMock.expectLastCall().andThrow(new ConnectException());
        expect(this.mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(this.mockOtpOverSmsDeviceService, this.mockHelpdeskManager, this.mockSecurityAuditLogger);

        // operate
        FormTester requestOtpForm = this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + RegistrationPage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(RegistrationPage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(RegistrationPage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(this.mockOtpOverSmsDeviceService, this.mockHelpdeskManager, this.mockSecurityAuditLogger);

        this.wicket.assertRenderedPage(RegistrationPage.class);
        this.wicket.assertErrorMessages(new String[] { "errorServiceConnection" });
    }

    @Test
    public void testRegisterVerifyFailed()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String mobile = "+32 494 575 697";
        String convertedMobile = net.link.safeonline.custom.converter.PhoneNumberConverter.convertNumber(mobile);
        String otp = UUID.randomUUID().toString();
        String pin = "0000";

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(this.wicket.getServletSession());
        protocolContext.setDeviceOperation(DeviceOperationType.NEW_ACCOUNT_REGISTER);
        protocolContext.setSubject(userId);

        // verify
        RegistrationPage registrationPage = (RegistrationPage) this.wicket.startPage(RegistrationPage.class);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + RegistrationPage.REQUEST_OTP_FORM_ID, Form.class);
        this.wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + RegistrationPage.VERIFY_OTP_FORM_ID);

        // setup
        EJBTestUtils.inject(registrationPage, this.mockOtpOverSmsDeviceService);
        EJBTestUtils.inject(registrationPage, this.mockSamlAuthorityService);

        // stubs
        this.mockOtpOverSmsDeviceService.requestOtp(this.wicket.getServletSession(), convertedMobile);

        // prepare
        replay(this.mockOtpOverSmsDeviceService);

        // operate
        FormTester requestOtpForm = this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + RegistrationPage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(RegistrationPage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(RegistrationPage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(this.mockOtpOverSmsDeviceService);

        // verify
        registrationPage = (RegistrationPage) this.wicket.getLastRenderedPage();
        this.wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + RegistrationPage.REQUEST_OTP_FORM_ID);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + RegistrationPage.VERIFY_OTP_FORM_ID, Form.class);

        // setup
        EasyMock.reset(this.mockOtpOverSmsDeviceService);
        this.jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, this.mockHelpdeskManager);

        // stubs
        expect(this.mockOtpOverSmsDeviceService.verifyOtp(this.wicket.getServletSession(), otp)).andStubReturn(false);
        expect(this.mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(this.mockOtpOverSmsDeviceService, this.mockHelpdeskManager);

        // operate
        FormTester verifyOtpForm = this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + RegistrationPage.VERIFY_OTP_FORM_ID);
        verifyOtpForm.setValue(RegistrationPage.OTP_FIELD_ID, otp);
        verifyOtpForm.setValue(RegistrationPage.PIN1_FIELD_ID, pin);
        verifyOtpForm.setValue(RegistrationPage.PIN2_FIELD_ID, pin);
        verifyOtpForm.submit(RegistrationPage.SAVE_BUTTON_ID);

        // verify
        verify(this.mockOtpOverSmsDeviceService, this.mockHelpdeskManager);

        this.wicket.assertRenderedPage(RegistrationPage.class);
        this.wicket.assertErrorMessages(new String[] { "authenticationFailedMsg" });
    }

    @Test
    public void testRegisterPinIncorrect()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String mobile = "+32 494 575 697";
        String convertedMobile = net.link.safeonline.custom.converter.PhoneNumberConverter.convertNumber(mobile);
        String otp = UUID.randomUUID().toString();
        String pin = "0000";

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(this.wicket.getServletSession());
        protocolContext.setDeviceOperation(DeviceOperationType.NEW_ACCOUNT_REGISTER);
        protocolContext.setSubject(userId);

        // verify
        RegistrationPage registrationPage = (RegistrationPage) this.wicket.startPage(RegistrationPage.class);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + RegistrationPage.REQUEST_OTP_FORM_ID, Form.class);
        this.wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + RegistrationPage.VERIFY_OTP_FORM_ID);

        // setup
        EJBTestUtils.inject(registrationPage, this.mockOtpOverSmsDeviceService);
        EJBTestUtils.inject(registrationPage, this.mockSamlAuthorityService);

        // stubs
        this.mockOtpOverSmsDeviceService.requestOtp(this.wicket.getServletSession(), convertedMobile);

        // prepare
        replay(this.mockOtpOverSmsDeviceService);

        // operate
        FormTester requestOtpForm = this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + RegistrationPage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(RegistrationPage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(RegistrationPage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(this.mockOtpOverSmsDeviceService);

        // verify
        registrationPage = (RegistrationPage) this.wicket.getLastRenderedPage();
        this.wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + RegistrationPage.REQUEST_OTP_FORM_ID);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + RegistrationPage.VERIFY_OTP_FORM_ID, Form.class);

        // setup
        EasyMock.reset(this.mockOtpOverSmsDeviceService);
        this.jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, this.mockHelpdeskManager);
        this.jndiTestUtils.bindComponent(SecurityAuditLogger.JNDI_BINDING, this.mockSecurityAuditLogger);

        // stubs
        expect(this.mockOtpOverSmsDeviceService.verifyOtp(this.wicket.getServletSession(), otp)).andStubReturn(true);
        this.mockOtpOverSmsDeviceService.register(userId, convertedMobile, pin);
        org.easymock.EasyMock.expectLastCall().andThrow(new PermissionDeniedException(""));
        expect(this.mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(this.mockOtpOverSmsDeviceService, this.mockHelpdeskManager, this.mockSecurityAuditLogger);

        // operate
        FormTester verifyOtpForm = this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + RegistrationPage.VERIFY_OTP_FORM_ID);
        verifyOtpForm.setValue(RegistrationPage.OTP_FIELD_ID, otp);
        verifyOtpForm.setValue(RegistrationPage.PIN1_FIELD_ID, pin);
        verifyOtpForm.setValue(RegistrationPage.PIN2_FIELD_ID, pin);
        verifyOtpForm.submit(RegistrationPage.SAVE_BUTTON_ID);

        // verify
        verify(this.mockOtpOverSmsDeviceService, this.mockHelpdeskManager, this.mockSecurityAuditLogger);

        this.wicket.assertRenderedPage(RegistrationPage.class);
        this.wicket.assertErrorMessages(new String[] { "errorPinNotCorrect" });
    }

}
