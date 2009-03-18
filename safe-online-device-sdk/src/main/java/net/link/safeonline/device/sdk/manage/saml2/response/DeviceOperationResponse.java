/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device.sdk.manage.saml2.response;

import java.util.List;

import javax.xml.namespace.QName;

import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.StatusResponseType;


/**
 * <h2>{@link DeviceOperationResponse}<br>
 * <sub>Device Operation Response Message</sub></h2>
 * 
 * <p>
 * Device Operation Response Message.
 * </p>
 * 
 * <p>
 * <i>Oct 21, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public interface DeviceOperationResponse extends StatusResponseType {

    /** Element local name. */
    public static final String DEFAULT_ELEMENT_LOCAL_NAME   = "DeviceOperationResponse";

    /** Default element name. */
    public static final QName  DEFAULT_ELEMENT_NAME         = new QName(SAMLConstants.SAML20P_NS, DEFAULT_ELEMENT_LOCAL_NAME,
                                                                    SAMLConstants.SAML20P_PREFIX);

    /** Local name of the XSI type. */
    public static final String TYPE_LOCAL_NAME              = "DeviceOperationResponseType";

    /** QName of the XSI type. */
    public static final QName  TYPE_NAME                    = new QName(SAMLConstants.SAML20P_NS, TYPE_LOCAL_NAME,
                                                                    SAMLConstants.SAML20P_PREFIX);

    /** Device Operation attribute name. */
    public static final String DEVICE_OPERATION_ATTRIB_NAME = "DeviceOperation";

    /** Device attribute name. */
    public static final String DEVICE_ATTRIB_NAME           = "Device";

    /** Subject attribute name. */
    public static final String SUBJECT_ATTRIB_NAME          = "Subject";

    /** URI for Failed status code. */
    public static final String FAILED_URI                   = "urn:net:lin-k:safe-online:SAML:2.0:status:Failed";


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
     * Gets the name of the subject of this request.
     * 
     * @return the value of the Subject attribute
     */
    public String getSubjectName();

    /**
     * Sets the name of the subject of this request.
     * 
     * @param subjectName
     *            the new value of the Subject attribute
     */
    public void setSubjectName(String subjectName);

    /**
     * Return the list of Assertion child elements.
     * 
     * @return the list of Assertion child elements
     */
    public List<Assertion> getAssertions();
}
