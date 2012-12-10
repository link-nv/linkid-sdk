package net.link.safeonline.sdk.auth.protocol.saml2.subjectattributes;

import org.opensaml.saml2.core.impl.AttributeStatementImpl;


public class SubjectAttributesImpl extends AttributeStatementImpl implements SubjectAttributes {

    /**
     * Constructor
     *
     * @param namespaceURI     the namespace the element is in
     * @param elementLocalName the local name of the XML element this Object represents
     * @param namespacePrefix  the prefix for the given namespace
     */
    public SubjectAttributesImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {

        super( namespaceURI, elementLocalName, namespacePrefix );
    }
}
