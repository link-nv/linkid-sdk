package test.unit.net.link.safeonline.digipass.webapp;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.digipass.webapp.MainPage;
import net.link.safeonline.digipass.webapp.RegisterPage;
import net.link.safeonline.digipass.webapp.RemovePage;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.keystore.service.KeyService;
import net.link.safeonline.model.digipass.DigipassConstants;
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
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class RemovePageTest {

    private DigipassDeviceService mockDigipassDeviceService;

    private WicketTester          wicket;

    private KeyService            mockKeyService;

    private JndiTestUtils         jndiTestUtils;


    @Before
    public void setUp()
            throws Exception {

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.setNamingStrategy(new FieldNamingStrategy());

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
    public void testRemoveDigipass()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String serialNumber = "12345678";
        DummyNameIdentifierMappingClient.setUserId(userId);

        List<AttributeDO> digipassAttributes = new LinkedList<AttributeDO>();
        AttributeDO digipass = new AttributeDO(DigipassConstants.DIGIPASS_DEVICE_ATTRIBUTE, DatatypeType.STRING);
        digipass.setStringValue(serialNumber);
        digipassAttributes.add(digipass);

        // MainPage: Verify.
        wicket.assertRenderedPage(MainPage.class);

        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + MainPage.REGISTER_ID, RegisterPage.class);
        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + MainPage.REMOVE_ID, RemovePage.class);

        // MainPage: Click to remove digipass
        wicket.clickLink(TemplatePage.CONTENT_ID + ":" + MainPage.REMOVE_ID);

        // RemovePage: Verify.
        wicket.assertRenderedPage(RemovePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + RemovePage.GET_FORM_ID, Form.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + RemovePage.LIST_FORM_ID);

        // setup
        RemovePage removePage = (RemovePage) wicket.getLastRenderedPage();
        EJBTestUtils.inject(removePage, mockDigipassDeviceService);

        // stubs
        expect(mockDigipassDeviceService.getDigipasses(userId, removePage.getLocale())).andStubReturn(digipassAttributes);

        // prepare
        replay(mockDigipassDeviceService);

        // RemovePage: retrieve digipass for user
        FormTester getForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + RemovePage.GET_FORM_ID);
        getForm.setValue(RemovePage.LOGIN_FIELD_ID, UUID.randomUUID().toString());
        getForm.submit(RemovePage.VIEW_BUTTON_ID);

        // verify
        verify(mockDigipassDeviceService);

        // RemovePage: Verify
        wicket.assertRenderedPage(RemovePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + RemovePage.LIST_FORM_ID, Form.class);
        wicket.assertListView(TemplatePage.CONTENT_ID + ":" + RemovePage.LIST_FORM_ID + ":" + RemovePage.DIGIPASS_LIST_ID,
                digipassAttributes);

        // setup
        EasyMock.reset(mockDigipassDeviceService);

        // stubs
        mockDigipassDeviceService.remove(serialNumber);

        // prepare
        replay(mockDigipassDeviceService);

        // RemovePage: remove digipass
        wicket.clickLink(TemplatePage.CONTENT_ID + ":" + RemovePage.LIST_FORM_ID + ":" + RemovePage.DIGIPASS_LIST_ID + ":0:"
                + RemovePage.REMOVE_LINK_ID);

        // verify
        verify(mockDigipassDeviceService);

        wicket.assertRenderedPage(MainPage.class);
    }

    @Test
    public void testRemoveDigipassSubjectNotFound()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        DummyNameIdentifierMappingClient.setUserId(userId);

        // MainPage: Verify.
        wicket.assertRenderedPage(MainPage.class);

        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + MainPage.REGISTER_ID, RegisterPage.class);
        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + MainPage.REMOVE_ID, RemovePage.class);

        // MainPage: Click to remove digipass
        wicket.clickLink(TemplatePage.CONTENT_ID + ":" + MainPage.REMOVE_ID);

        // RemovePage: Verify.
        wicket.assertRenderedPage(RemovePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + RemovePage.GET_FORM_ID, Form.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + RemovePage.LIST_FORM_ID);

        // setup
        RemovePage removePage = (RemovePage) wicket.getLastRenderedPage();
        EJBTestUtils.inject(removePage, mockDigipassDeviceService);

        // stubs
        expect(mockDigipassDeviceService.getDigipasses(userId, removePage.getLocale())).andThrow(new SubjectNotFoundException());

        // prepare
        replay(mockDigipassDeviceService);

        // RemovePage: retrieve digipass for user
        FormTester getForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + RemovePage.GET_FORM_ID);
        getForm.setValue(RemovePage.LOGIN_FIELD_ID, UUID.randomUUID().toString());
        getForm.submit(RemovePage.VIEW_BUTTON_ID);

        // verify
        verify(mockDigipassDeviceService);

        // RemovePage: Verify
        wicket.assertRenderedPage(RemovePage.class);
        wicket.assertErrorMessages(new String[] { "errorSubjectNotFound" });
    }

    @Test
    public void testRemoveDigipassNoRegistrationsFound()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        DummyNameIdentifierMappingClient.setUserId(userId);

        // MainPage: Verify.
        wicket.assertRenderedPage(MainPage.class);

        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + MainPage.REGISTER_ID, RegisterPage.class);
        wicket.assertPageLink(TemplatePage.CONTENT_ID + ":" + MainPage.REMOVE_ID, RemovePage.class);

        // MainPage: Click to remove digipass
        wicket.clickLink(TemplatePage.CONTENT_ID + ":" + MainPage.REMOVE_ID);

        // RemovePage: Verify.
        wicket.assertRenderedPage(RemovePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + RemovePage.GET_FORM_ID, Form.class);
        wicket.assertInvisible(TemplatePage.CONTENT_ID + ":" + RemovePage.LIST_FORM_ID);

        // setup
        RemovePage removePage = (RemovePage) wicket.getLastRenderedPage();
        EJBTestUtils.inject(removePage, mockDigipassDeviceService);

        // stubs
        expect(mockDigipassDeviceService.getDigipasses(userId, removePage.getLocale())).andStubReturn(new LinkedList<AttributeDO>());

        // prepare
        replay(mockDigipassDeviceService);

        // RemovePage: retrieve digipass for user
        FormTester getForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + RemovePage.GET_FORM_ID);
        getForm.setValue(RemovePage.LOGIN_FIELD_ID, UUID.randomUUID().toString());
        getForm.submit(RemovePage.VIEW_BUTTON_ID);

        // verify
        verify(mockDigipassDeviceService);

        // RemovePage: Verify
        wicket.assertRenderedPage(RemovePage.class);
        wicket.assertErrorMessages(new String[] { "errorNoDeviceRegistrationsFound" });
    }
}
