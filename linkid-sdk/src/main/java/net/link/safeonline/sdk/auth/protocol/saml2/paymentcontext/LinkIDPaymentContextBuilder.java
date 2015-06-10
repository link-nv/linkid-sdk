/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.saml2.paymentcontext;

import org.opensaml.common.impl.AbstractSAMLObjectBuilder;
import org.opensaml.common.xml.SAMLConstants;


public class LinkIDPaymentContextBuilder extends AbstractSAMLObjectBuilder<LinkIDPaymentContext> {

    @Override
    public LinkIDPaymentContext buildObject() {

        return buildObject( SAMLConstants.SAML20_NS, LinkIDPaymentContext.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX );
    }

    @Override
    public LinkIDPaymentContext buildObject(String namespaceURI, String localName, String namespacePrefix) {

        return new LinkIDPaymentContextImpl( namespaceURI, localName, namespacePrefix );
    }
}
