package test.unit.net.link.safeonline.encap.webapp;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.UUID;

import junit.framework.TestCase;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.encap.webapp.EnablePage;
import net.link.safeonline.helpdesk.HelpdeskManager;
import net.link.safeonline.model.encap.EncapDeviceService;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class EnablePageTest extends TestCase {

    private EncapDeviceService   mockEncapDeviceService;

    private SamlAuthorityService mockSamlAuthorityService;

    private HelpdeskManager      mockHelpdeskManager;

    private WicketTester         wicket;

    private JndiTestUtils        jndiTestUtils;


    @Override
    @Before
    public void setUp()
            throws Exception {

        super.setUp();

        WicketUtil.setUnitTesting(true);

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();

        mockEncapDeviceService = createMock(EncapDeviceService.class);
        mockSamlAuthorityService = createMock(SamlAuthorityService.class);
        mockHelpdeskManager = createMock(HelpdeskManager.class);

        wicket = new WicketTester(new EncapTestApplication());
        wicket.processRequestCycle();

    }

    @Override
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
        protocolContext.setAttribute(serialNumber);

        // verify
        EnablePage enablePage = (EnablePage) wicket.startPage(EnablePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID, Form.class);

        // setup
        EJBTestUtils.inject(enablePage, mockEncapDeviceService);
        EJBTestUtils.inject(enablePage, mockSamlAuthorityService);

        // stubs
        expect(mockEncapDeviceService.enable(userId, serialNumber, token)).andStubReturn(userId);
        expect(mockSamlAuthorityService.getAuthnAssertionValidity()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockEncapDeviceService, mockSamlAuthorityService);

        // operate
        FormTester enableForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID);
        enableForm.setValue(EnablePage.TOKEN_FIELD_ID, token);
        enableForm.submit(EnablePage.ENABLE_BUTTON_ID);

        // verify
        verify(mockEncapDeviceService, mockSamlAuthorityService);
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
        protocolContext.setAttribute(serialNumber);

        // verify
        EnablePage enablePage = (EnablePage) wicket.startPage(EnablePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID, Form.class);

        // setup
        EJBTestUtils.inject(enablePage, mockEncapDeviceService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);

        // stubs
        expect(mockEncapDeviceService.enable(userId, serialNumber, token)).andThrow(new SubjectNotFoundException());
        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockEncapDeviceService, mockHelpdeskManager);

        // operate
        FormTester enableForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID);
        enableForm.setValue(EnablePage.TOKEN_FIELD_ID, token);
        enableForm.submit(EnablePage.ENABLE_BUTTON_ID);

        // verify
        verify(mockEncapDeviceService, mockHelpdeskManager);

        wicket.assertRenderedPage(EnablePage.class);
        wicket.assertErrorMessages(new String[] { "encapNotRegistered" });

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
        protocolContext.setAttribute(serialNumber);

        // verify
        EnablePage enablePage = (EnablePage) wicket.startPage(EnablePage.class);
        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID, Form.class);

        // setup
        EJBTestUtils.inject(enablePage, mockEncapDeviceService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);

        // stubs
        expect(mockEncapDeviceService.enable(userId, serialNumber, token)).andStubReturn(null);
        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(mockEncapDeviceService, mockHelpdeskManager);

        // operate
        FormTester enableForm = wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID);
        enableForm.setValue(EnablePage.TOKEN_FIELD_ID, token);
        enableForm.submit(EnablePage.ENABLE_BUTTON_ID);

        // verify
        verify(mockEncapDeviceService, mockHelpdeskManager);

        wicket.assertRenderedPage(EnablePage.class);
        wicket.assertErrorMessages(new String[] { "authenticationFailedMsg" });

    }
}
