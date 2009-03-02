package test.unit.net.link.safeonline.digipass.webapp;

import static org.easymock.EasyMock.checkOrder;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.UUID;

import junit.framework.TestCase;
import net.link.safeonline.authentication.exception.DeviceAuthenticationException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.AuthenticationContext;
import net.link.safeonline.digipass.webapp.AuthenticationPage;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.helpdesk.HelpdeskManager;
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


public class AuthenticationPageTest extends TestCase {

    private DigipassDeviceService     mockDigipassDeviceService;

    private SamlAuthorityService      mockSamlAuthorityService;

    private HelpdeskManager           mockHelpdeskManager;

    private WicketTester              wicket;

    private JndiTestUtils             jndiTestUtils;

    private KeyService                mockKeyService;

    private NodeAuthenticationService mockNodeAuthenticationService;


    @Override
    @Before
    public void setUp()
            throws Exception {

        super.setUp();

        WicketUtil.setUnitTesting(true);

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();

        mockNodeAuthenticationService = createMock(NodeAuthenticationService.class);
        mockDigipassDeviceService = createMock(DigipassDeviceService.class);
        mockSamlAuthorityService = createMock(SamlAuthorityService.class);
        mockHelpdeskManager = createMock(HelpdeskManager.class);
        mockKeyService = createMock(KeyService.class);

        // Initialize MBean's
        final KeyPair nodeKeyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate nodeCertificate = PkiTestUtils.generateSelfSignedCertificate(nodeKeyPair, "CN=Test");
        expect(mockKeyService.getPrivateKeyEntry(SafeOnlineNodeKeyStore.class)).andReturn(
                new PrivateKeyEntry(nodeKeyPair.getPrivate(), new Certificate[] { nodeCertificate }));

        checkOrder(mockKeyService, false);
        replay(mockKeyService);

        jndiTestUtils.bindComponent(KeyService.JNDI_BINDING, mockKeyService);

        wicket = new WicketTester(new DigipassTestApplication());
        wicket.processRequestCycle();
    }

    @Override
    @After
    public void tearDown()
            throws Exception {

        jndiTestUtils.tearDown();
    }

    @Test
    public void testAuthenticate()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String token = "000000";
        DummyNameIdentifierMappingClient.setUserId(userId);

        // Authentication Page: Verify.
        AuthenticationPage authenticationPage = (AuthenticationPage) wicket.startPage(AuthenticationPage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID, Form.class);

        // setup
        EJBTestUtils.inject(authenticationPage, mockDigipassDeviceService);
        EJBTestUtils.inject(authenticationPage, mockSamlAuthorityService);
        EJBTestUtils.inject(authenticationPage, mockNodeAuthenticationService);

        // stubs
        mockDigipassDeviceService.authenticate(userId, token);
        expect(mockSamlAuthorityService.getAuthnAssertionValidity()).andStubReturn(Integer.MAX_VALUE);
        expect(mockNodeAuthenticationService.getLocalNode()).andReturn(new NodeEntity("Test", null, null, 0, 0, null));

        // prepare
        replay(mockDigipassDeviceService, mockSamlAuthorityService, mockNodeAuthenticationService);

        // RegisterPage: Register digipass for user
        FormTester authenticationForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID);
        authenticationForm.setValue(AuthenticationPage.LOGIN_NAME_FIELD_ID, UUID.randomUUID().toString());
        authenticationForm.setValue(AuthenticationPage.TOKEN_FIELD_ID, token);
        authenticationForm.submit(AuthenticationPage.LOGIN_BUTTON_ID);

        // verify
        verify(mockDigipassDeviceService, mockSamlAuthorityService);
        assertNotNull("No authenticated user on the session.", //
                AuthenticationContext.getAuthenticationContext(wicket.getServletSession()).getUserId());
    }

    @Test
    public void testAuthenticateSubjectNotFound()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String token = "000000";
        DummyNameIdentifierMappingClient.setUserId(userId);

        // Authentication Page: Verify.
        AuthenticationPage authenticationPage = (AuthenticationPage) wicket.startPage(AuthenticationPage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID, Form.class);

        // setup
        EJBTestUtils.inject(authenticationPage, mockDigipassDeviceService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);

        // stubs
        mockDigipassDeviceService.authenticate(userId, token);
        expectLastCall().andThrow(new SubjectNotFoundException());
        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockDigipassDeviceService, mockHelpdeskManager);

        // operate
        FormTester authenticationForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID);
        authenticationForm.setValue(AuthenticationPage.LOGIN_NAME_FIELD_ID, UUID.randomUUID().toString());
        authenticationForm.setValue(AuthenticationPage.TOKEN_FIELD_ID, token);
        authenticationForm.submit(AuthenticationPage.LOGIN_BUTTON_ID);

        // verify
        verify(mockDigipassDeviceService, mockHelpdeskManager);

        wicket.assertRenderedPage(AuthenticationPage.class);
        wicket.assertErrorMessages(new String[] { "digipassNotRegistered" });
        assertNull("There was an authenticated user on the session.", //
                AuthenticationContext.getAuthenticationContext(wicket.getServletSession()).getUserId());
    }

    @Test
    public void testAuthenticateDeviceDisabled()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String token = "000000";
        DummyNameIdentifierMappingClient.setUserId(userId);

        // Authentication Page: Verify.
        AuthenticationPage authenticationPage = (AuthenticationPage) wicket.startPage(AuthenticationPage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID, Form.class);

        // setup
        EJBTestUtils.inject(authenticationPage, mockDigipassDeviceService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);

        // stubs
        mockDigipassDeviceService.authenticate(userId, token);
        expectLastCall().andThrow(new DeviceDisabledException());
        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockDigipassDeviceService, mockHelpdeskManager);

        // operate
        FormTester authenticationForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID);
        authenticationForm.setValue(AuthenticationPage.LOGIN_NAME_FIELD_ID, UUID.randomUUID().toString());
        authenticationForm.setValue(AuthenticationPage.TOKEN_FIELD_ID, token);
        authenticationForm.submit(AuthenticationPage.LOGIN_BUTTON_ID);

        // verify
        verify(mockDigipassDeviceService, mockHelpdeskManager);

        wicket.assertRenderedPage(AuthenticationPage.class);
        wicket.assertErrorMessages(new String[] { "digipassDisabled" });
        assertNull("There was an authenticated user on the session.", //
                AuthenticationContext.getAuthenticationContext(wicket.getServletSession()).getUserId());
    }

    @Test
    public void testAuthenticateFailed()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String token = "000000";
        DummyNameIdentifierMappingClient.setUserId(userId);

        // Authentication Page: Verify.
        AuthenticationPage authenticationPage = (AuthenticationPage) wicket.startPage(AuthenticationPage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID, Form.class);

        // setup
        EJBTestUtils.inject(authenticationPage, mockDigipassDeviceService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);

        // stubs
        mockDigipassDeviceService.authenticate(userId, token);
        expectLastCall().andThrow(new DeviceAuthenticationException());
        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockDigipassDeviceService, mockHelpdeskManager);

        // operate
        FormTester authenticationForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID);
        authenticationForm.setValue(AuthenticationPage.LOGIN_NAME_FIELD_ID, UUID.randomUUID().toString());
        authenticationForm.setValue(AuthenticationPage.TOKEN_FIELD_ID, token);
        authenticationForm.submit(AuthenticationPage.LOGIN_BUTTON_ID);

        // verify
        verify(mockDigipassDeviceService, mockHelpdeskManager);

        wicket.assertRenderedPage(AuthenticationPage.class);
        wicket.assertErrorMessages(new String[] { "authenticationFailedMsg" });
        assertNull("There was an authenticated user on the session.", //
                AuthenticationContext.getAuthenticationContext(wicket.getServletSession()).getUserId());
    }
}
