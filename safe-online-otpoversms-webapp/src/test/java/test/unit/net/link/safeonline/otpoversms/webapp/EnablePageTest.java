package test.unit.net.link.safeonline.otpoversms.webapp;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.net.ConnectException;
import java.util.UUID;

import junit.framework.TestCase;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.wicket.tools.WicketUtil;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.helpdesk.HelpdeskManager;
import net.link.safeonline.model.otpoversms.OtpOverSmsDeviceService;
import net.link.safeonline.otpoversms.webapp.EnablePage;
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


public class EnablePageTest extends TestCase {

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

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();

        mockOtpOverSmsDeviceService = createMock(OtpOverSmsDeviceService.class);
        mockSamlAuthorityService = createMock(SamlAuthorityService.class);
        mockHelpdeskManager = createMock(HelpdeskManager.class);
        mockSecurityAuditLogger = createMock(SecurityAuditLogger.class);

        wicket = new WicketTester(new OtpOverSmsTestApplication());

    }

    @Override
    @After
    public void tearDown()
            throws Exception {

        jndiTestUtils.tearDown();
    }

    @Test
    public void testEnable()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String mobile = "+32494575697";
        String otp = UUID.randomUUID().toString();
        String pin = "0000";

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(wicket.getServletSession());
        protocolContext.setSubject(userId);
        protocolContext.setAttribute(mobile);

        // verify
        EnablePage enablePage = (EnablePage) wicket.startPage(EnablePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + EnablePage.REQUEST_OTP_FORM_ID, Form.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID);

        // setup
        EJBTestUtils.inject(enablePage, mockOtpOverSmsDeviceService);
        EJBTestUtils.inject(enablePage, mockSamlAuthorityService);

        // stubs
        mockOtpOverSmsDeviceService.requestOtp(wicket.getServletSession(), mobile);

        // prepare
        replay(mockOtpOverSmsDeviceService);

        // operate
        FormTester requestOtpForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + EnablePage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(EnablePage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(EnablePage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(mockOtpOverSmsDeviceService);

        // verify
        enablePage = (EnablePage) wicket.getLastRenderedPage();
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + EnablePage.REQUEST_OTP_FORM_ID);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID, Form.class);

        // setup
        EasyMock.reset(mockOtpOverSmsDeviceService);

        // stubs
        expect(mockOtpOverSmsDeviceService.verifyOtp(wicket.getServletSession(), otp)).andStubReturn(true);
        expect(mockOtpOverSmsDeviceService.enable(userId, mobile, pin)).andStubReturn(true);
        expect(mockSamlAuthorityService.getAuthnAssertionValidity()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockOtpOverSmsDeviceService, mockSamlAuthorityService);

        // operate
        FormTester enableForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID);
        enableForm.setValue(EnablePage.OTP_FIELD_ID, otp);
        enableForm.setValue(EnablePage.PIN_FIELD_ID, pin);
        enableForm.submit(EnablePage.ENABLE_BUTTON_ID);

        // verify
        verify(mockOtpOverSmsDeviceService, mockSamlAuthorityService);
    }

    @Test
    public void testEnableConnectException()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String mobile = "+32494575697";

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(wicket.getServletSession());
        protocolContext.setSubject(userId);
        protocolContext.setAttribute(mobile);

        // verify
        EnablePage enablePage = (EnablePage) wicket.startPage(EnablePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + EnablePage.REQUEST_OTP_FORM_ID, Form.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID);

        // setup
        EJBTestUtils.inject(enablePage, mockOtpOverSmsDeviceService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);
        jndiTestUtils.bindComponent(SecurityAuditLogger.JNDI_BINDING, mockSecurityAuditLogger);

        // stubs
        mockOtpOverSmsDeviceService.requestOtp(wicket.getServletSession(), mobile);
        org.easymock.EasyMock.expectLastCall().andThrow(new ConnectException());
        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockOtpOverSmsDeviceService, mockHelpdeskManager, mockSecurityAuditLogger);

        // operate
        FormTester requestOtpForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + EnablePage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(EnablePage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(EnablePage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(mockOtpOverSmsDeviceService, mockHelpdeskManager, mockSecurityAuditLogger);

        // verify
        wicket.assertRenderedPage(EnablePage.class);
        wicket.assertErrorMessages(new String[] { "errorServiceConnection" });
    }

    @Test
    public void testEnableVerifyFailed()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String mobile = "+32494575697";
        String otp = UUID.randomUUID().toString();
        String pin = "0000";

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(wicket.getServletSession());
        protocolContext.setSubject(userId);
        protocolContext.setAttribute(mobile);

        // verify
        EnablePage enablePage = (EnablePage) wicket.startPage(EnablePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + EnablePage.REQUEST_OTP_FORM_ID, Form.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID);

        // setup
        EJBTestUtils.inject(enablePage, mockOtpOverSmsDeviceService);

        // stubs
        mockOtpOverSmsDeviceService.requestOtp(wicket.getServletSession(), mobile);

        // prepare
        replay(mockOtpOverSmsDeviceService);

        // operate
        FormTester requestOtpForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + EnablePage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(EnablePage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(EnablePage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(mockOtpOverSmsDeviceService);

        // verify
        enablePage = (EnablePage) wicket.getLastRenderedPage();
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + EnablePage.REQUEST_OTP_FORM_ID);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID, Form.class);

        // setup
        EasyMock.reset(mockOtpOverSmsDeviceService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);

        // stubs
        expect(mockOtpOverSmsDeviceService.verifyOtp(wicket.getServletSession(), otp)).andStubReturn(false);
        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockOtpOverSmsDeviceService, mockHelpdeskManager);

        // operate
        FormTester enableForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID);
        enableForm.setValue(EnablePage.OTP_FIELD_ID, otp);
        enableForm.setValue(EnablePage.PIN_FIELD_ID, pin);
        enableForm.submit(EnablePage.ENABLE_BUTTON_ID);

        // verify
        verify(mockOtpOverSmsDeviceService, mockHelpdeskManager);

        wicket.assertRenderedPage(EnablePage.class);
        wicket.assertErrorMessages(new String[] { "authenticationFailedMsg" });
    }

    @Test
    public void testEnablePinIncorrect()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String mobile = "+32494575697";
        String otp = UUID.randomUUID().toString();
        String pin = "0000";

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(wicket.getServletSession());
        protocolContext.setSubject(userId);
        protocolContext.setAttribute(mobile);

        // verify
        EnablePage enablePage = (EnablePage) wicket.startPage(EnablePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + EnablePage.REQUEST_OTP_FORM_ID, Form.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID);

        // setup
        EJBTestUtils.inject(enablePage, mockOtpOverSmsDeviceService);

        // stubs
        mockOtpOverSmsDeviceService.requestOtp(wicket.getServletSession(), mobile);

        // prepare
        replay(mockOtpOverSmsDeviceService);

        // operate
        FormTester requestOtpForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + EnablePage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(EnablePage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(EnablePage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(mockOtpOverSmsDeviceService);

        // verify
        enablePage = (EnablePage) wicket.getLastRenderedPage();
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + EnablePage.REQUEST_OTP_FORM_ID);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID, Form.class);

        // setup
        EasyMock.reset(mockOtpOverSmsDeviceService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);

        // stubs
        expect(mockOtpOverSmsDeviceService.verifyOtp(wicket.getServletSession(), otp)).andStubReturn(true);
        expect(mockOtpOverSmsDeviceService.enable(userId, mobile, pin)).andStubReturn(false);
        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockOtpOverSmsDeviceService, mockHelpdeskManager);
        // operate
        FormTester enableForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID);
        enableForm.setValue(EnablePage.OTP_FIELD_ID, otp);
        enableForm.setValue(EnablePage.PIN_FIELD_ID, pin);
        enableForm.submit(EnablePage.ENABLE_BUTTON_ID);

        // verify
        verify(mockOtpOverSmsDeviceService, mockHelpdeskManager);

        wicket.assertRenderedPage(EnablePage.class);
        wicket.assertErrorMessages(new String[] { "errorPinNotCorrect" });
    }
}
