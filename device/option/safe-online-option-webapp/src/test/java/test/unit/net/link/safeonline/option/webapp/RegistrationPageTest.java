package test.unit.net.link.safeonline.option.webapp;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.UUID;

import javax.servlet.http.Cookie;

import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.helpdesk.HelpdeskManager;
import net.link.safeonline.model.option.OptionDeviceService;
import net.link.safeonline.option.webapp.OptionApplication;
import net.link.safeonline.option.webapp.OptionDevice;
import net.link.safeonline.option.webapp.RegistrationPage;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.test.UrlPageSource;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.protocol.http.MockHttpServletRequest;
import org.apache.wicket.protocol.http.MockHttpServletResponse;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class RegistrationPageTest {

    private JndiTestUtils        jndiTestUtils;
    private OptionDeviceService  mockOptionDeviceService;
    private SamlAuthorityService mockSamlAuthorityService;
    private HelpdeskManager      mockHelpdeskManager;
    private WicketTester         wicket;

    private static final String  TEST_USERID = UUID.randomUUID().toString();
    private static final String  TEST_PIN    = "0000";


    @Before
    public void setUp()
            throws Exception {

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();

        mockOptionDeviceService = createMock(OptionDeviceService.class);
        mockSamlAuthorityService = createMock(SamlAuthorityService.class);
        mockHelpdeskManager = createMock(HelpdeskManager.class);

        wicket = new WicketTester(new OptionTestApplication());
        wicket.processRequestCycle();
    }

    @After
    public void tearDown()
            throws Exception {

        jndiTestUtils.tearDown();
    }

    /**
     * Sets wicket up to begin authentication and injects the Option device service, SAML authority service and HelpDesk service.
     * 
     * @return The {@link FormTester} for the authentication for on the authentication page.
     */
    private FormTester prepareRegistration()
            throws Exception {

        // Initialize contexts.
        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(wicket.getServletSession());
        protocolContext.setSubject(TEST_USERID);

        // Inject EJBs.
        EJBTestUtils.inject(wicket.getLastRenderedPage(), mockOptionDeviceService);
        EJBTestUtils.inject(wicket.getLastRenderedPage(), mockSamlAuthorityService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);

        // Setup mocks.
        expect(mockSamlAuthorityService.getAuthnAssertionValidity()).andStubReturn(Integer.MAX_VALUE);
        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);
        replay(mockSamlAuthorityService, mockHelpdeskManager);

        // Return Authentication Form.
        return getRegistrationForm(wicket);
    }

    public static FormTester getRegistrationForm(WicketTester wicket) {

        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + RegistrationPage.REGISTER_FORM_ID, Form.class);
        return wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + RegistrationPage.REGISTER_FORM_ID);
    }

    @Test
    public void testRegister()
            throws Exception {

        // Setup.
        wicket.startPage(new UrlPageSource(OptionApplication.REGISTRATION_MOUNTPOINT));
        FormTester form = prepareRegistration();

        // Describe Expected Scenario.
        mockOptionDeviceService.register((String) EasyMock.anyObject(), EasyMock.matches(TEST_USERID), (String) EasyMock.anyObject());
        replay(mockOptionDeviceService);

        // Pass the PIN for our device.
        form.setValue(RegistrationPage.PIN_FIELD_ID, TEST_PIN);
        form.setValue(RegistrationPage.PIN_CONFIRM_FIELD_ID, TEST_PIN);
        form.submit(RegistrationPage.REGISTER_BUTTON_ID);

        // Check for errors.
        wicket.assertRenderedPage(RegistrationPage.class);
        wicket.assertNoErrorMessage();
        verify(mockOptionDeviceService, mockSamlAuthorityService, mockHelpdeskManager);
        assertTrue("Registration failed.", //
                ProtocolContext.getProtocolContext(wicket.getServletSession()).getSuccess());

        Collection<Cookie> cookies = ((MockHttpServletResponse) wicket.getWicketResponse().getHttpServletResponse()).getCookies();
        ((MockHttpServletRequest) wicket.getWicketRequest().getHttpServletRequest())
                                                                                    .setCookies(cookies.toArray(new Cookie[cookies.size()]));
        assertNotNull("IMEI is not set.", //
                OptionDevice.validate(TEST_PIN));
    }
}
