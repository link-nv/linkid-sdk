/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.saml2.subjectattributes;

import org.opensaml.saml2.core.impl.AttributeStatementImpl;


public class LinkIDSubjectAttributesImpl extends AttributeStatementImpl implements LinkIDSubjectAttributes {

    /**
     * Constructor
     *
     * @param namespaceURI     the namespace the element is in
     * @param elementLocalName the local name of the XML element this Object represents
     * @param namespacePrefix  the prefix for the given namespace
     */
    public LinkIDSubjectAttributesImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {

        super( namespaceURI, elementLocalName, namespacePrefix );
    }
}
