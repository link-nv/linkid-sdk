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
    public void testEnable()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String mobile = "+32494575697";
        String otp = UUID.randomUUID().toString();
        String pin = "0000";

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(this.wicket.getServletSession());
        protocolContext.setSubject(userId);
        protocolContext.setAttribute(mobile);

        // verify
        EnablePage enablePage = (EnablePage) this.wicket.startPage(EnablePage.class);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + EnablePage.REQUEST_OTP_FORM_ID, Form.class);
        this.wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID);

        // setup
        EJBTestUtils.inject(enablePage, this.mockOtpOverSmsDeviceService);
        EJBTestUtils.inject(enablePage, this.mockSamlAuthorityService);

        // stubs
        this.mockOtpOverSmsDeviceService.requestOtp(this.wicket.getServletSession(), mobile);

        // prepare
        replay(this.mockOtpOverSmsDeviceService);

        // operate
        FormTester requestOtpForm = this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + EnablePage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(EnablePage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(EnablePage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(this.mockOtpOverSmsDeviceService);

        // verify
        enablePage = (EnablePage) this.wicket.getLastRenderedPage();
        this.wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + EnablePage.REQUEST_OTP_FORM_ID);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID, Form.class);

        // setup
        EasyMock.reset(this.mockOtpOverSmsDeviceService);

        // stubs
        expect(this.mockOtpOverSmsDeviceService.verifyOtp(this.wicket.getServletSession(), otp)).andStubReturn(true);
        expect(this.mockOtpOverSmsDeviceService.enable(userId, mobile, pin)).andStubReturn(true);
        expect(this.mockSamlAuthorityService.getAuthnAssertionValidity()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(this.mockOtpOverSmsDeviceService, this.mockSamlAuthorityService);

        // operate
        FormTester enableForm = this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID);
        enableForm.setValue(EnablePage.OTP_FIELD_ID, otp);
        enableForm.setValue(EnablePage.PIN_FIELD_ID, pin);
        enableForm.submit(EnablePage.ENABLE_BUTTON_ID);

        // verify
        verify(this.mockOtpOverSmsDeviceService, this.mockSamlAuthorityService);
    }

    @Test
    public void testEnableConnectException()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String mobile = "+32494575697";

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(this.wicket.getServletSession());
        protocolContext.setSubject(userId);
        protocolContext.setAttribute(mobile);

        // verify
        EnablePage enablePage = (EnablePage) this.wicket.startPage(EnablePage.class);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + EnablePage.REQUEST_OTP_FORM_ID, Form.class);
        this.wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID);

        // setup
        EJBTestUtils.inject(enablePage, this.mockOtpOverSmsDeviceService);
        this.jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, this.mockHelpdeskManager);
        this.jndiTestUtils.bindComponent(SecurityAuditLogger.JNDI_BINDING, this.mockSecurityAuditLogger);

        // stubs
        this.mockOtpOverSmsDeviceService.requestOtp(this.wicket.getServletSession(), mobile);
        org.easymock.EasyMock.expectLastCall().andThrow(new ConnectException());
        expect(this.mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(this.mockOtpOverSmsDeviceService, this.mockHelpdeskManager, this.mockSecurityAuditLogger);

        // operate
        FormTester requestOtpForm = this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + EnablePage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(EnablePage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(EnablePage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(this.mockOtpOverSmsDeviceService, this.mockHelpdeskManager, this.mockSecurityAuditLogger);

        // verify
        this.wicket.assertRenderedPage(EnablePage.class);
        this.wicket.assertErrorMessages(new String[] { "errorServiceConnection" });
    }

    @Test
    public void testEnableVerifyFailed()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String mobile = "+32494575697";
        String otp = UUID.randomUUID().toString();
        String pin = "0000";

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(this.wicket.getServletSession());
        protocolContext.setSubject(userId);
        protocolContext.setAttribute(mobile);

        // verify
        EnablePage enablePage = (EnablePage) this.wicket.startPage(EnablePage.class);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + EnablePage.REQUEST_OTP_FORM_ID, Form.class);
        this.wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID);

        // setup
        EJBTestUtils.inject(enablePage, this.mockOtpOverSmsDeviceService);

        // stubs
        this.mockOtpOverSmsDeviceService.requestOtp(this.wicket.getServletSession(), mobile);

        // prepare
        replay(this.mockOtpOverSmsDeviceService);

        // operate
        FormTester requestOtpForm = this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + EnablePage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(EnablePage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(EnablePage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(this.mockOtpOverSmsDeviceService);

        // verify
        enablePage = (EnablePage) this.wicket.getLastRenderedPage();
        this.wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + EnablePage.REQUEST_OTP_FORM_ID);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID, Form.class);

        // setup
        EasyMock.reset(this.mockOtpOverSmsDeviceService);
        this.jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, this.mockHelpdeskManager);

        // stubs
        expect(this.mockOtpOverSmsDeviceService.verifyOtp(this.wicket.getServletSession(), otp)).andStubReturn(false);
        expect(this.mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(this.mockOtpOverSmsDeviceService, this.mockHelpdeskManager);

        // operate
        FormTester enableForm = this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID);
        enableForm.setValue(EnablePage.OTP_FIELD_ID, otp);
        enableForm.setValue(EnablePage.PIN_FIELD_ID, pin);
        enableForm.submit(EnablePage.ENABLE_BUTTON_ID);

        // verify
        verify(this.mockOtpOverSmsDeviceService, this.mockHelpdeskManager);

        this.wicket.assertRenderedPage(EnablePage.class);
        this.wicket.assertErrorMessages(new String[] { "authenticationFailedMsg" });
    }

    @Test
    public void testEnablePinIncorrect()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String mobile = "+32494575697";
        String otp = UUID.randomUUID().toString();
        String pin = "0000";

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(this.wicket.getServletSession());
        protocolContext.setSubject(userId);
        protocolContext.setAttribute(mobile);

        // verify
        EnablePage enablePage = (EnablePage) this.wicket.startPage(EnablePage.class);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + EnablePage.REQUEST_OTP_FORM_ID, Form.class);
        this.wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID);

        // setup
        EJBTestUtils.inject(enablePage, this.mockOtpOverSmsDeviceService);

        // stubs
        this.mockOtpOverSmsDeviceService.requestOtp(this.wicket.getServletSession(), mobile);

        // prepare
        replay(this.mockOtpOverSmsDeviceService);

        // operate
        FormTester requestOtpForm = this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + EnablePage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(EnablePage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(EnablePage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(this.mockOtpOverSmsDeviceService);

        // verify
        enablePage = (EnablePage) this.wicket.getLastRenderedPage();
        this.wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + EnablePage.REQUEST_OTP_FORM_ID);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID, Form.class);

        // setup
        EasyMock.reset(this.mockOtpOverSmsDeviceService);
        this.jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, this.mockHelpdeskManager);

        // stubs
        expect(this.mockOtpOverSmsDeviceService.verifyOtp(this.wicket.getServletSession(), otp)).andStubReturn(true);
        expect(this.mockOtpOverSmsDeviceService.enable(userId, mobile, pin)).andStubReturn(false);
        expect(this.mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(this.mockOtpOverSmsDeviceService, this.mockHelpdeskManager);
        // operate
        FormTester enableForm = this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID);
        enableForm.setValue(EnablePage.OTP_FIELD_ID, otp);
        enableForm.setValue(EnablePage.PIN_FIELD_ID, pin);
        enableForm.submit(EnablePage.ENABLE_BUTTON_ID);

        // verify
        verify(this.mockOtpOverSmsDeviceService, this.mockHelpdeskManager);

        this.wicket.assertRenderedPage(EnablePage.class);
        this.wicket.assertErrorMessages(new String[] { "errorPinNotCorrect" });
    }
}
