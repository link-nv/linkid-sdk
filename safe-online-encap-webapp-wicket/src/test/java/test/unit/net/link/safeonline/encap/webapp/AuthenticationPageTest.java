package test.unit.net.link.safeonline.encap.webapp;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.UUID;

import junit.framework.TestCase;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.encap.webapp.AuthenticationPage;
import net.link.safeonline.helpdesk.HelpdeskManager;
import net.link.safeonline.model.encap.EncapDeviceService;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.JmxTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.MBeanActionHandler;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import net.link.safeonline.util.ee.IdentityServiceClient;
import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.tools.WicketUtil;
import net.link.safeonline.wicket.tools.olas.DummyNameIdentifierMappingClient;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class AuthenticationPageTest extends TestCase {

    private EncapDeviceService   mockEncapDeviceService;

    private SamlAuthorityService mockSamlAuthorityService;

    private HelpdeskManager      mockHelpdeskManager;

    private WicketTester         wicket;

    private JndiTestUtils        jndiTestUtils;


    @Override
    @Before
    public void setUp()
            throws Exception {

        super.setUp();

        WicketUtil.setUnitTesting(true);

        this.jndiTestUtils = new JndiTestUtils();
        this.jndiTestUtils.setUp();

        this.mockEncapDeviceService = createMock(EncapDeviceService.class);
        this.mockSamlAuthorityService = createMock(SamlAuthorityService.class);
        this.mockHelpdeskManager = createMock(HelpdeskManager.class);

        // Initialize MBean's
        JmxTestUtils jmxTestUtils = new JmxTestUtils();
        jmxTestUtils.setUp(AuthIdentityServiceClient.AUTH_IDENTITY_SERVICE);

        final KeyPair authKeyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate authCertificate = PkiTestUtils.generateSelfSignedCertificate(authKeyPair, "CN=Test");
        jmxTestUtils.registerActionHandler(AuthIdentityServiceClient.AUTH_IDENTITY_SERVICE, "getCertificate", new MBeanActionHandler() {

            public Object invoke(@SuppressWarnings("unused") Object[] arguments) {

                return authCertificate;
            }
        });

        jmxTestUtils.setUp(IdentityServiceClient.IDENTITY_SERVICE);

        final KeyPair keyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");
        jmxTestUtils.registerActionHandler(IdentityServiceClient.IDENTITY_SERVICE, "getCertificate", new MBeanActionHandler() {

            public Object invoke(@SuppressWarnings("unused") Object[] arguments) {

                return certificate;
            }
        });

        this.wicket = new WicketTester(new EncapTestApplication());
        this.wicket.processRequestCycle();

    }

    @Override
    @After
    public void tearDown()
            throws Exception {

        this.jndiTestUtils.tearDown();
    }

    @Test
    public void testAuthenticate()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String mobile = "0523012295";
        String challenge = "0123456789";
        String otp = "000000";

        // Authentication Page: Verify.
        this.wicket.startPage(AuthenticationPage.class);
        this.wicket.assertRenderedPage(AuthenticationPage.class);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID, Form.class);

        // setup
        EJBTestUtils.inject(this.wicket.getLastRenderedPage(), this.mockEncapDeviceService);
        EJBTestUtils.inject(this.wicket.getLastRenderedPage(), this.mockSamlAuthorityService);

        // stubs
        expect(this.mockSamlAuthorityService.getAuthnAssertionValidity()).andStubReturn(Integer.MAX_VALUE);
        expect(this.mockEncapDeviceService.authenticate(mobile, challenge, otp)).andStubReturn(userId);
        expect(this.mockEncapDeviceService.requestOTP(mobile)).andStubReturn(challenge);

        // prepare
        replay(this.mockEncapDeviceService, this.mockSamlAuthorityService);

        // Send OTP, get challenge.
        FormTester authenticationForm = this.wicket
                                                   .newFormTester(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID);
        authenticationForm.setValue(AuthenticationPage.MOBILE_FIELD_ID, mobile);
        authenticationForm.submit(AuthenticationPage.CHALLENGE_BUTTON_ID);

        // Authentication Page: Verify.
        this.wicket.startPage(AuthenticationPage.class);
        this.wicket.assertRenderedPage(AuthenticationPage.class);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID, Form.class);

        // Verify OTP with challenge.
        authenticationForm = this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID);
        authenticationForm.setValue(AuthenticationPage.OTP_FIELD_ID, otp);
        authenticationForm.submit(AuthenticationPage.LOGIN_BUTTON_ID);

        // verify
        verify(this.mockEncapDeviceService, this.mockSamlAuthorityService);
    }

    @Test
    public void testAuthenticateSubjectNotFound()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String token = "000000";
        DummyNameIdentifierMappingClient.setUserId(userId);

        // Authentication Page: Verify.
        AuthenticationPage authenticationPage = (AuthenticationPage) this.wicket.startPage(AuthenticationPage.class);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID, Form.class);

        // setup
        EJBTestUtils.inject(authenticationPage, this.mockEncapDeviceService);
        this.jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, this.mockHelpdeskManager);

        // stubs
        expect(this.mockEncapDeviceService.authenticate(userId, token)).andThrow(new SubjectNotFoundException());
        expect(this.mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(this.mockEncapDeviceService, this.mockHelpdeskManager);

        // operate
        FormTester authenticationForm = this.wicket
                                                   .newFormTester(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID);
        authenticationForm.setValue(AuthenticationPage.MOBILE_FIELD_ID, UUID.randomUUID().toString());
        authenticationForm.setValue(AuthenticationPage.OTP_FIELD_ID, token);
        authenticationForm.submit(AuthenticationPage.LOGIN_BUTTON_ID);

        // verify
        verify(this.mockEncapDeviceService, this.mockHelpdeskManager);

        this.wicket.assertRenderedPage(AuthenticationPage.class);
        this.wicket.assertErrorMessages(new String[] { "encapNotRegistered" });

    }

    @Test
    public void testAuthenticateDeviceDisabled()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String token = "000000";
        DummyNameIdentifierMappingClient.setUserId(userId);

        // Authentication Page: Verify.
        AuthenticationPage authenticationPage = (AuthenticationPage) this.wicket.startPage(AuthenticationPage.class);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID, Form.class);

        // setup
        EJBTestUtils.inject(authenticationPage, this.mockEncapDeviceService);
        this.jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, this.mockHelpdeskManager);

        // stubs
        expect(this.mockEncapDeviceService.authenticate(userId, token)).andThrow(new DeviceDisabledException());
        expect(this.mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(this.mockEncapDeviceService, this.mockHelpdeskManager);

        // operate
        FormTester authenticationForm = this.wicket
                                                   .newFormTester(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID);
        authenticationForm.setValue(AuthenticationPage.MOBILE_FIELD_ID, UUID.randomUUID().toString());
        authenticationForm.setValue(AuthenticationPage.OTP_FIELD_ID, token);
        authenticationForm.submit(AuthenticationPage.LOGIN_BUTTON_ID);

        // verify
        verify(this.mockEncapDeviceService, this.mockHelpdeskManager);

        this.wicket.assertRenderedPage(AuthenticationPage.class);
        this.wicket.assertErrorMessages(new String[] { "encapDisabled" });

    }

    @Test
    public void testAuthenticateFailed()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String token = "000000";
        DummyNameIdentifierMappingClient.setUserId(userId);

        // Authentication Page: Verify.
        AuthenticationPage authenticationPage = (AuthenticationPage) this.wicket.startPage(AuthenticationPage.class);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID, Form.class);

        // setup
        EJBTestUtils.inject(authenticationPage, this.mockEncapDeviceService);
        this.jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, this.mockHelpdeskManager);

        // stubs
        expect(this.mockEncapDeviceService.authenticate(userId, token)).andStubReturn(null);
        expect(this.mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(this.mockEncapDeviceService, this.mockHelpdeskManager);

        // operate
        FormTester authenticationForm = this.wicket
                                                   .newFormTester(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID);
        authenticationForm.setValue(AuthenticationPage.MOBILE_FIELD_ID, UUID.randomUUID().toString());
        authenticationForm.setValue(AuthenticationPage.OTP_FIELD_ID, token);
        authenticationForm.submit(AuthenticationPage.LOGIN_BUTTON_ID);

        // verify
        verify(this.mockEncapDeviceService, this.mockHelpdeskManager);

        this.wicket.assertRenderedPage(AuthenticationPage.class);
        this.wicket.assertErrorMessages(new String[] { "authenticationFailedMsg" });

    }
}
