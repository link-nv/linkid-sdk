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

import net.link.safeonline.auth.LoginManager;
import net.link.safeonline.auth.protocol.AuthenticationServiceManager;
import net.link.safeonline.auth.servlet.DeviceRegistrationLandingServlet;
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


public class DeviceRegistrationLandingServletTest {

    private static final Log      LOG                = LogFactory.getLog(DeviceRegistrationLandingServletTest.class);

    private ServletTestManager    servletTestManager;

    private JndiTestUtils         jndiTestUtils;

    private HttpClient            httpClient;

    private String                location;

    private String                deviceErrorPath    = "device-error";

    private String                registerDevicePath = "register-device";

    private String                newUserDevicePath  = "new-user-device";

    private String                loginPath          = "login";

    String                        userId             = UUID.randomUUID().toString();

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
        initParams.put(DeviceRegistrationLandingServlet.REGISTER_DEVICE_PATH, registerDevicePath);
        initParams.put(DeviceRegistrationLandingServlet.NEW_USER_DEVICE_PATH, newUserDevicePath);
        initParams.put(DeviceRegistrationLandingServlet.LOGIN_PATH, loginPath);
        initParams.put(DeviceRegistrationLandingServlet.DEVICE_ERROR_PATH, deviceErrorPath);
        Map<String, Object> initialSessionAttributes = new HashMap<String, Object>();
        initialSessionAttributes.put(AuthenticationServiceManager.AUTH_SERVICE_ATTRIBUTE, mockAuthenticationService);
        initialSessionAttributes.put(LoginManager.USERID_ATTRIBUTE, userId);

        servletTestManager.setUp(DeviceRegistrationLandingServlet.class, initParams, null, null, initialSessionAttributes);
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
    public void registrationFailedNewUser()
            throws Exception {

        // setup
        PostMethod postMethod = new PostMethod(location);

        // expectations
        expect(mockAuthenticationService.register((HttpServletRequest) EasyMock.anyObject())).andStubReturn(null);
        expect(mockAuthenticationService.getAuthenticationState()).andStubReturn(AuthenticationState.REDIRECTED);
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
        assertTrue(resultLocation.endsWith(newUserDevicePath));
    }

    @Test
    public void registrationFailedAuthedUser()
            throws Exception {

        // setup
        PostMethod postMethod = new PostMethod(location);

        // expectations
        expect(mockAuthenticationService.register((HttpServletRequest) EasyMock.anyObject())).andStubReturn(null);
        expect(mockAuthenticationService.getAuthenticationState()).andStubReturn(AuthenticationState.USER_AUTHENTICATED);
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
        assertTrue(resultLocation.endsWith(registerDevicePath));
    }

    @Test
    public void registrationSuccess()
            throws Exception {

        // setup
        DeviceEntity device = new DeviceEntity();

        PostMethod postMethod = new PostMethod(location);

        // expectations
        expect(mockAuthenticationService.getAuthenticationState()).andStubReturn(AuthenticationState.REDIRECTED);
        expect(mockAuthenticationService.register((HttpServletRequest) EasyMock.anyObject())).andStubReturn(userId);
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
        assertTrue(resultLocation.endsWith(loginPath));
        String resultUserId = (String) servletTestManager.getSessionAttribute(LoginManager.USERID_ATTRIBUTE);
        assertEquals(userId, resultUserId);

        DeviceEntity resultDevice = (DeviceEntity) servletTestManager.getSessionAttribute(LoginManager.AUTHENTICATION_DEVICE_ATTRIBUTE);
        assertEquals(device, resultDevice);
    }
}
