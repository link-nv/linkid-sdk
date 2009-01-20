package test.unit.net.link.safeonline.encap.webapp;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.UUID;

import junit.framework.TestCase;
import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.encap.webapp.MainPage;
import net.link.safeonline.encap.webapp.RegisterPage;
import net.link.safeonline.encap.webapp.RemovePage;
import net.link.safeonline.model.encap.EncapDeviceService;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.JmxTestUtils;
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
import org.junit.Before;
import org.junit.Test;


public class RegisterPageTest extends TestCase {

    private EncapDeviceService mockEncapDeviceService;

    private WicketTester       wicket;


    @Override
    @Before
    public void setUp()
            throws Exception {

        super.setUp();

        WicketUtil.setUnitTesting(true);

        mockEncapDeviceService = createMock(EncapDeviceService.class);

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

        wicket = new WicketTester(new EncapTestApplication());
        wicket.processRequestCycle();

    }

    @Test
    public void testRegisterEncap()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String serialNumber = "12345678";
        DummyNameIdentifierMappingClient.setUserId(userId);

        // MainPage: Verify.
        wicket.assertRenderedPage(MainPage.class);

        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + MainPage.REGISTER_ID, RegisterPage.class);
        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + MainPage.REMOVE_ID, RemovePage.class);

        // MainPage: Click to register encap
        wicket.clickLink(TemplatePage.CONTENT_ID + ":" + MainPage.REGISTER_ID);

        // RegisterPage: Verify.
        wicket.assertRenderedPage(RegisterPage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + RegisterPage.REGISTER_FORM_ID, Form.class);

        // setup
        RegisterPage registerPage = (RegisterPage) wicket.getLastRenderedPage();
        EJBTestUtils.inject(registerPage, mockEncapDeviceService);

        // stubs
        expect(mockEncapDeviceService.register(userId, serialNumber)).andStubReturn(userId);

        // prepare
        replay(mockEncapDeviceService);

        // RegisterPage: Register encap for user
        FormTester registerForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + RegisterPage.REGISTER_FORM_ID);
        registerForm.setValue(RegisterPage.LOGIN_FIELD_ID, UUID.randomUUID().toString());
        registerForm.setValue(RegisterPage.SERIALNUMBER_FIELD_ID, serialNumber);
        registerForm.submit(RegisterPage.REGISTER_BUTTON_ID);

        // verify
        verify(mockEncapDeviceService);

        wicket.assertRenderedPage(MainPage.class);
    }

    @Test
    public void testRegisterEncapSubjectNotFound()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String serialNumber = "12345678";
        DummyNameIdentifierMappingClient.setUserId(userId);

        // MainPage: Verify.
        wicket.assertRenderedPage(MainPage.class);

        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + MainPage.REGISTER_ID, RegisterPage.class);
        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + MainPage.REMOVE_ID, RemovePage.class);

        // MainPage: Click to register encap
        wicket.clickLink(TemplatePage.CONTENT_ID + ":" + MainPage.REGISTER_ID);

        // RegisterPage: Verify.
        wicket.assertRenderedPage(RegisterPage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + RegisterPage.REGISTER_FORM_ID, Form.class);

        // Setup
        RegisterPage registerPage = (RegisterPage) wicket.getLastRenderedPage();
        EJBTestUtils.inject(registerPage, mockEncapDeviceService);

        // Stubs
        expect(mockEncapDeviceService.register(userId, serialNumber)).andThrow(new SubjectNotFoundException());

        // Prepare
        replay(mockEncapDeviceService);

        // RegisterPage: Register encap for user
        FormTester registerForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + RegisterPage.REGISTER_FORM_ID);
        registerForm.setValue(RegisterPage.LOGIN_FIELD_ID, UUID.randomUUID().toString());
        registerForm.setValue(RegisterPage.SERIALNUMBER_FIELD_ID, serialNumber);
        registerForm.submit(RegisterPage.REGISTER_BUTTON_ID);

        // verify
        verify(mockEncapDeviceService);

        wicket.assertRenderedPage(RegisterPage.class);
        wicket.assertErrorMessages(new String[] { "errorSubjectNotFound" });
    }

    @Test
    public void testRegisterEncapAlreadyRegistered()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String serialNumber = "12345678";
        DummyNameIdentifierMappingClient.setUserId(userId);

        // MainPage: Verify.
        wicket.assertRenderedPage(MainPage.class);

        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + MainPage.REGISTER_ID, RegisterPage.class);
        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + MainPage.REMOVE_ID, RemovePage.class);

        // MainPage: Click to register encap
        wicket.clickLink(TemplatePage.CONTENT_ID + ":" + MainPage.REGISTER_ID);

        // RegisterPage: Verify.
        wicket.assertRenderedPage(RegisterPage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + RegisterPage.REGISTER_FORM_ID, Form.class);

        // Setup
        RegisterPage registerPage = (RegisterPage) wicket.getLastRenderedPage();
        EJBTestUtils.inject(registerPage, mockEncapDeviceService);

        // Stubs
        expect(mockEncapDeviceService.register(userId, serialNumber)).andThrow(new ArgumentIntegrityException());

        // Prepare
        replay(mockEncapDeviceService);

        // RegisterPage: Register encap for user
        FormTester registerForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + RegisterPage.REGISTER_FORM_ID);
        registerForm.setValue(RegisterPage.LOGIN_FIELD_ID, UUID.randomUUID().toString());
        registerForm.setValue(RegisterPage.SERIALNUMBER_FIELD_ID, serialNumber);
        registerForm.submit(RegisterPage.REGISTER_BUTTON_ID);

        // verify
        verify(mockEncapDeviceService);

        wicket.assertRenderedPage(RegisterPage.class);
        wicket.assertErrorMessages(new String[] { "errorEncapRegistered" });
    }
}
