/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device.sdk.saml2.response;

import org.opensaml.saml2.core.impl.StatusResponseTypeMarshaller;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.w3c.dom.Element;


/**
 * <h2>{@link DeviceOperationResponseMarshaller}<br>
 * <sub>A thread-safe Marshaller for {@link DeviceOperationResponse}.</sub></h2>
 * 
 * <p>
 * A thread-safe Marshaller for {@link DeviceOperationResponse}.
 * </p>
 * 
 * <p>
 * <i>Oct 21, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class DeviceOperationResponseMarshaller extends StatusResponseTypeMarshaller {

    @Override
    protected void marshallAttributes(XMLObject samlObject, Element domElement) throws MarshallingException {

        DeviceOperationResponse response = (DeviceOperationResponse) samlObject;

        if (response.getDeviceOperation() != null) {
            domElement.setAttributeNS(null, DeviceOperationResponse.DEVICE_OPERATION_ATTRIB_NAME, response
                    .getDeviceOperation());
        }

        if (response.getDevice() != null) {
            domElement.setAttributeNS(null, DeviceOperationResponse.DEVICE_ATTRIB_NAME, response.getDevice());
        }

        if (response.getSubjectName() != null) {
            domElement.setAttributeNS(null, DeviceOperationResponse.SUBJECT_ATTRIB_NAME, response.getSubjectName());
        }

        super.marshallAttributes(samlObject, domElement);
    }

}
