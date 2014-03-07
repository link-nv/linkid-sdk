/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.saml2.subjectattributes;

import org.opensaml.common.impl.AbstractSAMLObjectBuilder;
import org.opensaml.common.xml.SAMLConstants;


public class SubjectAttributesBuilder extends AbstractSAMLObjectBuilder<SubjectAttributes> {

    @Override
    public SubjectAttributes buildObject() {

        return buildObject( SAMLConstants.SAML20_NS, SubjectAttributes.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX );
    }

    @Override
    public SubjectAttributes buildObject(String namespaceURI, String localName, String namespacePrefix) {

        return new SubjectAttributesImpl( namespaceURI, localName, namespacePrefix );
    }
}
