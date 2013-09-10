package net.link.safeonline.sdk.auth.protocol.saml2.devicecontext;

import org.opensaml.saml2.core.impl.AttributeStatementImpl;


public class DeviceContextImpl extends AttributeStatementImpl implements DeviceContext {

    /**
     * Constructor
     *
     * @param namespaceURI     the namespace the element is in
     * @param elementLocalName the local name of the XML element this Object represents
     * @param namespacePrefix  the prefix for the given namespace
     */
    public DeviceContextImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {

        super( namespaceURI, elementLocalName, namespacePrefix );
    }
}
