package test.unit.net.link.safeonline.otpoversms.webapp;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.UUID;

import junit.framework.TestCase;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.demo.wicket.tools.WicketUtil;
import net.link.safeonline.demo.wicket.tools.olas.DummyNameIdentifierMappingClient;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.helpdesk.HelpdeskManager;
import net.link.safeonline.model.otpoversms.OtpOverSmsDeviceService;
import net.link.safeonline.otpoversms.webapp.RemovalPage;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.JmxTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.MBeanActionHandler;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import net.link.safeonline.util.ee.IdentityServiceClient;
import net.link.safeonline.webapp.template.TemplatePage;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class RemovalPageTest extends TestCase {

    private OtpOverSmsDeviceService mockOtpOverSmsDeviceService;

    private SamlAuthorityService    mockSamlAuthorityService;

    private HelpdeskManager         mockHelpdeskManager;

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

        this.wicket = new WicketTester(new OtpOverSmsTestApplication());

    }

    @Override
    @After
    public void tearDown()
            throws Exception {

        this.jndiTestUtils.tearDown();
    }

    @Test
    public void testRemove()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String mobile = "+32494575697";
        String pin = "0000";
        DummyNameIdentifierMappingClient.setUserId(userId);

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(this.wicket.getServletSession());
        protocolContext.setSubject(userId);
        protocolContext.setAttribute(mobile);

        // Remove Page: Verify.
        RemovalPage removalPage = (RemovalPage) this.wicket.startPage(RemovalPage.class);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + RemovalPage.REMOVAL_FORM_ID, Form.class);

        // setup
        EJBTestUtils.inject(removalPage, this.mockOtpOverSmsDeviceService);
        EJBTestUtils.inject(removalPage, this.mockSamlAuthorityService);

        // stubs
        expect(this.mockOtpOverSmsDeviceService.remove(userId, mobile, pin)).andStubReturn(true);
        expect(this.mockSamlAuthorityService.getAuthnAssertionValidity()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(this.mockOtpOverSmsDeviceService, this.mockSamlAuthorityService);

        // operate
        FormTester removalForm = this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + RemovalPage.REMOVAL_FORM_ID);
        removalForm.setValue(RemovalPage.PIN_FIELD_ID, pin);
        removalForm.submit(RemovalPage.REMOVE_BUTTON_ID);

        // verify
        verify(this.mockOtpOverSmsDeviceService, this.mockSamlAuthorityService);
    }

    @Test
    public void testRemovePinIncorrect()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String mobile = "+32494575697";
        String pin = "0000";
        DummyNameIdentifierMappingClient.setUserId(userId);

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(this.wicket.getServletSession());
        protocolContext.setSubject(userId);
        protocolContext.setAttribute(mobile);

        // Remove Page: Verify.
        RemovalPage removalPage = (RemovalPage) this.wicket.startPage(RemovalPage.class);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + RemovalPage.REMOVAL_FORM_ID, Form.class);

        // setup
        EJBTestUtils.inject(removalPage, this.mockOtpOverSmsDeviceService);
        this.jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, this.mockHelpdeskManager);

        // stubs
        expect(this.mockOtpOverSmsDeviceService.remove(userId, mobile, pin)).andStubReturn(false);
        expect(this.mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(this.mockOtpOverSmsDeviceService, this.mockHelpdeskManager);

        // operate
        FormTester removalForm = this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + RemovalPage.REMOVAL_FORM_ID);
        removalForm.setValue(RemovalPage.PIN_FIELD_ID, pin);
        removalForm.submit(RemovalPage.REMOVE_BUTTON_ID);

        // verify
        verify(this.mockOtpOverSmsDeviceService, this.mockHelpdeskManager);

        this.wicket.assertRenderedPage(RemovalPage.class);
        this.wicket.assertErrorMessages(new String[] { "errorPinNotCorrect" });
    }
}
