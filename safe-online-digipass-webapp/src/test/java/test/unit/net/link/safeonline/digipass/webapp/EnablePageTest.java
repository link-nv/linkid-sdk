package test.unit.net.link.safeonline.digipass.webapp;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.UUID;

import junit.framework.TestCase;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.demo.wicket.tools.WicketUtil;
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


public class EnablePageTest extends TestCase {

    private DigipassDeviceService mockDigipassDeviceService;

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

        this.jndiTestUtils = new JndiTestUtils();
        this.jndiTestUtils.setUp();

        this.mockDigipassDeviceService = createMock(DigipassDeviceService.class);
        this.mockSamlAuthorityService = createMock(SamlAuthorityService.class);
        this.mockHelpdeskManager = createMock(HelpdeskManager.class);

        this.wicket = new WicketTester(new DigipassTestApplication());
        this.wicket.processRequestCycle();

    }

    @Override
    @After
    public void tearDown()
            throws Exception {

        this.jndiTestUtils.tearDown();
    }

    @Test
    public void testEnable()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String token = "000000";
        String serialNumber = "12345678";

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(this.wicket.getServletSession());
        protocolContext.setSubject(userId);
        protocolContext.setAttribute(serialNumber);

        // verify
        EnablePage enablePage = (EnablePage) this.wicket.startPage(EnablePage.class);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID, Form.class);

        // setup
        EJBTestUtils.inject(enablePage, this.mockDigipassDeviceService);
        EJBTestUtils.inject(enablePage, this.mockSamlAuthorityService);

        // stubs
        expect(this.mockDigipassDeviceService.enable(userId, serialNumber, token)).andStubReturn(userId);
        expect(this.mockSamlAuthorityService.getAuthnAssertionValidity()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(this.mockDigipassDeviceService, this.mockSamlAuthorityService);

        // operate
        FormTester enableForm = this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID);
        enableForm.setValue(EnablePage.TOKEN_FIELD_ID, token);
        enableForm.submit(EnablePage.ENABLE_BUTTON_ID);

        // verify
        verify(this.mockDigipassDeviceService, this.mockSamlAuthorityService);
    }

    @Test
    public void testEnableSubjectNotFound()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String token = "000000";
        String serialNumber = "12345678";

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(this.wicket.getServletSession());
        protocolContext.setSubject(userId);
        protocolContext.setAttribute(serialNumber);

        // verify
        EnablePage enablePage = (EnablePage) this.wicket.startPage(EnablePage.class);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID, Form.class);

        // setup
        EJBTestUtils.inject(enablePage, this.mockDigipassDeviceService);
        this.jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, this.mockHelpdeskManager);

        // stubs
        expect(this.mockDigipassDeviceService.enable(userId, serialNumber, token)).andThrow(new SubjectNotFoundException());
        expect(this.mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(this.mockDigipassDeviceService, this.mockHelpdeskManager);

        // operate
        FormTester enableForm = this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID);
        enableForm.setValue(EnablePage.TOKEN_FIELD_ID, token);
        enableForm.submit(EnablePage.ENABLE_BUTTON_ID);

        // verify
        verify(this.mockDigipassDeviceService, this.mockHelpdeskManager);

        this.wicket.assertRenderedPage(EnablePage.class);
        this.wicket.assertErrorMessages(new String[] { "digipassNotRegistered" });

    }

    @Test
    public void testAuthenticateFailed()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String token = "000000";
        String serialNumber = "12345678";

        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(this.wicket.getServletSession());
        protocolContext.setSubject(userId);
        protocolContext.setAttribute(serialNumber);

        // verify
        EnablePage enablePage = (EnablePage) this.wicket.startPage(EnablePage.class);
        this.wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID, Form.class);

        // setup
        EJBTestUtils.inject(enablePage, this.mockDigipassDeviceService);
        this.jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, this.mockHelpdeskManager);

        // stubs
        expect(this.mockDigipassDeviceService.enable(userId, serialNumber, token)).andStubReturn(null);
        expect(this.mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);

        // prepare
        replay(this.mockDigipassDeviceService, this.mockHelpdeskManager);

        // operate
        FormTester enableForm = this.wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + EnablePage.ENABLE_FORM_ID);
        enableForm.setValue(EnablePage.TOKEN_FIELD_ID, token);
        enableForm.submit(EnablePage.ENABLE_BUTTON_ID);

        // verify
        verify(this.mockDigipassDeviceService, this.mockHelpdeskManager);

        this.wicket.assertRenderedPage(EnablePage.class);
        this.wicket.assertErrorMessages(new String[] { "authenticationFailedMsg" });

    }
}
