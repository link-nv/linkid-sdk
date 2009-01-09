/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.auth.servlet;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.auth.AuthenticationUtils;
import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.auth.protocol.AuthenticationServiceManager;
import net.link.safeonline.auth.servlet.DeviceLandingServlet;
import net.link.safeonline.authentication.service.AuthenticationService;
import net.link.safeonline.authentication.service.AuthenticationState;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.helpdesk.HelpdeskManager;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.ServletTestManager;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class DeviceLandingServletTest {

    private static final Log      LOG                 = LogFactory.getLog(DeviceLandingServletTest.class);

    private ServletTestManager    servletTestManager;

    private JndiTestUtils         jndiTestUtils;

    private HttpClient            httpClient;

    private String                location;

    private String                deviceErrorUrl      = "device-error";

    private String                startUrl            = "start";

    private String                loginUrl            = "login";

    private String                tryAnotherDeviceUrl = "try-another-device";

    private String                servletEndpointUrl  = "http://test.auth/servlet";

    private AuthenticationService mockAuthenticationService;

    private SubjectService        mockSubjectService;

    private HelpdeskManager       mockHelpdeskManager;

    private Object[]              mockObjects;


    @Before
    public void setUp()
            throws Exception {

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();

        mockAuthenticationService = createMock(AuthenticationService.class);
        mockSubjectService = createMock(SubjectService.class);
        mockHelpdeskManager = createMock(HelpdeskManager.class);
        jndiTestUtils.bindComponent("SafeOnline/SubjectServiceBean/local", mockSubjectService);
        jndiTestUtils.bindComponent("SafeOnline/HelpdeskManagerBean/local", mockHelpdeskManager);

        servletTestManager = new ServletTestManager();
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put("LoginUrl", loginUrl);
        initParams.put("TryAnotherDeviceUrl", tryAnotherDeviceUrl);
        initParams.put("DeviceErrorUrl", deviceErrorUrl);
        initParams.put("ServletEndpointUrl", servletEndpointUrl);
        Map<String, Object> initialSessionAttributes = new HashMap<String, Object>();
        initialSessionAttributes.put(AuthenticationServiceManager.AUTH_SERVICE_ATTRIBUTE, mockAuthenticationService);
        initialSessionAttributes.put(AuthenticationUtils.REQUEST_URL_INIT_PARAM, startUrl);

        servletTestManager.setUp(DeviceLandingServlet.class, initParams, null, null, initialSessionAttributes);
        location = servletTestManager.getServletLocation();
        httpClient = new HttpClient();

        mockObjects = new Object[] { mockAuthenticationService, mockSubjectService, mockHelpdeskManager };

        expect(mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(50);
    }

    @After
    public void tearDown()
            throws Exception {

        servletTestManager.tearDown();
    }

    @Test
    public void getNotAllowed()
            throws Exception {

        // setup
        GetMethod getMethod = new GetMethod(location);

        // operate
        int result = httpClient.executeMethod(getMethod);

        // verify
        LOG.debug("result: " + result);
        assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, result);
    }

    @Test
    public void authenticationNotRegistered()
            throws Exception {

        // setup
        PostMethod postMethod = new PostMethod(location);

        // expectations
        expect(mockAuthenticationService.getAuthenticationState()).andStubReturn(AuthenticationState.REDIRECTED);
        expect(mockAuthenticationService.authenticate((HttpServletRequest) EasyMock.anyObject())).andStubReturn(null);
        expect(mockSubjectService.getExceptionSubjectLogin((String) EasyMock.anyObject())).andStubReturn(null);

        // prepare
        replay(mockObjects);

        // operate
        int statusCode = httpClient.executeMethod(postMethod);

        // verify
        verify(mockObjects);
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        String resultLocation = postMethod.getResponseHeader("Location").getValue();
        LOG.debug("location: " + resultLocation);
        assertTrue(resultLocation.endsWith(tryAnotherDeviceUrl));
    }

    @Test
    public void authenticationFailed()
            throws Exception {

        // setup
        PostMethod postMethod = new PostMethod(location);

        // expectations
        expect(mockAuthenticationService.getAuthenticationState()).andStubReturn(AuthenticationState.INITIALIZED);
        expect(mockAuthenticationService.authenticate((HttpServletRequest) EasyMock.anyObject())).andStubReturn(null);
        expect(mockSubjectService.getExceptionSubjectLogin((String) EasyMock.anyObject())).andStubReturn(null);

        // prepare
        replay(mockObjects);

        // operate
        int statusCode = httpClient.executeMethod(postMethod);

        // verify
        verify(mockObjects);
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        String resultLocation = postMethod.getResponseHeader("Location").getValue();
        LOG.debug("location: " + resultLocation);
        assertTrue(resultLocation.endsWith(startUrl));
    }

    @Test
    public void authenticationSuccess()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        DeviceEntity device = new DeviceEntity();

        PostMethod postMethod = new PostMethod(location);

        // expectations
        expect(mockAuthenticationService.getAuthenticationState()).andStubReturn(AuthenticationState.REDIRECTED);
        expect(mockAuthenticationService.authenticate((HttpServletRequest) EasyMock.anyObject())).andStubReturn(userId);
        expect(mockAuthenticationService.getAuthenticationDevice()).andStubReturn(device);
        expect(mockSubjectService.getExceptionSubjectLogin((String) EasyMock.anyObject())).andStubReturn(null);
        expect(mockAuthenticationService.getSsoCookie()).andStubReturn(null);

        // prepare
        replay(mockObjects);

        // operate
        int statusCode = httpClient.executeMethod(postMethod);

        // verify
        verify(mockObjects);
        LOG.debug("status code: " + statusCode);
        LOG.debug("result body: " + postMethod.getResponseBodyAsString());
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        String resultLocation = postMethod.getResponseHeader("Location").getValue();
        LOG.debug("location: " + resultLocation);
        assertTrue(resultLocation.endsWith(loginUrl));
        String resultUserId = (String) servletTestManager.getSessionAttribute(LoginManager.USERID_ATTRIBUTE);
        assertEquals(userId, resultUserId);

        DeviceEntity resultDevice = (DeviceEntity) servletTestManager
                                                                          .getSessionAttribute(LoginManager.AUTHENTICATION_DEVICE_ATTRIBUTE);
        assertEquals(device, resultDevice);
    }
}
