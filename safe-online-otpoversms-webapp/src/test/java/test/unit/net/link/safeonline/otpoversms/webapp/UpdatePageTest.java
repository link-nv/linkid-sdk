/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

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
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.helpdesk.HelpdeskManager;
import net.link.safeonline.model.otpoversms.OtpOverSmsDeviceService;
import net.link.safeonline.osgi.sms.exception.SmsServiceException;
import net.link.safeonline.otpoversms.webapp.UpdatePage;
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


public class UpdatePageTest {

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

        jndiTestUtils.bindComponent(SecurityAuditLogger.JNDI_BINDING, mockSecurityAuditLogger);

        wicket = new WicketTester(new OtpOverSmsTestApplication());
    }

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
        protocolContext.setAttributeId(mobile);

        // verify
        UpdatePage updatePage = (UpdatePage) wicket.startPage(UpdatePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID, Form.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID);

        // setup
        jndiTestUtils.bindComponent(OtpOverSmsDeviceService.JNDI_BINDING, mockOtpOverSmsDeviceService);
        EJBTestUtils.inject(updatePage, mockSamlAuthorityService);

        // stubs
        mockOtpOverSmsDeviceService.requestOtp(mobile);
        expect(mockOtpOverSmsDeviceService.isChallenged()).andReturn(true);

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
        expect(mockOtpOverSmsDeviceService.isChallenged()).andReturn(true);
        mockOtpOverSmsDeviceService.update(userId, oldPin, newPin, otp);
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
        protocolContext.setAttributeId(mobile);

        // verify
        wicket.startPage(UpdatePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID, Form.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID);

        // setup
        jndiTestUtils.bindComponent(OtpOverSmsDeviceService.JNDI_BINDING, mockOtpOverSmsDeviceService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);

        // stubs
        mockOtpOverSmsDeviceService.requestOtp(mobile);
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
        wicket.assertErrorMessages(new String[] { "errorSubjectNotFound" });

    }

    @Test
    public void testUpdateDeviceDisabled()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String mobile = "+32494575697";
        String otp = UUID.randomUUID().toString();
        String oldPin = "0000";
        String newPin = "1111";

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(wicket.getServletSession());
        protocolContext.setSubject(userId);
        protocolContext.setAttributeId(mobile);

        // verify
        wicket.startPage(UpdatePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID, Form.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID);

        // setup
        jndiTestUtils.bindComponent(OtpOverSmsDeviceService.JNDI_BINDING, mockOtpOverSmsDeviceService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);

        // stubs
        mockOtpOverSmsDeviceService.requestOtp(mobile);
        expect(mockOtpOverSmsDeviceService.isChallenged()).andReturn(true);

        // prepare
        replay(mockOtpOverSmsDeviceService);

        // operate
        FormTester requestOtpForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(UpdatePage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(UpdatePage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(mockOtpOverSmsDeviceService);

        // verify
        wicket.getLastRenderedPage();
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID, Form.class);

        // setup
        reset(mockOtpOverSmsDeviceService);

        // stubs
        expect(mockOtpOverSmsDeviceService.isChallenged()).andReturn(true);
        mockOtpOverSmsDeviceService.update(userId, oldPin, newPin, otp);
        expectLastCall().andThrow(new DeviceDisabledException());
        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockOtpOverSmsDeviceService, mockSamlAuthorityService, mockHelpdeskManager);

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
        wicket.assertErrorMessages(new String[] { "errorDeviceDisabled" });

    }

    @Test
    public void testUpdateConnectException()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String mobile = "+32494575697";

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(wicket.getServletSession());
        protocolContext.setSubject(userId);
        protocolContext.setAttributeId(mobile);

        // verify
        wicket.startPage(UpdatePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID, Form.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID);

        // setup
        jndiTestUtils.bindComponent(OtpOverSmsDeviceService.JNDI_BINDING, mockOtpOverSmsDeviceService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);
        jndiTestUtils.bindComponent(SecurityAuditLogger.JNDI_BINDING, mockSecurityAuditLogger);

        // stubs
        mockOtpOverSmsDeviceService.requestOtp(mobile);
        org.easymock.EasyMock.expectLastCall().andThrow(new SmsServiceException());
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
        protocolContext.setAttributeId(mobile);

        // verify
        wicket.startPage(UpdatePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID, Form.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID);

        // setup
        jndiTestUtils.bindComponent(OtpOverSmsDeviceService.JNDI_BINDING, mockOtpOverSmsDeviceService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);

        // stubs
        mockOtpOverSmsDeviceService.requestOtp(mobile);
        expect(mockOtpOverSmsDeviceService.isChallenged()).andReturn(true);

        // prepare
        replay(mockOtpOverSmsDeviceService);

        // operate
        FormTester requestOtpForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(UpdatePage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(UpdatePage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(mockOtpOverSmsDeviceService);

        // verify
        wicket.getLastRenderedPage();
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID, Form.class);

        // setup
        reset(mockOtpOverSmsDeviceService);

        // stubs
        expect(mockOtpOverSmsDeviceService.isChallenged()).andReturn(true);
        mockOtpOverSmsDeviceService.update(userId, oldPin, newPin, otp);
        expectLastCall().andThrow(new DeviceAuthenticationException("Incorrect PIN"));
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
