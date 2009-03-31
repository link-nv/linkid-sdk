/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device.sdk.operation.saml2.request.device;

import javax.xml.namespace.QName;

import org.opensaml.common.SAMLObject;
import org.opensaml.common.xml.SAMLConstants;


/**
 * <h2>{@link AuthenticatedDevice}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Mar 26, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
public interface AuthenticatedDevice extends SAMLObject {

    /** Element local name. */
    public static final String DEFAULT_ELEMENT_LOCAL_NAME = "AuthenticatedDevice";

    /** Default element name. */
    public static final QName  DEFAULT_ELEMENT_NAME       = new QName(SAMLConstants.SAML20P_NS, DEFAULT_ELEMENT_LOCAL_NAME,
                                                                  SAMLConstants.SAML20P_PREFIX);

    /** Local name of the XSI type. */
    public static final String TYPE_LOCAL_NAME            = "AuthenticatedDeviceType";

    /** QName of the XSI type. */
    public static final QName  TYPE_NAME                  = new QName(SAMLConstants.SAML20_NS, TYPE_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);

    /** Device attribute name. */
    public static final String DEVICE_ATTRIB_NAME         = "Device";


    /**
     * Gets the device.
     * 
     * @return the value of the Device attribute
     */
    public String getDevice();

    /**
     * Sets the device.
     * 
     * @param device
     *            the new value of the Device attribute
     */
    public void setDevice(String device);

}
