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
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.helpdesk.HelpdeskManager;
import net.link.safeonline.model.otpoversms.OtpOverSmsDeviceService;
import net.link.safeonline.otpoversms.webapp.UpdatePage;
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


public class UpdatePageTest extends TestCase {

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
    public void testUpdate()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String mobile = "+32494575697";
        String otp = UUID.randomUUID().toString();
        String oldPin = "0000";
        String newPin = "1111";

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(wicket.getServletSession());
        protocolContext.setSubject(userId);
        protocolContext.setAttribute(mobile);

        // verify
        UpdatePage updatePage = (UpdatePage) wicket.startPage(UpdatePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID, Form.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID);

        // setup
        EJBTestUtils.inject(updatePage, mockOtpOverSmsDeviceService);
        EJBTestUtils.inject(updatePage, mockSamlAuthorityService);

        // stubs
        mockOtpOverSmsDeviceService.checkMobile(mobile);
        mockOtpOverSmsDeviceService.requestOtp(wicket.getServletSession(), mobile);

        // prepare
        replay(mockOtpOverSmsDeviceService);

        // operate
        FormTester requestOtpForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(UpdatePage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(UpdatePage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(mockOtpOverSmsDeviceService);

        // verify
        updatePage = (UpdatePage) wicket.getLastRenderedPage();
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID, Form.class);

        // setup
        EasyMock.reset(mockOtpOverSmsDeviceService);

        // stubs
        expect(mockOtpOverSmsDeviceService.verifyOtp(wicket.getServletSession(), mobile, otp)).andStubReturn(true);
        expect(mockOtpOverSmsDeviceService.update(userId, mobile, oldPin, newPin)).andStubReturn(true);
        expect(mockSamlAuthorityService.getAuthnAssertionValidity()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockOtpOverSmsDeviceService, mockSamlAuthorityService);

        // operate
        FormTester updateForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID);
        updateForm.setValue(UpdatePage.OTP_FIELD_ID, otp);
        updateForm.setValue(UpdatePage.OLDPIN_FIELD_ID, oldPin);
        updateForm.setValue(UpdatePage.PIN1_FIELD_ID, newPin);
        updateForm.setValue(UpdatePage.PIN2_FIELD_ID, newPin);
        updateForm.submit(UpdatePage.SAVE_BUTTON_ID);

        // verify
        verify(mockOtpOverSmsDeviceService, mockSamlAuthorityService);
    }

    @Test
    public void testUpdateSubjectNotFound()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String mobile = "+32494575697";

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(wicket.getServletSession());
        protocolContext.setSubject(userId);
        protocolContext.setAttribute(mobile);

        // verify
        UpdatePage updatePage = (UpdatePage) wicket.startPage(UpdatePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID, Form.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID);

        // setup
        EJBTestUtils.inject(updatePage, mockOtpOverSmsDeviceService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);

        // stubs
        mockOtpOverSmsDeviceService.checkMobile(mobile);
        org.easymock.EasyMock.expectLastCall().andThrow(new SubjectNotFoundException());
        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockOtpOverSmsDeviceService, mockHelpdeskManager);

        // operate
        FormTester requestOtpForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(UpdatePage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(UpdatePage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(mockOtpOverSmsDeviceService, mockHelpdeskManager);

        wicket.assertRenderedPage(UpdatePage.class);
        wicket.assertErrorMessages(new String[] { "mobileNotRegistered" });

    }

    @Test
    public void testUpdateDeviceDisabled()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String mobile = "+32494575697";

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(wicket.getServletSession());
        protocolContext.setSubject(userId);
        protocolContext.setAttribute(mobile);

        // verify
        UpdatePage updatePage = (UpdatePage) wicket.startPage(UpdatePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID, Form.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID);

        // setup
        EJBTestUtils.inject(updatePage, mockOtpOverSmsDeviceService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);

        // stubs
        mockOtpOverSmsDeviceService.checkMobile(mobile);
        org.easymock.EasyMock.expectLastCall().andThrow(new DeviceDisabledException());
        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockOtpOverSmsDeviceService, mockHelpdeskManager);

        // operate
        FormTester requestOtpForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(UpdatePage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(UpdatePage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(mockOtpOverSmsDeviceService, mockHelpdeskManager);

        wicket.assertRenderedPage(UpdatePage.class);
        wicket.assertErrorMessages(new String[] { "mobileDisabled" });

    }

    @Test
    public void testUpdateConnectException()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String mobile = "+32494575697";

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(wicket.getServletSession());
        protocolContext.setSubject(userId);
        protocolContext.setAttribute(mobile);

        // verify
        UpdatePage updatePage = (UpdatePage) wicket.startPage(UpdatePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID, Form.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID);

        // setup
        EJBTestUtils.inject(updatePage, mockOtpOverSmsDeviceService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);
        jndiTestUtils.bindComponent(SecurityAuditLogger.JNDI_BINDING, mockSecurityAuditLogger);

        // stubs
        mockOtpOverSmsDeviceService.checkMobile(mobile);
        mockOtpOverSmsDeviceService.requestOtp(wicket.getServletSession(), mobile);
        org.easymock.EasyMock.expectLastCall().andThrow(new ConnectException());
        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockOtpOverSmsDeviceService, mockHelpdeskManager, mockSecurityAuditLogger);

        // operate
        FormTester requestOtpForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(UpdatePage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(UpdatePage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(mockOtpOverSmsDeviceService, mockHelpdeskManager, mockSecurityAuditLogger);

        wicket.assertRenderedPage(UpdatePage.class);
        wicket.assertErrorMessages(new String[] { "errorServiceConnection" });

    }

    @Test
    public void testUpdateVerifyFailed()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String mobile = "+32494575697";
        String otp = UUID.randomUUID().toString();
        String oldPin = "0000";
        String newPin = "1111";

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(wicket.getServletSession());
        protocolContext.setSubject(userId);
        protocolContext.setAttribute(mobile);

        // verify
        UpdatePage updatePage = (UpdatePage) wicket.startPage(UpdatePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID, Form.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID);

        // setup
        EJBTestUtils.inject(updatePage, mockOtpOverSmsDeviceService);

        // stubs
        mockOtpOverSmsDeviceService.checkMobile(mobile);
        mockOtpOverSmsDeviceService.requestOtp(wicket.getServletSession(), mobile);

        // prepare
        replay(mockOtpOverSmsDeviceService);

        // operate
        FormTester requestOtpForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(UpdatePage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(UpdatePage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(mockOtpOverSmsDeviceService);

        // verify
        updatePage = (UpdatePage) wicket.getLastRenderedPage();
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID, Form.class);

        // setup
        EasyMock.reset(mockOtpOverSmsDeviceService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);

        // stubs
        expect(mockOtpOverSmsDeviceService.verifyOtp(wicket.getServletSession(), mobile, otp)).andStubReturn(false);
        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockOtpOverSmsDeviceService, mockHelpdeskManager);

        // operate
        FormTester updateForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID);
        updateForm.setValue(UpdatePage.OTP_FIELD_ID, otp);
        updateForm.setValue(UpdatePage.OLDPIN_FIELD_ID, oldPin);
        updateForm.setValue(UpdatePage.PIN1_FIELD_ID, newPin);
        updateForm.setValue(UpdatePage.PIN2_FIELD_ID, newPin);
        updateForm.submit(UpdatePage.SAVE_BUTTON_ID);

        // verify
        verify(mockOtpOverSmsDeviceService, mockHelpdeskManager);

        wicket.assertRenderedPage(UpdatePage.class);
        wicket.assertErrorMessages(new String[] { "authenticationFailedMsg" });
    }

    @Test
    public void testUpdatePinIncorrect()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String mobile = "+32494575697";
        String otp = UUID.randomUUID().toString();
        String oldPin = "0000";
        String newPin = "1111";

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(wicket.getServletSession());
        protocolContext.setSubject(userId);
        protocolContext.setAttribute(mobile);

        // verify
        UpdatePage updatePage = (UpdatePage) wicket.startPage(UpdatePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID, Form.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID);

        // setup
        EJBTestUtils.inject(updatePage, mockOtpOverSmsDeviceService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);

        // stubs
        mockOtpOverSmsDeviceService.checkMobile(mobile);
        mockOtpOverSmsDeviceService.requestOtp(wicket.getServletSession(), mobile);

        // prepare
        replay(mockOtpOverSmsDeviceService);

        // operate
        FormTester requestOtpForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(UpdatePage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(UpdatePage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(mockOtpOverSmsDeviceService);

        // verify
        updatePage = (UpdatePage) wicket.getLastRenderedPage();
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID, Form.class);

        // setup
        EasyMock.reset(mockOtpOverSmsDeviceService);

        // stubs
        expect(mockOtpOverSmsDeviceService.verifyOtp(wicket.getServletSession(), mobile, otp)).andStubReturn(true);
        expect(mockOtpOverSmsDeviceService.update(userId, mobile, oldPin, newPin)).andStubReturn(false);
        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockOtpOverSmsDeviceService, mockHelpdeskManager);

        // operate
        FormTester updateForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID);
        updateForm.setValue(UpdatePage.OTP_FIELD_ID, otp);
        updateForm.setValue(UpdatePage.OLDPIN_FIELD_ID, oldPin);
        updateForm.setValue(UpdatePage.PIN1_FIELD_ID, newPin);
        updateForm.setValue(UpdatePage.PIN2_FIELD_ID, newPin);
        updateForm.submit(UpdatePage.SAVE_BUTTON_ID);

        // verify
        verify(mockOtpOverSmsDeviceService, mockHelpdeskManager);

        wicket.assertRenderedPage(UpdatePage.class);
        wicket.assertErrorMessages(new String[] { "errorPinNotCorrect" });
    }
}
