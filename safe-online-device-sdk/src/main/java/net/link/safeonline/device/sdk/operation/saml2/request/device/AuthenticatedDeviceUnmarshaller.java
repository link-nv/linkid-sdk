/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device.sdk.operation.saml2.request.device;

import org.opensaml.common.impl.AbstractSAMLObjectUnmarshaller;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;
import org.w3c.dom.Attr;


/**
 * <h2>{@link AuthenticatedDeviceUnmarshaller}<br>
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
public class AuthenticatedDeviceUnmarshaller extends AbstractSAMLObjectUnmarshaller {

    @Override
    protected void processAttribute(XMLObject samlObject, Attr attribute)
            throws UnmarshallingException {

        AuthenticatedDevice authenticatedDevice = (AuthenticatedDevice) samlObject;

        if (attribute.getLocalName().equals(AuthenticatedDevice.DEVICE_ATTRIB_NAME)) {
            authenticatedDevice.setDevice(attribute.getValue());
        } else {
            super.processAttribute(samlObject, attribute);
        }
    }

}
