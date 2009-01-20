/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.otpoversms.webapp;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.net.ConnectException;
import java.util.UUID;

import junit.framework.TestCase;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.wicket.tools.WicketUtil;
import net.link.safeonline.helpdesk.HelpdeskManager;
import net.link.safeonline.model.otpoversms.OtpOverSmsDeviceService;
import net.link.safeonline.otpoversms.webapp.AuthenticationPage;
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


public class AuthenticationPageTest extends TestCase {

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
    public void testAuthenticate()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String mobile = "+32 494 575 697";
        String convertedMobile = net.link.safeonline.custom.converter.PhoneNumberConverter.convertNumber(mobile);
        String otp = UUID.randomUUID().toString();
        String pin = "0000";

        // verify
        AuthenticationPage authenticationPage = (AuthenticationPage) wicket.startPage(AuthenticationPage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.REQUEST_OTP_FORM_ID, Form.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.VERIFY_OTP_FORM_ID);

        // setup
        EJBTestUtils.inject(authenticationPage, mockOtpOverSmsDeviceService);
        EJBTestUtils.inject(authenticationPage, mockSamlAuthorityService);

        // stubs
        mockOtpOverSmsDeviceService.checkMobile(convertedMobile);
        mockOtpOverSmsDeviceService.requestOtp(wicket.getServletSession(), convertedMobile);

        // prepare
        replay(mockOtpOverSmsDeviceService);

        // operate
        FormTester requestOtpForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(AuthenticationPage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(AuthenticationPage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(mockOtpOverSmsDeviceService);

        // verify
        authenticationPage = (AuthenticationPage) wicket.getLastRenderedPage();
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.REQUEST_OTP_FORM_ID);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.VERIFY_OTP_FORM_ID, Form.class);

        // setup
        EasyMock.reset(mockOtpOverSmsDeviceService);

        // stubs
        expect(mockOtpOverSmsDeviceService.verifyOtp(wicket.getServletSession(), convertedMobile, otp)).andStubReturn(true);
        expect(mockOtpOverSmsDeviceService.authenticate(convertedMobile, pin)).andStubReturn(userId);
        expect(mockSamlAuthorityService.getAuthnAssertionValidity()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockOtpOverSmsDeviceService, mockSamlAuthorityService);

        // operate
        FormTester verifyOtpForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.VERIFY_OTP_FORM_ID);
        verifyOtpForm.setValue(AuthenticationPage.OTP_FIELD_ID, otp);
        verifyOtpForm.setValue(AuthenticationPage.PIN_FIELD_ID, pin);
        verifyOtpForm.submit(AuthenticationPage.LOGIN_BUTTON_ID);

        // verify
        verify(mockOtpOverSmsDeviceService, mockSamlAuthorityService);
    }

    @Test
    public void testAuthenticateSubjectNotFound()
            throws Exception {

        // setup
        String mobile = "+32 494 575 697";
        String convertedMobile = net.link.safeonline.custom.converter.PhoneNumberConverter.convertNumber(mobile);

        // Authentication Page: Verify.
        AuthenticationPage authenticationPage = (AuthenticationPage) wicket.startPage(AuthenticationPage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.REQUEST_OTP_FORM_ID, Form.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.VERIFY_OTP_FORM_ID);

        // setup
        EJBTestUtils.inject(authenticationPage, mockOtpOverSmsDeviceService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);

        // stubs
        mockOtpOverSmsDeviceService.checkMobile(convertedMobile);
        org.easymock.EasyMock.expectLastCall().andThrow(new SubjectNotFoundException());
        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockOtpOverSmsDeviceService, mockHelpdeskManager);

        // operate
        FormTester requestOtpForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(AuthenticationPage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(AuthenticationPage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(mockOtpOverSmsDeviceService, mockHelpdeskManager);

        wicket.assertRenderedPage(AuthenticationPage.class);
        wicket.assertErrorMessages(new String[] { "mobileNotRegistered" });

    }

    @Test
    public void testAuthenticateDeviceDisabled()
            throws Exception {

        // setup
        String mobile = "+32 494 575 697";
        String convertedMobile = net.link.safeonline.custom.converter.PhoneNumberConverter.convertNumber(mobile);

        // Authentication Page: Verify.
        AuthenticationPage authenticationPage = (AuthenticationPage) wicket.startPage(AuthenticationPage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.REQUEST_OTP_FORM_ID, Form.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.VERIFY_OTP_FORM_ID);

        // setup
        EJBTestUtils.inject(authenticationPage, mockOtpOverSmsDeviceService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);

        // stubs
        mockOtpOverSmsDeviceService.checkMobile(convertedMobile);
        org.easymock.EasyMock.expectLastCall().andThrow(new DeviceDisabledException());
        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockOtpOverSmsDeviceService, mockHelpdeskManager);

        // operate
        FormTester requestOtpForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(AuthenticationPage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(AuthenticationPage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(mockOtpOverSmsDeviceService, mockHelpdeskManager);

        wicket.assertRenderedPage(AuthenticationPage.class);
        wicket.assertErrorMessages(new String[] { "mobileDisabled" });

    }

    @Test
    public void testAuthenticateConnectException()
            throws Exception {

        // setup
        String mobile = "+32 494 575 697";
        String convertedMobile = net.link.safeonline.custom.converter.PhoneNumberConverter.convertNumber(mobile);

        // Authentication Page: Verify.
        AuthenticationPage authenticationPage = (AuthenticationPage) wicket.startPage(AuthenticationPage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.REQUEST_OTP_FORM_ID, Form.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.VERIFY_OTP_FORM_ID);

        // setup
        EJBTestUtils.inject(authenticationPage, mockOtpOverSmsDeviceService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);
        jndiTestUtils.bindComponent(SecurityAuditLogger.JNDI_BINDING, mockSecurityAuditLogger);

        // stubs
        mockOtpOverSmsDeviceService.checkMobile(convertedMobile);
        mockOtpOverSmsDeviceService.requestOtp(wicket.getServletSession(), convertedMobile);
        org.easymock.EasyMock.expectLastCall().andThrow(new ConnectException());
        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockOtpOverSmsDeviceService, mockHelpdeskManager, mockSecurityAuditLogger);

        // operate
        FormTester requestOtpForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(AuthenticationPage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(AuthenticationPage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(mockOtpOverSmsDeviceService, mockHelpdeskManager, mockSecurityAuditLogger);

        wicket.assertRenderedPage(AuthenticationPage.class);
        wicket.assertErrorMessages(new String[] { "errorServiceConnection" });

    }

    @Test
    public void testAuthenticateVerifyFailed()
            throws Exception {

        // setup
        String mobile = "+32 494 575 697";
        String convertedMobile = net.link.safeonline.custom.converter.PhoneNumberConverter.convertNumber(mobile);
        String otp = UUID.randomUUID().toString();
        String pin = "0000";

        // Authentication Page: Verify.
        AuthenticationPage authenticationPage = (AuthenticationPage) wicket.startPage(AuthenticationPage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.REQUEST_OTP_FORM_ID, Form.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.VERIFY_OTP_FORM_ID);

        // setup
        EJBTestUtils.inject(authenticationPage, mockOtpOverSmsDeviceService);
        EJBTestUtils.inject(authenticationPage, mockSamlAuthorityService);

        // stubs
        mockOtpOverSmsDeviceService.checkMobile(convertedMobile);
        mockOtpOverSmsDeviceService.requestOtp(wicket.getServletSession(), convertedMobile);

        // prepare
        replay(mockOtpOverSmsDeviceService);

        // operate
        FormTester requestOtpForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(AuthenticationPage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(AuthenticationPage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(mockOtpOverSmsDeviceService);

        // verify
        authenticationPage = (AuthenticationPage) wicket.getLastRenderedPage();
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.REQUEST_OTP_FORM_ID);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.VERIFY_OTP_FORM_ID, Form.class);

        // setup
        EasyMock.reset(mockOtpOverSmsDeviceService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);

        // stubs
        expect(mockOtpOverSmsDeviceService.verifyOtp(wicket.getServletSession(), convertedMobile, otp)).andStubReturn(false);
        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockOtpOverSmsDeviceService, mockHelpdeskManager);

        // operate
        FormTester verifyOtpForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.VERIFY_OTP_FORM_ID);
        verifyOtpForm.setValue(AuthenticationPage.OTP_FIELD_ID, otp);
        verifyOtpForm.setValue(AuthenticationPage.PIN_FIELD_ID, pin);
        verifyOtpForm.submit(AuthenticationPage.LOGIN_BUTTON_ID);

        // verify
        verify(mockOtpOverSmsDeviceService, mockHelpdeskManager);

        wicket.assertRenderedPage(AuthenticationPage.class);
        wicket.assertErrorMessages(new String[] { "authenticationFailedMsg" });

    }

    @Test
    public void testAuthenticateFailed()
            throws Exception {

        // setup
        String mobile = "+32 494 575 697";
        String convertedMobile = net.link.safeonline.custom.converter.PhoneNumberConverter.convertNumber(mobile);
        String otp = UUID.randomUUID().toString();
        String pin = "0000";

        // Authentication Page: Verify.
        AuthenticationPage authenticationPage = (AuthenticationPage) wicket.startPage(AuthenticationPage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.REQUEST_OTP_FORM_ID, Form.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.VERIFY_OTP_FORM_ID);

        // setup
        EJBTestUtils.inject(authenticationPage, mockOtpOverSmsDeviceService);
        EJBTestUtils.inject(authenticationPage, mockSamlAuthorityService);

        // stubs
        mockOtpOverSmsDeviceService.checkMobile(convertedMobile);
        mockOtpOverSmsDeviceService.requestOtp(wicket.getServletSession(), convertedMobile);

        // prepare
        replay(mockOtpOverSmsDeviceService);

        // operate
        FormTester requestOtpForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(AuthenticationPage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(AuthenticationPage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(mockOtpOverSmsDeviceService);

        // verify
        authenticationPage = (AuthenticationPage) wicket.getLastRenderedPage();
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.REQUEST_OTP_FORM_ID);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.VERIFY_OTP_FORM_ID, Form.class);

        // setup
        EasyMock.reset(mockOtpOverSmsDeviceService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);

        // stubs
        expect(mockOtpOverSmsDeviceService.verifyOtp(wicket.getServletSession(), convertedMobile, otp)).andStubReturn(true);
        expect(mockOtpOverSmsDeviceService.authenticate(convertedMobile, pin)).andStubReturn(null);
        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockOtpOverSmsDeviceService, mockHelpdeskManager);

        // operate
        FormTester verifyOtpForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.VERIFY_OTP_FORM_ID);
        verifyOtpForm.setValue(AuthenticationPage.OTP_FIELD_ID, otp);
        verifyOtpForm.setValue(AuthenticationPage.PIN_FIELD_ID, pin);
        verifyOtpForm.submit(AuthenticationPage.LOGIN_BUTTON_ID);

        // verify
        verify(mockOtpOverSmsDeviceService, mockHelpdeskManager);

        wicket.assertRenderedPage(AuthenticationPage.class);
        wicket.assertErrorMessages(new String[] { "authenticationFailedMsg" });

    }

}
