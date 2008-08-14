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
import net.link.safeonline.entity.DeviceMappingEntity;
import net.link.safeonline.entity.SubjectEntity;
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

    private String                deviceErrorUrl     = "device-error";

    private String                registerDeviceUrl  = "register-device";

    private String                newUserDeviceUrl   = "new-user-device";

    private String                loginUrl           = "login";

    private String                servletEndpointUrl = "http://test.auth/servlet";

    String                        userId             = UUID.randomUUID().toString();

    private AuthenticationService mockAuthenticationService;

    private SubjectService        mockSubjectService;

    private HelpdeskManager       mockHelpdeskManager;

    private Object[]              mockObjects;


    @Before
    public void setUp() throws Exception {

        this.jndiTestUtils = new JndiTestUtils();
        this.jndiTestUtils.setUp();

        this.mockAuthenticationService = createMock(AuthenticationService.class);
        this.mockSubjectService = createMock(SubjectService.class);
        this.mockHelpdeskManager = createMock(HelpdeskManager.class);
        this.jndiTestUtils.bindComponent("SafeOnline/SubjectServiceBean/local", this.mockSubjectService);
        this.jndiTestUtils.bindComponent("SafeOnline/HelpdeskManagerBean/local", this.mockHelpdeskManager);

        this.servletTestManager = new ServletTestManager();
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put("RegisterDeviceUrl", this.registerDeviceUrl);
        initParams.put("NewUserDeviceUrl", this.newUserDeviceUrl);
        initParams.put("LoginUrl", this.loginUrl);
        initParams.put("DeviceErrorUrl", this.deviceErrorUrl);
        initParams.put("ServletEndpointUrl", this.servletEndpointUrl);
        Map<String, Object> initialSessionAttributes = new HashMap<String, Object>();
        initialSessionAttributes.put(AuthenticationServiceManager.AUTH_SERVICE_ATTRIBUTE,
                this.mockAuthenticationService);
        initialSessionAttributes.put(LoginManager.USERID_ATTRIBUTE, this.userId);

        this.servletTestManager.setUp(DeviceRegistrationLandingServlet.class, initParams, null, null,
                initialSessionAttributes);
        this.location = this.servletTestManager.getServletLocation();
        this.httpClient = new HttpClient();

        this.mockObjects = new Object[] { this.mockAuthenticationService, this.mockSubjectService,
                this.mockHelpdeskManager };

        expect(this.mockHelpdeskManager.getHelpdeskContextLimit()).andStubReturn(50);
    }

    @After
    public void tearDown() throws Exception {

        this.servletTestManager.tearDown();
    }

    @Test
    public void getNotAllowed() throws Exception {

        // setup
        GetMethod getMethod = new GetMethod(this.location);

        // operate
        int result = this.httpClient.executeMethod(getMethod);

        // verify
        LOG.debug("result: " + result);
        assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, result);
    }

    @Test
    public void registrationFailedNewUser() throws Exception {

        // setup
        PostMethod postMethod = new PostMethod(this.location);

        // expectations
        expect(this.mockAuthenticationService.register((HttpServletRequest) EasyMock.anyObject())).andStubReturn(null);
        expect(this.mockAuthenticationService.getAuthenticationState()).andStubReturn(AuthenticationState.REDIRECTED);
        expect(this.mockSubjectService.getExceptionSubjectLogin((String) EasyMock.anyObject())).andStubReturn(null);

        // prepare
        replay(this.mockObjects);

        // operate
        int statusCode = this.httpClient.executeMethod(postMethod);

        // verify
        verify(this.mockObjects);
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        String resultLocation = postMethod.getResponseHeader("Location").getValue();
        LOG.debug("location: " + resultLocation);
        assertTrue(resultLocation.endsWith(this.newUserDeviceUrl));
    }

    @Test
    public void registrationFailedAuthedUser() throws Exception {

        // setup
        PostMethod postMethod = new PostMethod(this.location);

        // expectations
        expect(this.mockAuthenticationService.register((HttpServletRequest) EasyMock.anyObject())).andStubReturn(null);
        expect(this.mockAuthenticationService.getAuthenticationState()).andStubReturn(
                AuthenticationState.USER_AUTHENTICATED);
        expect(this.mockSubjectService.getExceptionSubjectLogin((String) EasyMock.anyObject())).andStubReturn(null);

        // prepare
        replay(this.mockObjects);

        // operate
        int statusCode = this.httpClient.executeMethod(postMethod);

        // verify
        verify(this.mockObjects);
        LOG.debug("status code: " + statusCode);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        String resultLocation = postMethod.getResponseHeader("Location").getValue();
        LOG.debug("location: " + resultLocation);
        assertTrue(resultLocation.endsWith(this.registerDeviceUrl));
    }

    @Test
    public void registrationSuccess() throws Exception {

        // setup
        String deviceMappingId = UUID.randomUUID().toString();
        SubjectEntity subject = new SubjectEntity(this.userId);
        DeviceEntity device = new DeviceEntity();
        DeviceMappingEntity deviceMapping = new DeviceMappingEntity(subject, deviceMappingId, device);

        PostMethod postMethod = new PostMethod(this.location);

        // expectations
        expect(this.mockAuthenticationService.getAuthenticationState()).andStubReturn(AuthenticationState.REDIRECTED);
        expect(this.mockAuthenticationService.register((HttpServletRequest) EasyMock.anyObject())).andStubReturn(
                deviceMapping);
        expect(this.mockSubjectService.getExceptionSubjectLogin((String) EasyMock.anyObject())).andStubReturn(null);

        // prepare
        replay(this.mockObjects);

        // operate
        int statusCode = this.httpClient.executeMethod(postMethod);

        // verify
        verify(this.mockObjects);
        LOG.debug("status code: " + statusCode);
        LOG.debug("result body: " + postMethod.getResponseBodyAsString());
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, statusCode);
        String resultLocation = postMethod.getResponseHeader("Location").getValue();
        LOG.debug("location: " + resultLocation);
        assertTrue(resultLocation.endsWith(this.loginUrl));
        String resultUserId = (String) this.servletTestManager.getSessionAttribute(LoginManager.USERID_ATTRIBUTE);
        assertEquals(this.userId, resultUserId);

        DeviceEntity resultDevice = (DeviceEntity) this.servletTestManager
                .getSessionAttribute(LoginManager.AUTHENTICATION_DEVICE_ATTRIBUTE);
        assertEquals(device, resultDevice);
    }
}
