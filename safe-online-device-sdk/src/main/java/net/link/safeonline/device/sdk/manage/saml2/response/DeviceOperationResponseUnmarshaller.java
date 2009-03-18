/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device.sdk.manage.saml2.response;

import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.impl.StatusResponseTypeUnmarshaller;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;
import org.w3c.dom.Attr;


/**
 * <h2>{@link DeviceOperationResponseUnmarshaller}<br>
 * <sub>A thread-safe Unmarshaller for {@link DeviceOperationResponse}.</sub></h2>
 * 
 * <p>
 * A thread-safe Unmarshaller for {@link DeviceOperationResponse}.
 * </p>
 * 
 * <p>
 * <i>Oct 21, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class DeviceOperationResponseUnmarshaller extends StatusResponseTypeUnmarshaller {

    @Override
    protected void processAttribute(XMLObject samlObject, Attr attribute)
            throws UnmarshallingException {

        DeviceOperationResponse response = (DeviceOperationResponse) samlObject;

        if (attribute.getLocalName().equals(DeviceOperationResponse.DEVICE_OPERATION_ATTRIB_NAME)) {
            response.setDeviceOperation(attribute.getValue());
        } else if (attribute.getLocalName().equals(DeviceOperationResponse.DEVICE_ATTRIB_NAME)) {
            response.setDevice(attribute.getValue());
        } else if (attribute.getLocalName().equals(DeviceOperationResponse.SUBJECT_ATTRIB_NAME)) {
            response.setSubjectName(attribute.getValue());
        } else {
            super.processAttribute(samlObject, attribute);
        }
    }

    @Override
    protected void processChildElement(XMLObject parentSAMLObject, XMLObject childSAMLObject)
            throws UnmarshallingException {

        DeviceOperationResponse resp = (DeviceOperationResponse) parentSAMLObject;

        if (childSAMLObject instanceof Assertion) {
            resp.getAssertions().add((Assertion) childSAMLObject);
        } else {
            super.processChildElement(parentSAMLObject, childSAMLObject);
        }
    }

}
