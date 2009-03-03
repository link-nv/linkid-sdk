package test.unit.net.link.safeonline.digipass.webapp;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.UUID;

import net.link.safeonline.authentication.exception.DeviceAuthenticationException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.digipass.webapp.EnablePage;
import net.link.safeonline.helpdesk.HelpdeskManager;
import net.link.safeonline.model.digipass.DigipassDeviceService;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.webapp.template.TemplatePage;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class EnablePageTest {

    private DigipassDeviceService mockDigipassDeviceService;

    private SamlAuthorityService  mockSamlAuthorityService;

    private HelpdeskManager       mockHelpdeskManager;

    private WicketTester          wicket;

    private JndiTestUtils         jndiTestUtils;


    @Before
    public void setUp()
            throws Exception {

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();

        mockDigipassDeviceService = createMock(DigipassDeviceService.class);
        mockSamlAuthorityService = createMock(SamlAuthorityService.class);
        mockHelpdeskManager = createMock(HelpdeskManager.class);

        wicket = new WicketTester(new DigipassTestApplication());
        wicket.processRequestCycle();

    }

    @After
    public void tearDown()
            throws Exception {

        jndiTestUtils.tearDown();
    }

    @Test
    public void testEnable()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String token = "000000";
        String serialNumber = "12345678";

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(wicket.getServletSession());
        protocolContext.setSubject(userId);
        protocolContext.setAttributeId(serialNumber);

        // verify
        EnablePage enablePage = (EnablePage) wicket.startPage(EnablePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID, Form.class);

        // setup
        EJBTestUtils.inject(enablePage, mockDigipassDeviceService);
        EJBTestUtils.inject(enablePage, mockSamlAuthorityService);

        // stubs
        mockDigipassDeviceService.enable(userId, serialNumber, token);
        expect(mockSamlAuthorityService.getAuthnAssertionValidity()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockDigipassDeviceService, mockSamlAuthorityService);

        // operate
        FormTester enableForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID);
        enableForm.setValue(EnablePage.TOKEN_FIELD_ID, token);
        enableForm.submit(EnablePage.ENABLE_BUTTON_ID);

        // verify
        verify(mockDigipassDeviceService, mockSamlAuthorityService);
    }

    @Test
    public void testEnableSubjectNotFound()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String token = "000000";
        String serialNumber = "12345678";

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(wicket.getServletSession());
        protocolContext.setSubject(userId);
        protocolContext.setAttributeId(serialNumber);

        // verify
        EnablePage enablePage = (EnablePage) wicket.startPage(EnablePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID, Form.class);

        // setup
        EJBTestUtils.inject(enablePage, mockDigipassDeviceService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);

        // stubs
        mockDigipassDeviceService.enable(userId, serialNumber, token);
        expectLastCall().andThrow(new SubjectNotFoundException());
        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockDigipassDeviceService, mockHelpdeskManager);

        // operate
        FormTester enableForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID);
        enableForm.setValue(EnablePage.TOKEN_FIELD_ID, token);
        enableForm.submit(EnablePage.ENABLE_BUTTON_ID);

        // verify
        verify(mockDigipassDeviceService, mockHelpdeskManager);

        wicket.assertRenderedPage(EnablePage.class);
        wicket.assertErrorMessages(new String[] { "digipassNotRegistered" });

    }

    @Test
    public void testAuthenticateFailed()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String token = "000000";
        String serialNumber = "12345678";

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(wicket.getServletSession());
        protocolContext.setSubject(userId);
        protocolContext.setAttributeId(serialNumber);

        // verify
        EnablePage enablePage = (EnablePage) wicket.startPage(EnablePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID, Form.class);

        // setup
        EJBTestUtils.inject(enablePage, mockDigipassDeviceService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);

        // stubs
        mockDigipassDeviceService.enable(userId, serialNumber, token);
        expectLastCall().andThrow(new DeviceAuthenticationException());
        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockDigipassDeviceService, mockHelpdeskManager);

        // operate
        FormTester enableForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID);
        enableForm.setValue(EnablePage.TOKEN_FIELD_ID, token);
        enableForm.submit(EnablePage.ENABLE_BUTTON_ID);

        // verify
        verify(mockDigipassDeviceService, mockHelpdeskManager);

        wicket.assertRenderedPage(EnablePage.class);
        wicket.assertErrorMessages(new String[] { "authenticationFailedMsg" });

    }
}
