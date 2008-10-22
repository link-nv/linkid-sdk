/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device.sdk.saml2.request;

import javax.xml.namespace.QName;

import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.RequestAbstractType;
import org.opensaml.saml2.core.Subject;


/**
 * <h2>{@link DeviceOperationRequest}<br>
 * <sub>Device Operation Request Message.</sub></h2>
 * 
 * <p>
 * Device Operation Request Message.
 * </p>
 * 
 * <p>
 * <i>Oct 20, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public interface DeviceOperationRequest extends RequestAbstractType {

    /** Element local name. */
    public static final String DEFAULT_ELEMENT_LOCAL_NAME       = "DeviceOperationRequest";

    /** Default element name. */
    public static final QName  DEFAULT_ELEMENT_NAME             = new QName(SAMLConstants.SAML20P_NS,
                                                                        DEFAULT_ELEMENT_LOCAL_NAME,
                                                                        SAMLConstants.SAML20P_PREFIX);

    /** ProtocolBinding attribute name. */
    public static final String PROTOCOL_BINDING_ATTRIB_NAME     = "ProtocolBinding";

    /** ServiceURL attribute name. */
    public static final String SERVICE_URL_ATTRIB_NAME          = "ServiceURL";

    /** Device Operation attribute name. */
    public static final String DEVICE_OPERATION_ATTRIB_NAME     = "DeviceOperation";

    /** Device attribute name. */
    public static final String DEVICE_ATTRIB_NAME               = "Device";

    /** Authenticated device attribute name. */
    public static final String AUTHENTICATED_DEVICE_ATTRIB_NAME = "AuthenticatedDevice";

    /** User attribute of device registration attribute name. */
    public static final String ATTRIBUTE_ATTRIB_NAME            = "Attribute";


    /**
     * Gets the protocol binding URI for the request.
     * 
     * @return the value of the ProtocolBinding attribute
     */
    public String getProtocolBinding();

    /**
     * Sets the protocol binding URI for the request.
     * 
     * @param protocolBinding
     *            the new value of the ProtocolBinding attribute
     */
    public void setProtocolBinding(String protocolBinding);

    /**
     * Gets the URL of the particular Service to which the response to this request should be delivered.
     * 
     * @return the value of the ServiceURL attribute
     */
    public String getServiceURL();

    /**
     * Sets the URL of the particular Service to which the response to this request should be delivered.
     * 
     * @param serviceURL
     *            the new value of the ServiceURL attribute
     */
    public void setServiceURL(String serviceURL);

    /**
     * Gets the device operation URI for the request.
     * 
     * @return the value of the ProtocolBinding attribute
     */
    public String getDeviceOperation();

    /**
     * Sets the device operation URI for the request.
     * 
     * @param deviceOperation
     *            the new value of the DeviceOperation attribute
     */
    public void setDeviceOperation(String deviceOperation);

    /**
     * Gets the name of the device of this request.
     * 
     * @return the value of the Device attribute
     */
    public String getDevice();

    /**
     * Sets the name of the device of this request.
     * 
     * @param device
     *            the new value of the Device attribute
     */
    public void setDevice(String device);

    /**
     * Gets the name of the authenticated device of this request.
     * 
     * @return the value of the AuthenticatedDevice attribute
     */
    public String getAuthenticatedDevice();

    /**
     * Sets the name of the device of this request.
     * 
     * @param authenticatedDevice
     *            the new value of the AuthenticatedDevice attribute
     */
    public void setAuthenticatedDevice(String authenticatedDevice);

    /**
     * Gets the {@link Subject} of the request.
     * 
     * @return the Subject of the request
     */
    public Subject getSubject();

    /**
     * Sets the {@link Subject} of the request.
     * 
     * @param subject
     *            the new value of the Subject of the request
     */
    public void setSubject(Subject subject);

    /**
     * Gets the optional attribute of this request. This attribute represents the OLAS user attribute that can be used
     * to specify a specific device registration.
     * 
     * @return the attribute of the request
     */
    public String getAttribute();

    /**
     * Sets the optional attribute of the request
     * 
     * @param attribute
     *            the new value of the optional attribute of the request. This attribute represents the OLAS user
     *            attribute specifying a specific device registration.
     */
    public void setAttribute(String attribute);
}
