package net.link.safeonline.sdk.auth.protocol.saml2.paymentresponse;

import org.opensaml.common.impl.AbstractSAMLObjectBuilder;
import org.opensaml.common.xml.SAMLConstants;


public class PaymentResponseBuilder extends AbstractSAMLObjectBuilder<PaymentResponse> {

    @Override
    public PaymentResponse buildObject() {

        return buildObject( SAMLConstants.SAML20_NS, PaymentResponse.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX );
    }

    @Override
    public PaymentResponse buildObject(String namespaceURI, String localName, String namespacePrefix) {

        return new PaymentResponseImpl( namespaceURI, localName, namespacePrefix );
    }
}
