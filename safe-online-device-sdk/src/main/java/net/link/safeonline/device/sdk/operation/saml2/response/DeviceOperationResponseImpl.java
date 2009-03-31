/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device.sdk.operation.saml2.response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.impl.StatusResponseTypeImpl;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.util.IndexedXMLObjectChildrenList;


/**
 * <h2>{@link DeviceOperationResponseImpl}<br>
 * <sub>Concrete implementation of {@link DeviceOperationResponse}.</sub></h2>
 * 
 * <p>
 * Concrete implementation of {@link DeviceOperationResponse}.
 * </p>
 * 
 * <p>
 * <i>Oct 21, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class DeviceOperationResponseImpl extends StatusResponseTypeImpl implements DeviceOperationResponse {

    private String                                        deviceOperation;

    private String                                        device;

    private String                                        subjectName;

    /** Assertion child elements. */
    private final IndexedXMLObjectChildrenList<XMLObject> indexedChildren;


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
    protected DeviceOperationResponseImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {

        super(namespaceURI, elementLocalName, namespacePrefix);
        indexedChildren = new IndexedXMLObjectChildrenList<XMLObject>(this);
    }

    /**
     * {@inheritDoc}
     */
    public String getDeviceOperation() {

        return deviceOperation;
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

        return device;
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
    public String getSubjectName() {

        return subjectName;
    }

    /**
     * {@inheritDoc}
     */
    public void setSubjectName(String subjectName) {

        this.subjectName = prepareForAssignment(this.subjectName, subjectName);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public List<Assertion> getAssertions() {

        return (List<Assertion>) indexedChildren.subList(Assertion.DEFAULT_ELEMENT_NAME);
    }

    /** {@inheritDoc} */
    @Override
    public List<XMLObject> getOrderedChildren() {

        ArrayList<XMLObject> children = new ArrayList<XMLObject>();

        if (super.getOrderedChildren() != null) {
            children.addAll(super.getOrderedChildren());
        }

        children.addAll(indexedChildren);

        if (children.size() == 0)
            return null;

        return Collections.unmodifiableList(children);
    }

}
