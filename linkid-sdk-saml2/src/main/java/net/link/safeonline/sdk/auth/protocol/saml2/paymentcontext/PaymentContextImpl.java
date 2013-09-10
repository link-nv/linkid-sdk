package net.link.safeonline.sdk.auth.protocol.saml2.paymentcontext;

import org.opensaml.saml2.core.impl.AttributeStatementImpl;


public class PaymentContextImpl extends AttributeStatementImpl implements PaymentContext {

    /**
     * Constructor
     *
     * @param namespaceURI     the namespace the element is in
     * @param elementLocalName the local name of the XML element this Object represents
     * @param namespacePrefix  the prefix for the given namespace
     */
    public PaymentContextImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {

        super( namespaceURI, elementLocalName, namespacePrefix );
    }
}
