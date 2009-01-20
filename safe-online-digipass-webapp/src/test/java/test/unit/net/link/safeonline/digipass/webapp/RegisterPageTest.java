package test.unit.net.link.safeonline.digipass.webapp;

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
import net.link.safeonline.digipass.webapp.MainPage;
import net.link.safeonline.digipass.webapp.RegisterPage;
import net.link.safeonline.digipass.webapp.RemovePage;
import net.link.safeonline.model.digipass.DigipassDeviceService;
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

    private DigipassDeviceService mockDigipassDeviceService;

    private WicketTester          wicket;


    @Override
    @Before
    public void setUp()
            throws Exception {

        super.setUp();

        WicketUtil.setUnitTesting(true);

        mockDigipassDeviceService = createMock(DigipassDeviceService.class);

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

        wicket = new WicketTester(new DigipassTestApplication());
        wicket.processRequestCycle();

    }

    @Test
    public void testRegisterDigipass()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String serialNumber = "12345678";
        DummyNameIdentifierMappingClient.setUserId(userId);

        // MainPage: Verify.
        wicket.assertRenderedPage(MainPage.class);

        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + MainPage.REGISTER_ID, RegisterPage.class);
        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + MainPage.REMOVE_ID, RemovePage.class);

        // MainPage: Click to register digipass
        wicket.clickLink(TemplatePage.CONTENT_ID + ":" + MainPage.REGISTER_ID);

        // RegisterPage: Verify.
        wicket.assertRenderedPage(RegisterPage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + RegisterPage.REGISTER_FORM_ID, Form.class);

        // setup
        RegisterPage registerPage = (RegisterPage) wicket.getLastRenderedPage();
        EJBTestUtils.inject(registerPage, mockDigipassDeviceService);

        // stubs
        expect(mockDigipassDeviceService.register(userId, serialNumber)).andStubReturn(userId);

        // prepare
        replay(mockDigipassDeviceService);

        // RegisterPage: Register digipass for user
        FormTester registerForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + RegisterPage.REGISTER_FORM_ID);
        registerForm.setValue(RegisterPage.LOGIN_FIELD_ID, UUID.randomUUID().toString());
        registerForm.setValue(RegisterPage.SERIALNUMBER_FIELD_ID, serialNumber);
        registerForm.submit(RegisterPage.REGISTER_BUTTON_ID);

        // verify
        verify(mockDigipassDeviceService);

        wicket.assertRenderedPage(MainPage.class);
    }

    @Test
    public void testRegisterDigipassSubjectNotFound()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String serialNumber = "12345678";
        DummyNameIdentifierMappingClient.setUserId(userId);

        // MainPage: Verify.
        wicket.assertRenderedPage(MainPage.class);

        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + MainPage.REGISTER_ID, RegisterPage.class);
        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + MainPage.REMOVE_ID, RemovePage.class);

        // MainPage: Click to register digipass
        wicket.clickLink(TemplatePage.CONTENT_ID + ":" + MainPage.REGISTER_ID);

        // RegisterPage: Verify.
        wicket.assertRenderedPage(RegisterPage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + RegisterPage.REGISTER_FORM_ID, Form.class);

        // Setup
        RegisterPage registerPage = (RegisterPage) wicket.getLastRenderedPage();
        EJBTestUtils.inject(registerPage, mockDigipassDeviceService);

        // Stubs
        expect(mockDigipassDeviceService.register(userId, serialNumber)).andThrow(new SubjectNotFoundException());

        // Prepare
        replay(mockDigipassDeviceService);

        // RegisterPage: Register digipass for user
        FormTester registerForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + RegisterPage.REGISTER_FORM_ID);
        registerForm.setValue(RegisterPage.LOGIN_FIELD_ID, UUID.randomUUID().toString());
        registerForm.setValue(RegisterPage.SERIALNUMBER_FIELD_ID, serialNumber);
        registerForm.submit(RegisterPage.REGISTER_BUTTON_ID);

        // verify
        verify(mockDigipassDeviceService);

        wicket.assertRenderedPage(RegisterPage.class);
        wicket.assertErrorMessages(new String[] { "errorSubjectNotFound" });
    }

    @Test
    public void testRegisterDigipassAlreadyRegistered()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String serialNumber = "12345678";
        DummyNameIdentifierMappingClient.setUserId(userId);

        // MainPage: Verify.
        wicket.assertRenderedPage(MainPage.class);

        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + MainPage.REGISTER_ID, RegisterPage.class);
        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + MainPage.REMOVE_ID, RemovePage.class);

        // MainPage: Click to register digipass
        wicket.clickLink(TemplatePage.CONTENT_ID + ":" + MainPage.REGISTER_ID);

        // RegisterPage: Verify.
        wicket.assertRenderedPage(RegisterPage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + RegisterPage.REGISTER_FORM_ID, Form.class);

        // Setup
        RegisterPage registerPage = (RegisterPage) wicket.getLastRenderedPage();
        EJBTestUtils.inject(registerPage, mockDigipassDeviceService);

        // Stubs
        expect(mockDigipassDeviceService.register(userId, serialNumber)).andThrow(new ArgumentIntegrityException());

        // Prepare
        replay(mockDigipassDeviceService);

        // RegisterPage: Register digipass for user
        FormTester registerForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + RegisterPage.REGISTER_FORM_ID);
        registerForm.setValue(RegisterPage.LOGIN_FIELD_ID, UUID.randomUUID().toString());
        registerForm.setValue(RegisterPage.SERIALNUMBER_FIELD_ID, serialNumber);
        registerForm.submit(RegisterPage.REGISTER_BUTTON_ID);

        // verify
        verify(mockDigipassDeviceService);

        wicket.assertRenderedPage(RegisterPage.class);
        wicket.assertErrorMessages(new String[] { "errorDigipassRegistered" });
    }
}
