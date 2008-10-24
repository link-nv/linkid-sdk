/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device.sdk.saml2.response;

import org.opensaml.common.impl.AbstractSAMLObjectBuilder;
import org.opensaml.common.xml.SAMLConstants;


/**
 * <h2>{@link DeviceOperationResponseBuilder}<br>
 * <sub>A Builder for {@link DeviceOperationResponse} objects.</sub></h2>
 * 
 * <p>
 * A Builder for {@link DeviceOperationResponse} objects.
 * </p>
 * 
 * <p>
 * <i>Oct 21, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class DeviceOperationResponseBuilder extends AbstractSAMLObjectBuilder<DeviceOperationResponse> {

    /**
     * {@inheritDoc}
     */
    @Override
    public DeviceOperationResponse buildObject() {

        return buildObject(SAMLConstants.SAML20P_NS, DeviceOperationResponse.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeviceOperationResponse buildObject(String namespaceURI, String localName, String namespacePrefix) {

        return new DeviceOperationResponseImpl(namespaceURI, localName, namespacePrefix);
    }

}
