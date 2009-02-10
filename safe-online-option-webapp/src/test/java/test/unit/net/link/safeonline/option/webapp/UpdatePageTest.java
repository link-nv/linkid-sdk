package test.unit.net.link.safeonline.option.webapp;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.UUID;

import javax.servlet.http.Cookie;

import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.helpdesk.HelpdeskManager;
import net.link.safeonline.option.webapp.OptionApplication;
import net.link.safeonline.option.webapp.OptionDevice;
import net.link.safeonline.option.webapp.UpdatePage;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.test.UrlPageSource;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.protocol.http.MockHttpServletRequest;
import org.apache.wicket.protocol.http.MockHttpServletResponse;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class UpdatePageTest {

    private JndiTestUtils        jndiTestUtils;
    private SamlAuthorityService mockSamlAuthorityService;
    private HelpdeskManager      mockHelpdeskManager;
    private WicketTester         wicket;

    private static final String  TEST_USERID  = UUID.randomUUID().toString();
    private static final String  TEST_OLD_PIN = "0000";
    private static final String  TEST_NEW_PIN = "0001";
    private static final String  TEST_IMEI    = "012345678912345";


    @Before
    public void setUp()
            throws Exception {

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();

        mockSamlAuthorityService = createMock(SamlAuthorityService.class);
        mockHelpdeskManager = createMock(HelpdeskManager.class);

        WicketUtil.setUnitTesting(true);
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
    private FormTester prepareUpdate()
            throws Exception {

        // Initialize contexts.
        ProtocolContext protocolContext = ProtocolContext.getProtocolContext(wicket.getServletSession());
        protocolContext.setSubject(TEST_USERID);

        // Inject EJBs.
        EJBTestUtils.inject(wicket.getLastRenderedPage(), mockSamlAuthorityService);
        jndiTestUtils.bindComponent(HelpdeskManager.JNDI_BINDING, mockHelpdeskManager);

        // Setup mocks.
        expect(mockSamlAuthorityService.getAuthnAssertionValidity()).andStubReturn(Integer.MAX_VALUE);
        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(Integer.MAX_VALUE);
        replay(mockSamlAuthorityService, mockHelpdeskManager);

        // Return Authentication Form.
        return getUpdateForm(wicket);
    }

    public static FormTester getUpdateForm(WicketTester wicket) {

        wicket.assertComponent(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID, Form.class);
        return wicket.newFormTester(TemplatePage.CONTENT_ID + ":" + UpdatePage.UPDATE_FORM_ID);
    }

    @Test
    public void testUpdate()
            throws Exception {

        // Setup.
        OptionDevice._update(TEST_OLD_PIN, TEST_IMEI);
        Cookie[] oldCookies = wicket.getWicketRequest().getCookies();

        wicket.startPage(new UrlPageSource(OptionApplication.UPDATE_MOUNTPOINT));
        FormTester form = prepareUpdate();
        // TODO: See SOS-393
        ((MockHttpServletRequest) wicket.getWicketRequest().getHttpServletRequest()).setCookies(oldCookies);

        // Pass the PIN for our device.
        form.setValue(UpdatePage.OLD_PIN_FIELD_ID, TEST_OLD_PIN);
        form.setValue(UpdatePage.NEW_PIN_FIELD_ID, TEST_NEW_PIN);
        form.setValue(UpdatePage.NEW_PIN_CONFIRM_FIELD_ID, TEST_NEW_PIN);
        form.submit(UpdatePage.UPDATE_BUTTON_ID);

        // Check for errors.
        wicket.assertRenderedPage(UpdatePage.class);
        wicket.assertNoErrorMessage();
        verify(mockSamlAuthorityService, mockHelpdeskManager);

        Collection<Cookie> newCookies = ((MockHttpServletResponse) wicket.getWicketResponse().getHttpServletResponse()).getCookies();
        ((MockHttpServletRequest) ((WebRequest) RequestCycle.get().getRequest()).getHttpServletRequest())
                                                                                                         .setCookies(newCookies
                                                                                                                               .toArray(new Cookie[newCookies
                                                                                                                                                             .size()]));
        String sampleImei = OptionDevice.validate(TEST_NEW_PIN);
        assertEquals(String.format("IMEI doesn't match.  Expected: %s, got: %s", TEST_IMEI, sampleImei), //
                TEST_IMEI, sampleImei);
    }

    public void testUpdateWrongPIN()
            throws Exception {

        // Setup.
        OptionDevice._update(TEST_OLD_PIN, TEST_IMEI);
        Cookie[] oldCookies = wicket.getWicketRequest().getCookies();

        wicket.startPage(new UrlPageSource(OptionApplication.UPDATE_MOUNTPOINT));
        FormTester form = prepareUpdate();
        // TODO: See SOS-393
        ((MockHttpServletRequest) wicket.getWicketRequest().getHttpServletRequest()).setCookies(oldCookies);

        // Pass the PIN for our device.
        form.setValue(UpdatePage.OLD_PIN_FIELD_ID, TEST_NEW_PIN);
        form.setValue(UpdatePage.NEW_PIN_FIELD_ID, TEST_NEW_PIN);
        form.setValue(UpdatePage.NEW_PIN_CONFIRM_FIELD_ID, TEST_NEW_PIN);
        form.submit(UpdatePage.UPDATE_BUTTON_ID);

        // Check for errors.
        wicket.assertRenderedPage(UpdatePage.class);
        wicket.assertErrorMessages(new String[] { "optionAuthenticationFailed" });
        verify(mockSamlAuthorityService, mockHelpdeskManager);

        Collection<Cookie> newCookies = ((MockHttpServletResponse) wicket.getWicketResponse().getHttpServletResponse()).getCookies();
        ((MockHttpServletRequest) ((WebRequest) RequestCycle.get().getRequest()).getHttpServletRequest())
                                                                                                         .setCookies(newCookies
                                                                                                                               .toArray(new Cookie[newCookies
                                                                                                                                                             .size()]));
        String sampleImei = OptionDevice.validate(TEST_OLD_PIN);
        assertEquals(String.format("IMEI doesn't match.  Expected: %s, got: %s", TEST_IMEI, sampleImei), //
                TEST_IMEI, sampleImei);
    }
}
