package test.unit.net.link.safeonline.password.webapp;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.UUID;

import net.link.safeonline.authentication.exception.DeviceAuthenticationException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.helpdesk.HelpdeskManager;
import net.link.safeonline.model.password.PasswordDeviceService;
import net.link.safeonline.password.webapp.EnablePage;
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


public class EnablePageTest {

    private PasswordDeviceService mockPasswordDeviceService;

    private SamlAuthorityService  mockSamlAuthorityService;

    private HelpdeskManager       mockHelpdeskManager;

    private WicketTester          wicket;

    private JndiTestUtils         jndiTestUtils;


    @Before
    public void setUp()
            throws Exception {

        WicketUtil.setUnitTesting(true);

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();

        mockPasswordDeviceService = createMock(PasswordDeviceService.class);
        mockSamlAuthorityService = createMock(SamlAuthorityService.class);
        mockHelpdeskManager = createMock(HelpdeskManager.class);

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

        wicket = new WicketTester(new PasswordTestApplication());

    }

    @After
    public void tearDown()
            throws Exception {

        jndiTestUtils.tearDown();
    }

    @Test
    public void testRemove()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String password = "test-password";
        DummyNameIdentifierMappingClient.setUserId(userId);

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(wicket.getServletSession());
        protocolContext.setSubject(userId);

        // Remove Page: Verify.
        EnablePage removalPage = (EnablePage) wicket.startPage(EnablePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID, Form.class);

        // setup
        EJBTestUtils.inject(removalPage, mockPasswordDeviceService);
        EJBTestUtils.inject(removalPage, mockSamlAuthorityService);

        // stubs
        mockPasswordDeviceService.enable(userId, password);
        expect(mockSamlAuthorityService.getAuthnAssertionValidity()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockPasswordDeviceService, mockSamlAuthorityService);

        // operate
        FormTester removalForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID);
        removalForm.setValue(EnablePage.PASSWORD_FIELD_ID, password);
        removalForm.submit(EnablePage.ENABLE_BUTTON_ID);

        // verify
        verify(mockPasswordDeviceService, mockSamlAuthorityService);
    }

    @Test
    public void testRemovePasswordsIncorrect()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String password = "test-password";
        DummyNameIdentifierMappingClient.setUserId(userId);

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(wicket.getServletSession());
        protocolContext.setSubject(userId);

        // Remove Page: Verify.
        EnablePage removalPage = (EnablePage) wicket.startPage(EnablePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID, Form.class);

        // setup
        EJBTestUtils.inject(removalPage, mockPasswordDeviceService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);

        // stubs
        mockPasswordDeviceService.enable(userId, password);
        expectLastCall().andThrow(new DeviceAuthenticationException());
        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockPasswordDeviceService, mockHelpdeskManager);

        // operate
        FormTester removalForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID);
        removalForm.setValue(EnablePage.PASSWORD_FIELD_ID, password);
        removalForm.submit(EnablePage.ENABLE_BUTTON_ID);

        // verify
        verify(mockPasswordDeviceService, mockHelpdeskManager);

        wicket.assertRenderedPage(EnablePage.class);
        wicket.assertErrorMessages(new String[] { "errorPasswordNotCorrect" });

    }
}
