/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device.sdk.saml2.request;

import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.impl.RequestAbstractTypeUnmarshaller;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;
import org.w3c.dom.Attr;


/**
 * <h2>{@link DeviceOperationRequestUnmarshaller}<br>
 * <sub>A thread-safe Unmarshaller for {@link DeviceOperationRequest}.</sub></h2>
 * 
 * <p>
 * A thread-safe Unmarshaller for {@link DeviceOperationRequest}.
 * </p>
 * 
 * <p>
 * <i>Oct 21, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class DeviceOperationRequestUnmarshaller extends RequestAbstractTypeUnmarshaller {

    @Override
    protected void processAttribute(XMLObject samlObject, Attr attribute)
            throws UnmarshallingException {

        DeviceOperationRequest req = (DeviceOperationRequest) samlObject;

        if (attribute.getLocalName().equals(DeviceOperationRequest.DEVICE_OPERATION_ATTRIB_NAME)) {
            req.setDeviceOperation(attribute.getValue());
        } else if (attribute.getLocalName().equals(DeviceOperationRequest.PROTOCOL_BINDING_ATTRIB_NAME)) {
            req.setProtocolBinding(attribute.getValue());
        } else if (attribute.getLocalName().equals(DeviceOperationRequest.SERVICE_URL_ATTRIB_NAME)) {
            req.setServiceURL(attribute.getValue());
        } else if (attribute.getLocalName().equals(DeviceOperationRequest.DEVICE_ATTRIB_NAME)) {
            req.setDevice(attribute.getValue());
        } else if (attribute.getLocalName().equals(DeviceOperationRequest.AUTHENTICATED_DEVICE_ATTRIB_NAME)) {
            req.setAuthenticatedDevice(attribute.getValue());
        } else if (attribute.getLocalName().equals(DeviceOperationRequest.ATTRIBUTE_ATTRIB_NAME)) {
            req.setAttribute(attribute.getValue());
        } else {
            super.processAttribute(samlObject, attribute);
        }
    }

    @Override
    protected void processChildElement(XMLObject parentSAMLObject, XMLObject childSAMLObject)
            throws UnmarshallingException {

        DeviceOperationRequest req = (DeviceOperationRequest) parentSAMLObject;

        if (childSAMLObject instanceof Subject) {
            req.setSubject((Subject) childSAMLObject);
        } else {
            super.processChildElement(parentSAMLObject, childSAMLObject);
        }
    }
}
