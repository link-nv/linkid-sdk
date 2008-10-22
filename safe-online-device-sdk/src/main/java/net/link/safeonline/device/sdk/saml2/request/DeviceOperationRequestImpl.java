/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device.sdk.saml2.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.impl.RequestAbstractTypeImpl;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.util.XMLObjectChildrenList;


/**
 * <h2>{@link DeviceOperationRequestImpl}<br>
 * <sub>Concrete implementation of {@link DeviceOperationRequest}.</sub></h2>
 * 
 * <p>
 * Concrete implementation of {@link DeviceOperationRequest}.
 * </p>
 * 
 * <p>
 * <i>Oct 20, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class DeviceOperationRequestImpl extends RequestAbstractTypeImpl implements DeviceOperationRequest {

    private String                                 deviceOperation;

    private String                                 device;

    private String                                 authenticatedDevice;

    private String                                 protocolBinding;

    private String                                 serviceURL;

    private Subject                                subject;

    private final XMLObjectChildrenList<Attribute> attributes;


    /**
     * Constructor.
     * 
     * @param namespaceURI
     *            the namespace the element is in
     * @param elementLocalName
     *            the local name of the XML element this Object represents
     * @param namespacePrefix
     *            the prefix for the given namespace
     */
    protected DeviceOperationRequestImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {

        super(namespaceURI, elementLocalName, namespacePrefix);
        this.attributes = new XMLObjectChildrenList<Attribute>(this);

    }

    /**
     * {@inheritDoc}
     */
    public String getDeviceOperation() {

        return this.deviceOperation;
    }

    /**
     * {@inheritDoc}
     */
    public void setDeviceOperation(String deviceOperation) {

        this.deviceOperation = prepareForAssignment(this.deviceOperation, deviceOperation);
    }

    /**
     * {@inheritDoc}
     */
    public String getDevice() {

        return this.device;
    }

    /**
     * {@inheritDoc}
     */
    public void setDevice(String device) {

        this.device = prepareForAssignment(this.device, device);
    }

    /**
     * {@inheritDoc}
     */
    public void setAuthenticatedDevice(String authenticatedDevice) {

        this.authenticatedDevice = prepareForAssignment(this.authenticatedDevice, authenticatedDevice);
    }

    /**
     * {@inheritDoc}
     */
    public String getAuthenticatedDevice() {

        return this.authenticatedDevice;
    }

    /**
     * {@inheritDoc}
     */
    public Subject getSubject() {

        return this.subject;
    }

    /**
     * {@inheritDoc}
     */
    public void setSubject(Subject subject) {

        this.subject = prepareForAssignment(this.subject, subject);
    }

    /**
     * {@inheritDoc}
     */
    public String getProtocolBinding() {

        return this.protocolBinding;
    }

    /**
     * {@inheritDoc}
     */
    public void setProtocolBinding(String protocolBinding) {

        this.protocolBinding = prepareForAssignment(this.protocolBinding, protocolBinding);
    }

    /**
     * {@inheritDoc}
     */
    public String getServiceURL() {

        return this.serviceURL;
    }

    /**
     * {@inheritDoc}
     */
    public void setServiceURL(String serviceURL) {

        this.serviceURL = prepareForAssignment(this.serviceURL, serviceURL);
    }

    /**
     * {@inheritDoc}
     */
    public List<Attribute> getAttributes() {

        return this.attributes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<XMLObject> getOrderedChildren() {

        ArrayList<XMLObject> children = new ArrayList<XMLObject>();

        if (super.getOrderedChildren() != null) {
            children.addAll(super.getOrderedChildren());
        }

        if (this.subject != null) {
            children.add(this.subject);
        }

        children.addAll(this.attributes);

        if (children.size() == 0)
            return null;

        return Collections.unmodifiableList(children);
    }

}
