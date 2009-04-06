package test.unit.net.link.safeonline.digipass.webapp;

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
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.digipass.webapp.MainPage;
import net.link.safeonline.digipass.webapp.RegistrationPage;
import net.link.safeonline.digipass.webapp.RemovePage;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.keystore.service.KeyService;
import net.link.safeonline.model.digipass.DigipassDeviceService;
import net.link.safeonline.sdk.test.DummyNameIdentifierMappingClient;
import net.link.safeonline.sdk.test.DummyServiceFactory;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.util.ee.FieldNamingStrategy;
import net.link.safeonline.webapp.template.TemplatePage;

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

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setNamingStrategy(new FieldNamingStrategy());
        jndiTestUtils.setUp();

        mockDigipassDeviceService = createMock(DigipassDeviceService.class);
        mockKeyService = createMock(KeyService.class);
        jndiTestUtils.bindComponent(KeyService.class, mockKeyService);

        KeyPair nodeKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate nodeCertificate = PkiTestUtils.generateSelfSignedCertificate(nodeKeyPair, "CN=Test");
        expect(mockKeyService.getPrivateKeyEntry(SafeOnlineNodeKeyStore.class)).andReturn(
                new PrivateKeyEntry(nodeKeyPair.getPrivate(), new Certificate[] { nodeCertificate })).anyTimes();
        replay(mockKeyService);

        DummyServiceFactory.install();

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

        // ProtocolContext protocolContext = ProtocolContext.getProtocolContext(wicket.getServletSession());
        // protocolContext.setSubject(userId);
        // protocolContext.setNodeName(nodeName);
        //
        // MainPage: Verify.
        wicket.assertRenderedPage(MainPage.class);

        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + MainPage.REGISTER_ID, RegistrationPage.class);
        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + MainPage.REMOVE_ID, RemovePage.class);

        // MainPage: Click to register digipass
        wicket.clickLink(TemplatePage.CONTENT_ID + ":" + MainPage.REGISTER_ID);

        // RegistrationPage: Verify.
        wicket.assertRenderedPage(RegistrationPage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + RegistrationPage.REGISTER_FORM_ID, Form.class);

        // setup
        RegistrationPage registerPage = (RegistrationPage) wicket.getLastRenderedPage();
        EJBTestUtils.inject(registerPage, mockDigipassDeviceService);

        // stubs
        expect(mockDigipassDeviceService.register(userId, serialNumber)).andStubReturn(userId);

        // prepare
        replay(mockDigipassDeviceService);

        // RegistrationPage: Register digipass for user
        FormTester registerForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + RegistrationPage.REGISTER_FORM_ID);
        registerForm.setValue(RegistrationPage.LOGIN_FIELD_ID, UUID.randomUUID().toString());
        registerForm.setValue(RegistrationPage.SERIALNUMBER_FIELD_ID, serialNumber);
        registerForm.submit(RegistrationPage.REGISTER_BUTTON_ID);

        // verify
        verify(mockDigipassDeviceService);

        wicket.assertRenderedPage(MainPage.class);
    }

    @Test
    public void testRegisterDigipassNodeNotFound()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String serialNumber = "12345678";
        DummyNameIdentifierMappingClient.setUserId(userId);

        // ProtocolContext protocolContext = ProtocolContext.getProtocolContext(wicket.getServletSession());
        // protocolContext.setSubject(userId);
        // protocolContext.setNodeName(nodeName);

        // MainPage: Verify.
        wicket.assertRenderedPage(MainPage.class);

        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + MainPage.REGISTER_ID, RegistrationPage.class);
        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + MainPage.REMOVE_ID, RemovePage.class);

        // MainPage: Click to register digipass
        wicket.clickLink(TemplatePage.CONTENT_ID + ":" + MainPage.REGISTER_ID);

        // RegistrationPage: Verify.
        wicket.assertRenderedPage(RegistrationPage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + RegistrationPage.REGISTER_FORM_ID, Form.class);

        // Setup
        RegistrationPage registerPage = (RegistrationPage) wicket.getLastRenderedPage();
        EJBTestUtils.inject(registerPage, mockDigipassDeviceService);

        // Stubs
        expect(mockDigipassDeviceService.register(userId, serialNumber)).andThrow(new NodeNotFoundException());

        // Prepare
        replay(mockDigipassDeviceService);

        // RegistrationPage: Register digipass for user
        FormTester registerForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + RegistrationPage.REGISTER_FORM_ID);
        registerForm.setValue(RegistrationPage.LOGIN_FIELD_ID, UUID.randomUUID().toString());
        registerForm.setValue(RegistrationPage.SERIALNUMBER_FIELD_ID, serialNumber);
        registerForm.submit(RegistrationPage.REGISTER_BUTTON_ID);

        // verify
        verify(mockDigipassDeviceService);

        wicket.assertRenderedPage(RegistrationPage.class);
        wicket.assertErrorMessages(new String[] { "errorNodeNotFound" });
    }

    @Test
    public void testRegisterDigipassAlreadyRegistered()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String serialNumber = "12345678";
        DummyNameIdentifierMappingClient.setUserId(userId);
        // ProtocolContext protocolContext = ProtocolContext.getProtocolContext(wicket.getServletSession());
        // protocolContext.setSubject(userId);
        // protocolContext.setNodeName(nodeName);

        // MainPage: Verify.
        wicket.assertRenderedPage(MainPage.class);

        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + MainPage.REGISTER_ID, RegistrationPage.class);
        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + MainPage.REMOVE_ID, RemovePage.class);

        // MainPage: Click to register digipass
        wicket.clickLink(TemplatePage.CONTENT_ID + ":" + MainPage.REGISTER_ID);

        // RegistrationPage: Verify.
        wicket.assertRenderedPage(RegistrationPage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + RegistrationPage.REGISTER_FORM_ID, Form.class);

        // Setup
        RegistrationPage registerPage = (RegistrationPage) wicket.getLastRenderedPage();
        EJBTestUtils.inject(registerPage, mockDigipassDeviceService);

        // Stubs
        expect(mockDigipassDeviceService.register(userId, serialNumber)).andThrow(new ArgumentIntegrityException());

        // Prepare
        replay(mockDigipassDeviceService);

        // RegistrationPage: Register digipass for user
        FormTester registerForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + RegistrationPage.REGISTER_FORM_ID);
        registerForm.setValue(RegistrationPage.LOGIN_FIELD_ID, UUID.randomUUID().toString());
        registerForm.setValue(RegistrationPage.SERIALNUMBER_FIELD_ID, serialNumber);
        registerForm.submit(RegistrationPage.REGISTER_BUTTON_ID);

        // verify
        verify(mockDigipassDeviceService);

        wicket.assertRenderedPage(RegistrationPage.class);
        wicket.assertErrorMessages(new String[] { "errorDigipassRegistered" });
    }
}
