/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.saml2.externalcode;

import org.opensaml.common.impl.AbstractSAMLObjectBuilder;
import org.opensaml.common.xml.SAMLConstants;


public class LinkIDExternalCodeResponseBuilder extends AbstractSAMLObjectBuilder<LinkIDExternalCodeResponse> {

    @Override
    public LinkIDExternalCodeResponse buildObject() {

        return buildObject( SAMLConstants.SAML20_NS, LinkIDExternalCodeResponse.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX );
    }

    @Override
    public LinkIDExternalCodeResponse buildObject(String namespaceURI, String localName, String namespacePrefix) {

        return new LinkIDExternalCodeResponseImpl( namespaceURI, localName, namespacePrefix );
    }
}
