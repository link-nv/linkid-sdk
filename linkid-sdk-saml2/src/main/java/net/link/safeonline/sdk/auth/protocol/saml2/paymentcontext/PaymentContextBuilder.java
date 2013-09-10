package net.link.safeonline.sdk.auth.protocol.saml2.paymentcontext;

import org.opensaml.common.impl.AbstractSAMLObjectBuilder;
import org.opensaml.common.xml.SAMLConstants;


public class PaymentContextBuilder extends AbstractSAMLObjectBuilder<PaymentContext> {

    @Override
    public PaymentContext buildObject() {

        return buildObject( SAMLConstants.SAML20_NS, PaymentContext.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX );
    }

    @Override
    public PaymentContext buildObject(String namespaceURI, String localName, String namespacePrefix) {

        return new PaymentContextImpl( namespaceURI, localName, namespacePrefix );
    }
}
