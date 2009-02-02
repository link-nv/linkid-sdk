package test.unit.net.link.safeonline.password.webapp;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.UUID;

import junit.framework.TestCase;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.helpdesk.HelpdeskManager;
import net.link.safeonline.model.password.PasswordDeviceService;
import net.link.safeonline.password.webapp.UpdatePage;
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


public class UpdatePageTest extends TestCase {

    private PasswordDeviceService mockPasswordDeviceService;

    private SamlAuthorityService  mockSamlAuthorityService;

    private HelpdeskManager       mockHelpdeskManager;

    private WicketTester          wicket;

    private JndiTestUtils         jndiTestUtils;


    @Override
    @Before
    public void setUp()
            throws Exception {

        super.setUp();

        WicketUtil.setUnitTesting(true);

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();

        mockPasswordDeviceService = createMock(PasswordDeviceService.class);
        mockSamlAuthorityService = createMock(SamlAuthorityService.class);
        mockHelpdeskManager = createMock(HelpdeskManager.class);

        wicket = new WicketTester(new PasswordTestApplication());

    }

    @Override
    @After
    public void tearDown()
            throws Exception {

        jndiTestUtils.tearDown();
    }

    @Test
    public void testUpdate()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String oldPassword = "test-old-password";
        String newPassword = "test-new-password";
        DummyNameIdentifierMappingClient.setUserId(userId);

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(wicket.getServletSession());
        protocolContext.setSubject(userId);

        // Update Page: Verify.
        UpdatePage updatePage = (UpdatePage) wicket.startPage(UpdatePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID, Form.class);

        // setup
        EJBTestUtils.inject(updatePage, mockPasswordDeviceService);
        EJBTestUtils.inject(updatePage, mockSamlAuthorityService);

        // stubs
        mockPasswordDeviceService.update(userId, oldPassword, newPassword);
        expect(mockSamlAuthorityService.getAuthnAssertionValidity()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockPasswordDeviceService, mockSamlAuthorityService);

        // operate
        FormTester updateForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID);
        updateForm.setValue(UpdatePage.OLDPASSWORD_FIELD_ID, oldPassword);
        updateForm.setValue(UpdatePage.PASSWORD1_FIELD_ID, newPassword);
        updateForm.setValue(UpdatePage.PASSWORD2_FIELD_ID, newPassword);
        updateForm.submit(UpdatePage.SAVE_BUTTON_ID);

        // verify
        verify(mockPasswordDeviceService, mockSamlAuthorityService);
    }

    @Test
    public void testUpdatePasswordsNotEqual()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String oldPassword = "test-old-password";
        String newPassword = "test-new-password";
        DummyNameIdentifierMappingClient.setUserId(userId);

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(wicket.getServletSession());
        protocolContext.setSubject(userId);

        // Update Page: Verify.
        UpdatePage updatePage = (UpdatePage) wicket.startPage(UpdatePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID, Form.class);

        // setup
        EJBTestUtils.inject(updatePage, mockPasswordDeviceService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);

        // stubs
        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockPasswordDeviceService, mockHelpdeskManager);

        // operate
        FormTester updateForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID);
        updateForm.setValue(UpdatePage.OLDPASSWORD_FIELD_ID, oldPassword);
        updateForm.setValue(UpdatePage.PASSWORD1_FIELD_ID, newPassword);
        updateForm.setValue(UpdatePage.PASSWORD2_FIELD_ID, "foobar-password");
        updateForm.submit(UpdatePage.SAVE_BUTTON_ID);

        // verify
        verify(mockPasswordDeviceService, mockHelpdeskManager);

        wicket.assertRenderedPage(UpdatePage.class);
        wicket.assertErrorMessages(new String[] { UpdatePage.PASSWORD2_FIELD_ID + ".EqualPasswordInputValidator" });

    }
}
