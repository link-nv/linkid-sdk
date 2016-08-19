/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.saml2;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.link.safeonline.sdk.api.attribute.LinkIDAttribute;
import net.link.safeonline.sdk.api.auth.LinkIDAuthenticationContext;
import net.link.safeonline.sdk.api.auth.LinkIDAuthnResponse;
import net.link.safeonline.sdk.api.ws.LinkIDWebServiceConstants;
import net.link.safeonline.sdk.auth.protocol.saml2.externalcode.LinkIDExternalCodeResponse;
import net.link.safeonline.sdk.auth.protocol.saml2.paymentresponse.LinkIDPaymentResponse;
import net.link.safeonline.sdk.auth.util.LinkIDDeviceContextUtils;
import net.link.util.InternalInconsistencyException;
import net.link.util.saml.Saml2Utils;
import net.link.util.saml.SamlUtils;
import net.link.util.util.ConversionUtils;
import org.jetbrains.annotations.Nullable;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.Namespace;
import org.opensaml.xml.NamespaceManager;
import org.opensaml.xml.XMLObject;
import org.w3c.dom.Element;


/**
 * <h2>{@link LinkIDSaml2Utils}<br> <sub>Utility class for SAML.</sub></h2>
 * <p/>
 * <p> Utility class for SAML. </p>
 * <p/>
 * <p> <i>Dec 16, 2008</i> </p>
 *
 * @author wvdhaute
 */
public abstract class LinkIDSaml2Utils extends Saml2Utils {

    /**
     * Generate a SAML v2.0 authentication request out of the specified linkID authentication context
     *
     * @param authenticationContext the linkID authentication context
     *
     * @return the generated SAML v2.0 request
     */
    public static AuthnRequest generate(final LinkIDAuthenticationContext authenticationContext) {

        Map<String, String> deviceContextMap = LinkIDDeviceContextUtils.generate( authenticationContext.getAuthenticationMessage(),
                authenticationContext.getFinishedMessage(), authenticationContext.getIdentityProfile(), authenticationContext.getSessionExpiryOverride(),
                authenticationContext.getTheme(), authenticationContext.getMobileLandingSuccess(), authenticationContext.getMobileLandingError(),
                authenticationContext.getMobileLandingCancel(), authenticationContext.getNotificationLocation() );

        return LinkIDAuthnRequestFactory.createAuthnRequest( authenticationContext.getApplicationName(), null, deviceContextMap,
                authenticationContext.getAttributeSuggestions(), authenticationContext.getPaymentContext(), authenticationContext.getCallback() );
    }

    /**
     * Parses the SAML v2.0 response
     *
     * @param response the SAML v2.0 response
     */
    public static LinkIDAuthnResponse parse(final Response response) {

        String userId = null;
        Map<String, List<LinkIDAttribute<Serializable>>> attributes = Maps.newHashMap();
        if (!response.getAssertions().isEmpty()) {
            Assertion assertion = response.getAssertions().get( 0 );
            userId = assertion.getSubject().getNameID().getValue();
            attributes.putAll( getAttributeValues( assertion ) );
        }

        net.link.safeonline.sdk.api.payment.LinkIDPaymentResponse paymentResponse = findPaymentResponse( response );
        net.link.safeonline.sdk.api.externalcode.LinkIDExternalCodeResponse externalCodeResponse = findExternalCodeResponse( response );

        return new LinkIDAuthnResponse( userId, attributes, paymentResponse, externalCodeResponse );
    }

    public static <X extends XMLObject> X unmarshall(Element xmlElement) {

        X xmlObject = SamlUtils.unmarshall( xmlElement );
        NamespaceManager xmlObjectNSM = new NamespaceManager( xmlObject );
        xmlObjectNSM.registerNamespace(
                new Namespace( LinkIDWebServiceConstants.SAFE_ONLINE_SAML_NAMESPACE, LinkIDWebServiceConstants.SAFE_ONLINE_SAML_PREFIX ) );

        return xmlObject;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, List<LinkIDAttribute<Serializable>>> getAttributeValues(Assertion assertion) {

        Map<String, List<LinkIDAttribute<Serializable>>> attributeMap = Maps.newHashMap();
        List<AttributeStatement> attrStatements = assertion.getAttributeStatements();
        if (attrStatements == null || attrStatements.isEmpty())
            return ImmutableMap.of();

        AttributeStatement attributeStatement = attrStatements.get( 0 );

        for (Attribute attribute : attributeStatement.getAttributes()) {

            LinkIDAttribute<Serializable> linkIDAttribute = findAttributeSDK( attribute );
            if (null != linkIDAttribute) {

                List<LinkIDAttribute<Serializable>> attributes = attributeMap.get( linkIDAttribute.getName() );
                if (null == attributes) {
                    attributes = new LinkedList<>();
                }
                attributes.add( linkIDAttribute );
                attributeMap.put( linkIDAttribute.getName(), attributes );
            }
        }

        return attributeMap;
    }

    @Nullable
    private static LinkIDAttribute<Serializable> findAttributeSDK(Attribute attributeType) {

        String attributeId = attributeType.getUnknownAttributes().get( LinkIDWebServiceConstants.ATTRIBUTE_ID );
        LinkIDAttribute<Serializable> attribute = new LinkIDAttribute<>( attributeId, attributeType.getName(), null );

        List<XMLObject> attributeValues = attributeType.getAttributeValues();
        if (attributeValues.isEmpty())
            return attribute;

        XMLObject xmlValue = attributeValues.get( 0 );
        if (null != xmlValue.getOrderedChildren() && !xmlValue.getOrderedChildren().isEmpty()) {
            // old compound, ignore
            return null;
        } else {
            // single/multi valued
            attribute.setValue( toJavaObject( xmlValue ) );
        }

        return attribute;
    }

    @Nullable
    public static net.link.safeonline.sdk.api.payment.LinkIDPaymentResponse findPaymentResponse(final Response samlResponse) {

        if (null == samlResponse.getExtensions())
            return null;

        if (null == samlResponse.getExtensions().getUnknownXMLObjects( LinkIDPaymentResponse.DEFAULT_ELEMENT_NAME ))
            return null;

        List<XMLObject> paymentResponses = samlResponse.getExtensions().getUnknownXMLObjects( LinkIDPaymentResponse.DEFAULT_ELEMENT_NAME );
        if (paymentResponses.size() > 1) {
            logger.err( "Only 1 PaymentResponse in the Response extensions element is supported" );
            throw new InternalInconsistencyException( "Failed to parse SAML2 response: Only 1 PaymentResponse in the Response extensions element is supported",
                    null );
        }

        if (paymentResponses.isEmpty()) {
            return null;
        }

        LinkIDPaymentResponse paymentResponse = (LinkIDPaymentResponse) paymentResponses.get( 0 );
        Map<String, String> paymentResponseMap = Maps.newHashMap();
        for (Attribute attribute : paymentResponse.getAttributes()) {
            String name = attribute.getName();
            List<XMLObject> attributeValues = attribute.getAttributeValues();
            if (!attributeValues.isEmpty()) {
                paymentResponseMap.put( name, ConversionUtils.toString( SamlUtils.toJavaObject( attributeValues.get( 0 ) ) ) );
            }
        }
        return net.link.safeonline.sdk.api.payment.LinkIDPaymentResponse.fromMap( paymentResponseMap );
    }

    @Nullable
    public static net.link.safeonline.sdk.api.externalcode.LinkIDExternalCodeResponse findExternalCodeResponse(final Response samlResponse) {

        if (null == samlResponse.getExtensions())
            return null;

        if (null == samlResponse.getExtensions().getUnknownXMLObjects( LinkIDExternalCodeResponse.DEFAULT_ELEMENT_NAME ))
            return null;

        List<XMLObject> externalCodeResponses = samlResponse.getExtensions().getUnknownXMLObjects( LinkIDExternalCodeResponse.DEFAULT_ELEMENT_NAME );
        if (externalCodeResponses.size() > 1) {
            logger.err( "Only 1 ExternalCodeResponse in the Response extensions element is supported" );
            throw new InternalInconsistencyException(
                    "Failed to parse SAML2 response: Only 1 ExternalCodeResponse in the Response extensions element is supported", null );
        }

        if (externalCodeResponses.isEmpty()) {
            return null;
        }

        LinkIDExternalCodeResponse linkIDExternalCodeResponse = (LinkIDExternalCodeResponse) externalCodeResponses.get( 0 );
        Map<String, String> externalCodeResponseMap = Maps.newHashMap();
        for (Attribute attribute : linkIDExternalCodeResponse.getAttributes()) {
            String name = attribute.getName();
            List<XMLObject> attributeValues = attribute.getAttributeValues();
            if (!attributeValues.isEmpty()) {
                externalCodeResponseMap.put( name, ConversionUtils.toString( SamlUtils.toJavaObject( attributeValues.get( 0 ) ) ) );
            }
        }
        return net.link.safeonline.sdk.api.externalcode.LinkIDExternalCodeResponse.fromMap( externalCodeResponseMap );
    }
}
