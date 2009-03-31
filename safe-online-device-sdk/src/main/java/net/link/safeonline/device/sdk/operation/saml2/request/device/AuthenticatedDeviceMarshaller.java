/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device.sdk.operation.saml2.request.device;

import org.opensaml.common.impl.AbstractSAMLObjectMarshaller;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.w3c.dom.Element;


/**
 * <h2>{@link AuthenticatedDeviceMarshaller}<br>
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
public class AuthenticatedDeviceMarshaller extends AbstractSAMLObjectMarshaller {

    @Override
    protected void marshallAttributes(XMLObject samlObject, Element domElement)
            throws MarshallingException {

        AuthenticatedDevice authenticatedDevice = (AuthenticatedDevice) samlObject;

        if (authenticatedDevice.getDevice() != null) {
            domElement.setAttributeNS(null, AuthenticatedDevice.DEVICE_ATTRIB_NAME, authenticatedDevice.getDevice());
        }

        super.marshallAttributes(samlObject, domElement);
    }

}
