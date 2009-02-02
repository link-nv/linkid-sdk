package test.unit.net.link.safeonline.password.webapp;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.UUID;

import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.helpdesk.HelpdeskManager;
import net.link.safeonline.model.password.PasswordDeviceService;
import net.link.safeonline.password.webapp.AuthenticationPage;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.tools.WicketUtil;
import net.link.safeonline.wicket.tools.olas.DummyNameIdentifierMappingClient;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class AuthenticationPageTest {

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

        wicket = new WicketTester(new PasswordTestApplication());

    }

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
        String login = "test-login";
        String password = "test-password";
        DummyNameIdentifierMappingClient.setUserId(userId);

        // Authentication Page: Verify.
        AuthenticationPage authenticationPage = (AuthenticationPage) wicket.startPage(AuthenticationPage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID, Form.class);

        // setup
        EJBTestUtils.inject(authenticationPage, mockPasswordDeviceService);
        EJBTestUtils.inject(authenticationPage, mockSamlAuthorityService);

        // stubs
        expect(mockPasswordDeviceService.authenticate(userId, password)).andStubReturn(userId);
        expect(mockSamlAuthorityService.getAuthnAssertionValidity()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockPasswordDeviceService, mockSamlAuthorityService);

        // operate
        FormTester authenticationForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID);
        authenticationForm.setValue(AuthenticationPage.LOGIN_NAME_FIELD_ID, login);
        authenticationForm.setValue(AuthenticationPage.PASSWORD_FIELD_ID, password);
        authenticationForm.submit(AuthenticationPage.LOGIN_BUTTON_ID);

        // verify
        verify(mockPasswordDeviceService, mockSamlAuthorityService);
    }

    @Test
    public void testAuthenticateSubjectNotFound()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String login = "test-login";
        String password = "test-password";
        DummyNameIdentifierMappingClient.setUserId(userId);

        // Authentication Page: Verify.
        AuthenticationPage authenticationPage = (AuthenticationPage) wicket.startPage(AuthenticationPage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID, Form.class);

        // setup
        EJBTestUtils.inject(authenticationPage, mockPasswordDeviceService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);

        // stubs
        expect(mockPasswordDeviceService.authenticate(userId, password)).andThrow(new SubjectNotFoundException());
        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockPasswordDeviceService, mockHelpdeskManager);

        // RegisterPage: Register digipass for user
        FormTester authenticationForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID);
        authenticationForm.setValue(AuthenticationPage.LOGIN_NAME_FIELD_ID, login);
        authenticationForm.setValue(AuthenticationPage.PASSWORD_FIELD_ID, password);
        authenticationForm.submit(AuthenticationPage.LOGIN_BUTTON_ID);

        // verify
        verify(mockPasswordDeviceService, mockHelpdeskManager);

        wicket.assertRenderedPage(AuthenticationPage.class);
        wicket.assertErrorMessages(new String[] { "authenticationFailedMsg" });

    }

    @Test
    public void testAuthenticateDeviceDisabled()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String login = "test-login";
        String password = "test-password";
        DummyNameIdentifierMappingClient.setUserId(userId);

        // Authentication Page: Verify.
        AuthenticationPage authenticationPage = (AuthenticationPage) wicket.startPage(AuthenticationPage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID, Form.class);

        // setup
        EJBTestUtils.inject(authenticationPage, mockPasswordDeviceService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);

        // stubs
        expect(mockPasswordDeviceService.authenticate(userId, password)).andThrow(new DeviceDisabledException());
        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockPasswordDeviceService, mockHelpdeskManager);

        // operate
        FormTester authenticationForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID);
        authenticationForm.setValue(AuthenticationPage.LOGIN_NAME_FIELD_ID, login);
        authenticationForm.setValue(AuthenticationPage.PASSWORD_FIELD_ID, password);
        authenticationForm.submit(AuthenticationPage.LOGIN_BUTTON_ID);

        // verify
        verify(mockPasswordDeviceService, mockHelpdeskManager);

        wicket.assertRenderedPage(AuthenticationPage.class);
        wicket.assertErrorMessages(new String[] { "errorDeviceDisabled" });

    }

    @Test
    public void testAuthenticateFailed()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String login = "test-login";
        String password = "test-password";
        DummyNameIdentifierMappingClient.setUserId(userId);

        // Authentication Page: Verify.
        AuthenticationPage authenticationPage = (AuthenticationPage) wicket.startPage(AuthenticationPage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID, Form.class);

        // setup
        EJBTestUtils.inject(authenticationPage, mockPasswordDeviceService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);

        // stubs
        expect(mockPasswordDeviceService.authenticate(userId, password)).andStubReturn(null);
        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockPasswordDeviceService, mockHelpdeskManager);

        // operate
        FormTester authenticationForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + AuthenticationPage.AUTHENTICATION_FORM_ID);
        authenticationForm.setValue(AuthenticationPage.LOGIN_NAME_FIELD_ID, login);
        authenticationForm.setValue(AuthenticationPage.PASSWORD_FIELD_ID, password);
        authenticationForm.submit(AuthenticationPage.LOGIN_BUTTON_ID);

        // verify
        verify(mockPasswordDeviceService, mockHelpdeskManager);

        wicket.assertRenderedPage(AuthenticationPage.class);
        wicket.assertErrorMessages(new String[] { "authenticationFailedMsg" });

    }
}
