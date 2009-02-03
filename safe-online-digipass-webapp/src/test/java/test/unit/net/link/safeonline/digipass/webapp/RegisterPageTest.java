package test.unit.net.link.safeonline.digipass.webapp;

import static org.easymock.EasyMock.checkOrder;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.UUID;

import net.link.safeonline.authentication.exception.ArgumentIntegrityException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.digipass.webapp.MainPage;
import net.link.safeonline.digipass.webapp.RegisterPage;
import net.link.safeonline.digipass.webapp.RemovePage;
import net.link.safeonline.keystore.SafeOnlineKeyStore;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.keystore.service.KeyService;
import net.link.safeonline.model.digipass.DigipassDeviceService;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.tools.WicketUtil;
import net.link.safeonline.wicket.tools.olas.DummyNameIdentifierMappingClient;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class RegisterPageTest {

    private DigipassDeviceService mockDigipassDeviceService;

    private WicketTester          wicket;

    private KeyService            mockKeyService;

    private JndiTestUtils         jndiTestUtils;


    @Before
    public void setUp()
            throws Exception {

        WicketUtil.setUnitTesting(true);

        mockDigipassDeviceService = createMock(DigipassDeviceService.class);
        mockKeyService = createMock(KeyService.class);

        final KeyPair nodeKeyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate nodeCertificate = PkiTestUtils.generateSelfSignedCertificate(nodeKeyPair, "CN=Test");
        expect(mockKeyService.getPrivateKeyEntry(SafeOnlineNodeKeyStore.class)).andReturn(
                new PrivateKeyEntry(nodeKeyPair.getPrivate(), new Certificate[] { nodeCertificate }));

        final KeyPair olasKeyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate olasCertificate = PkiTestUtils.generateSelfSignedCertificate(olasKeyPair, "CN=Test");
        expect(mockKeyService.getPrivateKeyEntry(SafeOnlineKeyStore.class)).andReturn(
                new PrivateKeyEntry(olasKeyPair.getPrivate(), new Certificate[] { olasCertificate }));

        checkOrder(mockKeyService, false);
        replay(mockKeyService);

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.bindComponent(KeyService.JNDI_BINDING, mockKeyService);

        wicket = new WicketTester(new DigipassTestApplication());
        wicket.processRequestCycle();
    }

    @After
    public void tearDown()
            throws Exception {

        jndiTestUtils.tearDown();
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
