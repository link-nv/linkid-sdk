/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device.sdk.auth.saml2;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <h2>{@link DeviceManager}<br>
 * <sub>Device Operation Manager for servlet container based web applications.</sub></h2>
 * 
 * <p>
 * Device Operation Manager for servlet container based web applications. The device operation request info is saved on
 * the HTTP session.
 * </p>
 * 
 * <p>
 * <i>Oct 22, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class DeviceManager {

    private static final Log   LOG                                = LogFactory.getLog(DeviceManager.class);

    public static final String APPLICATION_ID_SESSION_ATTRIBUTE   = "applicationId";

    public static final String APPLICATION_NAME_SESSION_ATTRIBUTE = "applicationName";


    private DeviceManager() {

        // empty
    }

    /**
     * Sets the applicationId.
     * 
     * @param applicationId
     * @param httpRequest
     */
    public static void setApplicationId(String applicationId, HttpServletRequest httpRequest) {

        LOG.debug("setting applicationId: " + applicationId);
        HttpSession session = httpRequest.getSession();
        session.setAttribute(APPLICATION_ID_SESSION_ATTRIBUTE, applicationId);

    }

    /**
     * Sets the applicationName.
     * 
     * @param applicationName
     * @param httpRequest
     */
    public static void setApplicationName(String applicationName, HttpServletRequest httpRequest) {

        LOG.debug("setting authenticated device: " + applicationName);
        HttpSession session = httpRequest.getSession();
        session.setAttribute(APPLICATION_NAME_SESSION_ATTRIBUTE, applicationName);

    }
}
