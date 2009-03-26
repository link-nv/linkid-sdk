/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device.sdk.operation.saml2.request.device;

import org.opensaml.common.impl.AbstractSAMLObjectBuilder;
import org.opensaml.common.xml.SAMLConstants;


/**
 * <h2>{@link AuthenticatedDeviceBuilder}<br>
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
public class AuthenticatedDeviceBuilder extends AbstractSAMLObjectBuilder<AuthenticatedDevice> {

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthenticatedDevice buildObject() {

        return buildObject(SAMLConstants.SAML20P_NS, AuthenticatedDevice.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthenticatedDevice buildObject(String namespaceURI, String localName, String namespacePrefix) {

        return new AuthenticatedDeviceImpl(namespaceURI, localName, namespacePrefix);
    }

}
