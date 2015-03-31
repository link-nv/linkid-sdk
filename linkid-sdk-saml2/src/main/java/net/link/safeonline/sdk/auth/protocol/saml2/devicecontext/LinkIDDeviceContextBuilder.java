/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.saml2.devicecontext;

import org.opensaml.common.impl.AbstractSAMLObjectBuilder;
import org.opensaml.common.xml.SAMLConstants;


public class LinkIDDeviceContextBuilder extends AbstractSAMLObjectBuilder<LinkIDDeviceContext> {

    @Override
    public LinkIDDeviceContext buildObject() {

        return buildObject( SAMLConstants.SAML20_NS, LinkIDDeviceContext.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX );
    }

    @Override
    public LinkIDDeviceContext buildObject(String namespaceURI, String localName, String namespacePrefix) {

        return new LinkIDDeviceContextImpl( namespaceURI, localName, namespacePrefix );
    }
}
