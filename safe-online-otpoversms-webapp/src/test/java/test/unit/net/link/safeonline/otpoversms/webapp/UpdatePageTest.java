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
    public void testUpdate()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String mobile = "+32494575697";
        String otp = UUID.randomUUID().toString();
        String oldPin = "0000";
        String newPin = "1111";

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(this.wicket.getServletSession());
        protocolContext.setSubject(userId);
        protocolContext.setAttribute(mobile);

        // verify
        UpdatePage updatePage = (UpdatePage) this.wicket.startPage(UpdatePage.class);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID, Form.class);
        this.wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID);

        // setup
        EJBTestUtils.inject(updatePage, this.mockOtpOverSmsDeviceService);
        EJBTestUtils.inject(updatePage, this.mockSamlAuthorityService);

        // stubs
        this.mockOtpOverSmsDeviceService.checkMobile(mobile);
        this.mockOtpOverSmsDeviceService.requestOtp(this.wicket.getServletSession(), mobile);

        // prepare
        replay(this.mockOtpOverSmsDeviceService);

        // operate
        FormTester requestOtpForm = this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(UpdatePage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(UpdatePage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(this.mockOtpOverSmsDeviceService);

        // verify
        updatePage = (UpdatePage) this.wicket.getLastRenderedPage();
        this.wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID, Form.class);

        // setup
        EasyMock.reset(this.mockOtpOverSmsDeviceService);

        // stubs
        expect(this.mockOtpOverSmsDeviceService.verifyOtp(this.wicket.getServletSession(), mobile, otp)).andStubReturn(true);
        expect(this.mockOtpOverSmsDeviceService.update(userId, mobile, oldPin, newPin)).andStubReturn(true);
        expect(this.mockSamlAuthorityService.getAuthnAssertionValidity()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(this.mockOtpOverSmsDeviceService, this.mockSamlAuthorityService);

        // operate
        FormTester updateForm = this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID);
        updateForm.setValue(UpdatePage.OTP_FIELD_ID, otp);
        updateForm.setValue(UpdatePage.OLDPIN_FIELD_ID, oldPin);
        updateForm.setValue(UpdatePage.PIN1_FIELD_ID, newPin);
        updateForm.setValue(UpdatePage.PIN2_FIELD_ID, newPin);
        updateForm.submit(UpdatePage.SAVE_BUTTON_ID);

        // verify
        verify(this.mockOtpOverSmsDeviceService, this.mockSamlAuthorityService);
    }

    @Test
    public void testUpdateSubjectNotFound()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String mobile = "+32494575697";

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(this.wicket.getServletSession());
        protocolContext.setSubject(userId);
        protocolContext.setAttribute(mobile);

        // verify
        UpdatePage updatePage = (UpdatePage) this.wicket.startPage(UpdatePage.class);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID, Form.class);
        this.wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID);

        // setup
        EJBTestUtils.inject(updatePage, this.mockOtpOverSmsDeviceService);
        this.jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, this.mockHelpdeskManager);

        // stubs
        this.mockOtpOverSmsDeviceService.checkMobile(mobile);
        org.easymock.EasyMock.expectLastCall().andThrow(new SubjectNotFoundException());
        expect(this.mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(this.mockOtpOverSmsDeviceService, this.mockHelpdeskManager);

        // operate
        FormTester requestOtpForm = this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(UpdatePage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(UpdatePage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(this.mockOtpOverSmsDeviceService, this.mockHelpdeskManager);

        this.wicket.assertRenderedPage(UpdatePage.class);
        this.wicket.assertErrorMessages(new String[] { "mobileNotRegistered" });

    }

    @Test
    public void testUpdateDeviceDisabled()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String mobile = "+32494575697";

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(this.wicket.getServletSession());
        protocolContext.setSubject(userId);
        protocolContext.setAttribute(mobile);

        // verify
        UpdatePage updatePage = (UpdatePage) this.wicket.startPage(UpdatePage.class);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID, Form.class);
        this.wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID);

        // setup
        EJBTestUtils.inject(updatePage, this.mockOtpOverSmsDeviceService);
        this.jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, this.mockHelpdeskManager);

        // stubs
        this.mockOtpOverSmsDeviceService.checkMobile(mobile);
        org.easymock.EasyMock.expectLastCall().andThrow(new DeviceDisabledException());
        expect(this.mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(this.mockOtpOverSmsDeviceService, this.mockHelpdeskManager);

        // operate
        FormTester requestOtpForm = this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(UpdatePage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(UpdatePage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(this.mockOtpOverSmsDeviceService, this.mockHelpdeskManager);

        this.wicket.assertRenderedPage(UpdatePage.class);
        this.wicket.assertErrorMessages(new String[] { "mobileDisabled" });

    }

    @Test
    public void testUpdateConnectException()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String mobile = "+32494575697";

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(this.wicket.getServletSession());
        protocolContext.setSubject(userId);
        protocolContext.setAttribute(mobile);

        // verify
        UpdatePage updatePage = (UpdatePage) this.wicket.startPage(UpdatePage.class);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID, Form.class);
        this.wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID);

        // setup
        EJBTestUtils.inject(updatePage, this.mockOtpOverSmsDeviceService);
        this.jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, this.mockHelpdeskManager);
        this.jndiTestUtils.bindComponent(SecurityAuditLogger.JNDI_BINDING, this.mockSecurityAuditLogger);

        // stubs
        this.mockOtpOverSmsDeviceService.checkMobile(mobile);
        this.mockOtpOverSmsDeviceService.requestOtp(this.wicket.getServletSession(), mobile);
        org.easymock.EasyMock.expectLastCall().andThrow(new ConnectException());
        expect(this.mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(this.mockOtpOverSmsDeviceService, this.mockHelpdeskManager, this.mockSecurityAuditLogger);

        // operate
        FormTester requestOtpForm = this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(UpdatePage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(UpdatePage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(this.mockOtpOverSmsDeviceService, this.mockHelpdeskManager, this.mockSecurityAuditLogger);

        this.wicket.assertRenderedPage(UpdatePage.class);
        this.wicket.assertErrorMessages(new String[] { "errorServiceConnection" });

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

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(this.wicket.getServletSession());
        protocolContext.setSubject(userId);
        protocolContext.setAttribute(mobile);

        // verify
        UpdatePage updatePage = (UpdatePage) this.wicket.startPage(UpdatePage.class);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID, Form.class);
        this.wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID);

        // setup
        EJBTestUtils.inject(updatePage, this.mockOtpOverSmsDeviceService);

        // stubs
        this.mockOtpOverSmsDeviceService.checkMobile(mobile);
        this.mockOtpOverSmsDeviceService.requestOtp(this.wicket.getServletSession(), mobile);

        // prepare
        replay(this.mockOtpOverSmsDeviceService);

        // operate
        FormTester requestOtpForm = this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(UpdatePage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(UpdatePage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(this.mockOtpOverSmsDeviceService);

        // verify
        updatePage = (UpdatePage) this.wicket.getLastRenderedPage();
        this.wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID, Form.class);

        // setup
        EasyMock.reset(this.mockOtpOverSmsDeviceService);
        this.jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, this.mockHelpdeskManager);

        // stubs
        expect(this.mockOtpOverSmsDeviceService.verifyOtp(this.wicket.getServletSession(), mobile, otp)).andStubReturn(false);
        expect(this.mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(this.mockOtpOverSmsDeviceService, this.mockHelpdeskManager);

        // operate
        FormTester updateForm = this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID);
        updateForm.setValue(UpdatePage.OTP_FIELD_ID, otp);
        updateForm.setValue(UpdatePage.OLDPIN_FIELD_ID, oldPin);
        updateForm.setValue(UpdatePage.PIN1_FIELD_ID, newPin);
        updateForm.setValue(UpdatePage.PIN2_FIELD_ID, newPin);
        updateForm.submit(UpdatePage.SAVE_BUTTON_ID);

        // verify
        verify(this.mockOtpOverSmsDeviceService, this.mockHelpdeskManager);

        this.wicket.assertRenderedPage(UpdatePage.class);
        this.wicket.assertErrorMessages(new String[] { "authenticationFailedMsg" });
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

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(this.wicket.getServletSession());
        protocolContext.setSubject(userId);
        protocolContext.setAttribute(mobile);

        // verify
        UpdatePage updatePage = (UpdatePage) this.wicket.startPage(UpdatePage.class);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID, Form.class);
        this.wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID);

        // setup
        EJBTestUtils.inject(updatePage, this.mockOtpOverSmsDeviceService);
        this.jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, this.mockHelpdeskManager);

        // stubs
        this.mockOtpOverSmsDeviceService.checkMobile(mobile);
        this.mockOtpOverSmsDeviceService.requestOtp(this.wicket.getServletSession(), mobile);

        // prepare
        replay(this.mockOtpOverSmsDeviceService);

        // operate
        FormTester requestOtpForm = this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID);
        requestOtpForm.setValue(UpdatePage.MOBILE_FIELD_ID, mobile);
        requestOtpForm.submit(UpdatePage.REQUEST_OTP_BUTTON_ID);

        // verify
        verify(this.mockOtpOverSmsDeviceService);

        // verify
        updatePage = (UpdatePage) this.wicket.getLastRenderedPage();
        this.wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + UpdatePage.REQUEST_OTP_FORM_ID);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID, Form.class);

        // setup
        EasyMock.reset(this.mockOtpOverSmsDeviceService);

        // stubs
        expect(this.mockOtpOverSmsDeviceService.verifyOtp(this.wicket.getServletSession(), mobile, otp)).andStubReturn(true);
        expect(this.mockOtpOverSmsDeviceService.update(userId, mobile, oldPin, newPin)).andStubReturn(false);
        expect(this.mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(this.mockOtpOverSmsDeviceService, this.mockHelpdeskManager);

        // operate
        FormTester updateForm = this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID);
        updateForm.setValue(UpdatePage.OTP_FIELD_ID, otp);
        updateForm.setValue(UpdatePage.OLDPIN_FIELD_ID, oldPin);
        updateForm.setValue(UpdatePage.PIN1_FIELD_ID, newPin);
        updateForm.setValue(UpdatePage.PIN2_FIELD_ID, newPin);
        updateForm.submit(UpdatePage.SAVE_BUTTON_ID);

        // verify
        verify(this.mockOtpOverSmsDeviceService, this.mockHelpdeskManager);

        this.wicket.assertRenderedPage(UpdatePage.class);
        this.wicket.assertErrorMessages(new String[] { "errorPinNotCorrect" });
    }
}
