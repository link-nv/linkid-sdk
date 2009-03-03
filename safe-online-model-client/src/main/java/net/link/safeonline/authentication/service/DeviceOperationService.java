/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service;

import javax.ejb.Local;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.NodeMappingNotFoundException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.device.sdk.saml2.DeviceOperationType;


/**
 * Device operation service interface. This service allows the authentication web application and user web application to register, remove
 * or update devices for a user. The bean behind this interface is stateful. This means that a certain method invocation pattern must be
 * respected.
 * 
 * @author wvdhaute
 */
@Local
public interface DeviceOperationService extends SafeOnlineService {

    public static final String JNDI_BINDING                       = SafeOnlineService.JNDI_PREFIX + "DeviceOperationServiceBean/local";

    /**
     * Used to store this stateful bean on the session
     */
    public static final String DEVICE_OPERATION_SERVICE_ATTRIBUTE = "DeviceOperationService";


    void abort();

    String redirect(String serviceUrl, String targetUrl, DeviceOperationType deviceOperation, String device, String authenticatedDevice,
                    String userId, String id, AttributeDO attribute)
            throws NodeNotFoundException, SubjectNotFoundException, DeviceNotFoundException;

    String finalize(HttpServletRequest request)
            throws NodeNotFoundException, ServletException, NodeMappingNotFoundException, DeviceNotFoundException, SubjectNotFoundException;
}
