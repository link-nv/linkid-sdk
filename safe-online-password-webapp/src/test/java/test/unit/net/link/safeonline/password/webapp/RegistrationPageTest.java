package test.unit.net.link.safeonline.password.webapp;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.UUID;

import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.device.sdk.saml2.DeviceOperationType;
import net.link.safeonline.model.password.PasswordDeviceService;
import net.link.safeonline.password.webapp.RegistrationPage;
import net.link.safeonline.sdk.test.DummyNameIdentifierMappingClient;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.webapp.template.TemplatePage;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class RegistrationPageTest {

    private PasswordDeviceService mockPasswordDeviceService;

    private SamlAuthorityService  mockSamlAuthorityService;

    private WicketTester          wicket;

    private JndiTestUtils         jndiTestUtils;


    @Before
    public void setUp()
            throws Exception {

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();

        mockPasswordDeviceService = createMock(PasswordDeviceService.class);
        mockSamlAuthorityService = createMock(SamlAuthorityService.class);

        wicket = new WicketTester(new PasswordTestApplication());

    }

    @After
    public void tearDown()
            throws Exception {

        jndiTestUtils.tearDown();
    }

    @Test
    public void testRegister()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String nodeName = "test-node-name";
        String password = "test-password";
        DummyNameIdentifierMappingClient.setUserId(userId);

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(wicket.getServletSession());
        protocolContext.setDeviceOperation(DeviceOperationType.NEW_ACCOUNT_REGISTER);
        protocolContext.setSubject(userId);
        protocolContext.setNodeName(nodeName);

        // setup
        RegistrationPage registrationPage = new RegistrationPage();
        EJBTestUtils.inject(registrationPage, mockPasswordDeviceService);
        EJBTestUtils.inject(registrationPage, mockSamlAuthorityService);

        // stubs
        expect(mockPasswordDeviceService.isPasswordConfigured(userId)).andReturn(false);
        mockPasswordDeviceService.register(nodeName, userId, password);
        expect(mockPasswordDeviceService.isPasswordConfigured(userId)).andReturn(true);
        expect(mockSamlAuthorityService.getAuthnAssertionValidity()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockPasswordDeviceService, mockSamlAuthorityService);

        // Registration Page: Verify.
        wicket.startPage(registrationPage);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + RegistrationPage.REGISTRATION_FORM_ID, Form.class);

        // operate
        FormTester registrationForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + RegistrationPage.REGISTRATION_FORM_ID);
        registrationForm.setValue(RegistrationPage.PASSWORD1_FIELD_ID, password);
        registrationForm.setValue(RegistrationPage.PASSWORD2_FIELD_ID, password);
        registrationForm.submit(RegistrationPage.SAVE_BUTTON_ID);

        // verify
        verify(mockPasswordDeviceService, mockSamlAuthorityService);
    }

    @Test
    public void testRegisterPasswordsNotEqual()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String password = "test-password";
        DummyNameIdentifierMappingClient.setUserId(userId);

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(wicket.getServletSession());
        protocolContext.setDeviceOperation(DeviceOperationType.NEW_ACCOUNT_REGISTER);
        protocolContext.setSubject(userId);

        // setup
        RegistrationPage registrationPage = new RegistrationPage();
        EJBTestUtils.inject(registrationPage, mockPasswordDeviceService);
        EJBTestUtils.inject(registrationPage, mockSamlAuthorityService);

        // stubs
        expect(mockPasswordDeviceService.isPasswordConfigured(userId)).andReturn(false);
        expect(mockPasswordDeviceService.isPasswordConfigured(userId)).andReturn(false);
        expect(mockSamlAuthorityService.getAuthnAssertionValidity()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockPasswordDeviceService, mockSamlAuthorityService);

        // Registration Page: Verify.
        wicket.startPage(registrationPage);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + RegistrationPage.REGISTRATION_FORM_ID, Form.class);

        // operate
        FormTester registrationForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + RegistrationPage.REGISTRATION_FORM_ID);
        registrationForm.setValue(RegistrationPage.PASSWORD1_FIELD_ID, password);
        registrationForm.setValue(RegistrationPage.PASSWORD2_FIELD_ID, "foobar-password");
        registrationForm.submit(RegistrationPage.SAVE_BUTTON_ID);

        // verify
        verify(mockPasswordDeviceService, mockSamlAuthorityService);

        wicket.assertRenderedPage(RegistrationPage.class);
        wicket.assertErrorMessages(new String[] { RegistrationPage.PASSWORD2_FIELD_ID + ".EqualPasswordInputValidator" });

    }

    @Test
    public void testRegisterPasswordAlreadyRegistered()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String password = "test-password";
        DummyNameIdentifierMappingClient.setUserId(userId);

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(wicket.getServletSession());
        protocolContext.setDeviceOperation(DeviceOperationType.NEW_ACCOUNT_REGISTER);
        protocolContext.setSubject(userId);

        // setup
        RegistrationPage registrationPage = new RegistrationPage();
        EJBTestUtils.inject(registrationPage, mockPasswordDeviceService);
        EJBTestUtils.inject(registrationPage, mockSamlAuthorityService);

        // stubs
        expect(mockPasswordDeviceService.isPasswordConfigured(userId)).andReturn(true);
        expect(mockSamlAuthorityService.getAuthnAssertionValidity()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockPasswordDeviceService, mockSamlAuthorityService);

        // Registration Page: Verify.
        wicket.startPage(registrationPage);
        System.err.println(wicket.getLastRenderedPage());
        wicket.dumpPage();
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + RegistrationPage.REGISTRATION_FORM_ID, Form.class);

        // operate
        FormTester registrationForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + RegistrationPage.REGISTRATION_FORM_ID);
        registrationForm.setValue(RegistrationPage.PASSWORD1_FIELD_ID, password);
        registrationForm.setValue(RegistrationPage.PASSWORD2_FIELD_ID, "foobar-password");
        registrationForm.submit(RegistrationPage.SAVE_BUTTON_ID);

        // verify
        verify(mockPasswordDeviceService, mockSamlAuthorityService);

        wicket.assertRenderedPage(RegistrationPage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + RegistrationPage.ALREADY_REGISTERED_LINK_ID, Link.class);
    }

}
