/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device.sdk.saml2.request;

import org.opensaml.common.impl.AbstractSAMLObjectBuilder;
import org.opensaml.common.xml.SAMLConstants;


/**
 * <h2>{@link DeviceOperationRequestBuilder}<br>
 * <sub> A Builder for {@link DeviceOperationRequest} objects.</sub></h2>
 * 
 * <p>
 * A Builder for {@link DeviceOperationRequest} objects.
 * </p>
 * 
 * <p>
 * <i>Oct 20, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class DeviceOperationRequestBuilder extends AbstractSAMLObjectBuilder<DeviceOperationRequest> {

    /**
     * Constructor.
     */
    public DeviceOperationRequestBuilder() {

    }

    /** {@inheritDoc} */
    @Override
    public DeviceOperationRequest buildObject() {

        return buildObject(SAMLConstants.SAML20P_NS, DeviceOperationRequest.DEFAULT_ELEMENT_LOCAL_NAME,
                SAMLConstants.SAML20P_PREFIX);
    }

    /** {@inheritDoc} */
    @Override
    public DeviceOperationRequest buildObject(String namespaceURI, String localName, String namespacePrefix) {

        return new DeviceOperationRequestImpl(namespaceURI, localName, namespacePrefix);
    }

}
