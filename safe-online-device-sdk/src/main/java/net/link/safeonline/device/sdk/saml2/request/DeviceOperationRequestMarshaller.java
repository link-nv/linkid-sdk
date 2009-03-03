/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device.sdk.saml2.request;

import org.opensaml.saml2.core.impl.RequestAbstractTypeMarshaller;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.w3c.dom.Element;


/**
 * <h2>{@link DeviceOperationRequestMarshaller}<br>
 * <sub>A thread-safe Marshaller for {@link DeviceOperationRequest}.</sub></h2>
 * 
 * <p>
 * A thread-safe Marshaller for {@link DeviceOperationRequest}.
 * </p>
 * 
 * <p>
 * <i>Oct 21, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class DeviceOperationRequestMarshaller extends RequestAbstractTypeMarshaller {

    @Override
    protected void marshallAttributes(XMLObject samlObject, Element domElement)
            throws MarshallingException {

        DeviceOperationRequest req = (DeviceOperationRequest) samlObject;

        if (req.getProtocolBinding() != null) {
            domElement.setAttributeNS(null, DeviceOperationRequest.PROTOCOL_BINDING_ATTRIB_NAME, req.getProtocolBinding());
        }

        if (req.getServiceURL() != null) {
            domElement.setAttributeNS(null, DeviceOperationRequest.SERVICE_URL_ATTRIB_NAME, req.getServiceURL());
        }

        if (req.getDeviceOperation() != null) {
            domElement.setAttributeNS(null, DeviceOperationRequest.DEVICE_OPERATION_ATTRIB_NAME, req.getDeviceOperation());
        }

        if (req.getDevice() != null) {
            domElement.setAttributeNS(null, DeviceOperationRequest.DEVICE_ATTRIB_NAME, req.getDevice());
        }

        if (req.getAuthenticatedDevice() != null) {
            domElement.setAttributeNS(null, DeviceOperationRequest.AUTHENTICATED_DEVICE_ATTRIB_NAME, req.getAuthenticatedDevice());
        }

        if (req.getAttributeId() != null) {
            domElement.setAttributeNS(null, DeviceOperationRequest.ATTRIBUTE_ID_ATTRIB_NAME, req.getAttributeId());
        }

        if (req.getAttribute() != null) {
            domElement.setAttributeNS(null, DeviceOperationRequest.ATTRIBUTE_ATTRIB_NAME, req.getAttribute());
        }

        super.marshallAttributes(samlObject, domElement);
    }
}
