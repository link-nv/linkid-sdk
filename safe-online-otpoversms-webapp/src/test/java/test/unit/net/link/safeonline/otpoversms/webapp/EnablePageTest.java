package test.unit.net.link.safeonline.otpoversms.webapp;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.util.UUID;

import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.exception.DeviceAuthenticationException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.helpdesk.HelpdeskManager;
import net.link.safeonline.model.otpoversms.OtpOverSmsDeviceService;
import net.link.safeonline.osgi.sms.exception.SmsServiceException;
import net.link.safeonline.otpoversms.webapp.EnablePage;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class EnablePageTest {

    private OtpOverSmsDeviceService mockOtpOverSmsDeviceService;

    private SamlAuthorityService    mockSamlAuthorityService;

    private HelpdeskManager         mockHelpdeskManager;

    private SecurityAuditLogger     mockSecurityAuditLogger;

    private WicketTester            wicket;

    private JndiTestUtils           jndiTestUtils;


    @Before
    public void setUp()
            throws Exception {

        WicketUtil.setUnitTesting(true);

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();

        mockOtpOverSmsDeviceService = createMock(OtpOverSmsDeviceService.class);
        mockSamlAuthorityService = createMock(SamlAuthorityService.class);
        mockHelpdeskManager = createMock(HelpdeskManager.class);
        mockSecurityAuditLogger = createMock(SecurityAuditLogger.class);

        wicket = new WicketTester(new OtpOverSmsTestApplication());
    }

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
        protocolContext.setAttributeId(mobile);

        // verify
        EnablePage enablePage = (EnablePage) wicket.startPage(EnablePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + EnablePage.REQUEST_OTP_FORM_ID, Form.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID);

        // setup
        jndiTestUtils.bindComponent(OtpOverSmsDeviceService.JNDI_BINDING, mockOtpOverSmsDeviceService);
        EJBTestUtils.inject(enablePage, mockSamlAuthorityService);

        // stubs
        mockOtpOverSmsDeviceService.requestOtp(mobile);
        expect(mockOtpOverSmsDeviceService.isChallenged()).andReturn(true);

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
        expect(mockOtpOverSmsDeviceService.isChallenged()).andReturn(true);
        mockOtpOverSmsDeviceService.enable(userId, pin, otp);
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
        protocolContext.setAttributeId(mobile);

        // verify
        EnablePage enablePage = (EnablePage) wicket.startPage(EnablePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + EnablePage.REQUEST_OTP_FORM_ID, Form.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID);

        // setup
        jndiTestUtils.bindComponent(OtpOverSmsDeviceService.JNDI_BINDING, mockOtpOverSmsDeviceService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);
        jndiTestUtils.bindComponent(SecurityAuditLogger.JNDI_BINDING, mockSecurityAuditLogger);
        EJBTestUtils.inject(enablePage, mockSamlAuthorityService);

        // stubs
        mockOtpOverSmsDeviceService.requestOtp(mobile);
        expectLastCall().andThrow(new SmsServiceException());
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
    public void testEnablePinIncorrect()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String mobile = "+32494575697";
        String otp = UUID.randomUUID().toString();
        String pin = "0000";

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(wicket.getServletSession());
        protocolContext.setSubject(userId);
        protocolContext.setAttributeId(mobile);

        // verify
        EnablePage enablePage = (EnablePage) wicket.startPage(EnablePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + EnablePage.REQUEST_OTP_FORM_ID, Form.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID);

        // setup
        jndiTestUtils.bindComponent(OtpOverSmsDeviceService.JNDI_BINDING, mockOtpOverSmsDeviceService);
        EJBTestUtils.inject(enablePage, mockSamlAuthorityService);

        // stubs
        mockOtpOverSmsDeviceService.requestOtp(mobile);
        expect(mockOtpOverSmsDeviceService.isChallenged()).andReturn(true);

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
        reset(mockOtpOverSmsDeviceService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);

        // stubs
        expect(mockOtpOverSmsDeviceService.isChallenged()).andReturn(true);
        mockOtpOverSmsDeviceService.enable(userId, pin, otp);
        expectLastCall().andThrow(new DeviceAuthenticationException("Incorrect PIN"));
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
}
