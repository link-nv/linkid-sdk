/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.user.servlet;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.authentication.exception.NodeMappingNotFoundException;
import net.link.safeonline.authentication.service.DeviceOperationService;
import net.link.safeonline.test.util.ServletTestManager;
import net.link.safeonline.user.servlet.DeviceLandingServlet;

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

    private static final Log       LOG                = LogFactory.getLog(DeviceLandingServletTest.class);

    private ServletTestManager     servletTestManager;

    private HttpClient             httpClient;

    private String                 location;

    private String                 errorPage          = "error-page";

    private String                 devicesPage        = "devices-page";

    private String                 servletEndpointUrl = "http://test.user/servlet";

    private DeviceOperationService mockDeviceOperationService;

    private Object[]               mockObjects;


    @Before
    public void setUp()
            throws Exception {

        mockDeviceOperationService = createMock(DeviceOperationService.class);

        servletTestManager = new ServletTestManager();
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put("ErrorPage", errorPage);
        initParams.put("DevicesPage", devicesPage);
        initParams.put("ServletEndpointUrl", servletEndpointUrl);
        Map<String, Object> initialSessionAttributes = new HashMap<String, Object>();
        initialSessionAttributes.put(DeviceOperationService.DEVICE_OPERATION_SERVICE_ATTRIBUTE, mockDeviceOperationService);

        servletTestManager.setUp(DeviceLandingServlet.class, initParams, null, null, initialSessionAttributes);
        location = servletTestManager.getServletLocation();
        httpClient = new HttpClient();

        mockObjects = new Object[] { mockDeviceOperationService };
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
    public void deviceOperationFailed()
            throws Exception {

        // setup
        PostMethod postMethod = new PostMethod(location);

        // expectations
        expect(mockDeviceOperationService.finalize((HttpServletRequest) EasyMock.anyObject())).andThrow(
                new NodeMappingNotFoundException());

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
        assertTrue(resultLocation.endsWith(errorPage));
    }

    @Test
    public void authenticationSuccess()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        PostMethod postMethod = new PostMethod(location);

        // expectations
        expect(mockDeviceOperationService.finalize((HttpServletRequest) EasyMock.anyObject())).andStubReturn(userId);

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
        assertTrue(resultLocation.endsWith(devicesPage));
        DeviceOperationService deviceOperationService = (DeviceOperationService) servletTestManager
                                                                                                        .getSessionAttribute(DeviceOperationService.DEVICE_OPERATION_SERVICE_ATTRIBUTE);
        assertNull(deviceOperationService);
    }
}
